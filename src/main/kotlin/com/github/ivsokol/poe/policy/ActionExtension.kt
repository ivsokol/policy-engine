package com.github.ivsokol.poe.policy

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.ContextStore
import com.github.ivsokol.poe.PolicyEntityEnum
import com.github.ivsokol.poe.action.IPolicyAction
import com.github.ivsokol.poe.action.IPolicyActionRefOrValue
import com.github.ivsokol.poe.action.PolicyActionRef
import com.github.ivsokol.poe.catalog.PolicyCatalog
import com.github.ivsokol.poe.catalog.getRefValueFromCatalog
import com.github.ivsokol.poe.variable.ContextStoreEnum
import com.github.ivsokol.poe.variable.checkConstraint
import org.slf4j.Logger
import org.slf4j.Marker

private val DEFAULT_ACTION_STRATEGY = ActionExecutionStrategyEnum.RUN_ALL

/** The default priority value for policy actions when no priority is specified. */
const val DEFAULT_POLICY_ACTION_RELATIONSHIP_PRIORITY = 0

const val DEFAULT_IGNORE_ERRORS =
    true // ignore errors in runAll; if false, still runs all, but reports false

/**
 * Runs the actions defined for this policy.
 *
 * This function filters the actions based on the policy result, and then executes each action
 * according to the configured action execution strategy (e.g. run all, stop on failure, rollback on
 * failure). If the action execution strategy is "until success", the function will return as soon
 * as an action succeeds. If the action execution strategy is "run all", the function will return
 * the overall result of all actions. If the action execution strategy is "rollback on failure", the
 * function will restore the data store from a snapshot if any action fails.
 *
 * @param idVerPath The identifier and version path for the policy.
 * @param context The current context for the policy execution.
 * @param policyCatalog The policy catalog used to look up actions.
 * @param policyResult The result of the policy evaluation.
 * @param logger The logger to use for logging.
 * @param marker The logging marker to use.
 * @return The overall result of running the actions.
 */
