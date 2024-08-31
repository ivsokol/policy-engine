package com.github.ivsokol.poe.condition

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import com.github.ivsokol.poe.variable.PolicyVariableStatic
import com.github.ivsokol.poe.variable.VariableValueFormatEnum
import com.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

private data class ContainsTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class ContainsTest :
    DescribeSpec({
      val context = Context(request = mapOf("str" to "a", "str2" to "b"))

      val operationEnum = OperationEnum.CONTAINS

      describe("logic") {
        withData(
            nameFn = { "Contains: ${it.name}" },
            listOf(
                // string
                ContainsTestData(
                    name = "string",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("abc"), string("b"))),
                    expected = true),
                ContainsTestData(
                    name = "string ignore case",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(string("abc"), string("B")),
                            stringIgnoreCase = true),
                    expected = true),
                ContainsTestData(
                    name = "string false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("abc"), string("d"))),
                    expected = false),
                ContainsTestData(
                    name = "int null",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(1), int(2))),
                    expected = null),
                ContainsTestData(
                    name = "string cast",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("123"), int(2))),
                    expected = true),
                // array
                ContainsTestData(
                    name = "array",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(PolicyVariableStatic(value = listOf(1, 2, 3)), int(2))),
                    expected = true),
                ContainsTestData(
                    name = "array false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(PolicyVariableStatic(value = listOf(1, 2, 3)), int(4))),
                    expected = false),
                // array will throw if wrong type, so null will be returned
                ContainsTestData(
                    name = "array wrong params",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(PolicyVariableStatic(value = listOf(1, 2, 3)), string("3"))),
                    expected = null),
                // array node
                ContainsTestData(
                    name = "array node",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = """[1,2,3]""",
                                        type = VariableValueTypeEnum.ARRAY,
                                        format = VariableValueFormatEnum.JSON),
                                    int(2))),
                    expected = true),
                ContainsTestData(
                    name = "array node false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = """[1,2,3]""",
                                        type = VariableValueTypeEnum.ARRAY,
                                        format = VariableValueFormatEnum.JSON),
                                    int(4))),
                    expected = false),
                // ArrayNode is resilient to wrong type
                ContainsTestData(
                    name = "array node wrong type",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = """[1,2,3]""",
                                        type = VariableValueTypeEnum.ARRAY,
                                        format = VariableValueFormatEnum.JSON),
                                    string("3"))),
                    expected = false),
                // json node
                ContainsTestData(
                    name = "json node",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = """[1,2,3]""",
                                        type = VariableValueTypeEnum.STRING,
                                        format = VariableValueFormatEnum.JSON),
                                    int(2))),
                    expected = true),
                ContainsTestData(
                    name = "json node false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = """[1,2,3]""",
                                        type = VariableValueTypeEnum.STRING,
                                        format = VariableValueFormatEnum.JSON),
                                    int(4))),
                    expected = false),
                ContainsTestData(
                    name = "json node null",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = """1""",
                                        type = VariableValueTypeEnum.STRING,
                                        format = VariableValueFormatEnum.JSON),
                                    int(4))),
                    expected = null),
            )) {
              it.given.check(context, EmptyPolicyCatalog()) shouldBe it.expected
            }
      }
    })
