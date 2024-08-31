package io.github.ivsokol.poe

import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.catalog.catalogSerializersModule
import io.github.ivsokol.poe.condition.IPolicyCondition
import io.github.ivsokol.poe.policy.ActionResult
import io.github.ivsokol.poe.policy.IPolicy
import io.github.ivsokol.poe.policy.PolicyResultEnum
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

/**
 * Configures JSON serializer used by the PolicyEngine to handle the serialization and
 * deserialization of the PolicyCatalog. The serializer is configured to use the
 * [catalogSerializersModule] for custom serialization logic, and to encode null values and default
 * values. This serializer is used only for Policy Catalog serialization and deserialization, and
 * not for [ContextStore].
 */
private val json = Json {
  serializersModule = catalogSerializersModule
  explicitNulls = false
  encodeDefaults = true
}

private const val DEFAULT_PRIORITY = 0

/**
 * Constructs a new [PolicyEngine] instance from the provided [policyCatalog] JSON string.
 *
 * @param policyCatalog The JSON string representation of a [PolicyCatalog] instance.
 * @return A new [PolicyEngine] instance initialized with the provided [PolicyCatalog].
 * @throws IllegalArgumentException if the [policyCatalog] is blank.
 */
fun PolicyEngine(policyCatalog: String): PolicyEngine {
  require(policyCatalog.isNotBlank()) { "Policy catalog must not be blank" }
  return PolicyEngine(json.decodeFromString<PolicyCatalog>(policyCatalog))
}

/**
 * Represents the PolicyEngine, which is responsible for evaluating policies and conditions from a
 * PolicyCatalog.
 *
 * The PolicyEngine provides various methods to check conditions and evaluate policies. It is
 * initialized with a PolicyCatalog, which contains the definitions of the policies and conditions.
 *
 * The PolicyEngine uses a custom JSON serializer to handle the serialization and deserialization of
 * the PolicyCatalog.
 */
class PolicyEngine(private val policyCatalog: PolicyCatalog = EmptyPolicyCatalog()) {

  private val logger = LoggerFactory.getLogger(this::class.java)
  private val markerCondition = MarkerFactory.getMarker("PolicyCondition")
  private val markerPolicy = MarkerFactory.getMarker("Policy")
  private val markerEngine = MarkerFactory.getMarker("PolicyEngine")

  init {
    logger.info(markerEngine, "Loaded catalog {}:{}", policyCatalog.id, policyCatalog.version)
  }

  /**
   * Returns the version of the [PolicyCatalog] associated with this [PolicyEngine] instance.
   *
   * @return The version of the [PolicyCatalog] as a [CalVer] object.
   */
  fun policyCatalogVersion() = policyCatalog.idVer

  /**
   * Checks a policy condition by its ID and optional version from the associated [PolicyCatalog].
   *
   * This method logs the start and end of the condition check, and records the result in the
   * provided [Context]. If the condition is not found in the [PolicyCatalog], an error is logged
   * and `null` is returned.
   *
   * @param id The ID of the policy condition to check.
   * @param version The optional version of the policy condition to check.
   * @param context The [Context] in which to perform the condition check.
   * @return `true` if the condition is met, `false` if the condition is not met, or `null` if the
   *   condition was not found.
   */
  fun checkCondition(id: String, context: Context, version: SemVer? = null): Boolean? {
    logger.info(
        markerCondition,
        "${context.id}:Checking condition $id${version?.let { ":$it" } ?: ""} from catalog {}",
        policyCatalog.idVer)
    context.event.add(context.id, PolicyEntityEnum.ENGINE_START, policyCatalog.idVer, true)
    val condition = policyCatalog.getPolicyCondition(id, version)
    if (condition == null) {
      context.event.add(
          context.id,
          PolicyEntityEnum.POLICY_CATALOG,
          policyCatalog.idVer,
          false,
          null,
          false,
          "Condition $id${version?.let { ":$it" } ?: ""} not found")
      logger.error(
          markerCondition, "${context.id}:Condition $id${version?.let { ":$it" } ?: ""} not found")
      context.event.add(context.id, PolicyEntityEnum.ENGINE_END, policyCatalog.idVer, false)
      return null
    }
    return condition.check(context, policyCatalog).also {
      context.event.add(context.id, PolicyEntityEnum.ENGINE_END, policyCatalog.idVer, true, it)
      logger.info(markerCondition, "${context.id}:Condition ${condition.identity()} result: $it")
    }
  }

