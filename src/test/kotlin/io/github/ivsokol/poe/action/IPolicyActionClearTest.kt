package io.github.ivsokol.poe.action

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.event.InMemoryEventTestHandler
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

class IPolicyActionClearTest :
    DescribeSpec({
      val context = Context(request = mapOf("1" to "2"), event = InMemoryEventTestHandler())

      beforeTest { context.dataStore()["a"] = "value" }

      afterTest {
        (context.event as InMemoryEventTestHandler).clear()
        context.removeLastFromPath()
        context.cache.clear()
      }

      describe("run") {
        it("should clear value") {
          val given = PolicyActionClear(key = "a")
          val actual = given.run(context, EmptyPolicyCatalog())
          actual shouldBe true
          context.dataStore()["a"] shouldBe null
        }
        it("should clear non existing value") {
          val given = PolicyActionClear(key = "b")
          val actual = given.run(context, EmptyPolicyCatalog())
          actual shouldBe true
          context.dataStore()["a"] shouldBe "value"
        }
        it("should fail on non existing value") {
          val given = PolicyActionClear(key = "b", failOnMissingKey = true)
          val actual = given.run(context, EmptyPolicyCatalog())
          actual shouldBe false
          context.dataStore()["a"] shouldBe "value"
        }
      }

      describe("childRefs") {
        it("should return null") {
          val actual = PolicyActionClear(key = "a")
          actual.childRefs() shouldBe null
        }
      }

      describe("identity") {
        it("should return empty") {
          val actual = PolicyActionClear(key = "a")
          actual.identity() shouldBe ""
        }
        it("should throw when bad id") {
          shouldThrow<IllegalArgumentException> { PolicyActionClear(id = "", key = "a") }
              .message shouldBe "Id must not be empty"
        }
        it("should return id") {
          val actual = PolicyActionClear(id = "1", key = "a")
          actual.identity() shouldBe "1"
        }
        it("should return idVer") {
          val actual = PolicyActionClear(id = "1", version = SemVer(1, 0, 0), key = "a")
          actual.identity() shouldBe "1:1.0.0"
        }
      }

      describe("events") {
        it("clear") {
          val given = PolicyActionClear(id = "pva1", key = "a")
          given.run(context, EmptyPolicyCatalog())
          val actualEvents = context.event.list()
          actualEvents[0].contextId shouldNotBe null
          actualEvents shouldHaveSize 1
          actualEvents[0].success shouldBe true
          actualEvents[0].entityId shouldBe "pva1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.POLICY_ACTION_CLEAR
          actualEvents[0].reason shouldBe null
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false
        }
        it("exception") {
          val given = PolicyActionClear(id = "pva1", key = "b", failOnMissingKey = true)
          given.run(context, EmptyPolicyCatalog())
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pva1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.POLICY_ACTION_CLEAR
          actualEvents[0].reason shouldContain "Missing key: b"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false
        }
      }

      describe("naming") {
        it("not named") {
          val given = PolicyActionClear(key = "a")
          given.run(context, EmptyPolicyCatalog())
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe ""
        }
        it("child") {
          context.addToPath("actions")
          context.addToPath("0")
          val given = PolicyActionClear(key = "a")
          given.run(context, EmptyPolicyCatalog())
          context.removeLastFromPath()
          context.removeLastFromPath()
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe "actions/0"
        }
        it("named child") {
          context.addToPath("actions")
          context.addToPath("0")
          val given = PolicyActionClear(id = "pva1", key = "a")
          given.run(context, EmptyPolicyCatalog())
          context.removeLastFromPath()
          context.removeLastFromPath()
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe "actions/0(pva1)"
        }
      }
    })
