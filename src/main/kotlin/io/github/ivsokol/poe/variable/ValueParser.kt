/* (C)2024 */
package io.github.ivsokol.poe.variable

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.ivsokol.poe.Options
import java.math.BigDecimal
import java.time.*
import java.time.format.DateTimeFormatter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger(PolicyVariableStatic::class.java)

// Regex for Integer and Long digits
private val REGEX_DIGIT = Regex("-?\\d+")
// Regex for Double, Float and BigDecimal digits
private val REGEX_DECIMAL_DIGIT = Regex("-?(\\d+\\.\\d+|\\.\\d+|\\d+\\.|\\d+)([eE][-+]?\\d+)?")

/**
 * Parses the provided value according to the specified type and format, using the provided options.
 *
 * @param value The value to be parsed.
 * @param type The type of the value to be parsed.
 * @param format The format of the value to be parsed, if applicable.
 * @param options The options to be used during parsing, such as date and time formatters, and an
 *   object mapper.
 * @return The parsed [VariableValue].
 */
internal fun parseValue(
    value: Any,
    type: VariableValueTypeEnum,
    format: VariableValueFormatEnum?,
    options: Options,
    timeFormat: String? = null,
    dateFormat: String? = null,
    dateTimeFormat: String? = null,
): VariableValue {
  val runtimeType = determineRuntimeType(value)
  return if (runtimeType == runtimeTypeFromTypeAndFormat(type, format)) {
    VariableValue(runtimeType, value)
  } else
      when (type) {
        VariableValueTypeEnum.STRING ->
            castString(
                value,
                format,
                if (dateFormat.isNullOrBlank()) options.dateFormatter
                else DateTimeFormatter.ofPattern(dateFormat),
                if (dateTimeFormat.isNullOrBlank()) options.dateTimeFormatter
                else DateTimeFormatter.ofPattern(dateTimeFormat),
                if (timeFormat.isNullOrBlank()) options.timeFormatter
                else DateTimeFormatter.ofPattern(timeFormat),
                options,
                options.objectMapper)
        VariableValueTypeEnum.BOOLEAN -> castBoolean(value, format, options.objectMapper)
        VariableValueTypeEnum.INT -> castInt(value, format, options.objectMapper)
        VariableValueTypeEnum.NUMBER -> castNumber(value, format, options.objectMapper)
        VariableValueTypeEnum.ARRAY -> castArray(value, format, options.objectMapper)
        VariableValueTypeEnum.OBJECT -> castObject(value, format, options.objectMapper)
      }
}

/**
 * Parses the provided string value according to the specified format, using the provided formatters
 * and object mapper.
 *
 * @param value The string value to be parsed.
 * @param format The format of the value to be parsed, if applicable.
 * @param dateFormatter The date formatter to be used for parsing date values.
 * @param dateTimeFormatter The date-time formatter to be used for parsing date-time values.
 * @param timeFormatter The time formatter to be used for parsing time values.
 * @param objectMapper The object mapper to be used for parsing JSON values.
 * @return The parsed [VariableValue], or [NullVariableValue] if parsing fails.
 */
private fun castString(
    value: Any,
    format: VariableValueFormatEnum?,
    dateFormatter: DateTimeFormatter,
    dateTimeFormatter: DateTimeFormatter,
    timeFormatter: DateTimeFormatter,
    options: Options,
    objectMapper: ObjectMapper
): VariableValue =
    when {
      value is String ->
          try {
            parseString(
                value,
                format,
                dateFormatter,
                dateTimeFormatter,
                timeFormatter,
                options,
                objectMapper)
          } catch (e: Exception) {
            logger.error("Error parsing string value: $value", e)
            NullVariableValue()
          }
      value is JsonNode && value.isTextual ->
          try {
            parseString(
                value.textValue(),
                format,
                dateFormatter,
                dateTimeFormatter,
                timeFormatter,
                options,
                objectMapper)
          } catch (e: Exception) {
            logger.error("Error parsing string value: $value", e)
            NullVariableValue()
          }
      else -> NullVariableValue()
    }

