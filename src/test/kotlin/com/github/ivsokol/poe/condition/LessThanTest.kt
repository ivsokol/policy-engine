package com.github.ivsokol.poe.condition

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import com.github.ivsokol.poe.variable.PolicyVariableStatic
import com.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.time.*

private data class LessThanTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class LessThanTest :
    DescribeSpec({
      val context = Context()
      val currentDate = LocalDate.now()
      val localTime = LocalTime.now()
      val currentDateTime = OffsetDateTime.now()

      val operationEnum = OperationEnum.LESS_THAN

      describe("logic") {
        withData(
            nameFn = { "LessThan: ${it.name}" },
            listOf(
                LessThanTestData(
                    name = "0 < 1",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(0), int(1))),
                    expected = true),
                LessThanTestData(
                    name = "1 < 1",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(1), int(1))),
                    expected = false),
                LessThanTestData(
                    name = "2 < 1",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(2), int(1))),
                    expected = false),
                LessThanTestData(
                    "a < b",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("a"), string("b"))),
                    expected = true),
                LessThanTestData(
                    "b < b",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("b"), string("b"))),
                    expected = false),
                LessThanTestData(
                    "c < b",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("c"), string("b"))),
                    expected = false),
                LessThanTestData(
                    "yesterday < today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate.minusDays(1)), date(currentDate))),
                    expected = true),
                LessThanTestData(
                    "today < today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate), date(currentDate))),
                    expected = false),
                LessThanTestData(
                    "tomorrow < today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate.plusDays(1)), date(currentDate))),
                    expected = false),
                LessThanTestData(
                    "past < now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(currentDateTime.minusSeconds(1)),
                                    dateTime(currentDateTime))),
                    expected = true),
                LessThanTestData(
                    "now < now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(dateTime(currentDateTime), dateTime(currentDateTime))),
                    expected = false),
                LessThanTestData(
                    "future < now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(currentDateTime.plusSeconds(1)),
                                    dateTime(currentDateTime))),
                    expected = false),
                LessThanTestData(
                    "past < now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime.minusSeconds(1)), time(localTime))),
                    expected = true),
                LessThanTestData(
                    "now < now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime), time(localTime))),
                    expected = false),
                LessThanTestData(
                    "future < now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime.plusSeconds(1)), time(localTime))),
                    expected = false),
                LessThanTestData(
                    "0.1f < 1f",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(float(0.1f), float(1f))),
                    expected = true),
                LessThanTestData(
                    "1.1f < 1.1f",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(float(1.1f), float(1.1f))),
                    expected = false),
                LessThanTestData(
                    "2.1f < 1f",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(float(2.1f), float(1f))),
                    expected = false),
                LessThanTestData(
                    "0.1d < 1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(0.1), double(1.0))),
                    expected = true),
                LessThanTestData(
                    "1.1d < 1.1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(1.1), double(1.1))),
                    expected = false),
                LessThanTestData(
                    "2.1d < 1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(2.1), double(1.0))),
                    expected = false),
                LessThanTestData(
                    "0.1bd < 1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(0.1.toBigDecimal()),
                                    bigDecimal(1.0.toBigDecimal()))),
                    expected = true),
                LessThanTestData(
                    "1.1bd < 1.1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(1.1.toBigDecimal()),
                                    bigDecimal(1.1.toBigDecimal()))),
                    expected = false),
                LessThanTestData(
                    "2.1bd < 1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(2.1.toBigDecimal()),
                                    bigDecimal(1.0.toBigDecimal()))),
                    expected = false),
                LessThanTestData(
                    "0L < 1L",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(long(0), long(1))),
                    expected = true),
                LessThanTestData(
                    "1L < 1L",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(long(1), long(1))),
                    expected = false),
                LessThanTestData(
                    "2L < 1L",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(long(2), long(1))),
                    expected = false),
                LessThanTestData(
                    "P0D < P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(period(Period.parse("P0D")), period(Period.parse("P1D")))),
                    expected = true),
                LessThanTestData(
                    "P1D < P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(period(Period.parse("P1D")), period(Period.parse("P1D")))),
                    expected = false),
                LessThanTestData(
                    "P2D < P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(period(Period.parse("P2D")), period(Period.parse("P1D")))),
                    expected = false),
                LessThanTestData(
                    "PT0H < PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    duration(Duration.ofHours(0)), duration(Duration.ofHours(1)))),
                    expected = true),
                LessThanTestData(
                    "PT1H < PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    duration(Duration.ofHours(1)), duration(Duration.ofHours(1)))),
                    expected = false),
                LessThanTestData(
                    "PT2H < PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    duration(Duration.ofHours(2)), duration(Duration.ofHours(1)))),
                    expected = false),
                // edge case tests
                LessThanTestData(
                    "b < C, ignore case",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(string("b"), string("C")),
                            stringIgnoreCase = true),
                    expected = true),
                LessThanTestData(
                    "b < C",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("b"), string("C"))),
                    expected = false),
                LessThanTestData(
                    name = "2 < 1, negate",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(int(2), int(1)),
                            negateResult = true),
                    expected = true),
                LessThanTestData(
                    "boolean",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(bool(true), bool(false))),
                    expected = null),
                LessThanTestData(
                    "negate null",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(bool(true), bool(false)),
                            negateResult = true),
                    expected = null),
                LessThanTestData(
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
                LessThanTestData(
                    "cast",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(0.1), int(1))),
                    expected = true),
                LessThanTestData(
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
