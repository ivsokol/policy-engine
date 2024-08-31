package io.github.ivsokol.poe.policy

import io.github.ivsokol.poe.*
import io.github.ivsokol.poe.action.IPolicyAction
import io.github.ivsokol.poe.action.PolicyActionRef
import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.condition.IPolicyCondition
import io.github.ivsokol.poe.condition.IPolicyConditionRefOrValue
import io.github.ivsokol.poe.condition.PolicyConditionRef
import io.github.ivsokol.poe.variable.checkConstraint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

private const val LENIENT_CONSTRAINTS_DEFAULT = true

/**
 * Represents a default policy configuration, which defines the default behavior for a policy. The
 * `PolicyDefault` class encapsulates the default policy settings, including the default result,
 * optional constraint, actions, execution strategy, and other configuration options. This class is
 * used as part of the policy evaluation process to determine the overall policy result.
 */
@Serializable
@SerialName("PolicyDefault")
data class PolicyDefault(
    val default: PolicyResultEnum,
    override val constraint: IPolicyConditionRefOrValue? = null,
    override val actions: List<PolicyActionRelationship>? = null,
    override val actionExecutionStrategy: ActionExecutionStrategyEnum? = null,
    override val lenientConstraints: Boolean? = null,
    override val ignoreErrors: Boolean? = null,
    override val priority: Int? = null
) : IPolicy {
  override val id: String = "${'$'}${default}"
  override val version: SemVer? = null
  override val description: String? = null
  override val labels: List<String>? = null

  @Transient private val logger = LoggerFactory.getLogger(this::class.java)
  @Transient private val marker = MarkerFactory.getMarker("Policy")
  @Transient private var _childRefs: MutableSet<PolicyEntityRefItem> = mutableSetOf()

  init {

    constraint?.also {
      when (it) {
        is PolicyConditionRef ->
            _childRefs.add(
                PolicyEntityRefItem(PolicyEntityRefEnum.POLICY_CONDITION_REF, it.id, it.version))
        is IPolicyCondition -> it.childRefs()?.also { c -> _childRefs.addAll(c) }
        else -> error("$id: Unsupported type for constraint ${it::class.java.simpleName}")
      }
    }
    actions?.onEach { ar ->
      when (ar.action) {
        is PolicyActionRef ->
            _childRefs.add(
                PolicyEntityRefItem(
                    PolicyEntityRefEnum.POLICY_ACTION_REF, ar.action.id, ar.action.version))
        is IPolicyAction -> ar.action.childRefs()?.also { c -> _childRefs.addAll(c) }
        else -> error("$id: Unsupported type for action ${ar.action::class.java.simpleName}")
      }
      ar.constraint?.also {
        when (ar.constraint) {
          is PolicyConditionRef ->
              _childRefs.add(
                  PolicyEntityRefItem(
                      PolicyEntityRefEnum.POLICY_CONDITION_REF,
                      ar.constraint.id,
                      ar.constraint.version))
          is IPolicyCondition -> ar.constraint.childRefs()?.also { c -> _childRefs.addAll(c) }
          else ->
              error("$id: Unsupported type for constraint ${ar.constraint::class.java.simpleName}")
        }
      }
    }
  }

  /**
   * Evaluates the policy and returns the result.
   *
   * @param context The context in which the policy is being evaluated.
   * @param policyCatalog The catalog of policies.
   * @return The result of evaluating the policy.
   */
  override fun evaluate(context: Context, policyCatalog: PolicyCatalog): PolicyResultEnum {
    val idVerPath = context.getFullPath(id)
    logger.debug(marker, "${context.id}->$idVerPath:Evaluating PolicyDefault")

    if (constraint != null && context.isRootPath(id)) {
      val constraintResult =
          checkConstraint(
              constraint,
              context,
              policyCatalog,
              idVerPath,
              PolicyEntityEnum.POLICY_DEFAULT,
              logger,
              marker)
      logger.debug(
          marker, "${context.id}->$idVerPath:Constraint for policy is {}", constraintResult)
      when (constraintResult) {
        true -> Unit
        false ->
            return cacheAndReturn(
                id, context, PolicyResultEnum.NOT_APPLICABLE, idVerPath, logger, marker)
        null ->
            // not applicable if lenient
            return if (lenientConstraints ?: LENIENT_CONSTRAINTS_DEFAULT)
                cacheAndReturn(
                    id, context, PolicyResultEnum.NOT_APPLICABLE, idVerPath, logger, marker)
            else
            // indeterminate if strict
            cacheAndReturn(
                    id,
                    context,
                    PolicyResultEnum.INDETERMINATE_DENY_PERMIT,
                    idVerPath,
                    logger,
                    marker)
      }
    }
    // add to event
    context.event.add(context.id, PolicyEntityEnum.POLICY_DEFAULT, idVerPath, true, default)
    // log
    logger.debug(marker, "${context.id}->$idVerPath:${this.javaClass.simpleName} evaluated")
    logger.trace(
        marker,
        "${context.id}->$idVerPath:${this.javaClass.simpleName} evaluation result: {}",
        default)
    return default
  }

  override fun isSuccess(result: PolicyResultEnum): Boolean = true

  override fun runActions(
      context: Context,
      policyCatalog: PolicyCatalog,
      policyResult: PolicyResultEnum
  ): ActionResult =
      this.runActions(
          context.getFullPath(""), context, policyCatalog, policyResult, this.logger, this.marker)

  override fun identity(): String = id

  override fun childRefs(): Set<PolicyEntityRefItem>? = null
}
