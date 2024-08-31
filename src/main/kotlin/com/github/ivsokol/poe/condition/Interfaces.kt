package com.github.ivsokol.poe.condition

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.IManaged
import com.github.ivsokol.poe.PolicyEntityRefItem
import com.github.ivsokol.poe.SemVer
import com.github.ivsokol.poe.catalog.PolicyCatalog
import kotlinx.serialization.Serializable

typealias OperationResult = Boolean

/** Interface for all PolicyCondition entities or references */
@Serializable(with = IPolicyConditionRefOrValueSerializer::class)
interface IPolicyConditionRefOrValue

/** Interface for all PolicyCondition entities */
@Serializable(with = IPolicyConditionSerializer::class)
interface IPolicyCondition : IManaged, IPolicyConditionRefOrValue {
  override val id: String?
  override val version: SemVer?
  override val description: String?
  override val labels: List<String>?
  /**
   * Flag that indicates if the result of the Condition should be negated (true becomes false and
   * false becomes true). If result is null, negation is not applied
   */
  val negateResult: Boolean?

  /**
   * Checks result of the Condition
   *
   * @param context - context that is used in the processing of a request
   * @return PolicyCondition result.
   */
  fun check(context: Context, policyCatalog: PolicyCatalog): Boolean?

  fun childRefs(): Set<PolicyEntityRefItem>?
}
