package com.github.ivsokol.poe.policy

import com.github.ivsokol.poe.IRef
import com.github.ivsokol.poe.PolicyEntityRefEnum
import com.github.ivsokol.poe.SemVer
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
