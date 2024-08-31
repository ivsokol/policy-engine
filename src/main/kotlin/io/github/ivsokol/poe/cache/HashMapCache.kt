/* (C)2024 */
package io.github.ivsokol.poe.cache

import com.fasterxml.jackson.databind.JsonNode
import io.github.ivsokol.poe.ICache
import io.github.ivsokol.poe.policy.PolicyResultEnum
import io.github.ivsokol.poe.variable.VariableValue

private const val KEY_CANNOT_BE_BLANK_MSG = "key cannot be blank"

/**
 * This class is used to store policy, condition and variable results in memory.
 *
 * It provides methods to put, get, and check the existence of values in various stores, such as
 * policy, variable, condition, and key-value stores.
 *
 * The class is thread-safe and can be used concurrently by multiple threads.
 */
class HashMapCache : ICache {
  private var policyStore: MutableMap<String, PolicyResultEnum> = mutableMapOf()
  private var valueStore: MutableMap<String, VariableValue> = mutableMapOf()
  private var conditionStore: MutableMap<String, Boolean?> = mutableMapOf()
  private var keyValueAsJsonNode: MutableMap<String, JsonNode?> = mutableMapOf()
  private var keyValueAsString: MutableMap<String, String?> = mutableMapOf()

  /**
   * This method is used to store policy, condition, variable, and key-value results in memory.
   *
   * @param store The type of store to use for the given key and value.
   * @param key The key to use for storing the value.
   * @param value The value to store in the specified store.
   * @throws IllegalArgumentException If the provided value is not of the expected type for the
   *   given store.
   */
  override fun put(store: PolicyStoreCacheEnum, key: String, value: Any?) {
    require(key.isNotBlank()) { KEY_CANNOT_BE_BLANK_MSG }
    return when (store) {
      PolicyStoreCacheEnum.POLICY ->
          policyStore[key] =
              value as? PolicyResultEnum
                  ?: throw IllegalArgumentException(
                      "Provided value is not of type PolicyResultEnum")
      PolicyStoreCacheEnum.VARIABLE ->
          valueStore[key] =
              value as? VariableValue
                  ?: throw IllegalArgumentException("Provided value is not of type VariableValue")
      PolicyStoreCacheEnum.CONDITION ->
          conditionStore[key] =
              if (value != null)
                  value as? Boolean
                      ?: throw IllegalArgumentException("Provided value is not of type Boolean")
              else null
      PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE ->
          keyValueAsJsonNode[key] =
              if (value != null)
                  value as? JsonNode
                      ?: throw IllegalArgumentException("Provided value is not of type JsonNode")
              else null
      PolicyStoreCacheEnum.KEY_VALUE_AS_STRING ->
          keyValueAsString[key] =
              if (value != null)
                  value as? String
                      ?: throw IllegalArgumentException("Provided value is not of type String")
              else null
    }
  }

  /**
   * This method is used to store a variable value in memory.
   *
   * @param key The key to use for storing the variable value.
   * @param value The variable value to store.
   * @throws IllegalArgumentException If the provided key is blank.
   */
  override fun putVariable(key: String, value: VariableValue) {
    require(key.isNotBlank()) { KEY_CANNOT_BE_BLANK_MSG }
    valueStore[key] = value
  }

  /**
   * This method is used to store a condition value in memory.
   *
   * @param key The key to use for storing the condition value.
   * @param value The condition value to store. Can be null.
   * @throws IllegalArgumentException If the provided key is blank.
   */
  override fun putCondition(key: String, value: Boolean?) {
    require(key.isNotBlank()) { KEY_CANNOT_BE_BLANK_MSG }
    conditionStore[key] = value
  }

  /**
   * This method is used to store a policy result in memory.
   *
   * @param key The key to use for storing the policy result.
   * @param value The policy result to store.
   * @throws IllegalArgumentException If the provided key is blank.
   */
  override fun putPolicy(key: String, value: PolicyResultEnum) {
    require(key.isNotBlank()) { KEY_CANNOT_BE_BLANK_MSG }
    policyStore[key] = value
  }

