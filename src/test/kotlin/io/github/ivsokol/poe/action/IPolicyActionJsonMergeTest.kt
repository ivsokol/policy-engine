package io.github.ivsokol.poe.action

import com.fasterxml.jackson.databind.node.TextNode
import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.condition.PolicyConditionDefault
import io.github.ivsokol.poe.event.InMemoryEventTestHandler
import io.github.ivsokol.poe.variable.*
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import java.math.BigDecimal

private data class MergeFoo(val foo: List<String>)

class IPolicyActionJsonMergeTest :
    DescribeSpec({
      val context =
          Context(
              request =
                  mapOf(
                      "1" to "2",
                      "n1" to null,
                      "json" to """{"foo":["bar","baz"],"foo2":"baz"}""",
                      "m1" to """{"foo":["bar"]}""",
                      "m2" to MergeFoo(listOf("bar")),
                  ),
              event = InMemoryEventTestHandler())
      val policyCatalog =
          PolicyCatalog(
              id = "test-catalog",
              policyConditions = listOf(PolicyConditionDefault()),
              policyVariables =
                  listOf(
                      PolicyVariableStatic(
                          id = "pvs1", value = "v2", type = VariableValueTypeEnum.STRING),
                      PolicyVariableDynamic(
                          id = "pvd1", resolvers = listOf(PolicyVariableResolver(key = "n1"))),
                      PolicyVariableDynamic(
                          id = "pvdd1",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON,
                          resolvers =
                              listOf(
                                  PolicyVariableResolver(
                                      key = "orig",
                                      source = ContextStoreEnum.DATA,
                                  ))),
                      PolicyVariableDynamic(
                          id = "pvd2",
                          resolvers = listOf(PolicyVariableResolver(key = "json")),
                          type = VariableValueTypeEnum.OBJECT,
                          format = VariableValueFormatEnum.JSON),
                      PolicyVariableDynamic(
                          id = "pvd3",
                          resolvers = listOf(PolicyVariableResolver(key = "m1")),
                          type = VariableValueTypeEnum.OBJECT,
                          format = VariableValueFormatEnum.JSON),
                  ),
          )

      beforeAny {
        context.dataStore()["a"] = "value"
        context.dataStore()["orig"] = """{"a":["b"]}"""
        context.dataStore()["n"] = null
      }

      afterAny {
        (context.event as InMemoryEventTestHandler).clear()
        context.removeLastFromPath()
        context.cache.clear()
        context.dataStore().clear()
      }

      describe("run") {
        it("should set value") {
          val given =
              PolicyActionJsonMerge(
                  key = "a", source = PolicyVariableRef("pvd2"), merge = PolicyVariableRef("pvd3"))
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["a"].toString() shouldEqualJson """{"foo":["bar"],"foo2":"baz"}"""
        }
        it("should update data value") {
          val given =
              PolicyActionJsonMerge(
                  key = "orig",
                  source = PolicyVariableRef("pvdd1"),
                  merge = PolicyVariableRef("pvd3"),
                  destinationType = VariableValueTypeEnum.STRING)
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["orig"] shouldBe """{"a":["b"],"foo":["bar"]}"""
        }
        it("should set non existing value") {
          val given =
              PolicyActionJsonMerge(
                  key = "b", source = PolicyVariableRef("pvd1"), merge = PolicyVariableRef("pvd3"))
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["a"] shouldBe "value"
          context.dataStore()["b"].toString() shouldEqualJson """{"foo":["bar"]}"""
        }
        it("should fail on non existing key") {
          val given =
              PolicyActionJsonMerge(
                  key = "c",
                  source = PolicyVariableRef("pvd1"),
                  merge = PolicyVariableRef("pvd3"),
                  failOnMissingKey = true)
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
        }
        it("should fail on existing key") {
          val given =
              PolicyActionJsonMerge(
                  key = "a",
                  source = PolicyVariableRef("pvd1"),
                  merge = PolicyVariableRef("pvd3"),
                  failOnExistingKey = true)
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
        }
        it("should fail on null source") {
          val given =
              PolicyActionJsonMerge(
                  key = "a",
                  source = PolicyVariableRef("pvd1"),
                  merge = PolicyVariableRef("pvd3"),
                  failOnNullSource = true)
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
        }
        it("should not fail on null source") {
          val given =
              PolicyActionJsonMerge(
                  key = "a", source = PolicyVariableRef("pvd1"), merge = PolicyVariableRef("pvd3"))
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["a"].toString() shouldEqualJson """{"foo":["bar"]}"""
        }
        it("should fail on null source when not resolved") {
          val given =
              PolicyActionJsonMerge(
                  key = "a",
                  source = PolicyVariableRef("pvd999"),
                  merge = PolicyVariableRef("pvd3"),
                  failOnNullSource = true)
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
        }
        it("should not fail on null source when not resolved") {
          val given =
              PolicyActionJsonMerge(
                  key = "a",
                  source = PolicyVariableRef("pvd999"),
                  merge = PolicyVariableRef("pvd3"))
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["a"].toString() shouldEqualJson """{"foo":["bar"]}"""
        }

        it("should not fail when source not json") {
          val given =
              PolicyActionJsonMerge(
                  key = "a",
                  source = PolicyVariableStatic(value = "a"),
                  merge = PolicyVariableRef("pvd3"),
              )
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["a"].toString() shouldEqualJson """{"foo":["bar"]}"""
        }
        it("should not fail when merge not json") {
          val given =
              PolicyActionJsonMerge(
                  key = "a",
                  merge = PolicyVariableStatic(value = "true"),
                  source = PolicyVariableRef("pvd3"))
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          (context.dataStore()["a"] as TextNode).asText() shouldBe "true"
        }
        it("should cast to string") {
          val given =
              PolicyActionJsonMerge(
                  key = "a",
                  merge = PolicyVariableStatic(value = "true"),
                  source = PolicyVariableRef("pvd3"),
                  destinationType = VariableValueTypeEnum.STRING)
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["a"] shouldBe "true"
        }
        it("should cast to BigDecimal") {
          val given =
              PolicyActionJsonMerge(
                  key = "a",
                  merge = PolicyVariableStatic(value = "3.22"),
                  source = PolicyVariableRef("pvd3"),
                  destinationType = VariableValueTypeEnum.NUMBER,
                  destinationFormat = VariableValueFormatEnum.BIG_DECIMAL)
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["a"] shouldBe BigDecimal("3.22")
        }
        it("should fail when source cast to json not possible") {
          val given =
              PolicyActionJsonMerge(
                  key = "a",
                  source =
                      PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolver(key = "m2"))),
                  merge = PolicyVariableRef("pvd3"),
              )
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
          context.dataStore()["a"] shouldBe "value"
        }
        it("should fail when merge cast to json not possible") {
          val given =
              PolicyActionJsonMerge(
                  key = "a",
                  merge =
                      PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolver(key = "m2"))),
                  source = PolicyVariableRef("pvd3"),
              )
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
          context.dataStore()["a"] shouldBe "value"
        }
        it("should fail when merge not found") {
          val given =
              PolicyActionJsonMerge(
                  key = "a",
                  merge = PolicyVariableRef("pvd999"),
                  source = PolicyVariableRef("pvd3"),
                  failOnNullMerge = true)
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
          context.dataStore()["a"] shouldBe "value"
        }
      }

      describe("childRefs") {
        it("should return null") {
          val actual =
              PolicyActionJsonMerge(
                  key = "a",
                  source =
                      PolicyVariableStatic(
                          id = "pvs1", value = "v2", type = VariableValueTypeEnum.STRING),
                  merge =
                      PolicyVariableStatic(
                          id = "pvs1", value = "v2", type = VariableValueTypeEnum.STRING))
          actual.childRefs() shouldBe null
        }
        it("should return variable") {
          val actual =
              PolicyActionJsonMerge(
                  key = "a", source = PolicyVariableRef("pvd2"), merge = PolicyVariableRef("pvd3"))
          actual.childRefs() shouldNotBe null
          actual.childRefs()!!.shouldHaveSize(2)
        }
      }

      describe("identity") {
        it("should return empty") {
          val actual =
              PolicyActionJsonMerge(
                  key = "a", source = PolicyVariableRef("pvd2"), merge = PolicyVariableRef("pvd3"))
          actual.identity() shouldBe ""
        }
        it("should throw when bad id") {
          shouldThrow<IllegalArgumentException> {
                PolicyActionJsonMerge(
                    id = "()",
                    key = "a",
                    source = PolicyVariableRef("pvd2"),
                    merge = PolicyVariableRef("pvd3"))
              }
              .message shouldBe
              "Id allowed characters are letters, numbers, square brackets, underscore, dot and hyphen"
        }
        it("should return id") {
          val actual =
              PolicyActionJsonMerge(
                  id = "1",
                  key = "a",
                  source = PolicyVariableRef("pvd2"),
                  merge = PolicyVariableRef("pvd3"))
          actual.identity() shouldBe "1"
        }
        it("should return idVer") {
          val actual =
              PolicyActionJsonMerge(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  key = "a",
                  source = PolicyVariableRef("pvd2"),
                  merge = PolicyVariableRef("pvd3"))
          actual.identity() shouldBe "1:1.0.0"
        }
      }

      describe("events") {
        it("merge") {
          val given =
              PolicyActionJsonMerge(
                  id = "pajm1",
                  key = "a",
                  source =
                      PolicyVariableStatic(
                          value = """{"foo":["bar"]}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  merge =
                      PolicyVariableStatic(
                          value = """{"foo2":"baz"}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON))
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 2
          actualEvents[2].contextId shouldNotBe null
          actualEvents[2].success shouldBe true
          actualEvents[2].entityId shouldBe "pajm1"
          actualEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_MERGE
          actualEvents[2].reason shouldBe null
          actualEvents[2].message shouldBe """{"foo":["bar"],"foo2":"baz"}"""
          actualEvents[2].fromCache shouldBe false
        }
        it("missing key") {
          val given =
              PolicyActionJsonMerge(
                  id = "pajm1",
                  key = "b",
                  source =
                      PolicyVariableStatic(
                          value = """{"foo":["bar"]}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  merge =
                      PolicyVariableStatic(
                          value = """{"foo2":"baz"}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  failOnMissingKey = true)
          given.run(context, policyCatalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pajm1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_MERGE
          actualEvents[0].reason shouldContain "Missing key: b"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false
        }
        it("existing key") {
          val given =
              PolicyActionJsonMerge(
                  id = "pajm1",
                  key = "a",
                  source =
                      PolicyVariableStatic(
                          value = """{"foo":["bar"]}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  merge =
                      PolicyVariableStatic(
                          value = """{"foo2":"baz"}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  failOnExistingKey = true)
          given.run(context, policyCatalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pajm1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_MERGE
          actualEvents[0].reason shouldContain "Existing key: a"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false
        }
        it("null source") {
          val given =
              PolicyActionJsonMerge(
                  id = "pajm1",
                  key = "a",
                  source = PolicyVariableRef("pvd999"),
                  merge =
                      PolicyVariableStatic(
                          value = """{"foo2":"baz"}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  failOnNullSource = true)
          given.run(context, policyCatalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 2
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pajm1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_MERGE
          actualEvents[0].reason shouldContain "not found in catalog"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false

          actualEvents[1].contextId shouldNotBe null
          actualEvents[1].success shouldBe false
          actualEvents[1].entityId shouldBe "pajm1"
          actualEvents[1].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_MERGE
          actualEvents[1].reason shouldContain "Null source value"
          actualEvents[1].message shouldBe null
          actualEvents[1].fromCache shouldBe false
        }
        it("source cast fail") {
          val given =
              PolicyActionJsonMerge(
                  id = "pajm1",
                  key = "a",
                  source =
                      PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolver(key = "m2"))),
                  merge = PolicyVariableRef("pvd3"),
              )
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents[0].entity shouldBe PolicyEntityEnum.VALUE_RESOLVER
          actualEvents[1].entity shouldBe PolicyEntityEnum.VARIABLE_DYNAMIC

          actualEvents[2].contextId shouldNotBe null
          actualEvents[2].success shouldBe false
          actualEvents[2].entityId shouldBe "pajm1"
          actualEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_MERGE
          actualEvents[2].reason shouldContain "Cannot cast"
          actualEvents[2].message shouldBe null
          actualEvents[2].fromCache shouldBe false
        }
        it("null merge") {
          val given =
              PolicyActionJsonMerge(
                  id = "pajm1",
                  key = "a",
                  merge = PolicyVariableRef("pvd999"),
                  source =
                      PolicyVariableStatic(
                          value = """{"foo2":"baz"}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  failOnNullMerge = true)
          given.run(context, policyCatalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents[0].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          actualEvents[1].contextId shouldNotBe null
          actualEvents[1].success shouldBe false
          actualEvents[1].entityId shouldBe "pajm1"
          actualEvents[1].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_MERGE
          actualEvents[1].reason shouldContain "not found in catalog"
          actualEvents[1].message shouldBe null
          actualEvents[1].fromCache shouldBe false

          actualEvents[2].contextId shouldNotBe null
          actualEvents[2].success shouldBe false
          actualEvents[2].entityId shouldBe "pajm1"
          actualEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_MERGE
          actualEvents[2].reason shouldContain "Null merge value"
          actualEvents[2].message shouldBe null
          actualEvents[2].fromCache shouldBe false
        }
        it("merge cast fail") {
          val given =
              PolicyActionJsonMerge(
                  id = "pajm1",
                  key = "a",
                  merge =
                      PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolver(key = "m2"))),
                  source = PolicyVariableRef("pvd3"),
              )
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 5
          actualEvents[0].entity shouldBe PolicyEntityEnum.VALUE_RESOLVER
          actualEvents[1].entity shouldBe PolicyEntityEnum.VARIABLE_DYNAMIC
          actualEvents[2].entity shouldBe PolicyEntityEnum.VALUE_RESOLVER
          actualEvents[3].entity shouldBe PolicyEntityEnum.VARIABLE_DYNAMIC

          actualEvents[4].contextId shouldNotBe null
          actualEvents[4].success shouldBe false
          actualEvents[4].entityId shouldBe "pajm1"
          actualEvents[4].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_MERGE
          actualEvents[4].reason shouldContain "Cannot cast"
          actualEvents[4].message shouldBe null
          actualEvents[4].fromCache shouldBe false
        }
        it("exception") {
          val given =
              PolicyActionJsonMerge(
                  id = "pajm1",
                  key = "a",
                  source =
                      PolicyVariableStatic(
                          value = """{"foo":["bar"]}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  merge =
                      PolicyVariableStatic(
                          value = """{"foo2":"baz"}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON))
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 2
          actualEvents[2].contextId shouldNotBe null
          actualEvents[2].success shouldBe true
          actualEvents[2].entityId shouldBe "pajm1"
          actualEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_MERGE
          actualEvents[2].reason shouldBe null
          actualEvents[2].message shouldBe """{"foo":["bar"],"foo2":"baz"}"""
          actualEvents[2].fromCache shouldBe false
        }
      }

      describe("naming") {
        it("not named") {
          val given =
              PolicyActionJsonMerge(
                  key = "a",
                  source =
                      PolicyVariableStatic(
                          value = """{"foo":["bar"]}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  merge =
                      PolicyVariableStatic(
                          value = """{"foo2":"baz"}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON))
          given.run(context, policyCatalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents[0].entityId shouldBe "source"
          actualEvents[1].entityId shouldBe "merge"
          actualEvents[2].entityId shouldBe ""
        }
        it("child") {
          context.addToPath("actions")
          context.addToPath("0")
          val given =
              PolicyActionJsonMerge(
                  key = "a",
                  source =
                      PolicyVariableStatic(
                          value = """{"foo":["bar"]}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  merge =
                      PolicyVariableStatic(
                          value = """{"foo2":"baz"}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON))
          given.run(context, policyCatalog)
          context.removeLastFromPath()
          context.removeLastFromPath()
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents[0].entityId shouldBe "actions/0/source"
          actualEvents[1].entityId shouldBe "actions/0/merge"
          actualEvents[2].entityId shouldBe "actions/0"
        }
        it("named child") {
          context.addToPath("actions")
          context.addToPath("0")
          val given =
              PolicyActionJsonMerge(
                  id = "pajm1",
                  key = "a",
                  source =
                      PolicyVariableStatic(
                          value = """{"foo":["bar"]}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  merge =
                      PolicyVariableStatic(
                          value = """{"foo2":"baz"}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON))
          given.run(context, policyCatalog)
          context.removeLastFromPath()
          context.removeLastFromPath()
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents[0].entityId shouldBe "actions/0(pajm1)/source"
          actualEvents[1].entityId shouldBe "actions/0(pajm1)/merge"
          actualEvents[2].entityId shouldBe "actions/0(pajm1)"
        }
      }
    })
