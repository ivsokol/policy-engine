package io.github.ivsokol.poe.variable

import io.github.ivsokol.poe.DefaultObjectMapper
import io.github.ivsokol.poe.Options
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ValueParserIntTest :
    DescribeSpec({
      val options = Options()
      val objectMapper = DefaultObjectMapper()

      describe("int parser") {
        it("should return NullVariableValue for non-int values") {
          val value = "fooBar"
          val type = VariableValueTypeEnum.INT
          val format = VariableValueFormatEnum.DATE
          val result = parseValue(value, type, format, options)
          result shouldBe NullVariableValue()
        }

        it("should ignore wrong format value") {
          val value = 42
          val type = VariableValueTypeEnum.INT
          val format = VariableValueFormatEnum.DATE
          val result = parseValue(value, type, format, options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.INT, 42)
        }

        it("parseValue should return NullVariableValue for wrong format type") {
          val value = objectMapper.readTree("42")
          val type = VariableValueTypeEnum.INT
          val format = VariableValueFormatEnum.DATE

          val result = parseValue(value, type, format, options)

          result shouldBe NullVariableValue()
        }

        it("parseValue should return VariableValue of type INT when type is INT") {
          val value = 42
          val type = VariableValueTypeEnum.INT
          val format = null

          val result = parseValue(value, type, format, options)

          result.type shouldBe VariableRuntimeTypeEnum.INT
          result.body shouldBe value
        }

        it(
            "parseValue should return VariableValue of type INT when type is INT and value is string") {
              val value = "42"
              val type = VariableValueTypeEnum.INT
              val format = null

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.INT
              result.body shouldBe value.toInt()
            }

        it(
            "parseValue should return VariableValue of type INT when type is INT and value is JsonNode") {
              val value = objectMapper.readTree("42")
              val type = VariableValueTypeEnum.INT
              val format = null

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.INT
              result.body shouldBe value.intValue()
            }

        it(
            "parseValue should return VariableValue of type JSON_NODE when type is INT and value is string and format is JSON") {
              val value = "42"
              val type = VariableValueTypeEnum.INT
              val format = VariableValueFormatEnum.JSON

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.JSON_NODE
              result.body shouldBe objectMapper.readTree(value)
            }

        it(
            "parseValue should return VariableValue of type LONG when type is INT and format is LONG") {
              val value = Long.MAX_VALUE
              val type = VariableValueTypeEnum.INT
              val format = VariableValueFormatEnum.LONG

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.LONG
              result.body shouldBe value
            }

        it("parseValue should return NullVariableValue type is Long and format is DATE") {
          val value = Long.MAX_VALUE
          val type = VariableValueTypeEnum.INT
          val format = VariableValueFormatEnum.DATE

          val result = parseValue(value, type, format, options)

          result shouldBe NullVariableValue()
        }

        it(
            "parseValue should return VariableValue of type LONG when type is INT and format is null") {
              val value = Long.MAX_VALUE
              val type = VariableValueTypeEnum.INT
              val format = null

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.LONG
              result.body shouldBe value
            }

        it(
            "parseValue should return VariableValue of type INT when type is Long and value is JsonNode") {
              val value = objectMapper.readTree(Long.MAX_VALUE.toString())
              val type = VariableValueTypeEnum.INT
              val format = null

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.LONG
              result.body shouldBe value.longValue()
            }

        it(
            "parseValue should return VariableValue of type INT when format is Long and value is string") {
              val value = "42"
              val type = VariableValueTypeEnum.INT
              val format = VariableValueFormatEnum.LONG

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.LONG
              result.body shouldBe value.toLongOrNull()
            }
      }
    })
