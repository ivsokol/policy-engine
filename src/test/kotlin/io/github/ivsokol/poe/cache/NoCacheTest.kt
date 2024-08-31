package io.github.ivsokol.poe.cache

import io.github.ivsokol.poe.DefaultObjectMapper
import io.github.ivsokol.poe.policy.PolicyResultEnum
import io.github.ivsokol.poe.variable.VariableRuntimeTypeEnum
import io.github.ivsokol.poe.variable.VariableValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class NoCacheTest :
    DescribeSpec({
      val objectMapper = DefaultObjectMapper()
      describe("VariableValue") {
        it("VariableValue") {
          val given = VariableValue(type = VariableRuntimeTypeEnum.STRING, body = "11")
          val cache = NoCache()
          cache.put(PolicyStoreCacheEnum.VARIABLE, "1", given)
          cache.putVariable("2", given)
          cache.get(PolicyStoreCacheEnum.VARIABLE, "1") shouldBe null
          cache.getVariable("2") shouldBe null
          cache.hasKey(PolicyStoreCacheEnum.VARIABLE, "2") shouldBe false
          cache.clear()
        }
      }

      describe("Condition") {
        it("Condition") {
          val given = true
          val cache = NoCache()
          cache.put(PolicyStoreCacheEnum.CONDITION, "1", given)
          cache.putCondition("2", given)
          cache.get(PolicyStoreCacheEnum.CONDITION, "1") shouldBe null
          cache.getCondition("2") shouldBe null
        }
      }

      describe("Policy") {
        it("Policy") {
          val given = PolicyResultEnum.PERMIT
          val cache = NoCache()
          cache.put(PolicyStoreCacheEnum.POLICY, "1", given)
          cache.putPolicy("2", given)
          cache.get(PolicyStoreCacheEnum.POLICY, "1") shouldBe null
          cache.getPolicy("2") shouldBe null
        }
      }

      describe("keyValueAsJsonNode") {
        it("keyValue") {
          val given = objectMapper.readTree("""{"foo":"bar"}""")
          val cache = NoCache()
          cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, "1", given)
          cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, "2", given)

          cache.get(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, "1") shouldBe null
          cache.getJsonNodeKeyValue("2") shouldBe null
        }
      }

      describe("keyValueAsString") {
        it("string") {
          val given = "foo"
          val cache = NoCache()
          cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, "1", given)
          cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, "2", given)

          cache.get(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, "1") shouldBe null
          cache.getStringKeyValue("2") shouldBe null
        }
      }
    })
