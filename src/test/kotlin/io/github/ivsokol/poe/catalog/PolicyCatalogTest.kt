package io.github.ivsokol.poe.catalog

import io.github.ivsokol.poe.*
import io.github.ivsokol.poe.action.PolicyActionClear
import io.github.ivsokol.poe.condition.OperationEnum
import io.github.ivsokol.poe.condition.PolicyConditionAtomic
import io.github.ivsokol.poe.condition.PolicyConditionDefault
import io.github.ivsokol.poe.policy.Policy
import io.github.ivsokol.poe.policy.PolicyDefault
import io.github.ivsokol.poe.policy.PolicyTargetEffectEnum
import io.github.ivsokol.poe.variable.PolicyVariableResolver
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PolicyCatalogTest :
    DescribeSpec({
      val policyCatalog =
          PolicyCatalog(
              id = "test-catalog",
              policyConditions =
                  listOf(
                      condition("c1", SemVer(1, 0, 0), "release1"),
                      condition("c1", SemVer(1, 0, 0), "duplicateRelease1"),
                      condition("c1", SemVer(2, 0, 0), "release2"),
                      condition("c1", SemVer(2, 0, 0, "SNAPSHOT"), "release2Snapshot"),
                      condition("c1", null, "null"),
                      condition("c2", null, "c2null", listOf("l1", "l2")),
                      condition("c2", SemVer(1, 0, 0), "c2", listOf("l1", "l2")),
                      condition("c3", SemVer(1, 0, 0), "c3", listOf("l1")),
                      condition("c3", SemVer(1, 2, 0), "c3", listOf("l1")),
                      condition("c4", SemVer(1, 0, 0), "c4", listOf("l2")),
                  ),
              policyVariables =
                  listOf(
                      variable("v1", SemVer(1, 0, 0), "release1"),
                      variable("v1", SemVer(1, 0, 0), "duplicateRelease1"),
                      variable("v1", SemVer(2, 0, 0), "release2"),
                      variable("v1", SemVer(2, 0, 0, "SNAPSHOT"), "release2Snapshot"),
                      variable("v1", null, "null"),
                      variable("v2", null, "null"),
                      variable("v2", SemVer(1, 0, 0), "null"),
                      variable("v3", SemVer(1, 0, 0), "null"),
                  ),
              policyVariableResolvers =
                  listOf(
                      resolver("r1", SemVer(1, 0, 0), "release1"),
                      resolver("r1", SemVer(1, 0, 0), "duplicateRelease1"),
                      resolver("r1", SemVer(2, 0, 0), "release2"),
                      resolver("r1", SemVer(2, 0, 0, "SNAPSHOT"), "release2Snapshot"),
                      resolver("r1", null, "null"),
                      resolver("r2", null, "null"),
                      resolver("r2", SemVer(1, 0, 0), "null"),
                      resolver("r3", SemVer(1, 0, 0), "null"),
                  ),
              policyActions =
                  listOf(
                      action("a1", SemVer(1, 0, 0), "release1"),
                      action("a1", SemVer(1, 0, 0), "duplicateRelease1"),
                      action("a1", SemVer(2, 0, 0), "release2"),
                      action("a1", SemVer(2, 0, 0, "SNAPSHOT"), "release2Snapshot"),
                      action("a1", null, "null"),
                      action("a2", null, "null"),
                      action("a2", SemVer(1, 0, 0), "null"),
                      action("a3", SemVer(1, 0, 0), "null"),
                  ),
              policies =
                  listOf(
                      policy("p1", SemVer(1, 0, 0), "release1"),
                      policy("p1", SemVer(1, 0, 0), "duplicateRelease1"),
                      policy("p1", SemVer(2, 0, 0), "release2"),
                      policy("p1", SemVer(2, 0, 0, "SNAPSHOT"), "release2Snapshot"),
                      policy("p1", null, "null"),
                      policy("p2", null, "p2null", listOf("l1", "l2")),
                      policy("p2", SemVer(1, 0, 0), "p2", listOf("l1", "l2")),
                      policy("p3", SemVer(1, 0, 0), "p3", listOf("l1")),
                      policy("p3", SemVer(1, 2, 0), "p3", listOf("l1")),
                      policy("p4", SemVer(1, 0, 0), "p4", listOf("l2")),
                  ),
          )

      describe("getPolicyCondition") {
        it("should return null if there is no condition with given id") {
          val catalog = EmptyPolicyCatalog()
          catalog.getPolicyCondition("id") shouldBe null
        }
        it("should return specific version of condition if version is specified") {
          policyCatalog.getPolicyCondition("c1", SemVer(1, 0, 0)) shouldBe
              condition("c1", SemVer(1, 0, 0), "release1")
        }
        it(
            "should return null if version is specified but there is no condition with given version") {
              policyCatalog.getPolicyCondition("c1", SemVer(1, 0, 1)) shouldBe null
            }
        it(
            "should return latest version of condition if version is not specified and there is no condition without version") {
              policyCatalog.getPolicyCondition("c3") shouldBe
                  condition("c3", SemVer(1, 2, 0), "c3", listOf("l1"))
            }
        it(
            "should return correct condition if version is not specified and there is condition without version") {
              policyCatalog.getPolicyCondition("c1") shouldBe condition("c1", null, "null")
            }
      }

      describe("getPolicyVariable") {
        it("should return null if there is no variable with given id") {
          val catalog = EmptyPolicyCatalog()
          catalog.getPolicyVariable("id") shouldBe null
        }
        it("should return specific version of variable if version is specified") {
          policyCatalog.getPolicyVariable("v1", SemVer(1, 0, 0)) shouldBe
              variable("v1", SemVer(1, 0, 0), "release1")
        }
        it(
            "should return null if version is specified but there is no variable with given version") {
              policyCatalog.getPolicyVariable("v1", SemVer(1, 0, 1)) shouldBe null
            }
        it("should return latest version of variable if version is not specified") {
          policyCatalog.getPolicyVariable("v3") shouldBe variable("v3", SemVer(1, 0, 0), "null")
        }
        it(
            "should return null version of variable if version is not specified and there is variable with no version") {
              policyCatalog.getPolicyVariable("v2") shouldBe variable("v2", null, "null")
            }
      }

      describe("getPolicyVariableResolver") {
        it("should return null if there is no resolver with given id") {
          val catalog = EmptyPolicyCatalog()
          catalog.getPolicyVariableResolver("id") shouldBe null
        }
        it("should return specific version of resolver if version is specified") {
          policyCatalog.getPolicyVariableResolver("r1", SemVer(1, 0, 0)) shouldBe
              resolver("r1", SemVer(1, 0, 0), "release1")
        }
        it(
            "should return null if version is specified but there is no resolver with given version") {
              policyCatalog.getPolicyVariableResolver("r1", SemVer(1, 0, 1)) shouldBe null
            }
        it("should return latest version of resolver if version is not specified") {
          policyCatalog.getPolicyVariableResolver("r3") shouldBe
              resolver("r3", SemVer(1, 0, 0), "null")
        }
        it(
            "should return null version of resolver if version is not specified and there is resolver with no version") {
              policyCatalog.getPolicyVariableResolver("r2") shouldBe resolver("r2", null, "null")
            }
      }

      describe("getPolicyAction") {
        it("should return null if there is no action with given id") {
          val catalog = EmptyPolicyCatalog()
          catalog.getPolicyAction("id") shouldBe null
        }
        it("should return specific version of action if version is specified") {
          policyCatalog.getPolicyAction("a1", SemVer(1, 0, 0)) shouldBe
              action("a1", SemVer(1, 0, 0), "release1")
        }
        it("should return null if version is specified but there is no action with given version") {
          policyCatalog.getPolicyAction("a1", SemVer(1, 0, 1)) shouldBe null
        }
        it("should return latest version of action if version is not specified") {
          policyCatalog.getPolicyAction("a3") shouldBe action("a3", SemVer(1, 0, 0), "null")
        }
        it(
            "should return null version of action if version is not specified and there is action with no version") {
              policyCatalog.getPolicyAction("a2") shouldBe action("a2", null, "null")
            }
      }

      describe("getPolicy") {
        it("should return null if there is no policy with given id") {
          val catalog = EmptyPolicyCatalog()
          catalog.getPolicy("id") shouldBe null
        }
        it("should return specific version of policy if version is specified") {
          policyCatalog.getPolicy("p1", SemVer(1, 0, 0)) shouldBe
              policy("p1", SemVer(1, 0, 0), "release1")
        }
        it("should return null if version is specified but there is no policy with given version") {
          policyCatalog.getPolicy("p1", SemVer(1, 0, 1)) shouldBe null
        }
        it("should return latest version of policy if version is not specified") {
          policyCatalog.getPolicy("p3") shouldBe policy("p3", SemVer(1, 2, 0), "p3", listOf("l1"))
        }
        it(
            "should return null version of policy if version is not specified and there is policy with no version") {
              policyCatalog.getPolicy("p2") shouldBe
                  policy("p2", null, "p2null", listOf("l1", "l2"))
            }
      }

      describe("searchConditionsByLabels") {
        it("should throw if labels are empty") {
          shouldThrow<IllegalArgumentException> {
                policyCatalog.searchConditionsByLabels(
                    emptySet(), logic = LabelSearchLogicEnum.ANY_OF)
              }
              .message shouldBe "Labels must not be empty"
        }
        it("should return empty list if there are no conditions with given labels") {
          policyCatalog.searchConditionsByLabels(
              setOf("l3"), logic = LabelSearchLogicEnum.ANY_OF) shouldBe emptyList()
        }
        it("should return conditions with given labels if logic is ANY_OF") {
          policyCatalog.searchConditionsByLabels(
              setOf("l1"), logic = LabelSearchLogicEnum.ANY_OF) shouldBe
              listOf(
                  condition("c2", SemVer(1, 0, 0), "c2", listOf("l1", "l2")),
                  condition("c3", SemVer(1, 2, 0), "c3", listOf("l1")))
        }
        it("should return conditions with given labels if logic is ALL_OF") {
          policyCatalog.searchConditionsByLabels(
              setOf("l1", "l2"), logic = LabelSearchLogicEnum.ALL_OF) shouldBe
              listOf(condition("c2", SemVer(1, 0, 0), "c2", listOf("l1", "l2")))
        }
      }

      describe("searchPoliciesByLabels") {
        it("should throw if labels are empty") {
          shouldThrow<IllegalArgumentException> {
                policyCatalog.searchPoliciesByLabels(
                    emptySet(), logic = LabelSearchLogicEnum.ANY_OF)
              }
              .message shouldBe "Labels must not be empty"
        }
        it("should return empty list if there are no policies with given labels") {
          policyCatalog.searchPoliciesByLabels(
              setOf("l3"), logic = LabelSearchLogicEnum.ANY_OF) shouldBe emptyList()
        }
        it("should return policies with given labels if logic is ANY_OF") {
          policyCatalog.searchPoliciesByLabels(
              setOf("l1"), logic = LabelSearchLogicEnum.ANY_OF) shouldBe
              listOf(
                  policy("p2", SemVer(1, 0, 0), "p2", listOf("l1", "l2")),
                  policy("p3", SemVer(1, 2, 0), "p3", listOf("l1")))
        }
        it("should return policies with given labels if logic is ALL_OF") {
          policyCatalog.searchPoliciesByLabels(
              setOf("l1", "l2"), logic = LabelSearchLogicEnum.ALL_OF) shouldBe
              listOf(policy("p2", SemVer(1, 0, 0), "p2", listOf("l1", "l2")))
        }
      }

      describe("emptyCatalog") {
        val policyCatalogEmpty = EmptyPolicyCatalog()
        policyCatalogEmpty shouldNotBe null
        policyCatalogEmpty.version shouldBe DefaultCalVer()
        policyCatalogEmpty.id shouldNotBe null
      }

      describe("getAllConditions") {
        it("should return all conditions") {
          val actual = policyCatalog.getAllConditions()
          actual shouldContainExactlyInAnyOrder
              listOf(
                  condition("c1", SemVer(2, 0, 0), "release2"),
                  condition("c2", SemVer(1, 0, 0), "c2", listOf("l1", "l2")),
                  condition("c3", SemVer(1, 2, 0), "c3", listOf("l1")),
                  condition("c4", SemVer(1, 0, 0), "c4", listOf("l2")))
        }
        it("should return empty conditions") {
          val actual =
              EmptyPolicyCatalog().getAllConditions().filter { it !is PolicyConditionDefault }
          actual shouldBe emptyList()
        }
      }

      describe("getAllPolicies") {
        it("should return all policies") {
          val actual = policyCatalog.getAllPolicies()
          actual shouldContainExactlyInAnyOrder
              listOf(
                  policy("p1", SemVer(2, 0, 0), "release2"),
                  policy("p2", SemVer(1, 0, 0), "p2", listOf("l1", "l2")),
                  policy("p3", SemVer(1, 2, 0), "p3", listOf("l1")),
                  policy("p4", SemVer(1, 0, 0), "p4", listOf("l2")))
        }
        it("should return empty policies") {
          val actual = EmptyPolicyCatalog().getAllPolicies().filter { it !is PolicyDefault }
          actual shouldBe emptyList()
        }
      }

      describe("serialization/deserialization") {
        val json = Json {
          serializersModule = catalogSerializersModule
          explicitNulls = false
          encodeDefaults = true
        }
        it("should serialize") {
          val given =
              PolicyCatalog(
                  id = "id",
                  version = CalVer(2024, 2, 17),
                  policyConditions =
                      listOf(
                          condition("c1", SemVer(1, 0, 0), "release1"),
                      ),
                  policyVariables =
                      listOf(
                          variable("v1", SemVer(1, 0, 0), "release1"),
                      ),
                  policyVariableResolvers =
                      listOf(
                          resolver("r1", SemVer(1, 0, 0), "release1"),
                      ),
                  policies =
                      listOf(
                          policy("p1", SemVer(1, 0, 0), "release1"),
                      ),
                  policyActions =
                      listOf(
                          action("a1", SemVer(1, 0, 0), "release1"),
                      ))
          val actual = json.encodeToString(given)
          val expected =
              """
 {
  "id": "id",
  "version": "2024-02-17",
    "policies": [
      {
        "id": "p1",
        "version": "1.0.0",
        "description": "release1",
        "targetEffect": "permit",
        "condition": {
          "default": true
        }
      }
    ],
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
  ],
    "policyActions": [
      {
        "id": "a1",
        "version": "1.0.0",
        "description": "release1",
        "key": "foo",
        "type": "clear"
      }
    ]
}                   
"""
                  .trimIndent()
          actual shouldEqualJson expected
        }

        it("should serialize with defaults") {
          val given =
              PolicyCatalog(
                  id = "id",
                  version = CalVer(2024, 2, 17),
                  withDefaultConditions = true,
                  withDefaultPolicies = true,
                  policyConditions =
                      listOf(
                          condition("c1", SemVer(1, 0, 0), "release1"),
                      ),
                  policyVariables =
                      listOf(
                          variable("v1", SemVer(1, 0, 0), "release1"),
                      ),
                  policyVariableResolvers =
                      listOf(
                          resolver("r1", SemVer(1, 0, 0), "release1"),
                      ),
                  policies =
                      listOf(
                          policy("p1", SemVer(1, 0, 0), "release1"),
                      ),
                  policyActions =
                      listOf(
                          action("a1", SemVer(1, 0, 0), "release1"),
                      ))
          val actual = json.encodeToString(given)
          val expected =
              """
 {
  "id": "id",
  "version": "2024-02-17",
    "withDefaultPolicies": true,
    "withDefaultConditions": true,
    "policies": [
      {
        "id": "p1",
        "version": "1.0.0",
        "description": "release1",
        "targetEffect": "permit",
        "condition": {
          "default": true
        }
      }
    ],
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
  ],
    "policyActions": [
      {
        "id": "a1",
        "version": "1.0.0",
        "description": "release1",
        "key": "foo",
        "type": "clear"
      }
    ]
}                   
"""
                  .trimIndent()
          actual shouldEqualJson expected
        }

        it("should deserialize") {
          val given =
              """
    {
  "id": "id",
  "version": "2024-02-17",
    "policies": [
      {
        "id": "p1",
        "version": "1.0.0",
        "description": "release1",
        "targetEffect": "permit",
        "condition": {
          "default": true
        }
      }
    ],
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
  ],
    "policyActions": [
      {
        "id": "a1",
        "version": "1.0.0",
        "description": "release1",
        "key": "foo",
        "type": "clear"
      }
    ]
}               
              """
                  .trimIndent()
          val actual = json.decodeFromString<PolicyCatalog>(given)
          val expected =
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
                      ),
                  policies =
                      listOf(
                          policy("p1", SemVer(1, 0, 0), "release1"),
                      ),
                  policyActions =
                      listOf(
                          action("a1", SemVer(1, 0, 0), "release1"),
                      ))
          actual shouldBe expected
          actual.getAllPolicies().size shouldBe 1
          actual.getAllConditions().size shouldBe 1
        }

        it("should deserialize with defaults") {
          val given =
              """
    {
  "id": "id",
  "version": "2024-02-17",
      "withDefaultPolicies": true,
      "withDefaultConditions": true,
    "policies": [
      {
        "id": "p1",
        "version": "1.0.0",
        "description": "release1",
        "targetEffect": "permit",
        "condition": {
          "default": true
        }
      }
    ],
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
  ],
    "policyActions": [
      {
        "id": "a1",
        "version": "1.0.0",
        "description": "release1",
        "key": "foo",
        "type": "clear"
      }
    ]
}               
              """
                  .trimIndent()
          val actual = json.decodeFromString<PolicyCatalog>(given)
          val expected =
              PolicyCatalog(
                  id = "id",
                  version = CalVer(2024, 2, 17),
                  withDefaultPolicies = true,
                  withDefaultConditions = true,
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
                      ),
                  policies =
                      listOf(
                          policy("p1", SemVer(1, 0, 0), "release1"),
                      ),
                  policyActions =
                      listOf(
                          action("a1", SemVer(1, 0, 0), "release1"),
                      ))
          actual shouldBe expected
          actual.getAllPolicies().size shouldBe 7
          actual.getAllConditions().size shouldBe 4
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

private fun action(
    id: String,
    version: SemVer? = null,
    description: String,
    labels: List<String>? = null
) = PolicyActionClear(id, version, description, labels, key = "foo")

private fun policy(
    id: String,
    version: SemVer? = null,
    description: String,
    labels: List<String>? = null
) =
    Policy(
        id,
        version,
        description,
        labels,
        condition = PolicyConditionDefault(true),
        targetEffect = PolicyTargetEffectEnum.PERMIT)
