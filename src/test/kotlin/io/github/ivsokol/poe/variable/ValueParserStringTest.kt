package io.github.ivsokol.poe.variable

import io.github.ivsokol.poe.DefaultObjectMapper
import io.github.ivsokol.poe.Options
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.*

class ValueParserStringTest :
    DescribeSpec({
      val options = Options()
      val objectMapper = DefaultObjectMapper()

      describe("string parser") {
        it("should return NullVariableValue for non-string values") {
          val value = 42
          val type = VariableValueTypeEnum.STRING
          val format = VariableValueFormatEnum.DATE
          val result = parseValue(value, type, format, options)
          result shouldBe NullVariableValue()
        }

        it("should return NullVariableValue for wrong format value") {
          val value = "fooBar"
          val type = VariableValueTypeEnum.STRING
          val format = VariableValueFormatEnum.DATE
          val result = parseValue(value, type, format, options)
          result shouldBe NullVariableValue()
        }

        it("parseValue should return NullVariableValue for wrong format type") {
          val value = objectMapper.readTree("\"Hello, World!\"")
          val type = VariableValueTypeEnum.STRING
          val format = VariableValueFormatEnum.DATE

          val result = parseValue(value, type, format, options)

          result shouldBe NullVariableValue()
        }

        it("parseValue should return VariableValue of type STRING when type is STRING") {
          val value = "Hello, World!"
          val type = VariableValueTypeEnum.STRING
          val format = null

          val result = parseValue(value, type, format, options)

          result.type shouldBe VariableRuntimeTypeEnum.STRING
          result.body shouldBe value
        }

        it(
            "parseValue should return VariableValue of type STRING when type is STRING and value is JsonNode") {
              val value = objectMapper.readTree("\"Hello, World!\"")
              val type = VariableValueTypeEnum.STRING
              val format = null

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.STRING
              result.body shouldBe value.textValue()
            }

        it(
            "parseValue should return VariableValue of type DATE when type is DATE and format is DATE") {
              val value = "2024-07-24"
              val type = VariableValueTypeEnum.STRING
              val format = VariableValueFormatEnum.DATE

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.DATE
              result.body shouldBe LocalDate.of(2024, 7, 24)
            }

        it(
            "parseValue should return VariableValue of type DATE_TIME when type is STRING and format is DATE_TIME") {
              val value = "2024-07-24T12:34:56+01:00"
              val type = VariableValueTypeEnum.STRING
              val format = VariableValueFormatEnum.DATE_TIME

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.DATE_TIME
              result.body shouldBe
                  OffsetDateTime.of(2024, 7, 24, 12, 34, 56, 0, ZoneOffset.ofHours(1))
            }

        it(
            "parseValue should return VariableValue of type TIME when type is STRING and format is TIME") {
              val value = "12:34:56"
              val type = VariableValueTypeEnum.STRING
              val format = VariableValueFormatEnum.TIME

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.TIME
              result.body shouldBe LocalTime.of(12, 34, 56, 0)
            }

        it(
            "parseValue should return VariableValue of type STRING when type is STRING and format is PERIOD") {
              val value = "P23D"
              val type = VariableValueTypeEnum.STRING
              val format = VariableValueFormatEnum.PERIOD

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.PERIOD
              result.body shouldBe Period.ofDays(23)
            }

        it(
            "parseValue should return VariableValue of type STRING when type is STRING and format is DURATION") {
              val value = "P23DT12H34M56S"
              val type = VariableValueTypeEnum.STRING
              val format = VariableValueFormatEnum.DURATION

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.DURATION
              result.body shouldBe Duration.parse(value)
            }

        it(
            "parseValue should return VariableValue of type STRING when type is STRING and format is JSON") {
              val value = "{\"foo\":\"bar\"}"
              val type = VariableValueTypeEnum.STRING
              val format = VariableValueFormatEnum.JSON

              val result = parseValue(value, type, format, options)

              result.type shouldBe VariableRuntimeTypeEnum.JSON_NODE
              result.body shouldBe objectMapper.readTree(value)
            }

        it("parseValue should ignore bad format") {
          val value = "Hello, World!"
          val type = VariableValueTypeEnum.STRING
          val format = VariableValueFormatEnum.BIG_DECIMAL

          val result = parseValue(value, type, format, options)

          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, value)
        }
      }
    })
