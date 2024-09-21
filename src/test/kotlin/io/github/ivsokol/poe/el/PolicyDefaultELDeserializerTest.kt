package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.action.PolicyActionRef
import io.github.ivsokol.poe.action.PolicyActionSave
import io.github.ivsokol.poe.condition.OperationEnum
import io.github.ivsokol.poe.condition.PolicyConditionAtomic
import io.github.ivsokol.poe.policy.ActionExecutionStrategyEnum
import io.github.ivsokol.poe.policy.PolicyActionRelationship
import io.github.ivsokol.poe.policy.PolicyDefault
import io.github.ivsokol.poe.policy.PolicyResultEnum
import io.github.ivsokol.poe.variable.PolicyVariableRef
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PolicyDefaultELDeserializerTest :
    DescribeSpec({
      describe("basic parsing") {
        it("should parse correct policy") {
          val given = """#permit()"""
          val expected = PolicyDefault(PolicyResultEnum.PERMIT)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }

        it("should throw exception on no close command") {
          val given = """#permit("""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }
          actual.message shouldContain "Command not completed on position 0"
        }

        it("should throw exception on content") {
          val given = """#permit(content)"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }
          actual.message shouldContain "PolicyDefaultELDeserializer can not have contents"
        }

        it("should throw exception on child command") {
          val given = """#permit(*gt(#int(1), #int(2)))"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }
          actual.message shouldContain
              "Child command type mismatch on position 8 for command '#permit'. Expected: 'ACTION_RELATIONSHIP, POLICY_ACTION', actual: 'CONDITION_ATOMIC'"
        }

        it("should throw exception on bad command start ") {
          val given = """#permit{()"""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parsePolicy() }
          actual.message shouldContain "Unknown command #permit{ on position 0"
        }
      }
      describe("variations") {
        it("should parse correct permit policy") {
          val given = """#permit()"""
          val expected = PolicyDefault(PolicyResultEnum.PERMIT)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct permit policy with options") {
          val given =
              """#permit(#opts(lenientConstraints,ignoreErrors,priority=10,actionExecutionStrategy=untilSuccess))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  ignoreErrors = true,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS,
                  lenientConstraints = true,
                  priority = 10)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct deny policy") {
          val given = """#deny()"""
          val expected = PolicyDefault(PolicyResultEnum.DENY)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct deny policy with options") {
          val given =
              """#deny(#opts(lenientConstraints,ignoreErrors,priority=10,actionExecutionStrategy=untilSuccess))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.DENY,
                  ignoreErrors = true,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS,
                  lenientConstraints = true,
                  priority = 10)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct notApplicable policy") {
          val given = """#NA()"""
          val expected = PolicyDefault(PolicyResultEnum.NOT_APPLICABLE)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct notApplicable policy with options") {
          val given =
              """#NA(#opts(lenientConstraints,ignoreErrors,priority=10,actionExecutionStrategy=untilSuccess))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.NOT_APPLICABLE,
                  ignoreErrors = true,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS,
                  lenientConstraints = true,
                  priority = 10)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct indeterminateDP policy") {
          val given = """#indDP()"""
          val expected = PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY_PERMIT)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct indeterminateDP policy with options") {
          val given =
              """#indDP(#opts(lenientConstraints,ignoreErrors,priority=10,actionExecutionStrategy=untilSuccess))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.INDETERMINATE_DENY_PERMIT,
                  ignoreErrors = true,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS,
                  lenientConstraints = true,
                  priority = 10)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct indeterminateD policy") {
          val given = """#indD()"""
          val expected = PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct indeterminateD policy with options") {
          val given =
              """#indD(#opts(lenientConstraints,ignoreErrors,priority=10,actionExecutionStrategy=untilSuccess))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.INDETERMINATE_DENY,
                  ignoreErrors = true,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS,
                  lenientConstraints = true,
                  priority = 10)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct indeterminateP policy") {
          val given = """#indP()"""
          val expected = PolicyDefault(PolicyResultEnum.INDETERMINATE_PERMIT)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should parse correct indeterminateP policy with options") {
          val given =
              """#indP(#opts(lenientConstraints,ignoreErrors,priority=10,actionExecutionStrategy=untilSuccess))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.INDETERMINATE_PERMIT,
                  ignoreErrors = true,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS,
                  lenientConstraints = true,
                  priority = 10)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
      }
      describe("constraint") {
        it("no constraint") {
          val given = """#permit()"""
          val expected = PolicyDefault(PolicyResultEnum.PERMIT)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has constraint") {
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
      }
      describe("actions") {
        it("no action") {
          val given = """#permit()"""
          val expected = PolicyDefault(PolicyResultEnum.PERMIT)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has action") {
          val given = """#permit(*save(foo,#ref(pvd1)))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
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
          val given = """#permit(*act(#ref(pas1)))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actions = listOf(PolicyActionRelationship(PolicyActionRef("pas1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
      }
    })
