/* (C)2024 */
package io.github.ivsokol.poe.variable

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.BigDecimal
import java.time.*
import kotlinx.serialization.json.*

/**
 * Determines the runtime type of the given [body] object.
 *
 * If the [body] is not null, is not an [ArrayNode], and is either an array, list, or array-like
 * object, the runtime type is [VariableRuntimeTypeEnum.ARRAY]. Otherwise, the runtime type is
 * determined based on the actual type of the [body] object, returning the appropriate
 * [VariableRuntimeTypeEnum] value.
 *
 * @param body the object to determine the runtime type for
 * @return the determined [VariableRuntimeTypeEnum] for the [body] object
 */
internal fun determineRuntimeType(body: Any?): VariableRuntimeTypeEnum =
    if (body != null &&
        body !is ArrayNode &&
        (body is Array<*> || body is List<*> || body::class.java.isArray))
        VariableRuntimeTypeEnum.ARRAY
    else
        when (body) {
          null -> VariableRuntimeTypeEnum.NULL
          is Boolean -> VariableRuntimeTypeEnum.BOOLEAN
          is String -> VariableRuntimeTypeEnum.STRING
          is Int -> VariableRuntimeTypeEnum.INT
          is Double -> VariableRuntimeTypeEnum.DOUBLE
          is Long -> VariableRuntimeTypeEnum.LONG
          is Float -> VariableRuntimeTypeEnum.FLOAT
          is BigDecimal -> VariableRuntimeTypeEnum.BIG_DECIMAL
          is LocalDate -> VariableRuntimeTypeEnum.DATE
          is OffsetDateTime -> VariableRuntimeTypeEnum.DATE_TIME
          is LocalTime -> VariableRuntimeTypeEnum.TIME
          is Period -> VariableRuntimeTypeEnum.PERIOD
          is Duration -> VariableRuntimeTypeEnum.DURATION
          is ObjectNode -> VariableRuntimeTypeEnum.OBJECT_NODE
          is ArrayNode -> VariableRuntimeTypeEnum.ARRAY_NODE
          is JsonNode -> VariableRuntimeTypeEnum.JSON_NODE
          else -> VariableRuntimeTypeEnum.UNKNOWN
        }

/**
 * Determines the [VariableRuntimeTypeEnum] based on the provided [VariableValueTypeEnum] and
 * optional [VariableValueFormatEnum].
 *
 * This function maps the high-level [VariableValueTypeEnum] and [VariableValueFormatEnum] to the
 * more granular [VariableRuntimeTypeEnum] that represents the actual runtime type of the variable
 * value.
 *
 * @param type The [VariableValueTypeEnum] of the variable value.
 * @param format The optional [VariableValueFormatEnum] of the variable value.
 * @return The corresponding [VariableRuntimeTypeEnum] for the provided type and format.
 */
internal fun runtimeTypeFromTypeAndFormat(
    type: VariableValueTypeEnum,
    format: VariableValueFormatEnum?
): VariableRuntimeTypeEnum {
  return when (type) {
    VariableValueTypeEnum.BOOLEAN ->
        when (format) {
          null -> VariableRuntimeTypeEnum.BOOLEAN
          // if format is JSON, return JSON_NODE
          VariableValueFormatEnum.JSON -> VariableRuntimeTypeEnum.JSON_NODE
          else -> VariableRuntimeTypeEnum.BOOLEAN
        }
    VariableValueTypeEnum.INT ->
        when (format) {
          null -> VariableRuntimeTypeEnum.INT
          VariableValueFormatEnum.LONG -> VariableRuntimeTypeEnum.LONG
          // if format is JSON, return JSON_NODE
          VariableValueFormatEnum.JSON -> VariableRuntimeTypeEnum.JSON_NODE
          else -> VariableRuntimeTypeEnum.INT
        }
    VariableValueTypeEnum.STRING ->
        when (format) {
          null -> VariableRuntimeTypeEnum.STRING
          VariableValueFormatEnum.DATE_TIME -> VariableRuntimeTypeEnum.DATE_TIME
          VariableValueFormatEnum.DATE -> VariableRuntimeTypeEnum.DATE
          VariableValueFormatEnum.TIME -> VariableRuntimeTypeEnum.TIME
          VariableValueFormatEnum.PERIOD -> VariableRuntimeTypeEnum.PERIOD
          VariableValueFormatEnum.DURATION -> VariableRuntimeTypeEnum.DURATION
          VariableValueFormatEnum.JSON -> VariableRuntimeTypeEnum.JSON_NODE
          else -> VariableRuntimeTypeEnum.STRING
        }
    VariableValueTypeEnum.NUMBER ->
        when (format) {
          null -> VariableRuntimeTypeEnum.DOUBLE
          VariableValueFormatEnum.FLOAT -> VariableRuntimeTypeEnum.FLOAT
          VariableValueFormatEnum.DOUBLE -> VariableRuntimeTypeEnum.DOUBLE
          VariableValueFormatEnum.BIG_DECIMAL -> VariableRuntimeTypeEnum.BIG_DECIMAL
          VariableValueFormatEnum.JSON -> VariableRuntimeTypeEnum.JSON_NODE
          else -> VariableRuntimeTypeEnum.DOUBLE
        }
    VariableValueTypeEnum.OBJECT ->
        when (format) {
          null -> VariableRuntimeTypeEnum.UNKNOWN
          VariableValueFormatEnum.JSON -> VariableRuntimeTypeEnum.OBJECT_NODE
          else -> VariableRuntimeTypeEnum.UNKNOWN
        }
    VariableValueTypeEnum.ARRAY ->
        when (format) {
          null -> VariableRuntimeTypeEnum.ARRAY
          VariableValueFormatEnum.JSON -> VariableRuntimeTypeEnum.ARRAY_NODE
          else -> VariableRuntimeTypeEnum.ARRAY
        }
  }
}

