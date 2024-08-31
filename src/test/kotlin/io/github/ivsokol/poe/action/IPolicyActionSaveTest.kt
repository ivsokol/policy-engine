package io.github.ivsokol.poe.action

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.condition.PolicyConditionDefault
import io.github.ivsokol.poe.event.InMemoryEventTestHandler
import io.github.ivsokol.poe.variable.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

class IPolicyActionSaveTest :
    DescribeSpec({
      val context =
          Context(request = mapOf("1" to "2", "n1" to null), event = InMemoryEventTestHandler())
      val policyCatalog =
          PolicyCatalog(
              id = "test-catalog",
              policyConditions = listOf(PolicyConditionDefault()),
              policyVariables =
                  listOf(
                      PolicyVariableStatic(
                          id = "pvs1", value = "v2", type = VariableValueTypeEnum.STRING),
                      PolicyVariableDynamic(
                          id = "pvs2", resolvers = listOf(PolicyVariableResolver(key = "n1")))),
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
          val given = PolicyActionSave(key = "a", value = PolicyVariableRef("pvs1"))
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["a"] shouldBe "v2"
        }
        it("should set non existing value") {
          val given = PolicyActionSave(key = "b", value = PolicyVariableRef("pvs1"))
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["a"] shouldBe "value"
          context.dataStore()["b"] shouldBe "v2"
        }
        it("should fail on non existing key") {
          val given =
              PolicyActionSave(
                  key = "c", value = PolicyVariableRef("pvs1"), failOnMissingKey = true)
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
          context.dataStore()["a"] shouldBe "value"
        }
        it("should fail on existing key") {
          val given =
              PolicyActionSave(
                  key = "a", value = PolicyVariableRef("pvs1"), failOnExistingKey = true)
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
          context.dataStore()["a"] shouldBe "value"
        }
        it("should fail on null source") {
          val given =
              PolicyActionSave(
                  key = "a", value = PolicyVariableRef("pvs2"), failOnNullSource = true)
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
          context.dataStore()["a"] shouldBe "value"
        }
        it("should not fail on null source") {
          val given = PolicyActionSave(key = "a", value = PolicyVariableRef("pvs2"))
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["a"] shouldBe null
        }
        it("should fail on null source when not resolved") {
          val given =
              PolicyActionSave(
                  key = "a", value = PolicyVariableRef("pvs999"), failOnNullSource = true)
          val actual = given.run(context, policyCatalog)
          actual shouldBe false
          context.dataStore()["a"] shouldBe "value"
        }
        it("should not fail on null source when not resolved") {
          val given = PolicyActionSave(key = "a", value = PolicyVariableRef("pvs999"))
          val actual = given.run(context, policyCatalog)
          actual shouldBe true
          context.dataStore()["a"] shouldBe null
        }
      }

      describe("childRefs") {
        it("should return null") {
          val actual =
              PolicyActionSave(
                  key = "a",
                  value =
                      PolicyVariableStatic(
                          id = "pvs1", value = "v2", type = VariableValueTypeEnum.STRING))
          actual.childRefs() shouldBe null
        }
        it("should return variable") {
          val actual = PolicyActionSave(key = "a", value = PolicyVariableRef("pvs1"))
          actual.childRefs() shouldNotBe null
          actual.childRefs()!!.shouldHaveSize(1)
        }
      }

      describe("identity") {
        it("should return empty") {
          val actual = PolicyActionSave(key = "a", value = PolicyVariableRef("pvs1"))
          actual.identity() shouldBe ""
        }
        it("should throw when bad id") {
          shouldThrow<IllegalArgumentException> {
                PolicyActionSave(id = " ", key = "a", value = PolicyVariableRef("pvs1"))
              }
              .message shouldBe "Id must not be blank"
        }
        it("should return id") {
          val actual = PolicyActionSave(id = "1", key = "a", value = PolicyVariableRef("pvs1"))
          actual.identity() shouldBe "1"
        }
        it("should return idVer") {
          val actual =
              PolicyActionSave(
                  id = "1", version = SemVer(1, 0, 0), key = "a", value = PolicyVariableRef("pvs1"))
          actual.identity() shouldBe "1:1.0.0"
        }
      }

      describe("events") {
        it("set") {
          val given =
              PolicyActionSave(
                  id = "pva1",
                  key = "a",
                  value =
                      PolicyVariableStatic(
                          id = "pvs1", value = "v2", type = VariableValueTypeEnum.STRING))
          given.run(context, policyCatalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 2
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe true
          actualEvents[0].entityId shouldBe "pva1/source(pvs1)"
          actualEvents[0].entity shouldBe PolicyEntityEnum.VARIABLE_STATIC
          actualEvents[0].reason shouldBe null
          actualEvents[0].message shouldBe "VariableValue(type=STRING, body=v2)"
          actualEvents[0].fromCache shouldBe false

          actualEvents[1].contextId shouldNotBe null
          actualEvents[1].success shouldBe true
          actualEvents[1].entityId shouldBe "pva1"
          actualEvents[1].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE
          actualEvents[1].reason shouldBe null
          actualEvents[1].message shouldBe "v2"
          actualEvents[1].fromCache shouldBe false
        }
        it("missing key") {
          context.event.list() shouldHaveSize 0
          val given =
              PolicyActionSave(
                  id = "pva1",
                  key = "b",
                  value =
                      PolicyVariableStatic(
                          id = "pvs1", value = "v2", type = VariableValueTypeEnum.STRING),
                  failOnMissingKey = true)
          given.run(context, policyCatalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pva1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE
          actualEvents[0].reason shouldContain "Missing key: b"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false
        }
        it("existing key") {
          val given =
              PolicyActionSave(
                  id = "pva1",
                  key = "a",
                  value =
                      PolicyVariableStatic(
                          id = "pvs1", value = "v2", type = VariableValueTypeEnum.STRING),
                  failOnExistingKey = true)
          given.run(context, policyCatalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 1
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pva1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE
          actualEvents[0].reason shouldContain "Existing key: a"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false
        }
        it("null value") {
          val given =
              PolicyActionSave(
                  id = "pva1",
                  key = "a",
                  value = PolicyVariableRef("pvs999"),
                  failOnNullSource = true)
          given.run(context, policyCatalog)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 2
          actualEvents[0].contextId shouldNotBe null
          actualEvents[0].success shouldBe false
          actualEvents[0].entityId shouldBe "pva1"
          actualEvents[0].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE
          actualEvents[0].reason shouldContain "not found in catalog"
          actualEvents[0].message shouldBe null
          actualEvents[0].fromCache shouldBe false

          actualEvents[1].contextId shouldNotBe null
          actualEvents[1].success shouldBe false
          actualEvents[1].entityId shouldBe "pva1"
          actualEvents[1].entity shouldBe PolicyEntityEnum.POLICY_ACTION_SAVE
          actualEvents[1].reason shouldContain "Null source value"
          actualEvents[1].message shouldBe null
          actualEvents[1].fromCache shouldBe false
        }
      }

      describe("naming") {
        it("not named") {
          val given =
              PolicyActionSave(
                  key = "a",
                  value =
                      PolicyVariableStatic(
                          id = "pvs1", value = "v2", type = VariableValueTypeEnum.STRING))
          given.run(context, policyCatalog)
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.POLICY_ACTION_SAVE }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe ""
        }
        it("child") {
          context.addToPath("actions")
          context.addToPath("0")
          val given =
              PolicyActionSave(
                  key = "a",
                  value =
                      PolicyVariableStatic(
                          id = "pvs1", value = "v2", type = VariableValueTypeEnum.STRING))
          given.run(context, policyCatalog)
          context.removeLastFromPath()
          context.removeLastFromPath()
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.POLICY_ACTION_SAVE }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe "actions/0"
        }
        it("named child") {
          context.addToPath("actions")
          context.addToPath("0")
          val given =
              PolicyActionSave(
                  id = "pva1",
                  key = "a",
                  value =
                      PolicyVariableStatic(
                          id = "pvs1", value = "v2", type = VariableValueTypeEnum.STRING))
          given.run(context, policyCatalog)
          context.removeLastFromPath()
          context.removeLastFromPath()
          val actualEvents =
              context.event.list().filter { it.entity == PolicyEntityEnum.POLICY_ACTION_SAVE }
          actualEvents shouldHaveSize 1
          actualEvents[0].entityId shouldBe "actions/0(pva1)"
        }
      }
    })
