package com.github.ivsokol.poe.variable

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.ivsokol.poe.Options
import java.math.BigDecimal
import java.time.*

/**
 * Casts the given [VariableValue] to the specified [VariableRuntimeTypeEnum] type.
 *
 * @param type The target [VariableRuntimeTypeEnum] type to cast to.
 * @param value The [VariableValue] to be cast.
 * @param options The [Options] to use for the casting operation.
 * @return The cast [VariableValue].
 * @throws IllegalArgumentException If the cast is not possible.
 */
fun cast(type: VariableRuntimeTypeEnum, value: VariableValue, options: Options): VariableValue {
  return when (type) {
    VariableRuntimeTypeEnum.STRING -> castToString(value, options)
    VariableRuntimeTypeEnum.NULL -> throw IllegalArgumentException("Cannot cast null")
    VariableRuntimeTypeEnum.UNKNOWN -> throw IllegalArgumentException("Cannot cast unknown")
    VariableRuntimeTypeEnum.DATE -> castToDate(value, options)
    VariableRuntimeTypeEnum.DATE_TIME -> castToDateTime(value, options)
    VariableRuntimeTypeEnum.TIME -> castToTime(value, options)
    VariableRuntimeTypeEnum.PERIOD -> castToPeriod(value, options)
    VariableRuntimeTypeEnum.DURATION -> castToDuration(value, options)
    VariableRuntimeTypeEnum.LONG -> castToLong(value, options)
    VariableRuntimeTypeEnum.INT -> castToInt(value, options)
    VariableRuntimeTypeEnum.DOUBLE -> castToDouble(value, options)
    VariableRuntimeTypeEnum.FLOAT -> castToFloat(value, options)
    VariableRuntimeTypeEnum.BIG_DECIMAL -> castToBigDecimal(value, options)
    VariableRuntimeTypeEnum.BOOLEAN -> castToBoolean(value, options)
    VariableRuntimeTypeEnum.JSON_NODE -> castToJsonNode(value, options)
    VariableRuntimeTypeEnum.OBJECT_NODE -> castToObjectNode(value, options)
    VariableRuntimeTypeEnum.ARRAY_NODE -> castToArrayNode(value, options)
    VariableRuntimeTypeEnum.ARRAY -> castToArray(value)
  }
}

/**
 * Casts the given [VariableValue] to an array type.
 *
 * @param value The [VariableValue] to be cast.
 * @return The cast [VariableValue] as an array.
 * @throws IllegalArgumentException If the cast is not possible.
 */
private fun castToArray(value: VariableValue) =
    when (value.type) {
      VariableRuntimeTypeEnum.ARRAY -> value
      else -> throw IllegalArgumentException("Cannot cast ${value.type} to array")
    }

/**
 * Casts the given [VariableValue] to an ArrayNode type.
 *
 * @param value The [VariableValue] to be cast.
 * @param options The [Options] to use for the casting operation.
 * @return The cast [VariableValue] as an ArrayNode.
 * @throws IllegalArgumentException If the cast is not possible.
 */
private fun castToArrayNode(value: VariableValue, options: Options) =
    when (value.type) {
      VariableRuntimeTypeEnum.ARRAY_NODE -> value
      VariableRuntimeTypeEnum.STRING -> {
        val jsonNode = options.objectMapper.readTree(value.body as String)
        if (jsonNode.isArray) VariableValue(VariableRuntimeTypeEnum.ARRAY_NODE, jsonNode)
        else throw IllegalArgumentException("Cannot cast ${value.type} to ArrayNode")
      }
      VariableRuntimeTypeEnum.JSON_NODE ->
          if (value.body is ArrayNode) VariableValue(VariableRuntimeTypeEnum.ARRAY_NODE, value.body)
          else throw IllegalArgumentException("Cannot cast ${value.type} to ArrayNode")
      VariableRuntimeTypeEnum.ARRAY -> {
        val arrayNode = options.objectMapper.valueToTree<ArrayNode>(value.body)
        VariableValue(VariableRuntimeTypeEnum.ARRAY_NODE, arrayNode)
      }
      else -> throw IllegalArgumentException("Cannot cast ${value.type} to ArrayNode")
    }

