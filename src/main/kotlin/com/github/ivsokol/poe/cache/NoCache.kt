package com.github.ivsokol.poe.cache

import com.fasterxml.jackson.databind.JsonNode
import com.github.ivsokol.poe.ICache
import com.github.ivsokol.poe.policy.PolicyResultEnum
import com.github.ivsokol.poe.variable.VariableValue

/** This class is used to return cache miss for all queries while implementing ICache interface. */
/**
 * This class is an implementation of the [ICache] interface that does not cache any data. All
 * methods in this class simply return default values or perform no-op operations. This class is
 * useful for scenarios where caching is not required or desired.
 */
class NoCache : ICache {
  override fun put(store: PolicyStoreCacheEnum, key: String, value: Any?) = Unit

  override fun putVariable(key: String, value: VariableValue) = Unit

  override fun putCondition(key: String, value: Boolean?) = Unit

  override fun putPolicy(key: String, value: PolicyResultEnum) = Unit

  override fun get(store: PolicyStoreCacheEnum, key: String): Any? = null

  override fun hasKey(store: PolicyStoreCacheEnum, key: String): Boolean = false

  override fun getVariable(key: String): VariableValue? = null

  override fun getCondition(key: String): Boolean? = null

  override fun getPolicy(key: String): PolicyResultEnum? = null

  override fun getJsonNodeKeyValue(key: String): JsonNode? = null

  override fun getStringKeyValue(key: String): String? = null

  override fun clear() = Unit
}
