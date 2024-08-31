package io.github.ivsokol.poe.variable

import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.ivsokol.poe.DefaultObjectMapper
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

@Serializable private data class FooObjectNode(@Contextual val foo: ObjectNode?)

@OptIn(ExperimentalSerializationApi::class)
@Suppress("UNCHECKED_CAST")
class ObjectNodeSerializerTest :
    FunSpec({
      val objectMapper = DefaultObjectMapper()

      val module = SerializersModule {
        contextual(ObjectNode::class, ObjectNodeSerializer as KSerializer<ObjectNode>)
      }

      val json = Json { serializersModule = module }

      test("get descriptor") {
        val descriptor = ObjectNodeSerializer.descriptor
        descriptor.serialName shouldBe "ObjectNode?"
        descriptor.isNullable shouldBe true
      }

      test("should serialize null to null node") {
        val given = FooObjectNode(null)

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":null}"""
      }

      test("should serialize to object node") {
        val given =
            FooObjectNode(
                objectMapper.createObjectNode().let {
                  it.put("bar", "1")
                  it.put("bar2", "2")
                  it.put("bar3", "3")
                })

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":{"bar":"1","bar2":"2","bar3":"3"}}"""
      }

      test("should serialize nested object to object node") {
        val given =
            FooObjectNode(
                objectMapper.createObjectNode().let {
                  it.putObject("bar").put("bar", "1")
                  it.putObject("bar2").put("bar", "2")
                  it
                })

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":{"bar":{"bar":"1"},"bar2":{"bar":"2"}}}"""
      }

      test("should deserialize from object node") {
        val given = """{"foo":{"bar":"1","bar2":"2","bar3":"3"}}"""
        val actual = json.decodeFromString<FooObjectNode>(given)
        val expected =
            FooObjectNode(
                objectMapper.createObjectNode().let {
                  it.put("bar", "1")
                  it.put("bar2", "2")
                  it.put("bar3", "3")
                })
        actual shouldBeEqual expected
      }

      test("should deserialize from null node") {
        val given = """{"foo":null}"""
        val actual = json.decodeFromString<FooObjectNode>(given)
        val expected = FooObjectNode(null)
        actual shouldBeEqual expected
      }

      test("should throw exception from wrong node") {
        val given = """{"foo":["1","2","3"]}"""
        val exception =
            shouldThrow<IllegalArgumentException> { json.decodeFromString<FooObjectNode>(given) }
        exception.message shouldStartWith "Unexpected JSON token"
      }

      test("should deserialize nested object to object node") {
        val given = """{"foo":{"bar":{"bar":"1"},"bar2":{"bar":"2"}}}"""
        val actual = json.decodeFromString<FooObjectNode>(given)
        val expected =
            FooObjectNode(
                objectMapper.createObjectNode().let {
                  it.putObject("bar").put("bar", "1")
                  it.putObject("bar2").put("bar", "2")
                  it
                })
        actual shouldBeEqual expected
      }
    })
