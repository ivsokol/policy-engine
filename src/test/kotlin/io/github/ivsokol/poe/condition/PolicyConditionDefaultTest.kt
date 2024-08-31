package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.event.InMemoryEventTestHandler
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class PolicyConditionDefaultTest :
    FunSpec({
      val context = Context(event = InMemoryEventTestHandler())
      afterTest {
        (context.event as InMemoryEventTestHandler).clear()
        context.removeLastFromPath()
        context.cache.clear()
      }

      test("true") {
        val given = PolicyConditionDefault(true)
        given.identity() shouldBe "$" + "true"
        given.labels shouldBe null
        given.negateResult shouldBe null
        given.version shouldBe null
        given.description shouldBe null
        given.check(context, EmptyPolicyCatalog()) shouldBe true
      }

      test("false") {
        val given = PolicyConditionDefault(false)
        given.identity() shouldBe "$" + "false"
        given.labels shouldBe null
        given.negateResult shouldBe null
        given.version shouldBe null
        given.description shouldBe null
        given.check(context, EmptyPolicyCatalog()) shouldBe false
      }

      test("null") {
        val given = PolicyConditionDefault(null)
        given.identity() shouldBe "$" + "null"
        given.labels shouldBe null
        given.negateResult shouldBe null
        given.version shouldBe null
        given.description shouldBe null
        given.check(context, EmptyPolicyCatalog()) shouldBe null
      }

      test("event") {
        val given = PolicyConditionDefault(true)
        given.check(context, EmptyPolicyCatalog())
        val actualEvents = context.event.list()
        actualEvents shouldHaveSize 1
        actualEvents[0].contextId shouldNotBe null
        actualEvents[0].success shouldBe true
        actualEvents[0].entityId shouldBe "$" + "true"
        actualEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_DEFAULT
        actualEvents[0].reason shouldBe null
        actualEvents[0].message shouldBe true.toString()
        actualEvents[0].fromCache shouldBe false
      }

      test("named") {
        val given = PolicyConditionDefault(true)
        given.check(context, EmptyPolicyCatalog())
        val actualEvents = context.event.list()
        actualEvents shouldHaveSize 1
        actualEvents[0].entityId shouldBe "$" + "true"
      }

      test("named child") {
        context.addToPath("conditions")
        context.addToPath("0")
        val given = PolicyConditionDefault(true)
        given.check(context, EmptyPolicyCatalog())
        val actualEvents = context.event.list()
        actualEvents shouldHaveSize 1
        actualEvents[0].entityId shouldBe "conditions/0(" + "$" + "true)"
      }

      test("childRefs") {
        val given = PolicyConditionDefault(true)
        given.childRefs() shouldBe null
      }
    })
