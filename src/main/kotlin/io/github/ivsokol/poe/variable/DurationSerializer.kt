/* (C)2024 */
package io.github.ivsokol.poe.variable

import java.time.Duration
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
 * A custom serializer for [Duration] that serializes and deserializes [Duration] values as strings.
 *
 * This serializer is used to handle [Duration] values in serialization and deserialization
 * operations. It converts [Duration] values to and from string representations during serialization
 * and deserialization.
 */
object DurationSerializer : KSerializer<Duration?> {
  /**
   * Deserializes value as [String] and converts it to [Duration].
   *
   * @return [Duration] or null
   */
  @OptIn(ExperimentalSerializationApi::class)
  override fun deserialize(decoder: Decoder): Duration? {
    return Duration.parse(decoder.decodeNullableSerializableValue(String.serializer().nullable))
  }

  /** Converts [Duration] to [String] and serializes it. */
  @OptIn(ExperimentalSerializationApi::class)
  override fun serialize(encoder: Encoder, value: Duration?) {
    encoder.encodeNullableSerializableValue(String.serializer().nullable, value?.toString())
  }

  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("java.time.Duration", PrimitiveKind.STRING).nullable
}
