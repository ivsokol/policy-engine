/* (C)2024 */
package io.github.ivsokol.poe.condition

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonNull

/**
 * A serializer for the [PolicyConditionDefault] class that handles serialization and
 * deserialization of the `default` property.
 *
 * This serializer is used to convert the [PolicyConditionDefault] object to and from a JSON
 * representation. It ensures that the `default` property is properly serialized and deserialized,
 * handling cases where the value is `null`.
 */
object PolicyConditionDefaultSerializer : KSerializer<PolicyConditionDefault> {
  override val descriptor: SerialDescriptor =
      buildClassSerialDescriptor("PolicyConditionDefault") { element<Boolean?>("default") }

  @OptIn(ExperimentalSerializationApi::class)
  override fun serialize(encoder: Encoder, value: PolicyConditionDefault) {
    encoder.encodeStructure(descriptor) {
      if (value.default != null) {
        encodeBooleanElement(descriptor, 0, value.default)
      } else {
        encodeNullableSerializableElement(descriptor, 0, JsonNull.serializer(), JsonNull)
      }
    }
  }

  @OptIn(ExperimentalSerializationApi::class)
  override fun deserialize(decoder: Decoder): PolicyConditionDefault {
    var default: Boolean? = null

    val composite = decoder.beginStructure(descriptor)
    while (true) {
      when (val index = composite.decodeElementIndex(descriptor)) {
        0 ->
            default =
                composite.decodeNullableSerializableElement(descriptor, 0, Boolean.serializer())
        DECODE_DONE -> break // Input is over
        else -> error("Unexpected index: $index")
      }
    }
    composite.endStructure(descriptor)

    return PolicyConditionDefault(default)
  }
}
