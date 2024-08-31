package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.DefaultEnvironmentKey
import io.github.ivsokol.poe.variable.ContextStoreEnum
import io.github.ivsokol.poe.variable.VariableRuntimeTypeEnum
import io.github.ivsokol.poe.variable.VariableValue
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

/**
 * Checks if the given temporal value is in the past compared to the current date/time in the
 * provided context.
 *
 * @param temporal The temporal value to check, which can be a [LocalDate], [OffsetDateTime], or
 *   [LocalTime].
 * @param context The context containing the current date/time information.
 * @param conditionId The ID of the condition being evaluated, used for error reporting.
 * @return `true` if the temporal value is in the past, `false` otherwise.
 * @throws IllegalArgumentException if the temporal value type is not supported.
 */
internal fun isPast(temporal: VariableValue, context: Context, conditionId: String): Boolean =
    when (temporal.type) {
      VariableRuntimeTypeEnum.DATE -> (temporal.body as LocalDate).isBefore(getCurrentDate(context))
      VariableRuntimeTypeEnum.DATE_TIME ->
          (temporal.body as OffsetDateTime).isBefore(getCurrentDateTime(context))
      VariableRuntimeTypeEnum.TIME -> (temporal.body as LocalTime).isBefore(getLocalTime(context))
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${temporal.type} is not supported in condition")
    }

/**
 * Checks if the given temporal value is in the future compared to the current date/time in the
 * provided context.
 *
 * @param temporal The temporal value to check, which can be a [LocalDate], [OffsetDateTime], or
 *   [LocalTime].
 * @param context The context containing the current date/time information.
 * @param conditionId The ID of the condition being evaluated, used for error reporting.
 * @return `true` if the temporal value is in the future, `false` otherwise.
 * @throws IllegalArgumentException if the temporal value type is not supported.
 */
internal fun isFuture(temporal: VariableValue, context: Context, conditionId: String): Boolean =
    when (temporal.type) {
      VariableRuntimeTypeEnum.DATE -> (temporal.body as LocalDate).isAfter(getCurrentDate(context))
      VariableRuntimeTypeEnum.DATE_TIME ->
          (temporal.body as OffsetDateTime).isAfter(getCurrentDateTime(context))
      VariableRuntimeTypeEnum.TIME -> (temporal.body as LocalTime).isAfter(getLocalTime(context))
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${temporal.type} is not supported in condition")
    }

/**
 * Gets the current date from the provided context.
 *
 * If the current date is available in the environment store of the context, it is returned.
 * Otherwise, if the context has a custom clock, the current date is retrieved using that clock. If
 * no custom clock is available, the current date is retrieved using the default zone ID.
 *
 * @param context The context containing the current date information.
 * @return The current date.
 */
private fun getCurrentDate(context: Context): LocalDate =
    context.store(ContextStoreEnum.ENVIRONMENT)?.get(DefaultEnvironmentKey.CURRENT_DATE)
        as LocalDate?
        ?: if (context.options.clock != null) LocalDate.now(context.options.clock)
        else LocalDate.now(context.options.zoneId)

/**
 * Gets the current date-time from the provided context.
 *
 * If the current date-time is available in the environment store of the context, it is returned.
 * Otherwise, if the context has a custom clock, the current date-time is retrieved using that
 * clock. If no custom clock is available, the current date-time is retrieved using the default zone
 * ID.
 *
 * @param context The context containing the current date-time information.
 * @return The current date-time.
 */
private fun getCurrentDateTime(context: Context): OffsetDateTime =
    context.store(ContextStoreEnum.ENVIRONMENT)?.get(DefaultEnvironmentKey.CURRENT_DATE_TIME)
        as OffsetDateTime?
        ?: if (context.options.clock != null) OffsetDateTime.now(context.options.clock)
        else OffsetDateTime.now(context.options.zoneId)

/**
 * Gets the current time from the provided context.
 *
 * If the current time is available in the environment store of the context, it is returned.
 * Otherwise, if the context has a custom clock, the current time is retrieved using that clock. If
 * no custom clock is available, the current time is retrieved using the default zone ID.
 *
 * @param context The context containing the current time information.
 * @return The current time.
 */
private fun getLocalTime(context: Context): LocalTime =
    context.store(ContextStoreEnum.ENVIRONMENT)?.get(DefaultEnvironmentKey.LOCAL_TIME) as LocalTime?
        ?: if (context.options.clock != null) LocalTime.now(context.options.clock)
        else LocalTime.now(context.options.zoneId)
