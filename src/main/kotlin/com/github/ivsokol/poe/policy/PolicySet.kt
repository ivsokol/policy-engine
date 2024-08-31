package com.github.ivsokol.poe.policy

import com.github.ivsokol.poe.*
import com.github.ivsokol.poe.action.IPolicyAction
import com.github.ivsokol.poe.action.PolicyActionRef
import com.github.ivsokol.poe.catalog.PolicyCatalog
import com.github.ivsokol.poe.catalog.getRefValueFromCatalog
import com.github.ivsokol.poe.condition.IPolicyCondition
import com.github.ivsokol.poe.condition.IPolicyConditionRefOrValue
import com.github.ivsokol.poe.condition.PolicyConditionRef
import com.github.ivsokol.poe.variable.checkConstraint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import org.slf4j.MarkerFactory

/**
 * Constant that determines the default behavior for whether child actions should be run when
 * executing a [PolicySet]. If `true`, child actions will be run by default when executing the
 * [PolicySet]. If `false`, child actions will not be run by default. This constant can be
 * overridden at the [PolicySet] level by setting the `runChildActions` property.
 */
const val DEFAULT_RUN_CHILD_ACTIONS = false
/**
 * Constant that determines the default behavior for whether the "strict unless" logic should be
 * used when executing a [PolicySet]. If `true`, the "strict unless" logic will be used by default
 * when executing the [PolicySet]. This means that the [PolicySet] will return `INDETERMINATE` if
 * any of the child actions fail. If `false`, the "strict unless" logic will not be used by default,
 * and the [PolicySet] will return `FAIL` if any of the child actions fail. This constant can be
 * overridden at the [PolicySet] level by setting the `strictUnlessLogic` property.
 */
const val DEFAULT_STRICT_UNLESS_LOGIC = false // in unless logics return indeterminate if error
/**
 * Constant that determines the default behavior for whether the "indeterminate on action fail"
 * logic should be used when executing a [PolicySet]. If `true`, the [PolicySet] will return
 * `INDETERMINATE` if any of the child actions fail. If `false`, the [PolicySet] will return `FAIL`
 * if any of the child actions fail. This constant can be overridden at the [PolicySet] level by
 * setting the `indeterminateOnActionFail` property.
 */
const val DEFAULT_INDETERMINATE_ON_ACTION_FAIL = false

/**
 * Constant that determines the default behavior for whether the cache should be skipped when
 * executing a [PolicySet]. If `true`, the cache will be skipped by default when executing the
 * [PolicySet]. If `false`, the cache will not be skipped by default. This constant can be
 * overridden at the [PolicySet] level by setting the `skipCache` property.
 */
const val DEFAULT_SKIP_CACHE = false

/**
 * Constant that determines the default priority for policy relationships in a [PolicySet]. This
 * value is used when a [PolicyRelationship] does not specify a priority.
 */
const val DEFAULT_POLICY_RELATIONSHIP_PRIORITY = 0

/**
 * Represents a set of policies that can be evaluated together.
 *
 * A `PolicySet` is a collection of individual policies that are evaluated together using a
 * specified policy combination logic. The `PolicySet` class provides methods for evaluating the
 * policies and running their associated actions.
 *
 * The `PolicySet` class has the following properties:
 * - `id`: The unique identifier of the policy set.
 * - `version`: The version of the policy set.
 * - `description`: A description of the policy set.
 * - `labels`: A list of labels associated with the policy set.
 * - `constraint`: An optional constraint that must be satisfied for the policy set to be evaluated.
 * - `actions`: A list of actions associated with the policy set.
 * - `lenientConstraints`: A flag indicating whether the policy set should be evaluated if the
 *   constraint is null.
 * - `actionExecutionStrategy`: The strategy for executing actions in the policy set.
 * - `ignoreErrors`: A flag indicating whether errors during action execution should be ignored.
 * - `policyCombinationLogic`: The logic used to combine the policies in the policy set.
 * - `policies`: A list of individual policies that make up the policy set.
 * - `runChildActions`: A flag indicating whether actions associated with child policies should be
 *   run.
 * - `strictUnlessLogic`: A flag indicating whether the "Deny Unless Permit" and "Permit Unless
 *   Deny" policy combination logics should be strict.
 * - `indeterminateOnActionFail`: A flag indicating whether the policy set should return an
 *   indeterminate result if an action fails.
 * - `priority`: The priority of the policy set.
 * - `skipCache`: If cache should be skipped for this PolicySet
 */
