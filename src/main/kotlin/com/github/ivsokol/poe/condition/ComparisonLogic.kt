package com.github.ivsokol.poe.condition

import com.github.ivsokol.poe.variable.VariableRuntimeTypeEnum
import com.github.ivsokol.poe.variable.VariableValue
import java.math.BigDecimal

/**
 * Compares two [VariableValue] instances to determine if the left value is greater than the right
 * value.
 *
 * This function supports comparing values of various types, including strings, dates, times,
 * periods, durations, and numeric types. The comparison is performed based on the type of the left
 * [VariableValue].
 *
 * @param left The left [VariableValue] to compare.
 * @param right The right [VariableValue] to compare.
 * @param stringIgnoreCase Whether to ignore case when comparing string values.
 * @param conditionId The ID of the condition being evaluated.
 * @return `true` if the left value is greater than the right value, `false` otherwise.
 * @throws IllegalArgumentException if the type of the left [VariableValue] is not supported.
 */
internal fun gt(
    left: VariableValue,
    right: VariableValue,
    stringIgnoreCase: Boolean,
    conditionId: String
): Boolean =
    when (left.type) {
      VariableRuntimeTypeEnum.STRING ->
          (left.body as String).compareTo(right.body as String, stringIgnoreCase) > 0
      VariableRuntimeTypeEnum.DATE ->
          (left.body as java.time.LocalDate).isAfter(right.body as java.time.LocalDate)
      VariableRuntimeTypeEnum.DATE_TIME ->
          (left.body as java.time.OffsetDateTime).isAfter(right.body as java.time.OffsetDateTime)
      VariableRuntimeTypeEnum.TIME ->
          (left.body as java.time.LocalTime).isAfter(right.body as java.time.LocalTime)
      VariableRuntimeTypeEnum.PERIOD ->
          !((left.body as java.time.Period).minus(right.body as java.time.Period).isNegative) &&
              !(left.body.minus(right.body).isZero)
      VariableRuntimeTypeEnum.DURATION ->
          !((left.body as java.time.Duration).minus(right.body as java.time.Duration).isNegative) &&
              !(left.body.minus(right.body).isZero)
      VariableRuntimeTypeEnum.LONG -> (left.body as Long) > (right.body as Long)
      VariableRuntimeTypeEnum.INT -> (left.body as Int) > (right.body as Int)
      VariableRuntimeTypeEnum.DOUBLE -> (left.body as Double) > (right.body as Double)
      VariableRuntimeTypeEnum.FLOAT -> (left.body as Float) > (right.body as Float)
      VariableRuntimeTypeEnum.BIG_DECIMAL -> (left.body as BigDecimal) > (right.body as BigDecimal)
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${left.type} is not supported in condition")
    }

/**
 * Compares two [VariableValue] instances to determine if the left value is greater than or equal to
 * the right value.
 *
 * This function supports comparing values of various types, including strings, dates, times,
 * periods, durations, and numeric types. The comparison is performed based on the type of the left
 * [VariableValue].
 *
 * @param left The left [VariableValue] to compare.
 * @param right The right [VariableValue] to compare.
 * @param stringIgnoreCase Whether to ignore case when comparing string values.
 * @param conditionId The ID of the condition being evaluated.
 * @return `true` if the left value is greater than or equal to the right value, `false` otherwise.
 * @throws IllegalArgumentException if the type of the left [VariableValue] is not supported.
 */
internal fun gte(
    left: VariableValue,
    right: VariableValue,
    stringIgnoreCase: Boolean,
    conditionId: String
): Boolean =
    when (left.type) {
      VariableRuntimeTypeEnum.STRING ->
          (left.body as String).compareTo(right.body as String, stringIgnoreCase) >= 0
      VariableRuntimeTypeEnum.DATE ->
          (left.body as java.time.LocalDate).isAfter(right.body as java.time.LocalDate) ||
              left.body.isEqual(right.body)
      VariableRuntimeTypeEnum.DATE_TIME ->
          (left.body as java.time.OffsetDateTime).isAfter(right.body as java.time.OffsetDateTime) ||
              left.body.isEqual(right.body)
      VariableRuntimeTypeEnum.TIME ->
          (left.body as java.time.LocalTime).isAfter(right.body as java.time.LocalTime) ||
              left.body == right.body
      VariableRuntimeTypeEnum.PERIOD ->
          !((left.body as java.time.Period).minus(right.body as java.time.Period).isNegative)
      VariableRuntimeTypeEnum.DURATION ->
          !((left.body as java.time.Duration).minus(right.body as java.time.Duration).isNegative)
      VariableRuntimeTypeEnum.LONG -> (left.body as Long) >= (right.body as Long)
      VariableRuntimeTypeEnum.INT -> (left.body as Int) >= (right.body as Int)
      VariableRuntimeTypeEnum.DOUBLE -> (left.body as Double) >= (right.body as Double)
      VariableRuntimeTypeEnum.FLOAT -> (left.body as Float) >= (right.body as Float)
      VariableRuntimeTypeEnum.BIG_DECIMAL -> (left.body as BigDecimal) >= (right.body as BigDecimal)
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${left.type} is not supported in condition")
    }

