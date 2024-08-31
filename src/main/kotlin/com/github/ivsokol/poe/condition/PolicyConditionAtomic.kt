package com.github.ivsokol.poe.condition

import com.github.ivsokol.poe.*
import com.github.ivsokol.poe.catalog.PolicyCatalog
import com.github.ivsokol.poe.catalog.getRefValueFromCatalog
import com.github.ivsokol.poe.variable.*
import kotlin.properties.Delegates
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

private const val UNREACHABLE_CODE_ERROR = "Unreachable code"
private const val STRING_IGNORE_CASE_DEFAULT = false
private const val FIELDS_STRICT_CHECK_DEFAULT = false
private const val ARRAY_ORDER_STRICT_CHECK_DEFAULT = false
private const val NEGATE_RESULT_DEFAULT = false

@Serializable
@SerialName("PolicyConditionAtomic")
/**
 * A data class representing an atomic policy condition in the Policy Evaluation (POE) system.
 *
 * This class encapsulates the logic for evaluating a single policy condition, including the
 * operation to perform, the arguments to the operation, and various configuration options.
 *
 * The class provides methods for resolving variable references, performing type-safe comparisons,
 * and caching the results of condition evaluations. It also handles the logic for negating the
 * result of the condition evaluation if the `negateResult` flag is set.
 *
 * The class is serializable and can be used as part of a larger policy definition.
 *
 * @property id The unique identifier of the policy condition.
 * @property version The version of the policy condition.
 * @property description The description of the policy condition.
 * @property labels The labels associated with the policy condition.
 * @property negateResult Whether the result of the policy condition should be negated. Default
 *   value is false.
 * @property operation The operation to perform on the condition arguments.
 * @property args The arguments to the operation.
 * @property stringIgnoreCase Whether to ignore case when comparing strings. Default value is false.
 * @property fieldsStrictCheck Whether to perform strict checking of field names. Default value is
 *   false.
 * @property arrayOrderStrictCheck Whether to perform strict checking of array order. Default value
 *   is false.
 */
