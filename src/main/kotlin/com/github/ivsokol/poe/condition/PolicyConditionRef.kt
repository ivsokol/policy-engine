package com.github.ivsokol.poe.condition

import com.github.ivsokol.poe.IRef
import com.github.ivsokol.poe.PolicyEntityRefEnum
import com.github.ivsokol.poe.SemVer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a reference to a PolicyCondition entity.
 *
 * @property id The unique identifier for the PolicyCondition.
 * @property version The semantic version of the PolicyCondition, if applicable.
 */
@Serializable
@SerialName("PolicyConditionRef")
data class PolicyConditionRef(override val id: String, override val version: SemVer? = null) :
    IRef, IPolicyConditionRefOrValue {
  override val refType: PolicyEntityRefEnum = PolicyEntityRefEnum.POLICY_CONDITION_REF

  init {
    require(id.isNotBlank()) { "PolicyConditionRef -> ID must not be blank" }
  }
}