/**
 * Parses the provided string value according to the specified format, using the provided formatters
 * and object mapper.
 *
 * @param value The string value to be parsed.
 * @param format The format of the value to be parsed, if applicable.
 * @param dateFormatter The date formatter to be used for parsing date values.
 * @param dateTimeFormatter The date-time formatter to be used for parsing date-time values.
 * @param timeFormatter The time formatter to be used for parsing time values.
 * @param objectMapper The object mapper to be used for parsing JSON values.
 * @return The parsed [VariableValue], or [NullVariableValue] if parsing fails.
 */
private fun parseString(
    value: String,
    format: VariableValueFormatEnum?,
    dateFormatter: DateTimeFormatter,
    dateTimeFormatter: DateTimeFormatter,
    timeFormatter: DateTimeFormatter,
    options: Options,
    objectMapper: ObjectMapper
): VariableValue =
    when (format) {
      null -> VariableValue(VariableRuntimeTypeEnum.STRING, value)
      VariableValueFormatEnum.DATE ->
          VariableValue(VariableRuntimeTypeEnum.DATE, LocalDate.parse(value, dateFormatter))
      VariableValueFormatEnum.DATE_TIME ->
          runCatching {
                VariableValue(
                    VariableRuntimeTypeEnum.DATE_TIME,
                    OffsetDateTime.parse(value, dateTimeFormatter))
              }
              .getOrElse {
                // try to parse localDateTime, if such format provided
                val localDateTime = LocalDateTime.parse(value, dateTimeFormatter)
                val zoneOffset = options.zoneId.rules.getOffset(localDateTime)
                VariableValue(
                    VariableRuntimeTypeEnum.DATE_TIME, OffsetDateTime.of(localDateTime, zoneOffset))
              }
      VariableValueFormatEnum.TIME ->
          VariableValue(VariableRuntimeTypeEnum.TIME, LocalTime.parse(value, timeFormatter))
      VariableValueFormatEnum.PERIOD ->
          VariableValue(VariableRuntimeTypeEnum.PERIOD, Period.parse(value))
      VariableValueFormatEnum.DURATION ->
          VariableValue(VariableRuntimeTypeEnum.DURATION, Duration.parse(value))
      VariableValueFormatEnum.JSON ->
          VariableValue(VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree(value))
      else -> NullVariableValue()
    }

/**
 * Parses the provided value to a [VariableValue] of type [VariableRuntimeTypeEnum.BOOLEAN].
 *
 * If the provided [format] is [VariableValueFormatEnum.JSON], the value is parsed as a JSON node.
 * If the provided value is a [Boolean], it is returned as a [VariableValue] of type
 * [VariableRuntimeTypeEnum.BOOLEAN]. If the provided value is a [JsonNode] and it represents a
 * boolean value, it is returned as a [VariableValue] of type [VariableRuntimeTypeEnum.BOOLEAN]. If
 * the provided value is a [String] and it equals "true" or "false" (case-insensitive), it is
 * returned as a [VariableValue] of type [VariableRuntimeTypeEnum.BOOLEAN]. Otherwise,
 * [NullVariableValue] is returned.
 *
 * @param value The value to be parsed.
 * @param format The format of the value, if applicable.
 * @param objectMapper The object mapper to be used for parsing JSON values.
 * @return The parsed [VariableValue] of type [VariableRuntimeTypeEnum.BOOLEAN], or
 *   [NullVariableValue] if parsing fails.
 */
private fun castBoolean(
    value: Any,
    format: VariableValueFormatEnum?,
    objectMapper: ObjectMapper
): VariableValue =
    when {
      format != null && VariableValueFormatEnum.JSON == format ->
          VariableValue(VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree(value.toString()))
      value is Boolean -> VariableValue(VariableRuntimeTypeEnum.BOOLEAN, value)
      value is JsonNode && value.isBoolean ->
          VariableValue(VariableRuntimeTypeEnum.BOOLEAN, value.asBoolean())
      value is String && value.equals("true", ignoreCase = true) ->
          VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true)
      value is String && value.equals("false", ignoreCase = true) ->
          VariableValue(VariableRuntimeTypeEnum.BOOLEAN, false)
      else -> NullVariableValue()
    }