/**
 * Determines the [VariableValueTypeEnum] and [VariableValueFormatEnum] from the given
 * [VariableRuntimeTypeEnum].
 *
 * @param runtimeType The [VariableRuntimeTypeEnum] to determine the value type and format from.
 * @return A [Pair] of the determined [VariableValueTypeEnum] and [VariableValueFormatEnum], or
 *   `null` if the runtime type is unknown.
 */
internal fun typeAndFormatFromRuntimeType(
    runtimeType: VariableRuntimeTypeEnum
): Pair<VariableValueTypeEnum?, VariableValueFormatEnum?>? =
    when (runtimeType) {
      VariableRuntimeTypeEnum.NULL -> null
      VariableRuntimeTypeEnum.UNKNOWN -> null
      VariableRuntimeTypeEnum.JSON_NODE -> Pair(null, VariableValueFormatEnum.JSON)
      VariableRuntimeTypeEnum.STRING -> Pair(VariableValueTypeEnum.STRING, null)
      VariableRuntimeTypeEnum.DATE ->
          Pair(VariableValueTypeEnum.STRING, VariableValueFormatEnum.DATE)
      VariableRuntimeTypeEnum.DATE_TIME ->
          Pair(VariableValueTypeEnum.STRING, VariableValueFormatEnum.DATE_TIME)
      VariableRuntimeTypeEnum.TIME ->
          Pair(VariableValueTypeEnum.STRING, VariableValueFormatEnum.TIME)
      VariableRuntimeTypeEnum.PERIOD ->
          Pair(VariableValueTypeEnum.STRING, VariableValueFormatEnum.PERIOD)
      VariableRuntimeTypeEnum.DURATION ->
          Pair(VariableValueTypeEnum.STRING, VariableValueFormatEnum.DURATION)
      VariableRuntimeTypeEnum.LONG -> Pair(VariableValueTypeEnum.INT, VariableValueFormatEnum.LONG)
      VariableRuntimeTypeEnum.INT -> Pair(VariableValueTypeEnum.INT, null)
      VariableRuntimeTypeEnum.DOUBLE ->
          Pair(VariableValueTypeEnum.NUMBER, VariableValueFormatEnum.DOUBLE)
      VariableRuntimeTypeEnum.FLOAT ->
          Pair(VariableValueTypeEnum.NUMBER, VariableValueFormatEnum.FLOAT)
      VariableRuntimeTypeEnum.BIG_DECIMAL ->
          Pair(VariableValueTypeEnum.NUMBER, VariableValueFormatEnum.BIG_DECIMAL)
      VariableRuntimeTypeEnum.BOOLEAN -> Pair(VariableValueTypeEnum.BOOLEAN, null)
      VariableRuntimeTypeEnum.OBJECT_NODE ->
          Pair(VariableValueTypeEnum.OBJECT, VariableValueFormatEnum.JSON)
      VariableRuntimeTypeEnum.ARRAY_NODE ->
          Pair(VariableValueTypeEnum.ARRAY, VariableValueFormatEnum.JSON)
      VariableRuntimeTypeEnum.ARRAY -> Pair(VariableValueTypeEnum.ARRAY, null)
    }

/**
 * Determines the [VariableRuntimeTypeEnum] for the given [JsonElement].
 *
 * This function analyzes the type of the [JsonElement] and returns the corresponding
 * [VariableRuntimeTypeEnum] value. It handles various JSON primitive types, including strings,
 * numbers, booleans, and null, as well as JSON objects and arrays.
 *
 * @param jsonElement The [JsonElement] to determine the runtime type for.
 * @return The [VariableRuntimeTypeEnum] corresponding to the type of the [JsonElement], or `null`
 *   if the type is unknown.
 */
internal fun runtimeTypeFromJsonElement(jsonElement: JsonElement): VariableRuntimeTypeEnum =
    when (jsonElement) {
      is JsonNull -> VariableRuntimeTypeEnum.NULL
      is JsonObject -> VariableRuntimeTypeEnum.OBJECT_NODE
      is JsonArray -> VariableRuntimeTypeEnum.ARRAY_NODE
      is JsonPrimitive ->
          when {
            !jsonElement.jsonPrimitive.isString ->
                when {
                  jsonElement.jsonPrimitive.booleanOrNull != null -> VariableRuntimeTypeEnum.BOOLEAN
                  jsonElement.jsonPrimitive.intOrNull != null -> VariableRuntimeTypeEnum.INT
                  jsonElement.jsonPrimitive.longOrNull != null -> VariableRuntimeTypeEnum.LONG
                  // no float as doubleOrNull handles float values
                  jsonElement.jsonPrimitive.doubleOrNull != null -> VariableRuntimeTypeEnum.DOUBLE
                  else -> error("Unknown primitive non-string type: $jsonElement")
                }
            jsonElement.jsonPrimitive.isString -> {
              val content = jsonElement.jsonPrimitive.content
              when {
                content.toBigDecimalOrNull() != null -> VariableRuntimeTypeEnum.BIG_DECIMAL
                content.toLocalDateOrNull() != null -> VariableRuntimeTypeEnum.DATE
                content.toLocalTimeOrNull() != null -> VariableRuntimeTypeEnum.TIME
                content.toOffsetDateTimeOrNull() != null -> VariableRuntimeTypeEnum.DATE_TIME
                content.toPeriodOrNull() != null -> VariableRuntimeTypeEnum.PERIOD
                content.toDurationOrNull() != null -> VariableRuntimeTypeEnum.DURATION
                else -> VariableRuntimeTypeEnum.STRING
              }
            }
            else -> error("Unknown primitive type: $jsonElement")
          }
    }
