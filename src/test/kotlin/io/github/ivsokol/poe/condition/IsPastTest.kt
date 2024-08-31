package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.DefaultEnvironmentKey
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueFormatEnum
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.time.*

private data class IsPastTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class IsPastTest :
    DescribeSpec({
      val context = Context()
      val instant = Instant.parse("2024-02-28T03:42:56+11:00")
      val clock = Clock.fixed(instant, ZoneOffset.ofHours(11))
      val emptyContext =
          Context()
              .copy(
                  environment =
                      mapOf(
                          DefaultEnvironmentKey.CURRENT_DATE to null,
                          DefaultEnvironmentKey.CURRENT_TIME to null,
                          DefaultEnvironmentKey.CURRENT_DATE_TIME to null),
                  options = context.options.copy(clock = clock))
      val currentDate = LocalDate.now()
      val localTime = LocalTime.now()
      val currentDateTime = OffsetDateTime.now()
      val currentDateFake = LocalDate.now(clock)
      val localTimeFake = LocalTime.now(clock)
      val currentDateTimeFake = OffsetDateTime.now(clock)

      val operationEnum = OperationEnum.IS_PAST

      describe("logic") {
        withData(
            nameFn = { "IsPast: ${it.name}" },
            listOf(
                IsPastTestData(
                    "yesterday",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate.minusDays(1)))),
                    expected = true),
                IsPastTestData(
                    "today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(date(currentDate))),
                    expected = false),
                IsPastTestData(
                    "tomorrow",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate.plusDays(1)))),
                    expected = false),
                IsPastTestData(
                    "past, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(currentDateTime.minusSeconds(1)),
                                )),
                    expected = true),
                IsPastTestData(
                    "now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(dateTime(currentDateTime))),
                    expected = false),
                IsPastTestData(
                    "future, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(currentDateTime.plusSeconds(1)),
                                )),
                    expected = false),
                IsPastTestData(
                    "past, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime.minusSeconds(1)))),
                    expected = true),
                IsPastTestData(
                    "now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(time(localTime))),
                    expected = false),
                IsPastTestData(
                    "future, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime.plusSeconds(1)))),
                    expected = false),
                // egde cases
                IsPastTestData(
                    "future, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime.plusSeconds(1))),
                            negateResult = true),
                    expected = true),
                IsPastTestData(
                    "boolean",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(bool(true))),
                    expected = null),
                IsPastTestData(
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

      describe("null environment") {
        withData(
            nameFn = { "IsPastNullEnv: ${it.name}" },
            listOf(
                IsPastTestData(
                    "yesterday",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDateFake.minusDays(1)))),
                    expected = true),
                IsPastTestData(
                    "today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(date(currentDateFake))),
                    expected = false),
                IsPastTestData(
                    "tomorrow",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDateFake.plusDays(1)))),
                    expected = false),
                IsPastTestData(
                    "past, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(currentDateTimeFake.minusSeconds(1)),
                                )),
                    expected = true),
                IsPastTestData(
                    "now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(dateTime(currentDateTimeFake))),
                    expected = false),
                IsPastTestData(
                    "future, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(currentDateTimeFake.plusSeconds(1)),
                                )),
                    expected = false),
                IsPastTestData(
                    "past, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTimeFake.minusSeconds(1)))),
                    expected = true),
                IsPastTestData(
                    "now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(time(localTimeFake))),
                    expected = false),
                IsPastTestData(
                    "future, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTimeFake.plusSeconds(1)))),
                    expected = false),
                // egde cases
                IsPastTestData(
                    "future, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTimeFake.plusSeconds(1))),
                            negateResult = true),
                    expected = true),
                IsPastTestData(
                    "boolean",
                    given =
                        PolicyConditionAtomic(operation = operationEnum, args = listOf(bool(true))),
                    expected = null),
                IsPastTestData(
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
              it.given.check(emptyContext, EmptyPolicyCatalog()) shouldBe it.expected
            }
      }
    })
