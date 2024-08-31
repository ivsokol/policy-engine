package io.github.ivsokol.poe.variable

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class IPolicyVariableSerializerTest :
    FunSpec({
      val json = Json {
        serializersModule = variableSerializersModule
        explicitNulls = false
        encodeDefaults = true
      }

      test("should serialize minimal PolicyVariableStatic") {
        val given: IPolicyVariable =
            PolicyVariableStatic(
                value = "value",
            )
        val expected = """{"value":"value","type":"string"}"""

        val actual = json.encodeToString(given)

        actual shouldEqualJson expected
      }

      test("should serialize minimal PolicyVariableDynamic") {
        val given: IPolicyVariable =
            PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolver(key = "str")))
        val expected = """{"resolvers":[{"key": "str"}]}"""
        val actual = json.encodeToString(given)

        actual shouldEqualJson expected
      }

      test("should deserialize minimal PolicyVariableStatic") {
        val given = """{"value":"value"}"""
        val expected =
            PolicyVariableStatic(
                value = "value",
            )
        val actual: IPolicyVariable = json.decodeFromString(given)
        actual shouldBeEqual expected
      }

      test("should deserialize minimal PolicyVariableDynamic") {
        val given = """{"resolvers":[{"key": "str"}]}"""
        val expected =
            PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolver(key = "str")))
        val actual: IPolicyVariable = json.decodeFromString(given)
        actual shouldBeEqual expected
      }

      test("should throw error in deserialization for bad input JSON") {
        val given = """"value""""
        shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicyVariable>(given) }
            .message shouldBe "Not correct JsonElement for IPolicyVariable DeserializationStrategy"
      }

      test("should throw error in deserialization for bad input JSON Object") {
        val given = """{"key":"value"}"""
        shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicyVariable>(given) }
            .message shouldBe "No corresponding field for IPolicyVariable DeserializationStrategy"
      }
    })
