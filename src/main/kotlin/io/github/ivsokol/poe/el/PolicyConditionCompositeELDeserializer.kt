package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.condition.*

internal object PolicyConditionCompositeELDeserializer : CommandDeserializer {
  /**
   * Deserializes a policy condition composite command from a registry entry.
   *
   * @param command The registry entry containing the command to deserialize.
   * @param childCommands The child commands associated with the composite command.
   * @param contents The contents associated with the composite command.
   * @param options The EL options for the composite command.
   * @param constraint The constraint associated with the composite command.
   * @param refType The policy entity reference type associated with the composite command.
   * @return The deserialized policy condition composite.
   */
  override fun deserialize(
      command: RegistryEntry,
      childCommands: List<Any>,
      contents: List<String>,
      options: ELOptions?,
      constraint: Any?,
      refType: PolicyEntityRefEnum?
  ): Any {
    check(command.entityType == PolicyEntityEnum.CONDITION_COMPOSITE) {
      "PolicyConditionCompositeDeserializer can only be used with '${command.command}' command"
    }
    check(contents.isEmpty()) { "PolicyConditionCompositeDeserializer can not have contents" }
    val conditions = childCommands.mapNotNull { it as? IPolicyConditionRefOrValue }
    check(conditions.isNotEmpty()) { "PolicyConditionAtomicDeserializer must have conditions" }

    return when (command.entryType) {
      EntryTypeEnum.ANY_OF ->
          PolicyConditionComposite(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              conditions = conditions,
              conditionCombinationLogic = ConditionCombinationLogicEnum.ANY_OF,
              strictCheck = options?.strictCheck)
      EntryTypeEnum.ALL_OF ->
          PolicyConditionComposite(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              conditions = conditions,
              conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
              strictCheck = options?.strictCheck)
      EntryTypeEnum.NOT ->
          PolicyConditionComposite(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              conditions = conditions,
              conditionCombinationLogic = ConditionCombinationLogicEnum.NOT)
      EntryTypeEnum.N_OF -> {
        checkNotNull(options) { "Options must be provided for command '${command.command}'" }
        checkNotNull(options.minimumConditions) {
          "#opts(minimumConditions=?) must be provided for command '${command.command}'"
        }
        PolicyConditionComposite(
            id = options.id,
            version = options.version,
            description = options.description,
            labels = options.labels,
            negateResult = options.negateResult,
            conditions = conditions,
            conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
            minimumConditions = options.minimumConditions,
            optimizeNOfRun = options.optimizeNOfRun,
            strictCheck = options.strictCheck)
      }
      else ->
          throw IllegalArgumentException(
              "Unknown operation: ${command.entryType} on command: ${command.command}")
    }
  }
}
