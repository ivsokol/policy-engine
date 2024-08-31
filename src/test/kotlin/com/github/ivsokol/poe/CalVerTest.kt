package com.github.ivsokol.poe

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private data class CalVerSerializationTestData(val given: CalVer, val expected: String)

private data class CalVerDeserializationTestData(val given: String, val expected: CalVer)

class CalVerTest :
    FunSpec({
      withData(
          listOf(
              CalVerSerializationTestData(CalVer(2024, 2, 13), """"2024-02-13""""),
              CalVerSerializationTestData(CalVer(2024, 2, 13, 1), """"2024-02-13-1""""),
          )) { (given, expected) ->
            val actual = Json.encodeToString(given)
            actual shouldBe expected
          }

      withData(
          listOf(
              CalVerDeserializationTestData(""""2024-02-13"""", CalVer(2024, 2, 13)),
              CalVerDeserializationTestData(""""2024-02-13-1"""", CalVer(2024, 2, 13, 1)),
          )) { (given, expected) ->
            val actual = Json.decodeFromString<CalVer>(given)
            actual shouldBeEqual expected
          }

      test("Error deserialization") {
        shouldThrow<IllegalArgumentException> { Json.decodeFromString<CalVer>(""""1999-02-1"""") }
      }

      test("Init test zero") { shouldThrow<IllegalArgumentException> { CalVer(1999, 10, 15) } }

      test("Init test negative") { shouldThrow<IllegalArgumentException> { CalVer(-1999, 0, 0) } }

      test("Default CalVer") {
        DefaultCalVer() shouldBe
            LocalDate.now().let { CalVer(it.year, it.monthValue, it.dayOfMonth) }
      }

      test("compare") {
        val v1 = CalVer(2024, 2, 13)
        val v2 = CalVer(2024, 2, 13, 1)
        val v3 = CalVer(2024, 2, 13, 2)
        val v4 = CalVer(2024, 2, 14)
        val v5 = CalVer(2024, 3, 1)
        val v6 = CalVer(2025, 3, 1)
        val v7 = CalVer(2025, 3, 1)

        v1.compareTo(v1) shouldBe 0
        v1.compareTo(v2) shouldBe -1
        v1.compareTo(v3) shouldBe -1
        v1.compareTo(v4) shouldBe -1

        v2.compareTo(v1) shouldBe 1
        v2.compareTo(v3) shouldBe -1

        v3.compareTo(v1) shouldBe 1
        v3.compareTo(v2) shouldBe 1
        v3.compareTo(v4) shouldBe -1

        v4.compareTo(v5) shouldBe -1
        v5.compareTo(v6) shouldBe -1
        v6.compareTo(v7) shouldBe 0
      }
    })
