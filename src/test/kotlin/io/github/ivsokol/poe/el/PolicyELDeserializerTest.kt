package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.action.PolicyActionRef
import io.github.ivsokol.poe.action.PolicyActionSave
import io.github.ivsokol.poe.condition.*
import io.github.ivsokol.poe.policy.*
import io.github.ivsokol.poe.variable.PolicyVariableRef
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PolicyELDeserializerTest :
    DescribeSpec({
      describe("basic parsing") {
        it("should parse correct policy") {
          val given = """*permit(#ref(cond1))"""
          val expected =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionRef("cond1"))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }

        it("should throw exception on no close command") {
          val given = """*permit("""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }
          actual.message shouldContain "Command not completed on position 0"
        }

        it("should throw exception on content") {
          val given = """*permit(content)"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }
          actual.message shouldContain "PolicyELDeserializer can not have contents"
        }

        it("should throw exception on multiple conditions") {
          val given = """*permit(#ref(cond1),#ref(cond2))"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }
          actual.message shouldContain "PolicyELDeserializer must have exactly one condition"
        }

        it("should throw exception on child command") {
          val given = """*permit(*dyn(*key(foo)))"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }
          actual.message shouldContain
              "Child command type mismatch on position 8 for command '*permit'. Expected: 'CONDITION_ATOMIC, CONDITION_COMPOSITE, CONDITION_DEFAULT, REFERENCE, ACTION_RELATIONSHIP, POLICY_ACTION', actual: 'VARIABLE_DYNAMIC'"
        }

        it("should throw exception on bad command start ") {
          val given = """*permit{()"""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parsePolicy() }
          actual.message shouldContain "Unknown command *permit{ on position 0"
        }
      }
      describe("variations") {
        it("should parse correct permit policy with condition ref") {
          val given = """*permit(#ref(cond1))"""
          val expected =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionRef("cond1"))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct permit policy with condition atomic") {
          val given = """*permit(*gt(#int(1), #int(2)))"""
          val expected =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition =
                      PolicyConditionAtomic(
                          operation = OperationEnum.GREATER_THAN,
                          args =
                              listOf(
                                  PolicyVariableStatic(value = 1, type = VariableValueTypeEnum.INT),
                                  PolicyVariableStatic(value = 2, type = VariableValueTypeEnum.INT),
                              )))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct permit policy with condition composite") {
          val given = """*permit(*all(#ref(pcr1,1.2.3),#ref(pcr2)))"""
          val expected =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition =
                      PolicyConditionComposite(
                          conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                          conditions =
                              listOf(
                                  PolicyConditionRef("pcr1", SemVer(1, 2, 3)),
                                  PolicyConditionRef("pcr2"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct permit policy with condition default") {
          val given = """*permit(#true())"""
          val expected =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionDefault(true))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct permit policy with options") {
          val given =
              """*permit(#true(),#opts(lenientConstraints,ignoreErrors,priority=10,actionExecutionStrategy=untilSuccess,strictTargetEffect))"""
          val expected =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionDefault(true),
                  ignoreErrors = true,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS,
                  lenientConstraints = true,
                  priority = 10,
                  strictTargetEffect = true)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct deny policy") {
          val given = """*deny(#true())"""
          val expected =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(true))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct deny policy with options") {
          val given =
              """*deny(#true(),#opts(lenientConstraints,ignoreErrors,priority=10,actionExecutionStrategy=untilSuccess,strictTargetEffect))"""
          val expected =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(true),
                  ignoreErrors = true,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS,
                  lenientConstraints = true,
                  priority = 10,
                  strictTargetEffect = true)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
      }
      describe("constraint") {
        it("no constraint") {
          val given = """*permit(#ref(cond1))"""
          val expected =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionRef("cond1"))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has constraint") {
          val given = """*permit(#ref(cond1),*constraint(*gt(#int(1), #int(2))))"""
          val expected =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionRef("cond1"),
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
      }
      describe("actions") {
        it("no action") {
          val given = """*permit(#ref(cond1))"""
          val expected =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionRef("cond1"))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has action") {
          val given = """*permit(#ref(cond1),*save(foo,#ref(pvd1)))"""
          val expected =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionRef("cond1"),
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              PolicyActionSave(
                                  key = "foo",
                                  value = PolicyVariableRef(id = "pvd1"),
                              ))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has action relationship") {
          val given = """*permit(#ref(cond1),*act(#ref(pas1)))"""
          val expected =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionRef("cond1"),
                  actions = listOf(PolicyActionRelationship(PolicyActionRef("pas1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
      }
    })
