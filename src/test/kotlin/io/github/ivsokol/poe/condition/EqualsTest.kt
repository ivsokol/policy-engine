package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.variable.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.time.*

private data class EqualsTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class EqualsTest :
    DescribeSpec({
      val context = Context(request = mapOf("str" to "a", "str2" to "b"))
      val currentDate = LocalDate.now()
      val localTime = LocalTime.now()
      val currentDateTime = OffsetDateTime.now()

      val operationEnum = OperationEnum.EQUALS

      describe("logic") {
        withData(
            nameFn = { "Equals: ${it.name}" },
            listOf(
                EqualsTestData(
                    name = "0 == 1",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(0), int(1))),
                    expected = false),
                EqualsTestData(
                    name = "1 == 1",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(1), int(1))),
                    expected = true),
                EqualsTestData(
                    name = "2 == 1",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(2), int(1))),
                    expected = false),
                EqualsTestData(
                    "a == b",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("a"), string("b"))),
                    expected = false),
                EqualsTestData(
                    "b == b",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("b"), string("b"))),
                    expected = true),
                EqualsTestData(
                    "c == b",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("c"), string("b"))),
                    expected = false),
                EqualsTestData(
                    "yesterday == today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate.minusDays(1)), date(currentDate))),
                    expected = false),
                EqualsTestData(
                    "today == today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate), date(currentDate))),
                    expected = true),
                EqualsTestData(
                    "tomorrow == today",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(date(currentDate.plusDays(1)), date(currentDate))),
                    expected = false),
                EqualsTestData(
                    "past == now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(currentDateTime.minusSeconds(1)),
                                    dateTime(currentDateTime))),
                    expected = false),
                EqualsTestData(
                    "now == now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(dateTime(currentDateTime), dateTime(currentDateTime))),
                    expected = true),
                EqualsTestData(
                    "future == now, dateTime",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    dateTime(currentDateTime.plusSeconds(1)),
                                    dateTime(currentDateTime))),
                    expected = false),
                EqualsTestData(
                    "past == now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime.minusSeconds(1)), time(localTime))),
                    expected = false),
                EqualsTestData(
                    "now == now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime), time(localTime))),
                    expected = true),
                EqualsTestData(
                    "future == now, time",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(time(localTime.plusSeconds(1)), time(localTime))),
                    expected = false),
                EqualsTestData(
                    "0.1f == 1f",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(float(0.1f), float(1f))),
                    expected = false),
                EqualsTestData(
                    "1.1f == 1.1f",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(float(1.1f), float(1.1f))),
                    expected = true),
                EqualsTestData(
                    "2.1f == 1f",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(float(2.1f), float(1f))),
                    expected = false),
                EqualsTestData(
                    "0.1d == 1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(0.1), double(1.0))),
                    expected = false),
                EqualsTestData(
                    "1.1d == 1.1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(1.1), double(1.1))),
                    expected = true),
                EqualsTestData(
                    "2.1d == 1d",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(2.1), double(1.0))),
                    expected = false),
                EqualsTestData(
                    "0.1bd == 1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(0.1.toBigDecimal()),
                                    bigDecimal(1.0.toBigDecimal()))),
                    expected = false),
                EqualsTestData(
                    "1.1bd == 1.1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(1.1.toBigDecimal()),
                                    bigDecimal(1.1.toBigDecimal()))),
                    expected = true),
                EqualsTestData(
                    "2.1bd == 1bd",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    bigDecimal(2.1.toBigDecimal()),
                                    bigDecimal(1.0.toBigDecimal()))),
                    expected = false),
                EqualsTestData(
                    "0L == 1L",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(long(0), long(1))),
                    expected = false),
                EqualsTestData(
                    "1L == 1L",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(long(1), long(1))),
                    expected = true),
                EqualsTestData(
                    "2L == 1L",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(long(2), long(1))),
                    expected = false),
                EqualsTestData(
                    "P0D == P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(period(Period.parse("P0D")), period(Period.parse("P1D")))),
                    expected = false),
                EqualsTestData(
                    "P1D == P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(period(Period.parse("P1D")), period(Period.parse("P1D")))),
                    expected = true),
                EqualsTestData(
                    "P2D == P1D",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(period(Period.parse("P2D")), period(Period.parse("P1D")))),
                    expected = false),
                EqualsTestData(
                    "PT0H == PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    duration(Duration.ofHours(0)), duration(Duration.ofHours(1)))),
                    expected = false),
                EqualsTestData(
                    "PT1H == PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    duration(Duration.ofHours(1)), duration(Duration.ofHours(1)))),
                    expected = true),
                EqualsTestData(
                    "PT2H == PT1H",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    duration(Duration.ofHours(2)), duration(Duration.ofHours(1)))),
                    expected = false),
                EqualsTestData(
                    "true == false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(bool(true), bool(false))),
                    expected = false),
                EqualsTestData(
                    "true == true",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(bool(true), bool(true))),
                    expected = true),
                //  array
                EqualsTestData(
                    "array equals strict",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(value = listOf(1, 2, 3)),
                                    PolicyVariableStatic(value = listOf(1, 2, 3))),
                            arrayOrderStrictCheck = true,
                            fieldsStrictCheck = true),
                    expected = true),
                EqualsTestData(
                    "array equals lean order",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(value = listOf(1, 2, 3)),
                                    PolicyVariableStatic(value = listOf(1, 3, 2))),
                        ),
                    expected = true),
                EqualsTestData(
                    "array equals strict order,false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(value = listOf(1, 2, 3)),
                                    PolicyVariableStatic(value = listOf(1, 3, 2))),
                            arrayOrderStrictCheck = true,
                            fieldsStrictCheck = true),
                    expected = false),
                EqualsTestData(
                    "array equals strict elements,false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(value = listOf(1, 2, 3, 4)),
                                    PolicyVariableStatic(value = listOf(1, 2, 3))),
                            fieldsStrictCheck = true),
                    expected = false),
                // array node
                EqualsTestData(
                    "array equals strict",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = """[1,2,3]""",
                                        type = VariableValueTypeEnum.ARRAY,
                                        format = VariableValueFormatEnum.JSON),
                                    PolicyVariableStatic(
                                        value = """[1,2,3]""",
                                        type = VariableValueTypeEnum.ARRAY,
                                        format = VariableValueFormatEnum.JSON),
                                ),
                            arrayOrderStrictCheck = true,
                            fieldsStrictCheck = true),
                    expected = true),
                EqualsTestData(
                    "array equals lean order",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = """[1,2,3]""",
                                        type = VariableValueTypeEnum.ARRAY,
                                        format = VariableValueFormatEnum.JSON),
                                    PolicyVariableStatic(
                                        value = """[1,3,2]""",
                                        type = VariableValueTypeEnum.ARRAY,
                                        format = VariableValueFormatEnum.JSON),
                                ),
                        ),
                    expected = true),
                EqualsTestData(
                    "array equals strict order,false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = """[1,2,3]""",
                                        type = VariableValueTypeEnum.ARRAY,
                                        format = VariableValueFormatEnum.JSON),
                                    PolicyVariableStatic(
                                        value = """[1,3,2]""",
                                        type = VariableValueTypeEnum.ARRAY,
                                        format = VariableValueFormatEnum.JSON),
                                ),
                            arrayOrderStrictCheck = true,
                            fieldsStrictCheck = true),
                    expected = false),
                // object node
                EqualsTestData(
                    "object equals strict",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = """{"foo":"bar"}""",
                                        type = VariableValueTypeEnum.OBJECT,
                                        format = VariableValueFormatEnum.JSON),
                                    PolicyVariableStatic(
                                        value = """{"foo":"bar"}""",
                                        type = VariableValueTypeEnum.OBJECT,
                                        format = VariableValueFormatEnum.JSON),
                                ),
                            fieldsStrictCheck = true),
                    expected = true),
                EqualsTestData(
                    "object equals lean",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = """{"foo":"bar"}""",
                                        type = VariableValueTypeEnum.OBJECT,
                                        format = VariableValueFormatEnum.JSON),
                                    PolicyVariableStatic(
                                        value = """{"foo":"bar","ex":1}""",
                                        type = VariableValueTypeEnum.OBJECT,
                                        format = VariableValueFormatEnum.JSON),
                                ),
                        ),
                    expected = true),
                EqualsTestData(
                    "object equals strict,false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = """{"foo":"bar"}""",
                                        type = VariableValueTypeEnum.OBJECT,
                                        format = VariableValueFormatEnum.JSON),
                                    PolicyVariableStatic(
                                        value = """{"foo":"bar","ex":1}""",
                                        type = VariableValueTypeEnum.OBJECT,
                                        format = VariableValueFormatEnum.JSON),
                                ),
                            fieldsStrictCheck = true),
                    expected = false),
                EqualsTestData(
                    "object equals false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = """{"foo":"bar2"}""",
                                        type = VariableValueTypeEnum.OBJECT,
                                        format = VariableValueFormatEnum.JSON),
                                    PolicyVariableStatic(
                                        value = """{"foo":"bar"}""",
                                        type = VariableValueTypeEnum.OBJECT,
                                        format = VariableValueFormatEnum.JSON),
                                ),
                        ),
                    expected = false),
                // JSON Node
                EqualsTestData(
                    "JsonNode equals",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableDynamic(
                                        resolvers =
                                            listOf(
                                                PolicyVariableResolver(
                                                    path = "str",
                                                    engine =
                                                        PolicyVariableResolverEngineEnum
                                                            .JMES_PATH))),
                                    PolicyVariableDynamic(
                                        resolvers =
                                            listOf(
                                                PolicyVariableResolver(
                                                    path = "str",
                                                    engine =
                                                        PolicyVariableResolverEngineEnum
                                                            .JMES_PATH))),
                                ),
                        ),
                    expected = true),
                EqualsTestData(
                    "JsonNode equals false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableDynamic(
                                        resolvers =
                                            listOf(
                                                PolicyVariableResolver(
                                                    path = "str",
                                                    engine =
                                                        PolicyVariableResolverEngineEnum
                                                            .JMES_PATH))),
                                    PolicyVariableDynamic(
                                        resolvers =
                                            listOf(
                                                PolicyVariableResolver(
                                                    path = "str2",
                                                    engine =
                                                        PolicyVariableResolverEngineEnum
                                                            .JMES_PATH))),
                                ),
                        ),
                    expected = false),
                // edge case tests
                EqualsTestData(
                    "b == B, ignore case",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(string("b"), string("B")),
                            stringIgnoreCase = true),
                    expected = true),
                EqualsTestData(
                    "b == B",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("b"), string("B"))),
                    expected = false),
                EqualsTestData(
                    name = "2 == 1, negate",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(int(2), int(1)),
                            negateResult = true),
                    expected = true),
                EqualsTestData(
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
                EqualsTestData(
                    "negate null",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = "a", type = VariableValueTypeEnum.INT),
                                    int(1)),
                            negateResult = true),
                    expected = null),
                EqualsTestData(
                    "cast",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(double(1.0), int(1))),
                    expected = true),
                EqualsTestData(
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
