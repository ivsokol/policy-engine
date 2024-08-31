package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.time.*

private data class LessThanEqualTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class LessThanEqualTest :
    DescribeSpec({
      val context = Context()
      val currentDate = LocalDate.now()
      val localTime = LocalTime.now()
      val currentDateTime = OffsetDateTime.now()

      val operationEnum = OperationEnum.LESS_THAN_EQUAL

      describe("logic") {
        withData(
            nameFn = { "LessThanEqual: ${it.name}" },
            listOf(
                LessThanEqualTestData(
                    name = "0 <= 1",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(0), int(1))),
                    expected = true),
                LessThanEqualTestData(
                    name = "1 <= 1",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(1), int(1))),
                    expected = true),
                LessThanEqualTestData(
                    name = "2 <= 1",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(2), int(1))),
                    expected = false),
                LessThanEqualTestData(
                    "a <= b",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("a"), string("b"))),
                    expected = true),
                LessThanEqualTestData(
                    "b <= b",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("b"), string("b"))),
                    expected = true),
                LessThanEqualTestData(
                    "c <= b",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("c"), string("b"))),
                    expected = false),
                LessThanEqualTestData(
                    "yesterday <= today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate.minusDays(1)), date(currentDate))),
                    expected = true),
                LessThanEqualTestData(
                    "today <= today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate), date(currentDate))),
                    expected = true),
                LessThanEqualTestData(
                    "tomorrow <= today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate.plusDays(1)), date(currentDate))),
                    expected = false),
                LessThanEqualTestData(
                    "past <= now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(currentDateTime.minusSeconds(1)),
                                    dateTime(currentDateTime))),
                    expected = true),
                LessThanEqualTestData(
                    "now <= now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(dateTime(currentDateTime), dateTime(currentDateTime))),
                    expected = true),
                LessThanEqualTestData(
                    "future <= now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(currentDateTime.plusSeconds(1)),
                                    dateTime(currentDateTime))),
                    expected = false),
                LessThanEqualTestData(
                    "past <= now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime.minusSeconds(1)), time(localTime))),
                    expected = true),
                LessThanEqualTestData(
                    "now <= now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime), time(localTime))),
                    expected = true),
                LessThanEqualTestData(
                    "future <= now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime.plusSeconds(1)), time(localTime))),
                    expected = false),
                LessThanEqualTestData(
                    "0.1f <= 1f",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(float(0.1f), float(1f))),
                    expected = true),
                LessThanEqualTestData(
                    "1.1f <= 1.1f",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(float(1.1f), float(1.1f))),
                    expected = true),
                LessThanEqualTestData(
                    "2.1f <= 1f",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(float(2.1f), float(1f))),
                    expected = false),
                LessThanEqualTestData(
                    "0.1d <= 1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(0.1), double(1.0))),
                    expected = true),
                LessThanEqualTestData(
                    "1.1d <= 1.1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(1.1), double(1.1))),
                    expected = true),
                LessThanEqualTestData(
                    "2.1d <= 1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(2.1), double(1.0))),
                    expected = false),
                LessThanEqualTestData(
                    "0.1bd <= 1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(0.1.toBigDecimal()),
                                    bigDecimal(1.0.toBigDecimal()))),
                    expected = true),
                LessThanEqualTestData(
                    "1.1bd <= 1.1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(1.1.toBigDecimal()),
                                    bigDecimal(1.1.toBigDecimal()))),
                    expected = true),
                LessThanEqualTestData(
                    "2.1bd <= 1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(2.1.toBigDecimal()),
                                    bigDecimal(1.0.toBigDecimal()))),
                    expected = false),
                LessThanEqualTestData(
                    "0L <= 1L",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(long(0), long(1))),
                    expected = true),
                LessThanEqualTestData(
                    "1L <= 1L",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(long(1), long(1))),
                    expected = true),
                LessThanEqualTestData(
                    "2L <= 1L",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(long(2), long(1))),
                    expected = false),
                LessThanEqualTestData(
                    "P0D <= P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(period(Period.parse("P0D")), period(Period.parse("P1D")))),
                    expected = true),
                LessThanEqualTestData(
                    "P1D <= P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(period(Period.parse("P1D")), period(Period.parse("P1D")))),
                    expected = true),
                LessThanEqualTestData(
                    "P2D <= P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(period(Period.parse("P2D")), period(Period.parse("P1D")))),
                    expected = false),
                LessThanEqualTestData(
                    "PT0H <= PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    duration(Duration.ofHours(0)), duration(Duration.ofHours(1)))),
                    expected = true),
                LessThanEqualTestData(
                    "PT1H <= PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    duration(Duration.ofHours(1)), duration(Duration.ofHours(1)))),
                    expected = true),
                LessThanEqualTestData(
                    "PT2H <= PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    duration(Duration.ofHours(2)), duration(Duration.ofHours(1)))),
                    expected = false),
                // edge case tests
                LessThanEqualTestData(
                    "b <= C, ignore case",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(string("b"), string("C")),
                            stringIgnoreCase = true),
                    expected = true),
                LessThanEqualTestData(
                    "b <= C",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("b"), string("C"))),
                    expected = false),
                LessThanEqualTestData(
                    name = "2 <= 1, negate",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(int(2), int(1)),
                            negateResult = true),
                    expected = true),
                LessThanEqualTestData(
                    "boolean",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(bool(true), bool(false))),
                    expected = null),
                LessThanEqualTestData(
                    "negate null",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(bool(true), bool(false)),
                            negateResult = true),
                    expected = null),
                LessThanEqualTestData(
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
                LessThanEqualTestData(
                    "cast",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(2.1), int(1))),
                    expected = false),
                LessThanEqualTestData(
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
