/* (C)2024 */
package io.github.ivsokol.poe.variable

import java.time.OffsetDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes and deserializes [OffsetDateTime] to and from JSON string.
 *
 * This serializer converts [OffsetDateTime] to and from a JSON string representation. When
 * deserializing, it will parse the string into an [OffsetDateTime] instance. When serializing, it
 * will convert the [OffsetDateTime] instance to a string.
 */
object OffsetDateTimeSerializer : KSerializer<OffsetDateTime?> {
  /**
   * Deserializes value as [String] and converts it to [OffsetDateTime].
   *
   * @return [OffsetDateTime] or null
   */
  @OptIn(ExperimentalSerializationApi::class)
  override fun deserialize(decoder: Decoder): OffsetDateTime? {
    return OffsetDateTime.parse(
        decoder.decodeNullableSerializableValue(String.serializer().nullable))
  }

  /** Converts [OffsetDateTime] to [String] and serializes it. */
  @OptIn(ExperimentalSerializationApi::class)
  override fun serialize(encoder: Encoder, value: OffsetDateTime?) {
    encoder.encodeNullableSerializableValue(String.serializer().nullable, value?.toString())
  }

  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("OffsetDateTime", PrimitiveKind.STRING).nullable
}
