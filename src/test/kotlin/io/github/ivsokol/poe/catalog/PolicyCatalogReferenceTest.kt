package io.github.ivsokol.poe.catalog

import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.action.*
import io.github.ivsokol.poe.condition.*
import io.github.ivsokol.poe.policy.*
import io.github.ivsokol.poe.variable.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class PolicyCatalogReferenceTest :
    FunSpec({
      test("all refs ok") {
        val givenResolvers: List<PolicyVariableResolver> =
            listOf(
                PolicyVariableResolver(id = "pvr1", version = SemVer(1, 0, 0), key = "str1"),
                PolicyVariableResolver(id = "pvr2", key = "str1"),
            )
        val givenVariables: List<IPolicyVariable> =
            listOf(
                PolicyVariableDynamic(
                    id = "pvd1",
                    version = SemVer(1, 0, 0),
                    resolvers = listOf(PolicyVariableResolverRef("pvr1"))),
                PolicyVariableDynamic(
                    id = "pvd2", resolvers = listOf(PolicyVariableResolverRef("pvr2"))),
                PolicyVariableDynamic(
                    id = "pvd3", resolvers = listOf(PolicyVariableResolverRef("pvr2"))),
                PolicyVariableStatic(id = "pvs1", version = SemVer(1, 0, 0), value = "str1"),
                PolicyVariableStatic(id = "pvs2", value = "str1"),
            )
        val givenConditions: List<IPolicyCondition> =
            listOf(
                PolicyConditionAtomic(
                    id = "pca1",
                    version = SemVer(1, 0, 0),
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvd1"), PolicyVariableRef("pvd2"))),
                PolicyConditionAtomic(
                    id = "pca2",
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvs1"), PolicyVariableRef("pvs2"))),
                PolicyConditionComposite(
                    id = "pcc1",
                    version = SemVer(1, 0, 0),
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
                    source = PolicyVariableRef("pvs1"),
                    merge = PolicyVariableRef("pvs2"),
                ),
                io.github.ivsokol.poe.action.PolicyActionJsonPatch(
                    id = "pajp1",
                    version = SemVer(1, 0, 0),
                    key = "foo",
                    source = PolicyVariableRef("pvs1"),
                    patch = PolicyVariableRef("pvs2"),
                ),
            )

        val actual =
            PolicyCatalog(
                id = "test-catalog",
                policyConditions = givenConditions,
                policyVariables = givenVariables,
                policyVariableResolvers = givenResolvers,
                policyActions = givenActions)
        actual shouldNotBe null
      }

      test("missingResolverRefs") {
        val givenResolvers: List<PolicyVariableResolver> = emptyList()
        val givenVariables: List<IPolicyVariable> =
            listOf(
                PolicyVariableDynamic(
                    id = "pvd1",
                    version = SemVer(1, 0, 0),
                    resolvers = listOf(PolicyVariableResolverRef("pvr1"))),
                PolicyVariableDynamic(
                    id = "pvd2", resolvers = listOf(PolicyVariableResolverRef("pvr2"))),
                PolicyVariableDynamic(
                    id = "pvd3", resolvers = listOf(PolicyVariableResolverRef("pvr2"))),
                PolicyVariableStatic(id = "pvs1", version = SemVer(1, 0, 0), value = "str1"),
                PolicyVariableStatic(id = "pvs2", value = "str1"),
            )
        val givenConditions: List<IPolicyCondition> =
            listOf(
                PolicyConditionAtomic(
                    id = "pca1",
                    version = SemVer(1, 0, 0),
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvd1"), PolicyVariableRef("pvd2"))),
                PolicyConditionAtomic(
                    id = "pca2",
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvs1"), PolicyVariableRef("pvs2"))),
                PolicyConditionComposite(
                    id = "pcc1",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pca1"), PolicyConditionRef("pca2"))))

        shouldThrow<IllegalStateException> {
              PolicyCatalog(
                  id = "test-catalog",
                  policyConditions = givenConditions,
                  policyVariables = givenVariables,
                  policyVariableResolvers = givenResolvers)
            }
            .message shouldBe "Missing references in catalog"
      }

      test("missingVariablesRefs") {
        val givenResolvers: List<PolicyVariableResolver> = emptyList()
        val givenVariables: List<IPolicyVariable> = emptyList()
        val givenConditions: List<IPolicyCondition> =
            listOf(
                PolicyConditionAtomic(
                    id = "pca1",
                    version = SemVer(1, 0, 0),
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvd1"), PolicyVariableRef("pvd2"))),
                PolicyConditionAtomic(
                    id = "pca2",
                    operation = OperationEnum.EQUALS,
                    args = listOf(PolicyVariableRef("pvs1"), PolicyVariableRef("pvs2"))),
                PolicyConditionComposite(
                    id = "pcc1",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pca1"), PolicyConditionRef("pca2"))))

        shouldThrow<IllegalStateException> {
              PolicyCatalog(
                  id = "test-catalog",
                  policyConditions = givenConditions,
                  policyVariables = givenVariables,
                  policyVariableResolvers = givenResolvers)
            }
            .message shouldBe "Missing references in catalog"
      }

      test("missingActions") {
        val givenPolicies: List<IPolicy> =
            listOf(
                PolicyDefault(
                    PolicyResultEnum.PERMIT,
                    actions =
                        listOf(
                            PolicyActionRelationship(
                                action = PolicyActionRef("pa1"),
                            ))),
                Policy(
                    targetEffect = PolicyTargetEffectEnum.PERMIT,
                    condition = PolicyConditionDefault(true),
                    actions =
                        listOf(
                            PolicyActionRelationship(
                                action = PolicyActionRef("pa1"),
                            ))),
                PolicySet(
                    policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                    policies =
                        listOf(PolicyRelationship(policy = PolicyDefault(PolicyResultEnum.PERMIT))),
                    actions =
                        listOf(
                            PolicyActionRelationship(
                                action = PolicyActionRef("pa1"),
                            ))),
            )

        shouldThrow<IllegalStateException> {
              PolicyCatalog(id = "test-catalog", policies = givenPolicies)
            }
            .message shouldBe "Missing references in catalog"
      }

      test("missingConditionsRefs") {
        val givenResolvers: List<PolicyVariableResolver> = emptyList()
        val givenVariables: List<IPolicyVariable> = emptyList()
        val givenConditions: List<IPolicyCondition> =
            listOf(
                PolicyConditionComposite(
                    id = "pcc1",
                    version = SemVer(1, 0, 0),
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions = listOf(PolicyConditionRef("pca1"), PolicyConditionRef("pca2"))))

        shouldThrow<IllegalStateException> {
              PolicyCatalog(
                  id = "test-catalog",
                  policyConditions = givenConditions,
                  policyVariables = givenVariables,
                  policyVariableResolvers = givenResolvers,
              )
            }
            .message shouldBe "Missing references in catalog"
      }

      test("missingPolicyRefs") {
        val givenPolicies: List<IPolicy> =
            listOf(
                PolicySet(
                    id = "pcc1",
                    version = SemVer(1, 0, 0),
                    policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                    policies = listOf(PolicyRelationship(policy = PolicyRef("p1")))))

        shouldThrow<IllegalStateException> {
              PolicyCatalog(id = "test-catalog", policies = givenPolicies)
            }
            .message shouldBe "Missing references in catalog"
      }

      test("missing conditions on policy") {
        val givenPolicies: List<IPolicy> =
            listOf(
                Policy(
                    id = "p1",
                    version = SemVer(1, 0, 0),
                    targetEffect = PolicyTargetEffectEnum.PERMIT,
                    condition = PolicyConditionRef("pcc2")))

        shouldThrow<IllegalStateException> {
              PolicyCatalog(id = "test-catalog", policies = givenPolicies)
            }
            .message shouldBe "Missing references in catalog"
      }

      test("missing policy constraints on policy") {
        val givenPolicies: List<IPolicy> =
            listOf(
                PolicySet(
                    id = "p1",
                    version = SemVer(1, 0, 0),
                    policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                    policies =
                        listOf(
                            PolicyRelationship(
                                constraint = PolicyConditionRef("c1"),
                                policy = PolicyDefault(PolicyResultEnum.PERMIT)))))

        shouldThrow<IllegalStateException> {
              PolicyCatalog(id = "test-catalog", policies = givenPolicies)
            }
            .message shouldBe "Missing references in catalog"
      }
      test("missing actions constraints on policy") {
        val givenPolicies: List<IPolicy> =
            listOf(
                PolicyDefault(
                    PolicyResultEnum.PERMIT,
                    actions =
                        listOf(
                            PolicyActionRelationship(
                                constraint = PolicyConditionRef("c1"),
                                action = PolicyActionClear(key = "c1")))),
                Policy(
                    targetEffect = PolicyTargetEffectEnum.PERMIT,
                    condition = PolicyConditionDefault(true),
                    actions =
                        listOf(
                            PolicyActionRelationship(
                                constraint = PolicyConditionRef("c1"),
                                action = PolicyActionClear(key = "c1")))),
                PolicySet(
                    id = "p1",
                    version = SemVer(1, 0, 0),
                    policyCombinationLogic = PolicyCombinationLogicEnum.FIRST_APPLICABLE,
                    policies =
                        listOf(PolicyRelationship(policy = PolicyDefault(PolicyResultEnum.PERMIT))),
                    actions =
                        listOf(
                            PolicyActionRelationship(
                                constraint = PolicyConditionRef("c1"),
                                action = PolicyActionClear(key = "c1")))))

        shouldThrow<IllegalStateException> {
              PolicyCatalog(id = "test-catalog", policies = givenPolicies)
            }
            .message shouldBe "Missing references in catalog"
      }
    })
