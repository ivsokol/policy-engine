package com.github.ivsokol.poe.policy

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Serializes and deserializes [IPolicyRefOrValue] instances to and from JSON.
 *
 * This serializer handles both [PolicyRef] and [IPolicySerializer] instances, selecting the
 * appropriate deserializer based on the JSON object structure.
 */
object IPolicyRefOrValueSerializer :
    JsonContentPolymorphicSerializer<IPolicyRefOrValue>(IPolicyRefOrValue::class) {
  override fun selectDeserializer(
      element: JsonElement
  ): DeserializationStrategy<IPolicyRefOrValue> =
      when {
        element is JsonObject -> parseJsonObject(element)
        else ->
            throw IllegalArgumentException(
                "Not correct JsonElement for IPolicyRefOrValue DeserializationStrategy")
      }

  private fun parseJsonObject(element: JsonObject) =
      when {
        element.containsKey("refType") -> PolicyRef.serializer()
        element.containsKey("targetEffect") ||
            element.containsKey("policyCombinationLogic") ||
            element.containsKey("default") -> IPolicySerializer
        else ->
            throw IllegalArgumentException(
                "No corresponding field for IPolicyRefOrValue DeserializationStrategy")
      }
}
