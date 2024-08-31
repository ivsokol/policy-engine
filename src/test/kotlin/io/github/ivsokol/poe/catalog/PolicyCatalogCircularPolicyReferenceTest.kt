package io.github.ivsokol.poe.catalog

import io.github.ivsokol.poe.condition.PolicyConditionDefault
import io.github.ivsokol.poe.policy.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class PolicyCatalogCircularPolicyReferenceTest :
    FunSpec({
      test("all refs ok") {
        val givenPolicies: List<IPolicy> =
            listOf(
                Policy(
                    id = "p1",
                    targetEffect = PolicyTargetEffectEnum.PERMIT,
                    condition = PolicyConditionDefault(true)),
                Policy(
                    id = "p2",
                    targetEffect = PolicyTargetEffectEnum.PERMIT,
                    condition = PolicyConditionDefault(true)),
                PolicySet(
                    id = "ps1",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("ps11")),
                            PolicyRelationship(policy = PolicyRef("ps12")))),
                PolicySet(
                    id = "ps11",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("ps111")),
                            PolicyRelationship(policy = PolicyRef("ps112")))),
                PolicySet(
                    id = "ps111",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("p1")),
                            PolicyRelationship(policy = PolicyRef("p2")))),
                PolicySet(
                    id = "ps112",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("p1")),
                            PolicyRelationship(policy = PolicyRef("p2")))),
                PolicySet(
                    id = "ps12",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("ps121")),
                            PolicyRelationship(policy = PolicyRef("p2")))),
                PolicySet(
                    id = "ps121",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("p1")),
                            PolicyRelationship(policy = PolicyRef("p2")))),
            )

        val actual = PolicyCatalog(id = "test-catalog", policies = givenPolicies)
        actual shouldNotBe null
      }

      test("shallow circular ref on self") {
        val givenPolicies: List<IPolicy> =
            listOf(
                Policy(
                    id = "p1",
                    targetEffect = PolicyTargetEffectEnum.PERMIT,
                    condition = PolicyConditionDefault(true)),
                Policy(
                    id = "p2",
                    targetEffect = PolicyTargetEffectEnum.PERMIT,
                    condition = PolicyConditionDefault(true)),
                PolicySet(
                    id = "ps1",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("ps1")),
                            PolicyRelationship(policy = PolicyRef("p1")))),
                PolicySet(
                    id = "ps2",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("p1")),
                            PolicyRelationship(policy = PolicyRef("p2")))),
            )

        shouldThrow<IllegalStateException> {
              PolicyCatalog(id = "test-catalog", policies = givenPolicies)
            }
            .message shouldBe "Circular references in catalog"
      }

      test("deep circular ref on self via self refs") {
        val givenPolicies: List<IPolicy> =
            listOf(
                Policy(
                    id = "p1",
                    targetEffect = PolicyTargetEffectEnum.PERMIT,
                    condition = PolicyConditionDefault(true)),
                Policy(
                    id = "p2",
                    targetEffect = PolicyTargetEffectEnum.PERMIT,
                    condition = PolicyConditionDefault(true)),
                PolicySet(
                    id = "ps1",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("ps11")),
                            PolicyRelationship(policy = PolicyRef("ps12")))),
                PolicySet(
                    id = "ps11",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("ps111")),
                            PolicyRelationship(policy = PolicyRef("ps112")))),
                PolicySet(
                    id = "ps111",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("p1")),
                            PolicyRelationship(policy = PolicyRef("p2")))),
                PolicySet(
                    id = "ps112",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("ps1")), // self break
                            PolicyRelationship(policy = PolicyRef("p2")))),
                PolicySet(
                    id = "ps12",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("ps121")),
                            PolicyRelationship(policy = PolicyRef("p2")))),
                PolicySet(
                    id = "ps121",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("p1")),
                            PolicyRelationship(policy = PolicyRef("p2")))),
            )

        shouldThrow<IllegalStateException> {
              PolicyCatalog(id = "test-catalog", policies = givenPolicies)
            }
            .message shouldBe "Circular references in catalog"
      }

      test("deep circular ref on self via self embedded refs") {
        val givenPolicies: List<IPolicy> =
            listOf(
                Policy(
                    id = "p1",
                    targetEffect = PolicyTargetEffectEnum.PERMIT,
                    condition = PolicyConditionDefault(true)),
                Policy(
                    id = "p2",
                    targetEffect = PolicyTargetEffectEnum.PERMIT,
                    condition = PolicyConditionDefault(true)),
                PolicySet(
                    id = "ps1",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(
                                policy =
                                    PolicySet(
                                        id = "ps11",
                                        policyCombinationLogic =
                                            PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                                        policies =
                                            listOf(
                                                PolicyRelationship(
                                                    policy =
                                                        PolicySet(
                                                            id = "ps111",
                                                            policyCombinationLogic =
                                                                PolicyCombinationLogicEnum
                                                                    .PERMIT_UNLESS_DENY,
                                                            policies =
                                                                listOf(
                                                                    PolicyRelationship(
                                                                        policy = PolicyRef("p1")),
                                                                    PolicyRelationship(
                                                                        policy =
                                                                            PolicyRef("p2"))))),
                                                PolicyRelationship(
                                                    policy =
                                                        PolicySet(
                                                            id = "ps112",
                                                            policyCombinationLogic =
                                                                PolicyCombinationLogicEnum
                                                                    .PERMIT_UNLESS_DENY,
                                                            policies =
                                                                listOf(
                                                                    PolicyRelationship(
                                                                        policy =
                                                                            PolicyRef(
                                                                                "ps1")), // self
                                                                    // break
                                                                    PolicyRelationship(
                                                                        policy =
                                                                            PolicyRef("p2"))))),
                                            ))),
                        )),
            )

        shouldThrow<IllegalStateException> {
              PolicyCatalog(id = "test-catalog", policies = givenPolicies)
            }
            .message shouldBe "Circular references in catalog"
      }

      test("deep circular ref on self via other refs") {
        val givenPolicies: List<IPolicy> =
            listOf(
                Policy(
                    id = "p1",
                    targetEffect = PolicyTargetEffectEnum.PERMIT,
                    condition = PolicyConditionDefault(true)),
                Policy(
                    id = "p2",
                    targetEffect = PolicyTargetEffectEnum.PERMIT,
                    condition = PolicyConditionDefault(true)),
                PolicySet(
                    id = "ps1",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("ps11")),
                            PolicyRelationship(policy = PolicyRef("ps12")))),
                PolicySet(
                    id = "ps11",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("ps111")),
                            PolicyRelationship(policy = PolicyRef("ps112")))),
                PolicySet(
                    id = "ps111",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("p1")),
                            PolicyRelationship(policy = PolicyRef("p2")))),
                PolicySet(
                    id = "ps112",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("p1")),
                            PolicyRelationship(policy = PolicyRef("p2")))),
                PolicySet(
                    id = "ps12",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("ps121")),
                            PolicyRelationship(policy = PolicyRef("p2")))),
                PolicySet(
                    id = "ps121",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("p1")),
                            PolicyRelationship(policy = PolicyRef("ps3")))),
                PolicySet(
                    id = "ps3",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("ps1")), // other break
                            PolicyRelationship(policy = PolicyRef("p2")))),
            )

        shouldThrow<IllegalStateException> {
              PolicyCatalog(id = "test-catalog", policies = givenPolicies)
            }
            .message shouldBe "Circular references in catalog"
      }

      test("deep circular ref on self via other embedded refs") {
        val givenPolicies: List<IPolicy> =
            listOf(
                Policy(
                    id = "p1",
                    targetEffect = PolicyTargetEffectEnum.PERMIT,
                    condition = PolicyConditionDefault(true)),
                Policy(
                    id = "p2",
                    targetEffect = PolicyTargetEffectEnum.PERMIT,
                    condition = PolicyConditionDefault(true)),
                PolicySet(
                    id = "ps1",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(
                                policy =
                                    PolicySet(
                                        id = "ps11",
                                        policyCombinationLogic =
                                            PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                                        policies =
                                            listOf(
                                                PolicyRelationship(
                                                    policy =
                                                        PolicySet(
                                                            id = "ps111",
                                                            policyCombinationLogic =
                                                                PolicyCombinationLogicEnum
                                                                    .PERMIT_UNLESS_DENY,
                                                            policies =
                                                                listOf(
                                                                    PolicyRelationship(
                                                                        policy = PolicyRef("p1")),
                                                                    PolicyRelationship(
                                                                        policy =
                                                                            PolicyRef("p2"))))),
                                                PolicyRelationship(
                                                    policy =
                                                        PolicySet(
                                                            id = "ps112",
                                                            policyCombinationLogic =
                                                                PolicyCombinationLogicEnum
                                                                    .PERMIT_UNLESS_DENY,
                                                            policies =
                                                                listOf(
                                                                    PolicyRelationship(
                                                                        policy =
                                                                            PolicyRef(
                                                                                "ps3")), // other
                                                                    // break
                                                                    PolicyRelationship(
                                                                        policy =
                                                                            PolicyRef("p2"))))),
                                            ))),
                        )),
                PolicySet(
                    id = "ps3",
                    policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                    policies =
                        listOf(
                            PolicyRelationship(policy = PolicyRef("ps1")), // other break
                            PolicyRelationship(policy = PolicyRef("p2")))),
            )

        shouldThrow<IllegalStateException> {
              PolicyCatalog(id = "test-catalog", policies = givenPolicies)
            }
            .message shouldBe "Circular references in catalog"
      }
    })
