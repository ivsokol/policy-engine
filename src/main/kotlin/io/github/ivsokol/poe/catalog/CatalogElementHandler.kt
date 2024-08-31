package io.github.ivsokol.poe.catalog

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.IRef
import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.PolicyEntityRefEnum
import org.slf4j.Logger
import org.slf4j.Marker

/**
 * Retrieves a reference value from the policy catalog based on the provided [refOrValue] object.
 *
 * @param refOrValue The object that may be a reference or a value.
 * @param policyCatalog The policy catalog to use for retrieving the reference value.
 * @param idVerPath The path to the ID and version of the reference.
 * @param context The context of the operation.
 * @param parentEntity The parent entity of the reference.
 * @param logger The logger to use for logging errors.
 * @param marker The marker to use for logging.
 * @return The reference value, or null if the reference is not found in the catalog.
 */
internal inline fun <T, reified U, reified V> getRefValueFromCatalog(
    refOrValue: T & Any,
    policyCatalog: PolicyCatalog,
    idVerPath: String,
    context: Context,
    parentEntity: PolicyEntityEnum,
    logger: Logger,
    marker: Marker
): U? where V : IRef, U : T {
  if (refOrValue is U) return refOrValue
  check(refOrValue is V) {
    "${context.id}->$idVerPath:Unsupported type ${refOrValue::class.java.simpleName}"
  }

  val refValue =
      when (refOrValue.refType) {
        PolicyEntityRefEnum.POLICY_VARIABLE_RESOLVER_REF ->
            policyCatalog.getPolicyVariableResolver(refOrValue.id, refOrValue.version)
        PolicyEntityRefEnum.POLICY_REF -> policyCatalog.getPolicy(refOrValue.id, refOrValue.version)
        PolicyEntityRefEnum.POLICY_ACTION_REF ->
            policyCatalog.getPolicyAction(refOrValue.id, refOrValue.version)
        PolicyEntityRefEnum.POLICY_CONDITION_REF ->
            policyCatalog.getPolicyCondition(refOrValue.id, refOrValue.version)
        PolicyEntityRefEnum.POLICY_VARIABLE_REF ->
            policyCatalog.getPolicyVariable(refOrValue.id, refOrValue.version)
      }

  if (refValue != null) return refValue as U
  // if null log error
  context.event.add(
      context.id,
      parentEntity,
      idVerPath,
      false,
      null,
      false,
      "${refOrValue::class.java.simpleName}(${refOrValue.id}:${refOrValue.version}) not found in catalog")
  // log
  logger.error(
      marker,
      "${context.id}->$idVerPath:${refOrValue::class.java.simpleName}(${refOrValue.id}:${refOrValue.version}) not found in catalog")
  return null
}
