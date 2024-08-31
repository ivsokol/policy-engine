package io.github.ivsokol.poe.variable

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.condition.PolicyConditionDefault
import io.github.ivsokol.poe.event.InMemoryEventTestHandler
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Period

private val fullObjectBody =
    mapOf(
        "str" to "a",
        "str2" to "b",
        "i" to 1,
        "b" to true,
        "b1" to "true",
        "n" to null,
        "num" to 1.1,
        "date" to LocalDate.parse("2024-12-19"),
        "dateTime" to OffsetDateTime.parse("2024-12-19T12:00:00Z"),
        "period" to Period.parse("P1D"),
        "duration" to Duration.parse("PT1H"),
        "bigD" to BigDecimal("3.14"),
        "long" to Long.MAX_VALUE,
        "array" to listOf(1, 2, 3),
        "object" to mapOf("a1" to "b1"),
        "strObj" to """{"a2":"b2"}""",
    )

class PolicyVariableDynamicTest :
    DescribeSpec({
      val context = Context(request = fullObjectBody, event = InMemoryEventTestHandler())
      val catalog =
          PolicyCatalog(
              id = "test-catalog",
              policyConditions = listOf(PolicyConditionDefault()),
              policyVariables = emptyList(),
              policyVariableResolvers =
                  listOf(
                      PolicyVariableResolver(id = "pvr1", key = "str"),
                      PolicyVariableResolver(id = "pvr2", version = SemVer(1, 2, 3), key = "str2"),
                  ))

      afterTest {
        (context.event as InMemoryEventTestHandler).clear()
        context.removeLastFromPath()
        context.cache.clear()
      }

      describe("cache") {
        it("should resolve to String and not found in cache") {
          val given =
              PolicyVariableDynamic(
                  resolvers = listOf(PolicyVariableResolver(key = "str")),
                  type = VariableValueTypeEnum.STRING)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.STRING
          actual.body shouldBe "a"

          context.cache.get(
              io.github.ivsokol.poe.cache.PolicyStoreCacheEnum.VARIABLE, "str") shouldBe null
        }

        it("should resolve to String and found in cache") {
          val given =
              PolicyVariableDynamic(
                  id = "1",
                  resolvers = listOf(PolicyVariableResolver(key = "str")),
                  type = VariableValueTypeEnum.STRING)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.STRING
          actual.body shouldBe "a"

          // code coverage test
          val actual2 = given.resolve(context, EmptyPolicyCatalog())
          actual2.type shouldBe VariableRuntimeTypeEnum.STRING
          actual2.body shouldBe "a"

          context.cache.getVariable("1") shouldBe VariableValue(VariableRuntimeTypeEnum.STRING, "a")
        }
      }

      describe("priority") {
        it("should pick first one to be defined") {
          val given =
              PolicyVariableDynamic(
                  id = "pvd1",
                  resolvers =
                      listOf(
                          PolicyVariableResolver(key = "str"),
                          PolicyVariableResolver(key = "str2")),
                  type = VariableValueTypeEnum.STRING)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.STRING
          actual.body shouldBe "a"
        }

        it("should skip null values") {
          val given =
              PolicyVariableDynamic(
                  resolvers =
                      listOf(
                          PolicyVariableResolver(key = "str3"),
                          PolicyVariableResolver(key = "str2"),
                          PolicyVariableResolver(key = "str"),
                      ),
                  type = VariableValueTypeEnum.STRING)
          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual.type shouldBe VariableRuntimeTypeEnum.STRING
          actual.body shouldBe "b"
        }
      }

      describe("exception") {
        it("should return NullVariableValue when exception is thrown") {
          val given =
              PolicyVariableDynamic(
                  id = "1",
                  resolvers =
                      listOf(
                          PolicyVariableResolver(
                              path = ".long", engine = PolicyVariableResolverEngineEnum.JQ)),
                  type = VariableValueTypeEnum.INT)

          val actual = given.resolve(context, EmptyPolicyCatalog())
          actual shouldBe NullVariableValue()
        }
      }

      describe("childRefs") {
        it("should return null") {
          val actual =
              PolicyVariableDynamic(
                  resolvers =
                      listOf(
                          PolicyVariableResolver(key = "str3"),
                          PolicyVariableResolver(key = "str2"),
                          PolicyVariableResolver(key = "str"),
                      ),
                  type = VariableValueTypeEnum.STRING)
          actual.childRefs() shouldBe null
        }

        it("should return") {
          val given =
              PolicyVariableDynamic(
                  resolvers =
                      listOf(
                          PolicyVariableResolverRef(id = "str3"),
                          PolicyVariableResolverRef(id = "str3"),
                          PolicyVariableResolverRef(id = "str3", version = SemVer(1, 2, 3)),
                          PolicyVariableResolver(key = "str2"),
                      ),
                  type = VariableValueTypeEnum.STRING)
          val actual = given.childRefs()
          actual shouldNotBe null
          actual?.shouldHaveSize(2)
        }
      }

      describe("resolver ref") {
        it("should find resolver in catalog") {
          val given =
              PolicyVariableDynamic(
                  id = "pvd1",
                  resolvers = listOf(PolicyVariableResolverRef(id = "pvr1")),
                  type = VariableValueTypeEnum.STRING)
          val actual = given.resolve(context, catalog)
          actual.type shouldBe VariableRuntimeTypeEnum.STRING
          actual.body shouldBe "a"
        }
        it("should find latest resolver in catalog") {
          val given =
              PolicyVariableDynamic(
                  id = "pvd2",
                  resolvers = listOf(PolicyVariableResolverRef(id = "pvr2")),
                  type = VariableValueTypeEnum.STRING)
          val actual = given.resolve(context, catalog)
          actual.type shouldBe VariableRuntimeTypeEnum.STRING
          actual.body shouldBe "b"
        }
        it("should find exact resolver in catalog") {
          val given =
              PolicyVariableDynamic(
                  id = "pvd3",
                  resolvers =
                      listOf(PolicyVariableResolverRef(id = "pvr2", version = SemVer(1, 2, 3))),
                  type = VariableValueTypeEnum.STRING)
          val actual = given.resolve(context, catalog)
          actual.type shouldBe VariableRuntimeTypeEnum.STRING
          actual.body shouldBe "b"
        }
        it("should not find resolver in catalog") {
          val given =
              PolicyVariableDynamic(
                  id = "pvd4",
                  resolvers = listOf(PolicyVariableResolverRef(id = "pvr3")),
                  type = VariableValueTypeEnum.STRING)
          val actual = given.resolve(context, catalog)
          actual shouldBe NullVariableValue()
        }
      }

      describe("identity") {
        it("should return empty") {
          val actual =
              PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolverRef(id = "1")))
          actual.identity() shouldBe ""
        }
        it("should throw when bad id") {
          shouldThrow<IllegalArgumentException> {
                PolicyVariableDynamic(
                    id = " ", resolvers = listOf(PolicyVariableResolverRef(id = "1")))
              }
              .message shouldBe "Id must not be blank"
        }
        it("should return id") {
          val actual =
              PolicyVariableDynamic(
                  id = "1", resolvers = listOf(PolicyVariableResolverRef(id = "1")))
          actual.identity() shouldBe "1"
        }
        it("should return idVer") {
          val actual =
              PolicyVariableDynamic(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  resolvers = listOf(PolicyVariableResolverRef(id = "1")))
          actual.identity() shouldBe "1:1.0.0"
        }
      }

      describe("events") {
        it("found in cache") {
          val given =
              PolicyVariableDynamic(
                  id = "pvd1", resolvers = listOf(PolicyVariableResolver(key = "b")))
          given.resolve(context, EmptyPolicyCatalog())
          (context.event as InMemoryEventTestHandler).clear()
          context.removeLastFromPath()
          given.resolve(context, EmptyPolicyCatalog())
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC }
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe true
          actualEvents[0].entityId shouldBe "pvd1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VARIABLE_DYNAMIC
          actualEvents[0].reason shouldBe null
          actualEvents[0].message shouldBe
              VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true).toString()
          actualEvents[0].fromCache shouldBe true
        }
        it("no coercions") {
          val given =
              PolicyVariableDynamic(
                  id = "pvd2", resolvers = listOf(PolicyVariableResolver(key = "b")))
          given.resolve(context, EmptyPolicyCatalog())
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC }
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe true
          actualEvents[0].entityId shouldBe "pvd2"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VARIABLE_DYNAMIC
          actualEvents[0].reason shouldBe null
          actualEvents[0].message shouldBe
              VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true).toString()
          actualEvents[0].fromCache shouldBe false
        }
        it("coerced value") {
          val given =
              PolicyVariableDynamic(
                  id = "pvd3",
                  resolvers = listOf(PolicyVariableResolver(key = "b1")),
                  type = VariableValueTypeEnum.BOOLEAN)
          given.resolve(context, EmptyPolicyCatalog())
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC }
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe true
          actualEvents[0].entityId shouldBe "pvd3"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VARIABLE_DYNAMIC
          actualEvents[0].reason shouldBe null
          actualEvents[0].message shouldBe
              VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true).toString()
          actualEvents[0].fromCache shouldBe false
        }
        it("exception") {
          val given =
              PolicyVariableDynamic(
                  id = "pvd4",
                  resolvers =
                      listOf(
                          PolicyVariableResolver(
                              path = ".long", engine = PolicyVariableResolverEngineEnum.JQ)),
                  type = VariableValueTypeEnum.INT)

          given.resolve(context, EmptyPolicyCatalog())
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC }
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pvd4"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VARIABLE_DYNAMIC
          actualEvents[0].reason shouldContain "For input string"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false
        }
        it("exception in resolver ref") {
          val given =
              PolicyVariableDynamic(
                  id = "pvd4",
                  resolvers = listOf(PolicyVariableResolverRef(id = "pvr3")),
                  type = VariableValueTypeEnum.STRING)
          given.resolve(context, catalog)
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC }
          actualEvents shouldHaveSize 2
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pvd4"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VARIABLE_DYNAMIC
          actualEvents[0].reason shouldContain
              "PolicyVariableResolverRef(pvr3:null) not found in catalog"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false

          actualEvents[1].contextId shouldNotBe null
          actualEvents[1].success shouldBe true
          actualEvents[1].entityId shouldBe "pvd4"
          actualEvents[1].entity shouldBe PolicyEntityEnum.VARIABLE_DYNAMIC
          actualEvents[1].reason shouldBe null
          actualEvents[1].message shouldBe NullVariableValue().toString()
          actualEvents[1].fromCache shouldBe false

          actualEvents[1].timestamp shouldBeAfter actualEvents[0].timestamp
        }
      }

      describe("naming") {
        it("not named") {
          val ctx = Context(event = InMemoryEventTestHandler())
          val given = PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolver(key = "b")))
          given.resolve(ctx, EmptyPolicyCatalog())
          val actualEvents =
              ctx.event.list().filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe ""
        }
        it("child") {
          val ctx = Context(event = InMemoryEventTestHandler())
          ctx.addToPath("variables")
          ctx.addToPath("0")
          val given = PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolver(key = "b")))
          given.resolve(ctx, EmptyPolicyCatalog())
          val actualEvents =
              ctx.event.list().filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe "variables/0"
        }
        it("named child") {
          val ctx = Context(event = InMemoryEventTestHandler())
          ctx.addToPath("variables")
          ctx.addToPath("0")
          val given =
              PolicyVariableDynamic(
                  id = "pvd1", resolvers = listOf(PolicyVariableResolver(key = "b")))
          given.resolve(ctx, EmptyPolicyCatalog())
          val actualEvents =
              ctx.event.list().filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe "variables/0(pvd1)"
        }
        it("resolver naming") {
          val ctx = Context(event = InMemoryEventTestHandler())
          val given =
              PolicyVariableDynamic(
                  resolvers =
                      listOf(
                          PolicyVariableResolver(id = "pvr11", key = "none"),
                          PolicyVariableResolver(key = "none2"),
                          PolicyVariableResolverRef(id = "pvr3", version = SemVer(1, 0, 0)),
                          PolicyVariableResolverRef(id = "pvr4"),
                          PolicyVariableResolverRef(id = "pvr1"),
                      ))
          given.resolve(ctx, catalog)
          val actualEvents = ctx.event.list()
          actualEvents shouldHaveSize 6
          actualEvents[0].entityId shouldBe "resolvers/0(pvr11)"
          actualEvents[1].entityId shouldBe "resolvers/1"
          actualEvents[2].entityId shouldBe ""
          actualEvents[3].entityId shouldBe ""
          actualEvents[4].entityId shouldBe "resolvers/4(pvr1)"
          actualEvents[5].entityId shouldBe ""
        }
        it("resolver naming for named variable") {
          val ctx = Context(event = InMemoryEventTestHandler())
          val given =
              PolicyVariableDynamic(
                  id = "pvd1",
                  resolvers =
                      listOf(
                          PolicyVariableResolver(id = "pvr11", key = "none"),
                          PolicyVariableResolver(key = "none2"),
                          PolicyVariableResolverRef(id = "pvr3", version = SemVer(1, 0, 0)),
                          PolicyVariableResolverRef(id = "pvr4"),
                          PolicyVariableResolverRef(id = "pvr1"),
                      ))
          given.resolve(ctx, catalog)
          val actualEvents = ctx.event.list()
          actualEvents shouldHaveSize 6
          actualEvents[0].entityId shouldBe "pvd1/resolvers/0(pvr11)"
          actualEvents[1].entityId shouldBe "pvd1/resolvers/1"
          actualEvents[2].entityId shouldBe "pvd1"
          actualEvents[3].entityId shouldBe "pvd1"
          actualEvents[4].entityId shouldBe "pvd1/resolvers/4(pvr1)"
          actualEvents[5].entityId shouldBe "pvd1"
        }
      }
    })
