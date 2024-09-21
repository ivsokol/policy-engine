package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.action.IPolicyActionRefOrValue
import io.github.ivsokol.poe.condition.IPolicyConditionRefOrValue
import io.github.ivsokol.poe.policy.Policy
import io.github.ivsokol.poe.policy.PolicyActionRelationship
import io.github.ivsokol.poe.policy.PolicyTargetEffectEnum

internal object PolicyELDeserializer : CommandDeserializer {
  /**
   * Deserializes a policy command from the registry.
   *
   * @param command The registry entry for the policy command.
   * @param childCommands The child commands associated with the policy command.
   * @param contents The contents of the policy command.
   * @param options The EL options for the policy command.
   * @param constraint The constraint for the policy command.
   * @param refType The reference type for the policy command.
   * @return The deserialized policy.
   */
  override fun deserialize(
      command: RegistryEntry,
      childCommands: List<Any>,
      contents: List<String>,
      options: ELOptions?,
      constraint: Any?,
      refType: PolicyEntityRefEnum?
  ): Any {
    check(command.entityType == PolicyEntityEnum.POLICY) {
      "PolicyELDeserializer can only be used with '${command.command}' command"
    }
    check(contents.isEmpty()) { "PolicyELDeserializer can not have contents" }
    val conditions = childCommands.mapNotNull { it as? IPolicyConditionRefOrValue }
    check(conditions.isNotEmpty()) { "PolicyELDeserializer must have conditions" }
    check(conditions.size == 1) { "PolicyELDeserializer must have exactly one condition" }

    constraint?.also {
      check(it is IPolicyConditionRefOrValue) {
        "PolicyELDeserializer must have constraint of type IPolicyConditionRefOrValue"
      }
    }

    val actions = childCommands.mapNotNull { it as? IPolicyActionRefOrValue }
    val actionRelationships = childCommands.mapNotNull { it as? PolicyActionRelationship }
    val allActionRelationships =
        if (actions.isNotEmpty() || actionRelationships.isNotEmpty()) {
          val mappedActions = actions.map { PolicyActionRelationship(action = it) }
          mappedActions + actionRelationships
        } else null

    val targetEffect =
        when (command.entryType) {
          EntryTypeEnum.PERMIT -> PolicyTargetEffectEnum.PERMIT
          EntryTypeEnum.DENY -> PolicyTargetEffectEnum.DENY
          else -> throw IllegalArgumentException("Unknown target effect: ${command.entryType}")
        }

    return Policy(
        id = options?.id,
        version = options?.version,
        description = options?.description,
        labels = options?.labels,
        constraint = constraint?.let { it as IPolicyConditionRefOrValue },
        actions = allActionRelationships,
        lenientConstraints = options?.lenientConstraints,
        actionExecutionStrategy = options?.actionExecutionStrategy,
        ignoreErrors = options?.ignoreErrors,
        priority = options?.priority,
        targetEffect = targetEffect,
        condition = conditions[0],
        strictTargetEffect = options?.strictTargetEffect,
    )
  }
}
