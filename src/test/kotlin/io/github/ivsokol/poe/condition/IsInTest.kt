package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueFormatEnum
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

private data class IsInTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class IsInTest :
    DescribeSpec({
      val context = Context(request = mapOf("str" to "a", "str2" to "b"))

      val operationEnum = OperationEnum.IS_IN

      describe("logic") {
        withData(
            nameFn = { "IsIn: ${it.name}" },
            listOf(
                // string
                IsInTestData(
                    name = "string",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("b"), string("abc"))),
                    expected = true),
                IsInTestData(
                    name = "string ignore case",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(string("B"), string("abc")),
                            stringIgnoreCase = true),
                    expected = true),
                IsInTestData(
                    name = "string false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("d"), string("abc"))),
                    expected = false),
                IsInTestData(
                    name = "int null",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(1), int(2))),
                    expected = null),
                IsInTestData(
                    name = "string cast",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    int(2),
                                    string("123"),
                                )),
                    expected = true),
                // array
                IsInTestData(
                    name = "array",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    int(2),
                                    PolicyVariableStatic(value = listOf(1, 2, 3)),
                                )),
                    expected = true),
                IsInTestData(
                    name = "array false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    int(4),
                                    PolicyVariableStatic(value = listOf(1, 2, 3)),
                                )),
                    expected = false),
                // array will throw if wrong type, so null will be returned
                IsInTestData(
                    name = "array wrong params",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    string("3"),
                                    PolicyVariableStatic(value = listOf(1, 2, 3)),
                                )),
                    expected = null),
                // array node
                IsInTestData(
                    name = "array node",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    int(2),
                                    PolicyVariableStatic(
                                        value = """[1,2,3]""",
                                        type = VariableValueTypeEnum.ARRAY,
                                        format = VariableValueFormatEnum.JSON),
                                )),
                    expected = true),
                IsInTestData(
                    name = "array node false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    int(4),
                                    PolicyVariableStatic(
                                        value = """[1,2,3]""",
                                        type = VariableValueTypeEnum.ARRAY,
                                        format = VariableValueFormatEnum.JSON),
                                )),
                    expected = false),
                // ArrayNode is resilient to wrong type
                IsInTestData(
                    name = "array node wrong type",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    string("3"),
                                    PolicyVariableStatic(
                                        value = """[1,2,3]""",
                                        type = VariableValueTypeEnum.ARRAY,
                                        format = VariableValueFormatEnum.JSON),
                                )),
                    expected = false),
            )) {
              it.given.check(context, EmptyPolicyCatalog()) shouldBe it.expected
            }
      }
    })
