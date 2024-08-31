package com.github.ivsokol.poe.condition

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Serializes and deserializes [IPolicyConditionRefOrValue] to and from JSON. This serializer
 * handles both [PolicyConditionRef] and [IPolicyCondition] instances, selecting the appropriate
 * deserializer based on the JSON structure.
 */
object IPolicyConditionRefOrValueSerializer :
    JsonContentPolymorphicSerializer<IPolicyConditionRefOrValue>(
        IPolicyConditionRefOrValue::class) {
  override fun selectDeserializer(
      element: JsonElement
  ): DeserializationStrategy<IPolicyConditionRefOrValue> =
      when {
        element is JsonObject -> parseJsonObject(element)
        else ->
            throw IllegalArgumentException(
                "Not correct JsonElement for IPolicyConditionRefOrValue DeserializationStrategy")
      }

  private fun parseJsonObject(element: JsonObject) =
      when {
        element.containsKey("refType") -> PolicyConditionRef.serializer()
        element.containsKey("operation") ||
            element.containsKey("conditionCombinationLogic") ||
            element.containsKey("default") -> IPolicyConditionSerializer
        else ->
            throw IllegalArgumentException(
                "No corresponding field for IPolicyConditionRefOrValue DeserializationStrategy")
      }
}
