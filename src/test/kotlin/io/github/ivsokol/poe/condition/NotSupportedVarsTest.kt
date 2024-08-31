package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.variable.NullVariableValue
import io.github.ivsokol.poe.variable.VariableRuntimeTypeEnum
import io.github.ivsokol.poe.variable.VariableValue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NotSupportedVarsTest :
    FunSpec({
      val context = Context()
      val objectMapper = context.options.objectMapper

      test("gt NullVariableValue should throw") {
        shouldThrow<IllegalArgumentException> {
              gt(NullVariableValue(), NullVariableValue(), false, "foo")
            }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }

      test("gte NullVariableValue should throw") {
        shouldThrow<IllegalArgumentException> {
              gte(NullVariableValue(), NullVariableValue(), false, "foo")
            }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }

      test("lt NullVariableValue should throw") {
        shouldThrow<IllegalArgumentException> {
              lt(NullVariableValue(), NullVariableValue(), false, "foo")
            }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }

      test("lte NullVariableValue should throw") {
        shouldThrow<IllegalArgumentException> {
              lte(NullVariableValue(), NullVariableValue(), false, "foo")
            }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }

      test("isEmpty NullVariableValue should throw") {
        shouldThrow<IllegalArgumentException> { isEmpty(NullVariableValue(), "foo") }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }
      test("isNotEmpty NullVariableValue should throw") {
        shouldThrow<IllegalArgumentException> { isNotEmpty(NullVariableValue(), "foo") }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }
      test("isBlank NullVariableValue should throw") {
        shouldThrow<IllegalArgumentException> { isBlank(NullVariableValue(), "foo") }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }

      test("isNotBlank NullVariableValue should throw") {
        shouldThrow<IllegalArgumentException> { isNotBlank(NullVariableValue(), "foo") }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }

      test("equals UNKNOWN should throw") {
        shouldThrow<IllegalArgumentException> {
              eq(
                  VariableValue(VariableRuntimeTypeEnum.UNKNOWN, null),
                  NullVariableValue(),
                  false,
                  false,
                  false,
                  Context(),
                  "foo")
            }
            .message shouldBe "foo -> Variable type UNKNOWN is not supported in condition"
      }

      test("startsWith NULL should throw") {
        shouldThrow<IllegalArgumentException> {
              startsWith(
                  NullVariableValue(), NullVariableValue(), false, false, false, Context(), "foo")
            }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }

      test("endsWith NULL should throw") {
        shouldThrow<IllegalArgumentException> {
              endsWith(
                  NullVariableValue(), NullVariableValue(), false, false, false, context, "foo")
            }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }

      test("contains NULL should throw") {
        shouldThrow<IllegalArgumentException> {
              contains(
                  NullVariableValue(), NullVariableValue(), false, false, false, context, "foo")
            }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }

      test("contains bad JsonNode should throw") {
        shouldThrow<IllegalArgumentException> {
              contains(
                  VariableValue(VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree("1")),
                  NullVariableValue(),
                  false,
                  false,
                  false,
                  context,
                  "foo")
            }
            .message shouldBe "foo -> Variable type JSON_NODE is not supported in condition"
      }

      test("isPositive NULL should throw") {
        shouldThrow<IllegalArgumentException> { isPositive(NullVariableValue(), "foo") }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }
      test("isNegative NULL should throw") {
        shouldThrow<IllegalArgumentException> { isNegative(NullVariableValue(), "foo") }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }
      test("isZero NULL should throw") {
        shouldThrow<IllegalArgumentException> { isZero(NullVariableValue(), "foo") }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }

      test("isPast NULL should throw") {
        shouldThrow<IllegalArgumentException> { isPast(NullVariableValue(), context, "foo") }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }
      test("isFuture NULL should throw") {
        shouldThrow<IllegalArgumentException> { isFuture(NullVariableValue(), context, "foo") }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }

      test("hasKey NULL should throw") {
        shouldThrow<IllegalArgumentException> {
              hasKey(NullVariableValue(), NullVariableValue(), "foo")
            }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }

      test("hasKey bad JsonNode should throw") {
        shouldThrow<IllegalArgumentException> {
              hasKey(
                  VariableValue(VariableRuntimeTypeEnum.JSON_NODE, objectMapper.readTree("1")),
                  NullVariableValue(),
                  "foo")
            }
            .message shouldBe "foo -> Variable type JSON_NODE is not supported in condition"
      }

      test("isUnique NULL should throw") {
        shouldThrow<IllegalArgumentException> { isUnique(NullVariableValue(), "foo") }
            .message shouldBe "foo -> Variable type NULL is not supported in condition"
      }

      test("matchesSchema UNKNOWN should throw") {
        shouldThrow<IllegalArgumentException> {
              matchesSchema(
                  VariableValue(VariableRuntimeTypeEnum.UNKNOWN, null),
                  VariableValue(VariableRuntimeTypeEnum.STRING, """{"type":"string"}"""),
                  context,
                  "foo")
            }
            .message shouldBe "foo -> Variable type UNKNOWN is not supported in condition"
      }
    })
