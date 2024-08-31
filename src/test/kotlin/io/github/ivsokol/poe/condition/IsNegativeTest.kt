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

private data class IsNegativeTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class IsNegativeTest :
    DescribeSpec({
      val context = Context()

      val operationEnum = OperationEnum.IS_NEGATIVE

      describe("logic") {
        withData(
            nameFn = { "IsNegative: ${it.name}" },
            listOf(
                IsNegativeTestData(
                    name = "0",
                    given = PolicyConditionAtomic(operation = operationEnum, args = listOf(int(0))),
                    expected = false),
                IsNegativeTestData(
                    name = "1",
                    given = PolicyConditionAtomic(operation = operationEnum, args = listOf(int(1))),
                    expected = false),
                IsNegativeTestData(
                    name = "-1",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(int(-1))),
                    expected = true),
                IsNegativeTestData(
                    "a",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("a"))),
                    expected = null),
                IsNegativeTestData(
                    "1f",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(float(1f))),
                    expected = false),
                IsNegativeTestData(
                    "0f",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(float(0f))),
                    expected = false),
                IsNegativeTestData(
                    "-1f",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(float(-1f))),
                    expected = true),
                IsNegativeTestData(
                    "1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(1.0))),
                    expected = false),
                IsNegativeTestData(
                    "0d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(0.0))),
                    expected = false),
                IsNegativeTestData(
                    "-1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(-1.0))),
                    expected = true),
                IsNegativeTestData(
                    "1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(1.toBigDecimal()),
                                )),
                    expected = false),
                IsNegativeTestData(
                    "0bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(0.toBigDecimal()),
                                )),
                    expected = false),
                IsNegativeTestData(
                    "-1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal((-1).toBigDecimal()),
                                )),
                    expected = true),
                IsNegativeTestData(
                    "1L",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(long(1))),
                    expected = false),
                IsNegativeTestData(
                    "0L",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(long(0))),
                    expected = false),
                IsNegativeTestData(
                    "-1L",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(long(-1))),
                    expected = true),
                IsNegativeTestData(
                    "P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(period(Period.parse("P1D")))),
                    expected = false),
                IsNegativeTestData(
                    "P0D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    period(Period.parse("P0D")),
                                )),
                    expected = false),
                IsNegativeTestData(
                    "P-1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(period(Period.parse("P-1D")))),
                    expected = true),
                IsNegativeTestData(
                    "PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(duration(Duration.ofHours(1)))),
                    expected = false),
                IsNegativeTestData(
                    "PT0H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(duration(Duration.ofHours(0)))),
                    expected = false),
                IsNegativeTestData(
                    "PT-1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(duration(Duration.ofHours(-1)))),
                    expected = true),
                IsNegativeTestData(
                    name = "-1, negate",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(-1)), negateResult = true),
                    expected = false),
                IsNegativeTestData(
                    "boolean",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(bool(true))),
                    expected = null),
                IsNegativeTestData(
                    "negate null",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(bool(true)),
                            negateResult = true),
                    expected = null),
                IsNegativeTestData(
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
