package io.github.ivsokol.poe

import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.condition.OperationEnum
import io.github.ivsokol.poe.condition.PolicyConditionAtomic
import io.github.ivsokol.poe.variable.PolicyVariableResolver
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith

class PolicyEngineTest :
    DescribeSpec({
      describe("constructor") {
        val policyCatalog =
            PolicyCatalog(
                id = "id",
                version = CalVer(2024, 2, 17),
                policyConditions =
                    listOf(
                        condition("c1", SemVer(1, 0, 0), "release1"),
                    ),
                policyVariables =
                    listOf(
                        variable("v1", SemVer(1, 0, 0), "release1")
                            .copy(type = VariableValueTypeEnum.STRING),
                    ),
                policyVariableResolvers =
                    listOf(
                        resolver("r1", SemVer(1, 0, 0), "release1"),
                    ))

        it("should create engine from catalog") {
          val actual = PolicyEngine(policyCatalog)
          actual shouldNotBe null
          actual.policyCatalogVersion() shouldBe "id:2024-02-17"
        }
        it("should create engine from string") {
          val given =
              """
    {
  "id": "id2",
  "version": "2024-02-17",
  "policyConditions": [
    {
      "id": "c1",
      "version": "1.0.0",
      "description": "release1",
      "operation": "GreaterThan",
      "args": [
        {
          "type": "string",
          "value": "s1"
        },
        {
          "type": "string",
          "value": "s2"
        }
      ]
    }
  ],
  "policyVariables": [
    {
      "id": "v1",
      "version": "1.0.0",
      "description": "release1",
      "type": "string",
      "value": "value"
    }
  ],
  "policyVariableResolvers": [
    {
      "id": "r1",
      "version": "1.0.0",
      "description": "release1",
      "key": "str1"
    }
  ]
}               
              """
                  .trimIndent()
          val actual = PolicyEngine(given)
          actual shouldNotBe null
          actual.policyCatalogVersion() shouldBe "id2:2024-02-17"
        }
        it("should throw for blank string") {
          shouldThrow<IllegalArgumentException> { PolicyEngine(" ") }.message shouldBe
              "Policy catalog must not be blank"
        }
        it("should throw for bad json") {
          shouldThrow<IllegalArgumentException> { PolicyEngine("abc") }.message shouldStartWith
              "Unexpected JSON token at offset"
        }
        it("should throw for incorrect json") {
          shouldThrow<IllegalArgumentException> { PolicyEngine("""{"id":"22"}""") }
              .message shouldContain "Conditions and policies cannot be empty at the same time"
        }
      }
    })

private fun condition(
    id: String,
    version: SemVer?,
    description: String,
    labels: List<String>? = null
) =
    PolicyConditionAtomic(
        id = id,
        version = version,
        description = description,
        operation = OperationEnum.GREATER_THAN,
        labels = labels,
        args = listOf(strVar("s1"), strVar("s2")))

private fun strVar(value: String) =
    PolicyVariableStatic(value = value, type = VariableValueTypeEnum.STRING)

private fun variable(
    id: String,
    version: SemVer? = null,
    description: String,
    labels: List<String>? = null
) = PolicyVariableStatic(id, version, description, labels, "value")

private fun resolver(
    id: String,
    version: SemVer? = null,
    description: String,
    labels: List<String>? = null
) = PolicyVariableResolver(id, version, description, labels, key = "str1")
