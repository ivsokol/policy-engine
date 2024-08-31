package com.github.ivsokol.poe.policy

import com.github.ivsokol.poe.*
import com.github.ivsokol.poe.action.PolicyActionClear
import com.github.ivsokol.poe.action.PolicyActionRef
import com.github.ivsokol.poe.catalog.PolicyCatalog
import com.github.ivsokol.poe.condition.OperationEnum
import com.github.ivsokol.poe.condition.PolicyConditionAtomic
import com.github.ivsokol.poe.condition.PolicyConditionDefault
import com.github.ivsokol.poe.condition.PolicyConditionRef
import com.github.ivsokol.poe.event.InMemoryEventTestHandler
import com.github.ivsokol.poe.variable.PolicyVariableStatic
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class PolicySetTest :
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
      }

      describe("cache") {
        it("should cache") {
          val given =
              PolicySet(
                  id = "1",
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol1"),
                          ),
                          PolicyRelationship(
                              policy = PolicyRef("pol2"),
                          )))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
          val actual2 = given.evaluate(context, catalog)
          actual2 shouldBe PolicyResultEnum.PERMIT
          context.cache.getPolicy("1") shouldBe PolicyResultEnum.PERMIT
          context.cache.getPolicy("pol1") shouldBe PolicyResultEnum.PERMIT
          context.cache.getPolicy("pol2") shouldBe null
          context.cache.getPolicy("2") shouldBe null
        }
      }

      describe("policy ref") {
        it("should find policy in catalog") {
          val given =
              PolicySet(
                  id = "1",
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol1"),
                          ),
                      ))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
        }
        it("should find latest policy in catalog") {
          val given =
              PolicySet(
                  id = "1",
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol2"),
                          ),
                      ))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
        }
        it("should find exact policy in catalog") {
          val given =
              PolicySet(
                  id = "1",
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol2", version = SemVer(1, 0, 0)),
                          ),
                      ))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.DENY
        }
        it("should not find policy in catalog") {
          val given =
              PolicySet(
                  id = "1",
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol999"),
                          ),
                      ))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.INDETERMINATE_DENY_PERMIT
        }
      }

      describe("childRefs") {
        it("should return null") {
          val given =
              PolicySet(
                  id = "1",
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.PERMIT),
                          ),
                      ))
          given.childRefs() shouldBe null
        }

        it("childRefs populated") {
          val given =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol2", version = SemVer(1, 0, 0)),
                          ),
                      ),
                  constraint = PolicyConditionRef("cond123"),
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              constraint = PolicyConditionRef("cond456"),
                              action = PolicyActionRef("act123"))))
          given.childRefs()?.shouldHaveSize(4)
          given
              .childRefs()
              ?.shouldContain(
                  PolicyEntityRefItem(PolicyEntityRefEnum.POLICY_CONDITION_REF, "cond123", null))
          given
              .childRefs()
              ?.shouldContain(
                  PolicyEntityRefItem(PolicyEntityRefEnum.POLICY_REF, "pol2", SemVer(1, 0, 0)))
          given
              .childRefs()
              ?.shouldContain(
                  PolicyEntityRefItem(PolicyEntityRefEnum.POLICY_CONDITION_REF, "cond456", null))
          given
              .childRefs()
              ?.shouldContain(
                  PolicyEntityRefItem(PolicyEntityRefEnum.POLICY_ACTION_REF, "act123", null))
        }
      }

      describe("constraint") {
        it("constraintTester true") {
          val given =
              PolicySet(
                  constraint = PolicyConditionDefault(true),
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol2"),
                          ),
                      ),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
          val constraintEvents =
              context.event.list().filter { it.entityId.startsWith("constraint") }
          constraintEvents shouldHaveSize 1
          constraintEvents[0].contextId shouldNotBe null
          constraintEvents[0].success shouldBe true
          constraintEvents[0].entityId shouldBe "constraint(${'$'}true)"
          constraintEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
          constraintEvents[0].reason shouldBe null
          constraintEvents[0].message shouldBe "true"
          constraintEvents[0].fromCache shouldBe false
        }

        it("constraintTester false") {
          val given =
              PolicySet(
                  constraint = PolicyConditionDefault(false),
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol2"),
                          ),
                      ),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.NOT_APPLICABLE
          val constraintEvents =
              context.event.list().filter { it.entityId.startsWith("constraint") }
          constraintEvents shouldHaveSize 1
          constraintEvents[0].contextId shouldNotBe null
          constraintEvents[0].success shouldBe true
          constraintEvents[0].entityId shouldBe "constraint(${'$'}false)"
          constraintEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
          constraintEvents[0].reason shouldBe null
          constraintEvents[0].message shouldBe "false"
          constraintEvents[0].fromCache shouldBe false
        }

        it("constraintTester null lenient") {
          val given =
              PolicySet(
                  constraint = PolicyConditionDefault(null),
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol2"),
                          ),
                      ),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.NOT_APPLICABLE
          val constraintEvents =
              context.event.list().filter { it.entityId.startsWith("constraint") }
          constraintEvents shouldHaveSize 1
          constraintEvents[0].contextId shouldNotBe null
          constraintEvents[0].success shouldBe true
          constraintEvents[0].entityId shouldBe "constraint(${'$'}null)"
          constraintEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
          constraintEvents[0].reason shouldBe null
          constraintEvents[0].message shouldBe null
          constraintEvents[0].fromCache shouldBe false
        }

        it("constraintTester null strict") {
          val given =
              PolicySet(
                  constraint = PolicyConditionDefault(null),
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol2"),
                          ),
                      ),
                  lenientConstraints = false)
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.INDETERMINATE_DENY_PERMIT
          val constraintEvents =
              context.event.list().filter { it.entityId.startsWith("constraint") }
          constraintEvents shouldHaveSize 1
          constraintEvents[0].contextId shouldNotBe null
          constraintEvents[0].success shouldBe true
          constraintEvents[0].entityId shouldBe "constraint(${'$'}null)"
          constraintEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
          constraintEvents[0].reason shouldBe null
          constraintEvents[0].message shouldBe null
          constraintEvents[0].fromCache shouldBe false
        }
      }

      describe("identity") {
        it("should return empty") {
          val actual =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol1"),
                          ),
                          PolicyRelationship(
                              policy = PolicyRef("pol2"),
                          )))
          actual.identity() shouldBe ""
        }
        it("should throw when bad id") {
          shouldThrow<IllegalArgumentException> {
                PolicySet(
                    id = " ",
                    policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                    policies =
                        listOf(
                            PolicyRelationship(
                                policy = PolicyRef("pol1"),
                            ),
                            PolicyRelationship(
                                policy = PolicyRef("pol2"),
                            )))
              }
              .message shouldBe "Id must not be blank"
        }
        it("should return id") {
          val actual =
              PolicySet(
                  id = "1",
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol1"),
                          ),
                          PolicyRelationship(
                              policy = PolicyRef("pol2"),
                          )))
          actual.identity() shouldBe "1"
        }
        it("should return idVer") {
          val actual =
              PolicySet(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol1"),
                          ),
                          PolicyRelationship(
                              policy = PolicyRef("pol2"),
                          )))
          actual.identity() shouldBe "1:1.0.0"
        }
      }

      describe("naming") {
        afterTest {
          (context.event as InMemoryEventTestHandler).clear()
          context.removeLastFromPath()
          context.cache.clear()
        }
        it("not named") {
          val given =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol1"),
                          ),
                          PolicyRelationship(
                              policy = PolicyRef("pol2"),
                          )))
          given.evaluate(context, catalog)
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.POLICY_SET }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe ""
        }
        it("child") {
          context.addToPath("policies")
          context.addToPath("0")
          val given =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol1"),
                          ),
                          PolicyRelationship(
                              policy = PolicyRef("pol2"),
                          )))
          given.evaluate(context, catalog)
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.POLICY_SET }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe "policies/0"
        }
        it("named child") {
          context.addToPath("policies")
          context.addToPath("0")
          val given =
              PolicySet(
                  id = "pol1",
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol1"),
                          ),
                          PolicyRelationship(
                              policy = PolicyRef("pol2"),
                          )))
          given.evaluate(context, catalog)
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.POLICY_SET }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe "policies/0(pol1)"
        }
        it("policy naming") {
          val given =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol1"),
                          ),
                          PolicyRelationship(
                              policy = PolicyRef("pol2"),
                          )))
          given.evaluate(context, catalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents[0].entityId shouldBe "policies/0(pol1)/condition(${'$'}true)"
          actualEvents[1].entityId shouldBe "policies/0(pol1)"
          actualEvents[2].entityId shouldBe ""
        }
        it("policy naming for named policy") {
          val given =
              PolicySet(
                  id = "root1",
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol1"),
                          ),
                          PolicyRelationship(
                              policy = PolicyRef("pol2"),
                          )))
          given.evaluate(context, catalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents[0].entityId shouldBe "root1/policies/0(pol1)/condition(${'$'}true)"
          actualEvents[1].entityId shouldBe "root1/policies/0(pol1)"
          actualEvents[2].entityId shouldBe "root1"
        }
        it("constraint naming") {
          val given =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol1"),
                          ),
                          PolicyRelationship(
                              policy = PolicyRef("pol2"),
                          )),
                  constraint =
                      PolicyConditionAtomic(
                          operation = OperationEnum.EQUALS,
                          args =
                              listOf(
                                  PolicyVariableStatic(value = "1"),
                                  PolicyVariableStatic(value = "1"))),
              )
          given.evaluate(context, catalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 6
          actualEvents[0].entityId shouldBe "constraint/args/0"
          actualEvents[1].entityId shouldBe "constraint/args/1"
          actualEvents[2].entityId shouldBe "constraint"
          actualEvents[3].entityId shouldBe "policies/0(pol1)/condition(${'$'}true)"
          actualEvents[4].entityId shouldBe "policies/0(pol1)"
          actualEvents[5].entityId shouldBe ""
        }
        it("action naming") {
          val given =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyRef("pol1"),
                          ),
                          PolicyRelationship(
                              policy = PolicyRef("pol2"),
                          )),
                  actionExecutionStrategy = ActionExecutionStrategyEnum.RUN_ALL,
                  actions =
                      listOf(
                          PolicyActionRelationship(
                              constraint =
                                  PolicyConditionAtomic(
                                      operation = OperationEnum.EQUALS,
                                      args =
                                          listOf(
                                              PolicyVariableStatic(value = "1"),
                                              PolicyVariableStatic(value = "1"))),
                              action = PolicyActionClear(key = "1")),
                          PolicyActionRelationship(
                              constraint =
                                  PolicyConditionAtomic(
                                      id = "parCstr1",
                                      operation = OperationEnum.EQUALS,
                                      args =
                                          listOf(
                                              PolicyVariableStatic(value = "1"),
                                              PolicyVariableStatic(value = "1"))),
                              action = PolicyActionClear(id = "pa1", key = "2"))),
                  constraint =
                      PolicyConditionAtomic(
                          id = "cstr1",
                          operation = OperationEnum.EQUALS,
                          args =
                              listOf(
                                  PolicyVariableStatic(value = "1"),
                                  PolicyVariableStatic(value = "1"))),
              )
          given.evaluate(context, catalog)
          given.runActions(context, catalog, PolicyResultEnum.DENY)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 15
          actualEvents[0].entityId shouldBe "constraint(cstr1)/args/0"
          actualEvents[1].entityId shouldBe "constraint(cstr1)/args/1"
          actualEvents[2].entityId shouldBe "constraint(cstr1)"
          actualEvents[3].entityId shouldBe "policies/0(pol1)/condition(${'$'}true)"
          actualEvents[4].entityId shouldBe "policies/0(pol1)"
          actualEvents[5].entityId shouldBe ""
          actualEvents[6].entityId shouldBe "actions/0/constraint/args/0"
          actualEvents[7].entityId shouldBe "actions/0/constraint/args/1"
          actualEvents[8].entityId shouldBe "actions/0/constraint"
          actualEvents[9].entityId shouldBe "actions/0"
          actualEvents[10].entityId shouldBe "actions/1/constraint(parCstr1)/args/0"
          actualEvents[11].entityId shouldBe "actions/1/constraint(parCstr1)/args/1"
          actualEvents[12].entityId shouldBe "actions/1/constraint(parCstr1)"
          actualEvents[13].entityId shouldBe "actions/1(pa1)"
          actualEvents[14].entityId shouldBe ""
        }
      }

      describe("policy priority") {
        it("priority defined") {
          val given =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_OVERRIDES,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.DENY), priority = 1),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.NOT_APPLICABLE),
                              priority = 10),
                      ),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.DENY
          context.event.list() shouldHaveSize 3
          context.event.list()[0].entityId shouldBe "policies/1(${'$'}notApplicable)"
          context.event.list()[1].entityId shouldBe "policies/0(${'$'}deny)"
        }

        it("priority not defined") {
          val given =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                  policies =
                      listOf(
                          PolicyRelationship(policy = PolicyDefault(PolicyResultEnum.PERMIT)),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.NOT_APPLICABLE),
                          ),
                      ),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
          context.event.list() shouldHaveSize 3
          context.event.list()[0].entityId shouldBe "policies/0(${'$'}permit)"
          context.event.list()[1].entityId shouldBe "policies/1(${'$'}notApplicable)"
        }
        it("priority mix") {
          val given =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_UNLESS_DENY,
                  policies =
                      listOf(
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.DENY), priority = -1),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.NOT_APPLICABLE),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY_PERMIT),
                          ),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.PERMIT), priority = 10),
                          PolicyRelationship(
                              policy = PolicyDefault(PolicyResultEnum.INDETERMINATE_PERMIT),
                              priority = 10),
                      ),
              )
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.DENY
          context.event.list() shouldHaveSize 6
          context.event.list()[0].entityId shouldBe "policies/3(${'$'}permit)"
          context.event.list()[1].entityId shouldBe "policies/4(${'$'}indeterminatePermit)"
          context.event.list()[2].entityId shouldBe "policies/1(${'$'}notApplicable)"
          context.event.list()[3].entityId shouldBe "policies/2(${'$'}indeterminate)"
          context.event.list()[4].entityId shouldBe "policies/0(${'$'}deny)"
        }
      }
    })
