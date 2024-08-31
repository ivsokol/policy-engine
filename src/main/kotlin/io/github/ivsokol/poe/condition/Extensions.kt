package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.cache.PolicyStoreCacheEnum
import org.slf4j.Logger
import org.slf4j.Marker

/**
 * Retrieves a cached value for the given [IPolicyCondition] instance.
 *
 * If the [idVer] is not blank and the value is present in the cache, the cached value is returned
 * along with a flag indicating that the value was found in the cache. If the [idVer] is blank or
 * the value is not present in the cache, a pair of (false, null) is returned.
 *
 * The cached value is added to the [context].event and logged using the provided [logger] and
 * [marker].
 *
 * @param context The [Context] object containing the cache and event information.
 * @param idVer The unique identifier and version of the [IPolicyCondition] instance.
 * @param idVerPath The path of the [IPolicyCondition] instance.
 * @param logger The [Logger] instance to use for logging.
 * @param marker The [Marker] instance to use for logging.
 * @return A [Pair] containing a boolean flag indicating whether the value was found in the cache,
 *   and the cached value (or null if not found).
 */
internal fun IPolicyCondition.getFromCache(
    context: Context,
    idVer: String,
    idVerPath: String,
    logger: Logger,
    marker: Marker
): Pair<OperationResult, Boolean?> {
  if (idVer.isNotBlank() && context.cache.hasKey(PolicyStoreCacheEnum.CONDITION, idVer)) {
    val cached = context.cache.getCondition(idVer)
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
 * Caches the provided [result] for the given [IPolicyCondition] instance identified by [idVer], and
 * adds the result to the [context].event . The result is logged using the provided [logger] and
 * [marker].
 *
 * If [idVer] is not blank, the [result] is cached using the [PolicyStoreCacheEnum.CONDITION] cache.
 * The [context].event is updated with the [result] and a flag indicating whether the [result] is
 * not null. Logging is performed at the debug and trace levels, providing information about the
 * [IPolicyCondition] instance and the check result.
 *
 * @param idVer The unique identifier and version of the [IPolicyCondition] instance.
 * @param context The [Context] object containing the cache and event information.
 * @param result The result of the [IPolicyCondition] check.
 * @param idVerPath The path of the [IPolicyCondition] instance.
 * @param logger The [Logger] instance to use for logging.
 * @param marker The [Marker] instance to use for logging.
 * @return The provided [result].
 */
internal fun IPolicyCondition.cacheAndReturn(
    idVer: String,
    context: Context,
    result: Boolean?,
    idVerPath: String,
    logger: Logger,
    marker: Marker
): Boolean? {
  if (idVer.isNotBlank()) {
    // put in cache
    context.cache.put(PolicyStoreCacheEnum.CONDITION, idVer, result)
  }
  // add to event
  context.event.add(context.id, this.getEntityEnum(), idVerPath, result != null, result)
  // log
  logger.debug(marker, "${context.id}->$idVerPath:${this.javaClass.simpleName} checked")
  logger.trace(
      marker, "${context.id}->$idVerPath:${this.javaClass.simpleName} check result: {}", result)
  return result
}

/**
 * Returns the [PolicyEntityEnum] corresponding to the type of the [IPolicyCondition] instance.
 *
 * @return The [PolicyEntityEnum] for the [IPolicyCondition] instance.
 */
private fun IPolicyCondition.getEntityEnum(): PolicyEntityEnum =
    when (this) {
      is PolicyConditionAtomic -> PolicyEntityEnum.CONDITION_ATOMIC
      is PolicyConditionComposite -> PolicyEntityEnum.CONDITION_COMPOSITE
      else -> error("Unsupported PolicyCondition type: ${this::class.java}")
    }
