package com.github.ivsokol.poe.action

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException
import com.fasterxml.jackson.databind.node.TextNode
import com.github.fge.jackson.jsonpointer.JsonPointer
import com.github.fge.jsonpatch.*
import com.github.ivsokol.poe.DefaultObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class JsonPatchLibTest :
    FunSpec({
      val objectMapper = DefaultObjectMapper()
      val deepObject =
          objectMapper.readTree(
              """
        {
    "store": {
        "book": [
            {
                "category": "reference",
                "author": "Nigel Rees",
                "title": "Sayings of the Century",
                "price": 8.95
            },
            {
                "category": "fiction",
                "author": "Evelyn Waugh",
                "title": "Sword of Honour",
                "price": 12.99
            },
            {
                "category": "fiction",
                "author": "Herman Melville",
                "title": "Moby Dick",
                "isbn": "0-553-21311-3",
                "price": 8.99
            },
            {
                "category": "fiction",
                "author": "J. R. R. Tolkien",
                "title": "The Lord of the Rings",
                "isbn": "0-395-19395-8",
                "price": 22.99
            }
        ],
        "bicycle": {
            "color": "red",
            "price": 19.95
        }
    },
    "expensive": 10
}
      """
                  .trimIndent())

      test("should overwrite objects props") {
        val patch =
            JsonPatch.fromJson(
                objectMapper.readTree("""[{"op":"replace","path":"/expensive","value":20}]"""))
        val actual: JsonNode = patch.apply(deepObject)
        actual.get("expensive").asInt() shouldBe 20
      }

      test("should overwrite deep object prop") {
        val patch =
            JsonPatch.fromJson(
                objectMapper.readTree(
                    """[{"op":"replace","path":"/store/bicycle/color","value":"blue"}]"""))
        val actual: JsonNode = patch.apply(deepObject)
        actual.get("store").get("bicycle").get("color").asText() shouldBe "blue"
      }

      test("should add to deep object prop") {
        val patch =
            JsonPatch.fromJson(
                objectMapper.readTree(
                    """[{"op":"add","path":"/store/bicycle/brand","value":"Kona"}]"""))
        val actual: JsonNode = patch.apply(deepObject)
        actual.get("store").get("bicycle").get("brand").asText() shouldBe "Kona"
      }

      test("should remove from deep object prop") {
        val patch =
            JsonPatch.fromJson(
                objectMapper.readTree("""[{"op":"remove","path":"/store/bicycle/price"}]"""))
        val actual: JsonNode = patch.apply(deepObject)
        actual.get("store").get("bicycle").has("price") shouldBe false
      }

      test("should add new object prop") {
        val given = objectMapper.createObjectNode().put("foo", "bar")
        val patch = JsonPatch(listOf(AddOperation(JsonPointer.of("foo2"), TextNode("baz2"))))
        val actual: JsonNode = patch.apply(given)
        actual.get("foo").asText() shouldBe "bar"
        actual.get("foo2").asText() shouldBe "baz2"
        actual.properties().size shouldBe 2
      }

      test("should remove object prop") {
        val given = objectMapper.createObjectNode().put("foo", "bar").put("foo2", "baz")
        val patch = JsonPatch(listOf(RemoveOperation(JsonPointer.of("foo2"))))
        val actual: JsonNode = patch.apply(given)
        actual.get("foo").asText() shouldBe "bar"
        actual.properties().size shouldBe 1
      }

      test("should overwrite array") {
        val given = objectMapper.readTree("""["foo","baz"]""")
        val patch =
            JsonPatch(
                listOf(
                    ReplaceOperation(JsonPointer.empty(), objectMapper.readTree("""["foo2"]"""))))
        val actual: JsonNode = patch.apply(given)
        actual.toString() shouldBe """["foo2"]"""
      }

      test("should add to deep object array") {
        val patch =
            JsonPatch.fromJson(
                objectMapper.readTree(
                    """[{"op":"add","path":"/store/book/-","value":{
                "category": "reference",
                "author": "Edgar Allan Poe",
                "title": "The Raven",
                "price": 21.55
            }}]"""))
        val actual: JsonNode = patch.apply(deepObject)
        actual.get("store").get("book").size() shouldBe 5
        actual.get("store").get("book")[4].get("price").asDouble() shouldBe 21.55
        actual.get("store").get("book")[4].get("category").asText() shouldBe "reference"
        actual.get("store").get("book")[4].get("author").asText() shouldBe "Edgar Allan Poe"
        actual.get("store").get("book")[4].get("title").asText() shouldBe "The Raven"
      }

      test("should overwrite in deep object array") {
        val patch =
            JsonPatch.fromJson(
                objectMapper.readTree(
                    """[{"op":"replace","path":"/store/book/2","value":{
                "category": "reference",
                "author": "Edgar Allan Poe",
                "title": "The Raven",
                "price": 21.55
            }}]"""))
        val actual: JsonNode = patch.apply(deepObject)
        actual.get("store").get("book").size() shouldBe 4
        actual.get("store").get("book")[2].get("price").asDouble() shouldBe 21.55
        actual.get("store").get("book")[2].get("category").asText() shouldBe "reference"
        actual.get("store").get("book")[2].get("author").asText() shouldBe "Edgar Allan Poe"
        actual.get("store").get("book")[2].get("title").asText() shouldBe "The Raven"
      }

      test("should remove from deep object array") {
        val patch =
            JsonPatch.fromJson(
                objectMapper.readTree("""[{"op":"remove","path":"/store/book/2"}]"""))
        val actual: JsonNode = patch.apply(deepObject)
        actual.get("store").get("book").size() shouldBe 3
        actual.get("store").get("book")[2].get("price").asDouble() shouldBe 22.99
        actual.get("store").get("book")[2].get("category").asText() shouldBe "fiction"
        actual.get("store").get("book")[2].get("author").asText() shouldBe "J. R. R. Tolkien"
        actual.get("store").get("book")[2].get("title").asText() shouldBe "The Lord of the Rings"
        actual.get("store").get("book")[2].get("isbn").asText() shouldBe "0-395-19395-8"
      }

      test("should fail if deep object path doesn't exist") {
        val patch =
            JsonPatch.fromJson(
                objectMapper.readTree("""[{"op":"remove","path":"/store/book/8"}]"""))
        shouldThrow<JsonPatchException> { patch.apply(deepObject) }.message shouldContain
            "no such path in target JSON document"
      }

      test("should override if source is string") {
        val given = objectMapper.readTree(""""foo"""")
        val patch =
            JsonPatch(
                listOf(
                    ReplaceOperation(JsonPointer.empty(), objectMapper.readTree("""["foo2"]"""))))
        val actual: JsonNode = patch.apply(given)
        actual.toString() shouldBe """["foo2"]"""
      }

      test("should override if source is null") {
        val given = objectMapper.readTree("""""")
        val patch =
            JsonPatch(
                listOf(
                    ReplaceOperation(JsonPointer.empty(), objectMapper.readTree("""["foo2"]"""))))

        shouldThrow<JsonPatchException> { patch.apply(given) }.message shouldContain
            "no such path in target JSON document"
      }

      test("should fail if patch not in correct format") {
        shouldThrow<InvalidTypeIdException> {
              JsonPatch.fromJson(
                  objectMapper.readTree("""[{"op":"unknown","path":"/store/book/8"}]"""))
            }
            .message shouldContain "Could not resolve type id"
      }
    })
