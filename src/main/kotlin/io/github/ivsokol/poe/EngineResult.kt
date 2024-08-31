package io.github.ivsokol.poe

import io.github.ivsokol.poe.policy.ActionResult
import io.github.ivsokol.poe.policy.PolicyResultEnum
import kotlinx.serialization.Serializable

/**
 * Represents the result of an engine execution, containing information about the evaluated
 * conditions or policies.
 *
 * @property conditions A mutable map of condition names to their evaluation results (nullable
 *   Boolean).
 * @property policies A mutable map of policy names to a pair of the policy result enum and the
 *   associated action result (nullable).
 */
@Serializable
data class EngineResult(
    val conditions: MutableMap<String, Boolean?> = mutableMapOf(),
    val policies: MutableMap<String, Pair<PolicyResultEnum, ActionResult?>> = mutableMapOf()
)
