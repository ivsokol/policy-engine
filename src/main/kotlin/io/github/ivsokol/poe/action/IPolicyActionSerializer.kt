package io.github.ivsokol.poe.action

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.*

private const val TYPE_FIELD_NAME = "type"

/**
 * A polymorphic JSON serializer for [IPolicyAction] instances.
 *
 * This serializer handles the deserialization of [IPolicyAction] instances from JSON, automatically
 * selecting the appropriate concrete implementation based on the "type" field in the JSON object.
 */
object IPolicyActionSerializer :
    JsonContentPolymorphicSerializer<IPolicyAction>(IPolicyAction::class) {
  override fun selectDeserializer(element: JsonElement): DeserializationStrategy<IPolicyAction> =
      when {
        element is JsonObject -> parseJsonObject(element)
        else ->
            throw IllegalArgumentException(
                "Not correct JsonElement for IPolicyAction DeserializationStrategy")
      }

  private fun parseJsonObject(element: JsonObject) =
      when {
        element.containsKey(TYPE_FIELD_NAME) -> parseTypeField(element)
        else ->
            throw IllegalArgumentException(
                "No corresponding operation field for IPolicyAction DeserializationStrategy")
      }

  private fun parseTypeField(element: JsonObject) =
      if (element[TYPE_FIELD_NAME] is JsonPrimitive &&
          element[TYPE_FIELD_NAME]!!.jsonPrimitive.isString) {
        when (val typeVal = element[TYPE_FIELD_NAME]!!.jsonPrimitive.content) {
          "save" -> PolicyActionSave.serializer()
          "clear" -> PolicyActionClear.serializer()
          "jsonMerge" -> PolicyActionJsonMerge.serializer()
          "jsonPatch" -> io.github.ivsokol.poe.action.PolicyActionJsonPatch.serializer()
          else ->
              throw IllegalArgumentException(
                  "Unknown field type: $typeVal in IPolicyAction DeserializationStrategy")
        }
      } else {
        throw IllegalArgumentException(
            "Field type is not string in IPolicyAction DeserializationStrategy")
      }
}
