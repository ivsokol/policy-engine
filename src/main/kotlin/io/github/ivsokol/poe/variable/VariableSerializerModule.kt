package io.github.ivsokol.poe.variable

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.BigDecimal
import java.time.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus

@Suppress("UNCHECKED_CAST")
/**
 * Defines a [SerializersModule] that provides serializers for various data types used in the
 * application, including ArrayNode, ObjectNode, JsonNode, BigDecimal, Duration, Period, LocalDate,
 * OffsetDateTime, and LocalTime. This module is used in conjunction with the
 * [variableSerializersModule] to provide a comprehensive set of serializers for the application's
 * data.
 */
internal val variableSerializersCoreModule = SerializersModule {
  contextual(ArrayNode::class, ArrayNodeSerializer as KSerializer<ArrayNode>)
  contextual(ObjectNode::class, ObjectNodeSerializer as KSerializer<ObjectNode>)
  contextual(JsonNode::class, JsonNodeSerializer as KSerializer<JsonNode>)
  contextual(BigDecimal::class, BigDecimalSerializer as KSerializer<BigDecimal>)
  contextual(Duration::class, DurationSerializer as KSerializer<Duration>)
  contextual(Period::class, PeriodSerializer as KSerializer<Period>)
  contextual(LocalDate::class, LocalDateSerializer as KSerializer<LocalDate>)
  contextual(OffsetDateTime::class, OffsetDateTimeSerializer as KSerializer<OffsetDateTime>)
  contextual(LocalTime::class, LocalTimeSerializer as KSerializer<LocalTime>)
}

/**
 * Defines a [SerializersModule] that provides additional serializers for various data types used in
 * the application, including [PolicyVariableStatic], [IPolicyVariable],
 * [IPolicyVariableRefOrValue], and [IPolicyVariableResolverRefOrValue]. This module is used in
 * conjunction with the [variableSerializersCoreModule] to provide a comprehensive set of
 * serializers for the application's data.
 */
val variableSerializersModule =
    SerializersModule {
      contextual(PolicyVariableStatic::class, PolicyVariableStaticSerializer)
      contextual(IPolicyVariable::class, IPolicyVariableSerializer)
      contextual(IPolicyVariableRefOrValue::class, IPolicyVariableRefOrValueSerializer)
      contextual(
          IPolicyVariableResolverRefOrValue::class, IPolicyVariableResolverRefOrValueSerializer)
    } + variableSerializersCoreModule