/**
 * Casts the given [VariableValue] to an ObjectNode type.
 *
 * @param value The [VariableValue] to be cast.
 * @param options The [Options] to use for the casting operation.
 * @return The cast [VariableValue] as an ObjectNode.
 * @throws IllegalArgumentException If the cast is not possible.
 */
private fun castToObjectNode(value: VariableValue, options: Options) =
    when (value.type) {
      VariableRuntimeTypeEnum.OBJECT_NODE -> value
      VariableRuntimeTypeEnum.STRING -> {
        val jsonNode = options.objectMapper.readTree(value.body as String)
        if (jsonNode.isObject) VariableValue(VariableRuntimeTypeEnum.OBJECT_NODE, jsonNode)
        else throw IllegalArgumentException("Cannot cast ${value.type} to ObjectNode")
      }
      VariableRuntimeTypeEnum.JSON_NODE ->
          if (value.body is ObjectNode)
              VariableValue(VariableRuntimeTypeEnum.OBJECT_NODE, value.body)
          else throw IllegalArgumentException("Cannot cast ${value.type} to ObjectNode")
      else -> throw IllegalArgumentException("Cannot cast ${value.type} to ObjectNode")
    }

/**
 * Casts the given [VariableValue] to a JsonNode type.
 *
 * @param value The [VariableValue] to be cast.
 * @param options The [Options] to use for the casting operation.
 * @return The cast [VariableValue] as a JsonNode.
 * @throws IllegalArgumentException If the cast is not possible.
 */
private fun castToJsonNode(value: VariableValue, options: Options) =
    when (value.type) {
      VariableRuntimeTypeEnum.UNKNOWN ->
          throw IllegalArgumentException("Cannot cast ${value.type} to JsonNode")
      VariableRuntimeTypeEnum.NULL ->
          VariableValue(VariableRuntimeTypeEnum.JSON_NODE, NullNode.instance)
      VariableRuntimeTypeEnum.OBJECT_NODE ->
          VariableValue(VariableRuntimeTypeEnum.JSON_NODE, value.body as JsonNode)
      VariableRuntimeTypeEnum.ARRAY_NODE ->
          VariableValue(VariableRuntimeTypeEnum.JSON_NODE, value.body as JsonNode)
      VariableRuntimeTypeEnum.JSON_NODE -> value
      else ->
          VariableValue(
              VariableRuntimeTypeEnum.JSON_NODE, options.objectMapper.valueToTree(value.body))
    }

/**
 * Casts the given [VariableValue] to a boolean type.
 *
 * @param value The [VariableValue] to be cast.
 * @param options The [Options] to use for the casting operation.
 * @return The cast [VariableValue] as a boolean.
 * @throws IllegalArgumentException If the cast is not possible.
 */
private fun castToBoolean(value: VariableValue, options: Options) =
    when (value.type) {
      VariableRuntimeTypeEnum.STRING ->
          VariableValue(VariableRuntimeTypeEnum.BOOLEAN, (value.body as String).toBoolean())
      VariableRuntimeTypeEnum.JSON_NODE ->
          VariableValue(
              VariableRuntimeTypeEnum.BOOLEAN,
              ((jsonToString(options, value)).body as String).toBoolean())
      VariableRuntimeTypeEnum.BOOLEAN -> value
      else -> throw IllegalArgumentException("Cannot cast ${value.type} to boolean")
    }

/**
 * Casts the given [VariableValue] to a BigDecimal type.
 *
 * @param value The [VariableValue] to be cast.
 * @param options The [Options] to use for the casting operation.
 * @return The cast [VariableValue] as a BigDecimal.
 * @throws IllegalArgumentException If the cast is not possible.
 */
