package io.github.ivsokol.poe.variable

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.catalog.getRefValueFromCatalog
import io.github.ivsokol.poe.condition.IPolicyCondition
import io.github.ivsokol.poe.condition.IPolicyConditionRefOrValue
import io.github.ivsokol.poe.condition.PolicyConditionRef
import org.slf4j.Logger
import org.slf4j.Marker

/**
 * Retrieves the constraint from the provided [IPolicyConditionRefOrValue] object, resolving any
 * references if necessary.
 *
 * @param constraint The [IPolicyConditionRefOrValue] object representing the constraint to be
 *   retrieved.
 * @param context The [Context] object containing the current execution context.
 * @param policyCatalog The [PolicyCatalog] object containing the policy catalog data.
 * @param idVerPath The ID version path string.
 * @param parentEntity The [PolicyEntityEnum] representing the parent entity.
 * @param logger The [Logger] instance for logging.
 * @param marker The [Marker] instance for logging.
 * @return The resolved [IPolicyCondition] object, or `null` if the constraint is `null`.
 */
internal fun getConstraint(
    constraint: IPolicyConditionRefOrValue?,
    context: Context,
    policyCatalog: PolicyCatalog,
    idVerPath: String,
    parentEntity: PolicyEntityEnum,
    logger: Logger,
    marker: Marker
): IPolicyCondition? {
  if (constraint == null) return null
  if (constraint is IPolicyCondition) return constraint
  context.addToPath("constraint")
  val resolvedConstraint =
      getRefValueFromCatalog<IPolicyConditionRefOrValue, IPolicyCondition, PolicyConditionRef>(
          constraint, policyCatalog, idVerPath, context, parentEntity, logger, marker)
  context.removeLastFromPath()
  return resolvedConstraint
}

/**
 * Checks the provided [IPolicyConditionRefOrValue] constraint and returns a boolean indicating
 * whether the constraint is satisfied.
 *
 * @param constraint The [IPolicyConditionRefOrValue] object representing the constraint to be
 *   checked.
 * @param context The [Context] object containing the current execution context.
 * @param policyCatalog The [PolicyCatalog] object containing the policy catalog data.
 * @param idVerPath The ID version path string.
 * @param parentEntity The [PolicyEntityEnum] representing the parent entity.
 * @param logger The [Logger] instance for logging.
 * @param marker The [Marker] instance for logging.
 * @return `Boolean` indicating whether the constraint is satisfied, or `null` if the constraint is
 *   `null`.
 */
internal fun checkConstraint(
    constraint: IPolicyConditionRefOrValue?,
    context: Context,
    policyCatalog: PolicyCatalog,
    idVerPath: String,
    parentEntity: PolicyEntityEnum,
    logger: Logger,
    marker: Marker
): Boolean? {
  val constraintFetched =
      getConstraint(constraint, context, policyCatalog, idVerPath, parentEntity, logger, marker)
          ?: return null
  context.addToPath("constraint")
  val constraintChecked = constraintFetched.check(context, policyCatalog)
  context.removeLastFromPath()
  return constraintChecked
}
