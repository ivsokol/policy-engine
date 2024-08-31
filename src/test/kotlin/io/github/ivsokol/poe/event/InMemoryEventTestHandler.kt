/* (C)2024 */
package io.github.ivsokol.poe.event

import io.github.ivsokol.poe.IEvent
import io.github.ivsokol.poe.PolicyEntityEnum
import java.time.LocalDateTime

class InMemoryEventTestHandler : IEvent {
  private var eventStore = mutableListOf<EventItem>()

  override fun add(
      contextId: String,
      entity: PolicyEntityEnum,
      entityId: String,
      success: Boolean,
      value: Any?,
      fromCache: Boolean,
      reason: String?
  ) {
    eventStore.add(
        EventItem(
            LocalDateTime.now(),
            contextId,
            entity,
            entityId,
            if (value != null) if (value is String) value else value.toString() else null,
            success,
            fromCache,
            reason))
  }

  override fun list(): List<EventItem> = eventStore.toList()

  fun clear() = eventStore.clear()
}
