package io.emiliendev.pocargofabric8.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.model.annotation.Group
import io.fabric8.kubernetes.model.annotation.Version
import java.time.ZonedDateTime

const val GROUP = "argoproj.io"
const val VERSION = "v1alpha1"

@JsonInclude(JsonInclude.Include.NON_NULL)
@Version(VERSION)
@Group(GROUP)
data class Workflow(
    var spec: WorkflowSpec? = null,
    var status: WorkflowStatus? = null
) : CustomResource<WorkflowSpec, Any?>()

@JsonInclude(JsonInclude.Include.NON_NULL)
data class WorkflowStatus(
    val nodes: Map<String, NodeStatus>? = null,
    val phase: String? = null,
    val progress: String? = null,
    val startedAt: ZonedDateTime? = null,
    val finishedAt: ZonedDateTime? = null,
    val resourcesDuration: Any? = null,
    val conditions: Any? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class NodeStatus(
    val displayName: String? = null,
    val id: String? = null,
    val name: String? = null,
    val phase: String? = null,
    val progress: String? = null,
    val startedAt: ZonedDateTime? = null,
    val finishedAt: ZonedDateTime? = null,
    val templateName: String? = null,
    val templateScope: String? = null,
    val type: String? = null,
    val children: List<String>? = null,
    val boundaryID: String? = null,
    val hostNodeName: String? = null,
    val message: String? = null,
    val outputs: Any? = null,
    val outboundNodes: Any? = null,
    val resourcesDuration: Any? = null,
)