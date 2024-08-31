package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

private data class RegexpTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class RegexpTest :
    DescribeSpec({
      val context = Context()

      val operationEnum = OperationEnum.REGEXP_MATCH
      val camelCaseRegex = """^[a-z]+((\d)|([A-Z0-9][a-z0-9]+))*([A-Z])?${'$'}"""
      val digitRegex = """^(\d)*${'$'}"""

      describe("logic") {
        withData(
            nameFn = { "Regexp: ${it.name}" },
            listOf(
                // string
                RegexpTestData(
                    name = "string camel case",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(string("camelCase"), string(camelCaseRegex))),
                    expected = true),
                RegexpTestData(
                    name = "string camel case false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(string("PascalCase"), string(camelCaseRegex))),
                    expected = false),
                RegexpTestData(
                    name = "string cast",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum, args = listOf(int(123), string(digitRegex))),
                    expected = true),
                RegexpTestData(
                    name = "string cast false",
                    given =
                        PolicyConditionAtomic(
                            operation = operationEnum,
                            args = listOf(double(1.5), string(digitRegex))),
                    expected = false),
            )) {
              it.given.check(context, EmptyPolicyCatalog()) shouldBe it.expected
            }
      }
    })
