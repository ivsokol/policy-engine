package io.github.ivsokol.poe.catalog

import io.github.ivsokol.poe.*
import io.github.ivsokol.poe.action.IPolicyAction
import io.github.ivsokol.poe.condition.IPolicyCondition
import io.github.ivsokol.poe.condition.PolicyConditionComposite
import io.github.ivsokol.poe.condition.PolicyConditionDefault
import io.github.ivsokol.poe.policy.IPolicy
import io.github.ivsokol.poe.policy.PolicyDefault
import io.github.ivsokol.poe.policy.PolicyResultEnum
import io.github.ivsokol.poe.policy.PolicySet
import io.github.ivsokol.poe.variable.IPolicyVariable
import io.github.ivsokol.poe.variable.PolicyVariableResolver
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.slf4j.LoggerFactory

@Serializable
@SerialName("PolicyCatalog")
/**
 * Represents a catalog of policies, policy conditions, policy variables, policy variable resolvers,
 * and policy actions.
 *
 * The [PolicyCatalog] class is responsible for managing and providing access to the various
 * components that make up a policy catalog. It performs validation checks on the catalog, ensuring
 * that there are no circular references or missing references.
 *
 * The catalog can be used to retrieve specific policy conditions, variables, resolvers, actions,
 * and policies by their unique identifiers and versions. It also provides methods to search for
 * policy conditions and policies by their labels.
 *
 * @property id The unique identifier of the policy catalog.
 * @property version The version of the policy catalog, using the CalVer versioning scheme.
 * @property withDefaultPolicies A boolean indicating whether to include default policies in the
 *   catalog.
 * @property withDefaultConditions A boolean indicating whether to include default policy conditions
 *   in the catalog.
 * @property policies The list of policies in the catalog.
 * @property policyConditions The list of policy conditions in the catalog.
 * @property policyVariables The list of policy variables in the catalog.
 * @property policyVariableResolvers The list of policy variable resolvers in the catalog.
 * @property policyActions The list of policy actions in the catalog.
 */
