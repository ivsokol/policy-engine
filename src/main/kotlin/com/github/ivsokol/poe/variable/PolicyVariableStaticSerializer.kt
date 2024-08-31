@file:Suppress("UNCHECKED_CAST")

package com.github.ivsokol.poe.variable

import com.github.ivsokol.poe.SemVer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.serializer

/**
 * Serializer for [PolicyVariableStatic] objects, which are used to represent policy variables in
 * the system. This serializer handles the serialization and deserialization of
 * [PolicyVariableStatic] objects to and from JSON. It defines the structure of the JSON
 * representation, including fields for the variable's ID, version, description, labels, type,
 * format, and value. The serializer also handles the mapping of the variable's value to the
 * appropriate serializer based on the variable's type and format.
 */
object PolicyVariableStaticSerializer : KSerializer<PolicyVariableStatic> {
  private val json: Json = Json { serializersModule = variableSerializersCoreModule }
  override val descriptor: SerialDescriptor =
      buildClassSerialDescriptor("PolicyVariableStatic") {
        element<String?>("id")
        element<SemVer?>("version")
        element<String?>("description")
        element<List<String>?>("labels")
        element<VariableValueTypeEnum?>("type")
        element<VariableValueFormatEnum?>("format")
        element<String?>("timeFormat")
        element<String?>("dateFormat")
        element<String?>("dateTimeFormat")
        element("value", buildClassSerialDescriptor("Any"))
      }

  @OptIn(ExperimentalSerializationApi::class)
  override fun serialize(encoder: Encoder, value: PolicyVariableStatic) {
    encoder.encodeStructure(descriptor) {
      // Pick runtime type from type and format, if set. If not set, determine it from value class
      val runtimeType = value.runtimeType()
      // If type and format are not set, pick them from runtime type
      val typeAndFormat =
          if (value.type == null) typeAndFormatFromRuntimeType(runtimeType)
          else Pair(value.type, value.format)
      encodeNullableSerializableElement(descriptor, 0, String.serializer(), value.id)
      encodeNullableSerializableElement(descriptor, 1, SemVer.serializer(), value.version)
      encodeNullableSerializableElement(descriptor, 2, String.serializer(), value.description)
      encodeNullableSerializableElement(
          descriptor, 3, ListSerializer(String.serializer()), value.labels)
      encodeNullableSerializableElement(
          descriptor, 4, VariableValueTypeEnum.serializer(), value.type ?: typeAndFormat?.first)
      encodeNullableSerializableElement(
          descriptor,
          5,
          VariableValueFormatEnum.serializer(),
          value.format ?: typeAndFormat?.second)
      encodeNullableSerializableElement(descriptor, 6, String.serializer(), value.timeFormat)
      encodeNullableSerializableElement(descriptor, 7, String.serializer(), value.dateFormat)
      encodeNullableSerializableElement(descriptor, 8, String.serializer(), value.dateTimeFormat)
      encodeSerializableElement(descriptor, 9, getValueSerializer(runtimeType), value.value)
    }
  }

  @OptIn(ExperimentalSerializationApi::class)
  override fun deserialize(decoder: Decoder): PolicyVariableStatic {
    var id: String? = null
    var version: SemVer? = null
    var description: String? = null
    var labels: List<String>? = null
    var type: VariableValueTypeEnum? = null
    var format: VariableValueFormatEnum? = null
    var timeFormat: String? = null
    var dateFormat: String? = null
    var dateTimeFormat: String? = null
    var value: JsonElement? = null

    val composite = decoder.beginStructure(descriptor)
    while (true) {
      when (val index = composite.decodeElementIndex(descriptor)) {
        0 -> id = composite.decodeNullableSerializableElement(descriptor, 0, String.serializer())
        1 ->
            version =
                composite.decodeNullableSerializableElement(descriptor, 1, SemVer.serializer())
        2 ->
            description =
                composite.decodeNullableSerializableElement(descriptor, 2, String.serializer())
        3 ->
            labels =
                composite.decodeNullableSerializableElement(
                    descriptor, 3, ListSerializer(String.serializer()))
        4 ->
            type =
                composite.decodeNullableSerializableElement(
                    descriptor, 4, VariableValueTypeEnum.serializer())
        5 ->
            format =
                composite.decodeNullableSerializableElement(
                    descriptor, 5, VariableValueFormatEnum.serializer())
        6 ->
            timeFormat =
                composite.decodeNullableSerializableElement(descriptor, 6, String.serializer())
        7 ->
            dateFormat =
                composite.decodeNullableSerializableElement(descriptor, 7, String.serializer())
        8 ->
            dateTimeFormat =
                composite.decodeNullableSerializableElement(descriptor, 8, String.serializer())
        // as parsing of raw JSON is not sequential, value parameter is temporarily saved as
        // JsonElement. Later on it is cast to correct value, taking into consideration provided
        // type and format
        9 ->
            value =
                composite.decodeNullableSerializableElement(
                    descriptor, 9, serializer<JsonElement>())
        DECODE_DONE -> break // Input is over
        else -> error("Unexpected index: $index")
      }
    }
    composite.endStructure(descriptor)

    checkNotNull(value) { "Cannot deserialize PolicyVariableStatic without value" }
    val runtimeType =
        // determines runtimeType from type and format, if available
        if (type != null) runtimeTypeFromTypeAndFormat(type, format)
        // determines runtimeType from value class
        else runtimeTypeFromJsonElement(value)
    val decodedValue =
        json.decodeFromJsonElement(getValueDeserializer(runtimeType, type, format), value)
    return PolicyVariableStatic(
        id = id,
        version = version,
        description = description,
        labels = labels,
        value = decodedValue,
        type = type,
        format = format,
        timeFormat = timeFormat,
        dateFormat = dateFormat,
        dateTimeFormat = dateTimeFormat)
  }

