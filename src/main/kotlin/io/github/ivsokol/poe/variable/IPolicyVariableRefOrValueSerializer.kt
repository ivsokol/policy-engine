package io.github.ivsokol.poe.variable

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * A polymorphic serializer for [IPolicyVariableRefOrValue], which can deserialize either a
 * [PolicyVariableRef] or an [IPolicyVariable]. The serializer determines the appropriate
 * deserializer based on the structure of the incoming JSON element.
 */
object IPolicyVariableRefOrValueSerializer :
    JsonContentPolymorphicSerializer<IPolicyVariableRefOrValue>(IPolicyVariableRefOrValue::class) {
  override fun selectDeserializer(
      element: JsonElement
  ): DeserializationStrategy<IPolicyVariableRefOrValue> =
      when {
        element is JsonObject -> parseJsonObject(element)
        else ->
            throw IllegalArgumentException(
                "Not correct JsonElement for IPolicyVariableRefOrValue DeserializationStrategy")
      }

  private fun parseJsonObject(element: JsonObject) =
      when {
        element.containsKey("refType") -> PolicyVariableRef.serializer()
        element.containsKey("value") || element.containsKey("resolvers") ->
            IPolicyVariableSerializer
        else ->
            throw IllegalArgumentException(
                "No corresponding field for IPolicyVariableRefOrValue DeserializationStrategy")
      }
}
