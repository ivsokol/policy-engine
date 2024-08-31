package io.github.ivsokol.poe.variable

import com.fasterxml.jackson.databind.node.*
import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.DefaultObjectMapper
import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.event.InMemoryEventTestHandler
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Period

private const val fullStringBody =
    """
    {"str":"a","i":1,"b":true,"n":null,"num":1.1,"date":"2024-12-19","dateTime":"2024-12-19T12:00:00Z","period":"P1D","duration":"PT1H","bigD":"3.14","array":[1,2,3],"object":{"a1":"b1"},"strObj":"{\"a2\":\"b2\"}"}"
"""

private data class Bar(val a2: String)

private data class FooResolver(val a1: Bar)

private val fullObjectBody =
    mapOf(
        "user" to "jdoe",
        "str" to "a",
        "i" to 1,
        "b" to true,
        "n" to null,
        "num" to 1.1,
        "date" to LocalDate.parse("2024-12-19"),
        "dateTime" to OffsetDateTime.parse("2024-12-19T12:00:00Z"),
        "period" to Period.parse("P1D"),
        "duration" to Duration.parse("PT1H"),
        "bigD" to BigDecimal("3.14"),
        "array" to listOf(1, 2, 3),
        "object" to mapOf("a1" to "b1"),
        "strObj" to """{"a2":"b2"}""",
        "foo" to FooResolver(Bar("b1")))

