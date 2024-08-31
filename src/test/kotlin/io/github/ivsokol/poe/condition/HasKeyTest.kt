package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueFormatEnum
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

private data class HasKeyTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class HasKeyTest :
    DescribeSpec({
      val context = Context()

      val operationEnum = OperationEnum.HAS_KEY

      val json =
          PolicyVariableStatic(
              value = """{"a":1,"b":2,"c":3}""",
              type = VariableValueTypeEnum.OBJECT,
              format = VariableValueFormatEnum.JSON)

      val jsonNode =
          PolicyVariableStatic(
              value = """{"a":1,"b":2,"c":3}""",
              type = VariableValueTypeEnum.STRING,
              format = VariableValueFormatEnum.JSON)

      val strNode =
          PolicyVariableStatic(
              value = """"a"""",
              type = VariableValueTypeEnum.STRING,
              format = VariableValueFormatEnum.JSON)

      describe("logic") {
        withData(
            nameFn = { "HasKey: ${it.name}" },
            listOf(
                // object
                HasKeyTestData(
                    name = "object",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(json, string("a"))),
                    expected = true),
                HasKeyTestData(
                    name = "object false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(json, string("d"))),
                    expected = false),
                // json node
                HasKeyTestData(
                    name = "jsonNode",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(jsonNode, string("a"))),
                    expected = true),
                HasKeyTestData(
                    name = "object false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(jsonNode, string("d"))),
                    expected = false),
                // wrong type
                HasKeyTestData(
                    name = "object node wrong type",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(string("3"), string("a"))),
                    expected = null),
                HasKeyTestData(
                    name = "object key wrong type",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(json, int(2))),
                    expected = null),
                HasKeyTestData(
                    name = "json node wrong type",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(strNode, string("a"))),
                    expected = null),
            )) {
              it.given.check(context, EmptyPolicyCatalog()) shouldBe it.expected
            }
      }
    })
