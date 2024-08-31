package io.github.ivsokol.poe.examples

import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.catalog.catalogSerializersModule
import io.github.ivsokol.poe.condition.*
import io.github.ivsokol.poe.variable.PolicyVariableRef
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.core.spec.style.FunSpec
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PolicyConditionExamples :
    FunSpec({
      val json = Json {
        serializersModule = catalogSerializersModule
        explicitNulls = false
        encodeDefaults = true
        prettyPrint = true
      }
      val folder = "policy-condition"
      test("Minimal PolicyConditionAtomic with unary operation") {
        val example =
            PolicyConditionAtomic(
                operation = OperationEnum.IS_BLANK,
                args = listOf(PolicyVariableRef(id = "polVar1")))
        ExampleWriter.save(
            folder, "minimal-policy-condition-atomic-unary", json.encodeToString(example))
      }

      test("Minimal PolicyConditionAtomic") {
        val example =
            PolicyConditionAtomic(
                operation = OperationEnum.GREATER_THAN,
                args =
                    listOf(
                        PolicyVariableRef(id = "polVar1"),
                        PolicyVariableStatic(value = 42, type = VariableValueTypeEnum.INT)))
        ExampleWriter.save(folder, "minimal-policy-condition-atomic", json.encodeToString(example))
      }

      test("PolicyConditionAtomic with params") {
        val example =
            PolicyConditionAtomic(
                operation = OperationEnum.EQUALS,
                stringIgnoreCase = true,
                args =
                    listOf(
                        PolicyVariableRef(id = "polVar1"),
                        PolicyVariableStatic(
                            value = "fooBar", type = VariableValueTypeEnum.STRING)))
        ExampleWriter.save(folder, "policy-condition-atomic-params", json.encodeToString(example))
      }

      test("Managed PolicyConditionAtomic") {
        val example =
            PolicyConditionAtomic(
                id = "polCond1",
                version = SemVer(1, 2, 3),
                description = "This is a managed PolicyConditionAtomic",
                labels = listOf("label1"),
                operation = OperationEnum.EQUALS,
                stringIgnoreCase = true,
                args =
                    listOf(
                        PolicyVariableRef(id = "polVar1"),
                        PolicyVariableStatic(
                            value = "fooBar", type = VariableValueTypeEnum.STRING)))
        ExampleWriter.save(folder, "policy-condition-atomic-managed", json.encodeToString(example))
      }

      test("PolicyConditionComposite with not logic") {
        val example =
            PolicyConditionComposite(
                conditionCombinationLogic = ConditionCombinationLogicEnum.NOT,
                conditions =
                    listOf(
                        PolicyConditionAtomic(
                            operation = OperationEnum.IS_BLANK,
                            args = listOf(PolicyVariableRef(id = "polVar1")))))

        ExampleWriter.save(folder, "policy-condition-composite-not", json.encodeToString(example))
      }

      test("PolicyConditionComposite with anyOf logic") {
        val example =
            PolicyConditionComposite(
                conditionCombinationLogic = ConditionCombinationLogicEnum.ANY_OF,
                conditions =
                    listOf(
                        PolicyConditionAtomic(
                            operation = OperationEnum.IS_BLANK,
                            args = listOf(PolicyVariableRef(id = "polVar1"))),
                        PolicyConditionRef(
                            id = "polCond1",
                        ),
                        PolicyConditionDefault(true)))
        ExampleWriter.save(
            folder, "policy-condition-composite-any-of", json.encodeToString(example))
      }

      test("PolicyConditionComposite with allOf logic") {
        val example =
            PolicyConditionComposite(
                conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                strictCheck = false,
                conditions =
                    listOf(
                        PolicyConditionAtomic(
                            operation = OperationEnum.IS_BLANK,
                            args = listOf(PolicyVariableRef(id = "polVar1"))),
                        PolicyConditionRef(
                            id = "polCond1",
                        )))
        ExampleWriter.save(
            folder, "policy-condition-composite-all-of", json.encodeToString(example))
      }

      test("PolicyConditionComposite with nOf logic") {
        val example =
            PolicyConditionComposite(
                conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                minimumConditions = 2,
                optimizeNOfRun = false,
                conditions =
                    listOf(
                        PolicyConditionAtomic(
                            operation = OperationEnum.IS_BLANK,
                            args = listOf(PolicyVariableRef(id = "polVar1"))),
                        PolicyConditionAtomic(
                            operation = OperationEnum.IS_EMPTY,
                            args = listOf(PolicyVariableRef(id = "polVar2"))),
                        PolicyConditionRef(
                            id = "polCond1",
                        )))
        ExampleWriter.save(folder, "policy-condition-composite-n-of", json.encodeToString(example))
      }

      test("managed PolicyConditionComposite") {
        val example =
            PolicyConditionComposite(
                id = "polCond1",
                version = SemVer(1, 2, 3),
                description = "This is a managed PolicyConditionComposite",
                labels = listOf("label1"),
                conditionCombinationLogic = ConditionCombinationLogicEnum.NOT,
                conditions =
                    listOf(
                        PolicyConditionAtomic(
                            operation = OperationEnum.IS_BLANK,
                            args = listOf(PolicyVariableRef(id = "polVar1")))))

        ExampleWriter.save(
            folder, "policy-condition-composite-managed", json.encodeToString(example))
      }

      test("PolicyConditionDefault null") {
        val example = PolicyConditionDefault()
        ExampleWriter.save(folder, "policy-condition-default-null", json.encodeToString(example))
      }
      test("PolicyConditionDefault true") {
        val example = PolicyConditionDefault(true)
        ExampleWriter.save(folder, "policy-condition-default-true", json.encodeToString(example))
      }
      test("PolicyConditionDefault false") {
        val example = PolicyConditionDefault(false)
        ExampleWriter.save(folder, "policy-condition-default-false", json.encodeToString(example))
      }
      test("PolicyConditionDefault true reference") {
        val example = PolicyConditionRef("${'$'}true")
        ExampleWriter.save(
            folder, "policy-condition-default-ref-true", json.encodeToString(example))
      }
    })
