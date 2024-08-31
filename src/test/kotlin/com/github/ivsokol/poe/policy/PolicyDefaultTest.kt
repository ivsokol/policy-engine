package com.github.ivsokol.poe.policy

import com.github.ivsokol.poe.*
import com.github.ivsokol.poe.action.PolicyActionRef
import com.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import com.github.ivsokol.poe.condition.PolicyConditionDefault
import com.github.ivsokol.poe.condition.PolicyConditionRef
import com.github.ivsokol.poe.event.InMemoryEventTestHandler
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class PolicyDefaultTest :
    FunSpec({
      val context = Context(event = InMemoryEventTestHandler())
      afterTest {
        (context.event as InMemoryEventTestHandler).clear()
        context.removeLastFromPath()
        context.cache.clear()
      }

      test("permit") {
        val given = PolicyDefault(PolicyResultEnum.PERMIT)
        given.identity() shouldBe "$" + "permit"
        given.labels shouldBe null
        given.version shouldBe null
        given.description shouldBe null
        given.ignoreErrors shouldBe null
        given.evaluate(context, EmptyPolicyCatalog()) shouldBe PolicyResultEnum.PERMIT
      }

      test("deny") {
        val given = PolicyDefault(PolicyResultEnum.DENY)
        given.identity() shouldBe "$" + "deny"
        given.labels shouldBe null
        given.version shouldBe null
        given.description shouldBe null
        given.ignoreErrors shouldBe null
        given.evaluate(context, EmptyPolicyCatalog()) shouldBe PolicyResultEnum.DENY
      }

      test("indeterminateDeny") {
        val given = PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY)
        given.identity() shouldBe "$" + "indeterminateDeny"
        given.labels shouldBe null
        given.version shouldBe null
        given.description shouldBe null
        given.ignoreErrors shouldBe null
        given.evaluate(context, EmptyPolicyCatalog()) shouldBe PolicyResultEnum.INDETERMINATE_DENY
      }

      test("indeterminateDenyPermit") {
        val given = PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY_PERMIT)
        given.identity() shouldBe "$" + "indeterminate"
        given.labels shouldBe null
        given.version shouldBe null
        given.description shouldBe null
        given.ignoreErrors shouldBe null
        given.evaluate(context, EmptyPolicyCatalog()) shouldBe
            PolicyResultEnum.INDETERMINATE_DENY_PERMIT
      }

      test("indeterminatePermit") {
        val given = PolicyDefault(PolicyResultEnum.INDETERMINATE_PERMIT)
        given.identity() shouldBe "$" + "indeterminatePermit"
        given.labels shouldBe null
        given.version shouldBe null
        given.description shouldBe null
        given.ignoreErrors shouldBe null
        given.evaluate(context, EmptyPolicyCatalog()) shouldBe PolicyResultEnum.INDETERMINATE_PERMIT
      }

      test("notApplicable") {
        val given = PolicyDefault(PolicyResultEnum.NOT_APPLICABLE)
        given.identity() shouldBe "$" + "notApplicable"
        given.labels shouldBe null
        given.version shouldBe null
        given.description shouldBe null
        given.ignoreErrors shouldBe null
        given.evaluate(context, EmptyPolicyCatalog()) shouldBe PolicyResultEnum.NOT_APPLICABLE
      }

      test("isSuccess") {
        val given = PolicyDefault(PolicyResultEnum.NOT_APPLICABLE)
        given.identity() shouldBe "$" + "notApplicable"
        given.labels shouldBe null
        given.version shouldBe null
        given.description shouldBe null
        given.ignoreErrors shouldBe null
        given.isSuccess(PolicyResultEnum.PERMIT) shouldBe true
      }

      test("event") {
        val given = PolicyDefault(PolicyResultEnum.PERMIT)
        given.evaluate(context, EmptyPolicyCatalog())
        val actualEvents = context.event.list()
        actualEvents shouldHaveSize 1
        actualEvents[0].contextId shouldNotBe null
        actualEvents[0].success shouldBe true
        actualEvents[0].entityId shouldBe "$" + "permit"
        actualEvents[0].entity shouldBe PolicyEntityEnum.POLICY_DEFAULT
        actualEvents[0].reason shouldBe null
        actualEvents[0].message shouldBe "permit"
        actualEvents[0].fromCache shouldBe false
      }

      test("named") {
        val given = PolicyDefault(PolicyResultEnum.PERMIT)
        given.evaluate(context, EmptyPolicyCatalog())
        val actualEvents = context.event.list()
        actualEvents shouldHaveSize 1
        actualEvents[0].entityId shouldBe "$" + "permit"
      }

      test("named child") {
        context.addToPath("policies")
        context.addToPath("0")
        val given = PolicyDefault(PolicyResultEnum.PERMIT)
        given.evaluate(context, EmptyPolicyCatalog())
        val actualEvents = context.event.list()
        actualEvents shouldHaveSize 1
        actualEvents[0].entityId shouldBe "policies/0(" + "$" + "permit)"
      }

      test("childRefs null") {
        val given = PolicyDefault(PolicyResultEnum.PERMIT)
        given.childRefs() shouldBe null
      }

      test("childRefs populated") {
        val given =
            PolicyDefault(
                default = PolicyResultEnum.PERMIT,
                constraint = PolicyConditionRef("cond123"),
                actions =
                    listOf(
                        PolicyActionRelationship(
                            constraint = PolicyConditionRef("cond456"),
                            action = PolicyActionRef("act123"))))
        given.childRefs()?.shouldHaveSize(3)
        given
            .childRefs()
            ?.shouldContain(
                PolicyEntityRefItem(PolicyEntityRefEnum.POLICY_CONDITION_REF, "cond123", null))
        given
            .childRefs()
            ?.shouldContain(
                PolicyEntityRefItem(PolicyEntityRefEnum.POLICY_CONDITION_REF, "cond456", null))
        given
            .childRefs()
            ?.shouldContain(
                PolicyEntityRefItem(PolicyEntityRefEnum.POLICY_ACTION_REF, "act123", null))
      }

      test("constraintTester true") {
        val given =
            PolicyDefault(PolicyResultEnum.PERMIT, constraint = PolicyConditionDefault(true))
        val actual = given.evaluate(context, EmptyPolicyCatalog())
        actual shouldBe PolicyResultEnum.PERMIT
        val constraintEvents = context.event.list().filter { it.entityId.contains("/constraint") }
        constraintEvents shouldHaveSize 1
        constraintEvents[0].contextId shouldNotBe null
        constraintEvents[0].success shouldBe true
        constraintEvents[0].entityId shouldBe "${'$'}permit/constraint(${'$'}true)"
        constraintEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
        constraintEvents[0].reason shouldBe null
        constraintEvents[0].message shouldBe "true"
        constraintEvents[0].fromCache shouldBe false
      }

      test("constraintTester false") {
        val given =
            PolicyDefault(PolicyResultEnum.PERMIT, constraint = PolicyConditionDefault(false))
        val actual = given.evaluate(context, EmptyPolicyCatalog())
        actual shouldBe PolicyResultEnum.NOT_APPLICABLE
        val constraintEvents = context.event.list().filter { it.entityId.contains("/constraint") }
        constraintEvents shouldHaveSize 1
        constraintEvents[0].contextId shouldNotBe null
        constraintEvents[0].success shouldBe true
        constraintEvents[0].entityId shouldBe "${'$'}permit/constraint(${'$'}false)"
        constraintEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
        constraintEvents[0].reason shouldBe null
        constraintEvents[0].message shouldBe "false"
        constraintEvents[0].fromCache shouldBe false
      }

      test("constraintTester null lenient") {
        val given =
            PolicyDefault(PolicyResultEnum.PERMIT, constraint = PolicyConditionDefault(null))
        val actual = given.evaluate(context, EmptyPolicyCatalog())
        actual shouldBe PolicyResultEnum.NOT_APPLICABLE
        val constraintEvents = context.event.list().filter { it.entityId.contains("/constraint") }
        constraintEvents shouldHaveSize 1
        constraintEvents[0].contextId shouldNotBe null
        constraintEvents[0].success shouldBe true
        constraintEvents[0].entityId shouldBe "${'$'}permit/constraint(${'$'}null)"
        constraintEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
        constraintEvents[0].reason shouldBe null
        constraintEvents[0].message shouldBe null
        constraintEvents[0].fromCache shouldBe false
      }

      test("constraintTester null strict") {
        val given =
            PolicyDefault(
                PolicyResultEnum.PERMIT,
                constraint = PolicyConditionDefault(null),
                lenientConstraints = false)
        val actual = given.evaluate(context, EmptyPolicyCatalog())
        actual shouldBe PolicyResultEnum.INDETERMINATE_DENY_PERMIT
        val constraintEvents = context.event.list().filter { it.entityId.contains("/constraint") }
        constraintEvents shouldHaveSize 1
        constraintEvents[0].contextId shouldNotBe null
        constraintEvents[0].success shouldBe true
        constraintEvents[0].entityId shouldBe "${'$'}permit/constraint(${'$'}null)"
        constraintEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
        constraintEvents[0].reason shouldBe null
        constraintEvents[0].message shouldBe null
        constraintEvents[0].fromCache shouldBe false
      }

      test("runAction") {
        val given =
            PolicyDefault(
                PolicyResultEnum.PERMIT,
                actions =
                    listOf(
                        PolicyActionRelationship(
                            constraint = PolicyConditionDefault(false),
                            action = PolicyActionRef("act123"))))
        given.runActions(context, EmptyPolicyCatalog(), PolicyResultEnum.PERMIT) shouldBe true
      }
    })