data class PolicyConditionAtomic(
    override val id: String? = null,
    override val version: SemVer? = null,
    override val description: String? = null,
    override val labels: List<String>? = null,
    override val negateResult: Boolean? = null,
    val operation: OperationEnum,
    val args: List<IPolicyVariableRefOrValue>,
    val stringIgnoreCase: Boolean? = null,
    val fieldsStrictCheck: Boolean? = null,
    val arrayOrderStrictCheck: Boolean? = null,
) : IPolicyCondition {
  @Transient private val logger = LoggerFactory.getLogger(this::class.java)
  @Transient private val marker = MarkerFactory.getMarker("PolicyCondition")
  @Transient private val idVer: String = if (version != null) "$id:$version" else id ?: ""

  @Transient private var _childRefs: MutableSet<PolicyEntityRefItem> = mutableSetOf()

  private var _negateResult by Delegates.notNull<Boolean>()
  private var _stringIgnoreCase by Delegates.notNull<Boolean>()
  private var _fieldsStrictCheck by Delegates.notNull<Boolean>()
  private var _arrayOrderStrictCheck by Delegates.notNull<Boolean>()

  init {
    this.validateId()
    labels?.also { require(it.isNotEmpty()) { "$idVer:Labels must not be empty array" } }
    _negateResult = negateResult ?: NEGATE_RESULT_DEFAULT
    _stringIgnoreCase = stringIgnoreCase ?: STRING_IGNORE_CASE_DEFAULT
    _fieldsStrictCheck = fieldsStrictCheck ?: FIELDS_STRICT_CHECK_DEFAULT
    _arrayOrderStrictCheck = arrayOrderStrictCheck ?: ARRAY_ORDER_STRICT_CHECK_DEFAULT

    /**
     * Performs type-safe comparisons and checks based on the operation specified in the
     * PolicyConditionAtomic instance. The number of arguments required for the operation is
     * validated, ensuring that the condition has the correct number of arguments. For operations
     * that require two arguments, the condition must have exactly two arguments. For operations
     * that require one argument, the condition must have exactly one argument.
     */
    when (operation) {
      OperationEnum.EQUALS,
      OperationEnum.GREATER_THAN,
      OperationEnum.GREATER_THAN_EQUAL,
      OperationEnum.LESS_THAN,
      OperationEnum.LESS_THAN_EQUAL,
      OperationEnum.STARTS_WITH,
      OperationEnum.ENDS_WITH,
      OperationEnum.CONTAINS,
      OperationEnum.IS_IN,
      OperationEnum.REGEXP_MATCH,
      OperationEnum.HAS_KEY,
      OperationEnum.SCHEMA_MATCH ->
          require(args.size == 2) { "$idVer:condition must have exactly 2 arguments" }
      OperationEnum.IS_NULL,
      OperationEnum.IS_NOT_NULL,
      OperationEnum.IS_EMPTY,
      OperationEnum.IS_NOT_EMPTY,
      OperationEnum.IS_BLANK,
      OperationEnum.IS_NOT_BLANK,
      OperationEnum.IS_POSITIVE,
      OperationEnum.IS_NEGATIVE,
      OperationEnum.IS_ZERO,
      OperationEnum.IS_PAST,
      OperationEnum.IS_FUTURE,
      OperationEnum.IS_UNIQUE ->
          require(args.size == 1) { "$idVer:condition must have exactly 1 argument" }
    }

    /**
     * Iterates over the args list and adds any PolicyVariableRef or IPolicyVariable instances to
     * the _childRefs set. For any unsupported argument types, an error is thrown.
     */
    args.forEachIndexed { idx, it ->
      when (it) {
        is PolicyVariableRef ->
            _childRefs.add(
                PolicyEntityRefItem(PolicyEntityRefEnum.POLICY_VARIABLE_REF, it.id, it.version))
        is IPolicyVariable -> it.childRefs()?.also { c -> _childRefs.addAll(c) }
        else -> error("$idVer: Unsupported type for argument[$idx] ${it::class.java.simpleName}")
      }
    }
  }

  /**
   * Checks the condition specified by the [PolicyConditionAtomic] instance against the provided
   * [Context] and [PolicyCatalog]. This method resolves the variables, performs type-safe
   * comparisons and checks based on the operation, and caches the result. It handles null checks,
   * compliance checks, casting of input values, and applying the logic with optional negation. The
   * method returns the result of the condition check, or null if an exception occurs during the
   * check.
   *
   * @param context The [Context] object containing the variables and other information needed for
   *   the condition check.
   * @param policyCatalog The [PolicyCatalog] object containing the policy-related data.
   * @return The result of the condition check, or null if an exception occurs.
   */
  override fun check(context: Context, policyCatalog: PolicyCatalog): Boolean? {
    val idVerPath = context.getFullPath(idVer)
    logger.debug(marker, "${context.id}->$idVerPath:Checking PolicyConditionAtomic")
    // check cache
    val cached = getFromCache(context, idVer, idVerPath, logger, marker)
    // if found in cache, return from it
    if (cached.first) return cached.second

    // resolve variables
    context.addToPath("args")
    val inputValues = getFromVariables(context, policyCatalog, idVerPath)
    context.removeLastFromPath()

    // for null checks, return immediately
    if (operation == OperationEnum.IS_NULL || operation == OperationEnum.IS_NOT_NULL) {
      return if (operation == OperationEnum.IS_NULL)
          cacheAndReturn(idVer, context, isNull(inputValues[0]), idVerPath, logger, marker)
      else cacheAndReturn(idVer, context, isNotNull(inputValues[0]), idVerPath, logger, marker)
    }

    // check for null values
    if (operation != OperationEnum.SCHEMA_MATCH &&
        inputValues.any { it.type == VariableRuntimeTypeEnum.NULL }) {
      // log
      logger.debug(marker, "${context.id}->$idVerPath:inputValues are not resolved")
      return cacheAndReturn(idVer, context, null, idVerPath, logger, marker)
    }

    // run compliance checks
    try {
      runComplianceChecks(inputValues, idVerPath)
    } catch (e: Exception) {
      // add to event
      context.event.add(
          context.id,
          PolicyEntityEnum.CONDITION_ATOMIC,
          idVerPath,
          false,
          null,
          false,
          "${e::class.java.name}:${e.message}")
      // log
      logger.error(
          marker,
          "${context.id}->$idVerPath:compliant type checking threw an exception: {}",
          e.message,
          e)
      return cacheAndReturn(idVer, context, null, idVerPath, logger, marker)
    }

    // cast other values to first value for comparisons. Response contains operation failure info
    when (operation) {
      OperationEnum.EQUALS,
      OperationEnum.GREATER_THAN,
      OperationEnum.GREATER_THAN_EQUAL,
      OperationEnum.LESS_THAN,
      OperationEnum.LESS_THAN_EQUAL -> {
        if (!castToFirst(inputValues, context, idVerPath))
            return cacheAndReturn(idVer, context, null, idVerPath, logger, marker)
      }
      else -> Unit
    }

    // for IsIn revert inputs so Contains logic can be used
    if (operation == OperationEnum.IS_IN) {
      inputValues.reverse()
    }

    when (operation) {
      OperationEnum.STARTS_WITH,
      OperationEnum.ENDS_WITH,
      OperationEnum.CONTAINS,
      OperationEnum.IS_IN -> {
        // if left is string, cast right to string
        if (inputValues[0].type == VariableRuntimeTypeEnum.STRING &&
            !castToFirst(inputValues, context, idVerPath))
            return cacheAndReturn(idVer, context, null, idVerPath, logger, marker)
      }
      else -> Unit
    }

    // apply logic and negate if needed
    val result =
        try {
          when (operation) {
            OperationEnum.EQUALS ->
                eq(
                    inputValues[0],
                    inputValues[1],
                    _stringIgnoreCase,
                    _fieldsStrictCheck,
                    _arrayOrderStrictCheck,
                    context,
                    identityOrPath(idVerPath))
            OperationEnum.GREATER_THAN ->
                gt(inputValues[0], inputValues[1], _stringIgnoreCase, identityOrPath(idVerPath))
            OperationEnum.GREATER_THAN_EQUAL ->
                gte(inputValues[0], inputValues[1], _stringIgnoreCase, identityOrPath(idVerPath))
            OperationEnum.LESS_THAN ->
                lt(inputValues[0], inputValues[1], _stringIgnoreCase, identityOrPath(idVerPath))
            OperationEnum.LESS_THAN_EQUAL ->
                lte(inputValues[0], inputValues[1], _stringIgnoreCase, identityOrPath(idVerPath))
            OperationEnum.IS_BLANK -> isBlank(inputValues[0], identityOrPath(idVerPath))
            OperationEnum.IS_NOT_BLANK -> isNotBlank(inputValues[0], identityOrPath(idVerPath))
            OperationEnum.IS_EMPTY -> isEmpty(inputValues[0], identityOrPath(idVerPath))
            OperationEnum.IS_NOT_EMPTY -> isNotEmpty(inputValues[0], identityOrPath(idVerPath))
            OperationEnum.STARTS_WITH ->
                startsWith(
                    inputValues[0],
                    inputValues[1],
                    _stringIgnoreCase,
                    _fieldsStrictCheck,
                    _arrayOrderStrictCheck,
                    context,
                    identityOrPath(idVerPath))
            OperationEnum.ENDS_WITH ->
                endsWith(
                    inputValues[0],
                    inputValues[1],
                    _stringIgnoreCase,
                    _fieldsStrictCheck,
                    _arrayOrderStrictCheck,
                    context,
                    identityOrPath(idVerPath))
            OperationEnum.CONTAINS,
            OperationEnum.IS_IN ->
                contains(
                    inputValues[0],
                    inputValues[1],
                    _stringIgnoreCase,
                    _fieldsStrictCheck,
                    _arrayOrderStrictCheck,
                    context,
                    identityOrPath(idVerPath))
            OperationEnum.IS_POSITIVE -> isPositive(inputValues[0], identityOrPath(idVerPath))
            OperationEnum.IS_NEGATIVE -> isNegative(inputValues[0], identityOrPath(idVerPath))
            OperationEnum.IS_ZERO -> isZero(inputValues[0], identityOrPath(idVerPath))
            OperationEnum.IS_PAST -> isPast(inputValues[0], context, identityOrPath(idVerPath))
            OperationEnum.IS_FUTURE -> isFuture(inputValues[0], context, identityOrPath(idVerPath))
            OperationEnum.REGEXP_MATCH ->
                matchesRegexp(inputValues[0], inputValues[1], context.options)
            OperationEnum.HAS_KEY ->
                hasKey(inputValues[0], inputValues[1], identityOrPath(idVerPath))
            OperationEnum.IS_UNIQUE -> isUnique(inputValues[0], identityOrPath(idVerPath))
            OperationEnum.SCHEMA_MATCH ->
                matchesSchema(inputValues[0], inputValues[1], context, identityOrPath(idVerPath))
            else -> error(UNREACHABLE_CODE_ERROR)
          }.let { if (_negateResult) !it else it }
        } catch (e: Throwable) {
          // add to event
          context.event.add(
              context.id,
              PolicyEntityEnum.CONDITION_ATOMIC,
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
   * Returns a set of [PolicyEntityRefItem] instances representing the child references of this
   * policy condition, or `null` if there are no child references.
   */
  override fun childRefs(): Set<PolicyEntityRefItem>? =
      if (_childRefs.isNotEmpty()) _childRefs.toSet() else null

  /**
   * Runs compliance checks on the input values based on the operation being performed.
   *
   * This function is responsible for validating the input values against the expected conditions
   * for the given operation. It calls the appropriate compliance check function based on the
   * operation type.
   *
   * @param inputValues the list of input values to be checked
   */
  private fun runComplianceChecks(inputValues: List<VariableValue>, idVerPath: String) {
    when (operation) {
      OperationEnum.EQUALS -> Unit
      OperationEnum.GREATER_THAN,
      OperationEnum.GREATER_THAN_EQUAL,
      OperationEnum.LESS_THAN,
      OperationEnum.LESS_THAN_EQUAL ->
          complianceCheckGtGteLtLte(inputValues[0], identityOrPath(idVerPath))
      OperationEnum.IS_BLANK,
      OperationEnum.IS_NOT_BLANK -> complianceCheckString(inputValues[0], identityOrPath(idVerPath))
      OperationEnum.IS_EMPTY,
      OperationEnum.IS_NOT_EMPTY,
      OperationEnum.STARTS_WITH,
      OperationEnum.ENDS_WITH,
      OperationEnum.CONTAINS ->
          complianceCheckStringOrArray(inputValues[0], identityOrPath(idVerPath))
      OperationEnum.IS_IN -> complianceCheckStringOrArray(inputValues[1], identityOrPath(idVerPath))
      OperationEnum.IS_POSITIVE,
      OperationEnum.IS_NEGATIVE,
      OperationEnum.IS_ZERO ->
          complianceCheckMathematical(inputValues[0], identityOrPath(idVerPath))
      OperationEnum.IS_FUTURE,
      OperationEnum.IS_PAST -> complianceCheckDateTime(inputValues[0], identityOrPath(idVerPath))
      OperationEnum.REGEXP_MATCH -> complianceCheckString(inputValues[1], identityOrPath(idVerPath))
      OperationEnum.SCHEMA_MATCH ->
          complianceCheckObjectOrString(inputValues[1], identityOrPath(idVerPath))
      OperationEnum.IS_UNIQUE -> complianceCheckArray(inputValues[0], identityOrPath(idVerPath))
      OperationEnum.HAS_KEY -> {
        complianceCheckObject(inputValues[0], identityOrPath(idVerPath))
        complianceCheckString(inputValues[1], identityOrPath(idVerPath))
      }
      else -> error(UNREACHABLE_CODE_ERROR)
    }
  }

  /**
   * Casts the input values to the type of the first value in the list, if necessary.
   *
   * This function is responsible for ensuring that all input values have the same type as the first
   * value in the list. If any input value has a different type, it attempts to cast that value to
   * the type of the first value. If the cast fails, an error event is added to the context and the
   * function returns `false`.
   *
   * @param inputValues the list of input values to be cast
   * @param context the current context
   * @param idVerPath the path to the current policy condition
   * @return `true` if the cast was successful, `false` otherwise
   */
  private fun castToFirst(
      inputValues: MutableList<VariableValue>,
      context: Context,
      idVerPath: String
  ): OperationResult {
    if (inputValues.size > 1) {
      for (i in 1..(inputValues.size - 1)) {
        if (inputValues[i].type != inputValues[0].type) {
          try {
            inputValues[i] = cast(inputValues[0].type, inputValues[i], context.options)
          } catch (e: Exception) {
            // add to event
            context.event.add(
                context.id,
                PolicyEntityEnum.CONDITION_ATOMIC,
                idVerPath,
                false,
                null,
                false,
                "${e::class.java.name}:${e.message}")
            // log
            logger.error(
                marker,
                "${identityOrPath(idVerPath)} -> casting of inputValue[$i] threw an exception: {}",
                e.message,
                e)
            return false
          }
        }
      }
    }
    return true
  }

  /**
   * Retrieves a list of [VariableValue] objects from the provided [Context] and [PolicyCatalog],
   * based on the arguments specified in the [args] list.
   *
   * For each argument in [args], this function resolves the corresponding
   * [IPolicyVariableRefOrValue] from the [PolicyCatalog], and then resolves the [VariableValue]
   * from the [Context]. The resolved [VariableValue] objects are collected into a mutable list and
   * returned.
   *
   * @param context the current [Context]
   * @param policyCatalog the [PolicyCatalog] to use for resolving [IPolicyVariableRefOrValue]
   *   objects
   * @param idVerPath the path to the current policy condition
   * @return a mutable list of [VariableValue] objects
   */
  private fun getFromVariables(
      context: Context,
      policyCatalog: PolicyCatalog,
      idVerPath: String
  ): MutableList<VariableValue> =
      args
          .mapIndexed { idx, it ->
            context.addToPath("$idx")
            val resolvedVariableValue =
                getRefValueFromCatalog<
                        IPolicyVariableRefOrValue, IPolicyVariable, PolicyVariableRef>(
                        it,
                        policyCatalog,
                        idVerPath,
                        context,
                        PolicyEntityEnum.CONDITION_ATOMIC,
                        logger,
                        marker)
                    ?.resolve(context, policyCatalog) ?: NullVariableValue()
            context.removeLastFromPath()
            resolvedVariableValue
          }
          .toMutableList()

  /**
   * Returns the identity string for this [PolicyConditionAtomic] instance.
   *
   * @return the identity string, which is the [idVer] property.
   */
  override fun identity(): String = idVer

  /**
   * Returns the identity string for this [PolicyConditionAtomic] instance, or the provided
   * [idVerPath] if the identity is blank.
   *
   * @param idVerPath the path to the current policy condition
   * @return the identity string, which is the [idVer] property if not blank, otherwise the provided
   *   [idVerPath]
   */
  private fun identityOrPath(idVerPath: String): String = identity().ifBlank { idVerPath }
}
