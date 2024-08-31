package io.github.ivsokol.poe.variable

import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.TextNode
import io.github.ivsokol.poe.DefaultObjectMapper
import io.github.ivsokol.poe.Options
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.*

class VariableValueSmartCastTest :
    DescribeSpec({
      val options = Options(zoneId = ZoneId.of("CET"))
      val objectMapper = DefaultObjectMapper()

      describe("null and unknown") {
        it("null is not supported") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.NULL,
                    VariableValue(VariableRuntimeTypeEnum.STRING, "foo"),
                    options)
              }
          exception.message shouldBe "Cannot cast null"
        }
        it("unknown is not supported") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.UNKNOWN,
                    VariableValue(VariableRuntimeTypeEnum.STRING, "foo"),
                    options)
              }
          exception.message shouldBe "Cannot cast unknown"
        }
      }

      describe("string") {
        it("string should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(VariableRuntimeTypeEnum.STRING, "string"),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, "string")
        }

        it("int should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(VariableRuntimeTypeEnum.INT, 1),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, "1")
        }

        it("double should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(VariableRuntimeTypeEnum.DOUBLE, 1.0),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, "1.0")
        }

        it("long should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(VariableRuntimeTypeEnum.LONG, 1L),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, "1")
        }

        it("float should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(VariableRuntimeTypeEnum.FLOAT, 1.0f),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, "1.0")
        }

        it("bigDecimal should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal.valueOf(1.0)),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, "1.0")
        }

        it("boolean should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, "true")
        }

        it("date should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(VariableRuntimeTypeEnum.DATE, LocalDate.parse("2020-01-01")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, "2020-01-01")
        }

        it("datetime should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(
                      VariableRuntimeTypeEnum.DATE_TIME,
                      OffsetDateTime.parse("2020-01-01T00:00:00+01:00")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, "2020-01-01T00:00:00+01:00")
        }

        it("time should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(VariableRuntimeTypeEnum.TIME, LocalTime.parse("00:00:00")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, "00:00:00")
        }

        it("period should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(VariableRuntimeTypeEnum.PERIOD, Period.parse("P1D")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, "P1D")
        }

        it("duration should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(VariableRuntimeTypeEnum.DURATION, Duration.parse("PT1H")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, "PT1H")
        }

        it("jsonNode string should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(
                      VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree(""""string"""")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, "string")
        }

        it("jsonNode number should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree("""1""")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, "1")
        }

        it("objectNode should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(
                      VariableRuntimeTypeEnum.OBJECT_NODE,
                      objectMapper.readTree("""{"foo":"bar"}""")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, """{"foo":"bar"}""")
        }

        it("arrayNode should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(
                      VariableRuntimeTypeEnum.ARRAY_NODE,
                      objectMapper.readTree("""["foo","bar"]""")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, """["foo","bar"]""")
        }

        it("array should return string") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.STRING,
                  VariableValue(VariableRuntimeTypeEnum.ARRAY, listOf("foo", "bar")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, """["foo","bar"]""")
        }

        it("null should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.STRING,
                    VariableValue(VariableRuntimeTypeEnum.NULL, null),
                    options)
              }
          exception.message shouldBe "Cannot cast NULL to string"
        }
        it("unknown should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.STRING,
                    VariableValue(VariableRuntimeTypeEnum.UNKNOWN, null),
                    options)
              }
          exception.message shouldBe "Cannot cast UNKNOWN to string"
        }
      }

      describe("date") {
        it("string should return date") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DATE,
                  VariableValue(VariableRuntimeTypeEnum.STRING, "2020-01-01"),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.DATE, LocalDate.parse("2020-01-01"))
        }
        it("jsonNode string should return date") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DATE,
                  VariableValue(
                      VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree(""""2020-01-01"""")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.DATE, LocalDate.parse("2020-01-01"))
        }
        it("date should return date") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DATE,
                  VariableValue(VariableRuntimeTypeEnum.DATE, LocalDate.parse("2020-01-01")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.DATE, LocalDate.parse("2020-01-01"))
        }
        it("dateTime should return date") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DATE,
                  VariableValue(
                      VariableRuntimeTypeEnum.DATE_TIME,
                      OffsetDateTime.parse("2020-01-01T00:00:00+01:00")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.DATE, LocalDate.parse("2020-01-01"))
        }
        it("others should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.DATE,
                    VariableValue(VariableRuntimeTypeEnum.INT, 1),
                    options)
              }
          exception.message shouldBe "Cannot cast INT to date"
        }
      }

      describe("dateTime") {
        it("string should return dateTime") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DATE_TIME,
                  VariableValue(VariableRuntimeTypeEnum.STRING, "2020-01-01T00:00:00+01:00"),
                  options)
          result shouldBe
              VariableValue(
                  VariableRuntimeTypeEnum.DATE_TIME,
                  OffsetDateTime.parse("2020-01-01T00:00:00+01:00"))
        }
        it("jsonNode string should return dateTime") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DATE_TIME,
                  VariableValue(
                      VariableRuntimeTypeEnum.JSON_NODE,
                      objectMapper.readTree(""""2020-01-01T00:00:00+01:00"""")),
                  options)
          result shouldBe
              VariableValue(
                  VariableRuntimeTypeEnum.DATE_TIME,
                  OffsetDateTime.parse("2020-01-01T00:00:00+01:00"))
        }
        it("date should return dateTime") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DATE_TIME,
                  VariableValue(VariableRuntimeTypeEnum.DATE, LocalDate.parse("2020-01-01")),
                  options)
          result shouldBe
              VariableValue(
                  VariableRuntimeTypeEnum.DATE_TIME,
                  OffsetDateTime.parse("2020-01-01T00:00:00+01:00"))
        }
        it("dateTime should return dateTime") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DATE_TIME,
                  VariableValue(
                      VariableRuntimeTypeEnum.DATE_TIME,
                      OffsetDateTime.parse("2020-01-01T00:00:00+01:00")),
                  options)
          result shouldBe
              VariableValue(
                  VariableRuntimeTypeEnum.DATE_TIME,
                  OffsetDateTime.parse("2020-01-01T00:00:00+01:00"))
        }
        it("others should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.DATE_TIME,
                    VariableValue(VariableRuntimeTypeEnum.INT, 1),
                    options)
              }
          exception.message shouldBe "Cannot cast INT to dateTime"
        }
      }

      describe("time") {
        it("string should return time") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.TIME,
                  VariableValue(VariableRuntimeTypeEnum.STRING, "00:00:00"),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.TIME, LocalTime.parse("00:00:00"))
        }
        it("jsonNode string should return time") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.TIME,
                  VariableValue(
                      VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree(""""00:00:00"""")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.TIME, LocalTime.parse("00:00:00"))
        }
        it("time should return time") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.TIME,
                  VariableValue(VariableRuntimeTypeEnum.TIME, LocalTime.parse("00:00:00")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.TIME, LocalTime.parse("00:00:00"))
        }
        it("dateTime should return time") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.TIME,
                  VariableValue(
                      VariableRuntimeTypeEnum.DATE_TIME,
                      OffsetDateTime.parse("2020-01-01T00:00:00+01:00")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.TIME, LocalTime.parse("00:00:00"))
        }
        it("others should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.TIME,
                    VariableValue(VariableRuntimeTypeEnum.INT, 1),
                    options)
              }
          exception.message shouldBe "Cannot cast INT to time"
        }
      }

      describe("period") {
        it("string should return period") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.PERIOD,
                  VariableValue(VariableRuntimeTypeEnum.STRING, "P1Y2M3D"),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.PERIOD, Period.parse("P1Y2M3D"))
        }
        it("jsonNode string should return period") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.PERIOD,
                  VariableValue(
                      VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree(""""P1Y2M3D"""")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.PERIOD, Period.parse("P1Y2M3D"))
        }
        it("period should return period") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.PERIOD,
                  VariableValue(VariableRuntimeTypeEnum.PERIOD, Period.parse("P1Y2M3D")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.PERIOD, Period.parse("P1Y2M3D"))
        }
        it("duration should return period") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.PERIOD,
                  VariableValue(VariableRuntimeTypeEnum.DURATION, Duration.parse("P500DT34H5M6S")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.PERIOD, Period.parse("P501D"))
        }
        it("others should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.PERIOD,
                    VariableValue(VariableRuntimeTypeEnum.INT, 1),
                    options)
              }
          exception.message shouldBe "Cannot cast INT to period"
        }
      }

      describe("duration") {
        it("string should return duration") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DURATION,
                  VariableValue(VariableRuntimeTypeEnum.STRING, "P500DT34H5M6S"),
                  options)
          result shouldBe
              VariableValue(VariableRuntimeTypeEnum.DURATION, Duration.parse("P500DT34H5M6S"))
        }
        it("jsonNode string should return duration") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DURATION,
                  VariableValue(
                      VariableRuntimeTypeEnum.JSON_NODE,
                      objectMapper.readTree(""""P500DT34H5M6S"""")),
                  options)
          result shouldBe
              VariableValue(VariableRuntimeTypeEnum.DURATION, Duration.parse("P500DT34H5M6S"))
        }
        it("duration should return duration") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DURATION,
                  VariableValue(VariableRuntimeTypeEnum.DURATION, Duration.parse("P500DT34H5M6S")),
                  options)
          result shouldBe
              VariableValue(VariableRuntimeTypeEnum.DURATION, Duration.parse("P500DT34H5M6S"))
        }
        it("others should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.DURATION,
                    VariableValue(VariableRuntimeTypeEnum.INT, 1),
                    options)
              }
          exception.message shouldBe "Cannot cast INT to duration"
        }
      }

      describe("long") {
        it("string should return long") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.LONG,
                  VariableValue(VariableRuntimeTypeEnum.STRING, "1"),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.LONG, 1L)
        }
        it("jsonNode number should return long") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.LONG,
                  VariableValue(VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree("""1""")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.LONG, 1L)
        }
        it("long should return long") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.LONG,
                  VariableValue(VariableRuntimeTypeEnum.LONG, 1L),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.LONG, 1L)
        }
        it("int should return long") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.LONG,
                  VariableValue(VariableRuntimeTypeEnum.INT, 1),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.LONG, 1L)
        }
        it("double should return long") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.LONG,
                  VariableValue(VariableRuntimeTypeEnum.DOUBLE, 1.0),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.LONG, 1L)
        }
        it("float should return long") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.LONG,
                  VariableValue(VariableRuntimeTypeEnum.FLOAT, 1.0f),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.LONG, 1L)
        }
        it("bigDecimal should return long") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.LONG,
                  VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal.ONE),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.LONG, 1L)
        }
        it("others should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.LONG,
                    VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true),
                    options)
              }
          exception.message shouldBe "Cannot cast BOOLEAN to long"
        }
      }

      describe("int") {
        it("string should return int") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.INT,
                  VariableValue(VariableRuntimeTypeEnum.STRING, "1"),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.INT, 1)
        }
        it("jsonNode number should return int") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.INT,
                  VariableValue(VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree("""1""")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.INT, 1)
        }
        it("long should return int") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.INT,
                  VariableValue(VariableRuntimeTypeEnum.LONG, 1L),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.INT, 1)
        }
        it("int should return int") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.INT,
                  VariableValue(VariableRuntimeTypeEnum.INT, 1),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.INT, 1)
        }
        it("double should return int") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.INT,
                  VariableValue(VariableRuntimeTypeEnum.DOUBLE, 1.0),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.INT, 1)
        }
        it("float should return int") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.INT,
                  VariableValue(VariableRuntimeTypeEnum.FLOAT, 1.0f),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.INT, 1)
        }
        it("bigDecimal should return int") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.INT,
                  VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal.ONE),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.INT, 1)
        }
        it("others should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.INT,
                    VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true),
                    options)
              }
          exception.message shouldBe "Cannot cast BOOLEAN to int"
        }
      }

      describe("double") {
        it("string should return double") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DOUBLE,
                  VariableValue(VariableRuntimeTypeEnum.STRING, "1.2"),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.DOUBLE, 1.2)
        }
        it("jsonNode number should return double") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DOUBLE,
                  VariableValue(
                      VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree("""1.1""")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.DOUBLE, 1.1)
        }
        it("long should return double") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DOUBLE,
                  VariableValue(VariableRuntimeTypeEnum.LONG, 1L),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.DOUBLE, 1.0)
        }
        it("int should return double") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DOUBLE,
                  VariableValue(VariableRuntimeTypeEnum.INT, 1),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.DOUBLE, 1.0)
        }
        it("double should return double") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DOUBLE,
                  VariableValue(VariableRuntimeTypeEnum.DOUBLE, 1.0),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.DOUBLE, 1.0)
        }
        it("float should return double") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DOUBLE,
                  VariableValue(VariableRuntimeTypeEnum.FLOAT, 1.0f),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.DOUBLE, 1.0)
        }
        it("bigDecimal should return double") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.DOUBLE,
                  VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal.ONE),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.DOUBLE, 1.0)
        }
        it("others should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.DOUBLE,
                    VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true),
                    options)
              }
          exception.message shouldBe "Cannot cast BOOLEAN to double"
        }
      }

      describe("float") {
        it("string should return float") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.FLOAT,
                  VariableValue(VariableRuntimeTypeEnum.STRING, "1.2"),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.FLOAT, 1.2f)
        }
        it("jsonNode number should return float") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.FLOAT,
                  VariableValue(
                      VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree("""1.1""")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.FLOAT, 1.1f)
        }
        it("long should return float") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.FLOAT,
                  VariableValue(VariableRuntimeTypeEnum.LONG, 1L),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.FLOAT, 1.0f)
        }
        it("int should return float") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.FLOAT,
                  VariableValue(VariableRuntimeTypeEnum.INT, 1),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.FLOAT, 1.0f)
        }
        it("double should return float") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.FLOAT,
                  VariableValue(VariableRuntimeTypeEnum.DOUBLE, 1.0),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.FLOAT, 1.0f)
        }
        it("float should return float") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.FLOAT,
                  VariableValue(VariableRuntimeTypeEnum.FLOAT, 1.0f),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.FLOAT, 1.0f)
        }
        it("bigDecimal should return float") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.FLOAT,
                  VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal.ONE),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.FLOAT, 1.0f)
        }
        it("others should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.FLOAT,
                    VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true),
                    options)
              }
          exception.message shouldBe "Cannot cast BOOLEAN to float"
        }
      }

      describe("bigDecimal") {
        it("string should return bigDecimal") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.BIG_DECIMAL,
                  VariableValue(VariableRuntimeTypeEnum.STRING, "1.2"),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal("1.2"))
        }
        it("jsonNode number should return bigDecimal") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.BIG_DECIMAL,
                  VariableValue(
                      VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree("""1.1""")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal("1.1"))
        }
        it("long should return bigDecimal") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.BIG_DECIMAL,
                  VariableValue(VariableRuntimeTypeEnum.LONG, 1L),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal.ONE)
        }
        it("int should return bigDecimal") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.BIG_DECIMAL,
                  VariableValue(VariableRuntimeTypeEnum.INT, 1),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal.ONE)
        }
        it("double should return bigDecimal") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.BIG_DECIMAL,
                  VariableValue(VariableRuntimeTypeEnum.DOUBLE, 1.0),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal("1.0"))
        }
        it("float should return bigDecimal") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.BIG_DECIMAL,
                  VariableValue(VariableRuntimeTypeEnum.FLOAT, 1.0f),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal("1.0"))
        }
        it("bigDecimal should return bigDecimal") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.BIG_DECIMAL,
                  VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal.ONE),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal.ONE)
        }
        it("others should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.BIG_DECIMAL,
                    VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true),
                    options)
              }
          exception.message
        }
      }

      describe("boolean") {
        it("string should return boolean") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.BOOLEAN,
                  VariableValue(VariableRuntimeTypeEnum.STRING, "true"),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true)
        }
        it("jsonNode boolean should return boolean") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.BOOLEAN,
                  VariableValue(
                      VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree("""true""")),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true)
        }
        it("others should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.BOOLEAN,
                    VariableValue(VariableRuntimeTypeEnum.INT, 1),
                    options)
              }
          exception.message shouldBe "Cannot cast INT to boolean"
        }
      }

      describe("jsonNode") {
        it("string should return jsonNode") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.JSON_NODE,
                  VariableValue(VariableRuntimeTypeEnum.STRING, "true"),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.JSON_NODE, TextNode.valueOf("true"))
        }
        it("jsonNode boolean should return jsonNode") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.JSON_NODE,
                  VariableValue(
                      VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree("""true""")),
                  options)
          result shouldBe
              VariableValue(VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree("true"))
        }
        it("jsonNode number should return jsonNode") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.JSON_NODE,
                  VariableValue(
                      VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree("""1.0""")),
                  options)
          result shouldBe
              VariableValue(VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree("1.0"))
        }
        it("objectNode should return jsonNode") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.JSON_NODE,
                  VariableValue(
                      VariableRuntimeTypeEnum.OBJECT_NODE, objectMapper.createObjectNode()),
                  options)
          result shouldBe
              VariableValue(VariableRuntimeTypeEnum.JSON_NODE, objectMapper.createObjectNode())
        }
        it("arrayNode should return jsonNode") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.JSON_NODE,
                  VariableValue(VariableRuntimeTypeEnum.ARRAY_NODE, objectMapper.createArrayNode()),
                  options)
          result shouldBe
              VariableValue(VariableRuntimeTypeEnum.JSON_NODE, objectMapper.createArrayNode())
        }
        it("null should return jsonNode") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.JSON_NODE,
                  VariableValue(VariableRuntimeTypeEnum.NULL, null),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.JSON_NODE, NullNode.instance)
        }
        it("unknown should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.JSON_NODE,
                    VariableValue(VariableRuntimeTypeEnum.UNKNOWN, 1),
                    options)
              }
          exception.message shouldBe "Cannot cast UNKNOWN to JsonNode"
        }
      }

      describe("objectNode") {
        it("jsonNode object should return objectNode") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.OBJECT_NODE,
                  VariableValue(
                      VariableRuntimeTypeEnum.JSON_NODE,
                      objectMapper.readTree("""{"value":true}""")),
                  options)
          result shouldBe
              VariableValue(
                  VariableRuntimeTypeEnum.OBJECT_NODE,
                  objectMapper.createObjectNode().put("value", true))
        }
        it("jsonNode nonObject should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.OBJECT_NODE,
                    VariableValue(
                        VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree("""1.0""")),
                    options)
              }
          exception.message shouldBe "Cannot cast JSON_NODE to ObjectNode"
        }
        it("objectNode should return objectNode") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.OBJECT_NODE,
                  VariableValue(
                      VariableRuntimeTypeEnum.OBJECT_NODE, objectMapper.createObjectNode()),
                  options)
          result shouldBe
              VariableValue(VariableRuntimeTypeEnum.OBJECT_NODE, objectMapper.createObjectNode())
        }
        it("string should return objectNode") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.OBJECT_NODE,
                  VariableValue(VariableRuntimeTypeEnum.STRING, """{"value":true}"""),
                  options)
          result shouldBe
              VariableValue(
                  VariableRuntimeTypeEnum.OBJECT_NODE, objectMapper.readTree("""{"value":true}"""))
        }
        it("bad string should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.OBJECT_NODE,
                    VariableValue(VariableRuntimeTypeEnum.STRING, """"value""""),
                    options)
              }
          exception.message shouldBe "Cannot cast STRING to ObjectNode"
        }
        it("others should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.OBJECT_NODE,
                    VariableValue(VariableRuntimeTypeEnum.INT, 1),
                    options)
              }
          exception.message shouldBe "Cannot cast INT to ObjectNode"
        }
      }

      describe("arrayNode") {
        it("jsonNode array should return arrayNode") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.ARRAY_NODE,
                  VariableValue(
                      VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree("""[1,2,3]""")),
                  options)
          result shouldBe
              VariableValue(
                  VariableRuntimeTypeEnum.ARRAY_NODE,
                  objectMapper.createArrayNode().add(1).add(2).add(3))
        }
        it("jsonNode nonArray should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.ARRAY_NODE,
                    VariableValue(
                        VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree("""1.0""")),
                    options)
              }
          exception.message shouldBe "Cannot cast JSON_NODE to ArrayNode"
        }
        it("arrayNode should return arrayNode") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.ARRAY_NODE,
                  VariableValue(VariableRuntimeTypeEnum.ARRAY_NODE, objectMapper.createArrayNode()),
                  options)
          result shouldBe
              VariableValue(VariableRuntimeTypeEnum.ARRAY_NODE, objectMapper.createArrayNode())
        }
        it("string should return ArrayNode") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.ARRAY_NODE,
                  VariableValue(VariableRuntimeTypeEnum.STRING, """["a","b","c"]"""),
                  options)
          result shouldBe
              VariableValue(
                  VariableRuntimeTypeEnum.ARRAY_NODE, objectMapper.readTree("""["a","b","c"]"""))
        }
        it("bad string should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.ARRAY_NODE,
                    VariableValue(VariableRuntimeTypeEnum.STRING, """"value""""),
                    options)
              }
          exception.message shouldBe "Cannot cast STRING to ArrayNode"
        }
        it("array should pass") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.ARRAY_NODE,
                  VariableValue(VariableRuntimeTypeEnum.ARRAY, listOf(1, 2, 3)),
                  options)
          result shouldBe
              VariableValue(
                  VariableRuntimeTypeEnum.ARRAY_NODE, objectMapper.readTree("""[1,2,3]"""))
        }
        it("others should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.ARRAY_NODE,
                    VariableValue(VariableRuntimeTypeEnum.INT, 1),
                    options)
              }
          exception.message shouldBe "Cannot cast INT to ArrayNode"
        }
      }

      describe("array") {
        it("array should return array") {
          val result =
              cast(
                  VariableRuntimeTypeEnum.ARRAY,
                  VariableValue(VariableRuntimeTypeEnum.ARRAY, listOf(1, 2, 3)),
                  options)
          result shouldBe VariableValue(VariableRuntimeTypeEnum.ARRAY, listOf(1, 2, 3))
        }
        it("others should throw") {
          val exception =
              shouldThrow<IllegalArgumentException> {
                cast(
                    VariableRuntimeTypeEnum.ARRAY,
                    VariableValue(VariableRuntimeTypeEnum.INT, 1),
                    options)
              }
          exception.message shouldBe "Cannot cast INT to array"
        }
      }
    })
