package com.github.ivsokol.poe.variable

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import java.time.Period
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

@Serializable private data class FooPeriod(@Contextual val foo: Period?)

@OptIn(ExperimentalSerializationApi::class)
@Suppress("UNCHECKED_CAST")
class PeriodSerializerTest :
    FunSpec({
      val module = SerializersModule {
        contextual(Period::class, PeriodSerializer as KSerializer<Period>)
      }

      val json = Json { serializersModule = module }

      test("get descriptor") {
        val descriptor = PeriodSerializer.descriptor
        descriptor.serialName shouldBe "Period?"
        descriptor.isNullable shouldBe true
      }

      test("should serialize null to null node") {
        val given = FooPeriod(null)

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":null}"""
      }

      test("should serialize to Period node") {
        val given = FooPeriod(Period.ofDays(23))

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":"P23D"}"""
      }

      test("should deserialize from Period node") {
        val given = """{"foo":"P23D"}"""
        val actual = json.decodeFromString<FooPeriod>(given)
        val expected = FooPeriod(Period.ofDays(23))
        actual shouldBeEqual expected
      }

      test("should deserialize from null node") {
        val given = """{"foo":null}"""
        val actual = json.decodeFromString<FooPeriod>(given)
        val expected = FooPeriod(null)
        actual shouldBeEqual expected
      }

      test("should throw exception from wrong node") {
        val given = """{"foo":["1","2","3"]}"""
        val exception =
            shouldThrow<IllegalArgumentException> { json.decodeFromString<FooPeriod>(given) }
        exception.message shouldStartWith "Unexpected JSON token"
      }
    })
