package io.github.ivsokol.poe.policy

import kotlinx.serialization.modules.SerializersModule

/**
 * Creates [SerializersModule] with serializers for all implementations of [IPolicy] and
 * [IPolicyRefOrValue].
 *
 * @return [SerializersModule]
 */
val policySerializersModule = SerializersModule {
  contextual(IPolicy::class, IPolicySerializer)
  contextual(IPolicyRefOrValue::class, IPolicyRefOrValueSerializer)
}
