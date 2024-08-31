package com.github.ivsokol.poe.variable

import com.github.ivsokol.poe.DefaultObjectMapper
import com.github.ivsokol.poe.Options
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ValueParserBooleanTest :
    DescribeSpec({
      val options = Options()
      val objectMapper = DefaultObjectMapper()

      describe("boolean parser") {
        it("should return NullVariableValue for non-boolean values") {
          val value = 42
          val type = VariableValueTypeEnum.BOOLEAN
          val format = VariableValueFormatEnum.DATE
          val result = parseValue(value, type, format, options)
          result shouldBe NullVariableValue()
        }

        it("should return VariableValue of type BOOLEAN when type is BOOLEAN") {
          val value = true
          val type = VariableValueTypeEnum.BOOLEAN
          val format = null

          val result = parseValue(value, type, format, options)

          result.type shouldBe VariableRuntimeTypeEnum.BOOLEAN
          result.body shouldBe value
        }

        it(
            "should return VariableValue of type BOOLEAN when type is BOOLEAN and value is JsonNode") {
              val value = objectMapper.readTree("true")
              val type = VariableValueTypeEnum.BOOLEAN
              val format = null

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.BOOLEAN
              result.body shouldBe value.booleanValue()
            }

        it(
            "should return VariableValue of type JSON_NODE when type is BOOLEAN and value is string and format is JSON") {
              val value = "true"
              val type = VariableValueTypeEnum.BOOLEAN
              val format = VariableValueFormatEnum.JSON

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.JSON_NODE
              result.body shouldBe objectMapper.readTree(value)
            }

        it(
            "should return VariableValue of type BOOLEAN when type is BOOLEAN and value is string true") {
              val value = "true"
              val type = VariableValueTypeEnum.BOOLEAN
              val format = null

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.BOOLEAN
              result.body shouldBe true
            }

        it(
            "should return VariableValue of type BOOLEAN when type is BOOLEAN and value is string false") {
              val value = "false"
              val type = VariableValueTypeEnum.BOOLEAN
              val format = null

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.BOOLEAN
              result.body shouldBe false
            }
      }
    })