private fun castToBigDecimal(value: VariableValue, options: Options) =
    when (value.type) {
      VariableRuntimeTypeEnum.STRING ->
          VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal(value.body as String))
      VariableRuntimeTypeEnum.JSON_NODE ->
          VariableValue(
              VariableRuntimeTypeEnum.BIG_DECIMAL,
              ((jsonToString(options, value)).body as String).toBigDecimal())
      VariableRuntimeTypeEnum.BIG_DECIMAL -> value
      VariableRuntimeTypeEnum.LONG ->
          VariableValue(
              VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal((value.body as Long).toString()))
      VariableRuntimeTypeEnum.INT ->
          VariableValue(
              VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal((value.body as Int).toString()))
      VariableRuntimeTypeEnum.DOUBLE ->
          VariableValue(
              VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal((value.body as Double).toString()))
      VariableRuntimeTypeEnum.FLOAT ->
          VariableValue(
              VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal((value.body as Float).toString()))
      else -> throw IllegalArgumentException("Cannot cast ${value.type} to big decimal")
    }

/**
 * Casts the given [VariableValue] to a Float type.
 *
 * @param value The [VariableValue] to be cast.
 * @param options The [Options] to use for the casting operation.
 * @return The cast [VariableValue] as a Float.
 * @throws IllegalArgumentException If the cast is not possible.
 */
private fun castToFloat(value: VariableValue, options: Options) =
    when (value.type) {
      VariableRuntimeTypeEnum.STRING ->
          VariableValue(VariableRuntimeTypeEnum.FLOAT, (value.body as String).toFloat())
      VariableRuntimeTypeEnum.JSON_NODE ->
          VariableValue(
              VariableRuntimeTypeEnum.FLOAT,
              ((jsonToString(options, value)).body as String).toFloat())
      VariableRuntimeTypeEnum.FLOAT -> value
      VariableRuntimeTypeEnum.LONG ->
          VariableValue(VariableRuntimeTypeEnum.FLOAT, (value.body as Long).toFloat())
      VariableRuntimeTypeEnum.INT ->
          VariableValue(VariableRuntimeTypeEnum.FLOAT, (value.body as Int).toFloat())
      VariableRuntimeTypeEnum.DOUBLE ->
          VariableValue(VariableRuntimeTypeEnum.FLOAT, (value.body as Double).toFloat())
      VariableRuntimeTypeEnum.BIG_DECIMAL ->
          VariableValue(VariableRuntimeTypeEnum.FLOAT, (value.body as BigDecimal).toFloat())
      else -> throw IllegalArgumentException("Cannot cast ${value.type} to float")
    }

/**
 * Casts the given [VariableValue] to a Double type.
 *
 * @param value The [VariableValue] to be cast.
 * @param options The [Options] to use for the casting operation.
 * @return The cast [VariableValue] as a Double.
 * @throws IllegalArgumentException If the cast is not possible.
 */
private fun castToDouble(value: VariableValue, options: Options) =
    when (value.type) {
      VariableRuntimeTypeEnum.STRING ->
          VariableValue(VariableRuntimeTypeEnum.DOUBLE, (value.body as String).toDouble())
      VariableRuntimeTypeEnum.JSON_NODE ->
          VariableValue(
              VariableRuntimeTypeEnum.DOUBLE,
              ((jsonToString(options, value)).body as String).toDouble())
      VariableRuntimeTypeEnum.DOUBLE -> value
      VariableRuntimeTypeEnum.LONG ->
          VariableValue(VariableRuntimeTypeEnum.DOUBLE, (value.body as Long).toDouble())
      VariableRuntimeTypeEnum.INT ->
          VariableValue(VariableRuntimeTypeEnum.DOUBLE, (value.body as Int).toDouble())
      VariableRuntimeTypeEnum.FLOAT ->
          VariableValue(VariableRuntimeTypeEnum.DOUBLE, (value.body as Float).toDouble())
      VariableRuntimeTypeEnum.BIG_DECIMAL ->
          VariableValue(VariableRuntimeTypeEnum.DOUBLE, (value.body as BigDecimal).toDouble())
      else -> throw IllegalArgumentException("Cannot cast ${value.type} to double")
    }