  /**
   * Checks the provided policy condition against the given context.
   *
   * This method logs the start and end of the condition check, and records the result in the
   * provided [Context]. The condition is checked using the [IPolicyCondition.check] method.
   *
   * @param condition The policy condition to check.
   * @param context The [Context] in which to perform the condition check.
   * @return `true` if the condition is met, `false` if the condition is not met.
   */
  fun checkCondition(condition: IPolicyCondition, context: Context): Boolean? {
    logger.info(
        markerCondition,
        "${context.id}:Checking custom condition with catalog {}",
        policyCatalog.idVer)
    context.event.add(context.id, PolicyEntityEnum.ENGINE_START, policyCatalog.idVer, true)
    return condition.check(context, policyCatalog).also {
      context.event.add(context.id, PolicyEntityEnum.ENGINE_END, policyCatalog.idVer, true, it)
      logger.info(markerCondition, "${context.id}:Custom condition result: $it")
    }
  }

  /**
   * Checks the policy conditions with the specified IDs.
   *
   * This method looks up the conditions in the associated [PolicyCatalog] and checks them against
   * the provided [Context]. If any of the conditions are not found in the catalog, an error is
   * logged and `null` is returned for that condition.
   *
   * @param ids The IDs of the conditions to check.
   * @param context The [Context] in which to perform the condition checks.
   * @return A map of condition IDs to their check results (`true` if the condition is met, `false`
   *   if the condition is not met, or `null` if the condition was not found).
   */
  fun checkConditionsByIds(ids: Set<String>, context: Context): Map<String, Boolean?> {
    logger.info(
        markerCondition,
        "${context.id}:Checking condition by ID list from catalog {}",
        policyCatalog.idVer)
    logger.trace(markerCondition, "${context.id}:Checking condition by ID list: {}", ids)
    context.event.add(context.id, PolicyEntityEnum.ENGINE_START, policyCatalog.idVer, true)
    for (id in ids) {
      val condition = policyCatalog.getPolicyCondition(id, null)
      if (condition == null) {
        context.event.add(
            context.id,
            PolicyEntityEnum.POLICY_CATALOG,
            policyCatalog.idVer,
            false,
            null,
            false,
            "Condition $id not found")
        logger.error(markerCondition, "${context.id}:Condition $id not found")
        context.addConditionResult(id, null)
      } else {
        val result =
            condition.check(context, policyCatalog).also {
              logger.debug(
                  markerCondition, "${context.id}:Condition ${condition.identity()} result: $it")
            }
        context.addConditionResult(id, result)
      }
      context.removeLastFromPath()
    }
    context.event.add(
        context.id,
        PolicyEntityEnum.ENGINE_END,
        policyCatalog.idVer,
        true,
        context.conditionResult())
    logger.info(markerCondition, "${context.id}:Condition check finished")
    return context.conditionResult()
  }

  /**
   * Checks the policy conditions with the specified references.
   *
   * This method looks up the conditions in the associated [PolicyCatalog] using the provided
   * references and checks them against the provided [Context]. If any of the conditions are not
   * found in the catalog, an error is logged and `null` is returned for that condition.
   *
   * @param refs The references of the conditions to check.
   * @param context The [Context] in which to perform the condition checks.
   * @return A map of condition references to their check results (`true` if the condition is met,
   *   `false` if the condition is not met, or `null` if the condition was not found).
   */
  fun checkConditionsByRefs(refs: Set<Reference>, context: Context): Map<String, Boolean?> {
    logger.info(
        markerCondition,
        "${context.id}:Checking condition by Ref list from catalog {}",
        policyCatalog.idVer)
    logger.trace(markerCondition, "${context.id}:Checking condition by Ref list: {}", refs)
    context.event.add(context.id, PolicyEntityEnum.ENGINE_START, policyCatalog.idVer, true)
    for (ref in refs) {
      val condition = policyCatalog.getPolicyCondition(ref.id, ref.version)
      val refId = "${ref.id}${ref.version?.let { ":$it" } ?: ""}"
      logger.trace(
          markerCondition,
          "${context.id}:returned condition {} for refId {}",
          condition?.identity(),
          refId)
      if (condition == null) {
        context.event.add(
            context.id,
            PolicyEntityEnum.POLICY_CATALOG,
            policyCatalog.idVer,
            false,
            null,
            false,
            "Condition $refId not found")
        logger.debug(markerCondition, "{}:Condition {} not found", context.id, ref)
        context.addConditionResult(refId, null)
      } else {
        val result =
            condition.check(context, policyCatalog).also {
              logger.debug(markerCondition, "{}:Condition {} result: {}", context.id, ref, it)
            }
        context.addConditionResult(refId, result)
      }
      context.removeLastFromPath()
    }
    context.event.add(
        context.id,
        PolicyEntityEnum.ENGINE_END,
        policyCatalog.idVer,
        true,
        context.conditionResult())
    logger.info(markerCondition, "${context.id}:Condition check finished")
    return context.conditionResult()
  }

