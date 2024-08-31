package com.github.ivsokol.poe.catalog

import com.github.ivsokol.poe.SemVer
import com.github.ivsokol.poe.condition.*
import com.github.ivsokol.poe.variable.IPolicyVariable
import com.github.ivsokol.poe.variable.PolicyVariableRef
import com.github.ivsokol.poe.variable.PolicyVariableResolver
import com.github.ivsokol.poe.variable.PolicyVariableStatic
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class PolicyCatalogCircularConditionReferenceTest :
    FunSpec({
      test("all refs ok") {
        val givenResolvers: List<PolicyVariableResolver> = emptyList()
        val givenVariables: List<IPolicyVariable> =
            listOf(
                PolicyVariableStatic(id = "pvs1", version = SemVer(1, 0, 0), value = "str1"),
                PolicyVariableStatic(id = "pvs2", value = "str1"),
            )
        val givenConditions: List<IPolicyCondition> =
            listOf(
                PolicyConditionAtomic(
                    id = "pca1",
                    version = SemVer(1, 0, 0),
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvs1"), PolicyVariableRef("pvs2"))),
                PolicyConditionAtomic(
                    id = "pca2",
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvs1"), PolicyVariableRef("pvs2"))),
                PolicyConditionComposite(
                    id = "pcc1",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pcc11"), PolicyConditionRef("pcc12"))),
                PolicyConditionComposite(
                    id = "pcc11",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions =
                        listOf(PolicyConditionRef("pcc111"), PolicyConditionRef("pcc112"))),
                PolicyConditionComposite(
                    id = "pcc111",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pca1"), PolicyConditionRef("pca2"))),
                PolicyConditionComposite(
                    id = "pcc112",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pca1"), PolicyConditionRef("pca2"))),
                PolicyConditionComposite(
                    id = "pcc12",
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pcc121"), PolicyConditionRef("pca1"))),
                PolicyConditionComposite(
                    id = "pcc121",
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pca1"), PolicyConditionRef("pca2"))),
            )

        val actual =
            PolicyCatalog(
                id = "test-catalog",
                policyConditions = givenConditions,
                policyVariables = givenVariables,
                policyVariableResolvers = givenResolvers)
        actual shouldNotBe null
      }

      test("shallow circular ref on self") {
        val givenResolvers: List<PolicyVariableResolver> = emptyList()
        val givenVariables: List<IPolicyVariable> =
            listOf(
                PolicyVariableStatic(id = "pvs1", version = SemVer(1, 0, 0), value = "str1"),
                PolicyVariableStatic(id = "pvs2", value = "str1"),
            )
        val givenConditions: List<IPolicyCondition> =
            listOf(
                PolicyConditionAtomic(
                    id = "pca1",
                    version = SemVer(1, 0, 0),
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvs1"), PolicyVariableRef("pvs2"))),
                PolicyConditionAtomic(
                    id = "pca2",
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvs1"), PolicyVariableRef("pvs2"))),
                PolicyConditionComposite(
                    id = "pcc2",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pcc1"), PolicyConditionRef("pcc2"))),
                PolicyConditionComposite(
                    id = "pcc1",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pca1"), PolicyConditionRef("pca2"))),
            )

        shouldThrow<IllegalStateException> {
              PolicyCatalog(
                  id = "test-catalog",
                  policyConditions = givenConditions,
                  policyVariables = givenVariables,
                  policyVariableResolvers = givenResolvers)
            }
            .message shouldBe "Circular references in catalog"
      }

      test("deep circular ref on self via self refs") {
        val givenResolvers: List<PolicyVariableResolver> = emptyList()
        val givenVariables: List<IPolicyVariable> =
            listOf(
                PolicyVariableStatic(id = "pvs1", version = SemVer(1, 0, 0), value = "str1"),
                PolicyVariableStatic(id = "pvs2", value = "str1"),
            )
        val givenConditions: List<IPolicyCondition> =
            listOf(
                PolicyConditionAtomic(
                    id = "pca1",
                    version = SemVer(1, 0, 0),
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvs1"), PolicyVariableRef("pvs2"))),
                PolicyConditionAtomic(
                    id = "pca2",
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvs1"), PolicyVariableRef("pvs2"))),
                PolicyConditionComposite(
                    id = "pcc1",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pcc11"), PolicyConditionRef("pcc12"))),
                PolicyConditionComposite(
                    id = "pcc11",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions =
                        listOf(PolicyConditionRef("pcc111"), PolicyConditionRef("pcc112"))),
                PolicyConditionComposite(
                    id = "pcc111",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pca1"), PolicyConditionRef("pca2"))),
                PolicyConditionComposite(
                    id = "pcc112",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions =
                        listOf(
                            PolicyConditionRef("pcc1"), // self break
                            PolicyConditionRef("pca2"))),
                PolicyConditionComposite(
                    id = "pcc12",
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pcc121"), PolicyConditionRef("pca1"))),
                PolicyConditionComposite(
                    id = "pcc121",
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pca1"), PolicyConditionRef("pca2"))),
            )

        shouldThrow<IllegalStateException> {
              PolicyCatalog(
                  id = "test-catalog",
                  policyConditions = givenConditions,
                  policyVariables = givenVariables,
                  policyVariableResolvers = givenResolvers)
            }
            .message shouldBe "Circular references in catalog"
      }

      test("deep circular ref on self via self embedded refs") {
        val givenResolvers: List<PolicyVariableResolver> = emptyList()
        val givenVariables: List<IPolicyVariable> =
            listOf(
                PolicyVariableStatic(id = "pvs1", version = SemVer(1, 0, 0), value = "str1"),
                PolicyVariableStatic(id = "pvs2", value = "str1"),
            )
        val givenConditions: List<IPolicyCondition> =
            listOf(
                PolicyConditionAtomic(
                    id = "pca1",
                    version = SemVer(1, 0, 0),
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvs1"), PolicyVariableRef("pvs2"))),
                PolicyConditionAtomic(
                    id = "pca2",
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvs1"), PolicyVariableRef("pvs2"))),
                PolicyConditionComposite(
                    id = "pcc1",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions =
                        listOf(
                            PolicyConditionComposite(
                                conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                                conditions =
                                    listOf(
                                        PolicyConditionComposite(
                                            conditionCombinationLogic =
                                                ConditionCombinationLogicEnum.ALL_OF,
                                            conditions =
                                                listOf(
                                                    PolicyConditionRef("pca1"),
                                                    PolicyConditionRef("pca2"))),
                                        PolicyConditionComposite(
                                            conditionCombinationLogic =
                                                ConditionCombinationLogicEnum.ALL_OF,
                                            conditions =
                                                listOf(
                                                    PolicyConditionRef("pcc1"), // self break
                                                    PolicyConditionRef("pca2"))),
                                    )),
                            PolicyConditionComposite(
                                conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                                conditions =
                                    listOf(
                                        PolicyConditionComposite(
                                            conditionCombinationLogic =
                                                ConditionCombinationLogicEnum.ALL_OF,
                                            conditions =
                                                listOf(
                                                    PolicyConditionRef("pca1"),
                                                    PolicyConditionRef("pca2"))),
                                        PolicyConditionRef("pca1"))),
                        )),
            )

        shouldThrow<IllegalStateException> {
              PolicyCatalog(
                  id = "test-catalog",
                  policyConditions = givenConditions,
                  policyVariables = givenVariables,
                  policyVariableResolvers = givenResolvers)
            }
            .message shouldBe "Circular references in catalog"
      }

      test("deep circular ref on self via other refs") {
        val givenResolvers: List<PolicyVariableResolver> = emptyList()
        val givenVariables: List<IPolicyVariable> =
            listOf(
                PolicyVariableStatic(id = "pvs1", version = SemVer(1, 0, 0), value = "str1"),
                PolicyVariableStatic(id = "pvs2", value = "str1"),
            )
        val givenConditions: List<IPolicyCondition> =
            listOf(
                PolicyConditionAtomic(
                    id = "pca1",
                    version = SemVer(1, 0, 0),
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvs1"), PolicyVariableRef("pvs2"))),
                PolicyConditionAtomic(
                    id = "pca2",
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvs1"), PolicyVariableRef("pvs2"))),
                PolicyConditionComposite(
                    id = "pcc1",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pcc11"), PolicyConditionRef("pcc12"))),
                PolicyConditionComposite(
                    id = "pcc11",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions =
                        listOf(PolicyConditionRef("pcc111"), PolicyConditionRef("pcc112"))),
                PolicyConditionComposite(
                    id = "pcc111",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pca1"), PolicyConditionRef("pca2"))),
                PolicyConditionComposite(
                    id = "pcc112",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pca1"), PolicyConditionRef("pca2"))),
                PolicyConditionComposite(
                    id = "pcc12",
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pcc121"), PolicyConditionRef("pca1"))),
                PolicyConditionComposite(
                    id = "pcc121",
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions =
                        listOf(
                            PolicyConditionRef("pca1"),
                            PolicyConditionRef("pcc3") // todo other break
                            )),
                PolicyConditionComposite(
                    id = "pcc3",
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pcc1"), PolicyConditionRef("pca1")),
                ))

        shouldThrow<IllegalStateException> {
              PolicyCatalog(
                  id = "test-catalog",
                  policyConditions = givenConditions,
                  policyVariables = givenVariables,
                  policyVariableResolvers = givenResolvers)
            }
            .message shouldBe "Circular references in catalog"
      }

      test("deep circular ref on self via other embedded refs") {
        val givenResolvers: List<PolicyVariableResolver> = emptyList()
        val givenVariables: List<IPolicyVariable> =
            listOf(
                PolicyVariableStatic(id = "pvs1", version = SemVer(1, 0, 0), value = "str1"),
                PolicyVariableStatic(id = "pvs2", value = "str1"),
            )
        val givenConditions: List<IPolicyCondition> =
            listOf(
                PolicyConditionAtomic(
                    id = "pca1",
                    version = SemVer(1, 0, 0),
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvs1"), PolicyVariableRef("pvs2"))),
                PolicyConditionAtomic(
                    id = "pca2",
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvs1"), PolicyVariableRef("pvs2"))),
                PolicyConditionComposite(
                    id = "pcc1",
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions =
                        listOf(
                            PolicyConditionComposite(
                                conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                                conditions =
                                    listOf(
                                        PolicyConditionComposite(
                                            conditionCombinationLogic =
                                                ConditionCombinationLogicEnum.ALL_OF,
                                            conditions =
                                                listOf(
                                                    PolicyConditionRef("pca1"),
                                                    PolicyConditionRef("pca2"))),
                                        PolicyConditionComposite(
                                            conditionCombinationLogic =
                                                ConditionCombinationLogicEnum.ALL_OF,
                                            conditions =
                                                listOf(
                                                    PolicyConditionRef("pca1"),
                                                    PolicyConditionRef("pca2"))),
                                    )),
                            PolicyConditionComposite(
                                conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                                conditions =
                                    listOf(
                                        PolicyConditionComposite(
                                            conditionCombinationLogic =
                                                ConditionCombinationLogicEnum.ALL_OF,
                                            conditions =
                                                listOf(
                                                    PolicyConditionRef("pca1"),
                                                    PolicyConditionRef("pcc3") // todo other break
                                                    )),
                                        PolicyConditionRef("pca1"))),
                        )),
                PolicyConditionComposite(
                    id = "pcc3",
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pcc1"), PolicyConditionRef("pca1")),
                ))

        shouldThrow<IllegalStateException> {
              PolicyCatalog(
                  id = "test-catalog",
                  policyConditions = givenConditions,
                  policyVariables = givenVariables,
                  policyVariableResolvers = givenResolvers)
            }
            .message shouldBe "Circular references in catalog"
      }
    })
