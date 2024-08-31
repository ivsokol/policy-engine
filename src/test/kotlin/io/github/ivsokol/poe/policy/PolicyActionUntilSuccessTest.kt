package io.github.ivsokol.poe.policy

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.action.PolicyActionRef
import io.github.ivsokol.poe.action.PolicyActionSave
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.condition.PolicyConditionDefault
import io.github.ivsokol.poe.event.InMemoryEventTestHandler
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PolicyActionUntilSuccessTest :
    DescribeSpec({
      val context = Context(event = InMemoryEventTestHandler())

      val catalog = EmptyPolicyCatalog()
      val strategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS

      afterTest {
        (context.event as InMemoryEventTestHandler).clear()
        context.removeLastFromPath()
        context.cache.clear()
        context.dataStore().clear()
      }

      describe("untilSuccess strategy") {
        it("run all actions, only one pass") {
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
          context.dataStore()["foo"] shouldBe "bar4"
          val allEvents = context.event.list()
          allEvents shouldHaveSize 8
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

          allEvents[7].entityId shouldBe "${'$'}permit"
          allEvents[7].entity shouldBe PolicyEntityEnum.POLICY_ACTION
          allEvents[7].success shouldBe true
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
          allEvents[3].success shouldBe true
        }

        it("run 3 actions, first fail") {
          val given =
              PolicyDefault(
                  PolicyResultEnum.PERMIT,
                  actionExecutionStrategy = strategy,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              action =
                                  PolicyActionSave(
                                      key = "foo",
                                      value = PolicyVariableStatic(value = "bar"),
                                      failOnMissingKey = true)),
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
          context.dataStore()["foo"] shouldBe "bar2"
          val allEvents = context.event.list()
          allEvents shouldHaveSize 5
          allEvents[0].entityId shouldBe "${'$'}permit"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "${'$'}permit/actions/0"
          allEvents[1].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE
          allEvents[1].success shouldBe false

          allEvents[2].entityId shouldBe "${'$'}permit/actions/1/source"
          allEvents[2].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[3].entityId shouldBe "${'$'}permit/actions/1"
          allEvents[3].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[4].entityId shouldBe "${'$'}permit"
          allEvents[4].entity shouldBe PolicyEntityEnum.POLICY_ACTION
          allEvents[4].success shouldBe true
        }

        it("run actions, all fail") {
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
          allEvents[5].success shouldBe false
        }
      }
    })
