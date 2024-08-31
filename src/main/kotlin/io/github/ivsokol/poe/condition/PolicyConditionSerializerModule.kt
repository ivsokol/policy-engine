package io.github.ivsokol.poe.condition

import kotlinx.serialization.modules.SerializersModule

/**
 * Configures the serializers module for [IPolicyCondition] and [IPolicyConditionRefOrValue] types.
 * This allows these types to be properly serialized and deserialized by the Kotlinx Serialization
 * library.
 */
val conditionSerializersModule = SerializersModule {
  contextual(IPolicyCondition::class, IPolicyConditionSerializer)
  contextual(IPolicyConditionRefOrValue::class, IPolicyConditionRefOrValueSerializer)
}
