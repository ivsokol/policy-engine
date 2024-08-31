/* (C)2024 */
package io.github.ivsokol.poe.variable

import com.fasterxml.jackson.databind.node.ArrayNode
import io.github.ivsokol.poe.DefaultObjectMapper
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonUnquotedLiteral

/**
 * Serializes and deserializes [ArrayNode] to and from JSON.
 *
 * The [ArrayNodeSerializer] is a [KSerializer] implementation that can serialize an [ArrayNode] to
 * a JSON string and deserialize a JSON string back to an [ArrayNode].
 *
 * The serialization process converts the [ArrayNode] to a [JsonUnquotedLiteral], which is then
 * serialized using the [JsonPrimitive.serializer()].
 *
 * The deserialization process first tries to decode the input as a [JsonArray] using
 * [JsonArray.serializer()]. If successful, the [JsonArray] is then converted to an [ArrayNode]
 * using the [DefaultObjectMapper].
 */
object ArrayNodeSerializer : KSerializer<ArrayNode?> {
  private val objectMapper = DefaultObjectMapper()

  /**
   * Deserializes [decoder] as [JsonArray] and converts it to [ArrayNode].
   *
   * @return [ArrayNode] or null
   */
  @OptIn(ExperimentalSerializationApi::class)
  override fun deserialize(decoder: Decoder): ArrayNode? {
    // tries to decode as JsonArray, if it fails, returns null
    val decoded = decoder.decodeNullableSerializableValue(JsonArray.serializer()) ?: return null
    // converts JsonArray to ArrayNode
    return objectMapper.readTree(decoded.toString()) as ArrayNode
  }

  /** Converts [value] to [JsonUnquotedLiteral] and serializes it. */
  @OptIn(ExperimentalSerializationApi::class)
  override fun serialize(encoder: Encoder, value: ArrayNode?) {
    // converts ArrayNode to JsonUnquotedLiteral
    val encoded = JsonUnquotedLiteral(objectMapper.writeValueAsString(value))
    // serializes JsonUnquotedLiteral
    encoder.encodeNullableSerializableValue(JsonPrimitive.serializer(), encoded)
  }

  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("ArrayNode", PrimitiveKind.STRING).nullable
}
