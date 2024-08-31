package com.github.ivsokol.poe.examples

import com.github.ivsokol.poe.SemVer
import com.github.ivsokol.poe.action.PolicyActionRef
import com.github.ivsokol.poe.catalog.catalogSerializersModule
import com.github.ivsokol.poe.condition.OperationEnum
import com.github.ivsokol.poe.condition.PolicyConditionAtomic
import com.github.ivsokol.poe.condition.PolicyConditionRef
import com.github.ivsokol.poe.policy.*
import com.github.ivsokol.poe.variable.PolicyVariableDynamic
import com.github.ivsokol.poe.variable.PolicyVariableResolverRef
import com.github.ivsokol.poe.variable.PolicyVariableStatic
import io.kotest.core.spec.style.FunSpec
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PolicyExamples :
    FunSpec({
      val json = Json {
        serializersModule = catalogSerializersModule
        explicitNulls = false
        encodeDefaults = true
        prettyPrint = true
      }
      val folder = "policy"
      test("Permit policy with PolicyConditionRef") {
        val example =
            Policy(
                targetEffect = PolicyTargetEffectEnum.PERMIT,
                condition = PolicyConditionRef(id = "polCond1"))
        ExampleWriter.save(folder, "permit-policy-with-condition-ref", json.encodeToString(example))
      }

      test("Deny policy with PolicyCondition") {
        val example =
            Policy(
                targetEffect = PolicyTargetEffectEnum.DENY,
                condition =
                    PolicyConditionAtomic(
                        operation = OperationEnum.LESS_THAN,
                        args =
                            listOf(
                                PolicyVariableDynamic(
                                    resolvers =
                                        listOf(PolicyVariableResolverRef(id = "birthdayResolver"))),
                                PolicyVariableStatic(value = 18))),
                constraint = PolicyConditionRef(id = "customerScoringEvent"),
                actionExecutionStrategy = ActionExecutionStrategyEnum.RUN_ALL,
                actions =
                    listOf(
                        PolicyActionRelationship(
                            executionMode = setOf(ActionExecutionModeEnum.ON_DENY),
                            action = PolicyActionRef(id = "setCustomerAsMinor"))),
                ignoreErrors = false)
        ExampleWriter.save(folder, "deny-policy-with-condition", json.encodeToString(example))
      }

      test("Managed Policy") {
        val example =
            Policy(
                id = "isCustomerMinor",
                version = SemVer(1, 0, 0),
                description = "Denys customers below 18 years old",
                labels = listOf("customer", "scoring"),
                targetEffect = PolicyTargetEffectEnum.DENY,
                condition =
                    PolicyConditionAtomic(
                        operation = OperationEnum.LESS_THAN,
                        args =
                            listOf(
                                PolicyVariableDynamic(
                                    resolvers =
                                        listOf(PolicyVariableResolverRef(id = "birthdayResolver"))),
                                PolicyVariableStatic(value = 18))),
                constraint = PolicyConditionRef(id = "customerScoringEvent"),
                actionExecutionStrategy = ActionExecutionStrategyEnum.RUN_ALL,
                actions =
                    listOf(
                        PolicyActionRelationship(
                            executionMode = setOf(ActionExecutionModeEnum.ON_DENY),
                            action = PolicyActionRef(id = "setCustomerAsMinor"))),
                ignoreErrors = false)
        ExampleWriter.save(folder, "managed-policy", json.encodeToString(example))
      }

      test("PolicySet with Policy references") {
        val example =
            PolicySet(
                policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                policies =
                    listOf(
                        PolicyRelationship(policy = PolicyRef(id = "isCustomerMinor")),
                        PolicyRelationship(policy = PolicyRef(id = "isCustomerInFraudList")),
                    ))
        ExampleWriter.save(folder, "policy-set-with-policy-refs", json.encodeToString(example))
      }

      test("Managed PolicySet") {
        val example =
            PolicySet(
                id = "isScoringPositive",
                version = SemVer(1, 0, 0),
                description = "Denys customers below 18 years old",
                labels = listOf("customer", "scoring"),
                policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                policies =
                    listOf(
                        PolicyRelationship(policy = PolicyRef(id = "isCustomerMinor")),
                        PolicyRelationship(policy = PolicyRef(id = "isCustomerInFraudList")),
                    ))
        ExampleWriter.save(folder, "managed-policy-set", json.encodeToString(example))
      }

      test("PolicyDefault permit") {
        val example = PolicyDefault(PolicyResultEnum.PERMIT)
        ExampleWriter.save(folder, "policy-default-permit", json.encodeToString(example))
      }
      test("PolicyDefault deny") {
        val example = PolicyDefault(PolicyResultEnum.DENY)
        ExampleWriter.save(folder, "policy-default-deny", json.encodeToString(example))
      }
      test("PolicyDefault indeterminate") {
        val example = PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY_PERMIT)
        ExampleWriter.save(folder, "policy-default-indeterminate", json.encodeToString(example))
      }
      test("PolicyDefault indeterminate permit") {
        val example = PolicyDefault(PolicyResultEnum.INDETERMINATE_PERMIT)
        ExampleWriter.save(
            folder, "policy-default-indeterminate-permit", json.encodeToString(example))
      }
      test("PolicyDefault indeterminate deny") {
        val example = PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY)
        ExampleWriter.save(
            folder, "policy-default-indeterminate-deny", json.encodeToString(example))
      }
      test("PolicyDefault notApplicable") {
        val example = PolicyDefault(PolicyResultEnum.NOT_APPLICABLE)
        ExampleWriter.save(folder, "policy-default-not-applicable", json.encodeToString(example))
      }
    })
