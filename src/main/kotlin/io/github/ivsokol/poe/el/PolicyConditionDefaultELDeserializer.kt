package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.condition.PolicyConditionDefault

internal object PolicyConditionDefaultELDeserializer : CommandDeserializer {
  /**
   * Deserializes a policy condition default command from a registry entry.
   *
   * @param command The registry entry containing the command to deserialize.
   * @param childCommands The list of child commands, which must be empty.
   * @param contents The list of contents, which must be empty.
   * @param options The EL options, which must be null.
   * @param constraint The constraint, which is ignored.
   * @param refType The policy entity reference type, which is ignored.
   * @return A [PolicyConditionDefault] instance representing the deserialized command.
   * @throws IllegalArgumentException if the command is not of the expected type or has unexpected
   *   properties.
   */
  override fun deserialize(
      command: RegistryEntry,
      childCommands: List<Any>,
      contents: List<String>,
      options: ELOptions?,
      constraint: Any?,
      refType: PolicyEntityRefEnum?
  ): Any {
    check(command.entityType == PolicyEntityEnum.CONDITION_DEFAULT) {
      "PolicyConditionDefaultDeserializer can only be used with '${command.command}' command"
    }
    check(contents.isEmpty()) { "PolicyConditionDefaultDeserializer can not have contents" }
    check(childCommands.isEmpty()) {
      "PolicyConditionDefaultDeserializer can not have child commands"
    }
    check(options == null) { "PolicyConditionDefaultDeserializer can not have options" }

    return when (command.entryType) {
      EntryTypeEnum.DEFAULT_TRUE -> PolicyConditionDefault(true)
      EntryTypeEnum.DEFAULT_FALSE -> PolicyConditionDefault(false)
      EntryTypeEnum.DEFAULT_NULL -> PolicyConditionDefault(null)
      else ->
          throw IllegalArgumentException(
              "Unknown operation: ${command.entryType} on command: ${command.command}")
    }
  }
}
