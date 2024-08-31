package com.github.ivsokol.poe.policy

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.PolicyEntityEnum
import com.github.ivsokol.poe.action.PolicyActionSave
import com.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import com.github.ivsokol.poe.condition.PolicyConditionDefault
import com.github.ivsokol.poe.event.InMemoryEventTestHandler
import com.github.ivsokol.poe.variable.PolicyVariableStatic
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PolicySetPermitOverridesTest :
    DescribeSpec({
      val context = Context(event = InMemoryEventTestHandler())

      val combinationLogic = PolicyCombinationLogicEnum.PERMIT_OVERRIDES

      val catalog = EmptyPolicyCatalog()

      afterTest {
        (context.event as InMemoryEventTestHandler).clear()
        context.removeLastFromPath()
        context.cache.clear()
        context.dataStore().clear()
      }

      describe("constraint") {
        it("constraintTester true") {
          val given =
              PolicySet(
                  policyCombinationLogic = combinationLogic,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.DENY),
                          ),
                          PolicyRelationship(
                              constraint = PolicyConditionDefault(true),
                              policy = PolicyDefault(PolicyResultEnum.PERMIT),
                          ),
                      ),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
          val allEvents = context.event.list()
          allEvents shouldHaveSize 4
          allEvents[0].entityId shouldBe "policies/0(${'$'}deny)"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "policies/1/constraint(${'$'}true)"
          allEvents[1].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT

          allEvents[2].entityId shouldBe "policies/1(${'$'}permit)"
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[3].entityId shouldBe ""
          allEvents[3].entity shouldBe PolicyEntityEnum.POLICY_SET
          allEvents[3].message shouldBe "permit"
        }

        it("constraintTester false") {
          val given =
              PolicySet(
                  policyCombinationLogic = combinationLogic,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.DENY),
                          ),
                          PolicyRelationship(
                              constraint = PolicyConditionDefault(false),
                              policy = PolicyDefault(PolicyResultEnum.PERMIT),
                          )),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.DENY
          val allEvents = context.event.list()
          allEvents shouldHaveSize 3
          allEvents[0].entityId shouldBe "policies/0(${'$'}deny)"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "policies/1/constraint(${'$'}false)"
          allEvents[1].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT

          allEvents[2].entityId shouldBe ""
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_SET
          allEvents[2].message shouldBe "deny"
        }

        it("constraintTester null") {
          val given =
              PolicySet(
                  policyCombinationLogic = combinationLogic,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.DENY),
                          ),
                          PolicyRelationship(
                              constraint = PolicyConditionDefault(null),
                              policy = PolicyDefault(PolicyResultEnum.PERMIT),
                          )),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.DENY
          val allEvents = context.event.list()
          allEvents shouldHaveSize 3
          allEvents[0].entityId shouldBe "policies/0(${'$'}deny)"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "policies/1/constraint(${'$'}null)"
          allEvents[1].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT

          allEvents[2].entityId shouldBe ""
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_SET
          allEvents[2].message shouldBe "deny"
        }

        it("constraintTester null strict") {
          val given =
              PolicySet(
                  policyCombinationLogic = combinationLogic,
                  lenientConstraints = false,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.DENY),
                          ),
                          PolicyRelationship(
                              constraint = PolicyConditionDefault(null),
                              policy = PolicyDefault(PolicyResultEnum.PERMIT),
                          )),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.INDETERMINATE_DENY_PERMIT
          val allEvents = context.event.list()
          allEvents shouldHaveSize 3
          allEvents[0].entityId shouldBe "policies/0(${'$'}deny)"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "policies/1/constraint(${'$'}null)"
          allEvents[1].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT

          allEvents[2].entityId shouldBe ""
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_SET
          allEvents[2].message shouldBe "indeterminate"
        }
      }

      describe("null policy ref") {
        it("null ref") {
          val given =
              PolicySet(
                  policyCombinationLogic = combinationLogic,
                  lenientConstraints = false,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol999"),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.DENY),
                          )),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.INDETERMINATE_DENY_PERMIT
          val allEvents = context.event.list()
          allEvents shouldHaveSize 3
          allEvents[0].entityId shouldBe ""
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_SET
          allEvents[0].reason shouldContain "not found in catalog"

          allEvents[1].entityId shouldBe ""
          allEvents[1].entity shouldBe PolicyEntityEnum.POLICY_SET
          allEvents[1].reason shouldContain "not found in catalog"

          allEvents[2].entityId shouldBe ""
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_SET
          allEvents[2].message shouldBe "indeterminate"
        }
      }

      describe("run child actions") {
        it("parent false") {
          val given =
              PolicySet(
                  runChildActions = false,
                  policyCombinationLogic = combinationLogic,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy =
                                  PolicyDefault(
                                      PolicyResultEnum.DENY,
                                      actions =
                                          listOf(
                                              PolicyActionRelationship(
                                                  action =
                                                      PolicyActionSave(
                                                          key = "foo",
                                                          value =
                                                              PolicyVariableStatic(
                                                                  value = "bar"))))),
                          )),
              )
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.DENY
          context.dataStore().containsKey("foo") shouldBe false
          val allEvents = context.event.list()
          allEvents shouldHaveSize 2
          allEvents[0].entityId shouldBe "policies/0(${'$'}deny)"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe ""
          allEvents[1].entity shouldBe PolicyEntityEnum.POLICY_SET
          allEvents[1].message shouldBe "deny"
        }
        it("parent true") {
          val given =
              PolicySet(
                  runChildActions = true,
                  policyCombinationLogic = combinationLogic,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy =
                                  PolicyDefault(
                                      PolicyResultEnum.DENY,
                                      actions =
                                          listOf(
                                              PolicyActionRelationship(
                                                  action =
                                                      PolicyActionSave(
                                                          key = "foo",
                                                          value =
                                                              PolicyVariableStatic(
                                                                  value = "bar"))))),
                          )),
              )
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.DENY
          context.dataStore().containsKey("foo") shouldBe true
          context.dataStore()["foo"] shouldBe "bar"
          val allEvents = context.event.list()
          allEvents shouldHaveSize 5
          allEvents[0].entityId shouldBe "policies/0(${'$'}deny)"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe "policies/0(${'$'}deny)/actions/0/source"
          allEvents[1].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          allEvents[2].entityId shouldBe "policies/0(${'$'}deny)/actions/0"
          allEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE

          allEvents[3].entityId shouldBe "policies/0(${'$'}deny)"
          allEvents[3].entity shouldBe PolicyEntityEnum.POLICY_ACTION

          allEvents[4].entityId shouldBe ""
          allEvents[4].entity shouldBe PolicyEntityEnum.POLICY_SET
          allEvents[4].message shouldBe "deny"
        }
        it("parent true, child false") {
          val given =
              PolicySet(
                  runChildActions = true,
                  policyCombinationLogic = combinationLogic,
                  policies =
                      listOf(
                          PolicyRelationship(
                              runAction = false,
                              policy =
                                  PolicyDefault(
                                      PolicyResultEnum.DENY,
                                      actions =
                                          listOf(
                                              PolicyActionRelationship(
                                                  action =
                                                      PolicyActionSave(
                                                          key = "foo",
                                                          value =
                                                              PolicyVariableStatic(
                                                                  value = "bar"))))),
                          )),
              )
          context.dataStore().containsKey("foo") shouldBe false
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.DENY
          context.dataStore().containsKey("foo") shouldBe false
          val allEvents = context.event.list()
          allEvents shouldHaveSize 2
          allEvents[0].entityId shouldBe "policies/0(${'$'}deny)"
          allEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT

          allEvents[1].entityId shouldBe ""
          allEvents[1].entity shouldBe PolicyEntityEnum.POLICY_SET
          allEvents[1].message shouldBe "deny"
        }
      }

      describe("result handling logic") {
        it("has permit") {
          val given =
              PolicySet(
                  policyCombinationLogic = combinationLogic,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.DENY),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY_PERMIT),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.INDETERMINATE_PERMIT),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.NOT_APPLICABLE),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.PERMIT),
                          )),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
        }
        it("has indeterminateDP") {
          val given =
              PolicySet(
                  policyCombinationLogic = combinationLogic,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.DENY),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.INDETERMINATE_PERMIT),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.NOT_APPLICABLE),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY_PERMIT),
                          ),
                      ),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.INDETERMINATE_DENY_PERMIT
        }
        it("hasIndeterminateP && hasIndeterminateD") {
          val given =
              PolicySet(
                  policyCombinationLogic = combinationLogic,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.INDETERMINATE_PERMIT),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.NOT_APPLICABLE),
                          ),
                      ),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.INDETERMINATE_DENY_PERMIT
        }
        it("hasIndeterminateP && hasDeny") {
          val given =
              PolicySet(
                  policyCombinationLogic = combinationLogic,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.DENY),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.INDETERMINATE_PERMIT),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.NOT_APPLICABLE),
                          ),
                      ),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.INDETERMINATE_DENY_PERMIT
        }
        it("hasIndeterminateP") {
          val given =
              PolicySet(
                  policyCombinationLogic = combinationLogic,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.INDETERMINATE_PERMIT),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.NOT_APPLICABLE),
                          ),
                      ),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.INDETERMINATE_PERMIT
        }
        it("hasDeny") {
          val given =
              PolicySet(
                  policyCombinationLogic = combinationLogic,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.DENY),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.NOT_APPLICABLE),
                          ),
                      ),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.DENY
        }
        it("hasIndeterminateD") {
          val given =
              PolicySet(
                  policyCombinationLogic = combinationLogic,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.NOT_APPLICABLE),
                          ),
                      ),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.INDETERMINATE_DENY
        }
        it("action fail lenient") {
          val given =
              PolicySet(
                  runChildActions = true,
                  policyCombinationLogic = combinationLogic,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy =
                                  PolicyDefault(
                                      PolicyResultEnum.DENY,
                                      actions =
                                          listOf(
                                              PolicyActionRelationship(
                                                  action =
                                                      PolicyActionSave(
                                                          key = "none",
                                                          value =
                                                              PolicyVariableStatic(value = "none"),
                                                          failOnMissingKey = true)))),
                          ),
                      ),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.DENY
        }
        it("action fail strict") {
          val given =
              PolicySet(
                  runChildActions = true,
                  indeterminateOnActionFail = true,
                  policyCombinationLogic = combinationLogic,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy =
                                  PolicyDefault(
                                      PolicyResultEnum.DENY,
                                      ignoreErrors = false,
                                      actions =
                                          listOf(
                                              PolicyActionRelationship(
                                                  action =
                                                      PolicyActionSave(
                                                          key = "none",
                                                          value =
                                                              PolicyVariableStatic(value = "none"),
                                                          failOnMissingKey = true)))),
                          ),
                      ),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.INDETERMINATE_DENY_PERMIT
        }
        it("not applicable") {
          val given =
              PolicySet(
                  policyCombinationLogic = combinationLogic,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.NOT_APPLICABLE),
                          ),
                      ),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.NOT_APPLICABLE
        }
      }
    })
