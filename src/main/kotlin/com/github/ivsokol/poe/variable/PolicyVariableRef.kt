package com.github.ivsokol.poe.variable

import com.github.ivsokol.poe.IRef
import com.github.ivsokol.poe.PolicyEntityRefEnum
import com.github.ivsokol.poe.SemVer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a reference to a PolicyVariable entity.
 *
 * @property id The unique identifier of the PolicyVariable.
 * @property version The semantic version of the PolicyVariable, if applicable.
 */
@Serializable
@SerialName("PolicyVariableRef")
data class PolicyVariableRef(override val id: String, override val version: SemVer? = null) :
    IRef, IPolicyVariableRefOrValue {
  override val refType: PolicyEntityRefEnum = PolicyEntityRefEnum.POLICY_VARIABLE_REF
}