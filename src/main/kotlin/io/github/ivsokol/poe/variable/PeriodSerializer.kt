/* (C)2024 */
package io.github.ivsokol.poe.variable

import java.time.Period
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
 * Serializes and deserializes [Period] values to and from JSON.
 *
 * This serializer converts [Period] values to and from JSON strings using the standard
 * [Period.toString()] and [Period.parse()] methods.
 *
 * When deserializing, this serializer will return `null` if the input JSON is `null`.
 */
object PeriodSerializer : KSerializer<Period?> {
  /**
   * Deserializes value as [String] and converts it to [Period].
   *
   * @return [Period] or null
   */
  @OptIn(ExperimentalSerializationApi::class)
  override fun deserialize(decoder: Decoder): Period? {
    return Period.parse(decoder.decodeNullableSerializableValue(String.serializer().nullable))
  }

  /** Converts [Period] to [String] and serializes it. */
  @OptIn(ExperimentalSerializationApi::class)
  override fun serialize(encoder: Encoder, value: Period?) {
    encoder.encodeNullableSerializableValue(String.serializer().nullable, value?.toString())
  }

  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("Period", PrimitiveKind.STRING).nullable
}
