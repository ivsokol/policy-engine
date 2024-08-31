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

class PolicyActionStopOnFailureTest :
    DescribeSpec({
      val context = Context(event = InMemoryEventTestHandler())

      val catalog = EmptyPolicyCatalog()
      val strategy = ActionExecutionStrategyEnum.STOP_ON_FAILURE

      afterTest {
        (context.event as InMemoryEventTestHandler).clear()
        context.removeLastFromPath()
        context.cache.clear()
        context.dataStore().clear()
      }

      describe("stopOnFailure strategy") {
        it("run all actions until null ref") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = strategy,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar4"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "bar5"),
                                      failOnMissingKey = true)),
                          PolicyActionRelationship(
                              executionMode = setOf(ActionExecutionModeEnum.ON_DENY),
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "bar3"),
                                      failOnExistingKey = true)),
                          PolicyActionRelationship(
                              constraint = PolicyConditionDefault(false),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar"))),
                          PolicyActionRelationship(action = PolicyActionRef("1.0.0")),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar2")),
                          )))
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "bar5"
          val allEvents = context.event.list()
          allEvents shouldHaveSize 9
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

          allEvents[5].entityId shouldBe "${'$'}permit/actions/3/constraint(${'$'}false)"
          allEvents[5].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT

          allEvents[6].entityId shouldBe "${'$'}permit"
          allEvents[6].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT
          allEvents[6].success shouldBe false

          allEvents[7].entityId shouldBe "${'$'}permit/actions/4"
          allEvents[7].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT
          allEvents[7].success shouldBe false
          allEvents[7].reason shouldContain "not found in catalog"

          allEvents[8].entityId shouldBe "${'$'}permit"
          allEvents[8].entity shouldBe PolicyEntityEnum.POLICY_ACTION
          allEvents[8].success shouldBe false
        }

        it("run all actions until action fail") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = strategy,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar4"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "bar5"),
                                      failOnMissingKey = true)),
                          PolicyActionRelationship(
                              executionMode = setOf(ActionExecutionModeEnum.ON_DENY),
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "bar3"),
                                      failOnExistingKey = true)),
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
                                      failOnExistingKey = true),
                          ),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar6"))),
                      ))
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "bar5"
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

          allEvents[5].entityId shouldBe "${'$'}permit/actions/3/constraint(${'$'}false)"
          allEvents[5].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT

          allEvents[6].entityId shouldBe "${'$'}permit/actions/4"
          allEvents[6].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE
          allEvents[6].success shouldBe false
          allEvents[6].reason shouldContain "Existing key"

          allEvents[7].entityId shouldBe "${'$'}permit"
          allEvents[7].entity shouldBe PolicyEntityEnum.POLICY_ACTION
          allEvents[7].success shouldBe false
        }

        it("run all actions until strict constraint") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = strategy,
                  lenientConstraints = false,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar4"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "bar5"),
                                      failOnMissingKey = true)),
                          PolicyActionRelationship(
                              executionMode = setOf(ActionExecutionModeEnum.ON_DENY),
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "bar3"),
                                      failOnExistingKey = true)),
                          PolicyActionRelationship(
                              constraint = PolicyConditionDefault(null),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar6"))),
                      ))
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "bar5"
          val allEvents = context.event.list()
          allEvents shouldHaveSize 7
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

          allEvents[5].entityId shouldBe "${'$'}permit/actions/3/constraint(${'$'}null)"
          allEvents[5].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT

          allEvents[6].entityId shouldBe "${'$'}permit"
          allEvents[6].entity shouldBe PolicyEntityEnum.POLICY_ACTION
          allEvents[6].success shouldBe false
        }

        it("no valid actions") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = strategy,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              executionMode = setOf(ActionExecutionModeEnum.ON_DENY),
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "bar3"),
                                      failOnExistingKey = true)),
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
                                      failOnMissingKey = true),
                          ),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar6"))),
                      ))
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe false
          val allEvents = context.event.list()
          allEvents shouldHaveSize 4
          allEvents[0].entityId shouldBe "${'$'}permit"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "${'$'}permit/actions/1/constraint(${'$'}false)"
          allEvents[1].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT

          allEvents[2].entityId shouldBe "${'$'}permit/actions/2"
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE
          allEvents[2].success shouldBe false
          allEvents[2].reason shouldContain "Missing key"

          allEvents[3].entityId shouldBe "${'$'}permit"
          allEvents[3].entity shouldBe PolicyEntityEnum.POLICY_ACTION
          allEvents[3].success shouldBe false
        }

        it("only valid actions") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = strategy,
                  lenientConstraints = false,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar4"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "bar5"),
                                      failOnMissingKey = true)),
                          PolicyActionRelationship(
                              executionMode = setOf(ActionExecutionModeEnum.ON_DENY),
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "bar3"),
                                      failOnExistingKey = true)),
                          PolicyActionRelationship(
                              constraint = PolicyConditionDefault(false),
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar"))),
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo", value = PolicyVariableStatic(value = "bar6"))),
                      ))
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          given.runActions(context, catalog, actual)
          actual shouldBe PolicyResultEnum.PERMIT
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "bar6"
          val allEvents = context.event.list()
          allEvents shouldHaveSize 9
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

          allEvents[5].entityId shouldBe "${'$'}permit/actions/3/constraint(${'$'}false)"
          allEvents[5].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT

          allEvents[6].entityId shouldBe "${'$'}permit/actions/4/source"
          allEvents[6].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[7].entityId shouldBe "${'$'}permit/actions/4"
          allEvents[7].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[8].entityId shouldBe "${'$'}permit"
          allEvents[8].entity shouldBe PolicyEntityEnum.POLICY_ACTION
          allEvents[8].success shouldBe true
        }
      }
    })
