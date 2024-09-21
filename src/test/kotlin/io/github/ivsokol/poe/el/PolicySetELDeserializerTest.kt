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

class PolicySetELDeserializerTest :
    DescribeSpec({
      describe("basic parsing") {
        it("should parse correct policy set") {
          val given = """*DOverrides(#ref(pol1))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }

        it("should throw exception on no close command") {
          val given = """*DOverrides(#ref(pol1)"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }
          actual.message shouldContain "Command not completed on position 0"
        }

        it("should throw exception on content") {
          val given = """*DOverrides(content)"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }
          actual.message shouldContain "PolicySetELDeserializer can not have contents"
        }

        it("should throw exception on bad child command") {
          val given = """*DOverrides(*dyn(*key(foo)))"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }
          actual.message shouldContain
              "Child command type mismatch on position 12 for command '*DOverrides'. Expected: 'POLICY, POLICY_SET, POLICY_DEFAULT, REFERENCE, POLICY_RELATIONSHIP, ACTION_RELATIONSHIP, POLICY_ACTION', actual: 'VARIABLE_DYNAMIC'"
        }

        it("should throw exception on bad command start ") {
          val given = """*DOverrides{("""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parsePolicy() }
          actual.message shouldContain "Expected command start after command on position 11"
        }
      }
      describe("variations") {
        it("deny overrides minimal") {
          val given = """*DOverrides(#ref(pol1))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("deny overrides with options") {
          val given =
              """*DOverrides(#ref(pol1),#opts(id=pol,ver=1.2.3,desc="some desc",labels=a,lenientConstraints,actionExecutionStrategy=untilSuccess,ignoreErrors,priority=10,skipCache,runChildActions,indeterminateOnActionFail))"""
          val expected =
              PolicySet(
                  id = "pol",
                  version = SemVer(1, 2, 3),
                  description = "some desc",
                  labels = listOf("a"),
                  lenientConstraints = true,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS,
                  ignoreErrors = true,
                  priority = 10,
                  skipCache = true,
                  runChildActions = true,
                  indeterminateOnActionFail = true,
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("permit overrides minimal") {
          val given = """*POverrides(#ref(pol1))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_OVERRIDES,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("permit overrides with options") {
          val given =
              """*POverrides(#ref(pol1),#opts(id=pol,ver=1.2.3,desc="some desc",labels=a,lenientConstraints,actionExecutionStrategy=untilSuccess,ignoreErrors,priority=10,skipCache,runChildActions,indeterminateOnActionFail))"""
          val expected =
              PolicySet(
                  id = "pol",
                  version = SemVer(1, 2, 3),
                  description = "some desc",
                  labels = listOf("a"),
                  lenientConstraints = true,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS,
                  ignoreErrors = true,
                  priority = 10,
                  skipCache = true,
                  runChildActions = true,
                  indeterminateOnActionFail = true,
                  policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_OVERRIDES,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("deny unless permit minimal") {
          val given = """*DUnlessP(#ref(pol1))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_UNLESS_PERMIT,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("deny unless permit with options") {
          val given =
              """*DUnlessP(#ref(pol1),#opts(id=pol,ver=1.2.3,desc="some desc",labels=a,lenientConstraints,actionExecutionStrategy=untilSuccess,ignoreErrors,priority=10,skipCache,runChildActions,indeterminateOnActionFail,strictUnlessLogic))"""
          val expected =
              PolicySet(
                  id = "pol",
                  version = SemVer(1, 2, 3),
                  description = "some desc",
                  labels = listOf("a"),
                  lenientConstraints = true,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS,
                  ignoreErrors = true,
                  priority = 10,
                  skipCache = true,
                  runChildActions = true,
                  indeterminateOnActionFail = true,
                  strictUnlessLogic = true,
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_UNLESS_PERMIT,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("permit unless deny minimal") {
          val given = """*PUnlessD(#ref(pol1))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("permit unless deny with options") {
          val given =
              """*PUnlessD(#ref(pol1),#opts(id=pol,ver=1.2.3,desc="some desc",labels=a,lenientConstraints,actionExecutionStrategy=untilSuccess,ignoreErrors,priority=10,skipCache,runChildActions,indeterminateOnActionFail,strictUnlessLogic))"""
          val expected =
              PolicySet(
                  id = "pol",
                  version = SemVer(1, 2, 3),
                  description = "some desc",
                  labels = listOf("a"),
                  lenientConstraints = true,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS,
                  ignoreErrors = true,
                  priority = 10,
                  skipCache = true,
                  runChildActions = true,
                  indeterminateOnActionFail = true,
                  strictUnlessLogic = true,
                  policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("first applicable minimal") {
          val given = """*firstAppl(#ref(pol1))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("first applicable  with options") {
          val given =
              """*firstAppl(#ref(pol1),#opts(id=pol,ver=1.2.3,desc="some desc",labels=a,lenientConstraints,actionExecutionStrategy=untilSuccess,ignoreErrors,priority=10,skipCache,runChildActions,indeterminateOnActionFail))"""
          val expected =
              PolicySet(
                  id = "pol",
                  version = SemVer(1, 2, 3),
                  description = "some desc",
                  labels = listOf("a"),
                  lenientConstraints = true,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS,
                  ignoreErrors = true,
                  priority = 10,
                  skipCache = true,
                  runChildActions = true,
                  indeterminateOnActionFail = true,
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
      }
      describe("constraint") {
        it("no constraint") {
          val given = """*DOverrides(#ref(pol1))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has constraint") {
          val given = """*DOverrides(#ref(pol1),*constraint(*gt(#int(1), #int(2))))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))),
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
          val given = """*DOverrides(#ref(pol1))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has action") {
          val given = """*DOverrides(#ref(pol1),*save(foo,#ref(pvd1)))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))),
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
          val given = """*DOverrides(#ref(pol1),*act(#ref(pas1)))"""
          val expected =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies = listOf(PolicyRelationship(PolicyRef("pol1"))),
                  actions = listOf(PolicyActionRelationship(PolicyActionRef("pas1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
      }
    })
