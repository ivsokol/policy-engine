package io.github.ivsokol.poe.policy

import io.github.ivsokol.poe.condition.IPolicyConditionRefOrValue
import kotlinx.serialization.Serializable

/** The default value for the `runAction` property in a [PolicyRelationship] instance. */
const val DEFAULT_RUN_ACTION = true

/**
 * Represents a relationship between a PolicySet and its associated child Policy.
 *
 * @property constraint The condition that must be met for the policy's actions to be executed.
 * @property runAction Indicates whether the policy's actions should be executed when the constraint
 *   is met.
 * @property priority The priority of the policy. The higher the value, the higher the priority.
 *   Default is 0.
 * @property policy The policy that this relationship is associated with.
 */
@Serializable
data class PolicyRelationship(
    val policy: IPolicyRefOrValue,
    val constraint: IPolicyConditionRefOrValue? = null,
    val runAction: Boolean? = null,
    val priority: Int? = null
)