internal fun IPolicy.runActions(
    idVerPath: String,
    context: Context,
    policyCatalog: PolicyCatalog,
    policyResult: PolicyResultEnum,
    logger: Logger,
    marker: Marker
): ActionResult {
  // if no actions return true
  if (this.actions.isNullOrEmpty()) return true
  logger.debug(marker, "${context.id}->$idVerPath:Running Policy actions")
  context.addToPath("actions")
  // backup data store from context for rollback strategy
  var dataSnapshot: ContextStore? = null
  if (actionExecutionStrategy == ActionExecutionStrategyEnum.ROLLBACK_ON_FAILURE) {
    dataSnapshot = context.store(ContextStoreEnum.DATA)!!
  }
  // filter non-applicable actions by policy result
  logger.debug(marker, "${context.id}->$idVerPath:All actions size {}", this.actions!!.size)
  logger.trace(marker, "${context.id}->$idVerPath:All actions {}", this.actions)
  val filteredActions =
      this.actions!!.withIndex().filter {
        // filter by execution mode or if isSuccess
        it.value.executionMode?.shouldBeExecuted(policyResult) ?: this.isSuccess(policyResult)
      }
  logger.debug(marker, "${context.id}->$idVerPath:Filtered actions size {}", filteredActions.size)
  logger.trace(marker, "${context.id}->$idVerPath:Filtered actions {}", filteredActions)
  if (this.actions!!.size != filteredActions.size) {
    logger.debug(
        marker,
        "${context.id}->$idVerPath:Filtered OUT actions size {}",
        this.actions!!.size - filteredActions.size)
    logger.trace(
        marker,
        "${context.id}->$idVerPath:Filtered OUT actions {}",
        this.actions!! - filteredActions.toSet())
  }
  // if no applicable actions, return true
  if (filteredActions.isEmpty()) return true
  var totalActionResult =
      when (actionExecutionStrategy) {
        ActionExecutionStrategyEnum.UNTIL_SUCCESS -> false
        else -> true
      }
  try {
    for ((idx, ar) in
        filteredActions.sortedByDescending {
          it.value.priority ?: DEFAULT_POLICY_ACTION_RELATIONSHIP_PRIORITY
        }) {
      logger.debug(marker, "${context.id}->$idVerPath:Running action relationship {}", idx)
      context.addToPath(idx.toString())
      try {
        // check action constraint
        handleActionConstraint(ar, context, idVerPath, idx, policyCatalog, logger, marker)
        // check action result
        val actionResult = handleAction(ar, policyCatalog, idVerPath, context, idx, logger, marker)
        if (!actionResult &&
            (actionExecutionStrategy ?: DEFAULT_ACTION_STRATEGY) ==
                ActionExecutionStrategyEnum.RUN_ALL &&
            !(ignoreErrors ?: DEFAULT_IGNORE_ERRORS)) {
          totalActionResult = false
        }
        // for regular execution remove index from path
        context.removeLastFromPath()
      } catch (arie: ActionRelationshipItemException) {
        logger.debug(
            marker,
            "${context.id}->$idVerPath:Action {} threw item exception: {}",
            idx,
            arie.message)
        if ((actionExecutionStrategy ?: DEFAULT_ACTION_STRATEGY) ==
            ActionExecutionStrategyEnum.RUN_ALL && !(ignoreErrors ?: DEFAULT_IGNORE_ERRORS)) {
          totalActionResult = false
        }
      } finally {
        logger.debug(marker, "${context.id}->$idVerPath:Finishing action relationship {}", idx)
      }
    }
  } catch (e: Exception) {
    // remove actions
    context.removeLastFromPath()
    // return true for untilSuccess strategy and success exception
    if (actionExecutionStrategy == ActionExecutionStrategyEnum.UNTIL_SUCCESS &&
        e is ActionRelationshipSuccessException) {
      // add to event
      context.event.add(context.id, PolicyEntityEnum.POLICY_ACTION, idVerPath, true, true)
      // log
      logger.debug(marker, "${context.id}->$idVerPath:Policy action execution done")
      return true
    }
    // do rollback on rollback strategy
    if (actionExecutionStrategy == ActionExecutionStrategyEnum.ROLLBACK_ON_FAILURE) {
      checkNotNull(dataSnapshot) { "${context.id}->$idVerPath:Data snapshot is null" }
      context.rollbackDataStore(dataSnapshot)
    }

    // add to event
    context.event.add(
        context.id,
        PolicyEntityEnum.POLICY_ACTION,
        idVerPath,
        false,
        false,
        false,
        "${e::class.java.name}:${e.message}")
    // log
    logger.error(
        marker,
        "${context.id}->$idVerPath:Policy action execution threw an exception: {}",
        e.message,
        e)
    return false
  }
  // remove actions
  context.removeLastFromPath()
  // add to event
  context.event.add(
      context.id, PolicyEntityEnum.POLICY_ACTION, idVerPath, totalActionResult, totalActionResult)
  // log
  logger.debug(
      marker,
      "${context.id}->$idVerPath:Policy action execution done with result {}",
      totalActionResult)
  return totalActionResult
}

/**
 * Handles the execution of a policy action relationship.
 *
 * @param ar The policy action relationship to handle.
 * @param policyCatalog The policy catalog to use for resolving references.
 * @param idVerPath The identifier and version path for the current context.
 * @param context The current execution context.
 * @param idx The index of the action relationship.
 * @param logger The logger to use for logging.
 * @param marker The marker to use for logging.
 * @return `true` if the action was successful, `false` otherwise.
 * @throws ActionRelationshipItemException If the constraint for the action relationship is not
 *   satisfied.
 * @throws ActionRelationshipException If the action execution strategy is set to `STOP_ON_FAILURE`
 *   or `ROLLBACK_ON_FAILURE` and the action returns `false`.
 * @throws ActionRelationshipSuccessException If the action execution strategy is set to
 *   `UNTIL_SUCCESS` and the action returns `true`.
 */
