/* (C)2024 */
package com.github.ivsokol.poe

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.ivsokol.poe.cache.HashMapCache
import com.github.ivsokol.poe.event.InMemoryEventHandler
import com.github.ivsokol.poe.policy.ActionResult
import com.github.ivsokol.poe.policy.PolicyResultEnum
import com.github.ivsokol.poe.variable.ContextStoreEnum
import java.time.Clock
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * A type alias for a map of strings to nullable any values, representing a context store. Context
 * stores are used to hold data associated with a specific context, such as request, environment, or
 * subject data.
 */
typealias ContextStore = Map<String, Any?>

/**
 * Represents the context of a request, including the request data, environment, subject, and other
 * related information. This class provides methods to manage the context, such as retrieving
 * specific stores, managing the path, and storing condition and policy results.
 *
 * @property request the request data as a map of key-value pairs
 * @property environment the environment data as a map of key-value pairs. This data is added to the
 *   [DefaultEnvironment]
 * @property subject the subject data as a map of key-value pairs
 * @property cache the cache implementation used in the application
 * @property options the options used in the application, such as object mapper and date formatters
 * @property event the event handler implementation used in the application
 * @property id a unique identifier for the context
 * @property path a mutable list of strings representing the path of the context
 * @property data a mutable map of key-value pairs representing the data associated with the context
 * @property result the engine result associated with the context
 */
data class Context(
    private val request: ContextStore? = null,
    private var environment: ContextStore? = null,
    private val subject: ContextStore? = null,
    val cache: ICache = HashMapCache(),
    val options: Options = Options(),
    val event: IEvent = InMemoryEventHandler(),
) {
  val id: String = NanoIdUtils.randomNanoId()
  private val path: MutableList<String> = mutableListOf()
  private val data: MutableMap<String, Any?> = mutableMapOf()
  private val result: EngineResult = EngineResult()

  init {
    environment =
        if (environment.isNullOrEmpty()) DefaultEnvironment(options)
        else DefaultEnvironment(options) + environment!!
  }

  /**
   * Retrieves the specified context store from the current context.
   *
   * @param store the context store to retrieve
   * @return the requested context store, or `null` if the store is not available
   */
  fun store(store: ContextStoreEnum): ContextStore? =
      when (store) {
        ContextStoreEnum.REQUEST -> request
        ContextStoreEnum.ENVIRONMENT -> environment?.toMap()
        ContextStoreEnum.SUBJECT -> subject
        ContextStoreEnum.DATA -> data.toMap()
      }

  /**
   * Returns the current path as a string, with each level separated by a forward slash ("/").
   *
   * @return the current path as a string
   */
  private fun getPath(): String = path.joinToString("/")

  /**
   * Checks if the current context path is the root path, optionally with a specific identifier
   * version.
   *
   * @param idVer The identifier version to check the root path against. If left blank, the method
   *   will simply check if the path is empty.
   * @return `true` if the current context path is the root path, either with the specified
   *   identifier version or if the path is empty, `false` otherwise.
   */
  fun isRootPath(idVer: String): Boolean =
      if (idVer.isBlank()) path.isEmpty()
      else path.isNotEmpty() && path[0] == idVer && path.size == 1

  /**
   * Adds a new level to the current context path.
   *
   * @param level The level to add to the path. Must not be blank.
   */
  fun addToPath(level: String) {
    check(level.isNotBlank()) { "Level cannot be blank" }
    path.add(level)
  }

  /** Removes the last level from the current context path. */
  fun removeLastFromPath() {
    if (path.isNotEmpty()) path.removeLast()
  }

  /**
   * Generates the full path for the current context, optionally appending an identity string to the
   * last path level.
   *
   * @param identity The identity string to append to the last path level, if provided. If blank,
   *   the path is returned as-is.
   * @return The full path for the current context, with the optional identity appended to the last
   *   path level.
   */
  fun getFullPath(identity: String): String {
    if (identity.isNotBlank()) {
      if (path.isNotEmpty()) {
        val last = path.removeLast()
        path.add("$last($identity)")
      } else {
        path.add(identity)
      }
    }
    return getPath()
  }

  /**
   * Adds a condition result to the current context.
   *
   * @param conditionId The identifier of the condition.
   * @param resultValue The result value of the condition, either `true` or `false`.
   */
  fun addConditionResult(conditionId: String, resultValue: Boolean?) {
    this.result.conditions[conditionId] = resultValue
  }

  /**
   * Adds a policy result to the current context.
   *
   * @param policyId The identifier of the policy.
   * @param resultValue The result value of the policy, which is a pair of [PolicyResultEnum] and an
   *   optional [ActionResult].
   */
  fun addPolicyResult(policyId: String, resultValue: Pair<PolicyResultEnum, ActionResult?>) {
    this.result.policies[policyId] = resultValue
  }

  /**
   * Returns a map of condition identifiers and their corresponding result values.
   *
   * This method provides a way to retrieve the condition results that have been added to the
   * current context.
   *
   * @return A map where the keys are condition identifiers and the values are the corresponding
   *   result values (either `true` or `false`).
   */
  fun conditionResult(): Map<String, Boolean?> = result.conditions.toMap()

  /**
   * Returns a map of policy identifiers and their corresponding result values.
   *
   * This method provides a way to retrieve the policy results that have been added to the current
   * context.
   *
   * @return A map where the keys are policy identifiers and the values are the corresponding result
   *   values (a pair of [PolicyResultEnum] and an optional [ActionResult]).
   */
  fun policyResult(): Map<String, Pair<PolicyResultEnum, ActionResult?>> = result.policies.toMap()

  /**
   * Returns the current data store as a mutable map.
   *
   * The data store is a mutable map that holds the current state of the context. This method
   * provides a way to access and manipulate the data store directly.
   *
   * @return A mutable map representing the current data store.
   */
  fun dataStore(): MutableMap<String, Any?> = data

  /**
   * Rolls back the data store to the provided data snapshot.
   *
   * This method clears the current data store and replaces it with the data from the provided
   * [dataSnapshot]. This can be used to restore the data store to a previous state.
   *
   * @param dataSnapshot The data snapshot to restore the data store to.
   */
  fun rollbackDataStore(dataSnapshot: ContextStore) {
    this.data.clear()
    this.data.putAll(dataSnapshot)
  }
}

/**
 * Holds various configuration options for the context.
 *
 * @param objectMapper The ObjectMapper to use for serialization and deserialization.
 * @param dateTimeFormatter The DateTimeFormatter to use for formatting and parsing date-time
 *   values.
 * @param dateFormatter The DateTimeFormatter to use for formatting and parsing date values.
 * @param timeFormatter The DateTimeFormatter to use for formatting and parsing time values.
 * @param zoneId The ZoneId to use for date and time operations.
 * @param clock The Clock to use for time-related operations.
 * @param defaultSchemaUri The default schema URI to use.
 */
data class Options(
    val objectMapper: ObjectMapper = DefaultObjectMapper(),
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME,
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE,
    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME,
    val zoneId: ZoneId = ZoneId.systemDefault(),
    val clock: Clock? = null,
    val defaultSchemaUri: String = "https://github.com/ivsokol"
)