  /**
   * This method is used to retrieve a value from the specified cache store.
   *
   * @param store The cache store to retrieve the value from.
   * @param key The key to use for retrieving the value.
   * @return The value stored under the provided key, or null if the key does not exist.
   * @throws IllegalArgumentException If the provided key is blank.
   */
  override fun get(store: PolicyStoreCacheEnum, key: String): Any? {
    require(key.isNotBlank()) { KEY_CANNOT_BE_BLANK_MSG }
    return when (store) {
      PolicyStoreCacheEnum.POLICY -> policyStore[key]
      PolicyStoreCacheEnum.VARIABLE -> valueStore[key]
      PolicyStoreCacheEnum.CONDITION -> conditionStore[key]
      PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE -> keyValueAsJsonNode[key]
      PolicyStoreCacheEnum.KEY_VALUE_AS_STRING -> keyValueAsString[key]
    }
  }

  /**
   * Checks if the specified cache store contains the given key.
   *
   * @param store The cache store to check for the key.
   * @param key The key to check for in the cache store.
   * @return `true` if the cache store contains the given key, `false` otherwise.
   * @throws IllegalArgumentException If the provided key is blank.
   */
  override fun hasKey(store: PolicyStoreCacheEnum, key: String): Boolean {
    require(key.isNotBlank()) { KEY_CANNOT_BE_BLANK_MSG }
    return when (store) {
      PolicyStoreCacheEnum.POLICY -> policyStore.containsKey(key)
      PolicyStoreCacheEnum.VARIABLE -> valueStore.containsKey(key)
      PolicyStoreCacheEnum.CONDITION -> conditionStore.containsKey(key)
      PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE -> keyValueAsJsonNode.containsKey(key)
      PolicyStoreCacheEnum.KEY_VALUE_AS_STRING -> keyValueAsString.containsKey(key)
    }
  }

  /**
   * This method is used to retrieve a variable value from the specified cache store.
   *
   * @param key The key to use for retrieving the variable value.
   * @return The variable value stored under the provided key, or null if the key does not exist.
   * @throws IllegalArgumentException If the provided key is blank.
   */
  override fun getVariable(key: String): VariableValue? {
    require(key.isNotBlank()) { KEY_CANNOT_BE_BLANK_MSG }
    return valueStore[key]
  }

  /**
   * This method is used to retrieve a condition from the cache.
   *
   * @param key The key to use for retrieving the condition.
   * @return The condition stored under the provided key, or null if the key does not exist.
   * @throws IllegalArgumentException If the provided key is blank.
   */
  override fun getCondition(key: String): Boolean? {
    require(key.isNotBlank()) { KEY_CANNOT_BE_BLANK_MSG }
    return conditionStore[key]
  }

  /**
   * This method is used to retrieve a policy from the cache.
   *
   * @param key The key to use for retrieving the policy.
   * @return The policy stored under the provided key, or null if the key does not exist.
   * @throws IllegalArgumentException If the provided key is blank.
   */
  override fun getPolicy(key: String): PolicyResultEnum? {
    require(key.isNotBlank()) { KEY_CANNOT_BE_BLANK_MSG }
    return policyStore[key]
  }

  /**
   * This method is used to retrieve a key-value pair stored as a JsonNode from the cache.
   *
   * @param key The key to use for retrieving the JsonNode value.
   * @return The JsonNode value stored under the provided key, or null if the key does not exist.
   * @throws IllegalArgumentException If the provided key is blank.
   */
  override fun getJsonNodeKeyValue(key: String): JsonNode? {
    require(key.isNotBlank()) { KEY_CANNOT_BE_BLANK_MSG }
    return keyValueAsJsonNode[key]
  }

  /**
   * This method is used to retrieve a key-value pair stored as a String from the cache.
   *
   * @param key The key to use for retrieving the String value.
   * @return The String value stored under the provided key, or null if the key does not exist.
   * @throws IllegalArgumentException If the provided key is blank.
   */
  override fun getStringKeyValue(key: String): String? {
    require(key.isNotBlank()) { KEY_CANNOT_BE_BLANK_MSG }
    return keyValueAsString[key]
  }

  /**
   * This method is used to clear all the caches maintained by this class. It clears the policy
   * store, value store, condition store, JSON node key-value store, and string key-value store.
   */
  override fun clear() {
    policyStore.clear()
    valueStore.clear()
    conditionStore.clear()
    keyValueAsJsonNode.clear()
    keyValueAsString.clear()
  }

  override fun toString(): String {
    return "HashMapCache(policyStore=$policyStore,valueStore=$valueStore,conditionStore=$conditionStore,keyValueAsJsonNode=$keyValueAsJsonNode,keyValueAsString=$keyValueAsString)"
  }
}
