package io.github.ivsokol.poe.event

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.PolicyEntityEnum
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class InMemoryEventHandlerTest :
    FunSpec({
      test("should not add event") {
        val handler = InMemoryEventHandler(level = EventLevelEnum.NONE)
        val context = Context(event = handler)
        context.event.add(context.id, PolicyEntityEnum.VARIABLE_STATIC, "1", true, "2", true, null)
        val actualEvents = context.event.list()
        actualEvents shouldHaveSize 0
      }

      test("should have event") {
        val handler = InMemoryEventHandler()
        val context = Context(event = handler)
        context.event.add(context.id, PolicyEntityEnum.VARIABLE_STATIC, "1", true, "2", true, null)
        val actualEvents = context.event.list()
        actualEvents shouldHaveSize 1
        actualEvents[0].contextId shouldNotBe null
        actualEvents[0].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC
        actualEvents[0].entityId shouldBe "1"
        actualEvents[0].success shouldBe true
        actualEvents[0].fromCache shouldBe true
        actualEvents[0].reason shouldBe null
        actualEvents[0].message shouldBe null
      }
      test("should have event with details") {
        val handler = InMemoryEventHandler(level = EventLevelEnum.DETAILS)
        val context = Context(event = handler)
        context.event.add(context.id, PolicyEntityEnum.VARIABLE_STATIC, "1", true, "2", true, null)
        val actualEvents = context.event.list()
        actualEvents shouldHaveSize 1
        actualEvents[0].contextId shouldNotBe null
        actualEvents[0].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC
        actualEvents[0].entityId shouldBe "1"
        actualEvents[0].success shouldBe true
        actualEvents[0].fromCache shouldBe true
        actualEvents[0].reason shouldBe null
        actualEvents[0].message shouldBe "2"
      }
    })
