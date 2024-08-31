package com.github.ivsokol.poe.event

import com.github.ivsokol.poe.PolicyEntityEnum
import java.time.LocalDateTime

/**
 * Represents an event item that captures information about a policy-related event.
 *
 * @param timestamp The timestamp of the event.
 * @param contextId The unique identifier of the context in which the event occurred.
 * @param entity The type of policy entity associated with the event.
 * @param entityId The unique identifier of the policy entity associated with the event.
 * @param message An optional message describing the event.
 * @param success Indicates whether the event was successful or not.
 * @param fromCache Indicates whether the event data was retrieved from a cache.
 * @param reason An optional reason for the event.
 */
data class EventItem(
    val timestamp: LocalDateTime,
    val contextId: String,
    val entity: PolicyEntityEnum,
    val entityId: String,
    val message: String?,
    val success: Boolean,
    val fromCache: Boolean,
    val reason: String? = null
)
