package com.github.ivsokol.poe.condition

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Serializes and deserializes [IPolicyCondition] instances to and from JSON.
 *
 * This serializer handles the polymorphic nature of [IPolicyCondition] by selecting the appropriate
 * deserializer based on the JSON object structure. It supports deserializing
 * [PolicyConditionAtomic], [PolicyConditionComposite], and [PolicyConditionDefaultSerializer]
 * instances.
 */
object IPolicyConditionSerializer :
    JsonContentPolymorphicSerializer<IPolicyCondition>(IPolicyCondition::class) {
  override fun selectDeserializer(element: JsonElement): DeserializationStrategy<IPolicyCondition> =
      when {
        element is JsonObject -> parseJsonObject(element)
        else ->
            throw IllegalArgumentException(
                "Not correct JsonElement for IPolicyCondition DeserializationStrategy")
      }

  private fun parseJsonObject(element: JsonObject) =
      when {
        element.containsKey("operation") -> PolicyConditionAtomic.serializer()
        element.containsKey("conditionCombinationLogic") -> PolicyConditionComposite.serializer()
        element.containsKey("default") -> PolicyConditionDefaultSerializer
        else ->
            throw IllegalArgumentException(
                "No corresponding operation field for IPolicyCondition DeserializationStrategy")
      }
}
