package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.variable.IPolicyVariableResolverRefOrValue
import io.github.ivsokol.poe.variable.PolicyVariableDynamic

internal object PolicyVariableDynamicELDeserializer : CommandDeserializer {
  /**
   * Deserializes a PolicyVariableDynamic object from a RegistryEntry and its associated data.
   *
   * @param command The RegistryEntry containing the command to be deserialized.
   * @param childCommands The list of child commands associated with the command.
   * @param contents The list of content strings associated with the command.
   * @param options The ELOptions object containing additional options for the deserialization.
   * @param constraint The constraint object associated with the command.
   * @param refType The PolicyEntityRefEnum type associated with the command.
   * @return The deserialized PolicyVariableDynamic object.
   * @throws IllegalArgumentException if the command's entity type is not VARIABLE_DYNAMIC, if the
   *   contents list is not empty, or if the resolvers list is empty.
   */
  override fun deserialize(
      command: RegistryEntry,
      childCommands: List<Any>,
      contents: List<String>,
      options: ELOptions?,
      constraint: Any?,
      refType: PolicyEntityRefEnum?
  ): Any {
    check(command.entityType == PolicyEntityEnum.VARIABLE_DYNAMIC) {
      "PolicyVariableDynamicDeserializer can only be used with '${command.command}' command"
    }
    check(contents.isEmpty()) { "PolicyVariableDynamicDeserializer can not have contents" }
    val resolvers = childCommands.mapNotNull { it as? IPolicyVariableResolverRefOrValue }
    check(resolvers.isNotEmpty()) { "PolicyVariableDynamicDeserializer must have resolvers" }

    return PolicyVariableDynamic(
        id = options?.id,
        version = options?.version,
        description = options?.description,
        labels = options?.labels,
        resolvers = resolvers,
        type = options?.type,
        format = options?.format,
        timeFormat = options?.timeFormat,
        dateFormat = options?.dateFormat,
        dateTimeFormat = options?.dateTimeFormat,
    )
  }
}