  /**
   * Checks the conditions in the policy catalog that match the provided labels and logic.
   *
   * @param labels The set of labels to search for in the policy catalog.
   * @param logic The logic to use when searching for conditions by labels.
   * @param context The [Context] in which to perform the condition checks.
   * @return A map of condition references to their check results (`true` if the condition is met,
   *   `false` if the condition is not met, or `null` if the condition was not found).
   */
  fun checkConditionsByLabels(
      labels: Set<String>,
      logic: LabelSearchLogicEnum,
      context: Context
  ): Map<String, Boolean?> {
    logger.info(
        markerCondition,
        "${context.id}:Checking condition by labels:$labels with logic $logic from catalog {}",
        policyCatalog.idVer)
    context.event.add(context.id, PolicyEntityEnum.ENGINE_START, policyCatalog.idVer, true)
    val conditions = policyCatalog.searchConditionsByLabels(labels, logic)
    logger.info(markerCondition, "${context.id}:found {} conditions", conditions?.size ?: 0)
    logger.trace(markerCondition, "${context.id}:found conditions: {}", conditions)
    if (conditions.isNullOrEmpty()) {
      context.event.add(
          context.id,
          PolicyEntityEnum.POLICY_CATALOG,
          policyCatalog.idVer,
          false,
          null,
          false,
          "No conditions found")
    }
    for (condition in conditions!!) {
      val result =
          condition.check(context, policyCatalog).also {
            logger.debug(
                markerCondition, "${context.id}:Condition ${condition.identity()} result: $it")
          }
      context.addConditionResult(condition.identity(), result)
      context.removeLastFromPath()
    }
    context.event.add(
        context.id,
        PolicyEntityEnum.ENGINE_END,
        policyCatalog.idVer,
        true,
        context.conditionResult())
    logger.info(markerCondition, "${context.id}:Condition check finished")
    return context.conditionResult()
  }

  /**
   * Checks all conditions in the policy catalog and returns the results.
   *
   * @param context The [Context] in which to perform the condition checks.
   * @return A map of condition references to their check results (`true` if the condition is met,
   *   `false` if the condition is not met, or `null` if the condition was not found).
   */
  fun checkAllConditions(context: Context): Map<String, Boolean?> {
    logger.info(
        markerCondition,
        "${context.id}:Checking all conditions from catalog {}",
        policyCatalog.idVer)
    context.event.add(context.id, PolicyEntityEnum.ENGINE_START, policyCatalog.idVer, true)
    val conditions = policyCatalog.getAllConditions()
    logger.info(markerCondition, "${context.id}:found {} conditions", conditions.size)
    logger.trace(markerCondition, "${context.id}:found conditions: {}", conditions)
    if (conditions.isEmpty()) {
      context.event.add(
          context.id,
          PolicyEntityEnum.POLICY_CATALOG,
          policyCatalog.idVer,
          false,
          null,
          false,
          "No conditions found")
    }
    for (condition in conditions) {
      val result =
          condition.check(context, policyCatalog).also {
            logger.debug(
                markerCondition, "${context.id}:Condition ${condition.identity()} result: $it")
          }
      context.addConditionResult(condition.identity(), result)
      context.removeLastFromPath()
    }
    context.event.add(
        context.id,
        PolicyEntityEnum.ENGINE_END,
        policyCatalog.idVer,
        true,
        context.conditionResult())
    logger.info(markerCondition, "${context.id}:Condition check finished")
    return context.conditionResult()
  }

  // policies

  /**
   * Evaluates a policy from the policy catalog and returns the result.
   *
   * @param id The ID of the policy to evaluate.
   * @param context The context in which to evaluate the policy.
   * @param version The version of the policy to evaluate, or `null` to use the latest version.
   * @return A pair containing the policy result enum and the action result, if any.
   */
  fun evaluatePolicy(
      id: String,
      context: Context,
      version: SemVer? = null,
  ): Pair<PolicyResultEnum, ActionResult?> {
    logger.info(
        markerPolicy,
        "${context.id}:Evaluating policy $id${version?.let { ":$it" } ?: ""} from catalog {}",
        policyCatalog.idVer)
    context.event.add(context.id, PolicyEntityEnum.ENGINE_START, policyCatalog.idVer, true)
    val policy = policyCatalog.getPolicy(id, version)
    if (policy == null) {
      context.event.add(
          context.id,
          PolicyEntityEnum.POLICY_CATALOG,
          policyCatalog.idVer,
          false,
          null,
          false,
          "Policy $id${version?.let { ":$it" } ?: ""} not found")
      logger.error(
          markerPolicy, "${context.id}:Policy $id${version?.let { ":$it" } ?: ""} not found")
      context.event.add(context.id, PolicyEntityEnum.ENGINE_END, policyCatalog.idVer, false)
      return Pair(PolicyResultEnum.INDETERMINATE_DENY_PERMIT, null)
    }
    val executionResult = executePolicy(policy, context)

    context.event.add(
        context.id, PolicyEntityEnum.ENGINE_END, policyCatalog.idVer, true, executionResult)
    return executionResult
  }

