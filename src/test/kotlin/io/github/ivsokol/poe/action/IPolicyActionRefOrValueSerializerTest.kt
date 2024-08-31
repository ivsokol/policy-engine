package io.github.ivsokol.poe.action

import io.github.ivsokol.poe.SemVer
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class IPolicyActionRefOrValueSerializerTest :
    FunSpec({
      val json = Json {
        serializersModule = actionSerializersModule
        explicitNulls = false
        encodeDefaults = true
      }

      test("should serialize PolicyActionRef") {
        val given: IPolicyActionRefOrValue =
            PolicyActionRef(
                id = "id1",
                version = SemVer(1, 2, 3),
            )
        val expected = """{"id":"id1","version":"1.2.3","refType":"PolicyActionRef"}"""

        val actual = json.encodeToString(given)

        actual shouldEqualJson expected
      }

      test("should serialize PolicyAction") {
        val given: IPolicyActionRefOrValue = PolicyActionClear(key = "a")
        val expected = """{"type": "clear","key": "a"}"""

        val actual = json.encodeToString(given)

        actual shouldEqualJson expected
      }

      test("should deserialize PolicyActionRef") {
        val given = """{"id":"id1","version":"1.2.3","refType":"PolicyActionRef"}"""
        val expected =
            PolicyActionRef(
                id = "id1",
                version = SemVer(1, 2, 3),
            )
        val actual: IPolicyActionRefOrValue = json.decodeFromString(given)
        actual shouldBeEqual expected
      }

      test("should deserialize PolicyAction") {
        val given = """{"type": "clear","key": "a"}"""
        val expected: IPolicyAction = PolicyActionClear(key = "a")
        val actual: IPolicyActionRefOrValue = json.decodeFromString(given)
        actual shouldBeEqual expected
      }

      test("should throw error in deserialization for bad input JSON") {
        val given = """"value""""
        shouldThrow<IllegalArgumentException> {
              json.decodeFromString<IPolicyActionRefOrValue>(given)
            }
            .message shouldBe
            "Not correct JsonElement for IPolicyActionRefOrValue DeserializationStrategy"
      }

      test("should throw error in deserialization for bad input JSON Object") {
        val given = """{"key":"value"}"""
        shouldThrow<IllegalArgumentException> {
              json.decodeFromString<IPolicyActionRefOrValue>(given)
            }
            .message shouldBe
            "No corresponding field for IPolicyActionRefOrValue DeserializationStrategy"
      }
    })
