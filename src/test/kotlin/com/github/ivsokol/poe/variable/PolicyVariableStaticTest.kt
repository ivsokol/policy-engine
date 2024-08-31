package com.github.ivsokol.poe.variable

import com.github.ivsokol.poe.*
import com.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import com.github.ivsokol.poe.event.InMemoryEventTestHandler
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import java.math.BigDecimal
import java.time.*

class PolicyVariableStaticTest :
    DescribeSpec({
      val context =
          Context(
              request = mapOf("1" to "2"),
              event = InMemoryEventTestHandler(),
              options = Options(zoneId = ZoneId.of("CET")))

      val objectMapper = DefaultObjectMapper()

      afterTest {
        (context.event as InMemoryEventTestHandler).clear()
        context.removeLastFromPath()
        context.cache.clear()
      }

      describe("resolve") {
        it("should resolve to Boolean") {
          val given = PolicyVariableStatic(value = true)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.BOOLEAN
          actual.body shouldBe true
        }

        it("should resolve string to Boolean") {
          val given = PolicyVariableStatic(value = "false", type = VariableValueTypeEnum.BOOLEAN)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.BOOLEAN
          actual.body shouldBe false
        }

        it("should resolve bad string to Boolean") {
          val given = PolicyVariableStatic(value = "fal", type = VariableValueTypeEnum.BOOLEAN)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual shouldBe NullVariableValue()
        }

        it("should resolve to String") {
          val given = PolicyVariableStatic(value = "value")
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.STRING
          actual.body shouldBe "value"
        }

        it("should resolve string to String") {
          val given = PolicyVariableStatic(value = "value", type = VariableValueTypeEnum.STRING)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.STRING
          actual.body shouldBe "value"
        }

        it("should resolve to Int") {
          val given = PolicyVariableStatic(value = 1)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.INT
          actual.body shouldBe 1
        }

        it("should resolve string to Int") {
          val given = PolicyVariableStatic(value = "1", type = VariableValueTypeEnum.INT)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.INT
          actual.body shouldBe 1
        }

        it("should not resolve bad string to Int") {
          val given = PolicyVariableStatic(value = "foo", type = VariableValueTypeEnum.INT)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual shouldBe NullVariableValue()
        }

        it("should resolve to Double") {
          val given = PolicyVariableStatic(value = 1.0)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.DOUBLE
          actual.body shouldBe 1.0
        }

        it("should resolve string to Double") {
          val given =
              PolicyVariableStatic(
                  value = "1.0",
                  type = VariableValueTypeEnum.NUMBER,
                  format = VariableValueFormatEnum.DOUBLE)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.DOUBLE
          actual.body shouldBe 1.0
        }

        it("should not resolve bad string to Double") {
          val given =
              PolicyVariableStatic(
                  value = "foo",
                  type = VariableValueTypeEnum.NUMBER,
                  format = VariableValueFormatEnum.DOUBLE)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual shouldBe NullVariableValue()
        }

        it("should resolve to Long") {
          val given = PolicyVariableStatic(value = 1L)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.LONG
          actual.body shouldBe 1L
        }

        it("should resolve string to Long") {
          val given =
              PolicyVariableStatic(
                  value = "1",
                  type = VariableValueTypeEnum.INT,
                  format = VariableValueFormatEnum.LONG)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.LONG
          actual.body shouldBe 1L
        }

        it("should not resolve bad string to Long") {
          val given =
              PolicyVariableStatic(
                  value = "foo",
                  type = VariableValueTypeEnum.INT,
                  format = VariableValueFormatEnum.LONG)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual shouldBe NullVariableValue()
        }

        it("should resolve to Float") {
          val given = PolicyVariableStatic(value = 1.0f)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.FLOAT
          actual.body shouldBe 1.0f
        }

        it("should resolve string to Float") {
          val given =
              PolicyVariableStatic(
                  value = "1.0",
                  type = VariableValueTypeEnum.NUMBER,
                  format = VariableValueFormatEnum.FLOAT)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.FLOAT
          actual.body shouldBe 1.0f
        }

        it("should not resolve bad string to Float") {
          val given =
              PolicyVariableStatic(
                  value = "foo",
                  type = VariableValueTypeEnum.NUMBER,
                  format = VariableValueFormatEnum.FLOAT)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual shouldBe NullVariableValue()
        }

        it("should resolve to BigDecimal") {
          val given = PolicyVariableStatic(value = BigDecimal("1.0"))
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.BIG_DECIMAL
          actual.body shouldBe BigDecimal("1.0")
        }

        it("should resolve string to BigDecimal") {
          val given =
              PolicyVariableStatic(
                  value = "1.0",
                  type = VariableValueTypeEnum.NUMBER,
                  format = VariableValueFormatEnum.BIG_DECIMAL)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.BIG_DECIMAL
          actual.body shouldBe BigDecimal("1.0")
        }

        it("should not resolve bad string to BigDecimal") {
          val given =
              PolicyVariableStatic(
                  value = "foo",
                  type = VariableValueTypeEnum.NUMBER,
                  format = VariableValueFormatEnum.BIG_DECIMAL)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual shouldBe NullVariableValue()
        }

        it("should resolve to LocalDate") {
          val given =
              PolicyVariableStatic(
                  value = "2021-01-01",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.DATE)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.DATE
          actual.body shouldBe LocalDate.parse("2021-01-01")
        }

        it("should resolve custom date to LocalDate") {
          val given =
              PolicyVariableStatic(
                  value = "04.11.2021",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.DATE,
                  dateFormat = "dd.MM.yyyy")
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.DATE
          actual.body shouldBe LocalDate.parse("2021-11-04")
        }

        it("should not resolve bad string to LocalDate") {
          val given =
              PolicyVariableStatic(
                  value = "foo",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.DATE)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual shouldBe NullVariableValue()
        }

        it("should resolve to OffsetDateTime") {
          val given =
              PolicyVariableStatic(
                  value = "2021-01-01T00:00:00Z",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.DATE_TIME)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.DATE_TIME
          actual.body shouldBe OffsetDateTime.parse("2021-01-01T00:00:00Z")
        }

        it("should resolve custom dateTime to OffsetDateTime") {
          val given =
              PolicyVariableStatic(
                  value = "04.11.2021 12:46:52",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.DATE_TIME,
                  dateTimeFormat = "dd.MM.yyyy HH:mm:ss")
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.DATE_TIME
          actual.body shouldBe OffsetDateTime.parse("2021-11-04T12:46:52+01:00")
        }

        it("should not resolve bad string to OffsetDateTime") {
          val given =
              PolicyVariableStatic(
                  value = "foo",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.DATE_TIME)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual shouldBe NullVariableValue()
        }

        it("should resolve to LocalTime") {
          val given =
              PolicyVariableStatic(
                  value = "12:46:52.12456789",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.TIME)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.TIME
          actual.body shouldBe LocalTime.parse("12:46:52.12456789")
        }

        it("should resolve partial time to LocalTime") {
          val given =
              PolicyVariableStatic(
                  value = "12:46",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.TIME)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.TIME
          actual.body shouldBe LocalTime.parse("12:46:00")
        }

        it("should resolve custom time to LocalTime") {
          val given =
              PolicyVariableStatic(
                  value = "12/46",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.TIME,
                  timeFormat = "HH/mm")
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.TIME
          actual.body shouldBe LocalTime.parse("12:46:00")
        }

        it("should not resolve bad string to LocalTime") {
          val given =
              PolicyVariableStatic(
                  value = "foo",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.TIME)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual shouldBe NullVariableValue()
        }

        it("should resolve to Period") {
          val given =
              PolicyVariableStatic(
                  value = "P1Y",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.PERIOD)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.PERIOD
          actual.body shouldBe Period.parse("P1Y")
        }

        it("should not resolve bad string to Period") {
          val given =
              PolicyVariableStatic(
                  value = "foo",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.PERIOD)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual shouldBe NullVariableValue()
        }

        it("should resolve to Duration") {
          val given =
              PolicyVariableStatic(
                  value = "PT1H",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.DURATION)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.DURATION
          actual.body shouldBe Duration.parse("PT1H")
        }

        it("should not resolve bad string to Duration") {
          val given =
              PolicyVariableStatic(
                  value = "foo",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.DURATION)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual shouldBe NullVariableValue()
        }

        it("should resolve to ObjectNode") {
          val given =
              PolicyVariableStatic(
                  value = objectMapper.readTree("""{"1":"2"}"""),
                  type = VariableValueTypeEnum.OBJECT,
                  format = VariableValueFormatEnum.JSON)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.OBJECT_NODE
          actual.body shouldBe objectMapper.readTree("""{"1":"2"}""")
        }

        it("should resolve to ArrayNode") {
          val given =
              PolicyVariableStatic(
                  value = objectMapper.readTree("""["1","2"]"""),
                  type = VariableValueTypeEnum.ARRAY,
                  format = VariableValueFormatEnum.JSON)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.ARRAY_NODE
          actual.body shouldBe objectMapper.readTree("""["1","2"]""")
        }

        it("should resolve string of type JSON to JSONNode") {
          val given =
              PolicyVariableStatic(
                  value = """{"1":"2"}""",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.JSON)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.JSON_NODE
          actual.body shouldBe objectMapper.readTree("""{"1":"2"}""")
        }

        it("should return NullVariableValue when exception is thrown") {
          val given =
              PolicyVariableStatic(
                  id = "1",
                  value = "${Long.MAX_VALUE}",
                  type = VariableValueTypeEnum.INT,
                  format = null)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual shouldBe NullVariableValue()
        }
      }

      describe("cache") {
        it("should resolve to String and not found in cache") {
          val given = PolicyVariableStatic(value = "value")
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.STRING
          actual.body shouldBe "value"

          context.cache.get(
              com.github.ivsokol.poe.cache.PolicyStoreCacheEnum.VARIABLE, "value") shouldBe null
        }

        it("should resolve to String and found in cache") {
          val given = PolicyVariableStatic(id = "1", value = "value")
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.STRING
          actual.body shouldBe "value"

          // code coverage test
          val actual2 = given.copy().resolve(context, EmptyPolicyCatalog())
          actual2.type shouldBe VariableRuntimeTypeEnum.STRING
          actual2.body shouldBe "value"

          context.cache.getVariable("1") shouldBe
              VariableValue(VariableRuntimeTypeEnum.STRING, "value")
        }

        it("should resolve to LocalDate and found in cache") {
          val given =
              PolicyVariableStatic(
                  id = "1",
                  value = "2024-01-23",
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.DATE)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.DATE
          actual.body shouldBe LocalDate.parse("2024-01-23")

          context.cache.get(
              com.github.ivsokol.poe.cache.PolicyStoreCacheEnum.VARIABLE, "1") shouldBe
              VariableValue(VariableRuntimeTypeEnum.DATE, LocalDate.parse("2024-01-23"))
        }
      }

      describe("childRefs") {
        it("should return null") {
          val actual = PolicyVariableStatic(value = "value")
          actual.childRefs() shouldBe null
        }
      }

      describe("identity") {
        it("should return empty") {
          val actual = PolicyVariableStatic(value = "value")
          actual.identity() shouldBe ""
        }
        it("should throw when bad id") {
          shouldThrow<IllegalArgumentException> { PolicyVariableStatic(id = " ", value = "value") }
              .message shouldBe "Id must not be blank"
        }
        it("should return id") {
          val actual = PolicyVariableStatic(id = "1", value = "value")
          actual.identity() shouldBe "1"
        }
        it("should return idVer") {
          val actual = PolicyVariableStatic(id = "1", version = SemVer(1, 0, 0), value = "value")
          actual.identity() shouldBe "1:1.0.0"
        }
      }

      describe("events") {
        it("found in cache") {
          val ctx = Context(event = InMemoryEventTestHandler())
          val given = PolicyVariableStatic(id = "pvs1", value = true)
          // populate cache
          given.resolve(ctx, EmptyPolicyCatalog())
          (ctx.event as InMemoryEventTestHandler).clear()
          ctx.removeLastFromPath()
          given.resolve(ctx, EmptyPolicyCatalog())
          val actualEvents = ctx.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe true
          actualEvents[0].entityId shouldBe "pvs1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC
          actualEvents[0].reason shouldBe null
          actualEvents[0].message shouldBe
              VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true).toString()
          actualEvents[0].fromCache shouldBe true
        }
        it("no coercions") {
          val ctx = Context(event = InMemoryEventTestHandler())
          val given = PolicyVariableStatic(id = "pvs2", value = true)
          given.resolve(ctx, EmptyPolicyCatalog())
          val actualEvents = ctx.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe true
          actualEvents[0].entityId shouldBe "pvs2"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC
          actualEvents[0].reason shouldBe null
          actualEvents[0].message shouldBe
              VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true).toString()
          actualEvents[0].fromCache shouldBe false
        }
        it("coerced value") {
          val ctx = Context(event = InMemoryEventTestHandler())
          val given =
              PolicyVariableStatic(
                  id = "pvs3", value = "true", type = VariableValueTypeEnum.BOOLEAN)
          given.resolve(ctx, EmptyPolicyCatalog())
          val actualEvents = ctx.event.list()
          actualEvents[0].contextId shouldNotBe null
          actualEvents shouldHaveSize 1
          actualEvents[0].success shouldBe true
          actualEvents[0].entityId shouldBe "pvs3"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC
          actualEvents[0].reason shouldBe null
          actualEvents[0].message shouldBe
              VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true).toString()
          actualEvents[0].fromCache shouldBe false
        }
        it("exception") {
          val given =
              PolicyVariableStatic(
                  id = "pvs4",
                  value = "${Long.MAX_VALUE}",
                  type = VariableValueTypeEnum.INT,
                  format = null)
          given.resolve(context, EmptyPolicyCatalog())
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pvs4"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC
          actualEvents[0].reason shouldContain "For input string"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false
        }
      }
      describe("naming") {
        it("not named") {
          val ctx = Context(event = InMemoryEventTestHandler())
          val given = PolicyVariableStatic(value = true)
          given.resolve(ctx, EmptyPolicyCatalog())
          val actualEvents = ctx.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe ""
        }
        it("child") {
          val ctx = Context(event = InMemoryEventTestHandler())
          ctx.addToPath("variables")
          ctx.addToPath("0")
          val given = PolicyVariableStatic(value = true)
          given.resolve(ctx, EmptyPolicyCatalog())
          val actualEvents = ctx.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe "variables/0"
        }
        it("named child") {
          val ctx = Context(event = InMemoryEventTestHandler())
          ctx.addToPath("variables")
          ctx.addToPath("0")
          val given = PolicyVariableStatic(id = "pvs1", value = true)
          given.resolve(ctx, EmptyPolicyCatalog())
          val actualEvents = ctx.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe "variables/0(pvs1)"
        }
      }
    })