  /**
   * Evaluates a policy and returns the result.
   *
   * @param policy The policy to evaluate.
   * @param context The context in which to evaluate the policy.
   * @return A pair containing the policy result enum and the action result, if any.
   */
  fun evaluatePolicy(
      policy: IPolicy,
      context: Context,
  ): Pair<PolicyResultEnum, ActionResult?> {
    logger.info(
        markerPolicy, "${context.id}:Evaluating custom policy with catalog {}", policyCatalog.idVer)
    context.event.add(context.id, PolicyEntityEnum.ENGINE_START, policyCatalog.idVer, true)
    val executionResult = executePolicy(policy, context)

    context.event.add(
        context.id, PolicyEntityEnum.ENGINE_END, policyCatalog.idVer, true, executionResult)
    return executionResult
  }

  /**
   * Evaluates a set of policies by their IDs and returns a map of policy results.
   *
   * @param ids The set of policy IDs to evaluate.
   * @param context The context in which to evaluate the policies.
   * @return A map of policy IDs to their corresponding policy result and action result, if any.
   */
  fun evaluatePoliciesByIds(
      ids: Set<String>,
      context: Context
  ): Map<String, Pair<PolicyResultEnum, ActionResult?>> {
    logger.info(
        markerPolicy,
        "${context.id}:Evaluating policies by ID list from catalog {}",
        policyCatalog.idVer)
    logger.trace(markerPolicy, "${context.id}:Checking policies by ID list: {}", ids)
    context.event.add(context.id, PolicyEntityEnum.ENGINE_START, policyCatalog.idVer, true)
    val resolvedPolicies = emptyList<IPolicy>().toMutableList()
    for (id in ids) {
      val policy = policyCatalog.getPolicy(id, null)
      if (policy == null) {
        context.event.add(
            context.id,
            PolicyEntityEnum.POLICY_CATALOG,
            policyCatalog.idVer,
            false,
            null,
            false,
            "Policy $id not found")
        logger.error(markerPolicy, "${context.id}:Policy $id not found")
        context.addPolicyResult(id, Pair(PolicyResultEnum.INDETERMINATE_DENY_PERMIT, null))
      } else {
        resolvedPolicies.add(policy)
      }
    }
    resolvedPolicies
        .sortedByDescending { it.priority ?: DEFAULT_PRIORITY }
        .forEach { context.addPolicyResult(it.id!!, executePolicy(it, context)) }
    context.event.add(
        context.id, PolicyEntityEnum.ENGINE_END, policyCatalog.idVer, true, context.policyResult())
    logger.info(markerPolicy, "${context.id}:Policy evaluation finished")
    return context.policyResult()
  }

  /**
   * Evaluates a set of policies by their references and returns a map of policy results.
   *
   * @param refs The set of policy references to evaluate.
   * @param context The context in which to evaluate the policies.
   * @return A map of policy references to their corresponding policy result and action result, if
   *   any.
   */
  fun evaluatePoliciesByRefs(
      refs: Set<Reference>,
      context: Context
  ): Map<String, Pair<PolicyResultEnum, ActionResult?>> {
    logger.info(
        markerPolicy,
        "${context.id}:Evaluating policies by Ref list from catalog {}",
        policyCatalog.idVer)
    logger.trace(markerPolicy, "${context.id}:Checking policies by Ref list: {}", refs)
    context.event.add(context.id, PolicyEntityEnum.ENGINE_START, policyCatalog.idVer, true)
    val resolvedPolicies = emptyList<IPolicy>().toMutableList()
    for (ref in refs) {
      val policy = policyCatalog.getPolicy(ref.id, ref.version)
      val refId = "${ref.id}${ref.version?.let { ":$it" } ?: ""}"
      if (policy == null) {
        context.event.add(
            context.id,
            PolicyEntityEnum.POLICY_CATALOG,
            policyCatalog.idVer,
            false,
            null,
            false,
            "Policy $refId not found")
        logger.error(markerPolicy, "${context.id}:Policy $refId not found")
        context.addPolicyResult(refId, Pair(PolicyResultEnum.INDETERMINATE_DENY_PERMIT, null))
      } else {
        resolvedPolicies.add(policy)
      }
    }
    resolvedPolicies
        .sortedByDescending { it.priority ?: DEFAULT_PRIORITY }
        .forEach { p ->
          val refId = "${p.id}${p.version?.let { ":$it" } ?: ""}"
          context.addPolicyResult(refId, executePolicy(p, context))
        }
    context.event.add(
        context.id, PolicyEntityEnum.ENGINE_END, policyCatalog.idVer, true, context.policyResult())
    logger.info(markerPolicy, "${context.id}:Policy evaluation finished")
    return context.policyResult()
  }