data class PolicyCatalog(
    val id: String,
    val version: CalVer? = DefaultCalVer(),
    val withDefaultPolicies: Boolean? = null,
    val withDefaultConditions: Boolean? = null,
    private val policies: List<IPolicy> = emptyList(),
    private val policyConditions: List<IPolicyCondition> = emptyList(),
    private val policyVariables: List<IPolicyVariable> = emptyList(),
    private val policyVariableResolvers: List<PolicyVariableResolver> = emptyList(),
    private val policyActions: List<IPolicyAction> = emptyList(),
) {

  @Transient private val logger = LoggerFactory.getLogger(PolicyCatalog::class.java)
  @Transient val idVer: String = if (version != null) "$id:$version" else id ?: ""

  @Transient
  private val defaultPolicyList =
      listOf(
          PolicyDefault(PolicyResultEnum.PERMIT),
          PolicyDefault(PolicyResultEnum.DENY),
          PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY),
          PolicyDefault(PolicyResultEnum.INDETERMINATE_PERMIT),
          PolicyDefault(PolicyResultEnum.INDETERMINATE_DENY_PERMIT),
          PolicyDefault(PolicyResultEnum.NOT_APPLICABLE),
      )

  @Transient
  private val defaultPolicyConditionList =
      listOf(
          PolicyConditionDefault(true),
          PolicyConditionDefault(false),
          PolicyConditionDefault(null),
      )

  @Transient
  private val policyConditionMap: Map<String, List<IPolicyCondition>> =
      if (withDefaultConditions == true)
          (policyConditions + defaultPolicyConditionList).toCatalogMapCondition()
      else policyConditions.toCatalogMapCondition()

  @Transient
  private val policyVariableMap: Map<String, List<IPolicyVariable>> =
      policyVariables.toCatalogMapVariable()

  @Transient
  private val policyVariableResolverMap: Map<String, List<PolicyVariableResolver>> =
      policyVariableResolvers.toCatalogMapResolver()

  @Transient
  private val policyActionMap: Map<String, List<IPolicyAction>> = policyActions.toCatalogMapAction()

  @Transient
  private val policyMap: Map<String, List<IPolicy>> =
      if (withDefaultPolicies == true) (policies + defaultPolicyList).toCatalogMapPolicy()
      else policies.toCatalogMapPolicy()

  @Transient private val errors = mutableListOf<PolicyCatalogErrorItem>()

  @Transient private val checkRefCache = mutableMapOf<String, Boolean>()

  init {
    // check that conditions or policies are not empty
    require(policyConditions.isNotEmpty() || policies.isNotEmpty()) {
      "Conditions and policies cannot be empty at the same time"
    }
    // check child refs
    logger.debug("Checking child refs for variables")
    policyVariableMap.values.flatten().forEach {
      val id = it.identity()
      val children = it.childRefs()
      checkChildRefs(id, children)
    }
    logger.debug("Checking child refs for conditions")
    policyConditionMap.values.flatten().forEach {
      val id = it.identity()
      val children = it.childRefs()
      checkChildRefs(id, children)
    }
    logger.debug("Checking child refs for actions")
    policyActionMap.values.flatten().forEach {
      val id = it.identity()
      val children = it.childRefs()
      checkChildRefs(id, children)
    }
    logger.debug("Checking child refs for policies")
    policyMap.values.flatten().forEach {
      val id = it.identity()
      val children = it.childRefs()
      checkChildRefs(id, children)
    }
    checkRefCache.clear()
    if (errors.isNotEmpty()) {
      logger.info("Missing references: {}", errors)
      error("Missing references in catalog")
    }

    // check circular refs

    logger.debug("Checking circular refs for composite conditions")
    policyConditionMap.values.flatten().filterIsInstance<PolicyConditionComposite>().forEach {
      val id = extractIdFromIdentity(it)
      logger.debug("Checking circular ref for root condition {}", it.identity())
      checkCircularRefs(it, listOf(id), CircularReferenceEnum.CONDITION)
      logger.debug("Checking circular ref for root condition {} finished", it.identity())
    }

    logger.debug("Checking circular refs for PolicySets")
    policyMap.values.flatten().filterIsInstance<PolicySet>().forEach {
      val id = extractIdFromIdentity(it)
      logger.debug("Checking circular ref for root PolicySet {}", it.identity())
      checkCircularRefs(it, listOf(id), CircularReferenceEnum.POLICY)
      logger.debug("Checking circular ref for root PolicySet {} finished", it.identity())
    }
    if (errors.isNotEmpty()) {
      logger.info("Circular references: {}", errors)
      error("Circular references in catalog")
    }
  }

  private fun checkCircularRefs(
      parent: IManaged,
      ancestors: List<String>,
      circularRefType: CircularReferenceEnum
  ) {
    // get children of parent
    logger.debug("{}:fetching children for {}", parent.identity(), parent::class.java.simpleName)
    val childRefsResolved: List<IManaged>? =
        when (circularRefType) {
          CircularReferenceEnum.CONDITION ->
              (parent as PolicyConditionComposite)
                  .childRefs()
                  ?.filter { it.entity == PolicyEntityRefEnum.POLICY_CONDITION_REF }
                  ?.mapNotNull { this.getPolicyCondition(it.id, it.version) }
                  ?.filterIsInstance<PolicyConditionComposite>()
          CircularReferenceEnum.POLICY ->
              (parent as PolicySet)
                  .childRefs()
                  ?.filter { it.entity == PolicyEntityRefEnum.POLICY_REF }
                  ?.mapNotNull { this.getPolicy(it.id, it.version) }
                  ?.filterIsInstance<PolicySet>()
        }
    logger.trace(
        "{}:childRefsResolved: {}",
        parent.identity(),
        childRefsResolved?.map { extractIdFromIdentity(it) })
    if (childRefsResolved.isNullOrEmpty()) return
    for (childRef in childRefsResolved) {
      // check if childRef exists in ancestors
      val childId = extractIdFromIdentity(childRef)
      logger.debug(
          "{}:checking if child {} exists in ancestors", parent.identity(), childRef.identity())
      logger.trace("{}:ancestors: {}", childRef.identity(), ancestors)
      if (ancestors.contains(childId)) {
        logger.error("{}:child {} exists in ancestors!!!", parent.identity(), childRef.identity())
        errors.add(
            PolicyCatalogErrorItem(
                childRef.identity(),
                ErrorTypeEnum.CIRCULAR_REFERENCE_ERROR,
                "Circular reference found for type $circularRefType: ${ancestors.joinToString("->")}->$childId"))
      } else {
        logger.debug(
            "{}:child {} does not exists in ancestors", parent.identity(), childRef.identity())
        logger.debug("{}:checking children of child {}", parent.identity(), childRef.identity())
        // check children of childRef
        checkCircularRefs(childRef, ancestors.plus(childId), circularRefType)
      }
    }
  }

  private fun extractIdFromIdentity(entity: IManaged): String =
      entity.id ?: throw java.lang.IllegalStateException("Missing id for $entity")

  private fun checkChildRefs(id: String, refs: Set<PolicyEntityRefItem>?) {
    logger.debug("$id:Checking child refs for $id")
    logger.trace("$id:list of refs: {}", refs)
    if (refs == null) return
    if (refs.isEmpty()) return
    refs.forEach { ref ->
      val key = "${ref.entity}.${ref.id}"
      if (checkRefCache.containsKey(key)) {
        val found = checkRefCache[key]!!
        logger.debug("$id:key {} found in cache with value {}", key, found)
        if (!found) {
          logger.error("$id:non existing key in cache {}", key)
          errors.add(
              PolicyCatalogErrorItem(
                  id,
                  ErrorTypeEnum.NON_EXISTING_REFERENCE_ERROR,
                  "Reference ${ref.entity}(${ref.id}) not found at $id"))
        }
      } else {
        val foundRef: Any? =
            when (ref.entity) {
              PolicyEntityRefEnum.POLICY_REF -> this.getPolicy(ref.id, ref.version)
              PolicyEntityRefEnum.POLICY_ACTION_REF -> this.getPolicyAction(ref.id, ref.version)
              PolicyEntityRefEnum.POLICY_CONDITION_REF ->
                  this.getPolicyCondition(ref.id, ref.version)
              PolicyEntityRefEnum.POLICY_VARIABLE_REF -> this.getPolicyVariable(ref.id, ref.version)
              PolicyEntityRefEnum.POLICY_VARIABLE_RESOLVER_REF ->
                  this.getPolicyVariableResolver(ref.id, ref.version)
            }
        checkRefCache[key] = foundRef != null
        if (foundRef == null) {
          logger.error("$id:non existing key {}", key)
          errors.add(
              PolicyCatalogErrorItem(
                  id,
                  ErrorTypeEnum.NON_EXISTING_REFERENCE_ERROR,
                  "Reference ${ref.entity}(${ref.id}) not found at $id"))
        } else {
          logger.debug("$id:key {} found", key)
        }
      }
    }
  }

  fun getPolicyCondition(id: String, version: SemVer? = null): IPolicyCondition? =
      policyConditionMap.findByIdAndVersion(id, version)

  fun getPolicyVariable(id: String, version: SemVer? = null): IPolicyVariable? =
      policyVariableMap.findByIdAndVersion(id, version)

  fun getPolicyVariableResolver(id: String, version: SemVer? = null): PolicyVariableResolver? =
      policyVariableResolverMap.findByIdAndVersion(id, version)

  fun getPolicyAction(id: String, version: SemVer? = null): IPolicyAction? =
      policyActionMap.findByIdAndVersion(id, version)

  fun getPolicy(id: String, version: SemVer? = null): IPolicy? =
      policyMap.findByIdAndVersion(id, version)

  fun searchConditionsByLabels(
      labels: Set<String>,
      logic: LabelSearchLogicEnum
  ): List<IPolicyCondition>? = policyConditionMap.searchByLabels(labels, logic)

  fun searchPoliciesByLabels(labels: Set<String>, logic: LabelSearchLogicEnum): List<IPolicy>? =
      policyMap.searchByLabels(labels, logic)

  fun getAllConditions(): List<IPolicyCondition> = policyConditionMap.values.map { it.first() }

  fun getAllPolicies(): List<IPolicy> = policyMap.values.map { it.first() }
}
