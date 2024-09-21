package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueFormatEnum
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.math.BigDecimal
import java.time.*

class PolicyVariableStaticELDeserializerTest :
    DescribeSpec({
      describe("basic parsing") {
        it("should parse correct variable") {
          val given = """#str(22)"""
          val expected =
              PolicyVariableStatic(value = "22", type = VariableValueTypeEnum.STRING, format = null)

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }

        it("should parse correct variable with spaces") {
          val given = """#str(   22    )"""
          val expected =
              PolicyVariableStatic(value = "22", type = VariableValueTypeEnum.STRING, format = null)

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse correct variable with saved spaces") {
          val given = """#str("   22    ")"""
          val expected =
              PolicyVariableStatic(
                  value = "   22    ", type = VariableValueTypeEnum.STRING, format = null)

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse correct wrapped variable 1") {
          val given = """#str("22")"""
          val expected =
              PolicyVariableStatic(value = "22", type = VariableValueTypeEnum.STRING, format = null)

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse correct wrapped variable 2") {
          val given = """#str(`22`)"""
          val expected =
              PolicyVariableStatic(value = "22", type = VariableValueTypeEnum.STRING, format = null)

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse correct wrapped variable 2-1") {
          val given = """#str(`"22"`)"""
          val expected =
              PolicyVariableStatic(
                  value = """"22"""", type = VariableValueTypeEnum.STRING, format = null)

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse correct wrapped variable 3") {
          val given = "#str(" + getWrapper() + "22" + getWrapper() + ")"
          val expected =
              PolicyVariableStatic(value = "22", type = VariableValueTypeEnum.STRING, format = null)

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse correct wrapped variable 3-1") {
          val given = "#str(" + getWrapper() + """"22"""" + getWrapper() + ")"
          val expected =
              PolicyVariableStatic(
                  value = """"22"""", type = VariableValueTypeEnum.STRING, format = null)

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse correct wrapped variable 4") {
          val given = "#str(" + getWrapper() + SPECIAL_CHARS_DICTIONARY + getWrapper() + ")"
          val expected =
              PolicyVariableStatic(
                  value = SPECIAL_CHARS_DICTIONARY,
                  type = VariableValueTypeEnum.STRING,
                  format = null)

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }

        it("should throw exception on no close command") {
          val given = """#str(22"""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parseVariable() }
          actual.message shouldContain "Expected command end"
        }

        it("should throw exception on multiple content") {
          val given = """#str("22","33",#opts(id=stat))"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseVariable() }
          actual.message shouldContain
              "Too many arguments on position 0 for command '#str'. Expected: 1, actual: 2"
        }

        it("should throw exception on bad command") {
          val given = """*gt(22"""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parseVariable() }
          actual.message shouldContain "Invalid variable command: *gt"
        }

        it("should throw exception on child command") {
          val given = """#str(*gt(22))"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseVariable() }
          actual.message shouldContain "Child command type mismatch on position"
        }

        it("should throw exception on short command") {
          val given = """*g"""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parseVariable() }
          actual.message shouldContain "Command too short on position 0"
        }

        it("should throw exception on bad command start ") {
          val given = """#str{(22)"""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parseVariable() }
          actual.message shouldContain "Unknown command #str{ on position 0"
        }

        it("should throw on non dictionary chars") {
          val given = """#str&/(   22    )"""
          val actual =
              shouldThrow<IllegalArgumentException> { PEELParser(given).parseVariableResolver() }
          actual.message shouldBe "Unknown command #str&/ on position 0"
        }
      }
      describe("options parsing") {
        it("should parse correct variable with no options") {
          val given = """#str(22)"""
          val expected =
              PolicyVariableStatic(value = "22", type = VariableValueTypeEnum.STRING, format = null)

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse correct variable with empty options") {
          val given = """#str(22,#opts())"""
          val expected =
              PolicyVariableStatic(value = "22", type = VariableValueTypeEnum.STRING, format = null)

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse options on position 0") {
          val given = """#str(#opts(id=stat),22)"""
          val expected =
              PolicyVariableStatic(
                  id = "stat", value = "22", type = VariableValueTypeEnum.STRING, format = null)

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse correct variable with empty space options") {
          val given = """#str(22,#opts(     ))"""
          val expected =
              PolicyVariableStatic(value = "22", type = VariableValueTypeEnum.STRING, format = null)

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse correct managed options") {
          val given =
              """#str(22,#opts(id=stat,ver=1.2.3,desc="This is description with spaces",labels=foo|bar|a b))"""
          val expected =
              PolicyVariableStatic(
                  id = "stat",
                  version = SemVer(1, 2, 3),
                  description = "This is description with spaces",
                  labels = listOf("foo", "bar", "a b"),
                  value = "22",
                  type = VariableValueTypeEnum.STRING,
                  format = null)

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse correct full options") {
          val given =
              """#str(22,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo,isJson,foo=bar))"""
          val expected =
              PolicyVariableStatic(
                  id = "stat",
                  version = SemVer(1, 2, 3, "alpha", "label1"),
                  description = "This is description with spaces",
                  labels = listOf("foo"),
                  value = """"22"""",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.JSON)

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
      }
      describe("string variations") {
        it("should parse string") {
          val given = """#str(This is string)"""
          val expected =
              PolicyVariableStatic(
                  value = "This is string", type = VariableValueTypeEnum.STRING, format = null)
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse wrapped string") {
          val given = """#str(" This is string ")"""
          val expected =
              PolicyVariableStatic(
                  value = " This is string ", type = VariableValueTypeEnum.STRING, format = null)
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse json string") {
          val given = """#str(" This is string ",#opts(isJson))"""
          val expected =
              PolicyVariableStatic(
                  value = """" This is string """",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.JSON)
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse DATE variable correctly") {
          val given = """#date(2023-05-15)"""
          val expected =
              PolicyVariableStatic(
                  value = LocalDate.of(2023, 5, 15),
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.DATE)
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse DATE variable with custom formatter correctly") {
          val given = """#date(15.05.2023,#opts(dateFormat="dd.MM.yyyy"))"""
          val expected =
              PolicyVariableStatic(
                  value = LocalDate.of(2023, 5, 15),
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.DATE,
                  dateFormat = "dd.MM.yyyy")
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }

        it("should parse TIME variable correctly") {
          val given = """#time(14:30:00)"""
          val expected =
              PolicyVariableStatic(
                  value = LocalTime.of(14, 30, 0),
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.TIME)
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse TIME variable with custom format correctly") {
          val given = """#time("14/30/00",#opts(timeFormat="HH/mm/ss"))"""
          val expected =
              PolicyVariableStatic(
                  value = LocalTime.of(14, 30, 0),
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.TIME,
                  timeFormat = "HH/mm/ss")
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }

        it("should parse DATE_TIME variable correctly") {
          val given = """#dTime(2023-05-15T14:30:00+01:00)"""
          val expected =
              PolicyVariableStatic(
                  value = OffsetDateTime.of(2023, 5, 15, 14, 30, 0, 0, ZoneOffset.ofHours(1)),
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.DATE_TIME)
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse DATE_TIME variable with custom format correctly") {
          val given =
              """#dTime(15.05.2023 14:30:00.123 +01:00,#opts(dateTimeFormat="dd.MM.yyyy HH:mm:ss.SSS XXX"))"""
          val expected =
              PolicyVariableStatic(
                  value =
                      OffsetDateTime.of(2023, 5, 15, 14, 30, 0, 123000000, ZoneOffset.ofHours(1)),
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.DATE_TIME,
                  dateTimeFormat = "dd.MM.yyyy HH:mm:ss.SSS XXX")
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }

        it("should parse PERIOD variable correctly") {
          val given = """#per(P1Y2M3D)"""
          val expected =
              PolicyVariableStatic(
                  value = Period.of(1, 2, 3),
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.PERIOD)
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }

        it("should parse DURATION variable correctly") {
          val given = """#dur(PT1H30M)"""
          val expected =
              PolicyVariableStatic(
                  value = Duration.ofHours(1).plusMinutes(30),
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.DURATION)
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }

        it("should parse BOOLEAN variable correctly") {
          val given = """#bool(true)"""
          val expected =
              PolicyVariableStatic(
                  value = true, type = VariableValueTypeEnum.BOOLEAN, format = null)
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }

        it("should parse INT variable correctly") {
          val given = """#int(-2147483647)"""
          val expected =
              PolicyVariableStatic(
                  value = -2147483647, type = VariableValueTypeEnum.INT, format = null)
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }

        it("should parse LONG variable correctly") {
          val given = """#long(9223372036854775807)"""
          val expected =
              PolicyVariableStatic(
                  value = 9223372036854775807L,
                  type = VariableValueTypeEnum.INT,
                  format = VariableValueFormatEnum.LONG)
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }

        it("should parse NUM variable correctly") {
          val given = """#num(-3.14159)"""
          val expected =
              PolicyVariableStatic(
                  value = "-3.14159".toDouble(), type = VariableValueTypeEnum.NUMBER, format = null)
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }

        it("should parse FLOAT variable correctly") {
          val given = """#float(3.14159)"""
          val expected =
              PolicyVariableStatic(
                  value = "3.14159".toFloat(),
                  type = VariableValueTypeEnum.NUMBER,
                  format = VariableValueFormatEnum.FLOAT)
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }

        it("should parse BIG_DECIMAL variable correctly") {
          val given = """#bigD(123456789.987654321)"""
          val expected =
              PolicyVariableStatic(
                  value = BigDecimal("123456789.987654321"),
                  type = VariableValueTypeEnum.NUMBER,
                  format = VariableValueFormatEnum.BIG_DECIMAL)
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }

        it("should parse OBJECT_NODE variable correctly") {
          val given = """#obj(`{"key": "value", "foo": "bar"}`)"""
          val expected =
              PolicyVariableStatic(
                  value = """{"key": "value", "foo": "bar"}""",
                  type = VariableValueTypeEnum.OBJECT,
                  format = VariableValueFormatEnum.JSON)
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }

        it("should parse ARRAY_NODE variable correctly") {
          val given = """#arr(`[1, 2, 3]`)"""
          val expected =
              PolicyVariableStatic(
                  value = "[1, 2, 3]",
                  type = VariableValueTypeEnum.ARRAY,
                  format = VariableValueFormatEnum.JSON)
          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
      }
    })
