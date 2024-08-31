package com.github.ivsokol.poe.condition

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.DefaultEnvironmentKey
import com.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import com.github.ivsokol.poe.variable.ContextStoreEnum
import com.github.ivsokol.poe.variable.PolicyVariableStatic
import com.github.ivsokol.poe.variable.VariableValueFormatEnum
import com.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

private data class IsFutureTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class IsFutureTest :
    DescribeSpec({
      val context = Context()
      val currentDate = LocalDate.now()
      val localTime = LocalTime.now()
      val currentDateTime = OffsetDateTime.now()

      val operationEnum = OperationEnum.IS_FUTURE

      describe("logic") {
        withData(
            nameFn = { "IsFuture: ${it.name}" },
            listOf(
                IsFutureTestData(
                    "yesterday",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate.minusDays(1)))),
                    expected = false),
                IsFutureTestData(
                    "today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(date(currentDate))),
                    expected = false),
                IsFutureTestData(
                    "tomorrow",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate.plusDays(1)))),
                    expected = true),
                IsFutureTestData(
                    "past, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(currentDateTime.minusSeconds(1)),
                                )),
                    expected = false),
                IsFutureTestData(
                    "now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(
                                        context
                                            .store(ContextStoreEnum.ENVIRONMENT)
                                            ?.get(DefaultEnvironmentKey.CURRENT_DATE_TIME)
                                            as OffsetDateTime))),
                    expected = false),
                IsFutureTestData(
                    "future, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(currentDateTime.plusSeconds(1)),
                                )),
                    expected = true),
                IsFutureTestData(
                    "past, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime.minusSeconds(1)))),
                    expected = false),
                IsFutureTestData(
                    "now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    time(
                                        context
                                            .store(ContextStoreEnum.ENVIRONMENT)
                                            ?.get(DefaultEnvironmentKey.LOCAL_TIME) as LocalTime))),
                    expected = false),
                IsFutureTestData(
                    "future, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime.plusSeconds(1)))),
                    expected = true),
                // egde cases
                IsFutureTestData(
                    "future, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime.plusSeconds(1))),
                            negateResult = true),
                    expected = false),
                IsFutureTestData(
                    "boolean",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(bool(true))),
                    expected = null),
                IsFutureTestData(
                    "null",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = 1,
                                        type = VariableValueTypeEnum.STRING,
                                        format = VariableValueFormatEnum.DATE),
                                )),
                    expected = null),
            )) {
              it.given.check(context, EmptyPolicyCatalog()) shouldBe it.expected
            }
      }
    })
