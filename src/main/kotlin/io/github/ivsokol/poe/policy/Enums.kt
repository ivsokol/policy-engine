package io.github.ivsokol.poe.policy

import io.github.ivsokol.poe.policy.ActionExecutionModeEnum.*
import io.github.ivsokol.poe.policy.ActionExecutionStrategyEnum.*
import io.github.ivsokol.poe.policy.PolicyCombinationLogicEnum.*
import io.github.ivsokol.poe.policy.PolicyResultEnum.*
import io.github.ivsokol.poe.policy.PolicyTargetEffectEnum.DENY
import io.github.ivsokol.poe.policy.PolicyTargetEffectEnum.PERMIT
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enum representing the possible effects of a policy target.
 *
 * @property PERMIT Indicates that the policy target permits the requested action.
 * @property DENY Indicates that the policy target denies the requested action.
 */
@Serializable
enum class PolicyTargetEffectEnum {
  @SerialName("permit") PERMIT,
  @SerialName("deny") DENY,
}

/**
 * Enum representing the possible results of a policy evaluation.
 *
 * @property PERMIT Indicates that the policy permits the requested action.
 * @property DENY Indicates that the policy denies the requested action.
 * @property INDETERMINATE_PERMIT Indicates that the policy evaluation is indeterminate, but the
 *   default result is to permit the action.
 * @property INDETERMINATE_DENY Indicates that the policy evaluation is indeterminate, but the
 *   default result is to deny the action.
 * @property INDETERMINATE_DENY_PERMIT Indicates that the policy evaluation is indeterminate, with
 *   no default result.
 * @property NOT_APPLICABLE Indicates that the policy is not applicable to the requested action.
 */
@Serializable
enum class PolicyResultEnum {
  @SerialName("permit") PERMIT,
  @SerialName("deny") DENY,
  @SerialName("indeterminatePermit") INDETERMINATE_PERMIT,
  @SerialName("indeterminateDeny") INDETERMINATE_DENY,
  @SerialName("indeterminate") INDETERMINATE_DENY_PERMIT,
  @SerialName("notApplicable") NOT_APPLICABLE;

  override fun toString(): String =
      when (this) {
        PERMIT -> "permit"
        DENY -> "deny"
        INDETERMINATE_PERMIT -> "indeterminatePermit"
        INDETERMINATE_DENY -> "indeterminateDeny"
        INDETERMINATE_DENY_PERMIT -> "indeterminate"
        NOT_APPLICABLE -> "notApplicable"
      }
}

/**
 * Enum representing the possible action execution strategies.
 *
 * @property RUN_ALL Indicates that all actions should be executed regardless of the result.
 * @property UNTIL_SUCCESS Indicates that actions should be executed until one succeeds.
 * @property STOP_ON_FAILURE Indicates that execution should stop if any action fails.
 * @property ROLLBACK_ON_FAILURE Indicates that a rollback should be performed if any action fails.
 */
@Serializable
enum class ActionExecutionStrategyEnum {
  @SerialName("runAll") RUN_ALL,
  @SerialName("untilSuccess") UNTIL_SUCCESS,
  @SerialName("stopOnFailure") STOP_ON_FAILURE,
  @SerialName("rollbackOnFailure") ROLLBACK_ON_FAILURE
}

/**
 * Enum representing the possible action execution modes.
 *
 * @property ON_PERMIT Indicates that the action should be executed when the policy result is
 *   PERMIT.
 * @property ON_DENY Indicates that the action should be executed when the policy result is DENY.
 * @property ON_INDETERMINATE Indicates that the action should be executed when the policy result is
 *   INDETERMINATE.
 * @property ON_NOT_APPLICABLE Indicates that the action should be executed when the policy result
 *   is NOT_APPLICABLE.
 */
@Serializable
enum class ActionExecutionModeEnum {
  @SerialName("onPermit") ON_PERMIT,
  @SerialName("onDeny") ON_DENY,
  @SerialName("onIndeterminate") ON_INDETERMINATE,
  @SerialName("onNotApplicable") ON_NOT_APPLICABLE
}

/**
 * Enum representing the possible policy combination logic strategies.
 *
 * @property DENY_OVERRIDES Indicates that if any policy evaluates to DENY, the overall result is
 *   DENY.
 * @property PERMIT_OVERRIDES Indicates that if any policy evaluates to PERMIT, the overall result
 *   is PERMIT.
 * @property DENY_UNLESS_PERMIT Indicates that the overall result is DENY unless any policy evaluate
 *   to PERMIT.
 * @property PERMIT_UNLESS_DENY Indicates that the overall result is PERMIT unless any policy
 *   evaluates to DENY.
 * @property FIRST_APPLICABLE Indicates that the overall result is the result of the first
 *   applicable policy.
 * @property ONLY_ONE_APPLICABLE Indicates that there must be only one applicable policy, otherwise
 *   the result is INDETERMINATE.
 */
@Serializable
enum class PolicyCombinationLogicEnum {
  @SerialName("denyOverrides") DENY_OVERRIDES,
  @SerialName("permitOverrides") PERMIT_OVERRIDES,
  @SerialName("denyUnlessPermit") DENY_UNLESS_PERMIT,
  @SerialName("permitUnlessDeny") PERMIT_UNLESS_DENY,
  @SerialName("firstApplicable") FIRST_APPLICABLE,
  @SerialName("onlyOneApplicable") ONLY_ONE_APPLICABLE,
}
