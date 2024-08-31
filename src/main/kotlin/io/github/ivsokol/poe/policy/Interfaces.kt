package io.github.ivsokol.poe.policy

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.IManaged
import io.github.ivsokol.poe.PolicyEntityRefItem
import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.condition.IPolicyConditionRefOrValue
import kotlinx.serialization.Serializable

/**
 * Represents the result of executing policy actions. A `Boolean` value indicating whether the
 * actions were successful or not.
 */
typealias ActionResult = Boolean

/**
 * The default value for the `lenientConstraints` property of an [IPolicy] instance. When `true`,
 * the policy will continue executing actions even if some constraints fail. When `false`, the
 * policy will stop executing actions as soon as a constraint fails.
 */
const val DEFAULT_LENIENT_CONSTRAINTS = true

/**
 * Represents a policy that can be referenced or defined inline. This interface is used to provide a
 * common abstraction for policies that can be either referenced by ID or defined inline.
 */
@Serializable(with = IPolicyRefOrValueSerializer::class) interface IPolicyRefOrValue

@Serializable(with = IPolicySerializer::class)
interface IPolicy : IManaged, IPolicyRefOrValue {
  override val id: String?
  override val version: SemVer?
  override val description: String?
  override val labels: List<String>?

  val constraint: IPolicyConditionRefOrValue?
  val actions: List<PolicyActionRelationship>?
  val actionExecutionStrategy: ActionExecutionStrategyEnum?
  val lenientConstraints: Boolean?
  val ignoreErrors: Boolean?
  val priority: Int?

  /**
   * Evaluates the policy in the given context and returns the result.
   *
   * @param context The context in which to evaluate the policy.
   * @param policyCatalog The policy catalog to use for resolving policy references.
   * @return The result of evaluating the policy.
   */
  fun evaluate(context: Context, policyCatalog: PolicyCatalog): PolicyResultEnum

  /**
   * Determines whether the given [PolicyResultEnum] represents a successful policy evaluation.
   *
   * @param result The [PolicyResultEnum] to check.
   * @return `true` if the policy evaluation was successful, `false` otherwise.
   */
  fun isSuccess(result: PolicyResultEnum): Boolean

  /**
   * Runs the actions defined in the policy, given the provided context and policy catalog.
   *
   * @param context The context in which to execute the policy actions.
   * @param policyCatalog The policy catalog to use for resolving policy references.
   * @param policyResult The result of evaluating the policy.
   * @return The result of executing the policy actions.
   */
  fun runActions(
      context: Context,
      policyCatalog: PolicyCatalog,
      policyResult: PolicyResultEnum
  ): ActionResult

  /**
   * Returns a set of [PolicyEntityRefItem] objects representing the child policy references of this
   * policy.
   *
   * @return A set of [PolicyEntityRefItem] objects, or `null` if this policy has no child policy
   *   references.
   */
  fun childRefs(): Set<PolicyEntityRefItem>?
}
