package io.github.ivsokol.poe.policy

import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.action.PolicyActionRef
import io.github.ivsokol.poe.condition.PolicyConditionDefault
import io.github.ivsokol.poe.condition.PolicyConditionRef
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class IPolicySerializerTest :
    DescribeSpec({
      val json = Json {
        serializersModule = policySerializersModule
        explicitNulls = false
        encodeDefaults = true
      }

      describe("exceptions") {
        it("bad json") {
          val given = """{targetEffect": "permit"}"""
          shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicy>(given) }
              .message shouldStartWith "Unexpected JSON token at offset"
        }

        it("wrong json") {
          val given = """false"""
          shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicy>(given) }
              .message shouldStartWith "Not correct JsonElement for IPolicy DeserializationStrategy"
        }

        it("bad targetEffect") {
          val given = """{"targetEffect": "bad"}"""
          shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicy>(given) }
              .message shouldContain
              "PolicyTargetEffectEnum does not contain element with name 'bad'"
        }

        it("no targetEffect field") {
          val given = """{"left":{"type":"int","value":1},"right":{"type":"int","value":1}}"""
          shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicy>(given) }
              .message shouldBe
              "No corresponding operation field for IPolicy DeserializationStrategy"
        }
      }

      describe("Policy") {
        it("should serialize minimal") {
          val given: IPolicy =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(true))
          val expected = """{"targetEffect": "deny","condition":{"default":true}}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize minimal") {
          val expected: IPolicy =
              Policy(
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(true))

          val given = """{"targetEffect": "deny","condition":{"default":true}}"""
          val actual = json.decodeFromString<IPolicy>(given)
          actual shouldBe expected
        }

        it("should serialize full") {
          val given =
              Policy(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  description = "desc",
                  labels = listOf("1"),
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(true),
                  strictTargetEffect = false,
                  constraint = PolicyConditionRef(id = "2"),
                  actions = listOf(PolicyActionRelationship(action = PolicyActionRef(id = "3"))),
                  lenientConstraints = false,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.STOP_ON_FAILURE,
                  ignoreErrors = false,
                  priority = 100)
          val expected =
              """
{
  "id": "1",
  "version": "1.0.0",
  "description": "desc",
  "labels": [
    "1"
  ],
  "constraint": {
    "id": "2",
    "refType": "PolicyConditionRef"
  },
  "actions": [
    {
      "action": {
        "id": "3",
        "refType": "PolicyActionRef"
      }
    }
  ],
  "lenientConstraints": false,
  "actionExecutionStrategy": "stopOnFailure",
  "ignoreErrors": false,
  "targetEffect": "deny",
  "condition": {
    "default": true
  },
  "strictTargetEffect": false,
  "priority": 100
}
"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize full") {
          val expected =
              Policy(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  description = "desc",
                  labels = listOf("1"),
                  targetEffect = PolicyTargetEffectEnum.DENY,
                  condition = PolicyConditionDefault(true),
                  strictTargetEffect = false,
                  constraint = PolicyConditionRef(id = "2"),
                  actions = listOf(PolicyActionRelationship(action = PolicyActionRef(id = "3"))),
                  lenientConstraints = false,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.STOP_ON_FAILURE,
                  ignoreErrors = false,
                  priority = 100)
          val given =
              """
{
  "id": "1",
  "version": "1.0.0",
  "description": "desc",
  "labels": [
    "1"
  ],
  "constraint": {
    "id": "2",
    "refType": "PolicyConditionRef"
  },
  "actions": [
    {
      "action": {
        "id": "3",
        "refType": "PolicyActionRef"
      }
    }
  ],
  "lenientConstraints": false,
  "actionExecutionStrategy": "stopOnFailure",
  "ignoreErrors": false,
  "targetEffect": "deny",
  "condition": {
    "default": true
  },
  "strictTargetEffect": false,
  "priority": 100
}
"""

          val actual = json.decodeFromString<IPolicy>(given)
          actual shouldBe expected
        }
      }

      describe("PolicySet") {
        it("should serialize minimal") {
          val given: IPolicy =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies =
                      listOf(PolicyRelationship(policy = PolicyDefault(PolicyResultEnum.PERMIT))))
          val expected =
              """{"policyCombinationLogic": "denyOverrides", "policies":[{"policy":{"default":"permit","id":"${'$'}permit"}}]}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize minimal") {
          val expected: IPolicy =
              PolicySet(
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies =
                      listOf(PolicyRelationship(policy = PolicyDefault(PolicyResultEnum.PERMIT))))
          val given =
              """{"policyCombinationLogic": "denyOverrides", "policies":[{"policy":{"default":"permit","id":"${'$'}permit"}}]}"""
          val actual = json.decodeFromString<IPolicy>(given)
          actual shouldBe expected
        }

        it("should serialize full") {
          val given =
              PolicySet(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  description = "desc",
                  labels = listOf("1"),
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies =
                      listOf(PolicyRelationship(policy = PolicyDefault(PolicyResultEnum.PERMIT))),
                  runChildActions = false,
                  strictUnlessLogic = true,
                  constraint = PolicyConditionRef(id = "2"),
                  actions = listOf(PolicyActionRelationship(action = PolicyActionRef(id = "3"))),
                  lenientConstraints = false,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.STOP_ON_FAILURE,
                  ignoreErrors = false,
                  priority = 100)
          val expected =
              """
{
  "id": "1",
  "version": "1.0.0",
  "description": "desc",
  "labels": [
    "1"
  ],
  "constraint": {
    "id": "2",
    "refType": "PolicyConditionRef"
  },
  "actions": [
    {
      "action": {
        "id": "3",
        "refType": "PolicyActionRef"
      }
    }
  ],
  "lenientConstraints": false,
  "actionExecutionStrategy": "stopOnFailure",
  "ignoreErrors": false,
  "policyCombinationLogic": "denyOverrides",
  "policies": [
    {
      "policy": {
        "default": "permit",
        "id": "${'$'}permit"
      }
    }
  ],
  "runChildActions": false,
  "strictUnlessLogic": true,
  "priority": 100
}
              """
                  .trimIndent()
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize full") {
          val expected =
              PolicySet(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  description = "desc",
                  labels = listOf("1"),
                  policyCombinationLogic = PolicyCombinationLogicEnum.DENY_OVERRIDES,
                  policies =
                      listOf(PolicyRelationship(policy = PolicyDefault(PolicyResultEnum.PERMIT))),
                  runChildActions = false,
                  strictUnlessLogic = true,
                  constraint = PolicyConditionRef(id = "2"),
                  actions = listOf(PolicyActionRelationship(action = PolicyActionRef(id = "3"))),
                  lenientConstraints = false,
                  actionExecutionStrategy = ActionExecutionStrategyEnum.STOP_ON_FAILURE,
                  ignoreErrors = false,
                  priority = 100)
          val given =
              """
{
  "id": "1",
  "version": "1.0.0",
  "description": "desc",
  "labels": [
    "1"
  ],
  "constraint": {
    "id": "2",
    "refType": "PolicyConditionRef"
  },
  "actions": [
    {
      "action": {
        "id": "3",
        "refType": "PolicyActionRef"
      }
    }
  ],
  "lenientConstraints": false,
  "actionExecutionStrategy": "stopOnFailure",
  "ignoreErrors": false,
  "policyCombinationLogic": "denyOverrides",
  "policies": [
    {
      "policy": {
        "default": "permit",
        "id": "${'$'}permit"
      }
    }
  ],
  "runChildActions": false,
  "strictUnlessLogic": true,
  "priority": 100
}
              """
                  .trimIndent()
          val actual = json.decodeFromString<IPolicy>(given)
          actual shouldBe expected
        }
      }

      describe("PolicyConditionDefault") {
        it("should serialize minimal") {
          val given: IPolicy = PolicyDefault(PolicyResultEnum.PERMIT)
          val expected = """{"default":"permit","id":"${'$'}permit"}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize minimal") {
          val expected: IPolicy = PolicyDefault(PolicyResultEnum.DENY)
          val given = """{"default":"deny","id":"${'$'}deny"}"""
          val actual = json.decodeFromString<IPolicy>(given)
          actual shouldBe expected
        }

        it("should serialize full") {
          val given: IPolicy =
              PolicyDefault(
                  default = PolicyResultEnum.PERMIT,
                  constraint = PolicyConditionDefault(true),
                  actions = listOf(PolicyActionRelationship(action = PolicyActionRef("1"))),
                  actionExecutionStrategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS,
                  lenientConstraints = false,
                  priority = 100)
          val expected =
              """{"id":"${'$'}permit","default":"permit","constraint":{"default": true},"actions":[{"action":{"id": "1","refType": "PolicyActionRef"}}],"actionExecutionStrategy":"untilSuccess","lenientConstraints":false,"priority": 100}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize full") {
          val expected: IPolicy =
              PolicyDefault(
                  default = PolicyResultEnum.PERMIT,
                  constraint = PolicyConditionDefault(true),
                  actions = listOf(PolicyActionRelationship(action = PolicyActionRef("1"))),
                  actionExecutionStrategy = ActionExecutionStrategyEnum.UNTIL_SUCCESS,
                  lenientConstraints = false,
                  priority = 100)
          val given =
              """{"id":"${'$'}permit","default":"permit","constraint":{"default": true},"actions":[{"action":{"id": "1","refType": "PolicyActionRef"}}],"actionExecutionStrategy":"untilSuccess","lenientConstraints":false,"priority": 100}"""
          val actual = json.decodeFromString<IPolicy>(given)
          actual shouldBe expected
        }
      }
    })
