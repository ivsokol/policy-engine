/* (C)2024 */
package com.github.ivsokol.poe.variable

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.ivsokol.poe.DefaultObjectMapper
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonUnquotedLiteral

/**
 * A Kotlin serializer for [ObjectNode] that can serialize and deserialize [ObjectNode] instances.
 *
 * The serializer uses the [DefaultObjectMapper] to convert between [JsonObject] and [ObjectNode].
 * During deserialization, the serializer decodes the input as a nullable [JsonObject] and then
 * converts it to an [ObjectNode] using the [DefaultObjectMapper]. During serialization, the
 * serializer converts the [ObjectNode] to a [JsonUnquotedLiteral] and then serializes it.
 */
object ObjectNodeSerializer : KSerializer<ObjectNode?> {
  private val objectMapper = DefaultObjectMapper()

  /**
   * Deserializes value as [JsonObject] and converts it to [ObjectNode].
   *
   * @return [ObjectNode] or null
   */
  @OptIn(ExperimentalSerializationApi::class)
  override fun deserialize(decoder: Decoder): ObjectNode? {
    val decoded = decoder.decodeNullableSerializableValue(JsonObject.serializer()) ?: return null
    return objectMapper.readTree(decoded.toString()) as ObjectNode
  }

  /** Converts [ObjectNode] to [JsonUnquotedLiteral] and serializes it. */
  @OptIn(ExperimentalSerializationApi::class)
  override fun serialize(encoder: Encoder, value: ObjectNode?) {
    val encoded = JsonUnquotedLiteral(objectMapper.writeValueAsString(value))
    encoder.encodeNullableSerializableValue(JsonPrimitive.serializer(), encoded)
  }

  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("ObjectNode", PrimitiveKind.STRING).nullable
}
