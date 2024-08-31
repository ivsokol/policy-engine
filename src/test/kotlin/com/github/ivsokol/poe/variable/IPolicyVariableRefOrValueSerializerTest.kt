package com.github.ivsokol.poe.variable

import com.github.ivsokol.poe.SemVer
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class IPolicyVariableRefOrValueSerializerTest :
    FunSpec({
      val json = Json {
        serializersModule = variableSerializersModule
        explicitNulls = false
        encodeDefaults = true
      }

      test("should serialize PolicyVariableRef") {
        val given: IPolicyVariableRefOrValue =
            PolicyVariableRef(
                id = "id1",
                version = SemVer(1, 2, 3),
            )
        val expected = """{"id":"id1","version":"1.2.3","refType":"PolicyVariableRef"}"""

        val actual = json.encodeToString(given)

        actual shouldEqualJson expected
      }

      test("should serialize minimal PolicyVariableStatic") {
        val given: IPolicyVariableRefOrValue =
            PolicyVariableStatic(
                value = "value",
            )
        val expected = """{"value":"value","type":"string"}"""

        val actual = json.encodeToString(given)

        actual shouldEqualJson expected
      }

      test("should serialize minimal PolicyVariableDynamic") {
        val given: IPolicyVariableRefOrValue =
            PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolver(key = "str")))
        val expected = """{"resolvers":[{"key": "str"}]}"""
        val actual = json.encodeToString(given)

        actual shouldEqualJson expected
      }

      test("should deserialize PolicyVariableRef") {
        val given = """{"id":"id1","version":"1.2.3","refType":"PolicyVariableRef"}"""
        val expected =
            PolicyVariableRef(
                id = "id1",
                version = SemVer(1, 2, 3),
            )
        val actual: IPolicyVariableRefOrValue = json.decodeFromString(given)
        actual shouldBeEqual expected
      }

      test("should deserialize minimal PolicyVariableStatic") {
        val given = """{"value":"value"}"""
        val expected =
            PolicyVariableStatic(
                value = "value",
            )
        val actual: IPolicyVariableRefOrValue = json.decodeFromString(given)
        actual shouldBeEqual expected
      }

      test("should deserialize minimal PolicyVariableDynamic for key resolver") {
        val given = """{"resolvers":[{"key": "str"}]}"""
        val expected =
            PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolver(key = "str")))
        val actual: IPolicyVariableRefOrValue = json.decodeFromString(given)
        actual shouldBeEqual expected
      }

      test("should deserialize minimal PolicyVariableDynamic for path resolver") {
        val given = """{"resolvers":[{"path":"str","engine":"JQ"}]}"""
        val expected =
            PolicyVariableDynamic(
                resolvers =
                    listOf(
                        PolicyVariableResolver(
                            path = "str", engine = PolicyVariableResolverEngineEnum.JQ)))
        val actual: IPolicyVariableRefOrValue = json.decodeFromString(given)
        actual shouldBeEqual expected
      }

      test("should throw error in deserialization for bad input JSON") {
        val given = """"value""""
        shouldThrow<IllegalArgumentException> {
              json.decodeFromString<IPolicyVariableRefOrValue>(given)
            }
            .message shouldBe
            "Not correct JsonElement for IPolicyVariableRefOrValue DeserializationStrategy"
      }

      test("should throw error in deserialization for bad input JSON Object") {
        val given = """{"key":"value"}"""
        shouldThrow<IllegalArgumentException> {
              json.decodeFromString<IPolicyVariableRefOrValue>(given)
            }
            .message shouldBe
            "No corresponding field for IPolicyVariableRefOrValue DeserializationStrategy"
      }
    })
