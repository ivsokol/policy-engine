package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.SemVer
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class IPolicyConditionRefOrValueSerializerTest :
    FunSpec({
      val json = Json {
        serializersModule = conditionSerializersModule
        explicitNulls = false
        encodeDefaults = true
      }

      test("should serialize PolicyConditionRef") {
        val given: IPolicyConditionRefOrValue =
            PolicyConditionRef(
                id = "id1",
                version = SemVer(1, 2, 3),
            )
        val expected = """{"id":"id1","version":"1.2.3","refType":"PolicyConditionRef"}"""

        val actual = json.encodeToString(given)

        actual shouldEqualJson expected
      }

      test("should serialize PolicyCondition") {
        val given: IPolicyConditionRefOrValue =
            PolicyConditionAtomic(
                operation = OperationEnum.GREATER_THAN, args = listOf(int(1), int(2)))
        val expected =
            """{"args":[{"value":1,"type":"int"},{"value":2,"type":"int"}],"operation":"GreaterThan"}"""

        val actual = json.encodeToString(given)

        actual shouldEqualJson expected
      }

      test("should deserialize PolicyConditionRef") {
        val given = """{"id":"id1","version":"1.2.3","refType":"PolicyConditionRef"}"""
        val expected =
            PolicyConditionRef(
                id = "id1",
                version = SemVer(1, 2, 3),
            )
        val actual: IPolicyConditionRefOrValue = json.decodeFromString(given)
        actual shouldBeEqual expected
      }

      test("should deserialize PolicyCondition") {
        val given =
            """{"args":[{"value":1,"type":"int"},{"value":2,"type":"int"}],"operation":"GreaterThan"}"""
        val expected: IPolicyCondition =
            PolicyConditionAtomic(
                operation = OperationEnum.GREATER_THAN, args = listOf(int(1), int(2)))
        val actual: IPolicyConditionRefOrValue = json.decodeFromString(given)
        actual shouldBeEqual expected
      }

      test("should throw error in deserialization for bad input JSON") {
        val given = """"value""""
        shouldThrow<IllegalArgumentException> {
              json.decodeFromString<IPolicyConditionRefOrValue>(given)
            }
            .message shouldBe
            "Not correct JsonElement for IPolicyConditionRefOrValue DeserializationStrategy"
      }

      test("should throw error in deserialization for bad input JSON Object") {
        val given = """{"key":"value"}"""
        shouldThrow<IllegalArgumentException> {
              json.decodeFromString<IPolicyConditionRefOrValue>(given)
            }
            .message shouldBe
            "No corresponding field for IPolicyConditionRefOrValue DeserializationStrategy"
      }
    })
