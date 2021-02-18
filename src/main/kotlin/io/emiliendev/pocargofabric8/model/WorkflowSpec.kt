package io.emiliendev.pocargofabric8.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.DEDUCTION
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.ObjectMeta


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = JsonDeserializer.None::class)
data class WorkflowSpec(
    val entrypoint: Any? = null,
    val arguments: Any? = null,
    val templates: List<Template> = emptyList(),
    val volumes: Any? = null,
    val serviceAccountName: Any? = null,
    val activeDeadlineSeconds: Any? = null,
    val hostNetwork: Any? = null,
    val imagePullSecrets: Any? = null,
    val ttlSecondsAfterFinished: Any? = null
) : KubernetesResource

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = DEDUCTION)
@JsonSubTypes(
    JsonSubTypes.Type(Template.TemplateContainer::class),
    JsonSubTypes.Type(Template.TemplateDAG::class),
    JsonSubTypes.Type(Template.TemplateSteps::class)
)
sealed class Template {

    abstract val arguments: Any?
    abstract val inputs: Any?
    abstract val metadata: ObjectMeta?
    abstract val name: String?
    abstract val outputs: Any?

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = JsonDeserializer.None::class)
    data class TemplateContainer(
        @JsonProperty("arguments")
        override val arguments: Any? = null,
        @JsonProperty("container")
        val container: Container?,
        @JsonProperty("inputs")
        override val inputs: Any? = null,
        @JsonProperty("metadata")
        override val metadata: ObjectMeta? = null,
        @JsonProperty("name")
        override val name: String?,
        @JsonProperty("outputs")
        override val outputs: Any? = null,
        @JsonProperty("retryStrategy")
        val retryStrategy: RetryStrategy? = null
    ) : Template()

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = JsonDeserializer.None::class)
    data class TemplateDAG(
        @JsonProperty("arguments")
        override val arguments: Any? = null,
        @JsonProperty("dag")
        val dag: DAG,
        @JsonProperty("inputs")
        override val inputs: Any? = null,
        @JsonProperty("metadata")
        override val metadata: ObjectMeta? = null,
        @JsonProperty("name")
        override val name: String?,
        @JsonProperty("outputs")
        override val outputs: Any? = null
    ) : Template()

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = JsonDeserializer.None::class)
    data class TemplateSteps(
        @JsonProperty("arguments")
        override val arguments: Any?,
        @JsonProperty("steps")
        val steps: List<List<Step?>?>?,
        @JsonProperty("inputs")
        override val inputs: Any?,
        @JsonProperty("metadata")
        override val metadata: ObjectMeta?,
        @JsonProperty("name")
        override val name: String?,
        @JsonProperty("outputs")
        override val outputs: Any?
    ) : Template()
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = JsonDeserializer.None::class)
data class Container(
    @JsonProperty("env")
    val env: List<Env?>? = null,
    @JsonProperty("image")
    val image: String?,
    @JsonProperty("imagePullPolicy")
    val imagePullPolicy: String? = null,
    @JsonProperty("name")
    val name: String?,
    @JsonProperty("resources")
    val resources: Any? = null,
    @JsonProperty("command")
    val command: List<String>? = null,
    @JsonProperty("args")
    val args: List<String>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Env(
    @JsonProperty("env")
    val name: String?,
    @JsonProperty("value")
    val value: String?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DAG(
    @JsonProperty("failFast")
    val failFast: Boolean? = null,
    @JsonProperty("target")
    val target: String? = null,
    @JsonProperty("tasks")
    val tasks: List<DAGTask> = emptyList()
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DAGTask(
//    val arguments: Arguments? = null,
//    val continueOn: ContinueOn? = null,
    @JsonProperty("arguments")
    val arguments: Any? = null,
    @JsonProperty("continueOn")
    val continueOn: Any? = null,
    @JsonProperty("dependencies")
    val dependencies: List<String>? = null,
    @JsonProperty("depends")
    val depends: String? = null,
    @JsonProperty("name")
    val name: String? = null,
    @JsonProperty("onExit")
    val onExit: String? = null,
    @JsonProperty("template")
    val template: String? = null,
    @JsonProperty("templateRef")
    val templateRef: Any? = null,
//    val templateRef: TemplateRef? = null,
    @JsonProperty("when")
    val `when`: String? = null,
    @JsonProperty("withItems")
    val withItems: List<Any>? = null,
    @JsonProperty("withParam")
    val withParam: String? = null,
    @JsonProperty("withSequence")
    val withSequence: Any? = null
//    val withSequence: Sequence? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Step(
    @JsonProperty("arguments")
    val arguments: Any? = null,
    @JsonProperty("name")
    val name: String? = null,
    @JsonProperty("template")
    val template: String? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RetryStrategy(
    val limit: Int? = null,
    val retryPolicy: String? = null,
    val backoff: BackoffStrategy? = null,
    val affinity: AffinityStrategy? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BackoffStrategy(
    val duration: String? = null,
    val factor: Int? = null,
    val maxDuration: String? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AffinityStrategy(
    val nodeAntiAffinity: List<Any>? = null
)
