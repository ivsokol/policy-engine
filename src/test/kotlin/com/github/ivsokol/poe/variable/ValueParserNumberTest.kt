package com.github.ivsokol.poe.variable

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.DoubleNode
import com.github.ivsokol.poe.DefaultObjectMapper
import com.github.ivsokol.poe.Options
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ValueParserNumberTest :
    DescribeSpec({
      val options = Options()
      val objectMapper = DefaultObjectMapper()

      describe("number parser") {
        it("should return NullVariableValue for non-number values") {
          val value = "fooBar"
          val type = VariableValueTypeEnum.NUMBER
          val format = VariableValueFormatEnum.DATE
          val result = parseValue(value, type, format, options)
          result shouldBe NullVariableValue()
        }

        it("should return NullVariableValue for wrong format value") {
          val value = 42
          val type = VariableValueTypeEnum.NUMBER
          val format = VariableValueFormatEnum.DATE
          val result = parseValue(value, type, format, options)
          result shouldBe NullVariableValue()
        }

        it("parseValue should return NullVariableValue for wrong format type") {
          val value = objectMapper.readTree("42")
          val type = VariableValueTypeEnum.NUMBER
          val format = VariableValueFormatEnum.DATE

          val result = parseValue(value, type, format, options)

          result shouldBe NullVariableValue()
        }

        it("parseValue should return VariableValue of type DOUBLE when value is Double") {
          val value = 42.toDouble()
          val type = VariableValueTypeEnum.NUMBER
          val format = null

          val result = parseValue(value, type, format, options)

          result.type shouldBe VariableRuntimeTypeEnum.DOUBLE
          result.body shouldBe value
        }

        it("parseValue should return VariableValue of type DOUBLE when value is string") {
          val value = "42"
          val type = VariableValueTypeEnum.NUMBER
          val format = null

          val result = parseValue(value, type, format, options)

          result.type shouldBe VariableRuntimeTypeEnum.DOUBLE
          result.body shouldBe value.toDouble()
        }

        it(
            "parseValue should return VariableValue of type DOUBLE when value is string of specific format") {
              val value = "42e+2"
              val type = VariableValueTypeEnum.NUMBER
              val format = null

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.DOUBLE
              result.body shouldBe value.toDouble()
            }

        it("parseValue should return VariableValue of type DOUBLE when value is Float") {
          val value = 42.toFloat()
          val type = VariableValueTypeEnum.NUMBER
          val format = null

          val result = parseValue(value, type, format, options)

          result.type shouldBe VariableRuntimeTypeEnum.DOUBLE
          result.body shouldBe value
        }

        it(
            "parseValue should return VariableValue of type DOUBLE when value is Float and format is DOUBLE") {
              val value = 42.toFloat()
              val type = VariableValueTypeEnum.NUMBER
              val format = VariableValueFormatEnum.DOUBLE

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.DOUBLE
              result.body shouldBe value
            }

        it(
            "parseValue should return VariableValue of type FLOAT when value is Float and format is FLOAT") {
              val value = 42.toFloat()
              val type = VariableValueTypeEnum.NUMBER
              val format = VariableValueFormatEnum.FLOAT

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.FLOAT
              result.body shouldBe value
            }

        it(
            "parseValue should return VariableValue of type FLOAT when value is string and format is FLOAT") {
              val value = "42"
              val type = VariableValueTypeEnum.NUMBER
              val format = VariableValueFormatEnum.FLOAT

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.FLOAT
              result.body shouldBe value.toFloat()
            }

        it(
            "parseValue should return VariableValue of type BIG_DECIMAL when value is Float and format is BIG_DECIMAL") {
              val value = 42.toFloat()
              val type = VariableValueTypeEnum.NUMBER
              val format = VariableValueFormatEnum.BIG_DECIMAL

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.BIG_DECIMAL
              result.body shouldBe value.toBigDecimal()
            }

        it(
            "parseValue should return VariableValue of type BIG_DECIMAL when value is string and format is BIG_DECIMAL") {
              val value = "42"
              val type = VariableValueTypeEnum.NUMBER
              val format = VariableValueFormatEnum.BIG_DECIMAL

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.BIG_DECIMAL
              result.body shouldBe value.toBigDecimal()
            }

        it(
            "parseValue should return VariableValue of type BIG_DECIMAL when value is special string and format is BIG_DECIMAL") {
              val value = "42e+2"
              val type = VariableValueTypeEnum.NUMBER
              val format = VariableValueFormatEnum.BIG_DECIMAL

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.BIG_DECIMAL
              result.body shouldBe value.toBigDecimal()
            }

        it(
            "parseValue should return VariableValue of type JSON_NODE when value is JsonNode and format is JSON") {
              val value = objectMapper.readTree("42")
              val type = VariableValueTypeEnum.NUMBER
              val format = VariableValueFormatEnum.JSON

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.JSON_NODE
              result.body shouldBe value
            }

        it(
            "parseValue should return VariableValue of type JSON_NODE when value is Double and format is JSON") {
              val value = 42.toDouble()
              val type = VariableValueTypeEnum.NUMBER
              val format = VariableValueFormatEnum.JSON

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.JSON_NODE
              (result.body as DoubleNode).asDouble() shouldBe 42.toDouble()
              (result.body as JsonNode).isDouble shouldBe true
            }
      }
    })
