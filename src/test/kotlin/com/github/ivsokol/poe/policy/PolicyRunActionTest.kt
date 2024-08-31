package com.github.ivsokol.poe.policy

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.PolicyEntityEnum
import com.github.ivsokol.poe.SemVer
import com.github.ivsokol.poe.action.PolicyActionSave
import com.github.ivsokol.poe.catalog.PolicyCatalog
import com.github.ivsokol.poe.condition.PolicyConditionDefault
import com.github.ivsokol.poe.event.InMemoryEventTestHandler
import com.github.ivsokol.poe.variable.PolicyVariableStatic
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class PolicyRunActionTest :
    DescribeSpec({
      val context = Context(event = InMemoryEventTestHandler())

      val catalog =
          PolicyCatalog(
              id = "test-catalog",
              policies =
                  listOf(
                      Policy(
                          id = "pol1",
                          targetEffect = PolicyTargetEffectEnum.PERMIT,
                          condition = PolicyConditionDefault(true)),
                      Policy(
                          id = "pol2",
                          version = SemVer(1, 0, 0),
                          targetEffect = PolicyTargetEffectEnum.DENY,
                          condition = PolicyConditionDefault(true)),
                      Policy(
                          id = "pol2",
                          version = SemVer(2, 0, 0),
                          targetEffect = PolicyTargetEffectEnum.PERMIT,
                          condition = PolicyConditionDefault(true)),
                  ))

      afterTest {
        (context.event as InMemoryEventTestHandler).clear()
        context.removeLastFromPath()
        context.cache.clear()
        context.dataStore().clear()
      }

      describe("naming") {
        it("policy default") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar")))))
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "bar"
          val allEvents = context.event.list()
          allEvents shouldHaveSize 4
          allEvents[0].entityId shouldBe "${'$'}permit"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "${'$'}permit/actions/0/source"
          allEvents[1].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[2].entityId shouldBe "${'$'}permit/actions/0"
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[3].entityId shouldBe "${'$'}permit"
          allEvents[3].entity shouldBe PolicyEntityEnum.POLICY_ACTION
        }
        it("unnamed policy") {
          val given =
              Policy(
                  condition = PolicyConditionDefault(true),
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar2")))))

          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "bar2"
          val allEvents = context.event.list()
          allEvents shouldHaveSize 5
          allEvents[0].entityId shouldBe "condition(${'$'}true)"
          allEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT

          allEvents[1].entityId shouldBe ""
          allEvents[1].entity shouldBe PolicyEntityEnum.POLICY

          allEvents[2].entityId shouldBe "actions/0/source"
          allEvents[2].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[3].entityId shouldBe "actions/0"
          allEvents[3].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[4].entityId shouldBe ""
          allEvents[4].entity shouldBe PolicyEntityEnum.POLICY_ACTION
        }
        it("named policy") {
          val given =
              Policy(
                  id = "pol1",
                  condition = PolicyConditionDefault(true),
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar2")))))

          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "bar2"
          val allEvents = context.event.list()
          allEvents shouldHaveSize 5
          allEvents[0].entityId shouldBe "pol1/condition(${'$'}true)"
          allEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT

          allEvents[1].entityId shouldBe "pol1"
          allEvents[1].entity shouldBe PolicyEntityEnum.POLICY

          allEvents[2].entityId shouldBe "pol1/actions/0/source"
          allEvents[2].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[3].entityId shouldBe "pol1/actions/0"
          allEvents[3].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[4].entityId shouldBe "pol1"
          allEvents[4].entity shouldBe PolicyEntityEnum.POLICY_ACTION
        }
        it("unnamed policy set") {
          val given =
              PolicySet(
                  runChildActions = true,
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy =
                                  PolicyDefault(
                                      PolicyResultEnum.PERMIT,
                                      actions =
                                          listOf(
                                              PolicyActionRelationship(
                                                  action =
                                                      PolicyActionSave(
                                                          key = "foo",
                                                          value =
                                                              PolicyVariableStatic(
                                                                  value = "bar"))))),
                          ),
                          PolicyRelationship(
                              policy =
                                  Policy(
                                      condition = PolicyConditionDefault(true),
                                      targetEffect = PolicyTargetEffectEnum.PERMIT,
                                      actions =
                                          listOf(
                                              PolicyActionRelationship(
                                                  action =
                                                      PolicyActionSave(
                                                          key = "foo",
                                                          value =
                                                              PolicyVariableStatic(
                                                                  value = "bar2")))))),
                          PolicyRelationship(
                              policy =
                                  PolicySet(
                                      policyCombinationLogic =
                                          PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                                      policies =
                                          listOf(
                                              PolicyRelationship(
                                                  policy = PolicyDefault(PolicyResultEnum.PERMIT))),
                                      actions =
                                          listOf(
                                              PolicyActionRelationship(
                                                  action =
                                                      PolicyActionSave(
                                                          key = "foo",
                                                          value =
                                                              PolicyVariableStatic(
                                                                  value = "bar3"))))))),
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              executionMode = setOf(ActionExecutionModeEnum.ON_PERMIT),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar4")))))
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "bar4"
          val allEvents = context.event.list()
          allEvents shouldHaveSize 18
          allEvents[0].entityId shouldBe "policies/0(${'$'}permit)"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "policies/0(${'$'}permit)/actions/0/source"
          allEvents[1].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[2].entityId shouldBe "policies/0(${'$'}permit)/actions/0"
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[3].entityId shouldBe "policies/0(${'$'}permit)"
          allEvents[3].entity shouldBe PolicyEntityEnum.POLICY_ACTION

          allEvents[4].entityId shouldBe "policies/1/condition(${'$'}true)"
          allEvents[4].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT

          allEvents[5].entityId shouldBe "policies/1"
          allEvents[5].entity shouldBe PolicyEntityEnum.POLICY

          allEvents[6].entityId shouldBe "policies/1/actions/0/source"
          allEvents[6].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[7].entityId shouldBe "policies/1/actions/0"
          allEvents[7].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[8].entityId shouldBe "policies/1"
          allEvents[8].entity shouldBe PolicyEntityEnum.POLICY_ACTION

          allEvents[9].entityId shouldBe "policies/2/policies/0(${'$'}permit)"
          allEvents[9].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[10].entityId shouldBe "policies/2"
          allEvents[10].entity shouldBe PolicyEntityEnum.POLICY_SET

          allEvents[11].entityId shouldBe "policies/2/actions/0/source"
          allEvents[11].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[12].entityId shouldBe "policies/2/actions/0"
          allEvents[12].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[13].entityId shouldBe "policies/2"
          allEvents[13].entity shouldBe PolicyEntityEnum.POLICY_ACTION

          allEvents[14].entityId shouldBe ""
          allEvents[14].entity shouldBe PolicyEntityEnum.POLICY_SET

          allEvents[15].entityId shouldBe "actions/0/source"
          allEvents[15].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[16].entityId shouldBe "actions/0"
          allEvents[16].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[17].entityId shouldBe ""
          allEvents[17].entity shouldBe PolicyEntityEnum.POLICY_ACTION
        }

        it("named policy set") {
          val given =
              PolicySet(
                  id = "root1",
                  runChildActions = true,
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy =
                                  PolicyDefault(
                                      PolicyResultEnum.PERMIT,
                                      actions =
                                          listOf(
                                              PolicyActionRelationship(
                                                  action =
                                                      PolicyActionSave(
                                                          key = "foo",
                                                          value =
                                                              PolicyVariableStatic(
                                                                  value = "bar"))))),
                          ),
                          PolicyRelationship(
                              policy =
                                  Policy(
                                      id = "pol1",
                                      condition = PolicyConditionDefault(true),
                                      targetEffect = PolicyTargetEffectEnum.PERMIT,
                                      actions =
                                          listOf(
                                              PolicyActionRelationship(
                                                  action =
                                                      PolicyActionSave(
                                                          key = "foo",
                                                          value =
                                                              PolicyVariableStatic(
                                                                  value = "bar2")))))),
                          PolicyRelationship(
                              policy =
                                  PolicySet(
                                      id = "pset1",
                                      policyCombinationLogic =
                                          PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                                      policies =
                                          listOf(
                                              PolicyRelationship(
                                                  policy = PolicyDefault(PolicyResultEnum.PERMIT))),
                                      actions =
                                          listOf(
                                              PolicyActionRelationship(
                                                  action =
                                                      PolicyActionSave(
                                                          key = "foo",
                                                          value =
                                                              PolicyVariableStatic(
                                                                  value = "bar3"))))))),
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              executionMode = setOf(ActionExecutionModeEnum.ON_PERMIT),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar4")))))
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "bar4"
          val allEvents = context.event.list()
          allEvents shouldHaveSize 18
          allEvents[0].entityId shouldBe "root1/policies/0(${'$'}permit)"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "root1/policies/0(${'$'}permit)/actions/0/source"
          allEvents[1].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[2].entityId shouldBe "root1/policies/0(${'$'}permit)/actions/0"
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[3].entityId shouldBe "root1/policies/0(${'$'}permit)"
          allEvents[3].entity shouldBe PolicyEntityEnum.POLICY_ACTION

          allEvents[4].entityId shouldBe "root1/policies/1(pol1)/condition(${'$'}true)"
          allEvents[4].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT

          allEvents[5].entityId shouldBe "root1/policies/1(pol1)"
          allEvents[5].entity shouldBe PolicyEntityEnum.POLICY

          allEvents[6].entityId shouldBe "root1/policies/1(pol1)/actions/0/source"
          allEvents[6].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[7].entityId shouldBe "root1/policies/1(pol1)/actions/0"
          allEvents[7].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[8].entityId shouldBe "root1/policies/1(pol1)"
          allEvents[8].entity shouldBe PolicyEntityEnum.POLICY_ACTION

          allEvents[9].entityId shouldBe "root1/policies/2(pset1)/policies/0(${'$'}permit)"
          allEvents[9].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[10].entityId shouldBe "root1/policies/2(pset1)"
          allEvents[10].entity shouldBe PolicyEntityEnum.POLICY_SET

          allEvents[11].entityId shouldBe "root1/policies/2(pset1)/actions/0/source"
          allEvents[11].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[12].entityId shouldBe "root1/policies/2(pset1)/actions/0"
          allEvents[12].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[13].entityId shouldBe "root1/policies/2(pset1)"
          allEvents[13].entity shouldBe PolicyEntityEnum.POLICY_ACTION

          allEvents[14].entityId shouldBe "root1"
          allEvents[14].entity shouldBe PolicyEntityEnum.POLICY_SET

          allEvents[15].entityId shouldBe "root1/actions/0/source"
          allEvents[15].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[16].entityId shouldBe "root1/actions/0"
          allEvents[16].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[17].entityId shouldBe "root1"
          allEvents[17].entity shouldBe PolicyEntityEnum.POLICY_ACTION
        }
      }

      describe("constraint") {
        it("constraintTester true") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              constraint = PolicyConditionDefault(true),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar")))))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe false
          given.runActions(context, catalog, actual)
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "bar"
          val constraintEvents = context.event.list().filter { it.entityId.contains("constraint") }
          constraintEvents shouldHaveSize 1
          constraintEvents[0].contextId shouldNotBe null
          constraintEvents[0].success shouldBe true
          constraintEvents[0].entityId shouldBe "${'$'}permit/actions/0/constraint(${'$'}true)"
          constraintEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
          constraintEvents[0].reason shouldBe null
          constraintEvents[0].message shouldBe "true"
          constraintEvents[0].fromCache shouldBe false
        }

        it("constraintTester false - runAll with ignore") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              constraint = PolicyConditionDefault(false),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar")))))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe false
          given.runActions(context, catalog, actual)
          context.dataStore().containsKey("foo") shouldBe false
          val constraintEvents = context.event.list().filter { it.entityId.contains("constraint") }
          constraintEvents shouldHaveSize 1
          constraintEvents[0].contextId shouldNotBe null
          constraintEvents[0].success shouldBe true
          constraintEvents[0].entityId shouldBe "${'$'}permit/actions/0/constraint(${'$'}false)"
          constraintEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
          constraintEvents[0].reason shouldBe null
          constraintEvents[0].message shouldBe "false"
          constraintEvents[0].fromCache shouldBe false

          val policyActionEvent =
              context.event.list().filter { it.entity == PolicyEntityEnum.POLICY_ACTION }
          policyActionEvent[0].success shouldBe true
        }

        it("constraintTester false - runAll") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  ignoreErrors = false,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              constraint = PolicyConditionDefault(false),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar")))))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe false
          given.runActions(context, catalog, actual)
          context.dataStore().containsKey("foo") shouldBe false
          val constraintEvents = context.event.list().filter { it.entityId.contains("constraint") }
          constraintEvents shouldHaveSize 1
          constraintEvents[0].contextId shouldNotBe null
          constraintEvents[0].success shouldBe true
          constraintEvents[0].entityId shouldBe "${'$'}permit/actions/0/constraint(${'$'}false)"
          constraintEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
          constraintEvents[0].reason shouldBe null
          constraintEvents[0].message shouldBe "false"
          constraintEvents[0].fromCache shouldBe false

          val policyActionEvent =
              context.event.list().filter { it.entity == PolicyEntityEnum.POLICY_ACTION }
          policyActionEvent[0].success shouldBe false
        }

        it("constraintTester false - stopOnFailure") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.STOP_ON_FAILURE,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              constraint = PolicyConditionDefault(false),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar")))))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe false
          given.runActions(context, catalog, actual)
          context.dataStore().containsKey("foo") shouldBe false
          val constraintEvents = context.event.list().filter { it.entityId.contains("constraint") }
          constraintEvents shouldHaveSize 1
          constraintEvents[0].contextId shouldNotBe null
          constraintEvents[0].success shouldBe true
          constraintEvents[0].entityId shouldBe "${'$'}permit/actions/0/constraint(${'$'}false)"
          constraintEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
          constraintEvents[0].reason shouldBe null
          constraintEvents[0].message shouldBe "false"
          constraintEvents[0].fromCache shouldBe false

          val policyActionEvent =
              context.event.list().filter { it.entity == PolicyEntityEnum.POLICY_ACTION }
          policyActionEvent[0].success shouldBe true
        }

        it("constraintTester false - untilSuccess") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              constraint = PolicyConditionDefault(false),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar")))))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe false
          given.runActions(context, catalog, actual)
          context.dataStore().containsKey("foo") shouldBe false
          val constraintEvents = context.event.list().filter { it.entityId.contains("constraint") }
          constraintEvents shouldHaveSize 1
          constraintEvents[0].contextId shouldNotBe null
          constraintEvents[0].success shouldBe true
          constraintEvents[0].entityId shouldBe "${'$'}permit/actions/0/constraint(${'$'}false)"
          constraintEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
          constraintEvents[0].reason shouldBe null
          constraintEvents[0].message shouldBe "false"
          constraintEvents[0].fromCache shouldBe false

          val policyActionEvent =
              context.event.list().filter { it.entity == PolicyEntityEnum.POLICY_ACTION }
          policyActionEvent[0].success shouldBe false
        }

        it("constraintTester null lenient") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              constraint = PolicyConditionDefault(null),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar")))))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe false
          given.runActions(context, catalog, actual)
          context.dataStore().containsKey("foo") shouldBe false
          val constraintEvents = context.event.list().filter { it.entityId.contains("constraint") }
          constraintEvents shouldHaveSize 1
          constraintEvents[0].contextId shouldNotBe null
          constraintEvents[0].success shouldBe true
          constraintEvents[0].entityId shouldBe "${'$'}permit/actions/0/constraint(${'$'}null)"
          constraintEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
          constraintEvents[0].reason shouldBe null
          constraintEvents[0].message shouldBe null
          constraintEvents[0].fromCache shouldBe false

          val policyActionEvent =
              context.event.list().filter { it.entity == PolicyEntityEnum.POLICY_ACTION }
          policyActionEvent[0].success shouldBe true
        }

        it("constraintTester null strict") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.STOP_ON_FAILURE,
                  lenientConstraints = false,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              constraint = PolicyConditionDefault(null),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar")))))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe false
          given.runActions(context, catalog, actual)
          context.dataStore().containsKey("foo") shouldBe false
          val constraintEvents = context.event.list().filter { it.entityId.contains("constraint") }
          constraintEvents shouldHaveSize 1
          constraintEvents[0].contextId shouldNotBe null
          constraintEvents[0].success shouldBe true
          constraintEvents[0].entityId shouldBe "${'$'}permit/actions/0/constraint(${'$'}null)"
          constraintEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
          constraintEvents[0].reason shouldBe null
          constraintEvents[0].message shouldBe null
          constraintEvents[0].fromCache shouldBe false

          val policyActionEvent =
              context.event.list().filter { it.entity == PolicyEntityEnum.POLICY_ACTION }
          policyActionEvent[0].success shouldBe false
        }
      }

      describe("should be executed") {
        it("permit and result permit") {
          val given = setOf(ActionExecutionModeEnum.ON_PERMIT)
          given.shouldBeExecuted(PolicyResultEnum.PERMIT) shouldBe true
        }
        it("permit and result not permit") {
          val given = setOf(ActionExecutionModeEnum.ON_PERMIT)
          given.shouldBeExecuted(PolicyResultEnum.DENY) shouldBe false
        }
        it("deny and result deny") {
          val given = setOf(ActionExecutionModeEnum.ON_DENY)
          given.shouldBeExecuted(PolicyResultEnum.DENY) shouldBe true
        }
        it("deny and result not deny") {
          val given = setOf(ActionExecutionModeEnum.ON_DENY)
          given.shouldBeExecuted(PolicyResultEnum.PERMIT) shouldBe false
        }
        it("indeterminate and result indeterminateDeny") {
          val given = setOf(ActionExecutionModeEnum.ON_INDETERMINATE)
          given.shouldBeExecuted(PolicyResultEnum.INDETERMINATE_DENY) shouldBe true
        }
        it("indeterminate and result indeterminatePermit") {
          val given = setOf(ActionExecutionModeEnum.ON_INDETERMINATE)
          given.shouldBeExecuted(PolicyResultEnum.INDETERMINATE_PERMIT) shouldBe true
        }
        it("indeterminate and result indeterminateDenyPermit") {
          val given = setOf(ActionExecutionModeEnum.ON_INDETERMINATE)
          given.shouldBeExecuted(PolicyResultEnum.INDETERMINATE_DENY_PERMIT) shouldBe true
        }
        it("indeterminate and result not indeterminate") {
          val given = setOf(ActionExecutionModeEnum.ON_INDETERMINATE)
          given.shouldBeExecuted(PolicyResultEnum.NOT_APPLICABLE) shouldBe false
        }
        it("notApplicable and result notApplicable") {
          val given = setOf(ActionExecutionModeEnum.ON_NOT_APPLICABLE)
          given.shouldBeExecuted(PolicyResultEnum.NOT_APPLICABLE) shouldBe true
        }
        it("notApplicable and result not notApplicable") {
          val given = setOf(ActionExecutionModeEnum.ON_NOT_APPLICABLE)
          given.shouldBeExecuted(PolicyResultEnum.PERMIT) shouldBe false
        }
      }

      describe("priority") {
        it("priority defined") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.RUN_ALL,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              priority = 1,
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "prio1"))),
                          PolicyActionRelationship(
                              priority = 10,
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "prio10")))))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe false
          given.runActions(context, catalog, actual)
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "prio1"
          val actionSetEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.POLICY_ACTION_SAVE }
          actionSetEvents shouldHaveSize 2
          actionSetEvents[0].message shouldBe "prio10"
          actionSetEvents[1].message shouldBe "prio1"
          actionSetEvents[0].entityId shouldBe "${'$'}permit/actions/1"
          actionSetEvents[1].entityId shouldBe "${'$'}permit/actions/0"
        }

        it("priority not defined") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.RUN_ALL,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "prio1"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "prio10")))))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe false
          given.runActions(context, catalog, actual)
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "prio10"
          val actionSetEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.POLICY_ACTION_SAVE }
          actionSetEvents shouldHaveSize 2
          actionSetEvents[0].message shouldBe "prio1"
          actionSetEvents[1].message shouldBe "prio10"
          actionSetEvents[0].entityId shouldBe "${'$'}permit/actions/0"
          actionSetEvents[1].entityId shouldBe "${'$'}permit/actions/1"
        }

        it("priority mix") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.RUN_ALL,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              priority = -1,
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "prio-1"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "prioNull1"))),
                          PolicyActionRelationship(
                              priority = 10,
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "prio10"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "prioNull2"))),
                          PolicyActionRelationship(
                              priority = 10,
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "prio10-2"))),
                      ))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe false
          given.runActions(context, catalog, actual)
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "prio-1"
          val actionSetEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.POLICY_ACTION_SAVE }
          actionSetEvents shouldHaveSize 5
          actionSetEvents[0].message shouldBe "prio10"
          actionSetEvents[1].message shouldBe "prio10-2"
          actionSetEvents[2].message shouldBe "prioNull1"
          actionSetEvents[3].message shouldBe "prioNull2"
          actionSetEvents[4].message shouldBe "prio-1"
          actionSetEvents[0].entityId shouldBe "${'$'}permit/actions/2"
          actionSetEvents[1].entityId shouldBe "${'$'}permit/actions/4"
          actionSetEvents[2].entityId shouldBe "${'$'}permit/actions/1"
          actionSetEvents[3].entityId shouldBe "${'$'}permit/actions/3"
          actionSetEvents[4].entityId shouldBe "${'$'}permit/actions/0"
        }
      }
    })
