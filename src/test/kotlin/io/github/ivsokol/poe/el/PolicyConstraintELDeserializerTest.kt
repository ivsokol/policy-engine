package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.condition.*
import io.github.ivsokol.poe.policy.PolicyDefault
import io.github.ivsokol.poe.policy.PolicyResultEnum
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class PolicyConstraintELDeserializerTest :
    DescribeSpec({
      describe("constraint") {
        it("no constraint") {
          val given = """#permit()"""
          val expected = PolicyDefault(PolicyResultEnum.PERMIT)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has atomic constraint") {
          val given = """#permit(*constraint(*gt(#int(1), #int(2))))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  constraint =
                      PolicyConditionAtomic(
                          operation = OperationEnum.GREATER_THAN,
                          args =
                              listOf(
                                  PolicyVariableStatic(value = 1, type = VariableValueTypeEnum.INT),
                                  PolicyVariableStatic(
                                      value = 2, type = VariableValueTypeEnum.INT))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has composite constraint") {
          val given = """#permit(*constraint(*all(#ref(pca1), #ref(pca2))))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  constraint =
                      PolicyConditionComposite(
                          conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                          conditions =
                              listOf(PolicyConditionRef("pca1"), PolicyConditionRef("pca2"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has ref constraint") {
          val given = """#permit(*constraint(#ref(pca1)))"""
          val expected =
              PolicyDefault(PolicyResultEnum.PERMIT, constraint = PolicyConditionRef("pca1"))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should throw on bad constraint class ") {
          val given = """#permit(*constraint(#int(1))))"""
          shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }.message shouldBe
              "Child command type mismatch on position 20 for command '*constraint'. Expected: 'REFERENCE, CONDITION_ATOMIC, CONDITION_COMPOSITE, CONDITION_DEFAULT', actual: 'VARIABLE_STATIC'"
        }
        it("should throw on constraint content") {
          val given = """#permit(*constraint(content))"""
          shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }.message shouldBe
              "PolicyConstraintELDeserializer can not have contents"
        }
        it("should throw on multiple conditions in constraint") {
          val given = """#permit(*constraint(*gt(#int(1), #int(2)), *gt(#int(1), #int(2))))"""
          shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }.message shouldBe
              "Too many arguments on position 8 for command '*constraint'. Expected: 1, actual: 2"
        }
      }
    })
