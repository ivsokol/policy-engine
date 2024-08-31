package io.github.ivsokol.poe

import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.condition.*
import io.github.ivsokol.poe.policy.PolicyDefault
import io.github.ivsokol.poe.policy.PolicyResultEnum
import io.github.ivsokol.poe.variable.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime

class PolicyEngineConditionTest :
    DescribeSpec({
      describe("checkCondition by id and version") {
        val given = PolicyEngine(catalog())
        it("should check by id") {
          val context = Context()
          val actual = given.checkCondition("pca2", context)
          actual shouldBe true
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 5
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 1
        }
        it("should check by id and version") {
          val context = Context(request = mapOf("str1" to "a", "str2" to "a"))
          val actual = given.checkCondition("pca1", context, SemVer(1, 0, 0))
          actual shouldBe true
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 7
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.VALUE_RESOLVER } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 1
        }
        it("should return null for unknown id") {
          val context = Context()
          val actual = given.checkCondition("unknown", context)
          actual shouldBe null
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_CATALOG } shouldHaveSize 1
        }
        it("should return null for unknown version") {
          val context = Context()
          val actual = given.checkCondition("pca1", context, SemVer(999, 0, 0))
          actual shouldBe null
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_CATALOG } shouldHaveSize 1
        }
      }

      describe("check custom condition") {
        val given = PolicyEngine(catalog())
        it("should check with empty catalog") {
          val context = Context()
          val actual =
              PolicyEngine()
                  .checkCondition(
                      PolicyConditionAtomic(
                          operation = OperationEnum.IS_PAST,
                          args =
                              listOf(
                                  PolicyVariableStatic(
                                      value = OffsetDateTime.now().minusYears(1),
                                      type = VariableValueTypeEnum.STRING,
                                      format = VariableValueFormatEnum.DATE_TIME))),
                      context)
          actual shouldBe true
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 4
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 1
        }
        it("should check with provided catalog reference") {
          val context = Context()
          val actual =
              given.checkCondition(
                  PolicyConditionAtomic(
                      operation = OperationEnum.EQUALS,
                      args =
                          listOf(
                              PolicyVariableRef("pvs1"),
                              PolicyVariableStatic(
                                  value = "str1", type = VariableValueTypeEnum.STRING))),
                  context)
          actual shouldBe true
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 5
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 1
        }
        it("should fail if reference does not exist") {
          val context = Context(request = mapOf("str1" to "a", "str2" to "a"))
          val actual =
              given.checkCondition(
                  PolicyConditionAtomic(
                      operation = OperationEnum.EQUALS,
                      args =
                          listOf(
                              PolicyVariableRef("noVar"),
                              PolicyVariableStatic(
                                  value = "str1", type = VariableValueTypeEnum.STRING))),
                  context)
          actual shouldBe null
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 5
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 2
        }
      }

      describe("check condition id list") {
        val given = PolicyEngine(catalog())
        it("should check by id list") {
          val context = Context(request = mapOf("str1" to "a", "str2" to "a"))
          val actual = given.checkConditionsByIds(setOf("pca1", "pca2", "pca999"), context)
          actual shouldHaveSize 3
          actual["pca1"] shouldBe true
          actual["pca2"] shouldBe true
          actual["pca999"] shouldBe null
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 11
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_CATALOG } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.VALUE_RESOLVER } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 2
        }
      }

      describe("check condition ref list") {
        val given = PolicyEngine(catalog())
        it("should check by id and version") {
          val context = Context(request = mapOf("str1" to "a", "str2" to "a"))
          val actual =
              given.checkConditionsByRefs(
                  setOf(
                      Reference("pca1"),
                      Reference("pca1", SemVer(1, 0, 0)),
                      Reference("pca1", SemVer(2, 0, 0)),
                      Reference("pca2"),
                      Reference("pca999")),
                  context)
          actual shouldHaveSize 5
          actual["pca1"] shouldBe true
          actual["pca1:1.0.0"] shouldBe true
          actual["pca1:2.0.0"] shouldBe null
          actual["pca2"] shouldBe true
          actual["pca999"] shouldBe null
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 13
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_CATALOG } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.VALUE_RESOLVER } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 2
        }
      }

      describe("check condition labels") {
        val given = PolicyEngine(catalog())
        it("should check by any of") {
          val context = Context(request = mapOf("str1" to "a", "str2" to "a"))
          val actual =
              given.checkConditionsByLabels(setOf("a", "b"), LabelSearchLogicEnum.ANY_OF, context)
          actual shouldHaveSize 3
          actual["pca1:1.0.0"] shouldBe true
          actual["pca2"] shouldBe true
          actual["pcc1:1.0.0"] shouldBe true
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 13
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.VALUE_RESOLVER } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 4
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_COMPOSITE } shouldHaveSize 1
        }
        it("should check by all of") {
          val context = Context(request = mapOf("str1" to "a", "str2" to "a"))
          val actual =
              given.checkConditionsByLabels(setOf("a", "b"), LabelSearchLogicEnum.ALL_OF, context)
          actual shouldHaveSize 1
          actual["pcc1:1.0.0"] shouldBe true
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 11
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.VALUE_RESOLVER } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_COMPOSITE } shouldHaveSize 1
        }
        it("should not found") {
          val context = Context(request = mapOf("str1" to "a", "str2" to "a"))
          val actual =
              given.checkConditionsByLabels(
                  setOf("a", "b", "c"), LabelSearchLogicEnum.ALL_OF, context)
          actual shouldHaveSize 0
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_CATALOG } shouldHaveSize 1
        }
      }

      describe("check all conditions") {
        val given = PolicyEngine(catalog())
        it("should check all") {
          val context = Context(request = mapOf("str1" to "a", "str2" to "a"))
          val actual = given.checkAllConditions(context)
          actual shouldHaveSize 4
          actual["pca1:1.0.0"] shouldBe true
          actual["pca2"] shouldBe true
          actual["pca3"] shouldBe false
          actual["pcc1:1.0.0"] shouldBe true
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 16
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.VALUE_RESOLVER } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 4
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 5
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_COMPOSITE } shouldHaveSize 1
        }
        it("should check null") {
          val context = Context(request = mapOf("str1" to "a", "str2" to "a"))
          val actual =
              PolicyEngine(
                      PolicyCatalog(
                          id = "test-catalog",
                          policies = listOf(PolicyDefault(PolicyResultEnum.PERMIT))))
                  .checkAllConditions(context)
          actual shouldHaveSize 0
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_CATALOG } shouldHaveSize 1
        }
      }
    })

