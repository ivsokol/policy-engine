/* (C)2024 */
package io.github.ivsokol.poe.event

import io.github.ivsokol.poe.IEvent
import io.github.ivsokol.poe.PolicyEntityEnum
import java.time.LocalDateTime

/**
 * Enum representing the different levels of event logging.
 * - `NONE`: No events are logged.
 * - `BASIC`: Only basic event information is logged (e.g. timestamp, context ID, entity, entity ID,
 *   success/failure).
 * - `DETAILS`: In addition to basic information, detailed event data is logged (e.g. the value
 *   associated with the event).
 */
enum class EventLevelEnum {
  NONE,
  BASIC,
  DETAILS
}

/**
 * An in-memory implementation of the `IEvent` interface that stores event data.
 *
 * This class provides a simple in-memory event store that can be used to track events related to
 * policy entities. Events are stored in a mutable list, and the level of detail logged for each
 * event can be configured using the `EventLevelEnum`.
 *
 * @property level The level of event logging to use, defaulting to `EventLevelEnum.BASIC`.
 */
class InMemoryEventHandler(private val level: EventLevelEnum = EventLevelEnum.BASIC) : IEvent {
  private var eventStore = mutableListOf<EventItem>()

  /**
   * Adds an event to the in-memory event store.
   *
   * @param contextId The context ID associated with the event.
   * @param entity The policy entity associated with the event.
   * @param entityId The ID of the policy entity associated with the event.
   * @param success Whether the event was successful or not.
   * @param value The value associated with the event, if any.
   * @param fromCache Whether the event was retrieved from a cache.
   * @param reason The reason for the event, if any.
   */
  override fun add(
      contextId: String,
      entity: PolicyEntityEnum,
      entityId: String,
      success: Boolean,
      value: Any?,
      fromCache: Boolean,
      reason: String?
  ) {
    if (level == EventLevelEnum.NONE) return
    eventStore.add(
        EventItem(
            LocalDateTime.now(),
            contextId,
            entity,
            entityId,
            if (level == EventLevelEnum.DETAILS && value != null)
                if (value is String) value else value.toString()
            else null,
            success,
            fromCache,
            reason))
  }

  /**
   * Returns a list of all the event items stored in the in-memory event store.
   *
   * @return A list of [EventItem] objects representing the events stored in the event store.
   */
  override fun list(): List<EventItem> = eventStore.toList()
}
