package com.github.ivsokol.poe.condition

import com.fasterxml.jackson.databind.JsonNode
import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.variable.VariableRuntimeTypeEnum
import com.github.ivsokol.poe.variable.VariableValue
import io.json.compare.CompareMode
import io.json.compare.JSONCompare
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

private val logger = LoggerFactory.getLogger(PolicyConditionAtomic::class.java)
private val marker = MarkerFactory.getMarker("PolicyCondition")

/**
 * Compares two [VariableValue] instances for equality, supporting various data types and comparison
 * modes.
 *
 * @param left The left-hand [VariableValue] to compare.
 * @param right The right-hand [VariableValue] to compare.
 * @param stringIgnoreCase Whether to ignore case when comparing string values.
 * @param fieldsStrictCheck Whether to perform a strict check on object and array fields.
 * @param arrayOrderStrictCheck Whether to perform a strict check on the order of array elements.
 * @param context The [Context] object containing configuration options.
 * @param conditionId The ID of the condition being evaluated.
 * @return `true` if the two [VariableValue] instances are equal, `false` otherwise.
 */
internal fun eq(
    left: VariableValue,
    right: VariableValue,
    stringIgnoreCase: Boolean,
    fieldsStrictCheck: Boolean,
    arrayOrderStrictCheck: Boolean,
    context: Context,
    conditionId: String
): Boolean =
    when (left.type) {
      VariableRuntimeTypeEnum.STRING ->
          (left.body as String).equals(right.body as String, stringIgnoreCase)
      VariableRuntimeTypeEnum.NULL,
      VariableRuntimeTypeEnum.UNKNOWN ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type ${left.type} is not supported in condition")
      VariableRuntimeTypeEnum.DATE ->
          (left.body as java.time.LocalDate).isEqual((right.body as java.time.LocalDate))
      VariableRuntimeTypeEnum.DATE_TIME ->
          (left.body as java.time.OffsetDateTime).isEqual((right.body as java.time.OffsetDateTime))
      VariableRuntimeTypeEnum.TIME ->
          (left.body as java.time.LocalTime) == (right.body as java.time.LocalTime)
      VariableRuntimeTypeEnum.PERIOD ->
          (left.body as java.time.Period).minus((right.body as java.time.Period)).isZero
      VariableRuntimeTypeEnum.DURATION ->
          (left.body as java.time.Duration).minus((right.body as java.time.Duration)).isZero
      VariableRuntimeTypeEnum.LONG -> (left.body as Long) == (right.body as Long)
      VariableRuntimeTypeEnum.INT -> (left.body as Int) == (right.body as Int)
      VariableRuntimeTypeEnum.DOUBLE -> (left.body as Double) == (right.body as Double)
      VariableRuntimeTypeEnum.FLOAT -> (left.body as Float) == (right.body as Float)
      VariableRuntimeTypeEnum.BIG_DECIMAL ->
          (left.body as java.math.BigDecimal).compareTo((right.body as java.math.BigDecimal)) == 0
      VariableRuntimeTypeEnum.BOOLEAN -> (left.body as Boolean) == (right.body as Boolean)
      VariableRuntimeTypeEnum.JSON_NODE,
      VariableRuntimeTypeEnum.OBJECT_NODE,
      VariableRuntimeTypeEnum.ARRAY_NODE ->
          compareJSON(
              left.body as JsonNode,
              right.body as JsonNode,
              fieldsStrictCheck,
              arrayOrderStrictCheck,
              context,
              conditionId)
      VariableRuntimeTypeEnum.ARRAY ->
          compareJSON(
              context.options.objectMapper.valueToTree(left.body),
              context.options.objectMapper.valueToTree(right.body),
              fieldsStrictCheck,
              arrayOrderStrictCheck,
              context,
              conditionId)
    }

/**
 * Compares two JSON nodes for equality, with optional strict checks for fields and array order.
 *
 * @param left The left JSON node to compare.
 * @param right The right JSON node to compare.
 * @param fieldsStrictCheck If true, the comparison will be strict for JSON object fields.
 * @param arrayOrderStrictCheck If true, the comparison will be strict for the order of elements in
 *   JSON arrays.
 * @param context The context for the comparison, including options like the ObjectMapper.
 * @param conditionId The ID of the condition being evaluated, used for logging.
 * @return True if the JSON nodes are equal, false otherwise.
 */
private fun compareJSON(
    left: JsonNode,
    right: JsonNode,
    fieldsStrictCheck: Boolean,
    arrayOrderStrictCheck: Boolean,
    context: Context,
    conditionId: String
): Boolean {
  val leftJson = context.options.objectMapper.writeValueAsString(left)
  val rightJson = context.options.objectMapper.writeValueAsString(right)
  if (left.isNull && right.isNull) return true
  if ((left.isArray && right.isArray) || (left.isObject && right.isObject)) {
    val compareModes = emptySet<CompareMode>().toMutableSet()
    if (fieldsStrictCheck) {
      compareModes += CompareMode.JSON_OBJECT_NON_EXTENSIBLE
      compareModes += CompareMode.JSON_ARRAY_NON_EXTENSIBLE
    }
    if (arrayOrderStrictCheck) {
      compareModes += CompareMode.JSON_ARRAY_STRICT_ORDER
    }
    var result = true
    try {
      JSONCompare.assertMatches(leftJson, rightJson, compareModes)
    } catch (e: Throwable) {
      logger.error(marker, "$conditionId -> JSON comparison failed: ${e.message}", e)
      result = false
    }
    return result
  }
  return context.options.objectMapper.writeValueAsString(left) ==
      context.options.objectMapper.writeValueAsString(right)
}
