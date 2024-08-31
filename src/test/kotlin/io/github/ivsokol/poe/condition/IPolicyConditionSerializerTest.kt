package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.SemVer
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class IPolicyConditionSerializerTest :
    DescribeSpec({
      val json = Json {
        serializersModule = conditionSerializersModule
        explicitNulls = false
        encodeDefaults = true
      }

      describe("exceptions") {
        it("bad json") {
          val given = """{operation": "GreaterThan"}"""
          shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicyCondition>(given) }
              .message shouldStartWith "Unexpected JSON token at offset"
        }

        it("wrong json") {
          val given = """false"""
          shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicyCondition>(given) }
              .message shouldStartWith
              "Not correct JsonElement for IPolicyCondition DeserializationStrategy"
        }

        it("bad operation") {
          val given = """{"operation": "bad"}"""
          shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicyCondition>(given) }
              .message shouldContain "OperationEnum does not contain element with name 'bad'"
        }

        it("no operation field") {
          val given = """{"left":{"type":"int","value":1},"right":{"type":"int","value":1}}"""
          shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicyCondition>(given) }
              .message shouldBe
              "No corresponding operation field for IPolicyCondition DeserializationStrategy"
        }
      }

      describe("PolicyConditionAtomic") {
        it("should serialize minimal") {
          val given: IPolicyCondition =
              PolicyConditionAtomic(
                  operation = OperationEnum.GREATER_THAN, args = listOf(int(1), int(1)))
          val expected =
              """{"operation": "GreaterThan","args":[{"type":"int","value":1},{"type":"int","value":1}]}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize minimal") {
          val expected: IPolicyCondition =
              PolicyConditionAtomic(
                  operation = OperationEnum.GREATER_THAN, args = listOf(int(1), int(1)))
          val given =
              """{"operation": "GreaterThan","args":[{"type":"int","value":1},{"type":"int","value":1}]}"""
          val actual = json.decodeFromString<IPolicyCondition>(given)
          actual shouldBe expected
        }

        it("should serialize full") {
          val given =
              PolicyConditionAtomic(
                  operation = OperationEnum.GREATER_THAN,
                  id = "1",
                  version = SemVer(1, 0, 0),
                  description = "desc",
                  labels = listOf("1"),
                  args = listOf(string("1"), ref("2")),
                  negateResult = true,
                  stringIgnoreCase = true,
                  fieldsStrictCheck = true,
                  arrayOrderStrictCheck = true)
          val expected =
              """{"id":"1","version":"1.0.0","description":"desc","labels":["1"],"operation": "GreaterThan","args":[{"type":"string","value":"1"},{"id":"2","refType":"PolicyVariableRef"}],"negateResult": true,"stringIgnoreCase": true, "fieldsStrictCheck": true, "arrayOrderStrictCheck": true}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize full") {
          val expected =
              PolicyConditionAtomic(
                  operation = OperationEnum.GREATER_THAN,
                  id = "1",
                  version = SemVer(1, 0, 0),
                  description = "desc",
                  labels = listOf("1"),
                  args = listOf(string("1"), ref("2")),
                  negateResult = true,
                  stringIgnoreCase = true)
          val given =
              """{"id":"1","version":"1.0.0","description":"desc","labels":["1"],"operation": "GreaterThan","args":[{"type":"string","value":"1"},{"id":"2","refType":"PolicyVariableRef"}],"negateResult": true,"stringIgnoreCase": true}"""
          val actual = json.decodeFromString<IPolicyCondition>(given)
          actual shouldBe expected
        }
      }

      describe("PolicyConditionComposite") {
        it("should serialize minimal") {
          val given: IPolicyCondition =
              PolicyConditionComposite(
                  conditionCombinationLogic = ConditionCombinationLogicEnum.NOT,
                  conditions = listOf(PolicyConditionDefault(true)))
          val expected = """{"conditionCombinationLogic":"not","conditions":[{"default":true}]}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize minimal") {
          val expected: IPolicyCondition =
              PolicyConditionComposite(
                  conditionCombinationLogic = ConditionCombinationLogicEnum.NOT,
                  conditions = listOf(PolicyConditionDefault(true)))
          val given = """{"conditionCombinationLogic":"not","conditions":[{"default":true}]}"""
          val actual = json.decodeFromString<IPolicyCondition>(given)
          actual shouldBe expected
        }

        it("should serialize full") {
          val given =
              PolicyConditionComposite(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  description = "desc",
                  labels = listOf("1"),
                  negateResult = true,
                  conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                  conditions = listOf(PolicyConditionDefault(true), PolicyConditionDefault(false)),
                  minimumConditions = 1,
                  optimizeNOfRun = true)
          val expected =
              """{"id":"1","version":"1.0.0","description":"desc","labels":["1"],"negateResult": true,"conditionCombinationLogic":"nOf","conditions":[{"default":true},{"default":false}],"minimumConditions":1,"optimizeNOfRun":true}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize full") {
          val expected =
              PolicyConditionComposite(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  description = "desc",
                  labels = listOf("1"),
                  negateResult = true,
                  conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                  conditions = listOf(PolicyConditionDefault(true), PolicyConditionDefault(false)),
                  minimumConditions = 1,
                  optimizeNOfRun = true)
          val given =
              """{"id":"1","version":"1.0.0","description":"desc","labels":["1"],"negateResult": true,"conditionCombinationLogic":"nOf","conditions":[{"default":true},{"default":false}],"minimumConditions":1,"optimizeNOfRun":true}"""
          val actual = json.decodeFromString<IPolicyCondition>(given)
          actual shouldBe expected
        }
      }

      describe("PolicyConditionDefault") {
        it("should serialize non null") {
          val given: IPolicyCondition = PolicyConditionDefault(true)
          val expected = """{"default":true}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize non null") {
          val expected: IPolicyCondition = PolicyConditionDefault(false)
          val given = """{"default":false}"""
          val actual = json.decodeFromString<IPolicyCondition>(given)
          actual shouldBe expected
        }

        it("should serialize null") {
          val given: IPolicyCondition = PolicyConditionDefault(null)
          val expected = """{"default":null}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize null") {
          val expected: IPolicyCondition = PolicyConditionDefault(null)
          val given = """{"default":null}"""
          val actual = json.decodeFromString<IPolicyCondition>(given)
          actual shouldBe expected
        }
      }
    })
