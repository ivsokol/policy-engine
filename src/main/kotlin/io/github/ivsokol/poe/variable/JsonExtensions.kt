package io.github.ivsokol.poe.variable

import kotlinx.serialization.json.*

enum class JsonElementKind {
  NULL,
  BOOLEAN,
  STRING,
  NUMBER,
  INT,
  OBJECT,
  ARRAY
}

/**
 * Parses a nullable string to a JSON-compatible [Any] value.
 *
 * If the input string is null, returns null. If the input string is blank or empty, returns the
 * string itself. Otherwise, parses the string to a [JsonElement] using the provided [Json]
 * instance, and then recursively parses the [JsonElement] to a [Any] value using
 * [parseStringToJsonAny].
 *
 * @param json The [Json] instance to use for parsing the input string. Defaults to a [Json]
 *   instance with `ignoreUnknownKeys = false`.
 * @return The parsed [Any] value, or null if the input string is null.
 */
fun String?.toJsonAny(json: Json = Json { ignoreUnknownKeys = false }): Any? {
  if (this == null) return null
  if (this.isBlank() || this.isEmpty()) return this
  return parseStringToJsonAny(json.parseToJsonElement(this.trim()))
}

/**
 * Recursively parses a [JsonElement] to a [Any] value.
 *
 * This function takes a [JsonElement] and recursively parses it to a [Any] value. It handles the
 * different types of [JsonElement] (null, boolean, int, number, string, object, array) and returns
 * the corresponding [Any] value.
 *
 * @param elem The [JsonElement] to parse.
 * @return The parsed [Any] value, or null if the [JsonElement] is null.
 */
internal fun parseStringToJsonAny(elem: JsonElement): Any? {
  val decodeElem = elem.kind()
  return when (decodeElem.first) {
    JsonElementKind.NULL -> null
    JsonElementKind.BOOLEAN -> decodeElem.second
    JsonElementKind.INT -> decodeElem.second
    JsonElementKind.NUMBER -> decodeElem.second
    JsonElementKind.STRING -> decodeElem.second
    JsonElementKind.OBJECT -> {
      val obj = mutableMapOf<String, Any?>()
      (decodeElem.second as? JsonObject)?.forEach { obj[it.key] = parseStringToJsonAny(it.value) }
      obj
    }
    JsonElementKind.ARRAY -> {
      val arr = mutableListOf<Any?>()
      (decodeElem.second as? JsonArray)?.forEach {
        parseStringToJsonAny(it).let { i -> arr.add(i) }
      }
      arr
    }
  }
}

private fun JsonElement.isNull() = runCatching { this.jsonNull }

private fun JsonElement.isPrimitive() = runCatching { this.jsonPrimitive }

private fun JsonElement.isObject() = runCatching { this.jsonObject }

private fun JsonElement.isArray() = runCatching { this.jsonArray }

/**
 * Determines the type of the [JsonElement] and returns a pair of the [JsonElementKind] and the
 * corresponding value.
 *
 * This function examines the [JsonElement] and returns a pair containing the type of the element
 * (as a [JsonElementKind]) and the value of the element. It handles the different types of
 * [JsonElement] (null, primitive, object, array) and returns the appropriate pair.
 *
 * @return A pair containing the [JsonElementKind] and the corresponding value of the [JsonElement].
 * @throws IllegalArgumentException if the [JsonElement] is of an unknown type.
 */
internal fun JsonElement.kind(): Pair<JsonElementKind, Any> {
  if (this.isNull().isSuccess) return Pair(JsonElementKind.NULL, JsonNull)
  val primitive = this.isPrimitive()
  if (primitive.isSuccess) {
    return primitive.getOrThrow().kind()
  }
  val obj = this.isObject()
  if (obj.isSuccess) {
    return Pair(JsonElementKind.OBJECT, obj.getOrThrow())
  }
  val arr = this.isArray()
  if (arr.isSuccess) {
    return Pair(JsonElementKind.ARRAY, arr.getOrThrow())
  }
  throw IllegalArgumentException("Unknown JsonElement kind")
}

private fun JsonPrimitive.isBoolean() = kotlin.runCatching { this.boolean }

private fun JsonPrimitive.isInt() = kotlin.runCatching { this.int }

private fun JsonPrimitive.isLong() = kotlin.runCatching { this.long }

private fun JsonPrimitive.isNumber() = kotlin.runCatching { this.double }

private fun JsonPrimitive.isString() = kotlin.runCatching { this.content }

/**
 * Determines the type of the [JsonPrimitive] and returns a pair of the [JsonElementKind] and the
 * corresponding value.
 *
 * This function examines the [JsonPrimitive] and returns a pair containing the type of the element
 * (as a [JsonElementKind]) and the value of the element. It handles the different types of
 * [JsonPrimitive] (boolean, int, long, number, string) and returns the appropriate pair.
 *
 * @return A pair containing the [JsonElementKind] and the corresponding value of the
 *   [JsonPrimitive].
 * @throws IllegalArgumentException if the [JsonPrimitive] is of an unknown type.
 */
internal fun JsonPrimitive.kind(): Pair<JsonElementKind, Any> {
  val bool = this.isBoolean()
  if (bool.isSuccess) return Pair(JsonElementKind.BOOLEAN, bool.getOrThrow())
  val int = this.isInt()
  if (int.isSuccess) return Pair(JsonElementKind.INT, int.getOrThrow())
  val long = this.isLong()
  if (long.isSuccess) return Pair(JsonElementKind.INT, long.getOrThrow())
  val number = this.isNumber()
  if (number.isSuccess) return Pair(JsonElementKind.NUMBER, number.getOrThrow())
  val string = this.isString()
  if (string.isSuccess) return Pair(JsonElementKind.STRING, string.getOrThrow())
  throw IllegalArgumentException("Unknown JsonPrimitive type")
}
