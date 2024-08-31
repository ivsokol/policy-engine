package io.github.ivsokol.poe.action

import io.github.ivsokol.poe.IRef
import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.SemVer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a reference to a policy action.
 *
 * @property id The unique identifier of the policy action.
 * @property version The version of the policy action, if applicable.
 */
@Serializable
@SerialName("PolicyActionRef")
data class PolicyActionRef(override val id: String, override val version: SemVer? = null) :
    IRef, IPolicyActionRefOrValue {
  override val refType: PolicyEntityRefEnum = PolicyEntityRefEnum.POLICY_ACTION_REF

  init {
    require(id.isNotBlank()) { "PolicyActionRef -> ID must not be blank" }
  }
}
