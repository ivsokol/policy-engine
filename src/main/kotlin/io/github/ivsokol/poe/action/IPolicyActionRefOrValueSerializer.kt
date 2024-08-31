package io.github.ivsokol.poe.action

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Serializes and deserializes [IPolicyActionRefOrValue] instances to and from JSON.
 *
 * This serializer handles both [PolicyActionRef] and [IPolicyAction] instances, selecting the
 * appropriate deserializer based on the JSON element structure.
 */
object IPolicyActionRefOrValueSerializer :
    JsonContentPolymorphicSerializer<IPolicyActionRefOrValue>(IPolicyActionRefOrValue::class) {
  override fun selectDeserializer(
      element: JsonElement
  ): DeserializationStrategy<IPolicyActionRefOrValue> =
      when {
        element is JsonObject -> parseJsonObject(element)
        else ->
            throw IllegalArgumentException(
                "Not correct JsonElement for IPolicyActionRefOrValue DeserializationStrategy")
      }

  private fun parseJsonObject(element: JsonObject) =
      when {
        element.containsKey("refType") -> PolicyActionRef.serializer()
        element.containsKey("type") -> IPolicyActionSerializer
        else ->
            throw IllegalArgumentException(
                "No corresponding field for IPolicyActionRefOrValue DeserializationStrategy")
      }
}
