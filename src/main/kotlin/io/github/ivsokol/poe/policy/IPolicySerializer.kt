package io.github.ivsokol.poe.policy

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * A polymorphic serializer for [IPolicy] that can deserialize JSON elements into the appropriate
 * [IPolicy] implementation.
 *
 * The serializer uses the contents of the JSON object to determine the correct [IPolicy]
 * implementation to deserialize. If the JSON object contains a "targetEffect" key, it will
 * deserialize to a [Policy]. If the JSON object contains a "policyCombinationLogic" key, it will
 * deserialize to a [PolicySet]. If the JSON object contains a "default" key, it will deserialize to
 * a [PolicyDefault]. If the JSON object does not contain any of these keys, an
 * [IllegalArgumentException] will be thrown.
 */
object IPolicySerializer : JsonContentPolymorphicSerializer<IPolicy>(IPolicy::class) {
  override fun selectDeserializer(element: JsonElement): DeserializationStrategy<IPolicy> =
      when {
        element is JsonObject -> parseJsonObject(element)
        else ->
            throw IllegalArgumentException(
                "Not correct JsonElement for IPolicy DeserializationStrategy")
      }

  private fun parseJsonObject(element: JsonObject) =
      when {
        element.containsKey("targetEffect") -> Policy.serializer()
        element.containsKey("policyCombinationLogic") -> PolicySet.serializer()
        element.containsKey("default") -> PolicyDefault.serializer()
        else ->
            throw IllegalArgumentException(
                "No corresponding operation field for IPolicy DeserializationStrategy")
      }
}
