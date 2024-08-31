package com.github.ivsokol.poe.examples

import com.github.ivsokol.poe.SemVer
import com.github.ivsokol.poe.action.PolicyActionClear
import com.github.ivsokol.poe.action.PolicyActionJsonMerge
import com.github.ivsokol.poe.action.PolicyActionJsonPatch
import com.github.ivsokol.poe.action.PolicyActionSave
import com.github.ivsokol.poe.catalog.catalogSerializersModule
import com.github.ivsokol.poe.variable.*
import io.kotest.core.spec.style.FunSpec
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PolicyActionExamples :
    FunSpec({
      val json = Json {
        serializersModule = catalogSerializersModule
        explicitNulls = false
        encodeDefaults = true
        prettyPrint = true
      }
      val folder = "policy-action"
      test("PolicyActionSave with static variable and mandatory new key") {
        val example =
            PolicyActionSave(
                key = "foo", value = PolicyVariableStatic(value = "bar"), failOnExistingKey = true)
        ExampleWriter.save(folder, "policy-action-save-static", json.encodeToString(example))
      }

      test("PolicyActionSave with non null dynamic variable") {
        val example =
            PolicyActionSave(
                key = "foo",
                value =
                    PolicyVariableDynamic(
                        resolvers = listOf(PolicyVariableResolverRef(id = "birthdayResolver"))),
                failOnMissingKey = true,
                failOnNullSource = true)
        ExampleWriter.save(folder, "policy-action-save-dynamic", json.encodeToString(example))
      }

      test("Managed PolicyActionSave") {
        val example =
            PolicyActionSave(
                id = "polAct1",
                version = SemVer(1, 2, 3),
                description = "This is a managed PolicyActionSave",
                labels = listOf("label1"),
                key = "foo",
                value =
                    PolicyVariableDynamic(
                        resolvers = listOf(PolicyVariableResolverRef(id = "birthdayResolver"))),
                failOnMissingKey = true,
                failOnNullSource = true,
            )
        ExampleWriter.save(folder, "policy-action-save-managed", json.encodeToString(example))
      }

      test("PolicyActionClear") {
        val example =
            PolicyActionClear(
                key = "foo",
                failOnMissingKey = true,
            )
        ExampleWriter.save(folder, "policy-action-clear", json.encodeToString(example))
      }

      test("Managed PolicyActionClear") {
        val example =
            PolicyActionClear(
                id = "polAct1",
                version = SemVer(1, 2, 3),
                description = "This is a managed PolicyActionClear",
                labels = listOf("label1"),
                key = "foo",
                failOnMissingKey = true,
            )
        ExampleWriter.save(folder, "policy-action-clear-managed", json.encodeToString(example))
      }

      test("PolicyActionJsonMerge") {
        val example =
            PolicyActionJsonMerge(
                key = "someJson",
                source = PolicyVariableStatic(value = """{"foo":"bar"}"""),
                merge = PolicyVariableStatic(value = """{"foo":"baz"}"""),
            )
        ExampleWriter.save(folder, "policy-action-json-merge", json.encodeToString(example))
      }

      test("Managed PolicyActionJsonMerge") {
        val example =
            PolicyActionJsonMerge(
                id = "polAct1",
                version = SemVer(1, 2, 3),
                description = "This is a managed PolicyActionJsonMerge",
                labels = listOf("label1"),
                key = "someJson",
                source = PolicyVariableRef(id = "polVar1"),
                merge = PolicyVariableStatic(value = """{"foo":"baz"}"""),
                failOnNullSource = true,
                failOnMissingKey = true)
        ExampleWriter.save(folder, "policy-action-json-merge-managed", json.encodeToString(example))
      }

      test("PolicyActionJsonPatch") {
        val example =
            PolicyActionJsonPatch(
                key = "someJson",
                source = PolicyVariableStatic(value = """{"foo":["bar","baz"],"foo2":"baz"}"""),
                patch =
                    PolicyVariableStatic(
                        value = """[{"op":"replace","path":"/foo","value":["bar"]}]"""),
            )
        ExampleWriter.save(folder, "policy-action-json-patch", json.encodeToString(example))
      }

      test("Managed PolicyActionJsonPatch") {
        val example =
            PolicyActionJsonPatch(
                id = "polAct1",
                version = SemVer(1, 2, 3),
                description = "This is a managed PolicyActionJsonPatch",
                labels = listOf("label1"),
                key = "someJson",
                source = PolicyVariableRef(id = "polVar1"),
                patch =
                    PolicyVariableStatic(
                        value = """[{"op":"replace","path":"/foo","value":["bar"]}]"""),
                failOnNullSource = true,
                failOnMissingKey = true)
        ExampleWriter.save(folder, "policy-action-json-patch-managed", json.encodeToString(example))
      }
    })