/**
 * Casts the given [VariableValue] to an Int type.
 *
 * @param value The [VariableValue] to be cast.
 * @param options The [Options] to use for the casting operation.
 * @return The cast [VariableValue] as an Int.
 * @throws IllegalArgumentException If the cast is not possible.
 */
private fun castToInt(value: VariableValue, options: Options) =
    when (value.type) {
      VariableRuntimeTypeEnum.STRING ->
          VariableValue(VariableRuntimeTypeEnum.INT, (value.body as String).toInt())
      VariableRuntimeTypeEnum.JSON_NODE ->
          VariableValue(
              VariableRuntimeTypeEnum.INT, ((jsonToString(options, value)).body as String).toInt())
      VariableRuntimeTypeEnum.INT -> value
      VariableRuntimeTypeEnum.LONG ->
          VariableValue(VariableRuntimeTypeEnum.INT, (value.body as Long).toInt())
      VariableRuntimeTypeEnum.DOUBLE ->
          VariableValue(VariableRuntimeTypeEnum.INT, (value.body as Double).toInt())
      VariableRuntimeTypeEnum.FLOAT ->
          VariableValue(VariableRuntimeTypeEnum.INT, (value.body as Float).toInt())
      VariableRuntimeTypeEnum.BIG_DECIMAL ->
          VariableValue(VariableRuntimeTypeEnum.INT, (value.body as BigDecimal).toInt())
      else -> throw IllegalArgumentException("Cannot cast ${value.type} to int")
    }

/**
 * Casts the given [VariableValue] to a Long type.
 *
 * @param value The [VariableValue] to be cast.
 * @param options The [Options] to use for the casting operation.
 * @return The cast [VariableValue] as a Long.
 * @throws IllegalArgumentException If the cast is not possible.
 */
private fun castToLong(value: VariableValue, options: Options) =
    when (value.type) {
      VariableRuntimeTypeEnum.STRING ->
          VariableValue(VariableRuntimeTypeEnum.LONG, (value.body as String).toLong())
      VariableRuntimeTypeEnum.JSON_NODE ->
          VariableValue(
              VariableRuntimeTypeEnum.LONG,
              ((jsonToString(options, value)).body as String).toLong())
      VariableRuntimeTypeEnum.LONG -> value
      VariableRuntimeTypeEnum.INT ->
          VariableValue(VariableRuntimeTypeEnum.LONG, (value.body as Int).toLong())
      VariableRuntimeTypeEnum.DOUBLE ->
          VariableValue(VariableRuntimeTypeEnum.LONG, (value.body as Double).toLong())
      VariableRuntimeTypeEnum.FLOAT ->
          VariableValue(VariableRuntimeTypeEnum.LONG, (value.body as Float).toLong())
      VariableRuntimeTypeEnum.BIG_DECIMAL ->
          VariableValue(VariableRuntimeTypeEnum.LONG, (value.body as BigDecimal).toLong())
      else -> throw IllegalArgumentException("Cannot cast ${value.type} to long")
    }

/**
 * Casts the given [VariableValue] to a Duration type.
 *
 * @param value The [VariableValue] to be cast.
 * @param options The [Options] to use for the casting operation.
 * @return The cast [VariableValue] as a Duration.
 * @throws IllegalArgumentException If the cast is not possible.
 */
private fun castToDuration(value: VariableValue, options: Options) =
    when (value.type) {
      VariableRuntimeTypeEnum.STRING ->
          VariableValue(VariableRuntimeTypeEnum.DURATION, Duration.parse(value.body as String))
      VariableRuntimeTypeEnum.JSON_NODE ->
          VariableValue(
              VariableRuntimeTypeEnum.DURATION,
              Duration.parse(jsonToString(options, value).body as String))
      VariableRuntimeTypeEnum.DURATION -> value
      else -> throw IllegalArgumentException("Cannot cast ${value.type} to duration")
    }

