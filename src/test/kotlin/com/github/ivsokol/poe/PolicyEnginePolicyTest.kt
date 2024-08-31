package com.github.ivsokol.poe

import com.github.ivsokol.poe.action.*
import com.github.ivsokol.poe.catalog.PolicyCatalog
import com.github.ivsokol.poe.condition.*
import com.github.ivsokol.poe.policy.*
import com.github.ivsokol.poe.variable.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime

class PolicyEnginePolicyTest :
    DescribeSpec({
      describe("checkPolicy by id and version") {
        val given = PolicyEngine(catalog())
        it("should evaluate by id") {
          val context = context()
          val actual = given.evaluatePolicy("pol2", context)
          actual shouldBe Pair(PolicyResultEnum.DENY, true)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 15
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_COMPOSITE } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VALUE_RESOLVER } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION_SAVE } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION } shouldHaveSize 1
        }
        it("should evaluate by id and version") {
          val context = context()
          val actual = given.evaluatePolicy("pol1", context, SemVer(1, 0, 0))
          actual shouldBe Pair(PolicyResultEnum.PERMIT, true)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 10
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.VALUE_RESOLVER } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION_CLEAR } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION } shouldHaveSize 1
        }
        it("should return null for unknown id") {
          val context = context()
          val actual = given.evaluatePolicy("unknown", context)
          actual shouldBe Pair(PolicyResultEnum.INDETERMINATE_DENY_PERMIT, null)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_CATALOG } shouldHaveSize 1
        }
        it("should return null for unknown version") {
          val context = context()
          val actual = given.evaluatePolicy("pol1", context, SemVer(999, 0, 0))
          actual shouldBe Pair(PolicyResultEnum.INDETERMINATE_DENY_PERMIT, null)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_CATALOG } shouldHaveSize 1
        }
      }

      describe("check custom Policy") {
        val given = PolicyEngine(catalog())
        it("should evaluate with empty catalog") {
          val context = context()
          val actual =
              PolicyEngine()
                  .evaluatePolicy(
                      Policy(
                          targetEffect = PolicyTargetEffectEnum.PERMIT,
                          condition =
                              PolicyConditionAtomic(
                                  operation = OperationEnum.IS_PAST,
                                  args =
                                      listOf(
                                          PolicyVariableStatic(
                                              value = OffsetDateTime.now().minusYears(1),
                                              type = VariableValueTypeEnum.STRING,
                                              format = VariableValueFormatEnum.DATE_TIME))),
                          actions =
                              listOf(
                                  PolicyActionRelationship(
                                      action =
                                          PolicyActionSave(
                                              key = "foo",
                                              value = PolicyVariableStatic(value = "bar"))))),
                      context)
          actual shouldBe Pair(PolicyResultEnum.PERMIT, true)
          val actualEvents = context.event.list()
          context.dataStore().keys shouldContain "foo"
          actualEvents shouldHaveSize 8
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION_SAVE } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION } shouldHaveSize 1
        }
        it("should evaluate with existing catalog") {
          val context = context()
          val actual =
              given.evaluatePolicy(
                  Policy(
                      targetEffect = PolicyTargetEffectEnum.PERMIT,
                      condition =
                          PolicyConditionAtomic(
                              operation = OperationEnum.EQUALS,
                              args =
                                  listOf(
                                      PolicyVariableRef("pvs1"),
                                      PolicyVariableStatic(
                                          value = "str1", type = VariableValueTypeEnum.STRING))),
                      actions =
                          listOf(
                              PolicyActionRelationship(
                                  action =
                                      PolicyActionSave(
                                          key = "foo",
                                          value = PolicyVariableStatic(value = "bar"))))),
                  context)
          actual shouldBe Pair(PolicyResultEnum.PERMIT, true)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 9
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION_SAVE } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION } shouldHaveSize 1
        }
        it("should fail if unknown reference") {
          val context = context()
          val actual =
              given.evaluatePolicy(
                  Policy(
                      targetEffect = PolicyTargetEffectEnum.PERMIT,
                      condition =
                          PolicyConditionAtomic(
                              operation = OperationEnum.EQUALS,
                              args =
                                  listOf(
                                      PolicyVariableRef("noVar"),
                                      PolicyVariableStatic(
                                          value = "str1", type = VariableValueTypeEnum.STRING))),
                      actions =
                          listOf(
                              PolicyActionRelationship(
                                  executionMode = setOf(ActionExecutionModeEnum.ON_PERMIT),
                                  action =
                                      PolicyActionSave(
                                          key = "foo",
                                          value = PolicyVariableStatic(value = "bar"))))),
                  context)
          actual shouldBe Pair(PolicyResultEnum.INDETERMINATE_PERMIT, true)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 6
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY } shouldHaveSize 1
        }
      }

      describe("evaluate policy id list") {
        val given = PolicyEngine(catalog())
        it("should evaluate by id list") {
          val context = context()
          val actual = given.evaluatePoliciesByIds(setOf("pol1", "pol2", "pol999"), context)
          actual shouldHaveSize 3
          actual.keys shouldContainExactly setOf("pol999", "pol2", "pol1")
          actual["pol1"] shouldBe Pair(PolicyResultEnum.PERMIT, true)
          actual["pol2"] shouldBe Pair(PolicyResultEnum.DENY, true)
          actual["pol999"] shouldBe Pair(PolicyResultEnum.INDETERMINATE_DENY_PERMIT, null)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 20
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_CATALOG } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_COMPOSITE } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VALUE_RESOLVER } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION_SAVE } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION_CLEAR } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION } shouldHaveSize 2
        }
      }

      describe("evaluate policy ref list") {
        val given = PolicyEngine(catalog())
        it("should evaluate by id and version") {
          val context = context()
          val actual =
              given.evaluatePoliciesByRefs(
                  setOf(
                      Reference("pol1"),
                      Reference("pol1", SemVer(1, 0, 0)),
                      Reference("pol1", SemVer(2, 0, 0)),
                      Reference("pol2"),
                      Reference("pol999")),
                  context)
          actual shouldHaveSize 4
          actual.keys shouldContainExactly setOf("pol1:2.0.0", "pol999", "pol2:1.0.0", "pol1:1.0.0")
          actual["pol1:1.0.0"] shouldBe Pair(PolicyResultEnum.PERMIT, true)
          actual["pol2:1.0.0"] shouldBe Pair(PolicyResultEnum.DENY, true)
          actual["pol999"] shouldBe Pair(PolicyResultEnum.INDETERMINATE_DENY_PERMIT, null)
          actual["pol1:2.0.0"] shouldBe Pair(PolicyResultEnum.INDETERMINATE_DENY_PERMIT, null)

          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 24
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_CATALOG } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_COMPOSITE } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VALUE_RESOLVER } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY } shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION_SAVE } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION_CLEAR } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION } shouldHaveSize 3
        }
      }

      describe("evaluate policy labels") {
        val given = PolicyEngine(catalog())
        it("should evaluate by any of") {
          val context = context()
          val actual =
              given.evaluatePoliciesByLabels(setOf("a", "b"), LabelSearchLogicEnum.ANY_OF, context)
          actual shouldHaveSize 3
          actual.keys shouldContainExactly setOf("pol2:1.0.0", "pol1:1.0.0", "pol4:1.0.0")
          actual["pol1:1.0.0"] shouldBe Pair(PolicyResultEnum.PERMIT, true)
          actual["pol2:1.0.0"] shouldBe Pair(PolicyResultEnum.DENY, true)
          actual["pol4:1.0.0"] shouldBe Pair(PolicyResultEnum.PERMIT, true)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 26
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 5
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_COMPOSITE } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VALUE_RESOLVER } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY } shouldHaveSize 4
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_SET } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION_SAVE } shouldHaveSize 1
          actualEvents.filter {
            it.entity == PolicyEntityEnum.POLICY_ACTION_JSON_PATCH
          } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION_CLEAR } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION } shouldHaveSize 3
        }
        it("should evaluate by all of") {
          val context = context()
          val actual =
              given.evaluatePoliciesByLabels(setOf("a", "b"), LabelSearchLogicEnum.ALL_OF, context)
          actual shouldHaveSize 1
          actual.keys shouldContainExactly setOf("pol4:1.0.0")
          actual["pol4:1.0.0"] shouldBe Pair(PolicyResultEnum.PERMIT, true)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 19
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 4
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 3
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_COMPOSITE } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VALUE_RESOLVER } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_SET } shouldHaveSize 1
          actualEvents.filter {
            it.entity == PolicyEntityEnum.POLICY_ACTION_JSON_PATCH
          } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION } shouldHaveSize 1
        }
        it("should not found") {
          val context = context()
          val actual =
              given.evaluatePoliciesByLabels(
                  setOf("a", "b", "c"), LabelSearchLogicEnum.ALL_OF, context)
          actual shouldHaveSize 0
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
        }
      }
      describe("evaluate all policies") {
        val given = PolicyEngine(catalog())
        it("should evaluate all") {
          val context = context()
          val actual = given.evaluateAllPolicies(context)
          actual shouldHaveSize 4
          actual.keys shouldContainExactly setOf("pol2:1.0.0", "pol1:1.0.0", "pol4:1.0.0", "pol3")
          actual["pol1:1.0.0"] shouldBe Pair(PolicyResultEnum.PERMIT, true)
          actual["pol2:1.0.0"] shouldBe Pair(PolicyResultEnum.DENY, true)
          actual["pol4:1.0.0"] shouldBe Pair(PolicyResultEnum.PERMIT, true)
          actual["pol3"] shouldBe Pair(PolicyResultEnum.DENY, true)
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 32
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_STATIC } shouldHaveSize 7
          actualEvents.filter { it.entity == PolicyEntityEnum.VARIABLE_DYNAMIC } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_ATOMIC } shouldHaveSize 4
          actualEvents.filter { it.entity == PolicyEntityEnum.CONDITION_COMPOSITE } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.VALUE_RESOLVER } shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY } shouldHaveSize 5
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION_SAVE } shouldHaveSize 1
          actualEvents.filter {
            it.entity == PolicyEntityEnum.POLICY_ACTION_JSON_MERGE
          } shouldHaveSize 1
          actualEvents.filter {
            it.entity == PolicyEntityEnum.POLICY_ACTION_JSON_PATCH
          } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION_CLEAR } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.POLICY_ACTION } shouldHaveSize 4
        }
        it("should evaluate null") {
          val context = context()
          val actual =
              PolicyEngine(
                      PolicyCatalog(
                          id = "test-catalog",
                          policyConditions = listOf(PolicyConditionDefault(true))))
                  .evaluateAllPolicies(context)
          actual shouldHaveSize 0
          val actualEvents = context.event.list()
          actualEvents shouldHaveSize 2
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_START } shouldHaveSize 1
          actualEvents.filter { it.entity == PolicyEntityEnum.ENGINE_END } shouldHaveSize 1
        }
      }
    })

