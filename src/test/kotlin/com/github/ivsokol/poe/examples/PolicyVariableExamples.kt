package com.github.ivsokol.poe.examples

import com.github.ivsokol.poe.SemVer
import com.github.ivsokol.poe.catalog.catalogSerializersModule
import com.github.ivsokol.poe.variable.*
import io.kotest.core.spec.style.FunSpec
import java.time.Duration
import java.time.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

class PolicyVariableExamples :
    FunSpec({
      val json = Json {
        serializersModule = catalogSerializersModule
        explicitNulls = false
        encodeDefaults = true
        prettyPrint = true
      }
      val folder = "policy-variable"
      test("Minimal PolicyVariableStatic") {
        val example = PolicyVariableStatic(value = 42)
        // default serializer adds type, so removal is necessary for example
        val jsonExample = json.encodeToJsonElement(example)
        ExampleWriter.save(
            folder,
            "minimal-policy-variable-static",
            json.encodeToString(jsonExample.jsonObject.filterKeys { k -> k != "type" }))
      }
      test("Embedded PolicyVariableStatic with type and format") {
        val example =
            PolicyVariableStatic(
                value = LocalDate.parse("2024-01-23"),
                type = VariableValueTypeEnum.STRING,
                format = VariableValueFormatEnum.DATE)
        ExampleWriter.save(folder, "embedded-policy-variable-static", json.encodeToString(example))
      }
      test("Managed PolicyVariableStatic") {
        val example =
            PolicyVariableStatic(
                id = "polVal1",
                version = SemVer(1, 2, 3),
                description = "This is a managed PolicyVariableStatic",
                labels = listOf("label1", "label2"),
                value = Duration.parse("PT1H"),
                type = VariableValueTypeEnum.STRING,
                format = VariableValueFormatEnum.DURATION)
        ExampleWriter.save(folder, "managed-policy-variable-static", json.encodeToString(example))
      }

      test("Minimal PolicyVariableDynamic") {
        val example = PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolver(key = "foo")))
        ExampleWriter.save(folder, "minimal-policy-variable-dynamic", json.encodeToString(example))
      }
      test("Minimal PolicyVariableDynamic with resolver reference") {
        val example =
            PolicyVariableDynamic(
                resolvers =
                    listOf(PolicyVariableResolverRef(id = "polVarRes1", version = SemVer(1, 2, 3))))
        ExampleWriter.save(
            folder, "minimal-policy-variable-dynamic-with-ref", json.encodeToString(example))
      }
      test("managed PolicyVariableDynamic with multiple resolvers") {
        val example =
            PolicyVariableDynamic(
                id = "polVal1",
                version = SemVer(1, 2, 3),
                description = "This is a managed PolicyVariableDynamic",
                labels = listOf("label1", "label2"),
                type = VariableValueTypeEnum.STRING,
                format = VariableValueFormatEnum.DURATION,
                resolvers =
                    listOf(
                        PolicyVariableResolverRef(id = "polVarRes1", version = SemVer(1, 2, 3)),
                        PolicyVariableResolver(key = "duration")))
        ExampleWriter.save(
            folder,
            "managed-policy-variable-dynamic-with-multiple-refs",
            json.encodeToString(example))
      }

      test("Key PolicyVariableResolver") {
        val example = PolicyVariableResolver(key = "foo")
        ExampleWriter.save(folder, "key-policy-variable-resolver", json.encodeToString(example))
      }
      test("JQ PolicyVariableResolver") {
        val example =
            PolicyVariableResolver(
                engine = PolicyVariableResolverEngineEnum.JQ, path = ".foo.a1.a2")
        ExampleWriter.save(folder, "jq-policy-variable-resolver", json.encodeToString(example))
      }
      test("JQ PolicyVariableResolver with prefilter") {
        val example =
            PolicyVariableResolver(
                engine = PolicyVariableResolverEngineEnum.JQ,
                source = ContextStoreEnum.SUBJECT,
                key = "foo",
                path = ".a1.a2")
        ExampleWriter.save(
            folder, "jq-prefilter-policy-variable-resolver", json.encodeToString(example))
      }
      test("JMESPath PolicyVariableResolver with prefilter") {
        val example =
            PolicyVariableResolver(
                engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                source = ContextStoreEnum.SUBJECT,
                key = "foo",
                path = "a1.a2")
        ExampleWriter.save(
            folder, "jmespath-prefilter-policy-variable-resolver", json.encodeToString(example))
      }
      test("Managed PolicyVariableResolver") {
        val example =
            PolicyVariableResolver(
                id = "polValRes1",
                version = SemVer(1, 2, 3),
                description = "This is a managed PolicyVariableResolver",
                labels = listOf("label1", "label2"),
                engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                source = ContextStoreEnum.SUBJECT,
                key = "foo",
                path = "a1.a2")
        ExampleWriter.save(folder, "managed-policy-variable-resolver", json.encodeToString(example))
      }
    })
