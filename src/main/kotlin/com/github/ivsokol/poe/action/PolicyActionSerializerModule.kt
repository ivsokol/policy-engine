package com.github.ivsokol.poe.action

import kotlinx.serialization.modules.SerializersModule

/**
 * Creates [SerializersModule] with serializers for all implementations of [IPolicyAction] and
 * [IPolicyActionRefOrValue].
 *
 * @return [SerializersModule]
 */
val actionSerializersModule = SerializersModule {
  contextual(IPolicyAction::class, IPolicyActionSerializer)
  contextual(IPolicyActionRefOrValue::class, IPolicyActionRefOrValueSerializer)
}
