package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.action.PolicyActionClear
import io.github.ivsokol.poe.action.PolicyActionJsonMerge
import io.github.ivsokol.poe.action.PolicyActionJsonPatch
import io.github.ivsokol.poe.action.PolicyActionSave
import io.github.ivsokol.poe.variable.IPolicyVariableRefOrValue

internal object PolicyActionELDeserializer : CommandDeserializer {
  /**
   * Deserializes a policy action command from a registry entry, child commands, contents, and
   * options.
   *
   * @param command The registry entry for the policy action command.
   * @param childCommands The child commands associated with the policy action.
   * @param contents The contents of the policy action.
   * @param options The EL options for the policy action.
   * @param constraint The constraint for the policy action.
   * @param refType The policy entity reference type for the policy action.
   * @return The deserialized policy action.
   * @throws IllegalArgumentException if the command is not a policy action, the contents are empty
   *   or have more than one element, or the operation is unknown.
   */
  override fun deserialize(
      command: RegistryEntry,
      childCommands: List<Any>,
      contents: List<String>,
      options: ELOptions?,
      constraint: Any?,
      refType: PolicyEntityRefEnum?
  ): Any {
    check(command.entityType == PolicyEntityEnum.POLICY_ACTION) {
      "PolicyActionELDeserializer can only be used with '${command.command}' command"
    }
    check(contents.isNotEmpty()) { "PolicyActionELDeserializer must have content" }
    check(contents.size == 1) { "PolicyActionELDeserializer must have only one content" }
    val key = contents[0]
    val variables = childCommands.mapNotNull { it as? IPolicyVariableRefOrValue }

    return when (command.entryType) {
      EntryTypeEnum.SAVE -> {
        check(variables.isNotEmpty()) { "PolicyActionSave must have variables" }
        check(variables.size == 1) { "PolicyActionSave must have only one variable" }
        PolicyActionSave(
            id = options?.id,
            version = options?.version,
            description = options?.description,
            labels = options?.labels,
            key = key,
            value = variables[0],
            failOnMissingKey = options?.failOnMissingKey,
            failOnExistingKey = options?.failOnExistingKey,
            failOnNullSource = options?.failOnNullSource)
      }
      EntryTypeEnum.CLEAR -> {
        check(variables.isEmpty()) { "PolicyActionClear must not have variables" }
        PolicyActionClear(
            id = options?.id,
            version = options?.version,
            description = options?.description,
            labels = options?.labels,
            key = key,
            failOnMissingKey = options?.failOnMissingKey,
        )
      }
      EntryTypeEnum.PATCH -> {
        check(variables.isNotEmpty()) { "PolicyActionJsonPatch must have variables" }
        check(variables.size == 2) { "PolicyActionJsonPatch must have exactly two variables" }
        PolicyActionJsonPatch(
            id = options?.id,
            version = options?.version,
            description = options?.description,
            labels = options?.labels,
            key = key,
            source = variables[0],
            patch = variables[1],
            failOnMissingKey = options?.failOnMissingKey,
            failOnExistingKey = options?.failOnExistingKey,
            failOnNullSource = options?.failOnNullSource,
            castNullSourceToArray = options?.castNullSourceToArray,
        )
      }
      EntryTypeEnum.MERGE -> {
        check(variables.isNotEmpty()) { "PolicyActionJsonMerge must have variables" }
        check(variables.size == 2) { "PolicyActionJsonMerge must have exactly two variables" }
        PolicyActionJsonMerge(
            id = options?.id,
            version = options?.version,
            description = options?.description,
            labels = options?.labels,
            key = key,
            source = variables[0],
            merge = variables[1],
            failOnMissingKey = options?.failOnMissingKey,
            failOnExistingKey = options?.failOnExistingKey,
            failOnNullSource = options?.failOnNullSource,
            failOnNullMerge = options?.failOnNullMerge,
            destinationType = options?.type,
            destinationFormat = options?.format,
        )
      }
      else ->
          throw IllegalArgumentException(
              "Unknown operation: ${command.entryType} on command: ${command.command}")
    }
  }
}
