package com.github.ivsokol.poe.policy

import com.github.ivsokol.poe.*
import com.github.ivsokol.poe.action.PolicyActionClear
import com.github.ivsokol.poe.action.PolicyActionRef
import com.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import com.github.ivsokol.poe.catalog.PolicyCatalog
import com.github.ivsokol.poe.condition.*
import com.github.ivsokol.poe.event.InMemoryEventTestHandler
import com.github.ivsokol.poe.variable.PolicyVariableStatic
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk

class PolicyTest :
    DescribeSpec({
      val context = Context(event = InMemoryEventTestHandler())

      val catalog =
          PolicyCatalog(
              id = "test-catalog",
              policyConditions =
                  listOf(
                      PolicyConditionAtomic(
                          id = "cond1",
                          operation = OperationEnum.EQUALS,
                          args =
                              listOf(
                                  PolicyVariableStatic(value = "1"),
                                  PolicyVariableStatic(value = "1"))),
                      PolicyConditionAtomic(
                          id = "cond2",
                          version = SemVer(1, 0, 0),
                          operation = OperationEnum.EQUALS,
                          args =
                              listOf(
                                  PolicyVariableStatic(value = "2"),
                                  PolicyVariableStatic(value = "1"))),
                      PolicyConditionAtomic(
                          id = "cond2",
                          version = SemVer(2, 0, 0),
                          operation = OperationEnum.EQUALS,
                          args =
                              listOf(
                                  PolicyVariableStatic(value = "3"),
                                  PolicyVariableStatic(value = "1")))))

      afterTest {
        (context.event as InMemoryEventTestHandler).clear()
        context.removeLastFromPath()
        context.cache.clear()
      }

      describe("cache") {
        it("should cache") {
          val given =
              Policy(
                  id = "1",
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionDefault(true))
          val actual = given.evaluate(context, EmptyPolicyCatalog())
          actual shouldBe PolicyResultEnum.PERMIT
          val actual2 = given.evaluate(context, EmptyPolicyCatalog())
          actual2 shouldBe PolicyResultEnum.PERMIT
          context.cache.getPolicy("1") shouldBe PolicyResultEnum.PERMIT
          context.cache.getPolicy("2") shouldBe null
        }
      }

      describe("condition ref") {
        it("should find condition in catalog") {
          val given =
              Policy(
                  id = "pol1",
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionRef("cond1"))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.PERMIT
        }
        it("should find latest condition in catalog") {
          val given =
              Policy(
                  id = "pol1",
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionRef("cond2"))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.NOT_APPLICABLE
        }
        it("should find exact condition in catalog") {
          val given =
              Policy(
                  id = "pol1",
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionRef(id = "cond2", version = SemVer(1, 0, 0)))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.NOT_APPLICABLE
        }
        it("should not find condition in catalog") {
          val given =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionRef(id = "cond999"))
          val actual = given.evaluate(context, catalog)
          actual shouldBe PolicyResultEnum.INDETERMINATE_PERMIT
        }
      }

      describe("childRefs") {
        it("should return null") {
          val given =
              Policy(
                  id = "pol1",
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionDefault(true))
          given.childRefs() shouldBe null
        }

        it("childRefs populated") {
          val given =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionRef("cond1", SemVer(1, 2, 3)),
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
                  PolicyEntityRefItem(
                      PolicyEntityRefEnum.POLICY_CONDITION_REF, "cond1", SemVer(1, 2, 3)))
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
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  constraint = PolicyConditionDefault(true),
                  condition = PolicyConditionDefault(true))
          val actual = given.evaluate(context, EmptyPolicyCatalog())
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
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  constraint = PolicyConditionDefault(false),
                  condition = PolicyConditionDefault(true))
          val actual = given.evaluate(context, EmptyPolicyCatalog())
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
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  constraint = PolicyConditionDefault(null),
                  condition = PolicyConditionDefault(true))
          val actual = given.evaluate(context, EmptyPolicyCatalog())
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
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  constraint = PolicyConditionDefault(null),
                  condition = PolicyConditionDefault(true),
                  lenientConstraints = false)
          val actual = given.evaluate(context, EmptyPolicyCatalog())
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

      describe("permit") {
        it("condition true") {
          val given =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionDefault(true))
          val actual = given.evaluate(context, EmptyPolicyCatalog())
          actual shouldBe PolicyResultEnum.PERMIT
        }
        it("condition false") {
          val given =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionDefault(false))
          val actual = given.evaluate(context, EmptyPolicyCatalog())
          actual shouldBe PolicyResultEnum.NOT_APPLICABLE
        }
        it("condition false strict") {
          val given =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionDefault(false),
                  strictTargetEffect = true)
          val actual = given.evaluate(context, EmptyPolicyCatalog())
          actual shouldBe PolicyResultEnum.DENY
        }
        it("condition null") {
          val given =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.PERMIT,
                  condition = PolicyConditionDefault(null))
          val actual = given.evaluate(context, EmptyPolicyCatalog())
          actual shouldBe PolicyResultEnum.INDETERMINATE_PERMIT
        }
      }

      describe("deny") {
        it("condition true") {
          val given =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(true))
          val actual = given.evaluate(context, EmptyPolicyCatalog())
          actual shouldBe PolicyResultEnum.DENY
        }
        it("condition false") {
          val given =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(false))
          val actual = given.evaluate(context, EmptyPolicyCatalog())
          actual shouldBe PolicyResultEnum.NOT_APPLICABLE
        }
        it("condition false strict") {
          val given =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(false),
                  strictTargetEffect = true)
          val actual = given.evaluate(context, EmptyPolicyCatalog())
          actual shouldBe PolicyResultEnum.PERMIT
        }
        it("condition null") {
          val given =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(null))
          val actual = given.evaluate(context, EmptyPolicyCatalog())
          actual shouldBe PolicyResultEnum.INDETERMINATE_DENY
        }
      }

      describe("runAction") {
        val given =
            Policy(
                targetEffect = PolicyTargetEffectEnum.DENY,
                condition = PolicyConditionDefault(true),
                actions =
                    listOf(
                        PolicyActionRelationship(
                            constraint = PolicyConditionDefault(false),
                            action = PolicyActionRef("act123"))))
        given.runActions(context, EmptyPolicyCatalog(), PolicyResultEnum.PERMIT) shouldBe true
      }

      describe("identity") {
        it("should return empty") {
          val actual =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(null))
          actual.identity() shouldBe ""
        }
        it("should throw when bad id") {
          shouldThrow<IllegalArgumentException> {
                Policy(
                    id = " ",
                    targetEffect = PolicyTargetEffectEnum.DENY,
                    condition = PolicyConditionDefault(null))
              }
              .message shouldBe "Id must not be blank"
        }
        it("should return id") {
          val actual =
              Policy(
                  id = "1",
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(null))
          actual.identity() shouldBe "1"
        }
        it("should return idVer") {
          val actual =
              Policy(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(null))
          actual.identity() shouldBe "1:1.0.0"
        }
      }

      describe("events") {
        it("minimal") {
          val given =
              Policy(
                  id = "pol1",
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(true))
          given.evaluate(context, EmptyPolicyCatalog())
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 2
          actualEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
          actualEvents[0].entityId shouldBe "pol1/condition(${'$'}true)"

          actualEvents[1].contextId shouldNotBe null
          actualEvents[1].success shouldBe true
          actualEvents[1].entityId shouldBe "pol1"
          actualEvents[1].entity shouldBe PolicyEntityEnum.POLICY
          actualEvents[1].reason shouldBe null
          actualEvents[1].message shouldBe "deny"
          actualEvents[1].fromCache shouldBe false
        }
        it("with constraint") {
          val given =
              Policy(
                  id = "pol1",
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  constraint = PolicyConditionDefault(true),
                  condition = PolicyConditionDefault(true))
          given.evaluate(context, EmptyPolicyCatalog())
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
          actualEvents[0].entityId shouldBe "pol1/constraint(${'$'}true)"

          actualEvents[1].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
          actualEvents[1].entityId shouldBe "pol1/condition(${'$'}true)"

          actualEvents[2].contextId shouldNotBe null
          actualEvents[2].success shouldBe true
          actualEvents[2].entityId shouldBe "pol1"
          actualEvents[2].entity shouldBe PolicyEntityEnum.POLICY
          actualEvents[2].reason shouldBe null
          actualEvents[2].message shouldBe "deny"
          actualEvents[2].fromCache shouldBe false
        }
        it("found in cache") {
          val given =
              Policy(
                  id = "pol1",
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(true))
          given.evaluate(context, EmptyPolicyCatalog())
          (context.event as InMemoryEventTestHandler).clear()
          context.removeLastFromPath()
          given.evaluate(context, EmptyPolicyCatalog())
          val actualEvents = context.event.list().filter { it.entity == PolicyEntityEnum.POLICY }
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe true
          actualEvents[0].entityId shouldBe "pol1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.POLICY
          actualEvents[0].reason shouldBe null
          actualEvents[0].message shouldBe "deny"
          actualEvents[0].fromCache shouldBe true
        }

        it("condition exception") {
          val mockCondition = mockk<IPolicyCondition>()
          every { mockCondition.check(any(), any()) } throws IllegalStateException("mock exception")
          every { mockCondition.childRefs() } returns null
          val given =
              Policy(
                  id = "pol1",
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = mockCondition)
          given.evaluate(context, catalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 2
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pol1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.POLICY
          actualEvents[0].reason shouldContain "mock exception"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false

          actualEvents[1].contextId shouldNotBe null
          actualEvents[1].success shouldBe false
          actualEvents[1].entityId shouldBe "pol1"
          actualEvents[1].entity shouldBe PolicyEntityEnum.POLICY
          actualEvents[1].reason shouldBe null
          actualEvents[1].message shouldBe "indeterminateDeny"
          actualEvents[1].fromCache shouldBe false

          actualEvents[1].timestamp shouldBeAfter actualEvents[0].timestamp
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
              Policy(
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(true))
          given.evaluate(context, EmptyPolicyCatalog())
          val actualEvents = context.event.list().filter { it.entity == PolicyEntityEnum.POLICY }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe ""
        }
        it("child") {
          context.addToPath("policies")
          context.addToPath("0")
          val given =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(true))
          given.evaluate(context, EmptyPolicyCatalog())
          val actualEvents = context.event.list().filter { it.entity == PolicyEntityEnum.POLICY }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe "policies/0"
        }
        it("named child") {
          context.addToPath("policies")
          context.addToPath("0")
          val given =
              Policy(
                  id = "pol1",
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(true))
          given.evaluate(context, EmptyPolicyCatalog())
          val actualEvents = context.event.list().filter { it.entity == PolicyEntityEnum.POLICY }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe "policies/0(pol1)"
        }
        it("condition naming") {
          val given =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition =
                      PolicyConditionAtomic(
                          id = "pca1",
                          operation = OperationEnum.EQUALS,
                          args =
                              listOf(
                                  PolicyVariableStatic(value = "1"),
                                  PolicyVariableStatic(value = "1"))))
          given.evaluate(context, catalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 4
          actualEvents[0].entityId shouldBe "condition(pca1)/args/0"
          actualEvents[1].entityId shouldBe "condition(pca1)/args/1"
          actualEvents[2].entityId shouldBe "condition(pca1)"
          actualEvents[3].entityId shouldBe ""
        }
        it("condition naming for named policy") {
          val given =
              Policy(
                  id = "pol1",
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition =
                      PolicyConditionAtomic(
                          id = "pca1",
                          operation = OperationEnum.EQUALS,
                          args =
                              listOf(
                                  PolicyVariableStatic(value = "1"),
                                  PolicyVariableStatic(value = "1"))))
          given.evaluate(context, catalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 4
          actualEvents[0].entityId shouldBe "pol1/condition(pca1)/args/0"
          actualEvents[1].entityId shouldBe "pol1/condition(pca1)/args/1"
          actualEvents[2].entityId shouldBe "pol1/condition(pca1)"
          actualEvents[3].entityId shouldBe "pol1"
        }
        it("constraint naming") {
          val given =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  constraint =
                      PolicyConditionAtomic(
                          operation = OperationEnum.EQUALS,
                          args =
                              listOf(
                                  PolicyVariableStatic(value = "1"),
                                  PolicyVariableStatic(value = "1"))),
                  condition = PolicyConditionDefault(true),
              )
          given.evaluate(context, catalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 5
          actualEvents[0].entityId shouldBe "constraint/args/0"
          actualEvents[1].entityId shouldBe "constraint/args/1"
          actualEvents[2].entityId shouldBe "constraint"
          actualEvents[3].entityId shouldBe "condition(${'$'}true)"
          actualEvents[4].entityId shouldBe ""
        }
        it("action naming") {
          val given =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.DENY,
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
                  condition = PolicyConditionDefault(true),
              )
          given.evaluate(context, catalog)
          given.runActions(context, EmptyPolicyCatalog(), PolicyResultEnum.DENY)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 14
          actualEvents[0].entityId shouldBe "constraint(cstr1)/args/0"
          actualEvents[1].entityId shouldBe "constraint(cstr1)/args/1"
          actualEvents[2].entityId shouldBe "constraint(cstr1)"
          actualEvents[3].entityId shouldBe "condition(${'$'}true)"
          actualEvents[4].entityId shouldBe ""
          actualEvents[5].entityId shouldBe "actions/0/constraint/args/0"
          actualEvents[6].entityId shouldBe "actions/0/constraint/args/1"
          actualEvents[7].entityId shouldBe "actions/0/constraint"
          actualEvents[8].entityId shouldBe "actions/0"
          actualEvents[9].entityId shouldBe "actions/1/constraint(parCstr1)/args/0"
          actualEvents[10].entityId shouldBe "actions/1/constraint(parCstr1)/args/1"
          actualEvents[11].entityId shouldBe "actions/1/constraint(parCstr1)"
          actualEvents[12].entityId shouldBe "actions/1(pa1)"
          actualEvents[13].entityId shouldBe ""
        }
      }
    })
