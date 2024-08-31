package com.github.ivsokol.poe.variable

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * A polymorphic serializer for [IPolicyVariable] instances.
 *
 * This serializer is responsible for selecting the appropriate deserialization strategy based on
 * the structure of the incoming JSON data. If the JSON element is a [JsonObject], it checks the
 * presence of specific fields to determine the appropriate deserialization strategy:
 * - If the object contains a "resolvers" field, it uses the [PolicyVariableDynamic.serializer] to
 *   deserialize the object.
 * - If the object contains a "value" field, it uses the [PolicyVariableStaticSerializer] to
 *   deserialize the object.
 * - If the object does not contain any of these fields, it throws an [IllegalArgumentException]
 *   indicating that the JSON element is not a valid [IPolicyVariable].
 *
 * If the JSON element is not a [JsonObject], it also throws an [IllegalArgumentException]
 * indicating that the JSON element is not a valid [IPolicyVariable].
 */
object IPolicyVariableSerializer :
    JsonContentPolymorphicSerializer<IPolicyVariable>(IPolicyVariable::class) {
  override fun selectDeserializer(element: JsonElement): DeserializationStrategy<IPolicyVariable> =
      when {
        element is JsonObject -> parseJsonObject(element)
        else ->
            throw IllegalArgumentException(
                "Not correct JsonElement for IPolicyVariable DeserializationStrategy")
      }

  private fun parseJsonObject(element: JsonObject) =
      when {
        element.containsKey("resolvers") -> PolicyVariableDynamic.serializer()
        element.containsKey("value") -> PolicyVariableStaticSerializer
        else ->
            throw IllegalArgumentException(
                "No corresponding field for IPolicyVariable DeserializationStrategy")
      }
}
