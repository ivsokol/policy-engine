package com.github.ivsokol.poe.condition

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import com.github.ivsokol.poe.variable.PolicyVariableStatic
import com.github.ivsokol.poe.variable.VariableValueFormatEnum
import com.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

private data class EndsWithTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class EndsWithTest :
    DescribeSpec({
      val context = Context(request = mapOf("str" to "a", "str2" to "b"))

      val operationEnum = OperationEnum.ENDS_WITH

      describe("logic") {
        withData(
            nameFn = { "EndsWith: ${it.name}" },
            listOf(
                // string
                EndsWithTestData(
                    name = "string",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("abc"), string("c"))),
                    expected = true),
                EndsWithTestData(
                    name = "string ignore case",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(string("abc"), string("C")),
                            stringIgnoreCase = true),
                    expected = true),
                EndsWithTestData(
                    name = "string false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("abc"), string("b"))),
                    expected = false),
                EndsWithTestData(
                    name = "int null",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(1), int(2))),
                    expected = null),
                EndsWithTestData(
                    name = "string cast",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("123"), int(3))),
                    expected = true),
                // array
                EndsWithTestData(
                    name = "array",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(PolicyVariableStatic(value = listOf(1, 2, 3)), int(3))),
                    expected = true),
                EndsWithTestData(
                    name = "array false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(PolicyVariableStatic(value = listOf(1, 2, 3)), int(2))),
                    expected = false),
                // array will throw if wrong type, so null will be returned
                EndsWithTestData(
                    name = "array wrong params",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(PolicyVariableStatic(value = listOf(1, 2, 3)), string("3"))),
                    expected = null),
                // array node
                EndsWithTestData(
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
                                    int(3))),
                    expected = true),
                EndsWithTestData(
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
                                    int(2))),
                    expected = false),
                // ArrayNode is resilient to wrong type
                EndsWithTestData(
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
            )) {
              it.given.check(context, EmptyPolicyCatalog()) shouldBe it.expected
            }
      }
    })
