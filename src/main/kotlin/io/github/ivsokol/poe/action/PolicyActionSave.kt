package io.github.ivsokol.poe.action

import io.github.ivsokol.poe.*
import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.catalog.getRefValueFromCatalog
import io.github.ivsokol.poe.variable.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

/** The default value for the `failOnMissingKey` property of a [PolicyActionSave] instance. */
private const val FAIL_ON_MISSING_KEY_DEFAULT = false

/** The default value for the `failOnExistingKey` property of a [PolicyActionSave] instance. */
private const val FAIL_ON_EXISTING_KEY_DEFAULT = false

/** The default value for the `failOnNullSource` property of a [PolicyActionSave] instance. */
private const val FAIL_ON_NULL_SOURCE_DEFAULT = false

/**
 * Represents a policy action that saves a value to a data store.
 *
 * @property id The unique identifier of the policy action.
 * @property version The version of the policy action.
 * @property description The description of the policy action.
 * @property labels The labels associated with the policy action.
 * @property key The key to use when saving the value in the data store.
 * @property value The source of the value to be saved.
 * @property failOnMissingKey Whether to fail the action if the key is missing from the data store.
 *   Default value is `false`.
 * @property failOnExistingKey Whether to fail the action if the key already exists in the data
 *   store. Default value is `false`.
 * @property failOnNullSource Whether to fail the action if the source value is null. Default value
 *   is `false`.
 */
@Serializable
@SerialName("PolicyActionSave")
data class PolicyActionSave(
    override val id: String? = null,
    override val version: SemVer? = null,
    override val description: String? = null,
    override val labels: List<String>? = null,
    val key: String,
    val value: IPolicyVariableRefOrValue,
    val failOnMissingKey: Boolean? = null,
    val failOnExistingKey: Boolean? = null,
    val failOnNullSource: Boolean? = null,
) : IPolicyAction {
  override val type: ActionTypeEnum = ActionTypeEnum.SAVE

  @Transient private val logger = LoggerFactory.getLogger(this::class.java)

  @Transient private val marker = MarkerFactory.getMarker("PolicyAction")

  @Transient private val idVer: String = if (version != null) "$id:$version" else id ?: ""

  @Transient private var _childRefs: MutableSet<PolicyEntityRefItem> = mutableSetOf()

  init {
    this.validateId()
    labels?.also { require(it.isNotEmpty()) { "$idVer:Labels must not be empty array" } }
    require(key.isNotBlank()) { "$idVer:Key must not be blank" }

    when (value) {
      is PolicyVariableRef ->
          _childRefs.add(
              PolicyEntityRefItem(PolicyEntityRefEnum.POLICY_VARIABLE_REF, value.id, value.version))
      is IPolicyVariable -> value.childRefs()?.also { c -> _childRefs.addAll(c) }
      else -> error("$idVer: Unsupported type for source ${value::class.java.simpleName}")
    }
  }

  override fun childRefs(): Set<PolicyEntityRefItem>? =
      if (_childRefs.isNotEmpty()) _childRefs.toSet() else null

  override fun identity(): String = idVer

  /**
   * Runs the PolicyActionSave, which saves a value to the data store based on the provided key and
   * source.
   *
   * @param context The current execution context.
   * @param policyCatalog The policy catalog to use for resolving policy variables.
   * @return `true` if the action was successful, `false` otherwise.
   */
  override fun run(context: Context, policyCatalog: PolicyCatalog): Boolean {
    val idVerPath = context.getFullPath(idVer)
    logger.debug(marker, "${context.id}->$idVerPath:Running PolicyActionSave")

    val dataStore = context.dataStore()
    if (dataStore.containsKey(key)) {
      if (failOnExistingKey ?: FAIL_ON_EXISTING_KEY_DEFAULT) {
        // add to event
        context.event.add(
            context.id,
            PolicyEntityEnum.POLICY_ACTION_SAVE,
            idVerPath,
            false,
            null,
            false,
            "Existing key: $key")
        // log
        logger.error(marker, "${context.id}->$idVerPath:Existing key {}", key)
        return false
      }
    } else {
      if (failOnMissingKey ?: FAIL_ON_MISSING_KEY_DEFAULT) {
        // add to event
        context.event.add(
            context.id,
            PolicyEntityEnum.POLICY_ACTION_SAVE,
            idVerPath,
            false,
            null,
            false,
            "Missing key: $key")
        // log
        logger.error(marker, "${context.id}->$idVerPath:Key $key is missing from data store")
        return false
      }
    }
    // get source value
    context.addToPath("source")
    val resolvedVariableValue =
        getRefValueFromCatalog<IPolicyVariableRefOrValue, IPolicyVariable, PolicyVariableRef>(
                value,
                policyCatalog,
                idVerPath,
                context,
                PolicyEntityEnum.POLICY_ACTION_SAVE,
                logger,
                marker)
            ?.resolve(context, policyCatalog) ?: NullVariableValue()
    context.removeLastFromPath()
    if ((failOnNullSource ?: FAIL_ON_NULL_SOURCE_DEFAULT) &&
        resolvedVariableValue.type == VariableRuntimeTypeEnum.NULL) {
      // add to event
      context.event.add(
          context.id,
          PolicyEntityEnum.POLICY_ACTION_SAVE,
          idVerPath,
          false,
          null,
          false,
          "Null source value")
      // log
      logger.error(marker, "${context.id}->$idVerPath:Null source value")
      return false
    }

    dataStore[key] = resolvedVariableValue.body
    // add to event
    context.event.add(
        context.id,
        PolicyEntityEnum.POLICY_ACTION_SAVE,
        idVerPath,
        true,
        resolvedVariableValue.body)
    // log
    logger.debug(marker, "${context.id}->$idVerPath:PolicyActionSave completed")
    logger.trace(
        marker, "${context.id}->$idVerPath:PolicyActionSave {}:{}", key, resolvedVariableValue.body)
    return true
  }
}
