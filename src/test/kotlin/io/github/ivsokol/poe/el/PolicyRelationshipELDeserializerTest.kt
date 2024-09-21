package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.condition.OperationEnum
import io.github.ivsokol.poe.condition.PolicyConditionAtomic
import io.github.ivsokol.poe.condition.PolicyConditionRef
import io.github.ivsokol.poe.policy.*
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class PolicyRelationshipELDeserializerTest :
    DescribeSpec({
      describe("policy relationship") {
        it("policy ref") {
          val given = """*DOverrides(#ref(pol1))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("policy set") {
          val given = """*DOverrides(*POverrides(#ref(pol1)))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies =
                      listOf(
                          PolicyRelationship(
                              PolicySet(
                                  policyCombinationLogic =
                                      PolicyCombinationLogicEnum.PERMIT_OVERRIDES,
                                  policies = listOf(PolicyRelationship(PolicyRef("pol1")))))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("policy default") {
          val given = """*DOverrides(#permit())"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies = listOf(PolicyRelationship(PolicyDefault(PolicyResultEnum.PERMIT))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("policy") {
          val given = """*DOverrides(*permit(#ref(cond1)))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies =
                      listOf(
                          PolicyRelationship(
                              Policy(
                                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                                  condition = PolicyConditionRef("cond1")))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("policy relationship minimal") {
          val given = """*DOverrides(*pol(*permit(#ref(cond1))))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies =
                      listOf(
                          PolicyRelationship(
                              Policy(
                                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                                  condition = PolicyConditionRef("cond1")))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("policy relationship with constraint") {
          val given =
              """*DOverrides(*pol(*permit(#ref(cond1)),*constraint(*gt(#int(1), #int(2)))))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies =
                      listOf(
                          PolicyRelationship(
                              Policy(
                                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                                  condition = PolicyConditionRef("cond1")),
                              constraint =
                                  PolicyConditionAtomic(
                                      operation = OperationEnum.GREATER_THAN,
                                      args =
                                          listOf(
                                              PolicyVariableStatic(
                                                  value = 1, type = VariableValueTypeEnum.INT),
                                              PolicyVariableStatic(
                                                  value = 2, type = VariableValueTypeEnum.INT))))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("policy relationship with options") {
          val given = """*DOverrides(*pol(*permit(#ref(cond1)),#opts(runAction,priority=10)))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies =
                      listOf(
                          PolicyRelationship(
                              Policy(
                                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                                  condition = PolicyConditionRef("cond1")),
                              runAction = true,
                              priority = 10)))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("policy relationship with policies") {
          val given = """*DOverrides(*deny(#ref(cond2)),*pol(*permit(#ref(cond1))))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies =
                      listOf(
                          PolicyRelationship(
                              Policy(
                                  targetEffect = PolicyTargetEffectEnum.DENY,
                                  condition = PolicyConditionRef("cond2")),
                          ),
                          PolicyRelationship(
                              Policy(
                                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                                  condition = PolicyConditionRef("cond1")))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should throw on policy relationship with multiple policies") {
          val given = """*DOverrides(*pol(*permit(#ref(cond1)),*deny(#ref(cond2))))"""
          shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }.message shouldBe
              "Too many arguments on position 12 for command '*pol'. Expected: 1, actual: 2"
        }
        it("should throw on content") {
          val given = """*DOverrides(*pol(content))"""
          shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }.message shouldBe
              "PolicyRelationshipELDeserializer can not have contents"
        }
        it("should throw on bad child command") {
          val given = """*DOverrides(*pol(*gt(#int(1),#int(2))))"""
          shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }.message shouldBe
              "Child command type mismatch on position 17 for command '*pol'. Expected: 'POLICY, POLICY_SET, POLICY_DEFAULT, REFERENCE', actual: 'CONDITION_ATOMIC'"
        }
      }
    })
