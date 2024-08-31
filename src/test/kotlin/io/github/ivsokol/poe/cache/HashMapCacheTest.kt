package io.github.ivsokol.poe.cache

import io.github.ivsokol.poe.DefaultObjectMapper
import io.github.ivsokol.poe.policy.PolicyResultEnum
import io.github.ivsokol.poe.variable.VariableRuntimeTypeEnum
import io.github.ivsokol.poe.variable.VariableValue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private data class Foo(val id: String, val foo: String)

class HashMapCacheTest :
    DescribeSpec({
      val objectMapper = DefaultObjectMapper()
      describe("VariableValue") {
        it("VariableValue") {
          val given = VariableValue(type = VariableRuntimeTypeEnum.STRING, body = "11")
          val given2 = VariableValue(type = VariableRuntimeTypeEnum.STRING, body = "22")
          val given21 = VariableValue(type = VariableRuntimeTypeEnum.STRING, body = "23")
          val cache = HashMapCache()
          cache.put(PolicyStoreCacheEnum.VARIABLE, "1", given)
          cache.putVariable("2", given2)
          cache.putVariable("2", given21)
          val actualFound = cache.get(PolicyStoreCacheEnum.VARIABLE, "1")
          val actual2Found = cache.get(PolicyStoreCacheEnum.VARIABLE, "2")
          val actualNotFound = cache.get(PolicyStoreCacheEnum.VARIABLE, "3")
          val actualNotFound2 = cache.getVariable("4")

          actualFound shouldBe given
          actual2Found shouldBe given21
          cache.hasKey(PolicyStoreCacheEnum.VARIABLE, "1") shouldBe true
          cache.hasKey(PolicyStoreCacheEnum.VARIABLE, "3") shouldBe false
          actualNotFound shouldBe null
          actualNotFound2 shouldBe null
        }

        it("wrong class") {
          val given = Foo(id = "1", foo = "22")
          val cache = HashMapCache()

          shouldThrow<IllegalArgumentException> {
                cache.put(PolicyStoreCacheEnum.VARIABLE, given.id, given)
              }
              .message shouldBe "Provided value is not of type VariableValue"
        }

        it("blank key when put") {
          val given = VariableValue(type = VariableRuntimeTypeEnum.STRING, body = "11")
          val cache = HashMapCache()

          shouldThrow<IllegalArgumentException> {
                cache.put(PolicyStoreCacheEnum.VARIABLE, " ", given)
              }
              .message shouldBe "key cannot be blank"
        }

        it("blank key when get") {
          val given = VariableValue(type = VariableRuntimeTypeEnum.STRING, body = "11")
          val cache = HashMapCache()

          cache.put(PolicyStoreCacheEnum.VARIABLE, "1", given)

          shouldThrow<IllegalArgumentException> { cache.getVariable(" ") }.message shouldBe
              "key cannot be blank"
        }
      }

      describe("Condition") {
        it("Condition") {
          val given = true
          val given2 = true
          val given21 = false
          val given3: Boolean? = null
          val given4 = null
          val cache = HashMapCache()
          cache.put(PolicyStoreCacheEnum.CONDITION, "1", given)
          cache.putCondition("2", given2)
          cache.putCondition("2", given21)
          cache.putCondition("3", given3)
          cache.putCondition("4", given4)
          val actualFound = cache.get(PolicyStoreCacheEnum.CONDITION, "1")
          val actual2Found = cache.get(PolicyStoreCacheEnum.CONDITION, "2")
          val actual3Found = cache.get(PolicyStoreCacheEnum.CONDITION, "3")
          val actual4Found = cache.get(PolicyStoreCacheEnum.CONDITION, "4")
          val actualNotFound = cache.get(PolicyStoreCacheEnum.CONDITION, "10")
          val actualNotFound2 = cache.getCondition("11")

          actualFound shouldBe given
          actual2Found shouldBe given21
          actual3Found shouldBe given3
          actual4Found shouldBe given4
          cache.hasKey(PolicyStoreCacheEnum.CONDITION, "1") shouldBe true
          cache.hasKey(PolicyStoreCacheEnum.CONDITION, "5") shouldBe false
          actualNotFound shouldBe null
          actualNotFound2 shouldBe null
        }

        it("wrong class") {
          val given = Foo(id = "1", foo = "22")
          val cache = HashMapCache()

          shouldThrow<IllegalArgumentException> {
                cache.put(PolicyStoreCacheEnum.CONDITION, given.id, given)
              }
              .message shouldBe "Provided value is not of type Boolean"
        }

        it("blank key when put") {
          val given = true
          val cache = HashMapCache()

          shouldThrow<IllegalArgumentException> {
                cache.put(PolicyStoreCacheEnum.CONDITION, " ", given)
              }
              .message shouldBe "key cannot be blank"
        }

        it("blank key when get") {
          val given = true
          val cache = HashMapCache()

          cache.put(PolicyStoreCacheEnum.CONDITION, "1", given)

          shouldThrow<IllegalArgumentException> { cache.getVariable(" ") }.message shouldBe
              "key cannot be blank"
        }
      }

      describe("Policy") {
        it("Policy") {
          val given = PolicyResultEnum.PERMIT
          val given2 = PolicyResultEnum.PERMIT
          val given21 = PolicyResultEnum.DENY

          val cache = HashMapCache()
          cache.put(PolicyStoreCacheEnum.POLICY, "1", given)
          cache.putPolicy("2", given2)
          cache.putPolicy("2", given21)
          val actualFound = cache.get(PolicyStoreCacheEnum.POLICY, "1")
          val actual2Found = cache.getPolicy("2")
          val actualNotFound = cache.get(PolicyStoreCacheEnum.POLICY, "10")
          val actualNotFound2 = cache.getPolicy("11")

          actualFound shouldBe given
          actual2Found shouldBe given21
          cache.hasKey(PolicyStoreCacheEnum.POLICY, "1") shouldBe true
          cache.hasKey(PolicyStoreCacheEnum.POLICY, "5") shouldBe false
          actualNotFound shouldBe null
          actualNotFound2 shouldBe null
        }

        it("wrong class") {
          val given = Foo(id = "1", foo = "22")
          val cache = HashMapCache()

          shouldThrow<IllegalArgumentException> {
                cache.put(PolicyStoreCacheEnum.POLICY, given.id, given)
              }
              .message shouldBe "Provided value is not of type PolicyResultEnum"
        }

        it("blank key when put") {
          val given = PolicyResultEnum.PERMIT
          val cache = HashMapCache()

          shouldThrow<IllegalArgumentException> {
                cache.put(PolicyStoreCacheEnum.POLICY, " ", given)
              }
              .message shouldBe "key cannot be blank"
        }

        it("blank key when get") {
          val given = PolicyResultEnum.PERMIT
          val cache = HashMapCache()

          cache.put(PolicyStoreCacheEnum.POLICY, "1", given)

          shouldThrow<IllegalArgumentException> { cache.getVariable(" ") }.message shouldBe
              "key cannot be blank"
        }
      }

      describe("keyValueAsJsonNode") {
        it("keyValue") {
          val given = objectMapper.readTree("""{"foo":"bar"}""")
          val given2 = objectMapper.readTree("""{"foo":"bar2"}""")
          val given21 = objectMapper.readTree("""{"foo":"bar21"}""")
          val given3 = null
          val cache = HashMapCache()
          cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, "1", given)
          cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, "2", given2)
          cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, "2", given21)
          cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, "3", given3)
          val actualFound = cache.get(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, "1")
          val actual2Found = cache.getJsonNodeKeyValue("2")
          val actual3Found = cache.getJsonNodeKeyValue("3")
          val actualNotFound = cache.get(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, "10")
          val actualNotFound2 = cache.getJsonNodeKeyValue("11")

          actualFound shouldBe given
          actual2Found shouldBe given21
          actual3Found shouldBe given3
          cache.hasKey(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, "1") shouldBe true
          cache.hasKey(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, "3") shouldBe true
          cache.hasKey(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, "10") shouldBe false
          actualNotFound shouldBe null
          actualNotFound2 shouldBe null
        }

        it("wrong class") {
          val given = Foo(id = "1", foo = "22")
          val cache = HashMapCache()

          shouldThrow<IllegalArgumentException> {
                cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, given.id, given)
              }
              .message shouldBe "Provided value is not of type JsonNode"
        }

        it("blank key when put") {
          val given = objectMapper.readTree("""{"foo":"bar"}""")
          val cache = HashMapCache()

          shouldThrow<IllegalArgumentException> {
                cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, " ", given)
              }
              .message shouldBe "key cannot be blank"
        }

        it("blank key when get") {
          val given = objectMapper.readTree("""{"foo":"bar"}""")
          val cache = HashMapCache()

          cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, "1", given)

          shouldThrow<IllegalArgumentException> { cache.getVariable(" ") }.message shouldBe
              "key cannot be blank"
        }
      }

      describe("keyValueAsString") {
        it("string") {
          val given = "foo"
          val given2 = "bar"
          val given21 = "bar21"
          val given3 = null
          val cache = HashMapCache()
          cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, "1", given)
          cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, "2", given2)
          cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, "2", given21)
          cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, "3", given3)
          val actualFound = cache.get(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, "1")
          val actual2Found = cache.getStringKeyValue("2")
          val actual3Found = cache.getStringKeyValue("3")
          val actualNotFound = cache.get(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, "10")
          val actualNotFound2 = cache.getStringKeyValue("11")

          actualFound shouldBe given
          actual2Found shouldBe given21
          actual3Found shouldBe given3
          cache.hasKey(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, "1") shouldBe true
          cache.hasKey(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, "3") shouldBe true
          cache.hasKey(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, "10") shouldBe false
          actualNotFound shouldBe null
          actualNotFound2 shouldBe null
        }

        it("wrong class") {
          val given = Foo(id = "1", foo = "22")
          val cache = HashMapCache()

          shouldThrow<IllegalArgumentException> {
                cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, given.id, given)
              }
              .message shouldBe "Provided value is not of type String"
        }

        it("blank key when put") {
          val given = """{"foo":"bar"}"""
          val cache = HashMapCache()

          shouldThrow<IllegalArgumentException> {
                cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, " ", given)
              }
              .message shouldBe "key cannot be blank"
        }

        it("blank key when get") {
          val given = """{"foo":"bar"}"""
          val cache = HashMapCache()

          cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, "1", given)

          shouldThrow<IllegalArgumentException> { cache.getVariable(" ") }.message shouldBe
              "key cannot be blank"
        }
      }
    })
