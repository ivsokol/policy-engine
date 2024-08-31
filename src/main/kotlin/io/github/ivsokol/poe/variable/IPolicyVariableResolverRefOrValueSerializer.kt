package io.github.ivsokol.poe.variable

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * A polymorphic serializer for [IPolicyVariableResolverRefOrValue] that selects the appropriate
 * deserialization strategy based on the JSON element structure.
 *
 * This serializer handles two cases:
 * 1. When the JSON element is a [JsonObject] with a "refType" field, it uses the
 *    [PolicyVariableResolverRef.serializer] to deserialize the element.
 * 2. When the JSON element is a [JsonObject] with a "key" or "path" field, it uses the
 *    [PolicyVariableResolver.serializer] to deserialize the element.
 *
 * If the JSON element does not match either of these cases, an [IllegalArgumentException] is
 * thrown.
 */
object IPolicyVariableResolverRefOrValueSerializer :
    JsonContentPolymorphicSerializer<IPolicyVariableResolverRefOrValue>(
        IPolicyVariableResolverRefOrValue::class) {
  override fun selectDeserializer(
      element: JsonElement
  ): DeserializationStrategy<IPolicyVariableResolverRefOrValue> =
      when {
        element is JsonObject -> parseJsonObject(element)
        else ->
            throw IllegalArgumentException(
                "Not correct JsonElement for IPolicyVariableResolverRefOrValue DeserializationStrategy")
      }

  private fun parseJsonObject(element: JsonObject) =
      when {
        element.containsKey("refType") -> PolicyVariableResolverRef.serializer()
        element.containsKey("key") || element.containsKey("path") ->
            PolicyVariableResolver.serializer()
        else ->
            throw IllegalArgumentException(
                "No corresponding field for IPolicyVariableResolverRefOrValue DeserializationStrategy")
      }
}
