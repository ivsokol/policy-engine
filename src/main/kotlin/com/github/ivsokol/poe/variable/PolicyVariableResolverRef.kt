package com.github.ivsokol.poe.variable

import com.github.ivsokol.poe.IRef
import com.github.ivsokol.poe.PolicyEntityRefEnum
import com.github.ivsokol.poe.SemVer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a reference to a PolicyVariableResolver.
 *
 * @property id The unique identifier of the PolicyVariableResolver.
 * @property version The semantic version of the PolicyVariableResolver.
 */
@Serializable
@SerialName("PolicyVariableResolverRef")
data class PolicyVariableResolverRef(
    override val id: String,
    override val version: SemVer? = null
) : IRef, IPolicyVariableResolverRefOrValue {
  override val refType: PolicyEntityRefEnum = PolicyEntityRefEnum.POLICY_VARIABLE_RESOLVER_REF
}
