package com.github.ivsokol.poe.variable

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import java.time.Duration
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

@Serializable private data class FooDuration(@Contextual val foo: Duration?)

@OptIn(ExperimentalSerializationApi::class)
@Suppress("UNCHECKED_CAST")
class DurationSerializerTest :
    FunSpec({
      val module = SerializersModule {
        contextual(Duration::class, DurationSerializer as KSerializer<Duration>)
      }

      val json = Json { serializersModule = module }

      test("get descriptor") {
        val descriptor = DurationSerializer.descriptor
        descriptor.serialName shouldBe "java.time.Duration?"
        descriptor.isNullable shouldBe true
      }

      test("should serialize null to null node") {
        val given = FooDuration(null)

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":null}"""
      }

      test("should serialize to Duration node") {
        val given = FooDuration(Duration.parse("P23DT23H12M34.123456789S"))

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":"PT575H12M34.123456789S"}"""
      }

      test("should deserialize from Duration node") {
        val given = """{"foo":"P23DT23H12M34.123456789S"}"""
        val actual = json.decodeFromString<FooDuration>(given)
        val expected = FooDuration(Duration.parse("P23DT23H12M34.123456789S"))
        actual shouldBeEqual expected
      }

      test("should deserialize from null node") {
        val given = """{"foo":null}"""
        val actual = json.decodeFromString<FooDuration>(given)
        val expected = FooDuration(null)
        actual shouldBeEqual expected
      }

      test("should throw exception from wrong node") {
        val given = """{"foo":["1","2","3"]}"""
        val exception =
            shouldThrow<IllegalArgumentException> { json.decodeFromString<FooDuration>(given) }
        exception.message shouldStartWith "Unexpected JSON token"
      }
    })
