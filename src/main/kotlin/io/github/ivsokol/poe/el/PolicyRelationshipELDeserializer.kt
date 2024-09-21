package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.condition.*
import io.github.ivsokol.poe.policy.IPolicyRefOrValue
import io.github.ivsokol.poe.policy.PolicyRelationship

internal object PolicyRelationshipELDeserializer : CommandDeserializer {
  /**
   * Deserializes a policy relationship from a registry entry.
   *
   * @param command The registry entry containing the policy relationship data.
   * @param childCommands The child commands associated with the policy relationship.
   * @param contents The contents associated with the policy relationship.
   * @param options The EL options associated with the policy relationship.
   * @param constraint The constraint associated with the policy relationship.
   * @param refType The policy entity reference type associated with the policy relationship.
   * @return The deserialized policy relationship.
   * @throws IllegalArgumentException if the command is not of type POLICY_RELATIONSHIP, if the
   *   contents are not empty, if there are no policies, if there is not exactly one policy, or if
   *   the constraint is not of type IPolicyConditionRefOrValue.
   */
  override fun deserialize(
      command: RegistryEntry,
      childCommands: List<Any>,
      contents: List<String>,
      options: ELOptions?,
      constraint: Any?,
      refType: PolicyEntityRefEnum?
  ): Any {
    check(command.entryType == EntryTypeEnum.POLICY_RELATIONSHIP) {
      "PolicyRelationshipELDeserializer can only be used with '${command.command}' command"
    }
    check(contents.isEmpty()) { "PolicyRelationshipELDeserializer can not have contents" }
    val policies = childCommands.mapNotNull { it as? IPolicyRefOrValue }
    check(policies.isNotEmpty()) { "PolicyRelationshipELDeserializer must have policies" }
    check(policies.size == 1) { "PolicyRelationshipELDeserializer must have exactly one policy" }
    constraint?.also {
      check(it is IPolicyConditionRefOrValue) {
        "PolicyRelationshipELDeserializer must have constraint of type IPolicyConditionRefOrValue"
      }
    }

    return PolicyRelationship(
        policy = policies[0],
        constraint = constraint?.let { it as IPolicyConditionRefOrValue },
        runAction = options?.runAction,
        priority = options?.priority,
    )
  }
}
