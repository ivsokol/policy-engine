package com.github.ivsokol.poe.condition

import com.fasterxml.jackson.databind.node.ArrayNode
import com.github.ivsokol.poe.variable.VariableRuntimeTypeEnum
import com.github.ivsokol.poe.variable.VariableValue

/**
 * Checks if the provided [VariableValue] is unique, i.e. contains no duplicate elements.
 *
 * @param arr The [VariableValue] to check for uniqueness.
 * @param conditionId The ID of the condition being evaluated.
 * @return `true` if the [VariableValue] is unique, `false` otherwise.
 * @throws IllegalArgumentException if the [VariableValue] type is not supported.
 */
internal fun isUnique(arr: VariableValue, conditionId: String): Boolean =
    when (arr.type) {
      VariableRuntimeTypeEnum.ARRAY -> (arr.body as List<*>).distinct().size == arr.body.size
      VariableRuntimeTypeEnum.ARRAY_NODE ->
          (arr.body as ArrayNode).distinct().size == arr.body.size()
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${arr.type} is not supported in condition")
    }
