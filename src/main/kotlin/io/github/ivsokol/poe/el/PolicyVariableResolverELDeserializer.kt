package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.variable.*

internal object PolicyVariableResolverELDeserializer : CommandDeserializer {
  /**
   * Deserializes a command of type [PolicyEntityEnum.VALUE_RESOLVER] into a
   * [PolicyVariableResolver] instance.
   *
   * The deserializer supports the following entry types:
   * - [EntryTypeEnum.KEY]: Deserializes the command contents as a key for the
   *   [PolicyVariableResolver].
   * - [EntryTypeEnum.JMES_PATH]: Deserializes the command contents as a JMESPath expression for the
   *   [PolicyVariableResolver].
   * - [EntryTypeEnum.JQ]: Deserializes the command contents as a jq expression for the
   *   [PolicyVariableResolver].
   *
   * The deserializer checks that the command has the correct entity type, has only one parsed
   * content, and has no child commands.
   *
   * @param command The [RegistryEntry] command to deserialize.
   * @param childCommands The list of child commands (must be empty).
   * @param contents The list of parsed contents (must have one element).
   * @param options The [ELOptions] for the command.
   * @param constraint The constraint object (not used).
   * @param refType The [PolicyEntityRefEnum] reference type (not used).
   * @return The deserialized [PolicyVariableResolver] instance.
   * @throws IllegalArgumentException if the command is not of the correct entity type, has more
   *   than one parsed content, or has child commands.
   */
  override fun deserialize(
      command: RegistryEntry,
      childCommands: List<Any>,
      contents: List<String>,
      options: ELOptions?,
      constraint: Any?,
      refType: PolicyEntityRefEnum?
  ): Any {
    check(command.entityType == PolicyEntityEnum.VALUE_RESOLVER) {
      "PolicyVariableResolverDeserializer can only be used with '${command.command}' command"
    }
    check(contents.size == 1) { "PolicyVariableResolver can only have one parsed content" }
    check(childCommands.isEmpty()) { "PolicyVariableResolver can not have child commands" }

    val body = contents.first()
    return when (command.entryType) {
      // key
      EntryTypeEnum.KEY ->
          PolicyVariableResolver(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              key = body,
              engine = PolicyVariableResolverEngineEnum.KEY,
              source = options?.source,
          )
      EntryTypeEnum.JMES_PATH ->
          PolicyVariableResolver(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              path = body,
              engine = PolicyVariableResolverEngineEnum.JMES_PATH,
              key = options?.key,
              source = options?.source,
          )
      EntryTypeEnum.JQ ->
          PolicyVariableResolver(
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              path = body,
              engine = PolicyVariableResolverEngineEnum.JQ,
              key = options?.key,
              source = options?.source,
          )
      // others
      else -> throw IllegalArgumentException("Unsupported type: ${command.entryType}")
    }
  }
}
