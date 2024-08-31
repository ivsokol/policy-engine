package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.time.*

private data class GreaterThanEqualTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class GreaterThanEqualTest :
    DescribeSpec({
      val context = Context()
      val currentDate = LocalDate.now()
      val localTime = LocalTime.now()
      val currentDateTime = OffsetDateTime.now()

      val operationEnum = OperationEnum.GREATER_THAN_EQUAL

      describe("logic") {
        withData(
            nameFn = { "GreaterThanEqual: ${it.name}" },
            listOf(
                GreaterThanEqualTestData(
                    name = "0 >= 1",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(0), int(1))),
                    expected = false),
                GreaterThanEqualTestData(
                    name = "1 >= 1",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(1), int(1))),
                    expected = true),
                GreaterThanEqualTestData(
                    name = "2 >= 1",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(2), int(1))),
                    expected = true),
                GreaterThanEqualTestData(
                    "a >= b",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("a"), string("b"))),
                    expected = false),
                GreaterThanEqualTestData(
                    "b >= b",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("b"), string("b"))),
                    expected = true),
                GreaterThanEqualTestData(
                    "c >= b",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("c"), string("b"))),
                    expected = true),
                GreaterThanEqualTestData(
                    "yesterday >= today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate.minusDays(1)), date(currentDate))),
                    expected = false),
                GreaterThanEqualTestData(
                    "today >= today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate), date(currentDate))),
                    expected = true),
                GreaterThanEqualTestData(
                    "tomorrow >= today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate.plusDays(1)), date(currentDate))),
                    expected = true),
                GreaterThanEqualTestData(
                    "past >= now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(currentDateTime.minusSeconds(1)),
                                    dateTime(currentDateTime))),
                    expected = false),
                GreaterThanEqualTestData(
                    "now >= now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(dateTime(currentDateTime), dateTime(currentDateTime))),
                    expected = true),
                GreaterThanEqualTestData(
                    "future >= now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(currentDateTime.plusSeconds(1)),
                                    dateTime(currentDateTime))),
                    expected = true),
                GreaterThanEqualTestData(
                    "past >= now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime.minusSeconds(1)), time(localTime))),
                    expected = false),
                GreaterThanEqualTestData(
                    "now >= now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime), time(localTime))),
                    expected = true),
                GreaterThanEqualTestData(
                    "future >= now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime.plusSeconds(1)), time(localTime))),
                    expected = true),
                GreaterThanEqualTestData(
                    "0.1f >= 1f",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(float(0.1f), float(1f))),
                    expected = false),
                GreaterThanEqualTestData(
                    "1.1f >= 1.1f",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(float(1.1f), float(1.1f))),
                    expected = true),
                GreaterThanEqualTestData(
                    "2.1f >= 1f",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(float(2.1f), float(1f))),
                    expected = true),
                GreaterThanEqualTestData(
                    "0.1d >= 1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(0.1), double(1.0))),
                    expected = false),
                GreaterThanEqualTestData(
                    "1.1d >= 1.1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(1.1), double(1.1))),
                    expected = true),
                GreaterThanEqualTestData(
                    "2.1d >= 1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(2.1), double(1.0))),
                    expected = true),
                GreaterThanEqualTestData(
                    "0.1bd >= 1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(0.1.toBigDecimal()),
                                    bigDecimal(1.0.toBigDecimal()))),
                    expected = false),
                GreaterThanEqualTestData(
                    "1.1bd >= 1.1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(1.1.toBigDecimal()),
                                    bigDecimal(1.1.toBigDecimal()))),
                    expected = true),
                GreaterThanEqualTestData(
                    "2.1bd >= 1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(2.1.toBigDecimal()),
                                    bigDecimal(1.0.toBigDecimal()))),
                    expected = true),
                GreaterThanEqualTestData(
                    "0L >= 1L",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(long(0), long(1))),
                    expected = false),
                GreaterThanEqualTestData(
                    "1L >= 1L",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(long(1), long(1))),
                    expected = true),
                GreaterThanEqualTestData(
                    "2L >= 1L",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(long(2), long(1))),
                    expected = true),
                GreaterThanEqualTestData(
                    "P0D >= P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(period(Period.parse("P0D")), period(Period.parse("P1D")))),
                    expected = false),
                GreaterThanEqualTestData(
                    "P1D >= P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(period(Period.parse("P1D")), period(Period.parse("P1D")))),
                    expected = true),
                GreaterThanEqualTestData(
                    "P2D >= P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(period(Period.parse("P2D")), period(Period.parse("P1D")))),
                    expected = true),
                GreaterThanEqualTestData(
                    "PT0H >= PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    duration(Duration.ofHours(0)), duration(Duration.ofHours(1)))),
                    expected = false),
                GreaterThanEqualTestData(
                    "PT1H >= PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    duration(Duration.ofHours(1)), duration(Duration.ofHours(1)))),
                    expected = true),
                GreaterThanEqualTestData(
                    "PT2H >= PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    duration(Duration.ofHours(2)), duration(Duration.ofHours(1)))),
                    expected = true),
                // edge case tests
                GreaterThanEqualTestData(
                    "b >= C, ignore case",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(string("b"), string("C")),
                            stringIgnoreCase = true),
                    expected = false),
                GreaterThanEqualTestData(
                    "b >= C",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("b"), string("C"))),
                    expected = true),
                GreaterThanEqualTestData(
                    name = "2 >= 1, negate",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(int(2), int(1)),
                            negateResult = true),
                    expected = false),
                GreaterThanEqualTestData(
                    "boolean",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(bool(true), bool(false))),
                    expected = null),
                GreaterThanEqualTestData(
                    "negate null",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(bool(true), bool(false)),
                            negateResult = true),
                    expected = null),
                GreaterThanEqualTestData(
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
                GreaterThanEqualTestData(
                    "cast",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(2.1), int(1))),
                    expected = true),
                GreaterThanEqualTestData(
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
