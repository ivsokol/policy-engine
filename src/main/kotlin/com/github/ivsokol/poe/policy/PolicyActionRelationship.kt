package com.github.ivsokol.poe.policy

import com.github.ivsokol.poe.action.IPolicyActionRefOrValue
import com.github.ivsokol.poe.condition.IPolicyConditionRefOrValue
import kotlinx.serialization.Serializable

/**
 * Represents a relationship between a policy and an action, including any constraints or execution
 * modes.
 *
 * @property constraint An optional policy condition that must be met for the action to be executed.
 * @property executionMode The set of execution modes for the action.
 * @property priority The priority of the policy. The higher the value, the higher the priority.
 *   Default is 0.
 * @property action The policy action to be executed.
 */
@Serializable
data class PolicyActionRelationship(
    val constraint: IPolicyConditionRefOrValue? = null,
    val executionMode: Set<ActionExecutionModeEnum>? = null,
    val priority: Int? = null,
    val action: IPolicyActionRefOrValue
)
