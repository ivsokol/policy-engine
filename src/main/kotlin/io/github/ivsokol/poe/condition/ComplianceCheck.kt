package io.github.ivsokol.poe.condition

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.ivsokol.poe.variable.VariableRuntimeTypeEnum
import io.github.ivsokol.poe.variable.VariableValue

/**
 * Checks the compliance of a [VariableValue] against a condition identified by the provided
 * [conditionId]. This function handles variables of types STRING, DATE, DATE_TIME, TIME, PERIOD,
 * DURATION, LONG, INT, DOUBLE, FLOAT, and BIG_DECIMAL. If the variable type is not supported, an
 * [IllegalArgumentException] is thrown with a message indicating the unsupported type.
 *
 * @param variable The [VariableValue] to check for compliance.
 * @param conditionId The identifier of the condition being checked.
 * @throws IllegalArgumentException if the variable type is not supported.
 */
internal fun complianceCheckGtGteLtLte(variable: VariableValue, conditionId: String): Unit =
    when (variable.type) {
      VariableRuntimeTypeEnum.STRING -> Unit
      VariableRuntimeTypeEnum.DATE -> Unit
      VariableRuntimeTypeEnum.DATE_TIME -> Unit
      VariableRuntimeTypeEnum.TIME -> Unit
      VariableRuntimeTypeEnum.PERIOD -> Unit
      VariableRuntimeTypeEnum.DURATION -> Unit
      VariableRuntimeTypeEnum.LONG -> Unit
      VariableRuntimeTypeEnum.INT -> Unit
      VariableRuntimeTypeEnum.DOUBLE -> Unit
      VariableRuntimeTypeEnum.FLOAT -> Unit
      VariableRuntimeTypeEnum.BIG_DECIMAL -> Unit
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${variable.type} is not supported in condition")
    }

/**
 * Checks the compliance of a [VariableValue] against a condition identified by the provided
 * [conditionId]. This function handles variables of type STRING. If the variable type is not
 * STRING, an [IllegalArgumentException] is thrown with a message indicating the unsupported type.
 *
 * @param variable The [VariableValue] to check for compliance.
 * @param conditionId The identifier of the condition being checked.
 * @throws IllegalArgumentException if the variable type is not supported.
 */
internal fun complianceCheckString(variable: VariableValue, conditionId: String): Unit =
    when (variable.type) {
      VariableRuntimeTypeEnum.STRING -> Unit
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${variable.type} is not supported in condition")
    }

/**
 * Checks the compliance of a [VariableValue] against a condition identified by the provided
 * [conditionId]. This function handles variables of type STRING, ARRAY, ARRAY_NODE, and JSON_NODE.
 * If the variable type is not supported, an [IllegalArgumentException] is thrown with a message
 * indicating the unsupported type.
 *
 * @param variable The [VariableValue] to check for compliance.
 * @param conditionId The identifier of the condition being checked.
 * @throws IllegalArgumentException if the variable type is not supported.
 */
internal fun complianceCheckStringOrArray(variable: VariableValue, conditionId: String): Unit =
    when (variable.type) {
      VariableRuntimeTypeEnum.STRING -> Unit
      VariableRuntimeTypeEnum.ARRAY,
      VariableRuntimeTypeEnum.ARRAY_NODE,
      VariableRuntimeTypeEnum.JSON_NODE -> complianceCheckArray(variable, conditionId)
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${variable.type} is not supported in condition")
    }

/**
 * Checks the compliance of a [VariableValue] against a condition identified by the provided
 * [conditionId]. This function handles variables of type ARRAY, ARRAY_NODE, and JSON_NODE where the
 * body is an ArrayNode. If the variable type is not supported, an [IllegalArgumentException] is
 * thrown with a message indicating the unsupported type.
 *
 * @param variable The [VariableValue] to check for compliance.
 * @param conditionId The identifier of the condition being checked.
 * @throws IllegalArgumentException if the variable type is not supported.
 */
