package io.github.ivsokol.poe

import io.github.ivsokol.poe.cache.HashMapCache
import io.github.ivsokol.poe.variable.ContextStoreEnum
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.LocalDateTime
import java.time.OffsetTime

class ContextTest :
    FunSpec({
      test("basic store") {
        val expected =
            Context(
                request = mapOf("1" to "2"),
                environment =
                    mapOf("now" to LocalDateTime.now(), DefaultEnvironmentKey.YEAR to 2021),
                subject = mapOf("user" to "guest"))
        expected.store(ContextStoreEnum.REQUEST)?.shouldHaveSize(1)
        expected.store(ContextStoreEnum.ENVIRONMENT)?.shouldHaveSize(19)
        expected.store(ContextStoreEnum.ENVIRONMENT)?.get("year") shouldBe 2021
        expected.store(ContextStoreEnum.ENVIRONMENT)?.get("offset") shouldBe
            OffsetTime.now().offset.toString()
        expected.store(ContextStoreEnum.SUBJECT)?.shouldHaveSize(1)
        expected.store(ContextStoreEnum.DATA)?.shouldHaveSize(0)

        expected.cache.shouldBeInstanceOf<HashMapCache>()
        expected.options.shouldBeInstanceOf<Options>()
      }

      test("minimal store") {
        val expected =
            Context(
                request = mapOf("1" to "2"),
            )
        expected.store(ContextStoreEnum.REQUEST)?.shouldHaveSize(1)
        expected.store(ContextStoreEnum.ENVIRONMENT)?.shouldHaveSize(18)
        expected.store(ContextStoreEnum.SUBJECT) shouldBe null
        expected.store(ContextStoreEnum.DATA)?.shouldHaveSize(0)

        expected.cache.shouldBeInstanceOf<HashMapCache>()
        expected.options.shouldBeInstanceOf<Options>()
      }

      test("empty store") {
        val ctx = Context()
        ctx.store(ContextStoreEnum.REQUEST) shouldBe null
        ctx.store(ContextStoreEnum.ENVIRONMENT)?.shouldHaveSize(18)
        ctx.store(ContextStoreEnum.SUBJECT) shouldBe null
      }
    })