  // determines runtimeType from type and format, if available. Otherwise, tries to determine it
  // from value class
  private fun PolicyVariableStatic.runtimeType(): VariableRuntimeTypeEnum {
    if (this.type != null) {
      return runtimeTypeFromTypeAndFormat(this.type, this.format)
    }
    return determineRuntimeType(this.value)
  }

  // map of supported serializers for each runtime type
  @Suppress("UNCHECKED_CAST")
  private val dataTypeSerializers: Map<VariableRuntimeTypeEnum, KSerializer<Any>> =
      mapOf(
              VariableRuntimeTypeEnum.STRING to serializer<String>(),
              VariableRuntimeTypeEnum.DATE to LocalDateSerializer,
              VariableRuntimeTypeEnum.DATE_TIME to OffsetDateTimeSerializer,
              VariableRuntimeTypeEnum.TIME to LocalTimeSerializer,
              VariableRuntimeTypeEnum.PERIOD to PeriodSerializer,
              VariableRuntimeTypeEnum.DURATION to DurationSerializer,
              VariableRuntimeTypeEnum.INT to serializer<Int>(),
              VariableRuntimeTypeEnum.LONG to serializer<Long>(),
              VariableRuntimeTypeEnum.DOUBLE to serializer<Double>(),
              VariableRuntimeTypeEnum.FLOAT to serializer<Float>(),
              VariableRuntimeTypeEnum.BIG_DECIMAL to BigDecimalSerializer,
              VariableRuntimeTypeEnum.BOOLEAN to serializer<Boolean>(),
              VariableRuntimeTypeEnum.OBJECT_NODE to ObjectNodeSerializer,
              VariableRuntimeTypeEnum.ARRAY_NODE to ArrayNodeSerializer,
              VariableRuntimeTypeEnum.NULL to serializer<JsonNull>())
          .mapValues { (_, v) -> v as KSerializer<Any> }

  private fun getValueSerializer(runtimeType: VariableRuntimeTypeEnum): KSerializer<Any> =
      dataTypeSerializers[runtimeType]
          ?: throw SerializationException(
              "Serializer for $runtimeType is not registered in PolicyVariableStaticSerializer")

  private fun getValueDeserializer(
      runtimeType: VariableRuntimeTypeEnum,
      type: VariableValueTypeEnum?,
      format: VariableValueFormatEnum?
  ): KSerializer<Any> {
    if (runtimeType == VariableRuntimeTypeEnum.NULL) {
      return serializer<JsonNull>() as KSerializer<Any>
    }
    return if (type != null) {

      when (type) {
        // if type is string, PolicyVariable will try to handle deserialization
        VariableValueTypeEnum.STRING -> serializer<String>()
        // PolicyVariable objectMapper will be used to deserialize value if format is JSON
        VariableValueTypeEnum.BOOLEAN,
        VariableValueTypeEnum.INT,
        VariableValueTypeEnum.NUMBER,
        VariableValueTypeEnum.OBJECT,
        VariableValueTypeEnum.ARRAY ->
            when (format) {
              VariableValueFormatEnum.JSON -> serializer<String>()
              else -> getValueSerializer(runtimeType)
            }
      }
          as KSerializer<Any>
    } else return getValueSerializer(runtimeType)
  }
}
