/* (C)2024 */
package com.github.ivsokol.poe

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enum representing the different types of policy entities that can be defined in the system.
 * - `VARIABLE_DYNAMIC`: Represents a dynamic policy variable.
 * - `VARIABLE_STATIC`: Represents a static policy variable.
 * - `VALUE_RESOLVER`: Represents a value resolver for dynamic policy variables.
 * - `CONDITION_ATOMIC`: Represents an atomic policy condition.
 * - `CONDITION_COMPOSITE`: Represents a composite policy condition.
 * - `CONDITION_DEFAULT`: Represents a default policy condition.
 * - `POLICY`: Represents a policy.
 * - `POLICY_SET`: Represents a set of policies.
 * - `POLICY_DEFAULT`: Represents a default policy.
 * - `POLICY_ACTION`: Represents a policy action.
 * - `POLICY_ACTION_SAVE`: Represents a policy action to save a value.
 * - `POLICY_ACTION_CLEAR`: Represents a policy action to clear a value.
 * - `POLICY_ACTION_JSON_PATCH`: Represents a policy action to apply a JSON patch.
 * - `POLICY_ACTION_JSON_MERGE`: Represents a policy action to apply a JSON merge.
 * - `POLICY_CATALOG`: Represents a policy catalog.
 * - `ENGINE_START`: Represents the start of the policy engine execution.
 * - `ENGINE_END`: Represents the end of the policy engine execution.
 */
enum class PolicyEntityEnum {
  VARIABLE_DYNAMIC,
  VARIABLE_STATIC,
  VALUE_RESOLVER,
  CONDITION_ATOMIC,
  CONDITION_COMPOSITE,
  CONDITION_DEFAULT,
  POLICY,
  POLICY_SET,
  POLICY_DEFAULT,
  POLICY_ACTION,
  POLICY_ACTION_SAVE,
  POLICY_ACTION_CLEAR,
  POLICY_ACTION_JSON_PATCH,
  POLICY_ACTION_JSON_MERGE,
  POLICY_CATALOG,
  ENGINE_START,
  ENGINE_END
}

/**
 * Enum representing the different types of policy entity references that can be used in the system.
 * - `POLICY_REF`: Represents a reference to a policy.
 * - `POLICY_ACTION_REF`: Represents a reference to a policy action.
 * - `POLICY_CONDITION_REF`: Represents a reference to a policy condition.
 * - `POLICY_VARIABLE_REF`: Represents a reference to a policy variable.
 * - `POLICY_VARIABLE_RESOLVER_REF`: Represents a reference to a policy variable resolver.
 */
@Serializable
enum class PolicyEntityRefEnum {
  @SerialName("PolicyRef") POLICY_REF,
  @SerialName("PolicyActionRef") POLICY_ACTION_REF,
  @SerialName("PolicyConditionRef") POLICY_CONDITION_REF,
  @SerialName("PolicyVariableRef") POLICY_VARIABLE_REF,
  @SerialName("PolicyVariableResolverRef") POLICY_VARIABLE_RESOLVER_REF,
}

/**
 * Enum representing the different types of label search logic that can be used in the system.
 * - `ANY_OF`: Indicates that at least one of the specified labels must match.
 * - `ALL_OF`: Indicates that all the specified labels must match.
 */
enum class LabelSearchLogicEnum {
  ANY_OF,
  ALL_OF
}

/**
 * Enum representing the different types of circular references that can occur in the system.
 * - `CONDITION`: Indicates a circular reference in a policy condition.
 * - `POLICY`: Indicates a circular reference in a policy.
 */
enum class CircularReferenceEnum {
  CONDITION,
  POLICY,
}
