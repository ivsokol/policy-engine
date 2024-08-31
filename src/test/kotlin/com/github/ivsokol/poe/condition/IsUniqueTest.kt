package com.github.ivsokol.poe.condition

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import com.github.ivsokol.poe.variable.PolicyVariableStatic
import com.github.ivsokol.poe.variable.VariableValueFormatEnum
import com.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

private data class IsUniqueTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class IsUniqueTest :
    DescribeSpec({
      val context = Context()

      val operationEnum = OperationEnum.IS_UNIQUE

      describe("logic") {
        withData(
            nameFn = { "IsUnique: ${it.name}" },
            listOf(
                // array
                IsUniqueTestData(
                    name = "array",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(PolicyVariableStatic(value = listOf(1, 2, 3)))),
                    expected = true),
                IsUniqueTestData(
                    name = "array false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(PolicyVariableStatic(value = listOf(1, 2, 2)))),
                    expected = false),
                // array node
                IsUniqueTestData(
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
                                )),
                    expected = true),
                IsUniqueTestData(
                    name = "array node false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args =
                                listOf(
                                    PolicyVariableStatic(
                                        value = """[1,2,2]""",
                                        type = VariableValueTypeEnum.ARRAY,
                                        format = VariableValueFormatEnum.JSON),
                                )),
                    expected = false),
                // wrong type
                IsUniqueTestData(
                    name = "array node wrong type",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("3"))),
                    expected = null),
            )) {
              it.given.check(context, EmptyPolicyCatalog()) shouldBe it.expected
            }
      }
    })