/**
 * Parses the provided value to a [VariableValue] of type [VariableRuntimeTypeEnum.INT] or
 * [VariableRuntimeTypeEnum.LONG].
 *
 * If the provided [format] is [VariableValueFormatEnum.JSON], the value is parsed as a JSON node.
 * If the provided value is an [Int], it is returned as a [VariableValue] of type
 * [VariableRuntimeTypeEnum.INT]. If the provided value is a [Long], it is returned as a
 * [VariableValue] of type [VariableRuntimeTypeEnum.LONG]. If the provided value is a [String] and
 * it matches the [REGEX_DIGIT] pattern, it is parsed as an [Int] or [Long] and returned as the
 * corresponding [VariableValue]. If the provided value is a [JsonNode] and it can be converted to
 * an [Int] or [Long], it is returned as the corresponding [VariableValue]. Otherwise,
 * [NullVariableValue] is returned.
 *
 * @param value The value to be parsed.
 * @param format The format of the value, if applicable.
 * @param objectMapper The object mapper to be used for parsing JSON values.
 * @return The parsed [VariableValue] of type [VariableRuntimeTypeEnum.INT] or
 *   [VariableRuntimeTypeEnum.LONG], or [NullVariableValue] if parsing fails.
 */
private fun castInt(
    value: Any,
    format: VariableValueFormatEnum?,
    objectMapper: ObjectMapper
): VariableValue =
    when {
      value is Int -> parseInt(value, format)
      value is Long -> parseLong(value, format)
      format != null && format == VariableValueFormatEnum.JSON ->
          VariableValue(VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree(value.toString()))
      format != null &&
          format == VariableValueFormatEnum.LONG &&
          value is String &&
          value.matches(REGEX_DIGIT) -> parseLong(value.toLong(), format)
      value is String && value.matches(REGEX_DIGIT) -> parseInt(value.toInt(), format)
      value is JsonNode && value.canConvertToInt() -> parseInt(value.asInt(), format)
      value is JsonNode && value.canConvertToLong() -> parseLong(value.asLong(), format)
      else -> NullVariableValue()
    }

/**
 * Parses the provided integer value to a [VariableValue] of type [VariableRuntimeTypeEnum.INT].
 *
 * If the provided [format] is not null, it returns [NullVariableValue] as the parsing is not
 * supported for the given format.
 *
 * @param value The integer value to be parsed.
 * @param format The format of the value, if applicable.
 * @return The parsed [VariableValue] of type [VariableRuntimeTypeEnum.INT], or [NullVariableValue]
 *   if parsing fails.
 */
private fun parseInt(value: Int, format: VariableValueFormatEnum?): VariableValue =
    when (format) {
      null -> VariableValue(VariableRuntimeTypeEnum.INT, value)
      else -> NullVariableValue()
    }

/**
 * Parses the provided long value to a [VariableValue] of type [VariableRuntimeTypeEnum.LONG].
 *
 * If the provided [format] is not null and is equal to [VariableValueFormatEnum.LONG], it returns a
 * [VariableValue] of type [VariableRuntimeTypeEnum.LONG] with the provided value. If the provided
 * [format] is not null and is not equal to [VariableValueFormatEnum.LONG], it returns
 * [NullVariableValue] as the parsing is not supported for the given format.
 *
 * @param value The long value to be parsed.
 * @param format The format of the value, if applicable.
 * @return The parsed [VariableValue] of type [VariableRuntimeTypeEnum.LONG], or [NullVariableValue]
 *   if parsing fails.
 */
private fun parseLong(value: Long, format: VariableValueFormatEnum?): VariableValue =
    when (format) {
      null -> VariableValue(VariableRuntimeTypeEnum.LONG, value)
      VariableValueFormatEnum.LONG -> VariableValue(VariableRuntimeTypeEnum.LONG, value)
      else -> NullVariableValue()
    }

