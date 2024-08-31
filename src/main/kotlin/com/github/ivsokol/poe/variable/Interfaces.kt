package com.github.ivsokol.poe.variable

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.IManaged
import com.github.ivsokol.poe.PolicyEntityRefItem
import com.github.ivsokol.poe.SemVer
import com.github.ivsokol.poe.catalog.PolicyCatalog
import kotlinx.serialization.Serializable

/** Interface for all PolicyVariable entities or references */
@Serializable(with = IPolicyVariableRefOrValueSerializer::class)
interface IPolicyVariableRefOrValue

/**
 * Defines the interface for all PolicyVariable entities or references. PolicyVariable entities
 * represent variables that can be used in policy definitions. The interface provides methods for
 * resolving the value of the variable and accessing any child policy entity references.
 *
 * @param context The context used in the processing of a request.
 * @param policyCatalog The policy catalog used to resolve the variable.
 * @return The resolved value of the variable.
 * @return The set of child policy entity references for this policy variable, or null if there are
 *   none.
 * @property id The unique identifier for the policy variable.
 * @property version The semantic version of the policy variable.
 * @property description The description of the policy variable.
 * @property labels The labels associated with the policy variable.
 * @property type The type of the variable value.
 * @property format The format of the variable value.
 * @property timeFormat The format for time values.
 * @property dateFormat The format for date values.
 * @property dateTimeFormat The format for date-time values.
 */
@Serializable(with = IPolicyVariableSerializer::class)
interface IPolicyVariable : IManaged, IPolicyVariableRefOrValue {
  override val id: String?
  override val version: SemVer?
  override val description: String?
  override val labels: List<String>?

  /** The type of the variable value. */
  val type: VariableValueTypeEnum?

  /** The format of the variable value. */
  val format: VariableValueFormatEnum?

  /** The format for time values. */
  val timeFormat: String?
  /** The format for date values. */
  val dateFormat: String?
  /** The format for date-time values. */
  val dateTimeFormat: String?

  /**
   * Resolves value of the variable
   *
   * @param context - context that is used in the processing of a request
   * @return resolved value
   */
  fun resolve(context: Context, policyCatalog: PolicyCatalog): VariableValue

  /**
   * Returns a set of child policy entity references for this policy variable.
   *
   * @return a set of child policy entity references, or null if there are none.
   */
  fun childRefs(): Set<PolicyEntityRefItem>?
}

/** Interface for all PolicyVariableResolver entities or references */
@Serializable(with = IPolicyVariableResolverRefOrValueSerializer::class)
interface IPolicyVariableResolverRefOrValue
