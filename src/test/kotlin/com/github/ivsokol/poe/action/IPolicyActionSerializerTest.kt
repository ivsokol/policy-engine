package com.github.ivsokol.poe.action

import com.github.ivsokol.poe.SemVer
import com.github.ivsokol.poe.variable.PolicyVariableRef
import com.github.ivsokol.poe.variable.VariableValueFormatEnum
import com.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class IPolicyActionSerializerTest :
    DescribeSpec({
      val json = Json {
        serializersModule = actionSerializersModule
        explicitNulls = false
        encodeDefaults = true
      }

      describe("exceptions") {
        it("bad json") {
          val given = """{foo": "bar"}"""
          shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicyAction>(given) }
              .message shouldStartWith "Unexpected JSON token at offset"
        }

        it("wrong json") {
          val given = """false"""
          shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicyAction>(given) }
              .message shouldStartWith
              "Not correct JsonElement for IPolicyAction DeserializationStrategy"
        }

        it("bad type") {
          val given = """{"type": "bad"}"""
          shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicyAction>(given) }
              .message shouldContain
              "Unknown field type: bad in IPolicyAction DeserializationStrategy"
        }

        it("wrong type variable") {
          val given = """{"type": 1}"""
          shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicyAction>(given) }
              .message shouldContain
              "Field type is not string in IPolicyAction DeserializationStrategy"
        }

        it("no type field") {
          val given = """{"default": "permit"}"""
          shouldThrow<IllegalArgumentException> { json.decodeFromString<IPolicyAction>(given) }
              .message shouldBe
              "No corresponding operation field for IPolicyAction DeserializationStrategy"
        }
      }

      describe("PolicyActionClear") {
        it("should serialize minimal") {
          val given: IPolicyAction = PolicyActionClear(key = "a")
          val expected = """{"type": "clear","key": "a"}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize minimal") {
          val expected: IPolicyAction = PolicyActionClear(key = "a")
          val given = """{"type": "clear","key": "a"}"""
          val actual = json.decodeFromString<IPolicyAction>(given)
          actual shouldBe expected
        }

        it("should serialize full") {
          val given =
              PolicyActionClear(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  description = "desc",
                  labels = listOf("1"),
                  key = "a",
                  failOnMissingKey = true)
          val expected =
              """{"id":"1","version":"1.0.0","description":"desc","labels":["1"],"type": "clear","key": "a","failOnMissingKey": true}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize full") {
          val expected =
              PolicyActionClear(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  description = "desc",
                  labels = listOf("1"),
                  key = "a",
                  failOnMissingKey = true)
          val given =
              """{"id":"1","version":"1.0.0","description":"desc","labels":["1"],"type": "clear","key": "a","failOnMissingKey": true}"""

          val actual = json.decodeFromString<IPolicyAction>(given)
          actual shouldBe expected
        }
      }

      describe("PolicyActionSave") {
        it("should serialize minimal") {
          val given: IPolicyAction = PolicyActionSave(key = "a", value = PolicyVariableRef("pvar1"))
          val expected =
              """{"type": "save","key": "a","value": {"id":"pvar1", "refType": "PolicyVariableRef"}}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize minimal") {
          val expected: IPolicyAction =
              PolicyActionSave(key = "a", value = PolicyVariableRef("pvar1"))
          val given =
              """{"type": "save","key": "a","value": {"id":"pvar1", "refType": "PolicyVariableRef"}}"""
          val actual = json.decodeFromString<IPolicyAction>(given)
          actual shouldBe expected
        }

        it("should serialize full") {
          val given =
              PolicyActionSave(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  description = "desc",
                  labels = listOf("1"),
                  key = "a",
                  value = PolicyVariableRef("pvar1"),
                  failOnMissingKey = true,
                  failOnExistingKey = true,
                  failOnNullSource = true)
          val expected =
              """{"id":"1","version":"1.0.0","description":"desc","labels":["1"],"type": "save","key": "a","value": {"id":"pvar1", "refType": "PolicyVariableRef"},"failOnMissingKey": true, "failOnExistingKey": true, "failOnNullSource": true}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize full") {
          val expected =
              PolicyActionSave(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  description = "desc",
                  labels = listOf("1"),
                  key = "a",
                  value = PolicyVariableRef("pvar1"),
                  failOnMissingKey = true,
                  failOnExistingKey = true,
                  failOnNullSource = true)
          val given =
              """{"id":"1","version":"1.0.0","description":"desc","labels":["1"],"type": "save","key": "a","value": {"id":"pvar1", "refType": "PolicyVariableRef"},"failOnMissingKey": true, "failOnExistingKey": true, "failOnNullSource": true}"""
          val actual = json.decodeFromString<IPolicyAction>(given)
          actual shouldBe expected
        }
      }

      describe("PolicyActionJsonMerge") {
        it("should serialize minimal") {
          val given: IPolicyAction =
              PolicyActionJsonMerge(
                  key = "a",
                  source = PolicyVariableRef("pvar1"),
                  merge = PolicyVariableRef("pvar2"))
          val expected =
              """{"type": "jsonMerge","key": "a","source": {"id":"pvar1", "refType": "PolicyVariableRef"},"merge": {"id":"pvar2", "refType": "PolicyVariableRef"}}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize minimal") {
          val expected: IPolicyAction =
              PolicyActionJsonMerge(
                  key = "a",
                  source = PolicyVariableRef("pvar1"),
                  merge = PolicyVariableRef("pvar2"))
          val given =
              """{"type": "jsonMerge","key": "a","source": {"id":"pvar1", "refType": "PolicyVariableRef"},"merge": {"id":"pvar2", "refType": "PolicyVariableRef"}}"""
          val actual = json.decodeFromString<IPolicyAction>(given)
          actual shouldBe expected
        }

        it("should serialize full") {
          val given =
              PolicyActionJsonMerge(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  description = "desc",
                  labels = listOf("1"),
                  key = "a",
                  source = PolicyVariableRef("pvar1"),
                  merge = PolicyVariableRef("pvar2"),
                  failOnMissingKey = true,
                  failOnExistingKey = true,
                  failOnNullSource = true,
                  failOnNullMerge = true,
                  destinationType = VariableValueTypeEnum.STRING,
                  destinationFormat = VariableValueFormatEnum.BIG_DECIMAL)
          val expected =
              """{"id":"1","version":"1.0.0","description":"desc","labels":["1"],"type": "jsonMerge","key": "a","source": {"id":"pvar1", "refType": "PolicyVariableRef"},"merge": {"id":"pvar2", "refType": "PolicyVariableRef"},"failOnMissingKey": true, "failOnExistingKey": true, "failOnNullSource": true, "failOnNullMerge": true, "destinationType": "string", "destinationFormat": "big-decimal"}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize full") {
          val expected =
              PolicyActionJsonMerge(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  description = "desc",
                  labels = listOf("1"),
                  key = "a",
                  source = PolicyVariableRef("pvar1"),
                  merge = PolicyVariableRef("pvar2"),
                  failOnMissingKey = true,
                  failOnExistingKey = true,
                  failOnNullSource = true,
                  failOnNullMerge = true,
                  destinationType = VariableValueTypeEnum.STRING,
                  destinationFormat = VariableValueFormatEnum.BIG_DECIMAL)
          val given =
              """{"id":"1","version":"1.0.0","description":"desc","labels":["1"],"type": "jsonMerge","key": "a","source": {"id":"pvar1", "refType": "PolicyVariableRef"},"merge": {"id":"pvar2", "refType": "PolicyVariableRef"},"failOnMissingKey": true, "failOnExistingKey": true, "failOnNullSource": true, "failOnNullMerge": true, "destinationType": "string", "destinationFormat": "big-decimal"}"""
          val actual = json.decodeFromString<IPolicyAction>(given)
          actual shouldBe expected
        }
      }

      describe("PolicyActionJsonPatch") {
        it("should serialize minimal") {
          val given: IPolicyAction =
              PolicyActionJsonPatch(
                  key = "a",
                  source = PolicyVariableRef("pvar1"),
                  patch = PolicyVariableRef("pvar2"))
          val expected =
              """{"type": "jsonPatch","key": "a","source": {"id":"pvar1", "refType": "PolicyVariableRef"},"patch": {"id":"pvar2", "refType": "PolicyVariableRef"}}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize minimal") {
          val expected: IPolicyAction =
              PolicyActionJsonPatch(
                  key = "a",
                  source = PolicyVariableRef("pvar1"),
                  patch = PolicyVariableRef("pvar2"))
          val given =
              """{"type": "jsonPatch","key": "a","source": {"id":"pvar1", "refType": "PolicyVariableRef"},"patch": {"id":"pvar2", "refType": "PolicyVariableRef"}}"""
          val actual = json.decodeFromString<IPolicyAction>(given)
          actual shouldBe expected
        }

        it("should serialize full") {
          val given =
              PolicyActionJsonPatch(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  description = "desc",
                  labels = listOf("1"),
                  key = "a",
                  source = PolicyVariableRef("pvar1"),
                  patch = PolicyVariableRef("pvar2"),
                  failOnMissingKey = true,
                  failOnExistingKey = true,
                  failOnNullSource = true,
                  castNullSourceToArray = true)
          val expected =
              """{"id":"1","version":"1.0.0","description":"desc","labels":["1"],"type": "jsonPatch","key": "a","source": {"id":"pvar1", "refType": "PolicyVariableRef"},"patch": {"id":"pvar2", "refType": "PolicyVariableRef"},"failOnMissingKey": true, "failOnExistingKey": true, "failOnNullSource": true, "castNullSourceToArray": true}"""
          val actual = json.encodeToString(given)
          actual shouldEqualJson expected
        }

        it("should deserialize full") {
          val expected =
              PolicyActionJsonPatch(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  description = "desc",
                  labels = listOf("1"),
                  key = "a",
                  source = PolicyVariableRef("pvar1"),
                  patch = PolicyVariableRef("pvar2"),
                  failOnMissingKey = true,
                  failOnExistingKey = true,
                  failOnNullSource = true,
                  castNullSourceToArray = true)
          val given =
              """{"id":"1","version":"1.0.0","description":"desc","labels":["1"],"type": "jsonPatch","key": "a","source": {"id":"pvar1", "refType": "PolicyVariableRef"},"patch": {"id":"pvar2", "refType": "PolicyVariableRef"},"failOnMissingKey": true, "failOnExistingKey": true, "failOnNullSource": true, "castNullSourceToArray": true}"""
          val actual = json.decodeFromString<IPolicyAction>(given)
          actual shouldBe expected
        }
      }
    })