/**
 * Parses the provided value to a [VariableValue] of the appropriate numeric type based on the
 * specified format.
 *
 * If the provided [format] is [VariableValueFormatEnum.JSON], the value is parsed as a JSON node.
 * If the provided [format] is [VariableValueFormatEnum.BIG_DECIMAL] and the value is a string
 * matching the [REGEX_DECIMAL_DIGIT] pattern, the value is parsed as a [BigDecimal]. If the value
 * is a string matching the [REGEX_DECIMAL_DIGIT] pattern, the value is parsed as a [Double]. If the
 * value is a [Number], the value is parsed according to the provided [format]. If the value is a
 * [JsonNode] and is a number, the value is parsed according to the provided [format]. If the value
 * cannot be parsed, [NullVariableValue] is returned.
 *
 * @param value The value to be parsed.
 * @param format The format of the value, if applicable.
 * @param objectMapper The ObjectMapper used for parsing JSON values.
 * @return The parsed [VariableValue] of the appropriate numeric type, or [NullVariableValue] if
 *   parsing fails.
 */
private fun castNumber(
    value: Any,
    format: VariableValueFormatEnum?,
    objectMapper: ObjectMapper
): VariableValue =
    when {
      format != null && format == VariableValueFormatEnum.JSON ->
          VariableValue(VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree(value.toString()))
      format != null &&
          format == VariableValueFormatEnum.BIG_DECIMAL &&
          value is String &&
          value.matches(REGEX_DECIMAL_DIGIT) ->
          VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, value.toBigDecimal())
      value is String && value.matches(REGEX_DECIMAL_DIGIT) -> parseNumber(value.toDouble(), format)
      value is Number -> parseNumber(value, format)
      value is JsonNode && value.isNumber -> parseNumber(value.asDouble(), format)
      else -> NullVariableValue()
    }

/**
 * Parses the provided numeric value to a [VariableValue] of the appropriate numeric type based on
 * the specified format.
 *
 * If the provided [format] is null or [VariableValueFormatEnum.DOUBLE], the value is parsed as a
 * [Double]. If the provided [format] is [VariableValueFormatEnum.FLOAT], the value is parsed as a
 * [Float]. If the provided [format] is [VariableValueFormatEnum.BIG_DECIMAL], the value is parsed
 * as a [BigDecimal]. If the provided [format] is not recognized, [NullVariableValue] is returned.
 *
 * @param value The numeric value to be parsed.
 * @param format The format of the value, if applicable.
 * @return The parsed [VariableValue] of the appropriate numeric type, or [NullVariableValue] if
 *   parsing fails.
 */
private fun parseNumber(value: Number, format: VariableValueFormatEnum?): VariableValue =
    when (format) {
      null -> VariableValue(VariableRuntimeTypeEnum.DOUBLE, value.toDouble())
      VariableValueFormatEnum.DOUBLE ->
          VariableValue(VariableRuntimeTypeEnum.DOUBLE, value.toDouble())
      VariableValueFormatEnum.FLOAT -> VariableValue(VariableRuntimeTypeEnum.FLOAT, value.toFloat())
      VariableValueFormatEnum.BIG_DECIMAL ->
          VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal.valueOf(value.toDouble()))
      else -> NullVariableValue()
    }

/**
 * Parses the provided value to a [VariableValue] of type [VariableRuntimeTypeEnum.OBJECT_NODE] if
 * the value is a JSON object.
 *
 * If the provided [format] is [VariableValueFormatEnum.JSON] and the [value] is a [JsonNode] that
 * represents a JSON object, this function will return a [VariableValue] with a runtime type of
 * [VariableRuntimeTypeEnum.OBJECT_NODE] and the [value] cast to an [ObjectNode].
 *
 * If the [format] is not [VariableValueFormatEnum.JSON] or the [value] is not a JSON object, this
 * function will call [parseAnyAsJsonObject] to attempt to parse the [value] as a JSON object.
 *
 * @param value The value to be parsed.
 * @param format The format of the value, if applicable.
 * @param objectMapper The ObjectMapper used for parsing JSON values.
 * @return The parsed [VariableValue] of type [VariableRuntimeTypeEnum.OBJECT_NODE], or
 *   [NullVariableValue] if parsing fails.
 */
