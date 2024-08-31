package com.github.ivsokol.poe.condition

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.DefaultObjectMapper
import com.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import com.github.ivsokol.poe.variable.PolicyVariableDynamic
import com.github.ivsokol.poe.variable.PolicyVariableResolver
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

private data class NullLogicTestData(
    val name: String,
    val given: PolicyConditionAtomic,
    val expected: Boolean?
)

class NullLogicTests :
    DescribeSpec({
      val objectMapper = DefaultObjectMapper()
      val fullObjectBody =
          mapOf(
              "str" to "a",
              "strEmpty" to "",
              "strBlank" to " ",
              "arrEmpty" to emptyList<String>(),
              "arrNodeEmpty" to objectMapper.createArrayNode(),
              "i" to 1,
              "n" to null,
          )
      val context = Context(request = fullObjectBody)

      describe("isNull") {
        val operationEnum = OperationEnum.IS_NULL
        withData(
            nameFn = { "IsNull: ${it.name}" },
            listOf(
                NullLogicTestData(
                    name = "null", given = atomic(operationEnum, "n"), expected = true),
                NullLogicTestData(
                    name = "nullKey", given = atomic(operationEnum, "null"), expected = true),
                NullLogicTestData(
                    name = "string", given = atomic(operationEnum, "str"), expected = false),
                NullLogicTestData(
                    name = "stringEmpty",
                    given = atomic(operationEnum, "strEmpty"),
                    expected = false),
                NullLogicTestData(
                    name = "stringBlank",
                    given = atomic(operationEnum, "strBlank"),
                    expected = false),
                NullLogicTestData(
                    name = "int", given = atomic(operationEnum, "i"), expected = false),
            )) {
              it.given.check(
                  context.copy(cache = com.github.ivsokol.poe.cache.HashMapCache()),
                  EmptyPolicyCatalog()) shouldBe it.expected
            }
      }

      describe("isNotNull") {
        val operationEnum = OperationEnum.IS_NOT_NULL
        withData(
            nameFn = { "IsNotNull: ${it.name}" },
            listOf(
                NullLogicTestData(
                    name = "null", given = atomic(operationEnum, "n"), expected = false),
                NullLogicTestData(
                    name = "nullKey", given = atomic(operationEnum, "null"), expected = false),
                NullLogicTestData(
                    name = "string", given = atomic(operationEnum, "str"), expected = true),
                NullLogicTestData(
                    name = "stringEmpty",
                    given = atomic(operationEnum, "strEmpty"),
                    expected = true),
                NullLogicTestData(
                    name = "stringBlank",
                    given = atomic(operationEnum, "strBlank"),
                    expected = true),
                NullLogicTestData(
                    name = "int", given = atomic(operationEnum, "i"), expected = true),
            )) {
              it.given.check(
                  context.copy(cache = com.github.ivsokol.poe.cache.HashMapCache()),
                  EmptyPolicyCatalog()) shouldBe it.expected
            }
      }

      describe("isEmpty") {
        val operationEnum = OperationEnum.IS_EMPTY
        withData(
            nameFn = { "IsEmpty: ${it.name}" },
            listOf(
                NullLogicTestData(
                    name = "null", given = atomic(operationEnum, "n"), expected = null),
                NullLogicTestData(
                    name = "nullKey", given = atomic(operationEnum, "null"), expected = null),
                NullLogicTestData(
                    name = "string", given = atomic(operationEnum, "str"), expected = false),
                NullLogicTestData(
                    name = "stringEmpty",
                    given = atomic(operationEnum, "strEmpty"),
                    expected = true),
                NullLogicTestData(
                    name = "stringBlank",
                    given = atomic(operationEnum, "strBlank"),
                    expected = false),
                NullLogicTestData(
                    name = "arrEmpty", given = atomic(operationEnum, "arrEmpty"), expected = true),
                NullLogicTestData(
                    name = "arrNodeEmpty",
                    given = atomic(operationEnum, "arrNodeEmpty"),
                    expected = true),
                NullLogicTestData(
                    name = "int", given = atomic(operationEnum, "i"), expected = null),
            )) {
              it.given.check(
                  context.copy(cache = com.github.ivsokol.poe.cache.HashMapCache()),
                  EmptyPolicyCatalog()) shouldBe it.expected
            }
      }

      describe("isNotEmpty") {
        val operationEnum = OperationEnum.IS_NOT_EMPTY
        withData(
            nameFn = { "IsNotEmpty: ${it.name}" },
            listOf(
                NullLogicTestData(
                    name = "null", given = atomic(operationEnum, "n"), expected = null),
                NullLogicTestData(
                    name = "nullKey", given = atomic(operationEnum, "null"), expected = null),
                NullLogicTestData(
                    name = "string", given = atomic(operationEnum, "str"), expected = true),
                NullLogicTestData(
                    name = "stringEmpty",
                    given = atomic(operationEnum, "strEmpty"),
                    expected = false),
                NullLogicTestData(
                    name = "stringBlank",
                    given = atomic(operationEnum, "strBlank"),
                    expected = true),
                NullLogicTestData(
                    name = "arrEmpty", given = atomic(operationEnum, "arrEmpty"), expected = false),
                NullLogicTestData(
                    name = "arrNodeEmpty",
                    given = atomic(operationEnum, "arrNodeEmpty"),
                    expected = false),
                NullLogicTestData(
                    name = "int", given = atomic(operationEnum, "i"), expected = null),
            )) {
              it.given.check(
                  context.copy(cache = com.github.ivsokol.poe.cache.HashMapCache()),
                  EmptyPolicyCatalog()) shouldBe it.expected
            }
      }

      describe("isBlank") {
        val operationEnum = OperationEnum.IS_BLANK
        withData(
            nameFn = { "IsBlank: ${it.name}" },
            listOf(
                NullLogicTestData(
                    name = "null", given = atomic(operationEnum, "n"), expected = null),
                NullLogicTestData(
                    name = "nullKey", given = atomic(operationEnum, "null"), expected = null),
                NullLogicTestData(
                    name = "string", given = atomic(operationEnum, "str"), expected = false),
                NullLogicTestData(
                    name = "stringEmpty",
                    given = atomic(operationEnum, "strEmpty"),
                    expected = true),
                NullLogicTestData(
                    name = "stringBlank",
                    given = atomic(operationEnum, "strBlank"),
                    expected = true),
                NullLogicTestData(
                    name = "arrEmpty", given = atomic(operationEnum, "arrEmpty"), expected = null),
                NullLogicTestData(
                    name = "arrNodeEmpty",
                    given = atomic(operationEnum, "arrNodeEmpty"),
                    expected = null),
                NullLogicTestData(
                    name = "int", given = atomic(operationEnum, "i"), expected = null),
            )) {
              it.given.check(
                  context.copy(cache = com.github.ivsokol.poe.cache.HashMapCache()),
                  EmptyPolicyCatalog()) shouldBe it.expected
            }
      }

      describe("isNotBlank") {
        val operationEnum = OperationEnum.IS_NOT_BLANK
        withData(
            nameFn = { "IsNotBlank: ${it.name}" },
            listOf(
                NullLogicTestData(
                    name = "null", given = atomic(operationEnum, "n"), expected = null),
                NullLogicTestData(
                    name = "nullKey", given = atomic(operationEnum, "null"), expected = null),
                NullLogicTestData(
                    name = "string", given = atomic(operationEnum, "str"), expected = true),
                NullLogicTestData(
                    name = "stringEmpty",
                    given = atomic(operationEnum, "strEmpty"),
                    expected = false),
                NullLogicTestData(
                    name = "stringBlank",
                    given = atomic(operationEnum, "strBlank"),
                    expected = false),
                NullLogicTestData(
                    name = "arrEmpty", given = atomic(operationEnum, "arrEmpty"), expected = null),
                NullLogicTestData(
                    name = "arrNodeEmpty",
                    given = atomic(operationEnum, "arrNodeEmpty"),
                    expected = null),
                NullLogicTestData(
                    name = "int", given = atomic(operationEnum, "i"), expected = null),
            )) {
              it.given.check(
                  context.copy(cache = com.github.ivsokol.poe.cache.HashMapCache()),
                  EmptyPolicyCatalog()) shouldBe it.expected
            }
      }
    })

private fun dynamic(key: String) =
    PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolver(key = key)))

private fun atomic(operation: OperationEnum, key: String) =
    PolicyConditionAtomic(operation = operation, args = listOf(dynamic(key)))