@Serializable
@SerialName("PolicySet")
data class PolicySet(
    override val id: String? = null,
    override val version: SemVer? = null,
    override val description: String? = null,
    override val labels: List<String>? = null,
    override val constraint: IPolicyConditionRefOrValue? = null,
    override val actions: List<PolicyActionRelationship>? = null,
    override val lenientConstraints: Boolean? = null,
    override val actionExecutionStrategy: ActionExecutionStrategyEnum? = null,
    override val ignoreErrors: Boolean? = null,
    val policyCombinationLogic: PolicyCombinationLogicEnum,
    val policies: List<PolicyRelationship>,
    val runChildActions: Boolean? = null,
    val strictUnlessLogic: Boolean? = null,
    val indeterminateOnActionFail: Boolean? = null,
    override val priority: Int? = null,
    val skipCache: Boolean? = null
) : IPolicy {
  @Transient private val logger = LoggerFactory.getLogger(this::class.java)

  @Transient private val marker = MarkerFactory.getMarker("Policy")

  @Transient private val idVer: String = if (version != null) "$id:$version" else id ?: ""

  @Transient private var _childRefs: MutableSet<PolicyEntityRefItem> = mutableSetOf()

  init {
    this.validateId()
    labels?.also { require(it.isNotEmpty()) { "$idVer:Labels must not be empty array" } }

    constraint?.also {
      when (it) {
        is PolicyConditionRef ->
            _childRefs.add(
                PolicyEntityRefItem(PolicyEntityRefEnum.POLICY_CONDITION_REF, it.id, it.version))
        is IPolicyCondition -> it.childRefs()?.also { c -> _childRefs.addAll(c) }
        else -> error("$idVer: Unsupported type for constraint ${it::class.java.simpleName}")
      }
    }
    policies.onEach { pr ->
      when (pr.policy) {
        is PolicyRef ->
            _childRefs.add(
                PolicyEntityRefItem(
                    PolicyEntityRefEnum.POLICY_REF, pr.policy.id, pr.policy.version))
        is IPolicy -> pr.policy.childRefs()?.also { c -> _childRefs.addAll(c) }
        else -> error("$idVer: Unsupported type for policy ${pr.policy::class.java.simpleName}")
      }
      pr.constraint?.also {
        when (pr.constraint) {
          is PolicyConditionRef ->
              _childRefs.add(
                  PolicyEntityRefItem(
                      PolicyEntityRefEnum.POLICY_CONDITION_REF,
                      pr.constraint.id,
                      pr.constraint.version))
          is IPolicyCondition -> pr.constraint.childRefs()?.also { c -> _childRefs.addAll(c) }
          else ->
              error(
                  "$idVer: Unsupported type for policy relationship constraint ${pr.constraint::class.java.simpleName}")
        }
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
                  "$idVer: Unsupported type for action relationship constraint ${ar.constraint::class.java.simpleName}")
        }
      }
    }
  }

  /**
   * Evaluates the policy set and returns the overall policy result.
   *
   * This method first checks if the policy set is cached, and if so, returns the cached result. If
   * the policy set is not cached, it checks the constraint for the policy set. If the constraint is
   * false, the method returns `NOT_APPLICABLE`. If the constraint is null and `lenientConstraints`
   * is false, the method returns `INDETERMINATE_DENY_PERMIT`.
   *
   * The method then applies the specified policy combination logic (e.g. `DENY_OVERRIDES`,
   * `PERMIT_OVERRIDES`, etc.) to the policies in the policy set and returns the overall policy
   * result. If an exception is thrown during the evaluation, the method logs the exception and
   * returns `INDETERMINATE_DENY_PERMIT`.
   *
   * @param context the evaluation context
   * @param policyCatalog the policy catalog
   * @return the overall policy result
   */
  override fun evaluate(context: Context, policyCatalog: PolicyCatalog): PolicyResultEnum {
    val idVerPath = context.getFullPath(idVer)
    logger.debug(marker, "${context.id}->$idVerPath:Evaluating PolicySet")
    // check cache
    if (!(skipCache ?: DEFAULT_SKIP_CACHE)) {
      val cached = getFromCache(context, idVer, idVerPath, logger, marker)
      // if found in cache, return from it
      if (cached.first) return cached.second!!
    }

    // constraints are only applied on root level policies, on child policies they are overridden by
    // relationship constraint
    if (constraint != null && context.isRootPath(idVer)) {
      val constraintResult =
          checkConstraint(
              constraint,
              context,
              policyCatalog,
              idVerPath,
              PolicyEntityEnum.POLICY_SET,
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

    // apply logic
    val result =
        try {
          when (policyCombinationLogic) {
            PolicyCombinationLogicEnum.DENY_OVERRIDES ->
                this.handleDenyOverrides(context, policyCatalog, idVerPath)
            PolicyCombinationLogicEnum.PERMIT_OVERRIDES ->
                this.handlePermitOverrides(context, policyCatalog, idVerPath)
            PolicyCombinationLogicEnum.DENY_UNLESS_PERMIT ->
                this.handleDenyUnlessPermit(context, policyCatalog, idVerPath)
            PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY ->
                this.handlePermitUnlessDeny(context, policyCatalog, idVerPath)
            PolicyCombinationLogicEnum.FIRST_APPLICABLE ->
                this.handleFirstApplicable(context, policyCatalog, idVerPath)
            PolicyCombinationLogicEnum.ONLY_ONE_APPLICABLE ->
                this.handleOnlyOneApplicable(context, policyCatalog, idVerPath)
          }
        } catch (e: Throwable) {
          // add to event
          context.event.add(
              context.id,
              PolicyEntityEnum.POLICY_SET,
              idVerPath,
              false,
              null,
              false,
              "${e::class.java.name}:${e.message}")
          // log
          logger.error(
              marker, "${context.id}->$idVerPath:operation threw an exception: {}", e.message, e)
          return cacheAndReturn(
              idVer, context, PolicyResultEnum.INDETERMINATE_DENY_PERMIT, idVerPath, logger, marker)
        }
    return cacheAndReturn(idVer, context, result, idVerPath, logger, marker)
  }

  /**
   * Handles the "Deny Overrides" policy combination logic for a set of policies.
   *
   * This function evaluates a set of policies and determines the overall policy result based on the
   * "Deny Overrides" logic. The "Deny Overrides" logic means that if any policy evaluates to
   * "Deny", the overall result is "Deny", regardless of any other policy results.
   *
   * @param context The current evaluation context.
   * @param policyCatalog The policy catalog containing the policies to evaluate.
   * @param idVerPath The path to the current policy set.
   * @return The overall policy result based on the "Deny Overrides" logic.
   */
  private fun handleDenyOverrides(
      context: Context,
      policyCatalog: PolicyCatalog,
      idVerPath: String
  ): PolicyResultEnum {
    var hasPermit = false
    var hasDeny = false
    var hasIndeterminateDP = false
    var hasIndeterminateD = false
    var hasIndeterminateP = false
    context.addToPath("policies")
    for ((idx, it) in
        policies.withIndex().sortedByDescending {
          it.value.priority ?: DEFAULT_POLICY_RELATIONSHIP_PRIORITY
        }) {
      context.addToPath("$idx")
      // handle constraint
      val constraintResult =
          handlePolicyRelationshipConstraint(
              it, context, idVerPath, idx, policyCatalog, logger, marker)
      when (constraintResult) {
        true -> Unit
        null ->
            if (lenientConstraints != false) {
              context.removeLastFromPath()
              continue
            } else {
              hasIndeterminateDP = true
              context.removeLastFromPath()
              continue
            }
        false -> {
          context.removeLastFromPath()
          continue
        }
      }

      // get policy
      val policy =
          getRefValueFromCatalog<IPolicyRefOrValue, IPolicy, PolicyRef>(
              it.policy,
              policyCatalog,
              idVerPath,
              context,
              PolicyEntityEnum.POLICY_SET,
              logger,
              marker)
      // handle null policy
      if (policy == null) {
        context.event.add(
            context.id,
            this.getEntityEnum(),
            idVerPath,
            false,
            null,
            false,
            "Policy not found in catalog")
        logger.debug(marker, "${context.id}->$idVerPath:Policy {} not found", idx)
        hasIndeterminateDP = true
        context.removeLastFromPath()
        break
      }
      // evaluate policy
      val policyResult = policy.evaluate(context, policyCatalog)
      // run policy action,
      // action result is ignored on parent level, as actions are executed on child
      if ((runChildActions ?: DEFAULT_RUN_CHILD_ACTIONS) && (it.runAction ?: DEFAULT_RUN_ACTION)) {
        val actionResult = policy.runActions(context, policyCatalog, policyResult)
        if (!actionResult && (indeterminateOnActionFail ?: DEFAULT_INDETERMINATE_ON_ACTION_FAIL)) {
          logger.error(marker, "${context.id}->$idVerPath:Policy {} action failed", idx)
          hasIndeterminateDP = true
        }
      }
      context.removeLastFromPath()
      when (policyResult) {
        PolicyResultEnum.PERMIT -> hasPermit = true
        PolicyResultEnum.DENY -> {
          hasDeny = true
          break
        }
        PolicyResultEnum.INDETERMINATE_PERMIT -> hasIndeterminateP = true
        PolicyResultEnum.INDETERMINATE_DENY -> hasIndeterminateD = true
        PolicyResultEnum.INDETERMINATE_DENY_PERMIT -> hasIndeterminateDP = true
        PolicyResultEnum.NOT_APPLICABLE -> Unit
      }
    }
    context.removeLastFromPath()
    return when {
      hasDeny -> PolicyResultEnum.DENY
      hasIndeterminateDP -> PolicyResultEnum.INDETERMINATE_DENY_PERMIT
      hasIndeterminateD && (hasIndeterminateP || hasPermit) ->
          PolicyResultEnum.INDETERMINATE_DENY_PERMIT
      hasIndeterminateD -> PolicyResultEnum.INDETERMINATE_DENY
      hasPermit -> PolicyResultEnum.PERMIT
      hasIndeterminateP -> PolicyResultEnum.INDETERMINATE_PERMIT
      else -> PolicyResultEnum.NOT_APPLICABLE
    }
  }

  /**
   * Handles the Permit Overrides policy evaluation logic.
   *
   * This function evaluates a set of policies and determines the overall policy result based on the
   * Permit Overrides policy evaluation strategy. It iterates through the policies, evaluates each
   * policy, and handles any policy relationship constraints. The final policy result is determined
   * based on the individual policy results.
   *
   * @param context The evaluation context.
   * @param policyCatalog The policy catalog.
   * @param idVerPath The ID verification path.
   * @return The overall policy result.
   */
  private fun handlePermitOverrides(
      context: Context,
      policyCatalog: PolicyCatalog,
      idVerPath: String
  ): PolicyResultEnum {
    var hasPermit = false
    var hasDeny = false
    var hasIndeterminateDP = false
    var hasIndeterminateD = false
    var hasIndeterminateP = false
    context.addToPath("policies")
    for ((idx, it) in
        policies.withIndex().sortedByDescending {
          it.value.priority ?: DEFAULT_POLICY_RELATIONSHIP_PRIORITY
        }) {
      context.addToPath("$idx")
      // handle constraint
      val constraintResult =
          handlePolicyRelationshipConstraint(
              it, context, idVerPath, idx, policyCatalog, logger, marker)
      when (constraintResult) {
        true -> Unit
        null ->
            if (lenientConstraints != false) {
              context.removeLastFromPath()
              continue
            } else {
              hasIndeterminateDP = true
              context.removeLastFromPath()
              continue
            }
        false -> {
          context.removeLastFromPath()
          continue
        }
      }

      // get policy
      val policy =
          getRefValueFromCatalog<IPolicyRefOrValue, IPolicy, PolicyRef>(
              it.policy,
              policyCatalog,
              idVerPath,
              context,
              PolicyEntityEnum.POLICY_SET,
              logger,
              marker)
      // handle null policy
      if (policy == null) {
        context.event.add(
            context.id,
            this.getEntityEnum(),
            idVerPath,
            false,
            null,
            false,
            "Policy not found in catalog")
        logger.debug(marker, "${context.id}->$idVerPath:Policy {} not found", idx)
        hasIndeterminateDP = true
        context.removeLastFromPath()
        break
      }
      // evaluate policy
      val policyResult = policy.evaluate(context, policyCatalog)
      // run policy action
      if ((runChildActions ?: DEFAULT_RUN_CHILD_ACTIONS) && (it.runAction ?: DEFAULT_RUN_ACTION)) {
        val actionResult = policy.runActions(context, policyCatalog, policyResult)
        if (!actionResult && (indeterminateOnActionFail ?: DEFAULT_INDETERMINATE_ON_ACTION_FAIL)) {
          logger.error(marker, "${context.id}->$idVerPath:Policy {} action failed", idx)
          hasIndeterminateDP = true
        }
      }
      context.removeLastFromPath()
      when (policyResult) {
        PolicyResultEnum.PERMIT -> {
          hasPermit = true
          break
        }
        PolicyResultEnum.DENY -> hasDeny = true
        PolicyResultEnum.INDETERMINATE_PERMIT -> hasIndeterminateP = true
        PolicyResultEnum.INDETERMINATE_DENY -> hasIndeterminateD = true
        PolicyResultEnum.INDETERMINATE_DENY_PERMIT -> hasIndeterminateDP = true
        PolicyResultEnum.NOT_APPLICABLE -> Unit
      }
    }
    context.removeLastFromPath()
    return when {
      hasPermit -> PolicyResultEnum.PERMIT
      hasIndeterminateDP -> PolicyResultEnum.INDETERMINATE_DENY_PERMIT
      hasIndeterminateP && (hasIndeterminateD || hasDeny) ->
          PolicyResultEnum.INDETERMINATE_DENY_PERMIT
      hasIndeterminateP -> PolicyResultEnum.INDETERMINATE_PERMIT
      hasDeny -> PolicyResultEnum.DENY
      hasIndeterminateD -> PolicyResultEnum.INDETERMINATE_DENY
      else -> PolicyResultEnum.NOT_APPLICABLE
    }
  }

  /**
   * Evaluates a set of policies and returns the overall policy result based on the "Deny Unless
   * Permit" logic.
   *
   * This function iterates through the policies in the policy set, evaluating each policy and
   * handling any constraints or null policies. The overall policy result is determined based on the
   * following rules:
   * - If any policy returns "Permit", the overall result is "Permit".
   * - If no policy returns "Permit" and any policy returns "Invalid", the overall result is
   *   "Indeterminate Deny Permit".
   * - If no policy returns "Permit" and all policies return "Deny", the overall result is "Deny".
   *
   * @param context The current evaluation context.
   * @param policyCatalog The policy catalog used for resolving policy references.
   * @param idVerPath The identifier verification path.
   * @return The overall policy result.
   */
  private fun handleDenyUnlessPermit(
      context: Context,
      policyCatalog: PolicyCatalog,
      idVerPath: String
  ): PolicyResultEnum {
    var hasPermit = false
    var hasInvalid = false
    context.addToPath("policies")
    for ((idx, it) in
        policies.withIndex().sortedByDescending {
          it.value.priority ?: DEFAULT_POLICY_RELATIONSHIP_PRIORITY
        }) {
      context.addToPath("$idx")
      // handle constraint
      val constraintResult =
          handlePolicyRelationshipConstraint(
              it, context, idVerPath, idx, policyCatalog, logger, marker)
      when (constraintResult) {
        true -> Unit
        null ->
            if (lenientConstraints != false) {
              context.removeLastFromPath()
              continue
            } else {
              hasInvalid = true
              context.removeLastFromPath()
              continue
            }
        false -> {
          context.removeLastFromPath()
          continue
        }
      }

      // get policy
      val policy =
          getRefValueFromCatalog<IPolicyRefOrValue, IPolicy, PolicyRef>(
              it.policy,
              policyCatalog,
              idVerPath,
              context,
              PolicyEntityEnum.POLICY_SET,
              logger,
              marker)
      // handle null policy
      if (policy == null) {
        context.event.add(
            context.id,
            this.getEntityEnum(),
            idVerPath,
            false,
            null,
            false,
            "Policy not found in catalog")
        logger.debug(marker, "${context.id}->$idVerPath:Policy {} not found", idx)
        hasInvalid = true
        context.removeLastFromPath()
        break
      }
      // evaluate policy
      val policyResult = policy.evaluate(context, policyCatalog)
      // run policy action
      if ((runChildActions ?: DEFAULT_RUN_CHILD_ACTIONS) && (it.runAction ?: DEFAULT_RUN_ACTION)) {
        val actionResult = policy.runActions(context, policyCatalog, policyResult)
        if (!actionResult && (indeterminateOnActionFail ?: DEFAULT_INDETERMINATE_ON_ACTION_FAIL)) {
          logger.error(marker, "${context.id}->$idVerPath:Policy {} action failed", idx)
          hasInvalid = true
        }
      }
      context.removeLastFromPath()
      when (policyResult) {
        PolicyResultEnum.PERMIT -> {
          hasPermit = true
          break
        }
        PolicyResultEnum.DENY -> Unit
        PolicyResultEnum.INDETERMINATE_PERMIT,
        PolicyResultEnum.INDETERMINATE_DENY,
        PolicyResultEnum.NOT_APPLICABLE,
        PolicyResultEnum.INDETERMINATE_DENY_PERMIT -> hasInvalid = true
      }
      if (hasInvalid && (strictUnlessLogic ?: DEFAULT_STRICT_UNLESS_LOGIC)) {
        break
      }
    }
    context.removeLastFromPath()
    return when {
      hasPermit -> PolicyResultEnum.PERMIT
      hasInvalid && (strictUnlessLogic ?: DEFAULT_STRICT_UNLESS_LOGIC) ->
          PolicyResultEnum.INDETERMINATE_DENY_PERMIT
      else -> PolicyResultEnum.DENY
    }
  }

  /**
   * Handles the "permit unless deny" policy evaluation logic.
   *
   * This function evaluates a set of policies using the "permit unless deny" logic. It iterates
   * through the policies, evaluating each one and tracking whether any policy has resulted in a
   * "deny" or "invalid" result. The final result is determined based on the overall evaluation of
   * the policy set.
   *
   * @param context The current evaluation context.
   * @param policyCatalog The policy catalog containing the policies to evaluate.
   * @param idVerPath The ID verification path.
   * @return The final policy result.
   */
  private fun handlePermitUnlessDeny(
      context: Context,
      policyCatalog: PolicyCatalog,
      idVerPath: String
  ): PolicyResultEnum {
    var hasDeny = false
    var hasInvalid = false
    context.addToPath("policies")
    for ((idx, it) in
        policies.withIndex().sortedByDescending {
          it.value.priority ?: DEFAULT_POLICY_RELATIONSHIP_PRIORITY
        }) {
      context.addToPath("$idx")
      // handle constraint
      val constraintResult =
          handlePolicyRelationshipConstraint(
              it, context, idVerPath, idx, policyCatalog, logger, marker)
      when (constraintResult) {
        true -> Unit
        null ->
            if (lenientConstraints != false) {
              context.removeLastFromPath()
              continue
            } else {
              hasInvalid = true
              context.removeLastFromPath()
              continue
            }
        false -> {
          context.removeLastFromPath()
          continue
        }
      }

      // get policy
      val policy =
          getRefValueFromCatalog<IPolicyRefOrValue, IPolicy, PolicyRef>(
              it.policy,
              policyCatalog,
              idVerPath,
              context,
              PolicyEntityEnum.POLICY_SET,
              logger,
              marker)
      // handle null policy
      if (policy == null) {
        context.event.add(
            context.id,
            this.getEntityEnum(),
            idVerPath,
            false,
            null,
            false,
            "Policy not found in catalog")
        logger.debug(marker, "${context.id}->$idVerPath:Policy {} not found", idx)
        hasInvalid = true
        context.removeLastFromPath()
        break
      }
      // evaluate policy
      val policyResult = policy.evaluate(context, policyCatalog)
      // run policy action
      if ((runChildActions ?: DEFAULT_RUN_CHILD_ACTIONS) && (it.runAction ?: DEFAULT_RUN_ACTION)) {
        val actionResult = policy.runActions(context, policyCatalog, policyResult)
        if (!actionResult && (indeterminateOnActionFail ?: DEFAULT_INDETERMINATE_ON_ACTION_FAIL)) {
          logger.error(marker, "${context.id}->$idVerPath:Policy {} action failed", idx)
          hasInvalid = true
        }
      }
      context.removeLastFromPath()
      when (policyResult) {
        PolicyResultEnum.DENY -> {
          hasDeny = true
          break
        }
        PolicyResultEnum.PERMIT -> Unit
        PolicyResultEnum.INDETERMINATE_PERMIT,
        PolicyResultEnum.INDETERMINATE_DENY,
        PolicyResultEnum.NOT_APPLICABLE,
        PolicyResultEnum.INDETERMINATE_DENY_PERMIT -> hasInvalid = true
      }
      if (hasInvalid && (strictUnlessLogic ?: DEFAULT_STRICT_UNLESS_LOGIC)) {
        break
      }
    }
    context.removeLastFromPath()
    return when {
      hasDeny -> PolicyResultEnum.DENY
      hasInvalid && (strictUnlessLogic ?: DEFAULT_STRICT_UNLESS_LOGIC) ->
          PolicyResultEnum.INDETERMINATE_DENY_PERMIT
      else -> PolicyResultEnum.PERMIT
    }
  }

  /**
   * Handles the "first applicable" policy evaluation logic.
   *
   * This function iterates through the policies in the policy set, evaluating each policy and
   * running its actions if applicable. The function returns the first policy result that is either
   * DENY or PERMIT, or INDETERMINATE_DENY_PERMIT if no policy is applicable.
   *
   * @param context The current evaluation context.
   * @param policyCatalog The policy catalog.
   * @param idVerPath The ID verification path.
   * @return The policy result.
   */
  private fun handleFirstApplicable(
      context: Context,
      policyCatalog: PolicyCatalog,
      idVerPath: String
  ): PolicyResultEnum {
    context.addToPath("policies")
    var endResult: PolicyResultEnum = PolicyResultEnum.NOT_APPLICABLE
    for ((idx, it) in
        policies.withIndex().sortedByDescending {
          it.value.priority ?: DEFAULT_POLICY_RELATIONSHIP_PRIORITY
        }) {
      context.addToPath("$idx")
      // handle constraint
      val constraintResult =
          handlePolicyRelationshipConstraint(
              it, context, idVerPath, idx, policyCatalog, logger, marker)
      when (constraintResult) {
        true -> Unit
        null ->
            if (lenientConstraints != false) {
              context.removeLastFromPath()
              continue
            } else {
              endResult = PolicyResultEnum.INDETERMINATE_DENY_PERMIT
              context.removeLastFromPath()
              continue
            }
        false -> {
          context.removeLastFromPath()
          continue
        }
      }

      // get policy
      val policy =
          getRefValueFromCatalog<IPolicyRefOrValue, IPolicy, PolicyRef>(
              it.policy,
              policyCatalog,
              idVerPath,
              context,
              PolicyEntityEnum.POLICY_SET,
              logger,
              marker)
      // handle null policy
      if (policy == null) {
        context.event.add(
            context.id,
            this.getEntityEnum(),
            idVerPath,
            false,
            null,
            false,
            "Policy not found in catalog")
        logger.debug(
            marker, "${context.id}->$idVerPath:Policy {} not found, returning INDETERMINATE", idx)
        endResult = PolicyResultEnum.INDETERMINATE_DENY_PERMIT
        context.removeLastFromPath()
        break
      }
      // evaluate policy
      var policyResult = policy.evaluate(context, policyCatalog)
      // run policy action
      if ((runChildActions ?: DEFAULT_RUN_CHILD_ACTIONS) && (it.runAction ?: DEFAULT_RUN_ACTION)) {
        val actionResult = policy.runActions(context, policyCatalog, policyResult)
        if (!actionResult && (indeterminateOnActionFail ?: DEFAULT_INDETERMINATE_ON_ACTION_FAIL)) {
          logger.error(marker, "${context.id}->$idVerPath:Policy {} action failed", idx)
          policyResult = PolicyResultEnum.INDETERMINATE_DENY_PERMIT
        }
      }
      context.removeLastFromPath()
      if (policyResult == PolicyResultEnum.DENY || policyResult == PolicyResultEnum.PERMIT) {
        endResult = policyResult
        break
      }
    }
    context.removeLastFromPath()
    return endResult
  }

  /**
   * Handles the "Only One Applicable" policy combination logic, where only one policy in the set
   * can be applicable. This function iterates through the policies in the set, evaluating each
   * policy and keeping track of the number of applicable policies. If more than one policy is
   * applicable, the function returns an INDETERMINATE_DENY_PERMIT result. Otherwise, it returns the
   * result of the single applicable policy.
   *
   * @param context The current evaluation context.
   * @param policyCatalog The policy catalog containing the policies.
   * @param idVerPath The path to the current policy set.
   * @return The final policy result, either DENY, PERMIT, or INDETERMINATE_DENY_PERMIT.
   */
  private fun handleOnlyOneApplicable(
      context: Context,
      policyCatalog: PolicyCatalog,
      idVerPath: String
  ): PolicyResultEnum {
    context.addToPath("policies")
    var numOfApplicable = 0
    var endResult: PolicyResultEnum = PolicyResultEnum.NOT_APPLICABLE
    for ((idx, it) in
        policies.withIndex().sortedByDescending {
          it.value.priority ?: DEFAULT_POLICY_RELATIONSHIP_PRIORITY
        }) {
      context.addToPath("$idx")
      // handle constraint
      val constraintResult =
          handlePolicyRelationshipConstraint(
              it, context, idVerPath, idx, policyCatalog, logger, marker)
      when (constraintResult) {
        true -> Unit
        null ->
            if (lenientConstraints != false) {
              context.removeLastFromPath()
              continue
            } else {
              endResult = PolicyResultEnum.INDETERMINATE_DENY_PERMIT
              context.removeLastFromPath()
              continue
            }
        false -> {
          context.removeLastFromPath()
          continue
        }
      }

      // get policy
      val policy =
          getRefValueFromCatalog<IPolicyRefOrValue, IPolicy, PolicyRef>(
              it.policy,
              policyCatalog,
              idVerPath,
              context,
              PolicyEntityEnum.POLICY_SET,
              logger,
              marker)
      // handle null policy
      if (policy == null) {
        context.event.add(
            context.id,
            this.getEntityEnum(),
            idVerPath,
            false,
            null,
            false,
            "Policy not found in catalog")
        logger.debug(
            marker, "${context.id}->$idVerPath:Policy {} not found, returning INDETERMINATE", idx)
        endResult = PolicyResultEnum.INDETERMINATE_DENY_PERMIT
        context.removeLastFromPath()
        break
      }
      // evaluate policy
      var policyResult = policy.evaluate(context, policyCatalog)
      // run policy action
      if ((runChildActions ?: DEFAULT_RUN_CHILD_ACTIONS) && (it.runAction ?: DEFAULT_RUN_ACTION)) {
        val actionResult = policy.runActions(context, policyCatalog, policyResult)
        if (!actionResult && (indeterminateOnActionFail ?: DEFAULT_INDETERMINATE_ON_ACTION_FAIL)) {
          logger.error(marker, "${context.id}->$idVerPath:Policy {} action failed", idx)
          policyResult = PolicyResultEnum.INDETERMINATE_DENY_PERMIT
        }
      }
      context.removeLastFromPath()
      if (policyResult == PolicyResultEnum.DENY || policyResult == PolicyResultEnum.PERMIT) {
        numOfApplicable++
        if (numOfApplicable > 1) {
          logger.debug(
              marker,
              "${context.id}->$idVerPath:More than 1 policy applicable, returning INDETERMINATE",
              idx)
          endResult = PolicyResultEnum.INDETERMINATE_DENY_PERMIT
          break
        }
        endResult = policyResult
      }
    }
    context.removeLastFromPath()
    return endResult
  }

  override fun runActions(
      context: Context,
      policyCatalog: PolicyCatalog,
      policyResult: PolicyResultEnum
  ): ActionResult =
      this.runActions(
          context.getFullPath(""), context, policyCatalog, policyResult, this.logger, this.marker)

  /**
   * Determines whether the given [PolicyResultEnum] represents a successful policy evaluation.
   *
   * The success of the policy evaluation depends on the configured [PolicyCombinationLogicEnum]:
   * - For `DENY_UNLESS_PERMIT` and `DENY_OVERRIDES` logic, a `DENY` result is considered
   *   successful.
   * - For `PERMIT_UNLESS_DENY` and `PERMIT_OVERRIDES` logic, a `PERMIT` result is considered
   *   successful.
   * - For `FIRST_APPLICABLE` and `ONLY_ONE_APPLICABLE` logic, both `PERMIT` and `DENY` results are
   *   considered successful.
   *
   * @param result The [PolicyResultEnum] to check for success.
   * @return `true` if the given [PolicyResultEnum] represents a successful policy evaluation,
   *   `false` otherwise.
   */
  override fun isSuccess(result: PolicyResultEnum): Boolean {
    when (policyCombinationLogic) {
      PolicyCombinationLogicEnum.DENY_UNLESS_PERMIT,
      PolicyCombinationLogicEnum.DENY_OVERRIDES -> if (result == PolicyResultEnum.DENY) return true
      PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
      PolicyCombinationLogicEnum.PERMIT_OVERRIDES ->
          if (result == PolicyResultEnum.PERMIT) return true
      PolicyCombinationLogicEnum.FIRST_APPLICABLE,
      PolicyCombinationLogicEnum.ONLY_ONE_APPLICABLE ->
          if (result == PolicyResultEnum.PERMIT || result == PolicyResultEnum.DENY) return true
    }
    return false
  }

  override fun childRefs(): Set<PolicyEntityRefItem>? =
      if (_childRefs.isNotEmpty()) _childRefs.toSet() else null

  override fun identity(): String = idVer

  /**
   * Handles the policy relationship constraint for the given [PolicyRelationship].
   *
   * This function checks the constraint for the policy relationship, if one is defined. It logs the
   * constraint check result and returns the result.
   *
   * @param pr The [PolicyRelationship] to check the constraint for.
   * @param context The [Context] for the policy evaluation.
   * @param idVerPath The path to the policy relationship.
   * @param idx The index of the policy relationship.
   * @param policyCatalog The [PolicyCatalog] to use for the constraint check.
   * @param logger The [Logger] to use for logging.
   * @param marker The [Marker] to use for logging.
   * @return `true` if the constraint is satisfied, `false` if the constraint is not satisfied, or
   *   `null` if there is no constraint.
   */
  private fun handlePolicyRelationshipConstraint(
      pr: PolicyRelationship,
      context: Context,
      idVerPath: String,
      idx: Int,
      policyCatalog: PolicyCatalog,
      logger: Logger,
      marker: Marker
  ): Boolean? {
    pr.constraint?.also {
      logger.debug(
          marker, "${context.id}->$idVerPath:Checking constraint for policy relationship {}", idx)
      val constraintResult =
          checkConstraint(it, context, policyCatalog, idVerPath, getEntityEnum(), logger, marker)
      logger.debug(
          marker,
          "${context.id}->$idVerPath:Constraint for policy relationship {} returned {}",
          idx,
          constraintResult)
      return constraintResult
    }
    return true
  }
}
