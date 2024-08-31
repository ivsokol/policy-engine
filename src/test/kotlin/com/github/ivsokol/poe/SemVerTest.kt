/* (C)2024 */
package com.github.ivsokol.poe

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private data class SemVerSerializationTestData(val given: SemVer, val expected: String)

private data class SemVerDeserializationTestData(val given: String, val expected: SemVer)

class SemVerTest :
    FunSpec({
      withData(
          listOf(
              SemVerSerializationTestData(SemVer(1, 2, 3), """"1.2.3""""),
              SemVerSerializationTestData(
                  SemVer(1, 2, 3, "SNAPSHOT", "alpha"), """"1.2.3-SNAPSHOT+alpha""""),
              SemVerSerializationTestData(SemVer(1, 2, 3, " ", "alpha"), """"1.2.3+alpha""""),
          )) { (given, expected) ->
            val actual = Json.encodeToString(given)
            actual shouldBe expected
          }

      withData(
          listOf(
              SemVerDeserializationTestData(""""1.2.3"""", SemVer(1, 2, 3)),
              SemVerDeserializationTestData(
                  """"1.2.3-SNAPSHOT+alpha"""", SemVer(1, 2, 3, "SNAPSHOT", "alpha")),
              SemVerDeserializationTestData(""""1.2.3+alpha"""", SemVer(1, 2, 3, null, "alpha")),
          )) { (given, expected) ->
            val actual = Json.decodeFromString<SemVer>(given)
            actual shouldBeEqual expected
          }

      test("Error deserialization") {
        shouldThrow<IllegalArgumentException> { Json.decodeFromString<SemVer>(""""1.2.3alpha"""") }
      }

      test("Init test zero") { shouldThrow<IllegalArgumentException> { SemVer(0, 0, 0) } }

      test("Init test negative") { shouldThrow<IllegalArgumentException> { SemVer(-1, 0, 0) } }

      test("Default SemVer") { DefaultSemVer().toString() shouldBe "0.1.0-SNAPSHOT" }

      test("compare") {
        val v1 = SemVer(1, 2, 3)
        val v2 = SemVer(1, 2, 3, "SNAPSHOT", null)
        val v3 = SemVer(1, 2, 3, "SNAPSHOT", "alpha")
        val v4 = SemVer(1, 2, 4)
        val v5 = SemVer(1, 3, 0)
        val v6 = SemVer(2, 0, 0)
        val v7 = SemVer(1, 2, 3, "RELEASE", null)
        val v8 = SemVer(1, 2, 3, "SNAPSHOT", "beta")

        v1.compareTo(v1) shouldBe 0
        v1.compareTo(v2) shouldBe 1
        v1.compareTo(v3) shouldBe 1
        v1.compareTo(v4) shouldBe -1
        v1.compareTo(v5) shouldBe -1
        v1.compareTo(v6) shouldBe -1

        v2.compareTo(v3) shouldBe 1
        v2.compareTo(v4) shouldBe -1
        v2.compareTo(v5) shouldBe -1
        v2.compareTo(v6) shouldBe -1
        v2.compareTo(v7) shouldBe 1
        v2.compareTo(v8) shouldBe 1

        v3.compareTo(v4) shouldBe -1
        v3.compareTo(v5) shouldBe -1
        v3.compareTo(v6) shouldBe -1
        v3.compareTo(v8) shouldBe -1
      }
    })