/**
 * Casts the given [VariableValue] to a Period type.
 *
 * @param value The [VariableValue] to be cast.
 * @param options The [Options] to use for the casting operation.
 * @return The cast [VariableValue] as a Period.
 * @throws IllegalArgumentException If the cast is not possible.
 */
private fun castToPeriod(value: VariableValue, options: Options) =
    when (value.type) {
      VariableRuntimeTypeEnum.STRING ->
          VariableValue(VariableRuntimeTypeEnum.PERIOD, Period.parse(value.body as String))
      VariableRuntimeTypeEnum.JSON_NODE ->
          VariableValue(
              VariableRuntimeTypeEnum.PERIOD,
              Period.parse(jsonToString(options, value).body as String))
      VariableRuntimeTypeEnum.PERIOD -> value
      VariableRuntimeTypeEnum.DURATION ->
          VariableValue(
              VariableRuntimeTypeEnum.PERIOD,
              Period.ofDays((value.body as Duration).toDays().toInt()))
      else -> throw IllegalArgumentException("Cannot cast ${value.type} to period")
    }

/**
 * Casts the given [VariableValue] to a Time type.
 *
 * @param value The [VariableValue] to be cast.
 * @param options The [Options] to use for the casting operation.
 * @return The cast [VariableValue] as a Time.
 * @throws IllegalArgumentException If the cast is not possible.
 */
private fun castToTime(value: VariableValue, options: Options) =
    when (value.type) {
      VariableRuntimeTypeEnum.STRING ->
          VariableValue(
              VariableRuntimeTypeEnum.TIME,
              LocalTime.parse(value.body as String, options.timeFormatter))
      VariableRuntimeTypeEnum.JSON_NODE ->
          VariableValue(
              VariableRuntimeTypeEnum.TIME,
              LocalTime.parse(jsonToString(options, value).body as String, options.timeFormatter))
      VariableRuntimeTypeEnum.TIME -> value
      VariableRuntimeTypeEnum.DATE_TIME ->
          VariableValue(VariableRuntimeTypeEnum.TIME, (value.body as OffsetDateTime).toLocalTime())
      else -> throw IllegalArgumentException("Cannot cast ${value.type} to time")
    }

/**
 * Casts the given [VariableValue] to a DateTime type.
 *
 * @param value The [VariableValue] to be cast.
 * @param options The [Options] to use for the casting operation.
 * @return The cast [VariableValue] as a DateTime.
 * @throws IllegalArgumentException If the cast is not possible.
 */
private fun castToDateTime(value: VariableValue, options: Options) =
    when (value.type) {
      VariableRuntimeTypeEnum.STRING ->
          VariableValue(
              VariableRuntimeTypeEnum.DATE_TIME,
              OffsetDateTime.parse(value.body as String, options.dateTimeFormatter))
      VariableRuntimeTypeEnum.JSON_NODE ->
          VariableValue(
              VariableRuntimeTypeEnum.DATE_TIME,
              OffsetDateTime.parse(
                  jsonToString(options, value).body as String, options.dateTimeFormatter))
      VariableRuntimeTypeEnum.DATE ->
          VariableValue(
              VariableRuntimeTypeEnum.DATE_TIME,
              OffsetDateTime.from((value.body as LocalDate).atStartOfDay().atZone(options.zoneId)))
      VariableRuntimeTypeEnum.DATE_TIME -> value
      else -> throw IllegalArgumentException("Cannot cast ${value.type} to dateTime")
    }

/**
 * Casts the given [VariableValue] to a Date type.
 *
 * @param value The [VariableValue] to be cast.
 * @param options The [Options] to use for the casting operation.
 * @return The cast [VariableValue] as a Date.
 * @throws IllegalArgumentException If the cast is not possible.
 */