  /**
   * Evaluates a set of policies by their labels and returns a map of policy results.
   *
   * @param labels The set of policy labels to evaluate.
   * @param logic The logic to use when searching for policies by labels.
   * @param context The context in which to evaluate the policies.
   * @return A map of policy references to their corresponding policy result and action result, if
   *   any.
   */
  fun evaluatePoliciesByLabels(
      labels: Set<String>,
      logic: LabelSearchLogicEnum,
      context: Context
  ): Map<String, Pair<PolicyResultEnum, ActionResult?>> {
    logger.info(
        markerPolicy,
        "${context.id}:Evaluating policies by labels from catalog {}",
        policyCatalog.idVer)
    logger.trace(
        markerPolicy,
        "{}:Evaluating policies by labels:{} with logic {} from catalog {}",
        context.id,
        labels,
        logic,
        policyCatalog.idVer)
    context.event.add(context.id, PolicyEntityEnum.ENGINE_START, policyCatalog.idVer, true)
    val policies = policyCatalog.searchPoliciesByLabels(labels, logic)

    policies
        ?.sortedByDescending { it.priority ?: DEFAULT_PRIORITY }
        ?.forEach { p -> context.addPolicyResult(p.identity(), executePolicy(p, context)) }
    context.event.add(
        context.id, PolicyEntityEnum.ENGINE_END, policyCatalog.idVer, true, context.policyResult())
    logger.info(markerPolicy, "${context.id}:Policy evaluation finished")
    return context.policyResult()
  }

  /**
   * Evaluates all policies from the policy catalog and returns a map of policy results.
   *
   * This method iterates through all policies in the policy catalog, evaluates each policy, and
   * returns a map of policy references to their corresponding policy result and action result.
   *
   * @param context The context in which to evaluate the policies.
   * @return A map of policy references to their corresponding policy result and action result, if
   *   any.
   */
  fun evaluateAllPolicies(context: Context): Map<String, Pair<PolicyResultEnum, ActionResult?>> {
    logger.info(
        markerPolicy, "${context.id}:Evaluating all policies from catalog {}", policyCatalog.idVer)
    logger.trace(
        markerPolicy, "{}:Evaluating all policies from catalog {}", context.id, policyCatalog.idVer)
    context.event.add(context.id, PolicyEntityEnum.ENGINE_START, policyCatalog.idVer, true)
    val policies = policyCatalog.getAllPolicies()

    policies
        .sortedByDescending { it.priority ?: DEFAULT_PRIORITY }
        .forEach { p -> context.addPolicyResult(p.identity(), executePolicy(p, context)) }
    context.event.add(
        context.id, PolicyEntityEnum.ENGINE_END, policyCatalog.idVer, true, context.policyResult())
    logger.info(markerPolicy, "${context.id}:Policy evaluation finished")
    return context.policyResult()
  }

  /**
   * Executes the specified policy and returns the policy result and action result.
   *
   * This method evaluates the given policy using the provided context and policy catalog, and then
   * runs the actions defined in the policy. The policy result and action result are returned as a
   * pair.
   *
   * @param policy The policy to execute.
   * @param context The context in which to execute the policy.
   * @return A pair containing the policy result and action result.
   */
  private fun executePolicy(
      policy: IPolicy,
      context: Context
  ): Pair<PolicyResultEnum, ActionResult?> {
    val policyResult = policy.evaluate(context, policyCatalog)
    logger.info(markerPolicy, "${context.id}:Policy ${policy.identity()} result: $policyResult")

    val actionResult = policy.runActions(context, policyCatalog, policyResult)
    logger.info(
        markerPolicy, "${context.id}:PolicyAction ${policy.identity()} result: $actionResult")
    context.removeLastFromPath()
    return Pair(policyResult, actionResult)
  }
}
