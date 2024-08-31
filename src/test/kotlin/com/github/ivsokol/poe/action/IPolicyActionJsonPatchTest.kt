package com.github.ivsokol.poe.action

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.PolicyEntityEnum
import com.github.ivsokol.poe.SemVer
import com.github.ivsokol.poe.catalog.PolicyCatalog
import com.github.ivsokol.poe.condition.PolicyConditionDefault
import com.github.ivsokol.poe.event.InMemoryEventTestHandler
import com.github.ivsokol.poe.variable.*
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

private data class PatchFoo(val foo: List<String>)

class IPolicyActionJsonPatchTest :
    DescribeSpec({
      val context =
          Context(
              request =
                  mapOf(
                      "1" to "2",
                      "n1" to null,
                      "json" to """{"foo":["bar","baz"],"foo2":"baz"}""",
                      "p1" to """[{"op":"replace","path":"/foo","value":["bar"]}]""",
                      "p2" to PatchFoo(listOf("bar")),
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
                          id = "pvd2",
                          resolvers = listOf(PolicyVariableResolver(key = "json")),
                          type = VariableValueTypeEnum.OBJECT,
                          format = VariableValueFormatEnum.JSON),
                      PolicyVariableDynamic(
                          id = "pvd3",
                          resolvers = listOf(PolicyVariableResolver(key = "p1")),
                          type = VariableValueTypeEnum.ARRAY,
                          format = VariableValueFormatEnum.JSON),
                  ),
          )

      beforeAny {
        context.dataStore()["a"] = "value"
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
              PolicyActionJsonPatch(
                  key = "a", source = PolicyVariableRef("pvd2"), patch = PolicyVariableRef("pvd3"))
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["a"].toString() shouldEqualJson """{"foo":["bar"],"foo2":"baz"}"""
        }
        it("should set non existing value") {
          val given =
              PolicyActionJsonPatch(
                  key = "b",
                  source = PolicyVariableRef("pvd1"),
                  patch =
                      PolicyVariableStatic(
                          value = """[{"op":"add","path":"/foo","value":["bar"]}]""",
                          type = VariableValueTypeEnum.ARRAY,
                          format = VariableValueFormatEnum.JSON))
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["a"] shouldBe "value"
          context.dataStore()["b"].toString() shouldEqualJson """{"foo":["bar"]}"""
        }
        it("should set non existing array") {
          val given =
              PolicyActionJsonPatch(
                  key = "b",
                  source = PolicyVariableRef("pvd1"),
                  patch =
                      PolicyVariableStatic(
                          value = """[{"op":"add","path":"/-","value":"bar"}]""",
                          type = VariableValueTypeEnum.ARRAY,
                          format = VariableValueFormatEnum.JSON),
                  castNullSourceToArray = true)
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["a"] shouldBe "value"
          context.dataStore()["b"].toString() shouldEqualJson """["bar"]"""
        }
        it("should fail on non existing key") {
          val given =
              PolicyActionJsonPatch(
                  key = "c",
                  source = PolicyVariableRef("pvd1"),
                  patch = PolicyVariableRef("pvd3"),
                  failOnMissingKey = true)
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
        }
        it("should fail on existing key") {
          val given =
              PolicyActionJsonPatch(
                  key = "a",
                  source = PolicyVariableRef("pvd1"),
                  patch = PolicyVariableRef("pvd3"),
                  failOnExistingKey = true)
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
        }
        it("should fail on null source") {
          val given =
              PolicyActionJsonPatch(
                  key = "a",
                  source = PolicyVariableRef("pvd1"),
                  patch = PolicyVariableRef("pvd3"),
                  failOnNullSource = true)
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
        }
        it("should not fail on null source") {
          val given =
              PolicyActionJsonPatch(
                  key = "a",
                  source = PolicyVariableRef("pvd1"),
                  patch =
                      PolicyVariableStatic(
                          value = """[{"op":"add","path":"/foo","value":["bar"]}]""",
                          type = VariableValueTypeEnum.ARRAY,
                          format = VariableValueFormatEnum.JSON))
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["a"].toString() shouldEqualJson """{"foo":["bar"]}"""
        }
        it("should fail on null source when not resolved") {
          val given =
              PolicyActionJsonPatch(
                  key = "a",
                  source = PolicyVariableRef("pvd999"),
                  patch = PolicyVariableRef("pvd3"),
                  failOnNullSource = true)
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
        }
        it("should not fail on null source when not resolved") {
          val given =
              PolicyActionJsonPatch(
                  key = "a",
                  source = PolicyVariableRef("pvd999"),
                  patch =
                      PolicyVariableStatic(
                          value = """[{"op":"add","path":"/foo","value":["bar"]}]""",
                          type = VariableValueTypeEnum.ARRAY,
                          format = VariableValueFormatEnum.JSON))
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["a"].toString() shouldEqualJson """{"foo":["bar"]}"""
        }

        it("should fail when source not json") {
          val given =
              PolicyActionJsonPatch(
                  key = "a",
                  source = PolicyVariableStatic(value = "a"),
                  patch =
                      PolicyVariableStatic(
                          value = """[{"op":"add","path":"/foo","value":["bar"]}]""",
                          type = VariableValueTypeEnum.ARRAY,
                          format = VariableValueFormatEnum.JSON),
              )
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
          context.dataStore()["a"] shouldBe "value"
        }
        it("should fail when patch not json") {
          val given =
              PolicyActionJsonPatch(
                  key = "a",
                  patch = PolicyVariableStatic(value = "true"),
                  source = PolicyVariableRef("pvd3"))
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
          context.dataStore()["a"] shouldBe "value"
        }

        it("should fail when source cast to json not possible") {
          val given =
              PolicyActionJsonPatch(
                  key = "a",
                  source =
                      PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolver(key = "p2"))),
                  patch = PolicyVariableRef("pvd3"),
              )
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
          context.dataStore()["a"] shouldBe "value"
        }
        it("should fail when patch cast to json not possible") {
          val given =
              PolicyActionJsonPatch(
                  key = "a",
                  patch =
                      PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolver(key = "p2"))),
                  source = PolicyVariableRef("pvd3"),
              )
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
          context.dataStore()["a"] shouldBe "value"
        }
        it("should fail when patch not found") {
          val given =
              PolicyActionJsonPatch(
                  key = "a",
                  patch = PolicyVariableRef("pvd999"),
                  source = PolicyVariableRef("pvd3"),
              )
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
          context.dataStore()["a"] shouldBe "value"
        }
      }

      describe("childRefs") {
        it("should return null") {
          val actual =
              PolicyActionJsonPatch(
                  key = "a",
                  source =
                      PolicyVariableStatic(
                          id = "pvs1", value = "v2", type = VariableValueTypeEnum.STRING),
                  patch =
                      PolicyVariableStatic(
                          id = "pvs1", value = "v2", type = VariableValueTypeEnum.STRING))
          actual.childRefs() shouldBe null
        }
        it("should return variable") {
          val actual =
              PolicyActionJsonPatch(
                  key = "a", source = PolicyVariableRef("pvd2"), patch = PolicyVariableRef("pvd3"))
          actual.childRefs() shouldNotBe null
          actual.childRefs()!!.shouldHaveSize(2)
        }
      }

      describe("identity") {
        it("should return empty") {
          val actual =
              PolicyActionJsonPatch(
                  key = "a", source = PolicyVariableRef("pvd2"), patch = PolicyVariableRef("pvd3"))
          actual.identity() shouldBe ""
        }
        it("should throw when bad id") {
          shouldThrow<IllegalArgumentException> {
                PolicyActionJsonPatch(
                    id = "()",
                    key = "a",
                    source = PolicyVariableRef("pvd2"),
                    patch = PolicyVariableRef("pvd3"))
              }
              .message shouldBe
              "Id allowed characters are letters, numbers, square brackets, underscore, dot and hyphen"
        }
        it("should throw when version without id") {
          shouldThrow<IllegalArgumentException> {
                PolicyActionJsonPatch(
                    version = SemVer(1, 0, 0),
                    key = "a",
                    source = PolicyVariableRef("pvd2"),
                    patch = PolicyVariableRef("pvd3"))
              }
              .message shouldBe "Version cannot be populated without id"
        }
        it("should return id") {
          val actual =
              PolicyActionJsonPatch(
                  id = "1",
                  key = "a",
                  source = PolicyVariableRef("pvd2"),
                  patch = PolicyVariableRef("pvd3"))
          actual.identity() shouldBe "1"
        }
        it("should return idVer") {
          val actual =
              PolicyActionJsonPatch(
                  id = "1",
                  version = SemVer(1, 0, 0),
                  key = "a",
                  source = PolicyVariableRef("pvd2"),
                  patch = PolicyVariableRef("pvd3"))
          actual.identity() shouldBe "1:1.0.0"
        }
      }

      describe("events") {
        it("patch") {
          val given =
              PolicyActionJsonPatch(
                  id = "pajp1",
                  key = "a",
                  source =
                      PolicyVariableStatic(
                          value = """{"foo":["bar"]}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  patch =
                      PolicyVariableStatic(
                          value = """[{"op":"add","path":"/foo/-","value":"baz"}]""",
                          type = VariableValueTypeEnum.ARRAY,
                          format = VariableValueFormatEnum.JSON))
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 2
          actualEvents[2].contextId shouldNotBe null
          actualEvents[2].success shouldBe true
          actualEvents[2].entityId shouldBe "pajp1"
          actualEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_PATCH
          actualEvents[2].reason shouldBe null
          actualEvents[2].message shouldBe """{"foo":["bar","baz"]}"""
          actualEvents[2].fromCache shouldBe false
        }
        it("missing key") {
          val given =
              PolicyActionJsonPatch(
                  id = "pajp1",
                  key = "b",
                  source =
                      PolicyVariableStatic(
                          value = """{"foo":["bar"]}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  patch =
                      PolicyVariableStatic(
                          value = """[{"op":"add","path":"/foo/-","value":"baz"}]""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  failOnMissingKey = true)
          given.run(context, policyCatalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pajp1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_PATCH
          actualEvents[0].reason shouldContain "Missing key: b"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false
        }
        it("existing key") {
          val given =
              PolicyActionJsonPatch(
                  id = "pajp1",
                  key = "a",
                  source =
                      PolicyVariableStatic(
                          value = """{"foo":["bar"]}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  patch =
                      PolicyVariableStatic(
                          value = """[{"op":"add","path":"/foo/-","value":"baz"}]""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  failOnExistingKey = true)
          given.run(context, policyCatalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pajp1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_PATCH
          actualEvents[0].reason shouldContain "Existing key: a"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false
        }
        it("null source") {
          val given =
              PolicyActionJsonPatch(
                  id = "pajp1",
                  key = "a",
                  source = PolicyVariableRef("pvd999"),
                  patch =
                      PolicyVariableStatic(
                          value = """[{"op":"add","path":"/foo/-","value":"baz"}]""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  failOnNullSource = true)
          given.run(context, policyCatalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 2
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pajp1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_PATCH
          actualEvents[0].reason shouldContain "not found in catalog"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false

          actualEvents[1].contextId shouldNotBe null
          actualEvents[1].success shouldBe false
          actualEvents[1].entityId shouldBe "pajp1"
          actualEvents[1].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_PATCH
          actualEvents[1].reason shouldContain "Null source value"
          actualEvents[1].message shouldBe null
          actualEvents[1].fromCache shouldBe false
        }
        it("source cast fail") {
          val given =
              PolicyActionJsonPatch(
                  id = "pajp1",
                  key = "a",
                  source =
                      PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolver(key = "p2"))),
                  patch = PolicyVariableRef("pvd3"),
              )
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents[0].entity shouldBe PolicyEntityEnum.VALUE_RESOLVER
          actualEvents[1].entity shouldBe PolicyEntityEnum.VARIABLE_DYNAMIC

          actualEvents[2].contextId shouldNotBe null
          actualEvents[2].success shouldBe false
          actualEvents[2].entityId shouldBe "pajp1"
          actualEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_PATCH
          actualEvents[2].reason shouldContain "Cannot cast"
          actualEvents[2].message shouldBe null
          actualEvents[2].fromCache shouldBe false
        }
        it("null patch") {
          val given =
              PolicyActionJsonPatch(
                  id = "pajp1",
                  key = "a",
                  patch = PolicyVariableRef("pvd999"),
                  source =
                      PolicyVariableStatic(
                          value = """{"foo2":"baz"}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
              )
          given.run(context, policyCatalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents[0].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          actualEvents[1].contextId shouldNotBe null
          actualEvents[1].success shouldBe false
          actualEvents[1].entityId shouldBe "pajp1"
          actualEvents[1].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_PATCH
          actualEvents[1].reason shouldContain "not found in catalog"
          actualEvents[1].message shouldBe null
          actualEvents[1].fromCache shouldBe false

          actualEvents[2].contextId shouldNotBe null
          actualEvents[2].success shouldBe false
          actualEvents[2].entityId shouldBe "pajp1"
          actualEvents[2].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_PATCH
          actualEvents[2].reason shouldContain "Cannot cast NULL to ArrayNode"
          actualEvents[2].message shouldBe null
          actualEvents[2].fromCache shouldBe false
        }
        it("patch cast fail") {
          val given =
              PolicyActionJsonPatch(
                  id = "pajp1",
                  key = "a",
                  patch =
                      PolicyVariableStatic(
                          value = """{"op":"add","path":"/foo/-","value":"baz"}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  source = PolicyVariableRef("pvd3"),
              )
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 4
          actualEvents[0].entity shouldBe PolicyEntityEnum.VALUE_RESOLVER
          actualEvents[1].entity shouldBe PolicyEntityEnum.VARIABLE_DYNAMIC
          actualEvents[2].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC

          actualEvents[3].contextId shouldNotBe null
          actualEvents[3].success shouldBe false
          actualEvents[3].entityId shouldBe "pajp1"
          actualEvents[3].entity shouldBe PolicyEntityEnum.POLICY_ACTION_JSON_PATCH
          actualEvents[3].reason shouldContain "Cannot cast"
          actualEvents[3].message shouldBe null
          actualEvents[3].fromCache shouldBe false
        }
      }

      describe("naming") {
        it("not named") {
          val given =
              PolicyActionJsonPatch(
                  key = "a",
                  source =
                      PolicyVariableStatic(
                          value = """{"foo":["bar"]}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  patch =
                      PolicyVariableStatic(
                          value = """[{"op":"add","path":"/foo/-","value":"baz"}]""",
                          type = VariableValueTypeEnum.ARRAY,
                          format = VariableValueFormatEnum.JSON))
          given.run(context, policyCatalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents[0].entityId shouldBe "source"
          actualEvents[1].entityId shouldBe "patch"
          actualEvents[2].entityId shouldBe ""
        }
        it("child") {
          context.addToPath("actions")
          context.addToPath("0")
          val given =
              PolicyActionJsonPatch(
                  key = "a",
                  source =
                      PolicyVariableStatic(
                          value = """{"foo":["bar"]}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  patch =
                      PolicyVariableStatic(
                          value = """[{"op":"add","path":"/foo/-","value":"baz"}]""",
                          type = VariableValueTypeEnum.ARRAY,
                          format = VariableValueFormatEnum.JSON))
          given.run(context, policyCatalog)
          context.removeLastFromPath()
          context.removeLastFromPath()
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents[0].entityId shouldBe "actions/0/source"
          actualEvents[1].entityId shouldBe "actions/0/patch"
          actualEvents[2].entityId shouldBe "actions/0"
        }
        it("named child") {
          context.addToPath("actions")
          context.addToPath("0")
          val given =
              PolicyActionJsonPatch(
                  id = "pajp1",
                  key = "a",
                  source =
                      PolicyVariableStatic(
                          value = """{"foo":["bar"]}""",
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.JSON),
                  patch =
                      PolicyVariableStatic(
                          value = """[{"op":"add","path":"/foo/-","value":"baz"}]""",
                          type = VariableValueTypeEnum.ARRAY,
                          format = VariableValueFormatEnum.JSON))
          given.run(context, policyCatalog)
          context.removeLastFromPath()
          context.removeLastFromPath()
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents[0].entityId shouldBe "actions/0(pajp1)/source"
          actualEvents[1].entityId shouldBe "actions/0(pajp1)/patch"
          actualEvents[2].entityId shouldBe "actions/0(pajp1)"
        }
      }
    })
