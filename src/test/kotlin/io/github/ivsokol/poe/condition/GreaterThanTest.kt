package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.time.*

private data class GreaterThanTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class GreaterThanTest :
    DescribeSpec({
      val context = Context()
      val currentDate = LocalDate.now()
      val localTime = LocalTime.now()
      val currentDateTime = OffsetDateTime.now()

      val operationEnum = OperationEnum.GREATER_THAN

      describe("logic") {
        withData(
            nameFn = { "GreaterThan: ${it.name}" },
            listOf(
                GreaterThanTestData(
                    name = "0 > 1",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(0), int(1))),
                    expected = false),
                GreaterThanTestData(
                    name = "1 > 1",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(1), int(1))),
                    expected = false),
                GreaterThanTestData(
                    name = "2 > 1",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(2), int(1))),
                    expected = true),
                GreaterThanTestData(
                    "a > b",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("a"), string("b"))),
                    expected = false),
                GreaterThanTestData(
                    "b > b",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("b"), string("b"))),
                    expected = false),
                GreaterThanTestData(
                    "c > b",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("c"), string("b"))),
                    expected = true),
                GreaterThanTestData(
                    "yesterday > today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate.minusDays(1)), date(currentDate))),
                    expected = false),
                GreaterThanTestData(
                    "today > today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate), date(currentDate))),
                    expected = false),
                GreaterThanTestData(
                    "tomorrow > today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate.plusDays(1)), date(currentDate))),
                    expected = true),
                GreaterThanTestData(
                    "past > now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(currentDateTime.minusSeconds(1)),
                                    dateTime(currentDateTime))),
                    expected = false),
                GreaterThanTestData(
                    "now > now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(dateTime(currentDateTime), dateTime(currentDateTime))),
                    expected = false),
                GreaterThanTestData(
                    "future > now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(currentDateTime.plusSeconds(1)),
                                    dateTime(currentDateTime))),
                    expected = true),
                GreaterThanTestData(
                    "past > now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime.minusSeconds(1)), time(localTime))),
                    expected = false),
                GreaterThanTestData(
                    "now > now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime), time(localTime))),
                    expected = false),
                GreaterThanTestData(
                    "future > now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime.plusSeconds(1)), time(localTime))),
                    expected = true),
                GreaterThanTestData(
                    "0.1f > 1f",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(float(0.1f), float(1f))),
                    expected = false),
                GreaterThanTestData(
                    "1.1f > 1.1f",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(float(1.1f), float(1.1f))),
                    expected = false),
                GreaterThanTestData(
                    "2.1f > 1f",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(float(2.1f), float(1f))),
                    expected = true),
                GreaterThanTestData(
                    "0.1d > 1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(0.1), double(1.0))),
                    expected = false),
                GreaterThanTestData(
                    "1.1d > 1.1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(1.1), double(1.1))),
                    expected = false),
                GreaterThanTestData(
                    "2.1d > 1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(2.1), double(1.0))),
                    expected = true),
                GreaterThanTestData(
                    "0.1bd > 1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(0.1.toBigDecimal()),
                                    bigDecimal(1.0.toBigDecimal()))),
                    expected = false),
                GreaterThanTestData(
                    "1.1bd > 1.1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(1.1.toBigDecimal()),
                                    bigDecimal(1.1.toBigDecimal()))),
                    expected = false),
                GreaterThanTestData(
                    "2.1bd > 1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(2.1.toBigDecimal()),
                                    bigDecimal(1.0.toBigDecimal()))),
                    expected = true),
                GreaterThanTestData(
                    "0L > 1L",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(long(0), long(1))),
                    expected = false),
                GreaterThanTestData(
                    "1L > 1L",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(long(1), long(1))),
                    expected = false),
                GreaterThanTestData(
                    "2L > 1L",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(long(2), long(1))),
                    expected = true),
                GreaterThanTestData(
                    "P0D > P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(period(Period.parse("P0D")), period(Period.parse("P1D")))),
                    expected = false),
                GreaterThanTestData(
                    "P1D > P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(period(Period.parse("P1D")), period(Period.parse("P1D")))),
                    expected = false),
                GreaterThanTestData(
                    "P2D > P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(period(Period.parse("P2D")), period(Period.parse("P1D")))),
                    expected = true),
                GreaterThanTestData(
                    "PT0H > PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    duration(Duration.ofHours(0)), duration(Duration.ofHours(1)))),
                    expected = false),
                GreaterThanTestData(
                    "PT1H > PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    duration(Duration.ofHours(1)), duration(Duration.ofHours(1)))),
                    expected = false),
                GreaterThanTestData(
                    "PT2H > PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    duration(Duration.ofHours(2)), duration(Duration.ofHours(1)))),
                    expected = true),
                // edge case tests
                GreaterThanTestData(
                    "b > C, ignore case",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(string("b"), string("C")),
                            stringIgnoreCase = true),
                    expected = false),
                GreaterThanTestData(
                    "b > C",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("b"), string("C"))),
                    expected = true),
                GreaterThanTestData(
                    name = "2 > 1, negate",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(int(2), int(1)),
                            negateResult = true),
                    expected = false),
                GreaterThanTestData(
                    "boolean",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(bool(true), bool(false))),
                    expected = null),
                GreaterThanTestData(
                    "negate null",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(bool(true), bool(false)),
                            negateResult = true),
                    expected = null),
                GreaterThanTestData(
                    "null",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = "a", type = VariableValueTypeEnum.INT),
                                    int(1))),
                    expected = null),
                GreaterThanTestData(
                    "cast",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(2.1), int(1))),
                    expected = true),
                GreaterThanTestData(
                    "cast fail",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(int(1), date(LocalDate.now()))),
                    expected = null),
            )) {
              it.given.check(context, EmptyPolicyCatalog()) shouldBe it.expected
            }
      }
    })
