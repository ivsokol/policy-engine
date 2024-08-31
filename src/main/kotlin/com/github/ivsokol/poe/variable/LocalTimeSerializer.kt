/* (C)2024 */
package com.github.ivsokol.poe.variable

import java.time.LocalTime
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
 * Serializes and deserializes [LocalTime] values to and from JSON.
 *
 * This serializer converts [LocalTime] values to and from JSON strings. It handles null values as
 * well.
 */
object LocalTimeSerializer : KSerializer<LocalTime?> {
  /**
   * Deserializes value as [String] and converts it to [LocalTime].
   *
   * @return [LocalTime] or null
   */
  @OptIn(ExperimentalSerializationApi::class)
  override fun deserialize(decoder: Decoder): LocalTime? {
    return LocalTime.parse(decoder.decodeNullableSerializableValue(String.serializer().nullable))
  }

  /** Converts [LocalTime] to [String] and serializes it. */
  @OptIn(ExperimentalSerializationApi::class)
  override fun serialize(encoder: Encoder, value: LocalTime?) {
    encoder.encodeNullableSerializableValue(String.serializer().nullable, value?.toString())
  }

  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING).nullable
}
