package com.github.ivsokol.poe.variable

import com.github.ivsokol.poe.DefaultObjectMapper
import com.github.ivsokol.poe.Options
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ValueParserJsonTest :
    DescribeSpec({
      val options = Options()
      val objectMapper = DefaultObjectMapper()

      describe("object parser") {
        it("should return NullVariableValue for non-object values and format JSON") {
          val value = "fooBar"
          val type = VariableValueTypeEnum.OBJECT
          val format = VariableValueFormatEnum.JSON
          val result = parseValue(value, type, format, options)
          result shouldBe NullVariableValue()
        }

        it("should return NullVariableValue for non-object values and format null") {
          val value = "fooBar"
          val type = VariableValueTypeEnum.OBJECT
          val format = VariableValueFormatEnum.JSON
          val result = parseValue(value, type, format, options)
          result shouldBe NullVariableValue()
        }

        it("should return NullVariableValue for wrong format value") {
          val value = objectMapper.readTree("42")
          val type = VariableValueTypeEnum.OBJECT
          val format = VariableValueFormatEnum.DATE
          val result = parseValue(value, type, format, options)
          result shouldBe NullVariableValue()
        }

        it(
            "should return VariableValue of type OBJECT_NODE when value is ObjectNode and format is JSON") {
              val value = objectMapper.readTree("""{"foo": "bar"}""")
              val type = VariableValueTypeEnum.OBJECT
              val format = VariableValueFormatEnum.JSON
              val result = parseValue(value, type, format, options)
              result shouldBe VariableValue(VariableRuntimeTypeEnum.OBJECT_NODE, value)
            }

        it(
            "should return VariableValue of type OBJECT_NODE when value is ObjectNode and format is null") {
              val value = objectMapper.readTree("""{"foo": "bar"}""")
              val type = VariableValueTypeEnum.OBJECT
              val format = null
              val result = parseValue(value, type, format, options)
              result shouldBe VariableValue(VariableRuntimeTypeEnum.OBJECT_NODE, value)
            }
      }

      describe("array parser") {
        it("should return NullVariableValue for non-array values and format JSON") {
          val value = "fooBar"
          val type = VariableValueTypeEnum.ARRAY
          val format = VariableValueFormatEnum.JSON
          val result = parseValue(value, type, format, options)
          result shouldBe NullVariableValue()
        }

        it("should return NullVariableValue for non-array values and format null") {
          val value = "fooBar"
          val type = VariableValueTypeEnum.ARRAY
          val format = null
          val result = parseValue(value, type, format, options)
          result shouldBe NullVariableValue()
        }

        it("should return NullVariableValue for wrong format value") {
          val value = objectMapper.readTree("42")
          val type = VariableValueTypeEnum.ARRAY
          val format = VariableValueFormatEnum.DATE
          val result = parseValue(value, type, format, options)
          result shouldBe NullVariableValue()
        }

        it(
            "should return VariableValue of type ARRAY_NODE when value is ArrayNode and format is JSON") {
              val value = objectMapper.readTree("""[{"foo": "bar"}]""")
              val type = VariableValueTypeEnum.ARRAY
              val format = VariableValueFormatEnum.JSON
              val result = parseValue(value, type, format, options)
              result shouldBe VariableValue(VariableRuntimeTypeEnum.ARRAY_NODE, value)
            }

        it(
            "should return VariableValue of type ARRAY_NODE when value is ArrayNode and format is null") {
              val value = objectMapper.readTree("""[{"foo": "bar"}]""")
              val type = VariableValueTypeEnum.ARRAY
              val format = null
              val result = parseValue(value, type, format, options)
              result shouldBe VariableValue(VariableRuntimeTypeEnum.ARRAY_NODE, value)
            }
      }
    })
