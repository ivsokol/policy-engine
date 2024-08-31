package io.github.ivsokol.poe.action

import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch
import io.github.ivsokol.poe.DefaultObjectMapper
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class JsonMergeLibTest :
    FunSpec({
      val objectMapper = DefaultObjectMapper()

      test("should overwrite objects props") {
        val given = objectMapper.createObjectNode().put("foo", "bar")
        val mergeValue = objectMapper.createObjectNode().put("foo", "baz")
        val patch = JsonMergePatch.fromJson(mergeValue)
        val actual: JsonNode = patch.apply(given)
        actual.get("foo").asText() shouldBe "baz"
        actual.properties().size shouldBe 1
      }

      test("should add new object prop") {
        val given = objectMapper.createObjectNode().put("foo", "bar")
        val mergeValue = objectMapper.createObjectNode().put("foo2", "baz")
        val patch = JsonMergePatch.fromJson(mergeValue)
        val actual: JsonNode = patch.apply(given)
        actual.get("foo").asText() shouldBe "bar"
        actual.get("foo2").asText() shouldBe "baz"
        actual.properties().size shouldBe 2
      }

      test("should remove object prop") {
        val given = objectMapper.createObjectNode().put("foo", "bar")
        val mergeValue = objectMapper.readTree("""{"foo":null,"foo2":"baz"}""")
        val patch = JsonMergePatch.fromJson(mergeValue)
        val actual: JsonNode = patch.apply(given)
        actual.toString() shouldBe """{"foo2":"baz"}"""
      }

      test("should overwrite array props") {
        val given = objectMapper.readTree("""{"foo":["bar","baz"]}""")
        val mergeValue = objectMapper.readTree("""{"foo":["bar2"]}""")
        val patch = JsonMergePatch.fromJson(mergeValue)
        val actual: JsonNode = patch.apply(given)
        actual.toString() shouldBe """{"foo":["bar2"]}"""
      }

      test("should override if source is string") {
        val given = objectMapper.readTree(""""foo"""")
        val mergeValue = objectMapper.readTree("""{"foo":"bar2"}""")
        val patch = JsonMergePatch.fromJson(mergeValue)
        val actual: JsonNode = patch.apply(given)
        actual.toString() shouldBe """{"foo":"bar2"}"""
      }

      test("should override if source is array") {
        val given = objectMapper.readTree("""["foo","bar"]""")
        val mergeValue = objectMapper.readTree("""{"foo":"bar2"}""")
        val patch = JsonMergePatch.fromJson(mergeValue)
        val actual: JsonNode = patch.apply(given)
        actual.toString() shouldBe """{"foo":"bar2"}"""
      }

      test("should override if source is null") {
        val given = objectMapper.readTree("""""")
        val mergeValue = objectMapper.readTree("""{"foo":"bar2"}""")
        val patch = JsonMergePatch.fromJson(mergeValue)
        val actual: JsonNode = patch.apply(given)
        actual.toString() shouldBe """{"foo":"bar2"}"""
      }

      test("boolean merge json") {
        val given = objectMapper.readTree("""{"foo":"bar"}""")
        val mergeValue = objectMapper.readTree("""true""")
        val patch = JsonMergePatch.fromJson(mergeValue)
        val actual: JsonNode = patch.apply(given)
        actual.toString() shouldBe """true"""
      }
    })
