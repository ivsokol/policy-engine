package com.github.ivsokol.poe.variable

import com.github.ivsokol.poe.SemVer
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class IPolicyVariableResolverRefOrValueSerializerTest :
    FunSpec({
      val json = Json {
        serializersModule = variableSerializersModule
        explicitNulls = false
        encodeDefaults = true
      }

      test("should serialize PolicyVariableResolverRef") {
        val given: IPolicyVariableResolverRefOrValue =
            PolicyVariableResolverRef(
                id = "id1",
                version = SemVer(1, 2, 3),
            )
        val expected = """{"id":"id1","version":"1.2.3","refType":"PolicyVariableResolverRef"}"""

        val actual = json.encodeToString(given)

        actual shouldEqualJson expected
      }

      test("should serialize minimal PolicyVariableResolver") {
        val given: IPolicyVariableResolverRefOrValue = PolicyVariableResolver(key = "str")
        val expected = """{"key":"str"}"""

        val actual = json.encodeToString(given)

        actual shouldEqualJson expected
      }

      test("should deserialize PolicyVariableResolverRef") {
        val given = """{"id":"id1","version":"1.2.3","refType":"PolicyVariableResolverRef"}"""
        val expected =
            PolicyVariableResolverRef(
                id = "id1",
                version = SemVer(1, 2, 3),
            )
        val actual: IPolicyVariableResolverRefOrValue = json.decodeFromString(given)
        actual shouldBeEqual expected
      }

      test("should deserialize minimal PolicyVariableResolver") {
        val given = """{"key":"str"}"""
        val expected =
            PolicyVariableResolver(
                key = "str",
            )
        val actual: IPolicyVariableResolverRefOrValue = json.decodeFromString(given)
        actual shouldBeEqual expected
      }

      test("should throw error in deserialization for bad input JSON") {
        val given = """"value""""
        shouldThrow<IllegalArgumentException> {
              json.decodeFromString<IPolicyVariableResolverRefOrValue>(given)
            }
            .message shouldBe
            "Not correct JsonElement for IPolicyVariableResolverRefOrValue DeserializationStrategy"
      }

      test("should throw error in deserialization for bad input JSON Object") {
        val given = """{"key2":"value"}"""
        shouldThrow<IllegalArgumentException> {
              json.decodeFromString<IPolicyVariableResolverRefOrValue>(given)
            }
            .message shouldBe
            "No corresponding field for IPolicyVariableResolverRefOrValue DeserializationStrategy"
      }
    })
