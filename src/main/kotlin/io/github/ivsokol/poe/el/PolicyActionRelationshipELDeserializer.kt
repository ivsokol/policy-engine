package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.action.IPolicyActionRefOrValue
import io.github.ivsokol.poe.condition.*
import io.github.ivsokol.poe.policy.PolicyActionRelationship

internal object PolicyActionRelationshipELDeserializer : CommandDeserializer {
  /**
   * Deserializes a policy action relationship from a registry entry.
   *
   * @param command The registry entry to deserialize.
   * @param childCommands The child commands of the registry entry.
   * @param contents The contents of the registry entry.
   * @param options The EL options for the registry entry.
   * @param constraint The constraint for the registry entry.
   * @param refType The policy entity reference type for the registry entry.
   * @return The deserialized policy action relationship.
   */
  override fun deserialize(
      command: RegistryEntry,
      childCommands: List<Any>,
      contents: List<String>,
      options: ELOptions?,
      constraint: Any?,
      refType: PolicyEntityRefEnum?
  ): Any {
    check(command.entryType == EntryTypeEnum.ACTION_RELATIONSHIP) {
      "PolicyActionRelationshipELDeserializer can only be used with '${command.command}' command"
    }
    check(contents.isEmpty()) { "PolicyActionRelationshipELDeserializer can not have contents" }
    val actions = childCommands.mapNotNull { it as? IPolicyActionRefOrValue }
    check(actions.isNotEmpty()) { "PolicyActionRelationshipELDeserializer must have actions" }
    check(actions.size == 1) {
      "PolicyActionRelationshipELDeserializer must have exactly one action"
    }
    constraint?.also {
      check(it is IPolicyConditionRefOrValue) {
        "PolicyActionRelationshipELDeserializer must have constraint of type IPolicyConditionRefOrValue"
      }
    }

    return PolicyActionRelationship(
        action = actions[0],
        constraint = constraint?.let { it as IPolicyConditionRefOrValue },
        executionMode = options?.executionMode,
        priority = options?.priority,
    )
  }
}