class PolicyVariableResolverTest :
    DescribeSpec({
      val objectMapper = DefaultObjectMapper()
      val context =
          Context(
              request =
                  mapOf(
                      "json" to objectMapper.readTree(fullStringBody),
                      "object" to """{"a1":"b1"}"""),
              subject = fullObjectBody,
              environment = mapOf("date" to "2024-12-19"),
              event = InMemoryEventTestHandler())

      afterTest {
        (context.event as InMemoryEventTestHandler).clear()
        context.removeLastFromPath()
        context.cache.clear()
      }

      describe("jmesPath") {
        it("should resolve json variable from request to TextNode with key") {
          val given =
              PolicyVariableResolver(
                  id = "pvr1",
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "object.a1",
                  key = "json")
          val actual = given.resolve(context)
          actual shouldBe TextNode("b1")
        }

        it("should resolve variable from subject to TextNode") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "user",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe TextNode("jdoe")
        }

        it("should resolve variable from environment to TextNode") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "date",
                  source = ContextStoreEnum.ENVIRONMENT)
          val actual = given.resolve(context)
          actual shouldBe TextNode("2024-12-19")
        }

        it("should resolve string variable from request to TextNode with key") {
          // when key is used then key value is serialized to Jackson, so path finding is possible
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH, path = "a1", key = "object")
          val actual = given.resolve(context)
          actual shouldBe TextNode("b1")
        }

        it("should NOT resolve string variable from request to TextNode without key") {
          // serializing ContextMap values by default is putting values as strings, so they are not
          // treated as json objects, therefore path finding is not possible
          val given =
              PolicyVariableResolver(
                  id = "pvr2",
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "object.a1")
          val actual = given.resolve(context)
          actual shouldBe null
        }

        it("should resolve json string variable from request to TextNode without key") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH, path = "json.str")
          val actual = given.resolve(context)
          actual shouldBe TextNode("a")
        }

        it("should resolve json int variable from request to IntNode without key") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH, path = "json.i")
          val actual = given.resolve(context)
          actual shouldBe IntNode(1)
        }

        it("should resolve json boolean variable from request to BoolNode without key") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH, path = "json.b")
          val actual = given.resolve(context)
          actual shouldBe BooleanNode.TRUE
        }

        it("should resolve json null variable from request to NullNode without key") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH, path = "json.n")
          val actual = given.resolve(context)
          actual shouldBe null
        }

        it("should resolve json number variable from request to DoubleNode without key") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH, path = "json.num")
          val actual = given.resolve(context)
          actual shouldBe DoubleNode(1.1)
        }

        it("should resolve json date variable from request to TextNode without key") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH, path = "json.date")
          val actual = given.resolve(context)
          actual shouldBe TextNode("2024-12-19")
        }

        it("should resolve json dateTime variable from request to TextNode without key") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH, path = "json.dateTime")
          val actual = given.resolve(context)
          actual shouldBe TextNode("2024-12-19T12:00:00Z")
        }

        it("should resolve json period variable from request to TextNode without key") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH, path = "json.period")
          val actual = given.resolve(context)
          actual shouldBe TextNode("P1D")
        }

        it("should resolve json duration variable from request to TextNode without key") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH, path = "json.duration")
          val actual = given.resolve(context)
          actual shouldBe TextNode("PT1H")
        }

        it("should resolve json bigD variable from request to TextNode without key") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH, path = "json.bigD")
          val actual = given.resolve(context)
          actual shouldBe TextNode("3.14")
        }

        it("should resolve json array variable from request to ArrayNode without key") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH, path = "json.array")
          val actual = given.resolve(context)
          actual shouldBe objectMapper.readTree("[1,2,3]")
        }

        it("should resolve json object variable from request to ObjectNode without key") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH, path = "json.object")
          val actual = given.resolve(context)
          actual shouldBe objectMapper.readTree("""{"a1":"b1"}""")
        }

        it("should resolve string in object variable from subject to TextNode") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "str",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe TextNode("a")
        }

        it("should resolve int in object variable from subject to IntNode") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "i",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe IntNode(1)
        }

        it("should resolve boolean in object variable from subject to BooleanNode") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "b",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe BooleanNode.TRUE
        }

        it("should resolve null in object variable from subject to NullNode") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "null",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe null
        }

        it("should resolve number in object variable from subject to DoubleNode") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "num",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe DoubleNode(1.1)
        }

        it("should resolve date in object variable from subject to TextNode") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "date",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe TextNode("2024-12-19")
        }

        it("should resolve dateTime in object variable from subject to TextNode") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "dateTime",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe TextNode("2024-12-19T12:00:00Z")
        }

        it("should resolve period in object variable from subject to TextNode") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "period",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe TextNode("P1D")
        }

        it("should resolve duration in object variable from subject to TextNode") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "duration",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe TextNode("PT1H")
        }

        it("should resolve BigDecimal in object variable from subject to TextNode") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "bigD",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe DecimalNode(BigDecimal("3.14"))
        }

        it("should resolve array in object variable from subject to ArrayNode") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "array",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe objectMapper.readTree("[1,2,3]")
        }

        it("should resolve object in object variable from subject to ObjectNode") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "object",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe objectMapper.readTree("""{"a1":"b1"}""")
        }

        it("should NOT resolve path in string variable from subject to TextNode") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "strObj.a2",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe null
        }

        it("should resolve object in object variable from subject to ObjectNode") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "foo.a1.a2",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe TextNode("b1")
        }
      }

      describe("key") {
        val ctx = Context(request = fullObjectBody, event = InMemoryEventTestHandler())
        afterTest {
          (ctx.event as InMemoryEventTestHandler).clear()
          ctx.removeLastFromPath()
        }
        it("should resolve string") {
          val given = PolicyVariableResolver(key = "str")
          val actual = given.resolve(ctx)
          actual shouldBe "a"
        }
        it("should resolve int") {
          val given = PolicyVariableResolver(key = "i")
          val actual = given.resolve(ctx)
          actual shouldBe 1
        }
        it("should resolve boolean") {
          val given = PolicyVariableResolver(key = "b")
          val actual = given.resolve(ctx)
          actual shouldBe true
        }
        it("should resolve null") {
          val given = PolicyVariableResolver(id = "pvr7", key = "n")
          val actual = given.resolve(ctx)
          actual shouldBe null
        }
        it("should resolve number") {
          val given = PolicyVariableResolver(key = "num")
          val actual = given.resolve(ctx)
          actual shouldBe 1.1
        }
        it("should resolve date") {
          val given = PolicyVariableResolver(key = "date")
          val actual = given.resolve(ctx)
          actual shouldBe LocalDate.parse("2024-12-19")
        }
        it("should resolve dateTime") {
          val given = PolicyVariableResolver(key = "dateTime")
          val actual = given.resolve(ctx)
          actual shouldBe OffsetDateTime.parse("2024-12-19T12:00:00Z")
        }
        it("should resolve period") {
          val given = PolicyVariableResolver(key = "period")
          val actual = given.resolve(ctx)
          actual shouldBe Period.parse("P1D")
        }
        it("should resolve duration") {
          val given = PolicyVariableResolver(key = "duration")
          val actual = given.resolve(ctx)
          actual shouldBe Duration.parse("PT1H")
        }
        it("should resolve big decimal") {
          val given = PolicyVariableResolver(key = "bigD")
          val actual = given.resolve(ctx)
          actual shouldBe BigDecimal("3.14")
        }
        it("should resolve array") {
          val given = PolicyVariableResolver(key = "array")
          val actual = given.resolve(ctx)
          actual shouldBe listOf(1, 2, 3)
        }
        it("should resolve object") {
          val given = PolicyVariableResolver(key = "object")
          val actual = given.resolve(ctx)
          actual shouldBe mapOf("a1" to "b1")
        }
        it("should resolve strObj") {
          val given = PolicyVariableResolver(key = "strObj")
          val actual = given.resolve(ctx)
          actual shouldBe """{"a2":"b2"}"""
        }
        it("should resolve foo") {
          val given = PolicyVariableResolver(key = "foo")
          val actual = given.resolve(ctx)
          actual shouldBe FooResolver(Bar("b1"))
        }
      }

      describe("jq") {
        it("should resolve json variable from request to string with key") {
          val given =
              PolicyVariableResolver(
                  path = ".object.a1", key = "json", engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "b1"
        }

        it("should resolve variable from subject to string") {
          val given =
              PolicyVariableResolver(
                  path = ".user",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "jdoe"
        }

        it("should resolve variable from environment to string") {
          val given =
              PolicyVariableResolver(
                  path = ".date",
                  source = ContextStoreEnum.ENVIRONMENT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "2024-12-19"
        }

        it("should resolve string variable from request to string with key") {
          // when key is used then key value is serialized to Jackson, so path finding is possible
          val given =
              PolicyVariableResolver(
                  path = ".a1", key = "object", engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "b1"
        }

        it("should NOT resolve string variable from request to string without key") {
          // serializing ContextMap values by default is putting values as strings, so they are not
          // treated as json objects, therefore path finding is not possible
          val given =
              PolicyVariableResolver(
                  id = "pvr8", path = ".object.a1", engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe null
        }

        it("should resolve json string variable from request to string without key") {
          val given =
              PolicyVariableResolver(
                  path = ".json.str", engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "a"
        }

        it("should resolve json int variable from request to string without key") {
          val given =
              PolicyVariableResolver(path = ".json.i", engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "1"
        }

        it("should resolve json boolean variable from request to string without key") {
          val given =
              PolicyVariableResolver(path = ".json.b", engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "true"
        }

        it("should resolve json null variable from request to null without key") {
          val given =
              PolicyVariableResolver(path = ".json.n", engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe null
        }

        it("should resolve json number variable from request to string without key") {
          val given =
              PolicyVariableResolver(
                  path = ".json.num", engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "1.1"
        }

        it("should resolve json date variable from request to string without key") {
          val given =
              PolicyVariableResolver(
                  path = ".json.date", engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "2024-12-19"
        }

        it("should resolve json dateTime variable from request to string without key") {
          val given =
              PolicyVariableResolver(
                  path = ".json.dateTime", engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "2024-12-19T12:00:00Z"
        }

        it("should resolve json period variable from request to TextNode without key") {
          val given =
              PolicyVariableResolver(
                  path = ".json.period", engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "P1D"
        }

        it("should resolve json duration variable from request to TextNode without key") {
          val given =
              PolicyVariableResolver(
                  path = ".json.duration", engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "PT1H"
        }

        it("should resolve json bigD variable from request to string without key") {
          val given =
              PolicyVariableResolver(
                  path = ".json.bigD", engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "3.14"
        }

        it("should resolve json array variable from request to string without key") {
          val given =
              PolicyVariableResolver(
                  path = ".json.array", engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "[1,2,3]"
        }

        it("should resolve json object variable from request to string without key") {
          val given =
              PolicyVariableResolver(
                  path = ".json.object", engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe """{"a1":"b1"}"""
        }

        it("should resolve string in object variable from subject to string") {
          val given =
              PolicyVariableResolver(
                  path = ".str",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "a"
        }

        it("should resolve int in object variable from subject to string") {
          val given =
              PolicyVariableResolver(
                  path = ".i",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "1"
        }

        it("should resolve boolean in object variable from subject to string") {
          val given =
              PolicyVariableResolver(
                  path = ".b",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "true"
        }

        it("should resolve null in object variable from subject to null") {
          val given =
              PolicyVariableResolver(
                  path = ".null",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe null
        }

        it("should resolve number in object variable from subject to string") {
          val given =
              PolicyVariableResolver(
                  path = ".num",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "1.1"
        }

        it("should resolve date in object variable from subject to string") {
          val given =
              PolicyVariableResolver(
                  path = ".date",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "2024-12-19"
        }

        it("should resolve dateTime in object variable from subject to string") {
          val given =
              PolicyVariableResolver(
                  path = ".dateTime",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "2024-12-19T12:00:00Z"
        }

        it("should resolve period in object variable from subject to string") {
          val given =
              PolicyVariableResolver(
                  path = ".period",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "P1D"
        }

        it("should resolve duration in object variable from subject to string") {
          val given =
              PolicyVariableResolver(
                  path = ".duration",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "PT1H"
        }

        it("should resolve BigDecimal in object variable from subject to TextNode") {
          val given =
              PolicyVariableResolver(
                  path = ".bigD",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "3.14"
        }

        it("should resolve array in object variable from subject to string") {
          val given =
              PolicyVariableResolver(
                  path = ".array",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "[1,2,3]"
        }

        it("should resolve object in object variable from subject to string") {
          val given =
              PolicyVariableResolver(
                  path = ".object",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe """{"a1":"b1"}"""
        }

        it("should NOT resolve path in string variable from subject to string") {
          val given =
              PolicyVariableResolver(
                  path = ".strObj.a2",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe null
        }

        it("should resolve object in object variable from subject to string") {
          val given =
              PolicyVariableResolver(
                  path = ".foo.a1.a2",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe "b1"
        }
      }

      describe("exceptions") {
        it("should return null when store is null or empty") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "a",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context.copy(subject = null))
          actual shouldBe null
        }

        it("should return null when key is bad") {
          val given =
              PolicyVariableResolver(
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "str",
                  key = "f",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe null
        }

        it("should return null when keyValue is null") {
          val given =
              PolicyVariableResolver(
                  id = "pvr5",
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "str",
                  key = "n",
                  source = ContextStoreEnum.SUBJECT)
          val actual = given.resolve(context)
          actual shouldBe null
        }

        it("should return null when path is not correct") {
          val given =
              PolicyVariableResolver(
                  id = "pvr6",
                  path = "%",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = given.resolve(context)
          actual shouldBe null
        }
      }

      describe("identity") {
        it("should return empty") {
          val actual = PolicyVariableResolver(key = "value")
          actual.identity() shouldBe ""
        }
        it("should throw when bad id") {
          shouldThrow<IllegalArgumentException> { PolicyVariableResolver(id = " ", key = "value") }
              .message shouldBe "Id must not be blank"
        }
        it("should return id") {
          val actual = PolicyVariableResolver(id = "1", key = "value")
          actual.identity() shouldBe "1"
        }
        it("should return idVer") {
          val actual = PolicyVariableResolver(id = "1", version = SemVer(1, 0, 0), key = "value")
          actual.identity() shouldBe "1:1.0.0"
        }
      }

      describe("events") {
        it("no source found") {
          val ctx = Context()
          val given =
              PolicyVariableResolver(
                  id = "pvr1", key = ".object", source = ContextStoreEnum.SUBJECT)
          given.resolve(ctx)
          val actualEvents = ctx.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pvr1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VALUE_RESOLVER
          actualEvents[0].message shouldBe null
          actualEvents[0].reason shouldContain "No source found"
          actualEvents[0].fromCache shouldBe false
        }
        it("bad JmesPath key") {
          val given =
              PolicyVariableResolver(
                  id = "pvr2",
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "n",
                  key = "json",
                  source = ContextStoreEnum.REQUEST)
          given.resolve(context)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pvr2"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VALUE_RESOLVER
          actualEvents[0].message shouldBe null
          actualEvents[0].reason shouldContain "JSONPath: No value found"
          actualEvents[0].fromCache shouldBe false
        }
        it("key value null") {
          val given =
              PolicyVariableResolver(id = "pvr3", key = "m", source = ContextStoreEnum.SUBJECT)
          given.resolve(context)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pvr3"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VALUE_RESOLVER
          actualEvents[0].message shouldBe null
          actualEvents[0].reason shouldContain "No value found"
          actualEvents[0].fromCache shouldBe false
        }
        it("bad JQ path") {
          val given =
              PolicyVariableResolver(
                  id = "pvr4",
                  path = "%",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          given.resolve(context)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pvr4"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VALUE_RESOLVER
          actualEvents[0].message shouldBe null
          actualEvents[0].reason shouldContain "JQ error"
          actualEvents[0].fromCache shouldBe false
        }
        it("JQ null") {
          val given =
              PolicyVariableResolver(
                  id = "pvr5",
                  path = ".null",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          given.resolve(context)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pvr5"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VALUE_RESOLVER
          actualEvents[0].message shouldBe null
          actualEvents[0].reason shouldContain "JQ: No value found"
          actualEvents[0].fromCache shouldBe false
        }
        it("key value ok") {
          val given =
              PolicyVariableResolver(id = "pvr6", key = "str", source = ContextStoreEnum.SUBJECT)
          given.resolve(context)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe true
          actualEvents[0].entityId shouldBe "pvr6"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VALUE_RESOLVER
          actualEvents[0].message shouldBe "a"
          actualEvents[0].reason shouldBe null
          actualEvents[0].fromCache shouldBe false
        }
        it("JSONPath ok") {
          val given =
              PolicyVariableResolver(
                  id = "pvr7",
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  path = "object.a1",
                  key = "json")
          given.resolve(context)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe true
          actualEvents[0].entityId shouldBe "pvr7"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VALUE_RESOLVER
          actualEvents[0].message shouldBe """"b1""""
          actualEvents[0].reason shouldBe null
          actualEvents[0].fromCache shouldBe false
        }
        it("JQ ok") {
          val given =
              PolicyVariableResolver(
                  id = "pvr8",
                  path = ".user",
                  source = ContextStoreEnum.SUBJECT,
                  engine = PolicyVariableResolverEngineEnum.JQ)
          given.resolve(context)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe true
          actualEvents[0].entityId shouldBe "pvr8"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VALUE_RESOLVER
          actualEvents[0].message shouldBe "jdoe"
          actualEvents[0].reason shouldBe null
          actualEvents[0].fromCache shouldBe false
        }
      }
    })
