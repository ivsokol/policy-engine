package com.github.ivsokol.poe.policy

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.PolicyEntityEnum
import com.github.ivsokol.poe.cache.PolicyStoreCacheEnum
import com.github.ivsokol.poe.condition.OperationResult
import org.slf4j.Logger
import org.slf4j.Marker

/**
 * Retrieves the policy result from the cache if available, and adds the result to the event log.
 *
 * @param context The current context.
 * @param idVer The ID and version of the policy.
 * @param idVerPath The path of the policy.
 * @param logger The logger to use for logging.
 * @param marker The marker to use for logging.
 * @return A pair containing a boolean indicating whether the policy was found in the cache, and the
 *   cached policy result (or null if not found).
 */
internal fun IPolicy.getFromCache(
    context: Context,
    idVer: String,
    idVerPath: String,
    logger: Logger,
    marker: Marker,
): Pair<OperationResult, PolicyResultEnum?> {
  if (idVer.isNotBlank() && context.cache.hasKey(PolicyStoreCacheEnum.POLICY, idVer)) {
    val cached = context.cache.getPolicy(idVer)!!
    // add to event
    context.event.add(context.id, this.getEntityEnum(), idVerPath, true, cached, true)
    // log
    logger.debug(marker, "${context.id}->$idVerPath:${this.javaClass.simpleName} found in cache")
    logger.trace(
        marker, "${context.id}->$idVerPath:${this.javaClass.simpleName} cached value: {}", cached)
    // cache hit
    return Pair(true, cached)
  }
  // if no id, skip cache
  return Pair(false, null)
}

/**
 * Caches the provided policy result and adds it to the event log.
 *
 * @param idVer The ID and version of the policy.
 * @param context The current context.
 * @param result The policy result to cache.
 * @param idVerPath The path of the policy.
 * @param logger The logger to use for logging.
 * @param marker The marker to use for logging.
 * @return The cached policy result.
 */
internal fun IPolicy.cacheAndReturn(
    idVer: String,
    context: Context,
    result: PolicyResultEnum,
    idVerPath: String,
    logger: Logger,
    marker: Marker,
): PolicyResultEnum {
  if (idVer.isNotBlank()) {
    // put in cache
    context.cache.put(PolicyStoreCacheEnum.POLICY, idVer, result)
  }
  // add to event
  context.event.add(context.id, this.getEntityEnum(), idVerPath, isSuccess(result), result)
  // log
  logger.debug(marker, "${context.id}->$idVerPath:${this.javaClass.simpleName} evaluated")
  logger.trace(
      marker,
      "${context.id}->$idVerPath:${this.javaClass.simpleName} evaluation result: {}",
      result)
  return result
}

/**
 * Returns the [PolicyEntityEnum] for the current [IPolicy] instance.
 *
 * This function is used to determine the type of policy entity (e.g. policy, policy set, policy
 * default) for a given [IPolicy] implementation.
 *
 * @return the [PolicyEntityEnum] for the current [IPolicy] instance
 */
internal fun IPolicy.getEntityEnum(): PolicyEntityEnum =
    when (this) {
      is Policy -> PolicyEntityEnum.POLICY
      is PolicySet -> PolicyEntityEnum.POLICY_SET
      is PolicyDefault -> PolicyEntityEnum.POLICY_DEFAULT
      else -> error("Unsupported Policy type: ${this::class.java}")
    }

/**
 * Determines whether the provided set of [ActionExecutionModeEnum] values should be executed based
 * on the given [PolicyResultEnum].
 *
 * This function checks the provided set of execution modes against the policy result to decide
 * whether the associated actions should be executed.
 *
 * @param result The [PolicyResultEnum] result to check against.
 * @return `true` if the actions should be executed, `false` otherwise.
 */
internal fun Set<ActionExecutionModeEnum>.shouldBeExecuted(result: PolicyResultEnum): Boolean {
  if (result == PolicyResultEnum.PERMIT && this.contains(ActionExecutionModeEnum.ON_PERMIT))
      return true
  if (result == PolicyResultEnum.DENY && this.contains(ActionExecutionModeEnum.ON_DENY)) return true
  if ((result in
      listOf(
          PolicyResultEnum.INDETERMINATE_DENY,
          PolicyResultEnum.INDETERMINATE_PERMIT,
          PolicyResultEnum.INDETERMINATE_DENY_PERMIT)) &&
      this.contains(ActionExecutionModeEnum.ON_INDETERMINATE))
      return true
  if (result == PolicyResultEnum.NOT_APPLICABLE &&
      this.contains(ActionExecutionModeEnum.ON_NOT_APPLICABLE))
      return true
  return false
}
