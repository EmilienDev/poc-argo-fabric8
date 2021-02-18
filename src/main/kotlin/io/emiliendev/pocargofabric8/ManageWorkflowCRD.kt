package io.emiliendev.pocargofabric8

import io.emiliendev.pocargofabric8.model.Container
import io.emiliendev.pocargofabric8.model.DAG
import io.emiliendev.pocargofabric8.model.DAGTask
import io.emiliendev.pocargofabric8.model.RetryStrategy
import io.emiliendev.pocargofabric8.model.Template
import io.emiliendev.pocargofabric8.model.Workflow
import io.emiliendev.pocargofabric8.model.WorkflowList
import io.emiliendev.pocargofabric8.model.WorkflowSpec
import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.Watcher
import io.fabric8.kubernetes.client.WatcherException
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import io.fabric8.kubernetes.internal.KubernetesDeserializer
import java.util.UUID
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

fun main() {
    val myNamespace = "argoele"

    // -- Client Workflows --
    val client = DefaultKubernetesClient()
    val workflowCRD: CustomResourceDefinition =
        client.apiextensions().v1beta1().customResourceDefinitions()
            .withName("workflows.argoproj.io").get()

    val workflowCrdContext = CustomResourceDefinitionContext.fromCrd(workflowCRD)
    KubernetesDeserializer.registerCustomKind(
        HasMetadata.getApiVersion(Workflow::class.java),
        "Workflow",
        Workflow::class.java
    )
    val workflowClient: MixedOperation<Workflow, WorkflowList, Resource<Workflow>> =
        client.customResources(workflowCrdContext, Workflow::class.java, WorkflowList::class.java)

    // -- List Workflows --
    val workflowList: List<Workflow?> = workflowClient.inNamespace(myNamespace).list().items
    workflowList.map {
        log.info { "Workflow retrieved: $it" }
    }

    // -- Create Workflow --
    val newDagWorkflow = Workflow(
        spec = WorkflowSpec(
            entrypoint = "statis",
            templates = listOf(
                Template.TemplateContainer(
                    name = "a-cowsay",
                    container = Container(
                        image = "docker/whalesay_FAIL_:latest",
                        name = ""
                    ),
                    retryStrategy = RetryStrategy(
                        limit = 1
                    )
                ),
                Template.TemplateContainer(
                    name = "d-cowsay",
                    container = Container(
                        image = "docker/whalesay:latest",
                        name = ""
                    )
                ),
                Template.TemplateContainer(
                    name = "b-sleep",
                    container = Container(
                        image = "alpine:latest",
                        name = "",
                        command = listOf("sh", "-c"),
                        args = listOf("sleep 10; echo haha")
                    )
                ),
                Template.TemplateContainer(
                    name = "exit-2",
                    container = Container(
                        image = "alpine:latest",
                        name = "",
                        command = listOf("sh", "-c"),
                        args = listOf("echo intentional failure; exit 2")
                    ),
                ),
                Template.TemplateDAG(
                    name = "statis",
                    dag = DAG(
                        tasks = listOf(
                            DAGTask(name = "A", template = "a-cowsay"),
                            DAGTask(name = "B", template = "b-sleep", depends = "A.Succeeded"),
                            DAGTask(name = "C", template = "exit-2", depends = "A"),
                            DAGTask(name = "D", template = "d-cowsay", depends = "B"),
                            DAGTask(name = "E", template = "d-cowsay", depends = "D"),
                            DAGTask(name = "F", template = "d-cowsay", depends = "C.Failed"),
                            DAGTask(name = "G", template = "d-cowsay", depends = "E.Succeeded"),
                            DAGTask(name = "G-bis", template = "d-cowsay", depends = "E.Failed"),
                            DAGTask(name = "G-ter", template = "d-cowsay", depends = "E.Errored"),
                            DAGTask(name = "G-ter-child", template = "d-cowsay", depends = "G-ter"),
                            DAGTask(name = "H", template = "d-cowsay", depends = "G"),
                            DAGTask(name = "I", template = "a-cowsay", depends = "H.Succeeded"),
                            DAGTask(name = "I-bis", template = "d-cowsay", depends = "H.Failed"),
                            DAGTask(name = "J", template = "d-cowsay", depends = "F.Succeeded && I.Succeeded"),
                        )
                    ),
                )
            )
        )
    )
    val metadata = ObjectMeta().apply {
        name = "test-dag-${UUID.randomUUID()}"
        namespace = myNamespace
    }
    newDagWorkflow.metadata = metadata

//    val ow: ObjectWriter = ObjectMapper().writer().withDefaultPrettyPrinter()
//    println(ow.writeValueAsString(`newDagWorkflow`))

    val created = workflowClient.inNamespace(myNamespace).createOrReplace(newDagWorkflow)
    log.info { "Workflow created: $created" }

    // -- Watch Workflows --
    workflowClient.inNamespace(myNamespace).watch(object : Watcher<Workflow> {
        override fun eventReceived(action: Watcher.Action, resource: Workflow?) {
            when (action) {
                Watcher.Action.ADDED -> log.info { "Workflow ${resource?.metadata?.name} ADDED in ${resource?.metadata?.namespace}" }
                Watcher.Action.MODIFIED -> log.info { "Workflow ${resource?.metadata?.name} MODIFIED in ${resource?.metadata?.namespace}" }
                Watcher.Action.DELETED -> log.info { "Workflow ${resource?.metadata?.name} DELETED in ${resource?.metadata?.namespace}" }
                Watcher.Action.ERROR -> log.error { "An ERROR event received for workflow with name '${resource?.metadata?.name}' in ${resource?.metadata?.namespace}" }
            }
        }

        override fun onClose(cause: WatcherException?) {
            log.info { "Watcher workflows has been close because: $cause" }
        }
    })

    // -- Watch Pods --
//    client.inNamespace(myNamespace).pods().watch(object : Watcher<Pod> {
//        override fun eventReceived(action: Watcher.Action, resource: Pod?) {
//            when (action) {
//                Watcher.Action.ADDED -> log.info { "Pod ${resource?.metadata?.name} added" }
//                Watcher.Action.MODIFIED -> log.info { "Pod ${resource?.metadata?.name} modified" }
//                Watcher.Action.DELETED -> log.info { "Pod ${resource?.metadata?.name} deleted" }
//                Watcher.Action.ERROR -> log.error { "An ERROR event received for pod with name '${resource?.metadata?.name}'" }
//            }
//        }
//
//        override fun onClose(cause: WatcherException?) {
//            log.info { "Watcher pods has been close because: $cause" }
//        }
//    })
}