private fun castObject(
    value: Any,
    format: VariableValueFormatEnum?,
    objectMapper: ObjectMapper
): VariableValue =
    when {
      value is JsonNode && value.isObject && format == VariableValueFormatEnum.JSON ->
          VariableValue(VariableRuntimeTypeEnum.OBJECT_NODE, value as ObjectNode)
      else -> parseAnyAsJsonObject(value, objectMapper)
    }

/**
 * Parses any value to a JSON object and returns a [VariableValue] of type
 * [VariableRuntimeTypeEnum.OBJECT_NODE] if parsing succeeds.
 *
 * If the provided [value] is a [String], it is first parsed to a [JsonNode] using
 * [parseStringToJsonNode]. Otherwise, the [ObjectMapper] is used to convert the [value] to a
 * [JsonNode].
 *
 * If the resulting [JsonNode] represents a JSON object, a [VariableValue] with a runtime type of
 * [VariableRuntimeTypeEnum.OBJECT_NODE] and the [JsonNode] cast to an [ObjectNode] is returned.
 * Otherwise, [NullVariableValue] is returned.
 *
 * @param value The value to be parsed to a JSON object.
 * @param objectMapper The [ObjectMapper] used for parsing JSON values.
 * @return The parsed [VariableValue] of type [VariableRuntimeTypeEnum.OBJECT_NODE], or
 *   [NullVariableValue] if parsing fails.
 */
private fun parseAnyAsJsonObject(value: Any, objectMapper: ObjectMapper): VariableValue {
  val jsonNode: JsonNode =
      if (value is String) parseStringToJsonNode(value, objectMapper)
      else objectMapper.valueToTree(value)
  return if (jsonNode.isObject) {
    VariableValue(VariableRuntimeTypeEnum.OBJECT_NODE, jsonNode as ObjectNode)
  } else NullVariableValue()
}

/**
 * Parses the provided JSON string to a [JsonNode] using the given [ObjectMapper]. If an exception
 * occurs during parsing, this function will return [NullNode.instance].
 *
 * @param value The JSON string to be parsed.
 * @param objectMapper The [ObjectMapper] to use for parsing the JSON string.
 * @return The parsed [JsonNode], or [NullNode.instance] if parsing fails.
 */
private fun parseStringToJsonNode(value: String, objectMapper: ObjectMapper): JsonNode =
    try {
      objectMapper.readTree(value)
    } catch (e: Exception) {
      // ignore exception
      NullNode.instance
    }

/**
 * Parses the provided value to a JSON array and returns a [VariableValue] of type
 * [VariableRuntimeTypeEnum.ARRAY_NODE] if parsing succeeds.
 *
 * If the provided [value] is a [JsonNode] and represents a JSON array, and the [format] is
 * [VariableValueFormatEnum.JSON], a [VariableValue] with a runtime type of
 * [VariableRuntimeTypeEnum.ARRAY_NODE] and the [JsonNode] cast to an [ArrayNode] is returned.
 *
 * Otherwise, the [parseAnyAsJsonArray] function is used to parse the [value] to a [JsonNode], and
 * if the resulting [JsonNode] represents a JSON array, a [VariableValue] with a runtime type of
 * [VariableRuntimeTypeEnum.ARRAY_NODE] and the [JsonNode] cast to an [ArrayNode] is returned. If
 * the parsing fails, [NullVariableValue] is returned.
 *
 * @param value The value to be parsed to a JSON array.
 * @param format The expected format of the [value].
 * @param objectMapper The [ObjectMapper] used for parsing JSON values.
 * @return The parsed [VariableValue] of type [VariableRuntimeTypeEnum.ARRAY_NODE], or
 *   [NullVariableValue] if parsing fails.
 */
private fun castArray(
    value: Any,
    format: VariableValueFormatEnum?,
    objectMapper: ObjectMapper
): VariableValue {
  if (value is JsonNode && value.isArray && format == VariableValueFormatEnum.JSON) {
    return VariableValue(VariableRuntimeTypeEnum.ARRAY_NODE, value as ArrayNode)
  }
  return parseAnyAsJsonArray(value, objectMapper)
}

