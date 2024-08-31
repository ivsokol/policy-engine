package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.variable.VariableRuntimeTypeEnum
import io.github.ivsokol.poe.variable.VariableValue
import java.math.BigDecimal

/**
 * Checks if the given [VariableValue] is positive.
 *
 * This function checks if the value represented by the [VariableValue] is positive, i.e. greater
 * than zero. It supports various types of values, including [java.time.Period],
 * [java.time.Duration], [Long], [Int], [Double], [Float], and [BigDecimal]. If the value type is
 * not supported, an [IllegalArgumentException] is thrown.
 *
 * @param value The [VariableValue] to check.
 * @param conditionId The ID of the condition being checked.
 * @return `true` if the value is positive, `false` otherwise.
 */
internal fun isPositive(value: VariableValue, conditionId: String): Boolean =
    when (value.type) {
      VariableRuntimeTypeEnum.PERIOD ->
          !((value.body as java.time.Period).isNegative) && !(value.body.isZero)
      VariableRuntimeTypeEnum.DURATION ->
          !((value.body as java.time.Duration).isNegative) && !(value.body.isZero)
      VariableRuntimeTypeEnum.LONG -> (value.body as Long) > 0L
      VariableRuntimeTypeEnum.INT -> (value.body as Int) > 0
      VariableRuntimeTypeEnum.DOUBLE -> (value.body as Double) > 0.0
      VariableRuntimeTypeEnum.FLOAT -> (value.body as Float) > 0.0f
      VariableRuntimeTypeEnum.BIG_DECIMAL -> (value.body as BigDecimal) > BigDecimal.ZERO
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${value.type} is not supported in condition")
    }

/**
 * Checks if the given [VariableValue] is negative.
 *
 * This function checks if the value represented by the [VariableValue] is negative, i.e. less than
 * zero. It supports various types of values, including [java.time.Period], [java.time.Duration],
 * [Long], [Int], [Double], [Float], and [BigDecimal]. If the value type is not supported, an
 * [IllegalArgumentException] is thrown.
 *
 * @param value The [VariableValue] to check.
 * @param conditionId The ID of the condition being checked.
 * @return `true` if the value is negative, `false` otherwise.
 */
internal fun isNegative(value: VariableValue, conditionId: String): Boolean =
    when (value.type) {
      VariableRuntimeTypeEnum.PERIOD -> (value.body as java.time.Period).isNegative
      VariableRuntimeTypeEnum.DURATION -> (value.body as java.time.Duration).isNegative
      VariableRuntimeTypeEnum.LONG -> (value.body as Long) < 0L
      VariableRuntimeTypeEnum.INT -> (value.body as Int) < 0
      VariableRuntimeTypeEnum.DOUBLE -> (value.body as Double) < 0.0
      VariableRuntimeTypeEnum.FLOAT -> (value.body as Float) < 0.0f
      VariableRuntimeTypeEnum.BIG_DECIMAL -> (value.body as BigDecimal) < BigDecimal.ZERO
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${value.type} is not supported in condition")
    }

/**
 * Checks if the given [VariableValue] is zero.
 *
 * This function checks if the value represented by the [VariableValue] is zero. It supports various
 * types of values, including [java.time.Period], [java.time.Duration], [Long], [Int], [Double],
 * [Float], and [BigDecimal]. If the value type is not supported, an [IllegalArgumentException] is
 * thrown.
 *
 * @param value The [VariableValue] to check.
 * @param conditionId The ID of the condition being checked.
 * @return `true` if the value is zero, `false` otherwise.
 */
internal fun isZero(value: VariableValue, conditionId: String): Boolean =
    when (value.type) {
      VariableRuntimeTypeEnum.PERIOD -> (value.body as java.time.Period).isZero
      VariableRuntimeTypeEnum.DURATION -> (value.body as java.time.Duration).isZero
      VariableRuntimeTypeEnum.LONG -> (value.body as Long) == 0L
      VariableRuntimeTypeEnum.INT -> (value.body as Int) == 0
      VariableRuntimeTypeEnum.DOUBLE -> (value.body as Double) == 0.0
      VariableRuntimeTypeEnum.FLOAT -> (value.body as Float) == 0.0f
      VariableRuntimeTypeEnum.BIG_DECIMAL -> (value.body as BigDecimal) == BigDecimal.ZERO
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${value.type} is not supported in condition")
    }
