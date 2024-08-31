package com.github.ivsokol.poe.condition

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.variable.VariableRuntimeTypeEnum
import com.github.ivsokol.poe.variable.VariableValue
import com.github.ivsokol.poe.variable.cast

/**
 * Checks if the [container] variable starts with the [item] variable.
 *
 * @param container The variable value to check.
 * @param item The item to check if the [container] starts with.
 * @param stringIgnoreCase Whether to ignore case when comparing strings.
 * @param fieldsStrictCheck Whether to perform a strict check on the fields of the container.
 * @param arrayOrderStrictCheck Whether to perform a strict check on the order of the array.
 * @param context The current context.
 * @param conditionId The ID of the condition.
 * @return `true` if the [container] starts with the [item], `false` otherwise.
 * @throws IllegalArgumentException if the [container] type is not supported.
 */
internal fun startsWith(
    container: VariableValue,
    item: VariableValue,
    stringIgnoreCase: Boolean,
    fieldsStrictCheck: Boolean,
    arrayOrderStrictCheck: Boolean,
    context: Context,
    conditionId: String
): Boolean =
    when (container.type) {
      VariableRuntimeTypeEnum.STRING ->
          (container.body as String).startsWith(
              cast(VariableRuntimeTypeEnum.STRING, item, context.options).body as String,
              stringIgnoreCase)
      VariableRuntimeTypeEnum.ARRAY ->
          eq(
              VariableValue((container.body as List<*>).first()),
              item,
              stringIgnoreCase,
              fieldsStrictCheck,
              arrayOrderStrictCheck,
              context,
              conditionId)
      VariableRuntimeTypeEnum.ARRAY_NODE ->
          eq(
              VariableValue((container.body as ArrayNode).first()),
              cast(VariableRuntimeTypeEnum.JSON_NODE, item, context.options),
              stringIgnoreCase,
              fieldsStrictCheck,
              arrayOrderStrictCheck,
              context,
              conditionId)
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${container.type} is not supported in condition")
    }

/**
 * Checks if the [container] variable ends with the [item] variable.
 *
 * @param container The variable value to check.
 * @param item The item to check if the [container] ends with.
 * @param stringIgnoreCase Whether to ignore case when comparing strings.
 * @param fieldsStrictCheck Whether to perform a strict check on the fields of the container.
 * @param arrayOrderStrictCheck Whether to perform a strict check on the order of the array.
 * @param context The current context.
 * @param conditionId The ID of the condition.
 * @return `true` if the [container] ends with the [item], `false` otherwise.
 * @throws IllegalArgumentException if the [container] type is not supported.
 */
internal fun endsWith(
    container: VariableValue,
    item: VariableValue,
    stringIgnoreCase: Boolean,
    fieldsStrictCheck: Boolean,
    arrayOrderStrictCheck: Boolean,
    context: Context,
    conditionId: String
): Boolean =
    when (container.type) {
      VariableRuntimeTypeEnum.STRING ->
          (container.body as String).endsWith(
              cast(VariableRuntimeTypeEnum.STRING, item, context.options).body as String,
              stringIgnoreCase)
      VariableRuntimeTypeEnum.ARRAY ->
          eq(
              VariableValue((container.body as List<*>).last()),
              item,
              stringIgnoreCase,
              fieldsStrictCheck,
              arrayOrderStrictCheck,
              context,
              conditionId)
      VariableRuntimeTypeEnum.ARRAY_NODE ->
          eq(
              VariableValue((container.body as ArrayNode).last()),
              cast(VariableRuntimeTypeEnum.JSON_NODE, item, context.options),
              stringIgnoreCase,
              fieldsStrictCheck,
              arrayOrderStrictCheck,
              context,
              conditionId)
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${container.type} is not supported in condition")
    }

/**
 * Checks if the [container] variable contains the [item] variable.
 *
 * @param container The variable value to check.
 * @param item The item to check if the [container] contains.
 * @param stringIgnoreCase Whether to ignore case when comparing strings.
 * @param fieldsStrictCheck Whether to perform a strict check on the fields of the container.
 * @param arrayOrderStrictCheck Whether to perform a strict check on the order of the array.
 * @param context The current context.
 * @param conditionId The ID of the condition.
 * @return `true` if the [container] contains the [item], `false` otherwise.
 * @throws IllegalArgumentException if the [container] type is not supported.
 */
internal fun contains(
    container: VariableValue,
    item: VariableValue,
    stringIgnoreCase: Boolean,
    fieldsStrictCheck: Boolean,
    arrayOrderStrictCheck: Boolean,
    context: Context,
    conditionId: String
): Boolean =
    when (container.type) {
      VariableRuntimeTypeEnum.STRING ->
          (container.body as String).contains(
              cast(VariableRuntimeTypeEnum.STRING, item, context.options).body as String,
              stringIgnoreCase)
      VariableRuntimeTypeEnum.ARRAY ->
          (container.body as List<*>).any {
            eq(
                VariableValue(it),
                item,
                stringIgnoreCase,
                fieldsStrictCheck,
                arrayOrderStrictCheck,
                context,
                conditionId)
          }
      VariableRuntimeTypeEnum.ARRAY_NODE ->
          (container.body as ArrayNode).any {
            eq(
                VariableValue(it),
                cast(VariableRuntimeTypeEnum.JSON_NODE, item, context.options),
                stringIgnoreCase,
                fieldsStrictCheck,
                arrayOrderStrictCheck,
                context,
                conditionId)
          }
      VariableRuntimeTypeEnum.JSON_NODE ->
          (container.body as? ArrayNode)?.any {
            eq(
                VariableValue(it),
                cast(VariableRuntimeTypeEnum.JSON_NODE, item, context.options),
                stringIgnoreCase,
                fieldsStrictCheck,
                arrayOrderStrictCheck,
                context,
                conditionId)
          }
              ?: throw IllegalArgumentException(
                  "$conditionId -> Variable type ${container.type} is not supported in condition")
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${container.type} is not supported in condition")
    }

/**
 * Checks if the provided [VariableValue] object contains the specified [key].
 *
 * @param obj The [VariableValue] object to check.
 * @param key The [VariableValue] representing the key to check for.
 * @param conditionId The ID of the condition being checked.
 * @return `true` if the object contains the specified key, `false` otherwise.
 * @throws IllegalArgumentException if the [VariableValue] type is not supported.
 */
internal fun hasKey(obj: VariableValue, key: VariableValue, conditionId: String) =
    when (obj.type) {
      VariableRuntimeTypeEnum.OBJECT_NODE -> (obj.body as ObjectNode).has(key.body as String)
      VariableRuntimeTypeEnum.JSON_NODE ->
          (obj.body as? ObjectNode)?.has(key.body as String)
              ?: throw IllegalArgumentException(
                  "$conditionId -> Variable type ${obj.type} is not supported in condition")
      else ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${obj.type} is not supported in condition")
    }