/**
 * Parses the provided value to a JSON array and returns a [VariableValue] of type
 * [VariableRuntimeTypeEnum.ARRAY_NODE] if parsing succeeds.
 *
 * If the provided [value] is a [String], it is first parsed to a [JsonNode] using
 * [parseStringToJsonNode]. Otherwise, the [ObjectMapper] is used to convert the [value] to a
 * [JsonNode].
 *
 * If the resulting [JsonNode] represents a JSON array, a [VariableValue] with a runtime type of
 * [VariableRuntimeTypeEnum.ARRAY_NODE] and the [JsonNode] cast to an [ArrayNode] is returned. If
 * the parsing fails, [NullVariableValue] is returned.
 *
 * @param value The value to be parsed to a JSON array.
 * @param objectMapper The [ObjectMapper] used for parsing JSON values.
 * @return The parsed [VariableValue] of type [VariableRuntimeTypeEnum.ARRAY_NODE], or
 *   [NullVariableValue] if parsing fails.
 */
private fun parseAnyAsJsonArray(value: Any, objectMapper: ObjectMapper): VariableValue {
  val jsonNode: JsonNode =
      if (value is String) parseStringToJsonNode(value, objectMapper)
      else objectMapper.valueToTree(value)
  return if (jsonNode.isArray) {
    VariableValue(VariableRuntimeTypeEnum.ARRAY_NODE, jsonNode as ArrayNode)
  } else NullVariableValue()
}

/**
 * Checks that the provided variable value type and format are compatible.
 *
 * If the [format] is not null, this function checks that the [format] is compatible with the
 * provided [type]. For example, a [VariableValueTypeEnum.STRING] type can have formats like
 * [VariableValueFormatEnum.DATE], [VariableValueFormatEnum.DATE_TIME], etc. A
 * [VariableValueTypeEnum.NUMBER] type can have formats like [VariableValueFormatEnum.FLOAT],
 * [VariableValueFormatEnum.DOUBLE], etc.
 *
 * If the types and formats are not compatible, this function will throw an exception with a message
 * explaining the incompatibility.
 *
 * @param type The type of the variable value.
 * @param format The format of the variable value, or null if no format is specified.
 * @throws IllegalArgumentException if the [format] is not compatible with the [type].
 */
internal fun checkTypeAndFormatCompliance(
    type: VariableValueTypeEnum?,
    format: VariableValueFormatEnum?
) {
  if (type == null || format == null) return
  when (type) {
    VariableValueTypeEnum.STRING ->
        check(
            format in
                setOf(
                    VariableValueFormatEnum.DATE,
                    VariableValueFormatEnum.DATE_TIME,
                    VariableValueFormatEnum.TIME,
                    VariableValueFormatEnum.PERIOD,
                    VariableValueFormatEnum.DURATION,
                    VariableValueFormatEnum.JSON)) {
              "Variable value format $format is not compatible with variable type $type"
            }
    VariableValueTypeEnum.NUMBER ->
        check(
            format in
                setOf(
                    VariableValueFormatEnum.FLOAT,
                    VariableValueFormatEnum.DOUBLE,
                    VariableValueFormatEnum.BIG_DECIMAL,
                    VariableValueFormatEnum.JSON,
                )) {
              "Variable value format $format is not compatible with variable type $type"
            }
    VariableValueTypeEnum.BOOLEAN ->
        check(format == VariableValueFormatEnum.JSON) {
          "Variable value format $format is not compatible with variable type $type"
        }
    VariableValueTypeEnum.INT ->
        check(
            format in
                setOf(
                    VariableValueFormatEnum.LONG,
                    VariableValueFormatEnum.JSON,
                )) {
              "Variable value format $format is not compatible with variable type $type"
            }
    VariableValueTypeEnum.OBJECT,
    VariableValueTypeEnum.ARRAY ->
        check(format == VariableValueFormatEnum.JSON) {
          "Variable value format $format is not compatible with variable type $type"
        }
  }
}
