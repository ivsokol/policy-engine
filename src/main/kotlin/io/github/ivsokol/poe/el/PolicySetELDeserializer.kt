package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.action.IPolicyActionRefOrValue
import io.github.ivsokol.poe.condition.IPolicyConditionRefOrValue
import io.github.ivsokol.poe.policy.*

internal object PolicySetELDeserializer : CommandDeserializer {
  /**
   * Deserializes a policy set command from the registry.
   *
   * This deserializer can only be used with the 'POLICY_SET' command type. It expects the command
   * to have a list of child commands, which can include policy references, policy relationships,
   * action references, and action relationships.
   *
   * The deserializer will create a [PolicySet] instance based on the command and child command
   * information, including the policy combination logic, constraints, actions, and child policies.
   *
   * @param command The registry entry for the policy set command.
   * @param childCommands The list of child commands associated with the policy set.
   * @param contents The list of content strings associated with the policy set (should be empty).
   * @param options The EL options for the policy set.
   * @param constraint The constraint associated with the policy set (must be an
   *   [IPolicyConditionRefOrValue]).
   * @param refType The policy entity reference type (must be [PolicyEntityRefEnum.POLICY_SET]).
   * @return The deserialized [PolicySet] instance.
   */
  override fun deserialize(
      command: RegistryEntry,
      childCommands: List<Any>,
      contents: List<String>,
      options: ELOptions?,
      constraint: Any?,
      refType: PolicyEntityRefEnum?
  ): Any {
    check(command.entityType == PolicyEntityEnum.POLICY_SET) {
      "PolicySetELDeserializer can only be used with '${command.command}' command"
    }
    check(contents.isEmpty()) { "PolicySetELDeserializer can not have contents" }
    val policies = childCommands.mapNotNull { it as? IPolicyRefOrValue }
    val policyRelationships = childCommands.mapNotNull { it as? PolicyRelationship }
    val allPolicyRelationships =
        if (policies.isNotEmpty() || policyRelationships.isNotEmpty()) {
          val mappedPolicies = policies.map { PolicyRelationship(policy = it) }
          mappedPolicies + policyRelationships
        } else null
    check(allPolicyRelationships.isNullOrEmpty().not()) {
      "PolicySetELDeserializer must have policies"
    }

    constraint?.also {
      check(it is IPolicyConditionRefOrValue) {
        "PolicySetELDeserializer must have constraint of type IPolicyConditionRefOrValue"
      }
    }

    val actions = childCommands.mapNotNull { it as? IPolicyActionRefOrValue }
    val actionRelationships = childCommands.mapNotNull { it as? PolicyActionRelationship }
    val allActionRelationships =
        if (actions.isNotEmpty() || actionRelationships.isNotEmpty()) {
          val mappedActions = actions.map { PolicyActionRelationship(action = it) }
          mappedActions + actionRelationships
        } else null

    return when (command.entryType) {
      EntryTypeEnum.DENY_OVERRIDES ->
          PolicySet(
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
              skipCache = options?.skipCache,
              runChildActions = options?.runChildActions,
              indeterminateOnActionFail = options?.indeterminateOnActionFail,
              policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
              policies = allPolicyRelationships!!,
          )
      EntryTypeEnum.PERMIT_OVERRIDES ->
          PolicySet(
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
              skipCache = options?.skipCache,
              runChildActions = options?.runChildActions,
              indeterminateOnActionFail = options?.indeterminateOnActionFail,
              policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_OVERRIDES,
              policies = allPolicyRelationships!!,
          )
      EntryTypeEnum.FIRST_APPLICABLE ->
          PolicySet(
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
              skipCache = options?.skipCache,
              runChildActions = options?.runChildActions,
              indeterminateOnActionFail = options?.indeterminateOnActionFail,
              policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
              policies = allPolicyRelationships!!,
          )
      EntryTypeEnum.DENY_UNLESS_PERMIT ->
          PolicySet(
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
              skipCache = options?.skipCache,
              runChildActions = options?.runChildActions,
              indeterminateOnActionFail = options?.indeterminateOnActionFail,
              strictUnlessLogic = options?.strictUnlessLogic,
              policyCombinationLogic = PolicyCombinationLogicEnum.DENY_UNLESS_PERMIT,
              policies = allPolicyRelationships!!,
          )
      EntryTypeEnum.PERMIT_UNLESS_DENY ->
          PolicySet(
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
              skipCache = options?.skipCache,
              runChildActions = options?.runChildActions,
              indeterminateOnActionFail = options?.indeterminateOnActionFail,
              strictUnlessLogic = options?.strictUnlessLogic,
              policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
              policies = allPolicyRelationships!!,
          )
      else -> throw IllegalArgumentException("Unknown target effect: ${command.entryType}")
    }
  }
}
