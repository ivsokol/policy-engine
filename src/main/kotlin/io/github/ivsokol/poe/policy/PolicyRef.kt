package io.github.ivsokol.poe.policy

import io.github.ivsokol.poe.IRef
import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.SemVer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a reference to a policy.
 *
 * @property id The unique identifier of the policy.
 * @property version The version of the policy, if applicable.
 */
@Serializable
@SerialName("PolicyRef")
data class PolicyRef(override val id: String, override val version: SemVer? = null) :
    IRef, IPolicyRefOrValue {
  override val refType: PolicyEntityRefEnum = PolicyEntityRefEnum.POLICY_REF

  init {
    require(id.isNotBlank()) { "PolicyRef -> ID must not be blank" }
  }
}
