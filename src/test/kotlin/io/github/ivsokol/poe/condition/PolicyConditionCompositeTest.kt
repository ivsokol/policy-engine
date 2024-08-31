package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.event.InMemoryEventTestHandler
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class PolicyConditionCompositeTest :
    DescribeSpec({
      val context = Context(event = InMemoryEventTestHandler())
      val operationEnum = OperationEnum.GREATER_THAN
      val catalog =
          PolicyCatalog(
              id = "test-catalog",
              policyConditions =
                  listOf(
                      PolicyConditionAtomic(
                          id = "pca1", operation = operationEnum, args = listOf(int(1), int(0))),
                      PolicyConditionAtomic(
                          id = "pca2",
                          version = SemVer(1, 2, 3),
                          operation = operationEnum,
                          args = listOf(int(1), int(2)))),
              policyVariables = emptyList(),
              policyVariableResolvers = emptyList())

      afterTest {
        (context.event as InMemoryEventTestHandler).clear()
        context.removeLastFromPath()
        context.cache.clear()
      }

      describe("allOf") {
        it("should return true") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions = listOf(fTrue(), fTrue()))
          val actual = given.check(context, catalog)
          actual shouldBe true
        }
        it("should return false") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions = listOf(fTrue(), fFalse()))
          val actual = given.check(context, catalog)
          actual shouldBe false
        }
        it("should return false if strict check set to false") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  strictCheck = false,
                  conditions = listOf(fTrue(), fFalse()))
          val actual = given.check(context, catalog)
          actual shouldBe false
        }

        it("should return null") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions = listOf(fTrue(), fNull()))
          val actual = given.check(context, catalog)
          actual shouldBe null
        }
        it("should return true on strictCheck set to false") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  strictCheck = false,
                  conditions = listOf(fTrue(), fNull()))
          val actual = given.check(context, catalog)
          actual shouldBe true
        }
        it("should negate") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  negateResult = true,
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions = listOf(fTrue(), fTrue()))
          val actual = given.check(context, catalog)
          actual shouldBe false
        }
      }

      describe("anyOf") {
        it("should return true") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ANY_OF,
                  conditions = listOf(fFalse(), fTrue()))
          val actual = given.check(context, catalog)
          actual shouldBe true
        }
        it("should return true if strict check set to false") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ANY_OF,
                  strictCheck = false,
                  conditions = listOf(fFalse(), fTrue()))
          val actual = given.check(context, catalog)
          actual shouldBe true
        }
        it("should return false") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ANY_OF,
                  conditions = listOf(fFalse(), fFalse()))
          val actual = given.check(context, catalog)
          actual shouldBe false
        }

        it("should return null") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ANY_OF,
                  conditions = listOf(fFalse(), fNull()))
          val actual = given.check(context, catalog)
          actual shouldBe null
        }
        it("should return false if strict check set to false") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ANY_OF,
                  strictCheck = false,
                  conditions = listOf(fFalse(), fNull()))
          val actual = given.check(context, catalog)
          actual shouldBe false
        }

        it("should negate") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  negateResult = true,
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ANY_OF,
                  conditions = listOf(fFalse(), fTrue()))
          val actual = given.check(context, catalog)
          actual shouldBe false
        }
      }

      describe("not") {
        it("should return true") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.NOT,
                  conditions = listOf(fFalse()))
          val actual = given.check(context, catalog)
          actual shouldBe true
        }
        it("should return false") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.NOT,
                  conditions = listOf(fTrue()))
          val actual = given.check(context, catalog)
          actual shouldBe false
        }

        it("should return null") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.NOT,
                  conditions = listOf(fNull()))
          val actual = given.check(context, catalog)
          actual shouldBe null
        }
        it("should throw exception") {
          shouldThrow<IllegalArgumentException> {
                PolicyConditionComposite(
                    id = "pcc1", // id
                    conditionCombinationLogic = ConditionCombinationLogicEnum.NOT,
                    conditions = listOf(fNull(), fTrue()))
              }
              .message shouldBe "pcc1:NOT condition must have exactly one condition"
        }
        it("should negate") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  negateResult = true,
                  conditionCombinationLogic = ConditionCombinationLogicEnum.NOT,
                  conditions = listOf(fFalse()))
          val actual = given.check(context, catalog)
          actual shouldBe false
        }
      }

      describe("nOf") {
        it("true") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                  minimumConditions = 3,
                  conditions =
                      listOf(
                          fTrue(),
                          fTrue(),
                          fFalse(),
                          fNull(),
                          fTrue(),
                      ))
          val actual = given.check(context, EmptyPolicyCatalog())
          actual shouldBe true
          val childEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_DEFAULT }
          childEvents.size shouldBe 5
        }
        it("fast true") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                  minimumConditions = 3,
                  conditions =
                      listOf(
                          fTrue(),
                          fTrue(),
                          fTrue(),
                          fFalse(),
                          fNull(),
                      ))
          val actual = given.check(context, EmptyPolicyCatalog())
          actual shouldBe true
          val childEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_DEFAULT }
          childEvents.size shouldBe 3
        }
        it("false") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                  minimumConditions = 3,
                  conditions =
                      listOf(
                          fTrue(),
                          fTrue(),
                          fFalse(),
                          fFalse(),
                          fFalse(),
                      ))
          val actual = given.check(context, EmptyPolicyCatalog())
          actual shouldBe false
          val childEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_DEFAULT }
          childEvents.size shouldBe 5
        }
        it("fast false") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                  minimumConditions = 3,
                  conditions =
                      listOf(
                          fFalse(),
                          fFalse(),
                          fFalse(),
                          fTrue(),
                          fTrue(),
                      ))
          val actual = given.check(context, EmptyPolicyCatalog())
          actual shouldBe false
          val childEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_DEFAULT }
          childEvents.size shouldBe 3
        }
        it("null") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                  minimumConditions = 3,
                  conditions =
                      listOf(
                          fTrue(),
                          fTrue(),
                          fFalse(),
                          fFalse(),
                          fNull(),
                      ))
          val actual = given.check(context, EmptyPolicyCatalog())
          actual shouldBe null
          val childEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_DEFAULT }
          childEvents.size shouldBe 5
        }
        it("fast null") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                  minimumConditions = 3,
                  conditions =
                      listOf(
                          fNull(),
                          fNull(),
                          fNull(),
                          fTrue(),
                          fTrue(),
                      ))
          val actual = given.check(context, EmptyPolicyCatalog())
          actual shouldBe null
          val childEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_DEFAULT }
          childEvents.size shouldBe 3
        }
        it("optimized null with false result") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                  minimumConditions = 3,
                  optimizeNOfRun = true,
                  conditions =
                      listOf(
                          fNull(),
                          fFalse(),
                          fNull(),
                          fFalse(),
                          fFalse(),
                      ))
          val actual = given.check(context, EmptyPolicyCatalog())
          actual shouldBe null
          val childEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_DEFAULT }
          childEvents.size shouldBe 3
        }
        it("non optimized null with null result") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                  minimumConditions = 3,
                  optimizeNOfRun = true,
                  conditions =
                      listOf(
                          fNull(),
                          fFalse(),
                          fNull(),
                          fTrue(),
                          fFalse(),
                      ))
          val actual = given.check(context, EmptyPolicyCatalog())
          actual shouldBe null
          val childEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_DEFAULT }
          childEvents.size shouldBe 3
        }
        it("non optimized null with false result") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                  minimumConditions = 3,
                  conditions =
                      listOf(
                          fNull(),
                          fFalse(),
                          fNull(),
                          fFalse(),
                          fFalse(),
                      ))
          val actual = given.check(context, EmptyPolicyCatalog())
          actual shouldBe false
          val childEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_DEFAULT }
          childEvents.size shouldBe 5
        }
        it("non optimized null with null result") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                  minimumConditions = 3,
                  conditions =
                      listOf(
                          fNull(),
                          fFalse(),
                          fNull(),
                          fTrue(),
                          fFalse(),
                      ))
          val actual = given.check(context, EmptyPolicyCatalog())
          actual shouldBe null
          val childEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_DEFAULT }
          childEvents.size shouldBe 5
        }
      }

      describe("nOfExceptions") {
        it("no minimumConditions") {
          shouldThrow<IllegalArgumentException> {
                PolicyConditionComposite(
                    id = "pcc1", // id
                    conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                    conditions = listOf(fNull(), fTrue()))
              }
              .message shouldBe "pcc1:Minimum conditions must not be null"
        }
        it("zero minimumConditions") {
          shouldThrow<IllegalArgumentException> {
                PolicyConditionComposite(
                    id = "pcc1", // id
                    conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                    minimumConditions = 0,
                    conditions = listOf(fNull(), fTrue()))
              }
              .message shouldBe "pcc1:Minimum conditions must be greater than 0"
        }
        it("too big minimumConditions") {
          shouldThrow<IllegalArgumentException> {
                PolicyConditionComposite(
                    id = "pcc1", // id
                    conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                    minimumConditions = 3,
                    conditions = listOf(fNull(), fTrue()))
              }
              .message shouldBe
              "pcc1:Minimum conditions must be less than or equal to number of conditions"
        }
      }

      describe("condition ref") {
        it("should find condition in catalog") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1",
                  conditionCombinationLogic = ConditionCombinationLogicEnum.NOT,
                  conditions = listOf(PolicyConditionRef(id = "pca1")))
          val actual = given.check(context, catalog)
          actual shouldBe false
        }
        it("should find latest condition in catalog") {
          val given =
              PolicyConditionComposite(
                  id = "pcc2",
                  conditionCombinationLogic = ConditionCombinationLogicEnum.NOT,
                  conditions = listOf(PolicyConditionRef(id = "pca2")))
          val actual = given.check(context, catalog)
          actual shouldBe true
        }
        it("should find exact condition in catalog") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1",
                  conditionCombinationLogic = ConditionCombinationLogicEnum.NOT,
                  conditions = listOf(PolicyConditionRef(id = "pca2", version = SemVer(1, 2, 3))))
          val actual = given.check(context, catalog)
          actual shouldBe true
        }
        it("should not find condition in catalog") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1",
                  conditionCombinationLogic = ConditionCombinationLogicEnum.NOT,
                  conditions = listOf(PolicyConditionRef(id = "pca999")))
          val actual = given.check(context, catalog)
          actual shouldBe null
        }
      }

      describe("childRefs") {
        it("should return null") {
          val actual =
              PolicyConditionComposite(
                  id = "pcc1",
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions = listOf(fTrue(), fTrue()))
          actual.childRefs() shouldBe null
        }

        it("should return distinct") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1",
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions =
                      listOf(PolicyConditionRef(id = "pca1"), PolicyConditionRef(id = "pca2")))
          val actual = given.childRefs()
          actual shouldNotBe null
          actual?.shouldHaveSize(2)
        }

        it("should return one") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1",
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions =
                      listOf(PolicyConditionRef(id = "pca1"), PolicyConditionRef(id = "pca1")))
          val actual = given.childRefs()
          actual shouldNotBe null
          actual?.shouldHaveSize(1)
        }
      }

      describe("identity") {
        it("should return empty") {
          val actual =
              PolicyConditionComposite(
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions = listOf(fTrue(), fTrue()))
          actual.identity() shouldBe ""
        }
        it("should throw when bad id") {
          shouldThrow<IllegalArgumentException> {
                PolicyConditionComposite(
                    id = " ",
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(fTrue(), fTrue()))
              }
              .message shouldBe "Id must not be blank"
        }
        it("should return id") {
          val actual =
              PolicyConditionComposite(
                  id = "pcc1",
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions = listOf(fTrue(), fTrue()))
          actual.identity() shouldBe "pcc1"
        }
        it("should return idVer") {
          val actual =
              PolicyConditionComposite(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions = listOf(fTrue(), fTrue()))
          actual.identity() shouldBe "1:1.0.0"
        }
      }

      describe("cache") {
        it("should cache") {
          val ctx = context.copy(cache = io.github.ivsokol.poe.cache.HashMapCache())
          val given =
              PolicyConditionComposite(
                  id = "1",
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions = listOf(PolicyConditionDefault(true)))
          val actual = given.check(ctx, EmptyPolicyCatalog())
          actual shouldBe true
          val actual2 = given.check(ctx, EmptyPolicyCatalog())
          actual2 shouldBe true
          ctx.cache.getCondition("1") shouldBe true
          ctx.cache.getCondition("2") shouldBe null
        }
      }

      describe("events") {
        it("ok response") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions = listOf(fTrue(), fTrue()))
          given.check(context, EmptyPolicyCatalog())
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_COMPOSITE }
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe true
          actualEvents[0].entityId shouldBe "pcc1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_COMPOSITE
          actualEvents[0].reason shouldBe null
          actualEvents[0].message shouldBe true.toString()
          actualEvents[0].fromCache shouldBe false
        }
        it("found in cache") {
          val given =
              PolicyConditionComposite(
                  id = "pcc2", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions = listOf(fTrue(), fTrue()))
          given.check(context, EmptyPolicyCatalog())
          (context.event as InMemoryEventTestHandler).clear()
          context.removeLastFromPath()
          given.check(context, EmptyPolicyCatalog())
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_COMPOSITE }
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe true
          actualEvents[0].entityId shouldBe "pcc2"
          actualEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_COMPOSITE
          actualEvents[0].reason shouldBe null
          actualEvents[0].message shouldBe true.toString()
          actualEvents[0].fromCache shouldBe true
        }
        it("null value returned") {
          val given =
              PolicyConditionComposite(
                  id = "pcc3", // id
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions = listOf(fTrue(), fNull()))
          given.check(context, catalog)
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_COMPOSITE }
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pcc3"
          actualEvents[0].entity shouldBe PolicyEntityEnum.CONDITION_COMPOSITE
          actualEvents[0].reason shouldBe null
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false
        }
      }

      describe("naming") {
        it("not named") {
          val given =
              PolicyConditionComposite(
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions = listOf(fTrue(), fNull()))
          given.check(context, EmptyPolicyCatalog())
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_COMPOSITE }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe ""
        }
        it("child") {
          context.addToPath("conditions")
          context.addToPath("0")
          val given =
              PolicyConditionComposite(
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions = listOf(fTrue(), fNull()))
          given.check(context, EmptyPolicyCatalog())
          context.removeLastFromPath()
          context.removeLastFromPath()
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_COMPOSITE }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe "conditions/0"
        }
        it("named child") {
          context.addToPath("conditions")
          context.addToPath("0")
          val given =
              PolicyConditionComposite(
                  id = "pcc1",
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions = listOf(fTrue(), fNull()))
          given.check(context, EmptyPolicyCatalog())
          context.removeLastFromPath()
          context.removeLastFromPath()
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.CONDITION_COMPOSITE }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe "conditions/0(pcc1)"
        }
        it("condition naming") {
          val given =
              PolicyConditionComposite(
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions =
                      listOf(
                          PolicyConditionRef(id = "pca1"),
                          PolicyConditionAtomic(
                              operation = operationEnum, args = listOf(int(1), int(0))),
                      ))
          given.check(context, catalog)
          val actualEvents =
              context.event.list().filter {
                it.entity in
                    listOf(
                        PolicyEntityEnum.CONDITION_COMPOSITE,
                        PolicyEntityEnum.CONDITION_ATOMIC,
                        PolicyEntityEnum.CONDITION_DEFAULT)
              }
          actualEvents shouldHaveSize 3
          actualEvents[0].entityId shouldBe "conditions/0(pca1)"
          actualEvents[1].entityId shouldBe "conditions/1"
          actualEvents[2].entityId shouldBe ""
        }
        it("variable naming for named condition") {
          val given =
              PolicyConditionComposite(
                  id = "pcc1",
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions =
                      listOf(
                          PolicyConditionRef(id = "pca1"),
                          PolicyConditionAtomic(
                              operation = operationEnum, args = listOf(int(1), int(0))),
                      ))
          given.check(context, catalog)
          val actualEvents =
              context.event.list().filter {
                it.entity in
                    listOf(
                        PolicyEntityEnum.CONDITION_COMPOSITE,
                        PolicyEntityEnum.CONDITION_ATOMIC,
                        PolicyEntityEnum.CONDITION_DEFAULT)
              }
          actualEvents shouldHaveSize 3
          actualEvents[0].entityId shouldBe "pcc1/conditions/0(pca1)"
          actualEvents[1].entityId shouldBe "pcc1/conditions/1"
          actualEvents[2].entityId shouldBe "pcc1"
        }
      }
    })

fun fTrue() = PolicyConditionDefault(true)

fun fFalse() = PolicyConditionDefault(false)

fun fNull() = PolicyConditionDefault()
