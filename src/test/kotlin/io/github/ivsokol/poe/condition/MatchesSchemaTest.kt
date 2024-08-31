package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.variable.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.*

class MatchesSchemaTest :
    DescribeSpec({
      val context = Context(request = mapOf("arr" to listOf("a", "b"), "arrInt" to listOf(1, 2)))
      describe("schema validation tests") {
        it("string schema") {
          val schema = """{"type": "string"}"""
          val json = """"foo""""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(string(json), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("jsonNode schema") {
          val schema = """{"type": "string"}"""
          val json = """"foo""""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string(json),
                              string(schema)
                                  .copy(
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.JSON)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("objectNode schema") {
          val schema = """{"type": "string"}"""
          val json = """"foo""""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string(json),
                              string(schema)
                                  .copy(
                                      type = VariableValueTypeEnum.OBJECT,
                                      format = VariableValueFormatEnum.JSON)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("bad schema") {
          val schema = """"type""""
          val json = """"foo""""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(string(json), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe null
        }
        it("int schema") {
          val json = """"foo""""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH, args = listOf(string(json), int(1)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe null
        }
      }

      describe("boolean") {
        it("boolean should pass") {
          val schema = """{"type": "boolean"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(bool(true), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("string should fail") {
          val schema = """{"type": "boolean"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(string("true"), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("cast should pass") {
          val schema = """{"type": "boolean"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string("true").copy(type = VariableValueTypeEnum.BOOLEAN),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("jsonNode should pass") {
          val schema = """{"type": "boolean"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string("true")
                                  .copy(
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
      }

      describe("int") {
        it("int should pass") {
          val schema = """{"type": "integer"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH, args = listOf(int(1), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("string should fail") {
          val schema = """{"type": "integer"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(string("1"), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("cast should pass") {
          val schema = """{"type": "integer"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string("1").copy(type = VariableValueTypeEnum.INT), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("jsonNode should pass") {
          val schema = """{"type": "integer"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string("1")
                                  .copy(
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
      }

      describe("long") {
        it("long should pass") {
          val schema = """{"type": "integer"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(long(Long.MAX_VALUE), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("string should fail") {
          val schema = """{"type": "integer"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(string("1"), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("cast should pass") {
          val schema = """{"type": "integer"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string("1")
                                  .copy(
                                      type = VariableValueTypeEnum.INT,
                                      format = VariableValueFormatEnum.LONG),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("jsonNode should pass") {
          val schema = """{"type": "integer"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string(Long.MAX_VALUE.toString())
                                  .copy(
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
      }

      describe("double") {
        it("number should pass") {
          val schema = """{"type": "number"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(double(1.0), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("string should fail") {
          val schema = """{"type": "number"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(string("1.0"), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("cast should pass") {
          val schema = """{"type": "number"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string("1.0").copy(type = VariableValueTypeEnum.NUMBER),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("jsonNode should pass") {
          val schema = """{"type": "number"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string("1.0")
                                  .copy(
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
      }

      describe("float") {
        it("number should pass") {
          val schema = """{"type": "number"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(float(1.0f), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("string should fail") {
          val schema = """{"type": "number"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(string("1.0"), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("cast should pass") {
          val schema = """{"type": "number"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string("1.0")
                                  .copy(
                                      type = VariableValueTypeEnum.NUMBER,
                                      format = VariableValueFormatEnum.FLOAT),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("jsonNode should pass") {
          val schema = """{"type": "number"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string(1.0f.toString())
                                  .copy(
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
      }

      describe("bigDecimal") {
        it("number should pass") {
          val schema = """{"type": "number"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(bigDecimal(BigDecimal.ONE), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("boolean should fail") {
          val schema = """{"type": "number"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(bool(true), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("cast should pass") {
          val schema = """{"type": "number"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string("1.0")
                                  .copy(
                                      type = VariableValueTypeEnum.NUMBER,
                                      format = VariableValueFormatEnum.BIG_DECIMAL),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("jsonNode should pass") {
          val schema = """{"type": "number"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string(BigDecimal.ONE.toString())
                                  .copy(
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
      }

      describe("date") {
        it("date should pass") {
          val schema = """{"type": "string", "format": "date"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(date(LocalDate.parse("2020-01-01")), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("int should fail") {
          val schema = """{"type": "string", "format": "date"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH, args = listOf(int(2), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("string should pass") {
          val schema = """{"type": "string", "format": "date"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(string("2020-01-01"), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("jsonNode should pass") {
          val schema = """{"type": "string", "format": "date"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string(""""2020-01-01"""")
                                  .copy(
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
      }

      describe("dateTime") {
        it("dateTime should pass") {
          val schema = """{"type": "string", "format": "date-time"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              dateTime(OffsetDateTime.parse("2020-01-01T00:00:00+01:00")),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("int should fail") {
          val schema = """{"type": "string", "format": "date-time"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH, args = listOf(int(2), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("string should pass") {
          val schema = """{"type": "string", "format": "date-time"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(string("2020-01-01T00:00:00+01:00"), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("jsonNode should pass") {
          val schema = """{"type": "string", "format": "date-time"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string(""""2020-01-01T00:00:00+01:00"""")
                                  .copy(
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
      }

      describe("time") {
        it("time should pass") {
          val schema = """{"type": "string", "format": "time"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(time(LocalTime.parse("00:00:00")), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("int should fail") {
          val schema = """{"type": "string", "format": "time"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH, args = listOf(int(2), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("string should pass") {
          val schema = """{"type": "string", "format": "time"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(string("00:00:00+01:00"), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("jsonNode should pass") {
          val schema = """{"type": "string", "format": "time"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string(""""00:00:00+01:00"""")
                                  .copy(
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
      }

      describe("period") {
        it("period should pass") {
          val schema = """{"type": "string", "format": "period"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(period(Period.parse("P1Y2M3D")), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("int should fail") {
          val schema = """{"type": "string", "format": "period"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH, args = listOf(int(2), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("string should pass") {
          val schema = """{"type": "string", "format": "period"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(string("P1Y2M3D"), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("jsonNode should pass") {
          val schema = """{"type": "string", "format": "period"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string(""""P1Y2M3D"""")
                                  .copy(
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
      }

      describe("duration") {
        it("duration should pass") {
          val schema = """{"type": "string", "format": "duration"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(duration(Duration.parse("PT1H2M3S")), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("int should fail") {
          val schema = """{"type": "string", "format": "duration"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH, args = listOf(int(2), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("string should pass") {
          val schema = """{"type": "string", "format": "duration"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(string("PT1H2M3S"), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("jsonNode should pass") {
          val schema = """{"type": "string", "format": "duration"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string(""""PT1H2M3S"""")
                                  .copy(
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
      }

      describe("object") {
        it("object should pass") {
          val schema = """{"type": "object", "properties": {"a": {"type": "string"}}}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              PolicyVariableStatic(
                                  type = VariableValueTypeEnum.OBJECT,
                                  format = VariableValueFormatEnum.JSON,
                                  value = """{"a": "b"}"""),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("bad object should fail") {
          val schema = """{"type": "object", "properties": {"a": {"type": "string"}}}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              PolicyVariableStatic(
                                  type = VariableValueTypeEnum.OBJECT,
                                  format = VariableValueFormatEnum.JSON,
                                  value = """{"a": 1}"""),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("cast string should pass") {
          val schema = """{"type": "object", "properties": {"a": {"type": "string"}}}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string("""{"a": "b"}""").copy(format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("string should fail") {
          val schema = """{"type": "object", "properties": {"a": {"type": "string"}}}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(string("""{"a": "b"}"""), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("jsonNode should pass") {
          val schema = """{"type": "object", "properties": {"a": {"type": "string"}}}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string("""{"a": "b"}""")
                                  .copy(
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("bad jsonNode should fail") {
          val schema = """{"type": "object", "properties": {"a": {"type": "string"}}}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string("""{"a": 1}""")
                                  .copy(
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("int should fail") {
          val schema = """{"type": "object", "properties": {"a": {"type": "string"}}}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH, args = listOf(int(1), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
      }

      describe("array") {
        it("array should pass") {
          val schema = """{"type": "array", "items": {"type": "string"}}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              PolicyVariableDynamic(
                                  resolvers = listOf(PolicyVariableResolver(key = "arr"))),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("bad array should fail") {
          val schema = """{"type": "array", "items": {"type": "string"}}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              PolicyVariableDynamic(
                                  resolvers = listOf(PolicyVariableResolver(key = "arrInt"))),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("array node should pass") {
          val schema = """{"type": "array", "items": {"type": "string"}}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              PolicyVariableStatic(
                                  type = VariableValueTypeEnum.ARRAY,
                                  format = VariableValueFormatEnum.JSON,
                                  value = """["a","b"]"""),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("bad array node should fail") {
          val schema = """{"type": "array", "items": {"type": "string"}}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              PolicyVariableStatic(
                                  type = VariableValueTypeEnum.ARRAY,
                                  format = VariableValueFormatEnum.JSON,
                                  value = """[1,2]"""),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("cast string should pass") {
          val schema = """{"type": "array", "items": {"type": "string"}}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string("""["a","b"]""").copy(format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("string should fail") {
          val schema = """{"type": "array", "items": {"type": "string"}}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(string("""["a","b"]"""), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("jsonNode should pass") {
          val schema = """{"type": "array", "items": {"type": "string"}}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string("""["a","b"]""")
                                  .copy(
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("bad jsonNode should fail") {
          val schema = """{"type": "array", "items": {"type": "string"}}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              string("""[1,2]""")
                                  .copy(
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.JSON),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
        it("int should fail") {
          val schema = """{"type": "array", "items": {"type": "string"}}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH, args = listOf(int(1), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
      }

      describe("null") {
        it("null should pass") {
          val schema = """{"type": "null"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args =
                          listOf(
                              PolicyVariableDynamic(
                                  resolvers = listOf(PolicyVariableResolver(key = "null"))),
                              string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe true
        }
        it("non null should fail") {
          val schema = """{"type": "null"}"""
          val result =
              PolicyConditionAtomic(
                      operation = OperationEnum.SCHEMA_MATCH,
                      args = listOf(string("a"), string(schema)))
                  .check(context, EmptyPolicyCatalog())
          result shouldBe false
        }
      }
    })
