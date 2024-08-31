package com.github.ivsokol.poe

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.*

class DefaultsTest :
    FunSpec({
      test("DefaultEnvironment with test clock") {
        val instant = Instant.parse("2024-02-28T03:42:56.123456789+11:00")
        val clock = Clock.fixed(instant, ZoneOffset.ofHours(11))
        val given = DefaultEnvironment(Options(clock = clock))
        given[DefaultEnvironmentKey.CURRENT_DATE] shouldBe LocalDate.parse("2024-02-28")
        given[DefaultEnvironmentKey.CURRENT_TIME] shouldBe
            OffsetTime.parse("03:42:56.123456789+11:00")
        given[DefaultEnvironmentKey.CURRENT_DATE_TIME] shouldBe
            OffsetDateTime.parse("2024-02-28T03:42:56.123456789+11:00")
        given[DefaultEnvironmentKey.LOCAL_TIME] shouldBe LocalTime.parse("03:42:56.123456789")
        given[DefaultEnvironmentKey.UTC_DATE_TIME] shouldBe
            OffsetDateTime.parse("2024-02-27T16:42:56.123456789Z")
        given[DefaultEnvironmentKey.UTC_DATE] shouldBe LocalDate.parse("2024-02-27")
        given[DefaultEnvironmentKey.UTC_TIME] shouldBe OffsetTime.parse("16:42:56.123456789Z")
        given[DefaultEnvironmentKey.YEAR] shouldBe 2024
        given[DefaultEnvironmentKey.MONTH] shouldBe 2
        given[DefaultEnvironmentKey.DAY] shouldBe 28
        given[DefaultEnvironmentKey.DAY_OF_WEEK] shouldBe 3
        given[DefaultEnvironmentKey.DAY_OF_YEAR] shouldBe 59
        given[DefaultEnvironmentKey.HOUR] shouldBe 3
        given[DefaultEnvironmentKey.MINUTE] shouldBe 42
        given[DefaultEnvironmentKey.SECOND] shouldBe 56
        given[DefaultEnvironmentKey.NANO] shouldBe 123456789
        given[DefaultEnvironmentKey.OFFSET] shouldBe "+11:00"
      }
    })
