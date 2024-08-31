package com.github.ivsokol.poe.policy

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.PolicyEntityEnum
import com.github.ivsokol.poe.action.PolicyActionRef
import com.github.ivsokol.poe.action.PolicyActionSave
import com.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import com.github.ivsokol.poe.condition.PolicyConditionDefault
import com.github.ivsokol.poe.event.InMemoryEventTestHandler
import com.github.ivsokol.poe.variable.PolicyVariableStatic
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PolicyActionRunAllTest :
    DescribeSpec({
      val context = Context(event = InMemoryEventTestHandler())

      val catalog = EmptyPolicyCatalog()
      val strategy = ActionExecutionStrategyEnum.RUN_ALL

      afterTest {
        (context.event as InMemoryEventTestHandler).clear()
        context.removeLastFromPath()
        context.cache.clear()
        context.dataStore().clear()
      }

      describe("runAll strategy") {
        it("run all actions, only last 2 pass") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = strategy,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              constraint = PolicyConditionDefault(false),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "bar2"),
                                      failOnMissingKey = true)),
                          PolicyActionRelationship(action = PolicyActionRef("1.0.0")),
                          PolicyActionRelationship(
                              executionMode = setOf(ActionExecutionModeEnum.ON_DENY),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar3"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar4"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar5"))),
                      ))
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "bar5"
          val allEvents = context.event.list()
          allEvents shouldHaveSize 10
          allEvents[0].entityId shouldBe "${'$'}permit"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "${'$'}permit/actions/0/constraint(${'$'}false)"
          allEvents[1].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT

          allEvents[2].entityId shouldBe "${'$'}permit/actions/1"
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE
          allEvents[2].success shouldBe false

          allEvents[3].entityId shouldBe "${'$'}permit"
          allEvents[3].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT
          allEvents[3].success shouldBe false

          allEvents[4].entityId shouldBe "${'$'}permit/actions/2"
          allEvents[4].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT
          allEvents[4].success shouldBe false
          allEvents[4].reason shouldContain "not found in catalog"

          allEvents[5].entityId shouldBe "${'$'}permit/actions/4/source"
          allEvents[5].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[6].entityId shouldBe "${'$'}permit/actions/4"
          allEvents[6].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[7].entityId shouldBe "${'$'}permit/actions/5/source"
          allEvents[7].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[8].entityId shouldBe "${'$'}permit/actions/5"
          allEvents[8].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[9].entityId shouldBe "${'$'}permit"
          allEvents[9].entity shouldBe PolicyEntityEnum.POLICY_ACTION
          allEvents[9].success shouldBe true
        }
        it("run 3 actions") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = strategy,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar2"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar3"))),
                      ))
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "bar3"
          val allEvents = context.event.list()
          allEvents shouldHaveSize 8
          allEvents[0].entityId shouldBe "${'$'}permit"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "${'$'}permit/actions/0/source"
          allEvents[1].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[2].entityId shouldBe "${'$'}permit/actions/0"
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[3].entityId shouldBe "${'$'}permit/actions/1/source"
          allEvents[3].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[4].entityId shouldBe "${'$'}permit/actions/1"
          allEvents[4].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[5].entityId shouldBe "${'$'}permit/actions/2/source"
          allEvents[5].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[6].entityId shouldBe "${'$'}permit/actions/2"
          allEvents[6].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[7].entityId shouldBe "${'$'}permit"
          allEvents[7].entity shouldBe PolicyEntityEnum.POLICY_ACTION
          allEvents[7].success shouldBe true
        }

        it("run 3 actions, middle fail") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = strategy,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "bar2"),
                                      failOnExistingKey = true)),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar3"))),
                      ))
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "bar3"
          val allEvents = context.event.list()
          allEvents shouldHaveSize 7
          allEvents[0].entityId shouldBe "${'$'}permit"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "${'$'}permit/actions/0/source"
          allEvents[1].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[2].entityId shouldBe "${'$'}permit/actions/0"
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[3].entityId shouldBe "${'$'}permit/actions/1"
          allEvents[3].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE
          allEvents[3].success shouldBe false

          allEvents[4].entityId shouldBe "${'$'}permit/actions/2/source"
          allEvents[4].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[5].entityId shouldBe "${'$'}permit/actions/2"
          allEvents[5].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[6].entityId shouldBe "${'$'}permit"
          allEvents[6].entity shouldBe PolicyEntityEnum.POLICY_ACTION
          allEvents[6].success shouldBe true
        }

        it("run 3 actions, middle fail, ignoreErrors false") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = strategy,
                  ignoreErrors = false,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "bar2"),
                                      failOnExistingKey = true)),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar3"))),
                      ))
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "bar3"
          val allEvents = context.event.list()
          allEvents shouldHaveSize 7
          allEvents[0].entityId shouldBe "${'$'}permit"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "${'$'}permit/actions/0/source"
          allEvents[1].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[2].entityId shouldBe "${'$'}permit/actions/0"
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[3].entityId shouldBe "${'$'}permit/actions/1"
          allEvents[3].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE
          allEvents[3].success shouldBe false

          allEvents[4].entityId shouldBe "${'$'}permit/actions/2/source"
          allEvents[4].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[5].entityId shouldBe "${'$'}permit/actions/2"
          allEvents[5].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[6].entityId shouldBe "${'$'}permit"
          allEvents[6].entity shouldBe PolicyEntityEnum.POLICY_ACTION
          allEvents[3].success shouldBe false
        }

        it("run 3 actions, middle null") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = strategy,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar"))),
                          PolicyActionRelationship(action = PolicyActionRef("1.0.0")),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar3"))),
                      ))
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "bar3"
          val allEvents = context.event.list()
          allEvents shouldHaveSize 8
          allEvents[0].entityId shouldBe "${'$'}permit"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "${'$'}permit/actions/0/source"
          allEvents[1].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[2].entityId shouldBe "${'$'}permit/actions/0"
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[3].entityId shouldBe "${'$'}permit"
          allEvents[3].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT
          allEvents[3].success shouldBe false
          allEvents[3].reason shouldContain "not found in catalog"

          allEvents[4].entityId shouldBe "${'$'}permit/actions/1"
          allEvents[4].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT
          allEvents[4].success shouldBe false
          allEvents[4].reason shouldContain "not found in catalog"

          allEvents[5].entityId shouldBe "${'$'}permit/actions/2/source"
          allEvents[5].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[6].entityId shouldBe "${'$'}permit/actions/2"
          allEvents[6].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[7].entityId shouldBe "${'$'}permit"
          allEvents[7].entity shouldBe PolicyEntityEnum.POLICY_ACTION
          allEvents[7].success shouldBe true
        }

        it("run all actions, all fail, ignore errors") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = strategy,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              constraint = PolicyConditionDefault(false),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "bar2"),
                                      failOnMissingKey = true)),
                          PolicyActionRelationship(action = PolicyActionRef("1.0.0")),
                          PolicyActionRelationship(
                              executionMode = setOf(ActionExecutionModeEnum.ON_DENY),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar3"))),
                      ))
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe false
          val allEvents = context.event.list()
          allEvents shouldHaveSize 6
          allEvents[0].entityId shouldBe "${'$'}permit"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "${'$'}permit/actions/0/constraint(${'$'}false)"
          allEvents[1].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT

          allEvents[2].entityId shouldBe "${'$'}permit/actions/1"
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE
          allEvents[2].success shouldBe false

          allEvents[3].entityId shouldBe "${'$'}permit"
          allEvents[3].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT
          allEvents[3].success shouldBe false

          allEvents[4].entityId shouldBe "${'$'}permit/actions/2"
          allEvents[4].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT
          allEvents[4].success shouldBe false
          allEvents[4].reason shouldContain "not found in catalog"

          allEvents[5].entityId shouldBe "${'$'}permit"
          allEvents[5].entity shouldBe PolicyEntityEnum.POLICY_ACTION
          allEvents[5].success shouldBe true
        }

        it("run all actions, all fail") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = strategy,
                  ignoreErrors = false,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              constraint = PolicyConditionDefault(false),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "bar2"),
                                      failOnMissingKey = true)),
                          PolicyActionRelationship(action = PolicyActionRef("1.0.0")),
                          PolicyActionRelationship(
                              executionMode = setOf(ActionExecutionModeEnum.ON_DENY),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar3"))),
                      ))
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe false
          val allEvents = context.event.list()
          allEvents shouldHaveSize 6
          allEvents[0].entityId shouldBe "${'$'}permit"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "${'$'}permit/actions/0/constraint(${'$'}false)"
          allEvents[1].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT

          allEvents[2].entityId shouldBe "${'$'}permit/actions/1"
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE
          allEvents[2].success shouldBe false

          allEvents[3].entityId shouldBe "${'$'}permit"
          allEvents[3].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT
          allEvents[3].success shouldBe false

          allEvents[4].entityId shouldBe "${'$'}permit/actions/2"
          allEvents[4].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT
          allEvents[4].success shouldBe false
          allEvents[4].reason shouldContain "not found in catalog"

          allEvents[5].entityId shouldBe "${'$'}permit"
          allEvents[5].entity shouldBe PolicyEntityEnum.POLICY_ACTION
          allEvents[5].success shouldBe false
        }
      }
    })
