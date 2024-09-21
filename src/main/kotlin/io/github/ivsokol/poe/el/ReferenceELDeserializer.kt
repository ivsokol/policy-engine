package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.SemVerSerializer
import io.github.ivsokol.poe.action.PolicyActionRef
import io.github.ivsokol.poe.condition.PolicyConditionRef
import io.github.ivsokol.poe.policy.PolicyRef
import io.github.ivsokol.poe.variable.PolicyVariableRef
import io.github.ivsokol.poe.variable.PolicyVariableResolverRef
import kotlinx.serialization.json.Json

internal object ReferenceELDeserializer : CommandDeserializer {
  /**
   * Deserializes a reference command from the registry.
   *
   * @param command The registry entry for the reference command.
   * @param childCommands The child commands of the reference command (should be empty).
   * @param contents The contents of the reference command, which should contain the ID and
   *   optionally the version.
   * @param options The EL options (unused).
   * @param constraint The constraint (unused).
   * @param refType The type of reference, which must be non-null.
   * @return The deserialized reference object, such as [PolicyRef], [PolicyConditionRef], etc.
   * @throws IllegalArgumentException if the reference is invalid (e.g. missing type, wrong entity
   *   type, invalid contents).
   */
  override fun deserialize(
      command: RegistryEntry,
      childCommands: List<Any>,
      contents: List<String>,
      options: ELOptions?,
      constraint: Any?,
      refType: PolicyEntityRefEnum?
  ): Any {
    check(refType != null) { "Reference must have a reference type" }
    check(command.entityType == PolicyEntityEnum.REFERENCE) {
      "Reference can only be used in a reference context"
    }
    check(contents.size in 1..2) { "Reference can only have id and optionally version" }
    check(childCommands.isEmpty()) { "Reference can not have child commands" }
    val version =
        contents.getOrNull(1)?.let {
          Json.decodeFromString(SemVerSerializer, it.wrapToJsonString())
        }

    return when (refType) {
      PolicyEntityRefEnum.POLICY_REF -> {
        val id = contents.first()
        PolicyRef(id, version)
      }
      PolicyEntityRefEnum.POLICY_CONDITION_REF -> {
        val id = contents.first()
        PolicyConditionRef(id, version)
      }
      PolicyEntityRefEnum.POLICY_VARIABLE_REF -> {
        val id = contents.first()
        PolicyVariableRef(id, version)
      }
      PolicyEntityRefEnum.POLICY_VARIABLE_RESOLVER_REF -> {
        val id = contents.first()
        PolicyVariableResolverRef(id, version)
      }
      PolicyEntityRefEnum.POLICY_ACTION_REF -> {
        val id = contents.first()
        PolicyActionRef(id, version)
      }
    }
  }
}
