package io.github.ivsokol.poe.policy

import io.github.ivsokol.poe.*
import io.github.ivsokol.poe.action.IPolicyAction
import io.github.ivsokol.poe.action.PolicyActionRef
import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.catalog.getRefValueFromCatalog
import io.github.ivsokol.poe.condition.IPolicyCondition
import io.github.ivsokol.poe.condition.IPolicyConditionRefOrValue
import io.github.ivsokol.poe.condition.PolicyConditionRef
import io.github.ivsokol.poe.variable.checkConstraint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

/**
 * The default value for the `strictTargetEffect` property of a [Policy] instance. When `true`, the
 * policy will strictly enforce the target effect, even if the condition is not met. When `false`,
 * the policy will not strictly enforce the target effect if the condition is not met.
 */
const val DEFAULT_STRICT_TARGET_EFFECT = false

/**
 * Represents a policy that can be evaluated and executed within a policy management system.
 *
 * A policy contains a condition, actions, and other metadata that define its behavior. The policy
 * can be evaluated against a given context, and if the condition is met, the associated actions can
 * be executed.
 *
 * The policy supports various configuration options, such as target effect, strict target effect,
 * and lenient constraints, which control the policy's behavior and decision-making process.
 *
 * The policy also maintains a set of child references, which represent the dependencies of the
 * policy, such as referenced conditions and actions.
 */
