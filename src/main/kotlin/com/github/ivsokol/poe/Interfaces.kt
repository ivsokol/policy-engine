/* (C)2024 */
package com.github.ivsokol.poe

import com.fasterxml.jackson.databind.JsonNode
import com.github.ivsokol.poe.cache.PolicyStoreCacheEnum
import com.github.ivsokol.poe.event.EventItem
import com.github.ivsokol.poe.policy.PolicyResultEnum
import com.github.ivsokol.poe.variable.VariableValue

/**
 * Represents a reference to a policy entity.
 *
 * @property id The unique identifier of the referenced entity.
 * @property version The version of the referenced entity, if applicable.
 * @property refType The type of the referenced entity.
 */
interface IRef {
  val id: String
  val version: SemVer?
  val refType: PolicyEntityRefEnum
}

/**
 * Represents a managed entity with an identifier, version, description, and labels.
 *
 * @return The identity of the managed entity as a string.
 * @property id The unique identifier of the managed entity.
 * @property version The version of the managed entity, if applicable.
 * @property description The description of the managed entity.
 * @property labels The list of labels associated with the managed entity.
 */
interface IManaged {
  val id: String?
  val version: SemVer?
  val description: String?
  val labels: List<String>?

  fun identity(): String
}

/**
 * Represents a cache interface for storing and retrieving various types of data, including
 * variables, conditions, policies, and JSON node values.
 *
 * @property put Stores a value in the cache under the specified store and key.
 * @property get Retrieves a value from the cache using the specified store and key.
 * @property hasKey Checks if the cache has a key stored in the specified store.
 * @property putVariable Stores a variable value in the cache.
 * @property putCondition Stores a condition value in the cache.
 * @property putPolicy Stores a policy result in the cache.
 * @property getVariable Retrieves a variable value from the cache.
 * @property getCondition Retrieves a condition value from the cache.
 * @property getPolicy Retrieves a policy result from the cache.
 * @property getJsonNodeKeyValue Retrieves a JSON node value from the cache.
 * @property getStringKeyValue Retrieves a string value from the cache.
 * @property clear Clears the entire cache.
 */
interface ICache {
  fun put(store: PolicyStoreCacheEnum, key: String, value: Any?)

  fun get(store: PolicyStoreCacheEnum, key: String): Any?

  fun hasKey(store: PolicyStoreCacheEnum, key: String): Boolean

  fun putVariable(key: String, value: VariableValue)

  fun putCondition(key: String, value: Boolean?)

  fun putPolicy(key: String, value: PolicyResultEnum)

  fun getVariable(key: String): VariableValue?

  fun getCondition(key: String): Boolean?

  fun getPolicy(key: String): PolicyResultEnum?

  fun getJsonNodeKeyValue(key: String): JsonNode?

  fun getStringKeyValue(key: String): String?

  fun clear()
}

/** Represents an event that can be added to a list of events. */
interface IEvent {
  /**
   * Adds an event to the list of events.
   *
   * @param contextId The ID of the context in which the event occurred.
   * @param entity The type of entity associated with the event.
   * @param entityId The ID of the entity associated with the event.
   * @param success Indicates whether the event was successful or not.
   * @param value An optional value associated with the event.
   * @param fromCache Indicates whether the event was retrieved from a cache.
   * @param reason An optional reason for the event.
   */
  fun add(
      contextId: String,
      entity: PolicyEntityEnum,
      entityId: String,
      success: Boolean,
      value: Any? = null,
      fromCache: Boolean = false,
      reason: String? = null
  )

  /**
   * Returns a list of [EventItem] instances.
   *
   * @return A list of [EventItem] instances.
   */
  fun list(): List<EventItem>
}
