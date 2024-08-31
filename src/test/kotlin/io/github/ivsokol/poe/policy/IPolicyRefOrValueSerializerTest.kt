package io.github.ivsokol.poe.policy

import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.condition.PolicyConditionDefault
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class IPolicyRefOrValueSerializerTest :
    FunSpec({
      val json = Json {
        serializersModule = policySerializersModule
        explicitNulls = false
        encodeDefaults = true
      }

      test("should serialize PolicyRef") {
        val given: IPolicyRefOrValue =
            PolicyRef(
                id = "id1",
                version = SemVer(1, 2, 3),
            )
        val expected = """{"id":"id1","version":"1.2.3","refType":"PolicyRef"}"""

        val actual = json.encodeToString(given)

        actual shouldEqualJson expected
      }

      test("should serialize Policy") {
        val given: IPolicyRefOrValue =
            Policy(
                targetEffect = PolicyTargetEffectEnum.DENY,
                condition = PolicyConditionDefault(true))
        val expected = """{"targetEffect": "deny","condition":{"default":true}}"""

        val actual = json.encodeToString(given)

        actual shouldEqualJson expected
      }

      test("should deserialize PolicyRef") {
        val given = """{"id":"id1","version":"1.2.3","refType":"PolicyRef"}"""
        val expected =
            PolicyRef(
                id = "id1",
                version = SemVer(1, 2, 3),
            )
        val actual: IPolicyRefOrValue = json.decodeFromString(given)
        actual shouldBeEqual expected
      }

      test("should deserialize Policy") {
        val given = """{"targetEffect": "deny","condition":{"default":true}}"""
        val expected: IPolicy =
            Policy(
                targetEffect = PolicyTargetEffectEnum.DENY,
                condition = PolicyConditionDefault(true))
        val actual: IPolicyRefOrValue = json.decodeFromString(given)
        actual shouldBeEqual expected
      }

      test("should throw error in deserialization for bad input JSON") {
        val given = """"value""""
        shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicyRefOrValue>(given) }
            .message shouldBe
            "Not correct JsonElement for IPolicyRefOrValue DeserializationStrategy"
      }

      test("should throw error in deserialization for bad input JSON Object") {
        val given = """{"key":"value"}"""
        shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicyRefOrValue>(given) }
            .message shouldBe "No corresponding field for IPolicyRefOrValue DeserializationStrategy"
      }
    })