/**
 * Compares two [VariableValue] instances to determine if the left value is less than the right
 * value.
 *
 * This function supports comparing values of various types, including strings, dates, times,
 * periods, durations, and numeric types. The comparison is performed based on the type of the left
 * [VariableValue].
 *
 * @param left The left [VariableValue] to compare.
 * @param right The right [VariableValue] to compare.
 * @param stringIgnoreCase Whether to ignore case when comparing string values.
 * @param conditionId The ID of the condition being evaluated.
 * @return `true` if the left value is less than the right value, `false` otherwise.
 * @throws IllegalArgumentException if the type of the left [VariableValue] is not supported.
 */
internal fun lt(
    left: VariableValue,
    right: VariableValue,
    stringIgnoreCase: Boolean,
    conditionId: String
): Boolean =
    when (left.type) {
      VariableRuntimeTypeEnum.STRING ->
          (left.body as String).compareTo(right.body as String, stringIgnoreCase) < 0
      VariableRuntimeTypeEnum.DATE ->
          (left.body as java.time.LocalDate).isBefore(right.body as java.time.LocalDate)
      VariableRuntimeTypeEnum.DATE_TIME ->
          (left.body as java.time.OffsetDateTime).isBefore(right.body as java.time.OffsetDateTime)
      VariableRuntimeTypeEnum.TIME ->
          (left.body as java.time.LocalTime).isBefore(right.body as java.time.LocalTime)
      VariableRuntimeTypeEnum.PERIOD ->
          (left.body as java.time.Period).minus(right.body as java.time.Period).isNegative
      VariableRuntimeTypeEnum.DURATION ->
          (left.body as java.time.Duration).minus(right.body as java.time.Duration).isNegative
      VariableRuntimeTypeEnum.LONG -> (left.body as Long) < (right.body as Long)
      VariableRuntimeTypeEnum.INT -> (left.body as Int) < (right.body as Int)
      VariableRuntimeTypeEnum.DOUBLE -> (left.body as Double) < (right.body as Double)
      VariableRuntimeTypeEnum.FLOAT -> (left.body as Float) < (right.body as Float)
      VariableRuntimeTypeEnum.BIG_DECIMAL -> (left.body as BigDecimal) < (right.body as BigDecimal)
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${left.type} is not supported in condition")
    }

/**
 * Compares two [VariableValue] instances to determine if the left value is less than or equal to
 * the right value.
 *
 * This function supports comparing values of various types, including strings, dates, times,
 * periods, durations, and numeric types. The comparison is performed based on the type of the left
 * [VariableValue].
 *
 * @param left The left [VariableValue] to compare.
 * @param right The right [VariableValue] to compare.
 * @param stringIgnoreCase Whether to ignore case when comparing string values.
 * @param conditionId The ID of the condition being evaluated.
 * @return `true` if the left value is less than or equal to the right value, `false` otherwise.
 * @throws IllegalArgumentException if the type of the left [VariableValue] is not supported.
 */
internal fun lte(
    left: VariableValue,
    right: VariableValue,
    stringIgnoreCase: Boolean,
    conditionId: String
): Boolean =
    when (left.type) {
      VariableRuntimeTypeEnum.STRING ->
          (left.body as String).compareTo(right.body as String, stringIgnoreCase) <= 0
      VariableRuntimeTypeEnum.DATE ->
          (left.body as java.time.LocalDate).isBefore(right.body as java.time.LocalDate) ||
              left.body.isEqual(right.body)
      VariableRuntimeTypeEnum.DATE_TIME ->
          (left.body as java.time.OffsetDateTime).isBefore(
              right.body as java.time.OffsetDateTime) || left.body.isEqual(right.body)
      VariableRuntimeTypeEnum.TIME ->
          (left.body as java.time.LocalTime).isBefore(right.body as java.time.LocalTime) ||
              left.body == right.body
      VariableRuntimeTypeEnum.PERIOD ->
          (left.body as java.time.Period).minus(right.body as java.time.Period).isNegative ||
              left.body.minus(right.body).isZero
      VariableRuntimeTypeEnum.DURATION ->
          (left.body as java.time.Duration).minus(right.body as java.time.Duration).isNegative ||
              left.body.minus(right.body).isZero
      VariableRuntimeTypeEnum.LONG -> (left.body as Long) <= (right.body as Long)
      VariableRuntimeTypeEnum.INT -> (left.body as Int) <= (right.body as Int)
      VariableRuntimeTypeEnum.DOUBLE -> (left.body as Double) <= (right.body as Double)
      VariableRuntimeTypeEnum.FLOAT -> (left.body as Float) <= (right.body as Float)
      VariableRuntimeTypeEnum.BIG_DECIMAL -> (left.body as BigDecimal) <= (right.body as BigDecimal)
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${left.type} is not supported in condition")
    }