private fun catalog(): PolicyCatalog {
  val givenResolvers: List<PolicyVariableResolver> =
      listOf(
          PolicyVariableResolver(id = "pvr1", version = SemVer(1, 0, 0), key = "str1"),
          PolicyVariableResolver(id = "pvr2", key = "str2"),
      )
  val givenVariables: List<IPolicyVariable> =
      listOf(
          PolicyVariableDynamic(
              id = "pvd1",
              version = SemVer(1, 0, 0),
              resolvers = listOf(PolicyVariableResolverRef("pvr1"))),
          PolicyVariableDynamic(id = "pvd2", resolvers = listOf(PolicyVariableResolverRef("pvr2"))),
          PolicyVariableDynamic(id = "pvd3", resolvers = listOf(PolicyVariableResolverRef("pvr2"))),
          PolicyVariableStatic(id = "pvs1", version = SemVer(1, 0, 0), value = "str1"),
          PolicyVariableStatic(id = "pvs2", value = "str1"),
          PolicyVariableStatic(id = "pvs3", value = "str2"),
      )
  val givenConditions: List<IPolicyCondition> =
      listOf(
          PolicyConditionAtomic(
              id = "pca1",
              version = SemVer(1, 0, 0),
              labels = listOf("a"),
              operation = OperationEnum.EQUALS,
              args = listOf(PolicyVariableRef("pvd1"), PolicyVariableRef("pvd2"))),
          PolicyConditionAtomic(
              id = "pca2",
              labels = listOf("b"),
              operation = OperationEnum.EQUALS,
              args = listOf(PolicyVariableRef("pvs1"), PolicyVariableRef("pvs2"))),
          PolicyConditionAtomic(
              id = "pca3",
              operation = OperationEnum.EQUALS,
              args = listOf(PolicyVariableRef("pvs3"), PolicyVariableRef("pvs2"))),
          PolicyConditionComposite(
              id = "pcc1",
              version = SemVer(1, 0, 0),
              labels = listOf("a", "b"),
              conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
              conditions = listOf(PolicyConditionRef("pca1"), PolicyConditionRef("pca2"))))
  return PolicyCatalog(
      id = "id",
      version = CalVer(2024, 2, 17),
      policyConditions = givenConditions,
      policyVariables = givenVariables,
      policyVariableResolvers = givenResolvers)
}