private fun castToDate(value: VariableValue, options: Options) =
    when (value.type) {
      VariableRuntimeTypeEnum.STRING ->
          VariableValue(
              VariableRuntimeTypeEnum.DATE,
              LocalDate.parse(value.body as String, options.dateFormatter))
      VariableRuntimeTypeEnum.JSON_NODE ->
          VariableValue(
              VariableRuntimeTypeEnum.DATE,
              LocalDate.parse(jsonToString(options, value).body as String, options.dateFormatter))
      VariableRuntimeTypeEnum.DATE -> value
      VariableRuntimeTypeEnum.DATE_TIME ->
          VariableValue(VariableRuntimeTypeEnum.DATE, (value.body as OffsetDateTime).toLocalDate())
      else -> throw IllegalArgumentException("Cannot cast ${value.type} to date")
    }

/**
 * Casts the given [VariableValue] to a String type.
 *
 * @param value The [VariableValue] to be cast.
 * @param options The [Options] to use for the casting operation.
 * @return The cast [VariableValue] as a String.
 * @throws IllegalArgumentException If the cast is not possible.
 */
private fun castToString(value: VariableValue, options: Options) =
    when (value.type) {
      VariableRuntimeTypeEnum.STRING -> value
      VariableRuntimeTypeEnum.BOOLEAN ->
          VariableValue(VariableRuntimeTypeEnum.STRING, value.body.toString())
      VariableRuntimeTypeEnum.DATE ->
          VariableValue(
              VariableRuntimeTypeEnum.STRING,
              (value.body as LocalDate).format(options.dateFormatter))
      VariableRuntimeTypeEnum.DATE_TIME ->
          VariableValue(
              VariableRuntimeTypeEnum.STRING,
              (value.body as OffsetDateTime).format(options.dateTimeFormatter))
      VariableRuntimeTypeEnum.TIME ->
          VariableValue(
              VariableRuntimeTypeEnum.STRING,
              (value.body as LocalTime).format(options.timeFormatter))
      VariableRuntimeTypeEnum.PERIOD ->
          VariableValue(VariableRuntimeTypeEnum.STRING, (value.body as Period).toString())
      VariableRuntimeTypeEnum.DURATION ->
          VariableValue(VariableRuntimeTypeEnum.STRING, (value.body as Duration).toString())
      VariableRuntimeTypeEnum.LONG ->
          VariableValue(VariableRuntimeTypeEnum.STRING, value.body.toString())
      VariableRuntimeTypeEnum.INT ->
          VariableValue(VariableRuntimeTypeEnum.STRING, value.body.toString())
      VariableRuntimeTypeEnum.DOUBLE ->
          VariableValue(VariableRuntimeTypeEnum.STRING, value.body.toString())
      VariableRuntimeTypeEnum.FLOAT ->
          VariableValue(VariableRuntimeTypeEnum.STRING, value.body.toString())
      VariableRuntimeTypeEnum.BIG_DECIMAL ->
          VariableValue(VariableRuntimeTypeEnum.STRING, value.body.toString())
      VariableRuntimeTypeEnum.JSON_NODE -> jsonToString(options, value)
      VariableRuntimeTypeEnum.OBJECT_NODE -> jsonToString(options, value)
      VariableRuntimeTypeEnum.ARRAY_NODE -> jsonToString(options, value)
      VariableRuntimeTypeEnum.ARRAY -> jsonToString(options, value)
      else -> throw IllegalArgumentException("Cannot cast ${value.type} to string")
    }

/**
 * Converts the given [VariableValue] to a JSON string representation.
 *
 * @param options The [Options] to use for the conversion.
 * @param value The [VariableValue] to be converted to a JSON string.
 * @return The JSON string representation of the [VariableValue].
 */
private fun jsonToString(options: Options, value: VariableValue) =
    VariableValue(
        VariableRuntimeTypeEnum.STRING,
        options.objectMapper.writeValueAsString(value.body)?.let { parseJacksonOutput(it) })

/**
 * Parses the given JSON string value, removing any surrounding double quotes if present.
 *
 * @param value The JSON string value to be parsed.
 * @return The parsed string value, with any surrounding double quotes removed.
 */
private fun parseJacksonOutput(value: String?): String? =
    value?.let {
      if (it.length >= 2 && it.startsWith("\"") && it.endsWith("\"")) it.substring(1, it.length - 1)
      else it
    }
