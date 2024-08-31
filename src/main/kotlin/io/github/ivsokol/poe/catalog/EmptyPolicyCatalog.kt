package io.github.ivsokol.poe.catalog

import io.github.ivsokol.poe.condition.PolicyConditionDefault
import io.github.ivsokol.poe.policy.PolicyDefault
import io.github.ivsokol.poe.policy.PolicyResultEnum

fun EmptyPolicyCatalog() =
    PolicyCatalog(
        id = "empty-policy-catalog",
        policies =
            listOf(
                PolicyDefault(PolicyResultEnum.PERMIT),
                PolicyDefault(PolicyResultEnum.DENY),
                PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY),
                PolicyDefault(PolicyResultEnum.INDETERMINATE_PERMIT),
                PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY_PERMIT),
                PolicyDefault(PolicyResultEnum.NOT_APPLICABLE),
            ),
        policyConditions =
            listOf(
                PolicyConditionDefault(true),
                PolicyConditionDefault(false),
                PolicyConditionDefault(null)),
        policyVariables = emptyList(),
        policyVariableResolvers = emptyList(),
        policyActions = emptyList())
