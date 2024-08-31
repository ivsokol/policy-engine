package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.time.Period

private data class IsZeroTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class IsZeroTest :
    DescribeSpec({
      val context = Context()

      val operationEnum = OperationEnum.IS_ZERO

      describe("logic") {
        withData(
            nameFn = { "IsZero: ${it.name}" },
            listOf(
                IsZeroTestData(
                    name = "0",
                    given = PolicyConditionAtomic(operation = operationEnum, args = listOf(int(0))),
                    expected = true),
                IsZeroTestData(
                    name = "1",
                    given = PolicyConditionAtomic(operation = operationEnum, args = listOf(int(1))),
                    expected = false),
                IsZeroTestData(
                    name = "-1",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(int(-1))),
                    expected = false),
                IsZeroTestData(
                    "a",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("a"))),
                    expected = null),
                IsZeroTestData(
                    "1f",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(float(1f))),
                    expected = false),
                IsZeroTestData(
                    "0f",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(float(0f))),
                    expected = true),
                IsZeroTestData(
                    "-1f",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(float(-1f))),
                    expected = false),
                IsZeroTestData(
                    "1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(1.0))),
                    expected = false),
                IsZeroTestData(
                    "0d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(0.0))),
                    expected = true),
                IsZeroTestData(
                    "-1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(-1.0))),
                    expected = false),
                IsZeroTestData(
                    "1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(1.toBigDecimal()),
                                )),
                    expected = false),
                IsZeroTestData(
                    "0bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(0.toBigDecimal()),
                                )),
                    expected = true),
                IsZeroTestData(
                    "-1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal((-1).toBigDecimal()),
                                )),
                    expected = false),
                IsZeroTestData(
                    "1L",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(long(1))),
                    expected = false),
                IsZeroTestData(
                    "0L",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(long(0))),
                    expected = true),
                IsZeroTestData(
                    "-1L",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(long(-1))),
                    expected = false),
                IsZeroTestData(
                    "P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(period(Period.parse("P1D")))),
                    expected = false),
                IsZeroTestData(
                    "P0D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    period(Period.parse("P0D")),
                                )),
                    expected = true),
                IsZeroTestData(
                    "P-1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(period(Period.parse("P-1D")))),
                    expected = false),
                IsZeroTestData(
                    "PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(duration(Duration.ofHours(1)))),
                    expected = false),
                IsZeroTestData(
                    "PT0H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(duration(Duration.ofHours(0)))),
                    expected = true),
                IsZeroTestData(
                    "PT-1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(duration(Duration.ofHours(-1)))),
                    expected = false),
                IsZeroTestData(
                    name = "0, negate",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(0)), negateResult = true),
                    expected = false),
                IsZeroTestData(
                    "boolean",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(bool(true))),
                    expected = null),
                IsZeroTestData(
                    "negate null",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(bool(true)),
                            negateResult = true),
                    expected = null),
                IsZeroTestData(
                    "null",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = "a", type = VariableValueTypeEnum.INT))),
                    expected = null),
            )) {
              it.given.check(context, EmptyPolicyCatalog()) shouldBe it.expected
            }
      }
    })