internal fun complianceCheckArray(variable: VariableValue, conditionId: String): Unit =
    when (variable.type) {
      VariableRuntimeTypeEnum.ARRAY -> Unit
      VariableRuntimeTypeEnum.ARRAY_NODE -> Unit
      VariableRuntimeTypeEnum.JSON_NODE ->
          if (variable.body is ArrayNode) Unit
          else
              throw IllegalArgumentException(
                  "$conditionId -> Variable type ${variable.type} is not supported in condition")
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${variable.type} is not supported in condition")
    }

/**
 * Checks the compliance of a [VariableValue] against a condition identified by the provided
 * [conditionId]. This function handles variables of type OBJECT_NODE and JSON_NODE where the body
 * is an ObjectNode. If the variable type is not supported, an [IllegalArgumentException] is thrown
 * with a message indicating the unsupported type.
 *
 * @param variable The [VariableValue] to check for compliance.
 * @param conditionId The identifier of the condition being checked.
 * @throws IllegalArgumentException if the variable type is not supported.
 */
internal fun complianceCheckObject(variable: VariableValue, conditionId: String): Unit =
    when (variable.type) {
      VariableRuntimeTypeEnum.OBJECT_NODE -> Unit
      VariableRuntimeTypeEnum.JSON_NODE ->
          if (variable.body is ObjectNode) Unit
          else
              throw IllegalArgumentException(
                  "$conditionId -> Variable type ${variable.type} is not supported in condition")
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${variable.type} is not supported in condition")
    }

/**
 * Checks the compliance of a [VariableValue] against a condition identified by the provided
 * [conditionId]. This function handles variables of type OBJECT_NODE, JSON_NODE, and STRING. If the
 * variable type is not supported, an [IllegalArgumentException] is thrown with a message indicating
 * the unsupported type.
 *
 * @param variable The [VariableValue] to check for compliance.
 * @param conditionId The identifier of the condition being checked.
 * @throws IllegalArgumentException if the variable type is not supported.
 */
internal fun complianceCheckObjectOrString(variable: VariableValue, conditionId: String): Unit =
    when (variable.type) {
      VariableRuntimeTypeEnum.OBJECT_NODE,
      VariableRuntimeTypeEnum.JSON_NODE -> complianceCheckObject(variable, conditionId)
      VariableRuntimeTypeEnum.STRING -> Unit
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${variable.type} is not supported in condition")
    }

// isPositive, isNegative, isZero
/**
 * Checks the compliance of a [VariableValue] against a condition identified by the provided
 * [conditionId]. This function handles variables of type PERIOD, DURATION, LONG, INT, DOUBLE,
 * FLOAT, and BIG_DECIMAL. If the variable type is not supported, an [IllegalArgumentException] is
 * thrown with a message indicating the unsupported type.
 *
 * @param variable The [VariableValue] to check for compliance.
 * @param conditionId The identifier of the condition being checked.
 * @throws IllegalArgumentException if the variable type is not supported.
 */
internal fun complianceCheckMathematical(variable: VariableValue, conditionId: String): Unit =
    when (variable.type) {
      VariableRuntimeTypeEnum.PERIOD -> Unit
      VariableRuntimeTypeEnum.DURATION -> Unit
      VariableRuntimeTypeEnum.LONG -> Unit
      VariableRuntimeTypeEnum.INT -> Unit
      VariableRuntimeTypeEnum.DOUBLE -> Unit
      VariableRuntimeTypeEnum.FLOAT -> Unit
      VariableRuntimeTypeEnum.BIG_DECIMAL -> Unit
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${variable.type} is not supported in condition")
    }

/**
 * Checks the compliance of a [VariableValue] against a condition identified by the provided
 * [conditionId]. This function handles variables of type DATE, DATE_TIME, and TIME. If the variable
 * type is not supported, an [IllegalArgumentException] is thrown with a message indicating the
 * unsupported type.
 *
 * @param variable The [VariableValue] to check for compliance.
 * @param conditionId The identifier of the condition being checked.
 * @throws IllegalArgumentException if the variable type is not supported.
 */
internal fun complianceCheckDateTime(variable: VariableValue, conditionId: String): Unit =
    when (variable.type) {
      VariableRuntimeTypeEnum.DATE -> Unit
      VariableRuntimeTypeEnum.DATE_TIME -> Unit
      VariableRuntimeTypeEnum.TIME -> Unit
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${variable.type} is not supported in condition")
    }
