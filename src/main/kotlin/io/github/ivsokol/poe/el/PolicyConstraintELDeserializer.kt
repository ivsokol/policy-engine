package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.condition.*

internal object PolicyConstraintELDeserializer : CommandDeserializer {
  /**
   * Deserializes a policy constraint command from a registry entry.
   *
   * @param command The registry entry containing the constraint command.
   * @param childCommands The child commands associated with the constraint.
   * @param contents The contents of the constraint, which should be empty.
   * @param options The EL options, which are not used.
   * @param constraint The constraint object, which is not used.
   * @param refType The policy entity reference type, which is not used.
   * @return The first policy condition reference or value from the child commands.
   * @throws IllegalArgumentException if the command is not a constraint, if the contents are not
   *   empty, or if there is not exactly one condition.
   */
  override fun deserialize(
      command: RegistryEntry,
      childCommands: List<Any>,
      contents: List<String>,
      options: ELOptions?,
      constraint: Any?,
      refType: PolicyEntityRefEnum?
  ): Any {
    check(command.entryType == EntryTypeEnum.CONSTRAINT) {
      "PolicyConstraintELDeserializer can only be used with '${command.command}' command"
    }
    check(contents.isEmpty()) { "PolicyConstraintELDeserializer can not have contents" }
    val conditions = childCommands.mapNotNull { it as? IPolicyConditionRefOrValue }
    check(conditions.isNotEmpty()) { "PolicyConstraintELDeserializer must have condition" }
    check(conditions.size == 1) { "PolicyConstraintELDeserializer must have exactly one condition" }

    return conditions[0]
  }
}
