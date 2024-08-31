package io.github.ivsokol.poe.variable

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import java.math.BigDecimal
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

@Serializable private data class FooBigDecimal(@Contextual val foo: BigDecimal?)

@OptIn(ExperimentalSerializationApi::class)
@Suppress("UNCHECKED_CAST")
class BigDecimalSerializerTest :
    FunSpec({
      val module = SerializersModule {
        contextual(BigDecimal::class, BigDecimalSerializer as KSerializer<BigDecimal>)
      }

      val json = Json { serializersModule = module }

      test("get descriptor") {
        val descriptor = BigDecimalSerializer.descriptor
        descriptor.serialName shouldBe "BigDecimal?"
        descriptor.isNullable shouldBe true
      }

      test("should serialize null to null node") {
        val given = FooBigDecimal(null)

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":null}"""
      }

      test("should serialize to BigDecimal node") {
        val given = FooBigDecimal(BigDecimal.valueOf(1.234))

        val actual = json.encodeToString(given)
        actual shouldEqualJson """{"foo":"1.234"}"""
      }

      test("should deserialize from BigDecimal node") {
        val given = """{"foo":"1.234"}"""
        val actual = json.decodeFromString<FooBigDecimal>(given)
        val expected = FooBigDecimal(BigDecimal.valueOf(1.234))
        actual shouldBeEqual expected
      }

      test("should deserialize from null node") {
        val given = """{"foo":null}"""
        val actual = json.decodeFromString<FooBigDecimal>(given)
        val expected = FooBigDecimal(null)
        actual shouldBeEqual expected
      }

      test("should throw exception from wrong node") {
        val given = """{"foo":["1","2","3"]}"""
        val exception =
            shouldThrow<IllegalArgumentException> { json.decodeFromString<FooBigDecimal>(given) }
        exception.message shouldStartWith "Unexpected JSON token"
      }
    })
