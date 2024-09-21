package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.action.*
import io.github.ivsokol.poe.condition.OperationEnum
import io.github.ivsokol.poe.condition.PolicyConditionAtomic
import io.github.ivsokol.poe.policy.ActionExecutionModeEnum
import io.github.ivsokol.poe.policy.PolicyActionRelationship
import io.github.ivsokol.poe.policy.PolicyDefault
import io.github.ivsokol.poe.policy.PolicyResultEnum
import io.github.ivsokol.poe.variable.PolicyVariableRef
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class PolicyActionRelationshipELDeserializerTest :
    DescribeSpec({
      describe("action relationship") {
        it("no actions") {
          val given = """#permit()"""
          val expected = PolicyDefault(PolicyResultEnum.PERMIT)
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has action save") {
          val given = """#permit(*save(foo,#str(bar)))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              PolicyActionSave(
                                  key = "foo",
                                  value =
                                      PolicyVariableStatic(
                                          value = "bar", type = VariableValueTypeEnum.STRING)))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has action clear") {
          val given = """#permit(*clear(foo))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actions = listOf(PolicyActionRelationship(PolicyActionClear(key = "foo"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has action patch") {
          val given = """#permit(*patch(foo,#ref(pvd1),#ref(pvd2)))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              PolicyActionJsonPatch(
                                  key = "foo",
                                  source = PolicyVariableRef(id = "pvd1"),
                                  patch = PolicyVariableRef(id = "pvd2"),
                              ))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has action merge") {
          val given = """#permit(*merge(foo,#ref(pvd1),#ref(pvd2)))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              PolicyActionJsonMerge(
                                  key = "foo",
                                  source = PolicyVariableRef(id = "pvd1"),
                                  merge = PolicyVariableRef(id = "pvd2"),
                              ))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has action ref") {
          val given = """#permit(*act(#ref(pas1)))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actions = listOf(PolicyActionRelationship(PolicyActionRef("pas1"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has multiple actions") {
          val given = """#permit(*save(foo,#str(bar)),*clear(foo2))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              PolicyActionSave(
                                  key = "foo",
                                  value =
                                      PolicyVariableStatic(
                                          value = "bar", type = VariableValueTypeEnum.STRING))),
                          PolicyActionRelationship(PolicyActionClear(key = "foo2"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has multiple action relationships") {
          val given = """#permit(*act(#ref(pas1)),*act(#ref(pas2)))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actions =
                      listOf(
                          PolicyActionRelationship(PolicyActionRef("pas1")),
                          PolicyActionRelationship(PolicyActionRef("pas2"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has action relationship with options") {
          val given =
              """#permit(*act(#ref(pas1),#opts(executionMode=onDeny|onPermit,priority=10)))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              PolicyActionRef("pas1"),
                              executionMode =
                                  setOf(
                                      ActionExecutionModeEnum.ON_DENY,
                                      ActionExecutionModeEnum.ON_PERMIT),
                              priority = 10)))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("has action relationship with constraint") {
          val given = """#permit(*act(#ref(pas1),*constraint(*gt(#int(1), #int(2)))))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              PolicyActionRef("pas1"),
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
        it("has action and action relationship") {
          val given = """#permit(*merge(foo,#ref(pvd1),#ref(pvd2)),*act(#ref(pas2)))"""
          val expected =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              PolicyActionJsonMerge(
                                  key = "foo",
                                  source = PolicyVariableRef(id = "pvd1"),
                                  merge = PolicyVariableRef(id = "pvd2"),
                              )),
                          PolicyActionRelationship(PolicyActionRef("pas2"))))
          val actual = PEELParser(given).parsePolicy()
          actual shouldBe expected
        }
        it("should throw on bad relationship class ") {
          val given = """#permit(*act(#int(1))))"""
          shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }.message shouldBe
              "Child command type mismatch on position 13 for command '*act'. Expected: 'POLICY_ACTION_SAVE, POLICY_ACTION_CLEAR, POLICY_ACTION_JSON_MERGE, POLICY_ACTION_JSON_PATCH, REFERENCE', actual: 'VARIABLE_STATIC'"
        }
        it("should throw on relationship content") {
          val given = """#permit(*act(content))"""
          shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }.message shouldBe
              "PolicyActionRelationshipELDeserializer can not have contents"
        }
        it("should throw on multiple actions") {
          val given = """#permit(*act(#ref(pas2),#ref(pas3)))"""
          shouldThrow<IllegalStateException> { PEELParser(given).parsePolicy() }.message shouldBe
              "Too many arguments on position 8 for command '*act'. Expected: 1, actual: 2"
        }
      }
    })
