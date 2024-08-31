package com.github.ivsokol.poe.variable

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import java.time.LocalDate
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

@Serializable private data class FooLocalDate(@Contextual val foo: LocalDate?)

@OptIn(ExperimentalSerializationApi::class)
@Suppress("UNCHECKED_CAST")
class LocalDateSerializerTest :
    FunSpec({
      val module = SerializersModule {
        contextual(LocalDate::class, LocalDateSerializer as KSerializer<LocalDate>)
      }

      val json = Json { serializersModule = module }

      test("get descriptor") {
        val descriptor = LocalDateSerializer.descriptor
        descriptor.serialName shouldBe "LocalDate?"
        descriptor.isNullable shouldBe true
      }

      test("should serialize null to null node") {
        val given = FooLocalDate(null)

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":null}"""
      }

      test("should serialize to LocalDate node") {
        val given = FooLocalDate(LocalDate.parse("2024-01-23"))

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":"2024-01-23"}"""
      }

      test("should deserialize from LocalDate node") {
        val given = """{"foo":"2024-01-23"}"""
        val actual = json.decodeFromString<FooLocalDate>(given)
        val expected = FooLocalDate(LocalDate.parse("2024-01-23"))
        actual shouldBeEqual expected
      }

      test("should deserialize from null node") {
        val given = """{"foo":null}"""
        val actual = json.decodeFromString<FooLocalDate>(given)
        val expected = FooLocalDate(null)
        actual shouldBeEqual expected
      }

      test("should throw exception from wrong node") {
        val given = """{"foo":["1","2","3"]}"""
        val exception =
            shouldThrow<IllegalArgumentException> { json.decodeFromString<FooLocalDate>(given) }
        exception.message shouldStartWith "Unexpected JSON token"
      }
    })
