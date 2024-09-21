package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.action.IPolicyActionRefOrValue
import io.github.ivsokol.poe.condition.IPolicyConditionRefOrValue
import io.github.ivsokol.poe.policy.*

internal object PolicyDefaultELDeserializer : CommandDeserializer {
  /**
   * Deserializes a policy default command from the registry.
   *
   * @param command The registry entry for the policy default command.
   * @param childCommands The child commands associated with the policy default.
   * @param contents The contents of the policy default command.
   * @param options Optional EL options to apply to the policy default.
   * @param constraint The constraint associated with the policy default.
   * @param refType The reference type of the policy default.
   * @return The deserialized policy default.
   * @throws IllegalArgumentException if the command is not a policy default or the constraint is
   *   not of the expected type.
   */
  override fun deserialize(
      command: RegistryEntry,
      childCommands: List<Any>,
      contents: List<String>,
      options: ELOptions?,
      constraint: Any?,
      refType: PolicyEntityRefEnum?
  ): Any {
    check(command.entityType == PolicyEntityEnum.POLICY_DEFAULT) {
      "PolicyDefaultELDeserializer can only be used with '${command.command}' command"
    }
    check(contents.isEmpty()) { "PolicyDefaultELDeserializer can not have contents" }

    constraint?.also {
      check(it is IPolicyConditionRefOrValue) {
        "PolicyDefaultELDeserializer must have constraint of type IPolicyConditionRefOrValue"
      }
    }

    val actions = childCommands.mapNotNull { it as? IPolicyActionRefOrValue }
    val actionRelationships = childCommands.mapNotNull { it as? PolicyActionRelationship }
    val allActionRelationships =
        if (actions.isNotEmpty() || actionRelationships.isNotEmpty()) {
          val mappedActions = actions.map { PolicyActionRelationship(action = it) }
          mappedActions + actionRelationships
        } else null

    val default =
        when (command.entryType) {
          EntryTypeEnum.DEFAULT_PERMIT -> PolicyResultEnum.PERMIT
          EntryTypeEnum.DEFAULT_DENY -> PolicyResultEnum.DENY
          EntryTypeEnum.DEFAULT_NOT_APPLICABLE -> PolicyResultEnum.NOT_APPLICABLE
          EntryTypeEnum.DEFAULT_INDETERMINATE_DENY_PERMIT ->
              PolicyResultEnum.INDETERMINATE_DENY_PERMIT
          EntryTypeEnum.DEFAULT_INDETERMINATE_DENY -> PolicyResultEnum.INDETERMINATE_DENY
          EntryTypeEnum.DEFAULT_INDETERMINATE_PERMIT -> PolicyResultEnum.INDETERMINATE_PERMIT
          else -> throw IllegalArgumentException("Unknown default policy: ${command.entryType}")
        }

    return PolicyDefault(
        constraint = constraint?.let { it as IPolicyConditionRefOrValue },
        actions = allActionRelationships,
        lenientConstraints = options?.lenientConstraints,
        actionExecutionStrategy = options?.actionExecutionStrategy,
        ignoreErrors = options?.ignoreErrors,
        priority = options?.priority,
        default = default,
    )
  }
}
