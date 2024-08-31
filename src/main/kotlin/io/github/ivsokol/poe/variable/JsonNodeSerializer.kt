package io.github.ivsokol.poe.variable

import com.fasterxml.jackson.databind.JsonNode
import io.github.ivsokol.poe.DefaultObjectMapper
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonUnquotedLiteral

/**
 * A serializer for [JsonNode] that can serialize and deserialize it to/from [JsonElement].
 *
 * The serializer uses [DefaultObjectMapper] to convert between [JsonNode] and [JsonElement]. When
 * deserializing, it first decodes the value as a nullable [JsonElement], and then converts it to a
 * [JsonNode] using [DefaultObjectMapper]. When serializing, it converts the [JsonNode] to a
 * [JsonUnquotedLiteral] and serializes it.
 */
object JsonNodeSerializer : KSerializer<JsonNode?> {
  private val objectMapper = DefaultObjectMapper()

  /**
   * Deserializes value as [JsonElement] and converts it to [JsonNode].
   *
   * @return [JsonNode] or null
   */
  @OptIn(ExperimentalSerializationApi::class)
  override fun deserialize(decoder: Decoder): JsonNode? {
    val decoded = decoder.decodeNullableSerializableValue(JsonElement.serializer()) ?: return null
    return objectMapper.readTree(decoded.toString())
  }

  /** Converts [JsonNode] to [JsonUnquotedLiteral] and serializes it. */
  @OptIn(ExperimentalSerializationApi::class)
  override fun serialize(encoder: Encoder, value: JsonNode?) {
    val encoded = JsonUnquotedLiteral(objectMapper.writeValueAsString(value))
    encoder.encodeNullableSerializableValue(JsonPrimitive.serializer(), encoded)
  }

  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("JsonNode", PrimitiveKind.STRING).nullable
}
