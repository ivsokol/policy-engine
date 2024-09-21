package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.condition.OperationEnum
import io.github.ivsokol.poe.condition.PolicyConditionAtomic
import io.github.ivsokol.poe.variable.IPolicyVariableRefOrValue

internal object PolicyConditionAtomicELDeserializer : CommandDeserializer {
  /**
   * Deserializes a policy condition atomic command from a registry entry.
   *
   * This deserializer is responsible for creating a [PolicyConditionAtomic] instance based on the
   * command type and its arguments. It supports various operation types such as greater than, less
   * than, equality, and more.
   *
   * @param command The registry entry containing the command information.
   * @param childCommands The list of child commands, which should be empty for this deserializer.
   * @param contents The list of content strings, which should be empty for this deserializer.
   * @param options Optional EL options to configure the deserialization.
   * @param constraint An optional constraint object.
   * @param refType An optional reference type.
   * @return The deserialized [PolicyConditionAtomic] instance.
   * @throws IllegalArgumentException if the command is not of the expected type or has invalid
   *   arguments.
   */
  override fun deserialize(
      command: RegistryEntry,
      childCommands: List<Any>,
      contents: List<String>,
      options: ELOptions?,
      constraint: Any?,
      refType: PolicyEntityRefEnum?
  ): Any {
    check(command.entityType == PolicyEntityEnum.CONDITION_ATOMIC) {
      "PolicyConditionAtomicDeserializer can only be used with '${command.command}' command"
    }
    check(contents.isEmpty()) { "PolicyConditionAtomicDeserializer can not have contents" }
    val args = childCommands.mapNotNull { it as? IPolicyVariableRefOrValue }
    check(args.isNotEmpty()) { "PolicyConditionAtomicDeserializer must have args" }

    return when (command.entryType) {
      EntryTypeEnum.GT ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              stringIgnoreCase = options?.stringIgnoreCase,
              args = args,
              operation = OperationEnum.GREATER_THAN)
      EntryTypeEnum.GTE ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              stringIgnoreCase = options?.stringIgnoreCase,
              args = args,
              operation = OperationEnum.GREATER_THAN_EQUAL)
      EntryTypeEnum.LT ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              stringIgnoreCase = options?.stringIgnoreCase,
              args = args,
              operation = OperationEnum.LESS_THAN)
      EntryTypeEnum.LTE ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              stringIgnoreCase = options?.stringIgnoreCase,
              args = args,
              operation = OperationEnum.LESS_THAN_EQUAL)
      EntryTypeEnum.IS_NULL ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              args = args,
              operation = OperationEnum.IS_NULL)
      EntryTypeEnum.IS_NOT_NULL ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              args = args,
              operation = OperationEnum.IS_NOT_NULL)
      EntryTypeEnum.IS_EMPTY ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              args = args,
              operation = OperationEnum.IS_EMPTY)
      EntryTypeEnum.IS_NOT_EMPTY ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              args = args,
              operation = OperationEnum.IS_NOT_EMPTY)
      EntryTypeEnum.IS_BLANK ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              args = args,
              operation = OperationEnum.IS_BLANK)
      EntryTypeEnum.IS_NOT_BLANK ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              args = args,
              operation = OperationEnum.IS_NOT_BLANK)
      EntryTypeEnum.STARTS_WITH ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              stringIgnoreCase = options?.stringIgnoreCase,
              fieldsStrictCheck = options?.fieldsStrictCheck,
              arrayOrderStrictCheck = options?.arrayOrderStrictCheck,
              args = args,
              operation = OperationEnum.STARTS_WITH)
      EntryTypeEnum.ENDS_WITH ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              stringIgnoreCase = options?.stringIgnoreCase,
              fieldsStrictCheck = options?.fieldsStrictCheck,
              arrayOrderStrictCheck = options?.arrayOrderStrictCheck,
              args = args,
              operation = OperationEnum.ENDS_WITH)
      EntryTypeEnum.CONTAINS ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              stringIgnoreCase = options?.stringIgnoreCase,
              fieldsStrictCheck = options?.fieldsStrictCheck,
              arrayOrderStrictCheck = options?.arrayOrderStrictCheck,
              args = args,
              operation = OperationEnum.CONTAINS)
      EntryTypeEnum.IS_IN ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              stringIgnoreCase = options?.stringIgnoreCase,
              fieldsStrictCheck = options?.fieldsStrictCheck,
              arrayOrderStrictCheck = options?.arrayOrderStrictCheck,
              args = args,
              operation = OperationEnum.IS_IN)
      EntryTypeEnum.EQ ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              stringIgnoreCase = options?.stringIgnoreCase,
              fieldsStrictCheck = options?.fieldsStrictCheck,
              arrayOrderStrictCheck = options?.arrayOrderStrictCheck,
              args = args,
              operation = OperationEnum.EQUALS)
      EntryTypeEnum.POS ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              args = args,
              operation = OperationEnum.IS_POSITIVE)
      EntryTypeEnum.NEG ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              args = args,
              operation = OperationEnum.IS_NEGATIVE)
      EntryTypeEnum.ZERO ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              args = args,
              operation = OperationEnum.IS_ZERO)
      EntryTypeEnum.PAST ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              args = args,
              operation = OperationEnum.IS_PAST)
      EntryTypeEnum.FUTURE ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              args = args,
              operation = OperationEnum.IS_FUTURE)
      EntryTypeEnum.REGEXP ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              args = args,
              operation = OperationEnum.REGEXP_MATCH)
      EntryTypeEnum.HAS_KEY ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              args = args,
              operation = OperationEnum.HAS_KEY)
      EntryTypeEnum.UNIQUE ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              args = args,
              operation = OperationEnum.IS_UNIQUE)
      EntryTypeEnum.SCHEMA ->
          PolicyConditionAtomic(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              negateResult = options?.negateResult,
              args = args,
              operation = OperationEnum.SCHEMA_MATCH)
      else ->
          throw IllegalArgumentException(
              "Unknown operation: ${command.entryType} on command: ${command.command}")
    }
  }
}
