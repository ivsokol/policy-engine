package com.github.ivsokol.poe.condition

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import com.github.ivsokol.poe.variable.PolicyVariableStatic
import com.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.time.Period

private data class IsPositiveTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class IsPositiveTest :
    DescribeSpec({
      val context = Context()

      val operationEnum = OperationEnum.IS_POSITIVE

      describe("logic") {
        withData(
            nameFn = { "IsPositive: ${it.name}" },
            listOf(
                IsPositiveTestData(
                    name = "0",
                    given = PolicyConditionAtomic(operation = operationEnum, args = listOf(int(0))),
                    expected = false),
                IsPositiveTestData(
                    name = "1",
                    given = PolicyConditionAtomic(operation = operationEnum, args = listOf(int(1))),
                    expected = true),
                IsPositiveTestData(
                    name = "-1",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(int(-1))),
                    expected = false),
                IsPositiveTestData(
                    "a",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("a"))),
                    expected = null),
                IsPositiveTestData(
                    "1f",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(float(1f))),
                    expected = true),
                IsPositiveTestData(
                    "0f",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(float(0f))),
                    expected = false),
                IsPositiveTestData(
                    "-1f",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(float(-1f))),
                    expected = false),
                IsPositiveTestData(
                    "1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(1.0))),
                    expected = true),
                IsPositiveTestData(
                    "0d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(0.0))),
                    expected = false),
                IsPositiveTestData(
                    "-1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(-1.0))),
                    expected = false),
                IsPositiveTestData(
                    "1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(1.toBigDecimal()),
                                )),
                    expected = true),
                IsPositiveTestData(
                    "0bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(0.toBigDecimal()),
                                )),
                    expected = false),
                IsPositiveTestData(
                    "-1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal((-1).toBigDecimal()),
                                )),
                    expected = false),
                IsPositiveTestData(
                    "1L",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(long(1))),
                    expected = true),
                IsPositiveTestData(
                    "0L",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(long(0))),
                    expected = false),
                IsPositiveTestData(
                    "-1L",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(long(-1))),
                    expected = false),
                IsPositiveTestData(
                    "P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(period(Period.parse("P1D")))),
                    expected = true),
                IsPositiveTestData(
                    "P0D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    period(Period.parse("P0D")),
                                )),
                    expected = false),
                IsPositiveTestData(
                    "P-1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(period(Period.parse("P-1D")))),
                    expected = false),
                IsPositiveTestData(
                    "PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(duration(Duration.ofHours(1)))),
                    expected = true),
                IsPositiveTestData(
                    "PT0H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(duration(Duration.ofHours(0)))),
                    expected = false),
                IsPositiveTestData(
                    "PT-1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(duration(Duration.ofHours(-1)))),
                    expected = false),
                IsPositiveTestData(
                    name = "1, negate",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(1)), negateResult = true),
                    expected = false),
                IsPositiveTestData(
                    "boolean",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(bool(true))),
                    expected = null),
                IsPositiveTestData(
                    "negate null",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(bool(true)),
                            negateResult = true),
                    expected = null),
                IsPositiveTestData(
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
