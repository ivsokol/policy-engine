/* (C)2024 */
package io.github.ivsokol.poe.variable

import io.github.ivsokol.poe.variable.BigDecimalSerializer.deserialize
import io.github.ivsokol.poe.variable.BigDecimalSerializer.serialize
import java.math.BigDecimal
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
 * Serializes and deserializes [BigDecimal] values to and from JSON.
 *
 * This serializer handles the conversion of [BigDecimal] values to and from their string
 * representation when serializing to and deserializing from JSON.
 *
 * @property descriptor The serial descriptor for the [BigDecimal] type.
 * @see deserialize
 * @see serialize
 */
object BigDecimalSerializer : KSerializer<BigDecimal?> {
  @OptIn(ExperimentalSerializationApi::class)
  override fun deserialize(decoder: Decoder): BigDecimal? {
    // deserializes String as BigDecimal
    return decoder
        .decodeNullableSerializableValue(String.serializer().nullable)
        ?.toBigDecimalOrNull()
  }

  @OptIn(ExperimentalSerializationApi::class)
  override fun serialize(encoder: Encoder, value: BigDecimal?) {
    // serializes BigDecimal as String
    encoder.encodeNullableSerializableValue(String.serializer().nullable, value?.toString())
  }

  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING).nullable
}
