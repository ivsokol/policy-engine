package io.github.ivsokol.poe.action

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enum representing the different types of actions that can be performed.
 * - `SAVE`: Represents a save action.
 * - `CLEAR`: Represents a clear action.
 * - `JSON_MERGE`: Represents a JSON merge action.
 * - `JSON_PATCH`: Represents a JSON patch action.
 */
@Serializable
enum class ActionTypeEnum {
  @SerialName("save") SAVE,
  @SerialName("clear") CLEAR,
  @SerialName("jsonMerge") JSON_MERGE,
  @SerialName("jsonPatch") JSON_PATCH
}
