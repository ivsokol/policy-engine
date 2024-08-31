package com.github.ivsokol.poe.condition

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.PolicyEntityEnum
import com.github.ivsokol.poe.SemVer
import com.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import com.github.ivsokol.poe.catalog.PolicyCatalog
import com.github.ivsokol.poe.event.InMemoryEventTestHandler
import com.github.ivsokol.poe.variable.PolicyVariableDynamic
import com.github.ivsokol.poe.variable.PolicyVariableRef
import com.github.ivsokol.poe.variable.PolicyVariableResolver
import com.github.ivsokol.poe.variable.PolicyVariableStatic
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith

class PolicyConditionAtomicTest :
    DescribeSpec({
      val context =
          Context(
              request = mapOf("num" to 1.1, "num2" to 2.1, "n" to null),
              event = InMemoryEventTestHandler())
      val operationEnum = OperationEnum.GREATER_THAN

      val catalog =
          PolicyCatalog(
              id = "test-catalog",
              policyConditions = listOf(PolicyConditionDefault()),
              policyVariables =
                  listOf(
                      PolicyVariableStatic(id = "pvs1", value = 1.1),
                      PolicyVariableStatic(id = "pvs2", version = SemVer(1, 2, 3), value = 2.1),
                      PolicyVariableStatic(id = "pvs3", value = "a"),
                      PolicyVariableDynamic(
                          id = "pvd1",
                          resolvers =
                              listOf(
                                  PolicyVariableResolver(id = "pvr1", key = "n"),
                              ))),
              policyVariableResolvers = emptyList())

      afterTest {
        (context.event as InMemoryEventTestHandler).clear()
        context.removeLastFromPath()
        context.cache.clear()
      }

      describe("cache") {
        it("should cache") {
          val given =
              PolicyConditionAtomic(
                  operation = operationEnum, id = "1", args = listOf(int(0), int(1)))
          val actual = given.check(context, EmptyPolicyCatalog())
          actual shouldBe false
          val actual2 = given.check(context, EmptyPolicyCatalog())
          actual2 shouldBe false
          context.cache.getCondition("1") shouldBe false
          context.cache.getCondition("2") shouldBe null
        }
      }

      describe("variable ref") {
        it("should find variable in catalog") {
          val given =
              PolicyConditionAtomic(
                  id = "pca1",
                  operation = operationEnum,
                  args = listOf(PolicyVariableRef(id = "pvs1"), int(0)))
          val actual = given.check(context, catalog)
          actual shouldBe true
        }
        it("should find latest variable in catalog") {
          val given =
              PolicyConditionAtomic(
                  id = "pca2",
                  operation = operationEnum,
                  args = listOf(PolicyVariableRef(id = "pvs2"), int(0)))
          val actual = given.check(context, catalog)
          actual shouldBe true
        }
        it("should find exact variable in catalog") {
          val given =
              PolicyConditionAtomic(
                  id = "pca3",
                  operation = operationEnum,
                  args = listOf(PolicyVariableRef(id = "pvs2", version = SemVer(1, 2, 3)), int(0)))
          val actual = given.check(context, catalog)
          actual shouldBe true
        }
        it("should not find variable in catalog") {
          val given =
              PolicyConditionAtomic(
                  id = "pca4",
                  operation = operationEnum,
                  args = listOf(PolicyVariableRef(id = "pvs999"), int(0)))
          val actual = given.check(context, catalog)
          actual shouldBe null
        }
      }

      describe("childRefs") {
        it("should return null") {
          val actual =
              PolicyConditionAtomic(
                  id = "pca1", operation = operationEnum, args = listOf(int(0), int(1)))
          actual.childRefs() shouldBe null
        }

        it("should return distinct") {
          val given =
              PolicyConditionAtomic(
                  id = "pca1",
                  operation = operationEnum,
                  args = listOf(PolicyVariableRef(id = "pvr1"), PolicyVariableRef(id = "pvr2")))
          val actual = given.childRefs()
          actual shouldNotBe null
          actual?.shouldHaveSize(2)
        }

        it("should return one") {
          val given =
              PolicyConditionAtomic(
                  id = "pca1",
                  operation = operationEnum,
                  args = listOf(PolicyVariableRef(id = "pvr1"), PolicyVariableRef(id = "pvr1")))
          val actual = given.childRefs()
          actual shouldNotBe null
          actual?.shouldHaveSize(1)
        }
      }

      describe("identity") {
        it("should return empty") {
          val actual =
              PolicyConditionAtomic(operation = operationEnum, args = listOf(int(0), int(1)))
          actual.identity() shouldBe ""
        }
        it("should throw when bad id") {
          shouldThrow<IllegalArgumentException> {
                PolicyConditionAtomic(
                    operation = operationEnum, id = " ", args = listOf(int(0), int(1)))
              }
              .message shouldBe "Id must not be blank"
        }
        it("should return id") {
          val actual =
              PolicyConditionAtomic(
                  operation = operationEnum, id = "1", args = listOf(int(0), int(1)))
          actual.identity() shouldBe "1"
        }
        it("should return idVer") {
          val actual =
              PolicyConditionAtomic(
                  operation = operationEnum,
                  id = "1",
                  version = SemVer(1, 0, 0),
                  args = listOf(int(0), int(1)))
          actual.identity() shouldBe "1:1.0.0"
        }
      }

      describe("events") {
        it("ok response") {
          val given =
              PolicyConditionAtomic(
                  id = "pca1", operation = operationEnum, args = listOf(int(1), int(0)))
          given.check(context, EmptyPolicyCatalog())
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC }
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe true
          actualEvents[0].entityId shouldBe "pca1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_ATOMIC
          actualEvents[0].reason shouldBe null
          actualEvents[0].message shouldBe true.toString()
          actualEvents[0].fromCache shouldBe false
        }
        it("found in cache") {
          val given =
              PolicyConditionAtomic(
                  id = "pca2", operation = operationEnum, args = listOf(int(1), int(0)))
          given.check(context, EmptyPolicyCatalog())
          (context.event as InMemoryEventTestHandler).clear()
          context.removeLastFromPath()
          given.check(context, EmptyPolicyCatalog())
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC }
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe true
          actualEvents[0].entityId shouldBe "pca2"
          actualEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_ATOMIC
          actualEvents[0].reason shouldBe null
          actualEvents[0].message shouldBe true.toString()
          actualEvents[0].fromCache shouldBe true
        }
        it("null value returned") {
          val given =
              PolicyConditionAtomic(
                  id = "pca3",
                  operation = operationEnum,
                  args = listOf(PolicyVariableRef(id = "pvd1"), int(0)))
          given.check(context, catalog)
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC }
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pca3"
          actualEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_ATOMIC
          actualEvents[0].reason shouldBe null
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false
        }
        it("cast to first exception") {
          val given =
              PolicyConditionAtomic(
                  id = "pca4",
                  operation = operationEnum,
                  args = listOf(int(0), PolicyVariableRef(id = "pvs3")))
          given.check(context, catalog)
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC }
          actualEvents shouldHaveSize 2
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pca4"
          actualEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_ATOMIC
          actualEvents[0].reason shouldStartWith "java.lang.NumberFormatException:"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false

          actualEvents[1].contextId shouldNotBe null
          actualEvents[1].success shouldBe false
          actualEvents[1].entityId shouldBe "pca4"
          actualEvents[1].entity shouldBe PolicyEntityEnum.CONDITION_ATOMIC
          actualEvents[1].reason shouldBe null
          actualEvents[1].message shouldBe null
          actualEvents[1].fromCache shouldBe false

          actualEvents[1].timestamp shouldBeAfter actualEvents[0].timestamp
        }
        it("compliance check exception") {
          val given =
              PolicyConditionAtomic(
                  id = "pca4",
                  operation = OperationEnum.CONTAINS,
                  args = listOf(int(0), PolicyVariableRef(id = "pvs3")))
          given.check(context, catalog)
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC }
          actualEvents shouldHaveSize 2
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pca4"
          actualEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_ATOMIC
          actualEvents[0].reason shouldContain "Variable type INT is not supported in condition"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false

          actualEvents[1].contextId shouldNotBe null
          actualEvents[1].success shouldBe false
          actualEvents[1].entityId shouldBe "pca4"
          actualEvents[1].entity shouldBe PolicyEntityEnum.CONDITION_ATOMIC
          actualEvents[1].reason shouldBe null
          actualEvents[1].message shouldBe null
          actualEvents[1].fromCache shouldBe false

          actualEvents[1].timestamp shouldBeAfter actualEvents[0].timestamp
        }
        it("operation exception") {
          val given =
              PolicyConditionAtomic(
                  id = "pca5",
                  operation = OperationEnum.STARTS_WITH,
                  args = listOf(PolicyVariableStatic(value = listOf(1, 2, 3)), string("1")))
          given.check(context, catalog)
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC }
          actualEvents shouldHaveSize 2
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pca5"
          actualEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_ATOMIC
          actualEvents[0].reason shouldContain
              "java.lang.ClassCastException:class java.lang.String cannot be cast to class java.lang.Integer"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false

          actualEvents[1].contextId shouldNotBe null
          actualEvents[1].success shouldBe false
          actualEvents[1].entityId shouldBe "pca5"
          actualEvents[1].entity shouldBe PolicyEntityEnum.CONDITION_ATOMIC
          actualEvents[1].reason shouldBe null
          actualEvents[1].message shouldBe null
          actualEvents[1].fromCache shouldBe false

          actualEvents[1].timestamp shouldBeAfter actualEvents[0].timestamp
        }
      }

      describe("naming") {
        it("not named") {
          val given =
              PolicyConditionAtomic(operation = operationEnum, args = listOf(int(0), int(1)))
          given.check(context, EmptyPolicyCatalog())
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe ""
        }
        it("child") {
          val ctx = Context(event = InMemoryEventTestHandler())
          ctx.addToPath("conditions")
          ctx.addToPath("0")
          val given =
              PolicyConditionAtomic(operation = operationEnum, args = listOf(int(0), int(1)))
          given.check(ctx, EmptyPolicyCatalog())
          val actualEvents =
              ctx.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe "conditions/0"
        }
        it("named child") {
          val ctx = Context(event = InMemoryEventTestHandler())
          ctx.addToPath("conditions")
          ctx.addToPath("0")
          val given =
              PolicyConditionAtomic(
                  id = "pca1", operation = operationEnum, args = listOf(int(0), int(1)))
          given.check(ctx, EmptyPolicyCatalog())
          val actualEvents =
              ctx.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe "conditions/0(pca1)"
        }
        it("variable naming") {
          val ctx = Context(event = InMemoryEventTestHandler())
          val given =
              PolicyConditionAtomic(
                  operation = operationEnum, args = listOf(PolicyVariableRef(id = "pvs1"), int(0)))
          given.check(ctx, catalog)
          val actualEvents = ctx.event.list()
          actualEvents shouldHaveSize 3
          actualEvents[0].entityId shouldBe "args/0(pvs1)"
          actualEvents[1].entityId shouldBe "args/1"
          actualEvents[2].entityId shouldBe ""
        }
        it("variable naming for named condition") {
          val ctx = Context(event = InMemoryEventTestHandler())
          val given =
              PolicyConditionAtomic(
                  id = "pca1",
                  operation = operationEnum,
                  args = listOf(PolicyVariableRef(id = "pvs2"), int(0)))
          given.check(ctx, catalog)
          val actualEvents = ctx.event.list()
          actualEvents shouldHaveSize 3
          actualEvents[0].entityId shouldBe "pca1/args/0(pvs2:1.2.3)"
          actualEvents[1].entityId shouldBe "pca1/args/1"
          actualEvents[2].entityId shouldBe "pca1"
        }
      }
    })