private fun context() =
    Context(
        request =
            mapOf(
                "str1" to "a",
                "str2" to "a",
            ),
    )

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
          PolicyVariableStatic(id = "pvsj1", value = """{"foo":["bar","baz"],"foo2":"baz"}"""),
          PolicyVariableStatic(id = "pvsj2", value = """{"foo":["bar"]}"""),
          PolicyVariableStatic(
              id = "pvsjp1", value = """[{"op":"replace","path":"/foo","value":["bar"]}]"""),
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
          PolicyConditionComposite(
              id = "pcc1",
              version = SemVer(1, 0, 0),
              labels = listOf("a", "b"),
              conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
              conditions = listOf(PolicyConditionRef("pca1"), PolicyConditionRef("pca2"))))
  val givenActions: List<IPolicyAction> =
      listOf(
          PolicyActionClear(id = "pac1", version = SemVer(1, 0, 0), key = "foo"),
          PolicyActionSave(
              id = "pas1",
              version = SemVer(1, 0, 0),
              key = "foo",
              value = PolicyVariableRef("pvs1"),
          ),
          PolicyActionJsonMerge(
              id = "pajm1",
              version = SemVer(1, 0, 0),
              key = "foo",
              source = PolicyVariableRef("pvsj1"),
              merge = PolicyVariableRef("pvsj2"),
          ),
          PolicyActionJsonPatch(
              id = "pajp1",
              version = SemVer(1, 0, 0),
              key = "foo",
              source = PolicyVariableRef("pvsj1"),
              patch = PolicyVariableRef("pvsjp1"),
          ),
      )
  val givenPolicies: List<IPolicy> =
      listOf(
          Policy(
              id = "pol1",
              labels = listOf("a"),
              priority = 10,
              version = SemVer(1, 0, 0),
              targetEffect = PolicyTargetEffectEnum.PERMIT,
              condition = PolicyConditionRef("pca1"),
              actions =
                  listOf(
                      PolicyActionRelationship(
                          action = PolicyActionRef("pac1"),
                      ))),
          Policy(
              id = "pol2",
              labels = listOf("b"),
              version = SemVer(1, 0, 0),
              priority = 20,
              targetEffect = PolicyTargetEffectEnum.DENY,
              condition = PolicyConditionRef("pcc1"),
              actions =
                  listOf(
                      PolicyActionRelationship(
                          action = PolicyActionRef("pas1"),
                      ))),
          Policy(
              id = "pol3",
              targetEffect = PolicyTargetEffectEnum.DENY,
              priority = -1,
              condition = PolicyConditionRef("pca2"),
              actions =
                  listOf(
                      PolicyActionRelationship(
                          action = PolicyActionRef("pajm1"),
                      ))),
          PolicySet(
              id = "pol4",
              version = SemVer(1, 0, 0),
              labels = listOf("a", "b"),
              policyCombinationLogic = PolicyCombinationLogicEnum.PERMIT_OVERRIDES,
              policies =
                  listOf(
                      PolicyRelationship(
                          policy = PolicyRef("pol2"),
                      ),
                      PolicyRelationship(
                          policy = PolicyRef("pol1"),
                      ),
                  ),
              actions =
                  listOf(
                      PolicyActionRelationship(
                          action = PolicyActionRef("pajp1"),
                      ))))
  return PolicyCatalog(
      id = "id",
      version = CalVer(2024, 2, 17),
      policyActions = givenActions,
      policies = givenPolicies,
      policyConditions = givenConditions,
      policyVariables = givenVariables,
      policyVariableResolvers = givenResolvers)
}
