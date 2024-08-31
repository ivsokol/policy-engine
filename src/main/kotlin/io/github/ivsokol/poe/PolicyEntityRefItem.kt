package io.github.ivsokol.poe

/**
 * Represents a reference to a policy entity, including the entity type, unique identifier, and
 * optional version.
 *
 * @property entity The type of policy entity being referenced.
 * @property id The unique identifier for the policy entity.
 * @property version The optional version of the policy entity.
 */
data class PolicyEntityRefItem(
    val entity: PolicyEntityRefEnum,
    val id: String,
    val version: SemVer? = null
)
