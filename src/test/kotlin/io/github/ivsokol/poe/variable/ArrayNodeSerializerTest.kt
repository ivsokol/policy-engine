package io.github.ivsokol.poe.variable

import com.fasterxml.jackson.databind.node.ArrayNode
import io.github.ivsokol.poe.DefaultObjectMapper
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

@Serializable private data class FooArrayNode(@Contextual val foo: ArrayNode?)

@OptIn(ExperimentalSerializationApi::class)
@Suppress("UNCHECKED_CAST")
class ArrayNodeSerializerTest :
    FunSpec({
      val objectMapper = DefaultObjectMapper()

      val module = SerializersModule {
        contextual(ArrayNode::class, ArrayNodeSerializer as KSerializer<ArrayNode>)
      }

      val json = Json { serializersModule = module }

      test("get descriptor") {
        val descriptor = ArrayNodeSerializer.descriptor
        descriptor.serialName shouldBe "ArrayNode?"
        descriptor.isNullable shouldBe true
      }

      test("should serialize null to null node") {
        val given = FooArrayNode(null)

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":null}"""
      }

      test("should serialize to array node") {
        val given =
            FooArrayNode(
                objectMapper.createArrayNode().let {
                  it.add("1")
                  it.add("2")
                  it.add("3")
                })

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":["1","2","3"]}"""
      }
      test("should serialize object to array node") {
        val given =
            FooArrayNode(
                objectMapper.createArrayNode().let {
                  it.add(objectMapper.createObjectNode().put("bar", "1"))
                  it.add(objectMapper.createObjectNode().put("bar", "2"))
                  it.add(objectMapper.createObjectNode().put("bar", "3"))
                })

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":[{"bar":"1"},{"bar":"2"},{"bar":"3"}]}"""
      }

      test("should deserialize from null node") {
        val given = """{"foo":null}"""
        val actual = json.decodeFromString<FooArrayNode>(given)
        val expected = FooArrayNode(null)
        actual shouldBeEqual expected
      }

      test("should deserialize from array node") {
        val given = """{"foo":["1","2","3"]}"""
        val actual = json.decodeFromString<FooArrayNode>(given)
        val expected =
            FooArrayNode(
                objectMapper.createArrayNode().let {
                  it.add("1")
                  it.add("2")
                  it.add("3")
                })
        actual shouldBeEqual expected
      }

      test("should deserialize object from array node") {
        val given = """{"foo":[{"bar":"1"},{"bar":"2"},{"bar":"3"}]}"""
        val actual = json.decodeFromString<FooArrayNode>(given)
        val expected =
            FooArrayNode(
                objectMapper.createArrayNode().let {
                  it.add(objectMapper.createObjectNode().put("bar", "1"))
                  it.add(objectMapper.createObjectNode().put("bar", "2"))
                  it.add(objectMapper.createObjectNode().put("bar", "3"))
                })
        actual shouldBeEqual expected
      }
    })
