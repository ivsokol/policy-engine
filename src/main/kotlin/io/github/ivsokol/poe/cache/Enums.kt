/* (C)2024 */
package io.github.ivsokol.poe.cache

/**
 * Enum representing the different types of caches used in the Policy Store.
 * - `VARIABLE`: Caches variables.
 * - `CONDITION`: Caches conditions.
 * - `POLICY`: Caches policies.
 * - `KEY_VALUE_AS_JSON_NODE`: Caches key-value pairs as JSON nodes.
 * - `KEY_VALUE_AS_STRING`: Caches key-value pairs as strings.
 */
enum class PolicyStoreCacheEnum {
  VARIABLE,
  CONDITION,
  POLICY,
  KEY_VALUE_AS_JSON_NODE,
  KEY_VALUE_AS_STRING
}
