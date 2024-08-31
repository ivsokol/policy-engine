package com.github.ivsokol.poe.variable

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

@Serializable private data class FooOffsetDateTime(@Contextual val foo: OffsetDateTime?)

@OptIn(ExperimentalSerializationApi::class)
@Suppress("UNCHECKED_CAST")
class OffsetDateTimeSerializerTest :
    FunSpec({
      val module = SerializersModule {
        contextual(OffsetDateTime::class, OffsetDateTimeSerializer as KSerializer<OffsetDateTime>)
      }

      val json = Json { serializersModule = module }

      test("get descriptor") {
        val descriptor = OffsetDateTimeSerializer.descriptor
        descriptor.serialName shouldBe "OffsetDateTime?"
        descriptor.isNullable shouldBe true
      }

      test("should serialize null to null node") {
        val given = FooOffsetDateTime(null)

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":null}"""
      }

      test("should serialize to OffsetDateTime node") {
        val given =
            FooOffsetDateTime(
                OffsetDateTime.of(2024, 1, 23, 1, 23, 50, 123456000, ZoneOffset.ofHours(1)))

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":"2024-01-23T01:23:50.123456+01:00"}"""
      }

      test("should deserialize from OffsetDateTime node") {
        val given = """{"foo":"2024-01-23T01:23:50.123456+01:00"}"""
        val actual = json.decodeFromString<FooOffsetDateTime>(given)
        val expected =
            FooOffsetDateTime(
                OffsetDateTime.of(2024, 1, 23, 1, 23, 50, 123456000, ZoneOffset.ofHours(1)))
        actual shouldBeEqual expected
      }

      test("should deserialize from null node") {
        val given = """{"foo":null}"""
        val actual = json.decodeFromString<FooOffsetDateTime>(given)
        val expected = FooOffsetDateTime(null)
        actual shouldBeEqual expected
      }

      test("should throw exception from wrong node") {
        val given = """{"foo":["1","2","3"]}"""
        val exception =
            shouldThrow<IllegalArgumentException> {
              json.decodeFromString<FooOffsetDateTime>(given)
            }
        exception.message shouldStartWith "Unexpected JSON token"
      }
    })
