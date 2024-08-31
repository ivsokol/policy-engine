/* (C)2024 */
package io.github.ivsokol.poe.variable

import java.time.LocalDate
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
 * A [KSerializer] implementation for [LocalDate] that serializes and deserializes it as a nullable
 * [String].
 *
 * This serializer allows [LocalDate] values to be serialized to and deserialized from JSON or other
 * formats that use strings to represent dates.
 *
 * @property descriptor The serial descriptor for this serializer, which is a nullable [String].
 * @method deserialize Deserializes a nullable [String] value and converts it to a [LocalDate] or
 *   null.
 * @method serialize Serializes a nullable [LocalDate] value to a nullable [String].
 */
object LocalDateSerializer : KSerializer<LocalDate?> {
  /**
   * Deserializes value as [String] and converts it to [LocalDate].
   *
   * @return [LocalDate] or null
   */
  @OptIn(ExperimentalSerializationApi::class)
  override fun deserialize(decoder: Decoder): LocalDate? {
    return LocalDate.parse(decoder.decodeNullableSerializableValue(String.serializer().nullable))
  }

  /** Converts [LocalDate] to [String] and serializes it. */
  @OptIn(ExperimentalSerializationApi::class)
  override fun serialize(encoder: Encoder, value: LocalDate?) {
    encoder.encodeNullableSerializableValue(String.serializer().nullable, value?.toString())
  }

  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING).nullable
}
