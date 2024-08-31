package com.github.ivsokol.poe.variable

import com.github.ivsokol.poe.SemVer
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PolicyVariableResolverSerializerTest :
    FunSpec({
      val json = Json {
        serializersModule = variableSerializersModule
        explicitNulls = false
        encodeDefaults = true
      }

      test("should serialize minimal PolicyVariableResolver") {
        val given = PolicyVariableResolver(key = "path1")
        val expected = """{"key":"path1"}"""

        val actual = json.encodeToString(given)

        actual shouldEqualJson expected
      }

      test("should deserialize minimal PolicyVariableResolver") {
        val expected = PolicyVariableResolver(key = "path1")
        val given = """{"key":"path1"}"""

        val actual = json.decodeFromString<PolicyVariableResolver>(given)

        actual shouldBe expected
      }

      test("should serialize PolicyVariableResolver with all fields") {
        val given =
            PolicyVariableResolver(
                id = "id",
                version = SemVer(1, 2, 3),
                description = "description",
                labels = listOf("label1", "label2"),
                source = ContextStoreEnum.DATA,
                key = "key1",
                path = "path1",
                engine = PolicyVariableResolverEngineEnum.JQ)
        val expected =
            """{"id": "id","version": "1.2.3","description": "description","labels": ["label1","label2"],"path":"path1","key": "key1","engine":"JQ","source":"data"}"""
        val actual = json.encodeToString(given)
        actual shouldEqualJson expected
      }

      test("should deserialize PolicyVariableResolver with all fields") {
        val expected =
            PolicyVariableResolver(
                id = "id",
                version = SemVer(1, 2, 3),
                description = "description",
                labels = listOf("label1", "label2"),
                source = ContextStoreEnum.DATA,
                key = "key1",
                path = "path1",
                engine = PolicyVariableResolverEngineEnum.JQ)
        val given =
            """{"id": "id","version": "1.2.3","description": "description","labels": ["label1","label2"],"path":"path1","key": "key1","engine":"JQ","source":"data"}"""
        val actual = json.decodeFromString<PolicyVariableResolver>(given)
        actual shouldBe expected
      }
    })
