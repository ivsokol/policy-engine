package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.*
import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.catalog.getRefValueFromCatalog
import kotlin.properties.Delegates
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

private const val NEGATE_RESULT_DEFAULT = false
private const val OPTIMIZE_N_OF_RUN_DEFAULT = false
private const val STRICT_CHECK_DEFAULT = true

/**
 * Represents a composite policy condition that combines multiple policy conditions using a
 * specified logic.
 *
 * @property id The unique identifier of the policy condition composite.
 * @property version The version of the policy condition composite.
 * @property description The description of the policy condition composite.
 * @property labels The labels associated with the policy condition composite.
 * @property negateResult Whether the result of the policy condition composite should be negated.
 *   Default value is false.
 * @property conditionCombinationLogic The logic used to combine the individual policy conditions.
 * @property conditions The list of policy conditions that make up the composite.
 * @property minimumConditions The minimum number of conditions that must be met for the N_OF
 *   combination logic.
 * @property optimizeNOfRun Whether to optimize the N_OF combination logic. Default value is false.
 * @property strictCheck Whether to perform strict checking of the condition combination logic.
 *   Default value is true.
 */
@Serializable
@SerialName("PolicyConditionComposite")
data class PolicyConditionComposite(
    override val id: String? = null,
    override val version: SemVer? = null,
    override val description: String? = null,
    override val labels: List<String>? = null,
    override val negateResult: Boolean? = null,
    val conditionCombinationLogic: ConditionCombinationLogicEnum,
    val conditions: List<IPolicyConditionRefOrValue>,
    val minimumConditions: Int? = null,
    val optimizeNOfRun: Boolean? = null,
    val strictCheck: Boolean? = null,
) : IPolicyCondition {
  @Transient private val logger = LoggerFactory.getLogger(this::class.java)
  @Transient private val marker = MarkerFactory.getMarker("PolicyCondition")
  @Transient private val idVer: String = if (version != null) "$id:$version" else id ?: ""
  @Transient private var _childRefs: MutableSet<PolicyEntityRefItem> = mutableSetOf()

  private var _optimizeNOfRun by Delegates.notNull<Boolean>()
  private var _negateResult by Delegates.notNull<Boolean>()
  private var _strictCheck by Delegates.notNull<Boolean>()

  init {
    this.validateId()
    labels?.also { require(it.isNotEmpty()) { "$idVer:Labels must not be empty array" } }
    _optimizeNOfRun = optimizeNOfRun ?: OPTIMIZE_N_OF_RUN_DEFAULT
    _negateResult = negateResult ?: NEGATE_RESULT_DEFAULT
    _strictCheck = strictCheck ?: STRICT_CHECK_DEFAULT

    require(conditions.isNotEmpty()) { "$idVer:Conditions must not be empty array" }

    /**
     * Validates the minimum conditions for the N_OF condition combination logic.
     * - Ensures the minimumConditions property is not null.
     * - Ensures the minimumConditions is greater than 0.
     * - Ensures the minimumConditions is less than or equal to the number of conditions.
     */
    if (conditionCombinationLogic == ConditionCombinationLogicEnum.N_OF) {
      requireNotNull(minimumConditions) { "$idVer:Minimum conditions must not be null" }
      require(minimumConditions > 0) { "$idVer:Minimum conditions must be greater than 0" }
      require(minimumConditions <= conditions.size) {
        "$idVer:Minimum conditions must be less than or equal to number of conditions"
      }
    }
    /**
     * Validates that the `NOT` condition combination logic has exactly one condition. The `NOT`
     * condition combination logic requires a single condition to negate.
     */
    if (conditionCombinationLogic == ConditionCombinationLogicEnum.NOT) {
      require(conditions.size == 1) { "$idVer:NOT condition must have exactly one condition" }
    }

    /**
     * Iterates through the conditions list and adds any PolicyConditionRef instances to the
     * _childRefs set. For any IPolicyCondition instances, it also adds their child references to
     * the _childRefs set. If any unsupported condition types are encountered, an error is thrown.
     */
    conditions.forEachIndexed { idx, it ->
      when (it) {
        is PolicyConditionRef ->
            _childRefs.add(
                PolicyEntityRefItem(PolicyEntityRefEnum.POLICY_CONDITION_REF, it.id, it.version))
        is IPolicyCondition -> it.childRefs()?.also { c -> _childRefs.addAll(c) }
        else -> error("$idVer: Unsupported type for condition[$idx] ${it::class.java.simpleName}")
      }
    }
  }

  /**
   * Checks the policy condition composite and returns a boolean result based on the configured
   * condition combination logic.
   *
   * This method first checks the cache for a previously computed result. If a cached result is
   * found, it is returned.
   *
   * If no cached result is found, the method applies the configured condition combination logic
   * (ANY_OF, ALL_OF, NOT, N_OF) and computes the result. The result is then negated if the
   * `_negateResult` flag is set.
   *
   * If an exception is thrown during the check, the exception details are added to the context
   * event and the method returns `null`.
   *
   * The computed result is cached and returned.
   *
   * @param context The current evaluation context.
   * @param policyCatalog The policy catalog used to resolve condition references.
   * @return The computed boolean result, or `null` if an exception occurred.
   */
  override fun check(context: Context, policyCatalog: PolicyCatalog): Boolean? {
    val idVerPath = context.getFullPath(idVer)
    logger.debug(marker, "${context.id}->$idVerPath:Checking PolicyConditionComposite")
    // check cache
    val cached = getFromCache(context, idVer, idVerPath, logger, marker)
    // if found in cache, return from it
    if (cached.first) return cached.second

    // apply logic and negate if needed
    val result =
        try {
          when (conditionCombinationLogic) {
            ConditionCombinationLogicEnum.ANY_OF -> handleAnyOf(context, policyCatalog, idVerPath)
            ConditionCombinationLogicEnum.ALL_OF -> handleAllOf(context, policyCatalog, idVerPath)
            ConditionCombinationLogicEnum.NOT -> handleNot(context, policyCatalog, idVerPath)
            ConditionCombinationLogicEnum.N_OF -> handleNOf(context, policyCatalog, idVerPath)
          }?.let { if (_negateResult) !it else it }
        } catch (e: Throwable) {
          // add to event
          context.event.add(
              context.id,
              PolicyEntityEnum.CONDITION_COMPOSITE,
              idVerPath,
              false,
              null,
              false,
              "${e::class.java.name}:${e.message}")
          // log
          logger.error(
              marker, "${context.id}->$idVerPath:operation threw an exception: {}", e.message, e)
          return cacheAndReturn(idVer, context, null, idVerPath, logger, marker)
        }
    return cacheAndReturn(idVer, context, result, idVerPath, logger, marker)
  }

  /**
   * Checks the policy condition composite by negating the result of the condition.
   *
   * This method retrieves condition from the [conditions] list, checks it using the provided
   * [policyCatalog], and then negates the result.
   *
   * If an exception is thrown during the check, the exception details are added to the
   * [context].event and the method returns `null`.
   *
   * @param context The current evaluation context.
   * @param policyCatalog The policy catalog used to resolve condition references.
   * @param idVerPath The full path of the policy condition composite.
   * @return The negated boolean result of the first condition, or `null` if an exception occurred.
   */
  private fun handleNot(
      context: Context,
      policyCatalog: PolicyCatalog,
      idVerPath: String
  ): Boolean? {
    context.addToPath("conditions")
    context.addToPath("0")
    val result =
        getRefValueFromCatalog<IPolicyConditionRefOrValue, IPolicyCondition, PolicyConditionRef>(
                conditions.first(),
                policyCatalog,
                idVerPath,
                context,
                PolicyEntityEnum.CONDITION_COMPOSITE,
                logger,
                marker)
            ?.check(context, policyCatalog)
    context.removeLastFromPath() // 0
    context.removeLastFromPath() // conditions
    return result?.not()
  }

  /**
   * Checks the policy condition composite by evaluating the "any of" condition.
   *
   * This method retrieves conditions from the [conditions] list, checks each one using the provided
   * [policyCatalog], and returns `true` if any of the conditions evaluate to `true`. If all
   * conditions evaluate to `false`, it returns `false`. If any condition evaluates to `null`, it
   * returns `null`.
   *
   * If an exception is thrown during the check, the exception details are added to the
   * [context].event and the method returns `null`.
   *
   * @param context The current evaluation context.
   * @param policyCatalog The policy catalog used to resolve condition references.
   * @param idVerPath The full path of the policy condition composite.
   * @return `true` if any of the conditions evaluate to `true`, `false` if all conditions evaluate
   *   to `false`, or `null` if any condition evaluates to `null` or an exception occurred.
   */
  private fun handleAnyOf(
      context: Context,
      policyCatalog: PolicyCatalog,
      idVerPath: String
  ): Boolean? {
    var hasNull = false
    context.addToPath("conditions")
    for ((idx, it) in conditions.withIndex()) {
      context.addToPath("$idx")
      val result =
          getRefValueFromCatalog<IPolicyConditionRefOrValue, IPolicyCondition, PolicyConditionRef>(
                  it,
                  policyCatalog,
                  idVerPath,
                  context,
                  PolicyEntityEnum.CONDITION_COMPOSITE,
                  logger,
                  marker)
              ?.check(context, policyCatalog)
      context.removeLastFromPath() // $idx
      if (result == true) return true.also { context.removeLastFromPath() }
      if (result == null) hasNull = true
    }
    context.removeLastFromPath() // conditions
    return if (hasNull && _strictCheck) {
      null
    } else {
      false
    }
  }

  /**
   * Checks the policy condition composite by evaluating the "all of" condition.
   *
   * This method retrieves conditions from the [conditions] list, checks each one using the provided
   * [policyCatalog], and returns `true` if all the conditions evaluate to `true`. If any condition
   * evaluates to `false`, it returns `false`. If any condition evaluates to `null`, it returns
   * `null`.
   *
   * If an exception is thrown during the check, the exception details are added to the
   * [context].event and the method returns `null`.
   *
   * @param context The current evaluation context.
   * @param policyCatalog The policy catalog used to resolve condition references.
   * @param idVerPath The full path of the policy condition composite.
   * @return `true` if all the conditions evaluate to `true`, `false` if any condition evaluates to
   *   `false`, or `null` if any condition evaluates to `null` or an exception occurred.
   */
  private fun handleAllOf(
      context: Context,
      policyCatalog: PolicyCatalog,
      idVerPath: String
  ): Boolean? {
    var hasNull = false
    context.addToPath("conditions")
    for ((idx, it) in conditions.withIndex()) {
      context.addToPath("$idx")
      val result =
          getRefValueFromCatalog<IPolicyConditionRefOrValue, IPolicyCondition, PolicyConditionRef>(
                  it,
                  policyCatalog,
                  idVerPath,
                  context,
                  PolicyEntityEnum.CONDITION_COMPOSITE,
                  logger,
                  marker)
              ?.check(context, policyCatalog)
      context.removeLastFromPath() // $idx
      if (result == false) return false.also { context.removeLastFromPath() }
      if (result == null) hasNull = true
    }
    context.removeLastFromPath() // conditions
    return if (hasNull && _strictCheck) {
      null
    } else {
      true
    }
  }

  /**
   * Checks the policy condition composite by evaluating the "n of" condition.
   *
   * This method retrieves conditions from the [conditions] list, checks each one using the provided
   * [policyCatalog], and returns `true` if at least [minimumConditions] conditions evaluate to
   * `true`. If less than [minimumConditions] conditions evaluate to `true`, it returns `false`. If
   * any condition evaluates to `null`, it returns `null`.
   *
   * If an exception is thrown during the check, the exception details are added to the
   * [context].event and the method returns `null`.
   *
   * @param context The current evaluation context.
   * @param policyCatalog The policy catalog used to resolve condition references.
   * @param idVerPath The full path of the policy condition composite.
   * @return `true` if at least [minimumConditions] conditions evaluate to `true`, `false` if less
   *   than [minimumConditions] conditions evaluate to `true`, or `null` if any condition evaluates
   *   to `null` or an exception occurred.
   */
  private fun handleNOf(
      context: Context,
      policyCatalog: PolicyCatalog,
      idVerPath: String
  ): Boolean? {
    var trueCounter = 0
    var falseCounter = 0
    var nullCounter = 0
    context.addToPath("conditions")
    for ((idx, it) in conditions.withIndex()) {
      context.addToPath("$idx")
      val result =
          getRefValueFromCatalog<IPolicyConditionRefOrValue, IPolicyCondition, PolicyConditionRef>(
                  it,
                  policyCatalog,
                  idVerPath,
                  context,
                  PolicyEntityEnum.CONDITION_COMPOSITE,
                  logger,
                  marker)
              ?.check(context, policyCatalog)
      context.removeLastFromPath() // $idx
      if (result == true) trueCounter++
      if (trueCounter >= minimumConditions!!) return true.also { context.removeLastFromPath() }
      if (result == false) falseCounter++
      if (result == null) nullCounter++
      // if falseCounter >= conditions.size - minimumConditions + 1, return false, as true is never
      // possible
      if (falseCounter >= conditions.size - minimumConditions + 1)
          return false.also { context.removeLastFromPath() }
      // if nullCounter >= conditions.size - minimumConditions + 1, return null, as true is never
      // possible
      if (nullCounter >= conditions.size - minimumConditions + 1)
          return null.also { context.removeLastFromPath() }
      // if optimizeNOfRun and falseCounter + nullCounter >= conditions.size - minimumConditions +
      // 1, return null as true is never possible. False is possible if conditions are run until end
      // (non optimized run)
      if (_optimizeNOfRun &&
          (falseCounter + nullCounter >= conditions.size - minimumConditions + 1))
          return null.also { context.removeLastFromPath() }
    }
    context.removeLastFromPath() // conditions
    return null
  }

  /**
   * Returns the unique identifier and version of this policy condition composite.
   *
   * @return The unique identifier and version of this policy condition composite.
   */
  override fun identity(): String = idVer

  /**
   * Returns a set of child policy entity references for this policy condition composite.
   *
   * @return A set of child policy entity references, or `null` if there are no child references.
   */
  override fun childRefs(): Set<PolicyEntityRefItem>? =
      if (_childRefs.isNotEmpty()) _childRefs.toSet() else null
}