@Serializable
@SerialName("Policy")
data class Policy(
    override val id: String? = null,
    override val version: SemVer? = null,
    override val description: String? = null,
    override val labels: List<String>? = null,
    override val constraint: IPolicyConditionRefOrValue? = null,
    override val actions: List<PolicyActionRelationship>? = null,
    override val lenientConstraints: Boolean? = null,
    override val actionExecutionStrategy: ActionExecutionStrategyEnum? = null,
    override val ignoreErrors: Boolean? = null,
    val targetEffect: PolicyTargetEffectEnum,
    val condition: IPolicyConditionRefOrValue,
    val strictTargetEffect: Boolean? = null,
    override val priority: Int? = null,
) : IPolicy {
  @Transient private val logger = LoggerFactory.getLogger(this::class.java)

  @Transient private val marker = MarkerFactory.getMarker("Policy")

  @Transient private val idVer: String = if (version != null) "$id:$version" else id ?: ""

  @Transient private var _childRefs: MutableSet<PolicyEntityRefItem> = mutableSetOf()

  init {
    this.validateId()
    labels?.also { require(it.isNotEmpty()) { "$idVer:Labels must not be empty array" } }

    condition.also {
      when (it) {
        is PolicyConditionRef ->
            _childRefs.add(
                PolicyEntityRefItem(PolicyEntityRefEnum.POLICY_CONDITION_REF, it.id, it.version))
        is IPolicyCondition -> it.childRefs()?.also { c -> _childRefs.addAll(c) }
        else -> error("$idVer: Unsupported type for condition ${it::class.java.simpleName}")
      }
    }
    constraint?.also {
      when (it) {
        is PolicyConditionRef ->
            _childRefs.add(
                PolicyEntityRefItem(PolicyEntityRefEnum.POLICY_CONDITION_REF, it.id, it.version))
        is IPolicyCondition -> it.childRefs()?.also { c -> _childRefs.addAll(c) }
        else -> error("$idVer: Unsupported type for constraint ${it::class.java.simpleName}")
      }
    }
    actions?.onEach { ar ->
      when (ar.action) {
        is PolicyActionRef ->
            _childRefs.add(
                PolicyEntityRefItem(
                    PolicyEntityRefEnum.POLICY_ACTION_REF, ar.action.id, ar.action.version))
        is IPolicyAction -> ar.action.childRefs()?.also { c -> _childRefs.addAll(c) }
        else -> error("$idVer: Unsupported type for action ${ar.action::class.java.simpleName}")
      }
      ar.constraint?.also {
        when (ar.constraint) {
          is PolicyConditionRef ->
              _childRefs.add(
                  PolicyEntityRefItem(
                      PolicyEntityRefEnum.POLICY_CONDITION_REF,
                      ar.constraint.id,
                      ar.constraint.version))
          is IPolicyCondition -> ar.constraint.childRefs()?.also { c -> _childRefs.addAll(c) }
          else ->
              error(
                  "$idVer: Unsupported type for constraint ${ar.constraint::class.java.simpleName}")
        }
      }
    }
  }

  /**
   * Evaluates the policy based on the given context and policy catalog.
   *
   * This method first checks if the policy is cached, and if so, returns the cached result. If the
   * policy is not cached, it checks the constraint for the policy, and if the constraint is not
   * met, returns a `NOT_APPLICABLE` result.
   *
   * If the constraint is met, it resolves the policy condition and checks it against the context.
   * The result of the condition check determines the final policy result, which is then cached and
   * returned.
   *
   * @param context The context to evaluate the policy against.
   * @param policyCatalog The policy catalog to use for resolving references.
   * @return The result of evaluating the policy.
   */
  override fun evaluate(context: Context, policyCatalog: PolicyCatalog): PolicyResultEnum {
    val idVerPath = context.getFullPath(idVer)
    logger.debug(marker, "${context.id}->$idVerPath:Evaluating Policy")
    // check cache
    val cached = getFromCache(context, idVer, idVerPath, logger, marker)
    // if found in cache, return from it
    if (cached.first) return cached.second!!

    // filter root policies
    if (constraint != null && context.isRootPath(idVer)) {
      val constraintResult =
          checkConstraint(
              constraint,
              context,
              policyCatalog,
              idVerPath,
              PolicyEntityEnum.POLICY,
              logger,
              marker)
      logger.debug(
          marker, "${context.id}->$idVerPath:Constraint for policy is {}", constraintResult)
      when (constraintResult) {
        true -> Unit
        false ->
            return cacheAndReturn(
                idVer, context, PolicyResultEnum.NOT_APPLICABLE, idVerPath, logger, marker)
        null ->
            // not applicable if lenient or null
            return if (lenientConstraints ?: DEFAULT_LENIENT_CONSTRAINTS)
                cacheAndReturn(
                    idVer, context, PolicyResultEnum.NOT_APPLICABLE, idVerPath, logger, marker)
            else
            // indeterminate if strict
            cacheAndReturn(
                    idVer,
                    context,
                    PolicyResultEnum.INDETERMINATE_DENY_PERMIT,
                    idVerPath,
                    logger,
                    marker)
      }
    }

    // resolve condition
    context.addToPath("condition")
    val resolvedCondition =
        getRefValueFromCatalog<IPolicyConditionRefOrValue, IPolicyCondition, PolicyConditionRef>(
            condition, policyCatalog, idVerPath, context, PolicyEntityEnum.POLICY, logger, marker)
    context.removeLastFromPath()

    if (resolvedCondition == null)
        return cacheAndReturn(idVer, context, getIndeterminate(), idVerPath, logger, marker)

    val result =
        try {
          context.addToPath("condition")
          val conditionResult = resolvedCondition.check(context, policyCatalog)
          context.removeLastFromPath()
          when (conditionResult) {
            true ->
                // return targetEffect
                if (targetEffect == PolicyTargetEffectEnum.PERMIT) PolicyResultEnum.PERMIT
                else PolicyResultEnum.DENY
            false ->
                // if strict return opposite target effect; if not strict return NOT_APPLICABLE
                if (strictTargetEffect ?: DEFAULT_STRICT_TARGET_EFFECT)
                    if (targetEffect == PolicyTargetEffectEnum.PERMIT) PolicyResultEnum.DENY
                    else PolicyResultEnum.PERMIT
                else PolicyResultEnum.NOT_APPLICABLE
            null -> getIndeterminate()
          }
        } catch (e: Throwable) {
          // add to event
          context.event.add(
              context.id,
              PolicyEntityEnum.POLICY,
              idVerPath,
              false,
              null,
              false,
              "${e::class.java.name}:${e.message}")
          // log
          logger.error(
              marker,
              "${context.id}->$idVerPath:condition resolution threw an exception: {}",
              e.message,
              e)
          return cacheAndReturn(idVer, context, getIndeterminate(), idVerPath, logger, marker)
        }
    return cacheAndReturn(idVer, context, result, idVerPath, logger, marker)
  }

  override fun runActions(
      context: Context,
      policyCatalog: PolicyCatalog,
      policyResult: PolicyResultEnum
  ): ActionResult =
      this.runActions(
          context.getFullPath(""), context, policyCatalog, policyResult, this.logger, this.marker)

  /**
   * Determines whether the given [PolicyResultEnum] is considered a successful result based on the
   * [targetEffect] property.
   *
   * If the [targetEffect] is [PolicyTargetEffectEnum.PERMIT] and the [result] is
   * [PolicyResultEnum.PERMIT], this returns `true`. If the [targetEffect] is
   * [PolicyTargetEffectEnum.DENY] and the [result] is [PolicyResultEnum.DENY], this returns `true`.
   * Otherwise, this returns `false`.
   *
   * @param result The [PolicyResultEnum] to check for success.
   * @return `true` if the [result] is considered a successful outcome, `false` otherwise.
   */
  override fun isSuccess(result: PolicyResultEnum): Boolean {
    if (result == PolicyResultEnum.PERMIT && targetEffect == PolicyTargetEffectEnum.PERMIT)
        return true
    if (result == PolicyResultEnum.DENY && targetEffect == PolicyTargetEffectEnum.DENY) return true
    return false
  }

  override fun childRefs(): Set<PolicyEntityRefItem>? =
      if (_childRefs.isNotEmpty()) _childRefs.toSet() else null

  override fun identity(): String = idVer

  /**
   * Returns the appropriate [PolicyResultEnum] value based on the [targetEffect] property. If the
   * [targetEffect] is [PolicyTargetEffectEnum.PERMIT], this returns
   * [PolicyResultEnum.INDETERMINATE_PERMIT]. If the [targetEffect] is
   * [PolicyTargetEffectEnum.DENY], this returns [PolicyResultEnum.INDETERMINATE_DENY].
   */
  private fun getIndeterminate(): PolicyResultEnum =
      when (targetEffect) {
        PolicyTargetEffectEnum.PERMIT -> PolicyResultEnum.INDETERMINATE_PERMIT
        PolicyTargetEffectEnum.DENY -> PolicyResultEnum.INDETERMINATE_DENY
      }
}
