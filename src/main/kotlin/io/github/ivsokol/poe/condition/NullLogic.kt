package io.github.ivsokol.poe.condition

import com.fasterxml.jackson.databind.node.ArrayNode
import io.github.ivsokol.poe.variable.VariableRuntimeTypeEnum
import io.github.ivsokol.poe.variable.VariableValue

/**
 * Checks if the given [VariableValue] is null.
 *
 * @param value the [VariableValue] to check for null
 * @return `true` if the [VariableValue] is null, `false` otherwise
 */
internal fun isNull(value: VariableValue): Boolean =
    when (value.type) {
      VariableRuntimeTypeEnum.NULL -> true
      else -> false
    }

/**
 * Checks if the given [VariableValue] is not null.
 *
 * @param value the [VariableValue] to check for not null
 * @return `true` if the [VariableValue] is not null, `false` otherwise
 */
internal fun isNotNull(value: VariableValue): Boolean =
    when (value.type) {
      VariableRuntimeTypeEnum.NULL -> false
      else -> true
    }

/**
 * Checks if the given [VariableValue] is empty.
 *
 * @param value the [VariableValue] to check for emptiness
 * @param conditionId the ID of the condition being checked
 * @return `true` if the [VariableValue] is empty, `false` otherwise
 * @throws IllegalArgumentException if the [VariableValue] type is not supported in the condition
 */
internal fun isEmpty(value: VariableValue, conditionId: String): Boolean =
    when (value.type) {
      VariableRuntimeTypeEnum.STRING -> (value.body as String).isEmpty()
      VariableRuntimeTypeEnum.ARRAY_NODE -> (value.body as ArrayNode).isEmpty
      VariableRuntimeTypeEnum.ARRAY -> (value.body as List<*>).isEmpty()
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${value.type} is not supported in condition")
    }

/**
 * Checks if the given [VariableValue] is not empty.
 *
 * @param value the [VariableValue] to check for not empty
 * @param conditionId the ID of the condition being checked
 * @return `true` if the [VariableValue] is not empty, `false` otherwise
 * @throws IllegalArgumentException if the [VariableValue] type is not supported in the condition
 */
internal fun isNotEmpty(value: VariableValue, conditionId: String): Boolean =
    when (value.type) {
      VariableRuntimeTypeEnum.STRING -> (value.body as String).isNotEmpty()
      VariableRuntimeTypeEnum.ARRAY_NODE -> !(value.body as ArrayNode).isEmpty
      VariableRuntimeTypeEnum.ARRAY -> (value.body as List<*>).isNotEmpty()
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${value.type} is not supported in condition")
    }

/**
 * Checks if the given [VariableValue] is blank.
 *
 * @param value the [VariableValue] to check for blankness
 * @param conditionId the ID of the condition being checked
 * @return `true` if the [VariableValue] is blank, `false` otherwise
 * @throws IllegalArgumentException if the [VariableValue] type is not supported in the condition
 */
internal fun isBlank(value: VariableValue, conditionId: String): Boolean =
    when (value.type) {
      VariableRuntimeTypeEnum.STRING -> (value.body as String).isBlank()
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${value.type} is not supported in condition")
    }

/**
 * Checks if the given [VariableValue] is not blank.
 *
 * @param value the [VariableValue] to check for not blankness
 * @param conditionId the ID of the condition being checked
 * @return `true` if the [VariableValue] is not blank, `false` otherwise
 * @throws IllegalArgumentException if the [VariableValue] type is not supported in the condition
 */
internal fun isNotBlank(value: VariableValue, conditionId: String): Boolean =
    when (value.type) {
      VariableRuntimeTypeEnum.STRING -> (value.body as String).isNotBlank()
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${value.type} is not supported in condition")
    }
