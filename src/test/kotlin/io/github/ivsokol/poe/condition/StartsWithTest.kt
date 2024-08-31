package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueFormatEnum
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

private data class StartsWithTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class StartsWithTest :
    DescribeSpec({
      val context = Context(request = mapOf("str" to "a", "str2" to "b"))

      val operationEnum = OperationEnum.STARTS_WITH

      describe("logic") {
        withData(
            nameFn = { "StartsWith: ${it.name}" },
            listOf(
                // string
                StartsWithTestData(
                    name = "string",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("abc"), string("a"))),
                    expected = true),
                StartsWithTestData(
                    name = "string ignore case",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(string("abc"), string("A")),
                            stringIgnoreCase = true),
                    expected = true),
                StartsWithTestData(
                    name = "string false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("abc"), string("b"))),
                    expected = false),
                StartsWithTestData(
                    name = "int null",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(1), int(2))),
                    expected = null),
                StartsWithTestData(
                    name = "string cast",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("123"), int(1))),
                    expected = true),
                // array
                StartsWithTestData(
                    name = "array",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(PolicyVariableStatic(value = listOf(1, 2, 3)), int(1))),
                    expected = true),
                StartsWithTestData(
                    name = "array false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(PolicyVariableStatic(value = listOf(1, 2, 3)), int(2))),
                    expected = false),
                // array will throw if wrong type, so null will be returned
                StartsWithTestData(
                    name = "array wrong params",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(PolicyVariableStatic(value = listOf(1, 2, 3)), string("1"))),
                    expected = null),
                // array node
                StartsWithTestData(
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
                                    int(1))),
                    expected = true),
                StartsWithTestData(
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
                StartsWithTestData(
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
                                    string("1"))),
                    expected = false),
            )) {
              it.given.check(context, EmptyPolicyCatalog()) shouldBe it.expected
            }
      }
    })
