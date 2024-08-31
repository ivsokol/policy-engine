package com.github.ivsokol.poe.variable

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import java.time.LocalTime
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

@Serializable private data class FooLocalTime(@Contextual val foo: LocalTime?)

@OptIn(ExperimentalSerializationApi::class)
@Suppress("UNCHECKED_CAST")
class LocalTimeSerializerTest :
    FunSpec({
      val module = SerializersModule {
        contextual(LocalTime::class, LocalTimeSerializer as KSerializer<LocalTime>)
      }

      val json = Json { serializersModule = module }

      test("get descriptor") {
        val descriptor = LocalTimeSerializer.descriptor
        descriptor.serialName shouldBe "LocalTime?"
        descriptor.isNullable shouldBe true
      }

      test("should serialize null to null node") {
        val given = FooLocalTime(null)

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":null}"""
      }

      test("should serialize to LocalTime node") {
        val given = FooLocalTime(LocalTime.of(1, 23, 50, 123456000))

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":"01:23:50.123456"}"""
      }

      test("should deserialize from LocalTime node") {
        val given = """{"foo":"01:23:50.123456"}"""
        val actual = json.decodeFromString<FooLocalTime>(given)
        val expected = FooLocalTime(LocalTime.of(1, 23, 50, 123456000))
        actual shouldBeEqual expected
      }

      test("should deserialize from partial LocalTime node") {
        val given = """{"foo":"01:23:50"}"""
        val actual = json.decodeFromString<FooLocalTime>(given)
        val expected = FooLocalTime(LocalTime.of(1, 23, 50))
        actual shouldBeEqual expected
      }

      test("should deserialize from null node") {
        val given = """{"foo":null}"""
        val actual = json.decodeFromString<FooLocalTime>(given)
        val expected = FooLocalTime(null)
        actual shouldBeEqual expected
      }

      test("should throw exception from wrong node") {
        val given = """{"foo":["1","2","3"]}"""
        val exception =
            shouldThrow<IllegalArgumentException> { json.decodeFromString<FooLocalTime>(given) }
        exception.message shouldStartWith "Unexpected JSON token"
      }
    })