internal fun IPolicy.handleAction(
    ar: PolicyActionRelationship,
    policyCatalog: PolicyCatalog,
    idVerPath: String,
    context: Context,
    idx: Int,
    logger: Logger,
    marker: Marker
): Boolean {
  val action =
      getRefValueFromCatalog<IPolicyActionRefOrValue, IPolicyAction, PolicyActionRef>(
          ar.action, policyCatalog, idVerPath, context, getEntityEnum(), logger, marker)
  if (action == null) {
    context.event.add(
        context.id,
        this.getEntityEnum(),
        "$idVerPath/actions/$idx",
        false,
        null,
        false,
        "Action ${ar.action} not found in catalog")
    context.removeLastFromPath()
    handleActionFailure(
        this.actionExecutionStrategy ?: DEFAULT_ACTION_STRATEGY,
        "${context.id}->$idVerPath:Action ${ar.action} not found on $idx")
  }
  val actionResult = action!!.run(context, policyCatalog)
  logger.debug(marker, "${context.id}->$idVerPath:Action {} returned {}", idx, actionResult)

  if (actionExecutionStrategy == ActionExecutionStrategyEnum.UNTIL_SUCCESS && actionResult) {
    logger.debug(marker, "${context.id}->$idVerPath:Action {} success, returning true", idx)
    // remove idx
    context.removeLastFromPath()
    throw ActionRelationshipSuccessException(
        "${context.id}->$idVerPath:Action $idx success, return")
  }
  if (!actionResult &&
      (actionExecutionStrategy == ActionExecutionStrategyEnum.STOP_ON_FAILURE ||
          actionExecutionStrategy == ActionExecutionStrategyEnum.ROLLBACK_ON_FAILURE)) {
    // remove idx
    context.removeLastFromPath()
    logger.debug(
        marker, "${context.id}->$idVerPath:Action {} false, stopping action execution", idx)
    throw ActionRelationshipException(
        "${context.id}->$idVerPath:Action $idx false, stopping action execution")
  }
  return actionResult
}

/**
 * Checks the constraint for a policy action relationship and handles the result.
 *
 * @param ar The policy action relationship to check the constraint for.
 * @param context The context for the policy execution.
 * @param idVerPath The ID version path for the policy.
 * @param idx The index of the action relationship.
 * @param policyCatalog The policy catalog.
 * @param logger The logger to use for logging.
 * @param marker The marker to use for logging.
 * @throws ActionRelationshipItemException If the constraint is not satisfied or is null and
 *   lenientConstraints is false.
 */
internal fun IPolicy.handleActionConstraint(
    ar: PolicyActionRelationship,
    context: Context,
    idVerPath: String,
    idx: Int,
    policyCatalog: PolicyCatalog,
    logger: Logger,
    marker: Marker
) {
  ar.constraint?.also {
    logger.debug(
        marker, "${context.id}->$idVerPath:Checking constraint for action relationship {}", idx)
    val constraintResult =
        checkConstraint(it, context, policyCatalog, idVerPath, getEntityEnum(), logger, marker)
    logger.debug(
        marker,
        "${context.id}->$idVerPath:Constraint for action relationship {} value {}",
        idx,
        constraintResult)
    when (constraintResult) {
      true -> Unit
      false -> {
        context.removeLastFromPath()
        throw ActionRelationshipItemException(
            "${context.id}->$idVerPath:Constraint not satisfied for action relationship $idx")
      }
      null -> {
        // remove idx
        context.removeLastFromPath()
        if (lenientConstraints != false)
            throw ActionRelationshipItemException(
                "${context.id}->$idVerPath:Constraint null, skipping action $idx")
        else
            handleActionFailure(
                this.actionExecutionStrategy ?: DEFAULT_ACTION_STRATEGY,
                "${context.id}->$idVerPath:Constraint null for action relationship $idx")
      }
    }
  }
}

/**
 * Handles the failure of an action relationship by throwing the appropriate exception based on the
 * configured action execution strategy.
 *
 * @param actionExecutionStrategy The action execution strategy to use when handling the failure.
 * @param message The error message to include in the exception.
 * @throws ActionRelationshipItemException If the action execution strategy is
 *   [ActionExecutionStrategyEnum.RUN_ALL] or [ActionExecutionStrategyEnum.UNTIL_SUCCESS].
 * @throws ActionRelationshipException If the action execution strategy is
 *   [ActionExecutionStrategyEnum.STOP_ON_FAILURE] or
 *   [ActionExecutionStrategyEnum.ROLLBACK_ON_FAILURE].
 */
private fun handleActionFailure(
    actionExecutionStrategy: ActionExecutionStrategyEnum,
    message: String
) {
  when (actionExecutionStrategy) {
    ActionExecutionStrategyEnum.RUN_ALL,
    ActionExecutionStrategyEnum.UNTIL_SUCCESS -> throw ActionRelationshipItemException(message)
    ActionExecutionStrategyEnum.STOP_ON_FAILURE,
    ActionExecutionStrategyEnum.ROLLBACK_ON_FAILURE -> throw ActionRelationshipException(message)
  }
}
