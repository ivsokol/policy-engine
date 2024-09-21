package io.github.ivsokol.poe.action

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.github.fge.jsonpatch.JsonPatch
import io.github.ivsokol.poe.*
import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.catalog.getRefValueFromCatalog
import io.github.ivsokol.poe.variable.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

/** The default value for the `failOnMissingKey` parameter of the `PolicyActionJsonPatch` class. */
private const val FAIL_ON_MISSING_KEY_DEFAULT = false

/** The default value for the `failOnExistingKey` parameter of the `PolicyActionJsonPatch` class. */
private const val FAIL_ON_EXISTING_KEY_DEFAULT = false

/** The default value for the `failOnNullSource` parameter of the `PolicyActionJsonPatch` class. */
private const val FAIL_ON_NULL_SOURCE_DEFAULT = false

/**
 * The default value for the `castNullSourceToArray` parameter of the `PolicyActionJsonPatch` class.
 */
private const val CAST_NULL_SOURCE_TO_ARRAY_DEFAULT = false

/**
 * Represents a policy action that applies a JSON patch to a value in the data store.
 *
 * @property id The unique identifier of the policy action.
 * @property version The version of the policy action.
 * @property description The description of the policy action.
 * @property labels The labels associated with the policy action.
 * @property key The key in the data store to apply the patch to.
 * @property source The source value to apply the patch to.
 * @property patch The JSON patch to apply to the source value.
 * @property failOnMissingKey Whether to fail the action if the key is missing from the data store.
 *   Default value is `false`.
 * @property failOnExistingKey Whether to fail the action if the key already exists in the data
 *   store. Default value is `false`.
 * @property failOnNullSource Whether to fail the action if the source value is null. Default value
 *   is `false`.
 * @property castNullSourceToArray Whether to cast a null source value to an empty array. Default
 *   value is `false`.
 */
@Serializable
@SerialName("PolicyActionJsonPatch")
data class PolicyActionJsonPatch(
    override val id: String? = null,
    override val version: SemVer? = null,
    override val description: String? = null,
    override val labels: List<String>? = null,
    val key: String,
    val source: IPolicyVariableRefOrValue,
    val patch: IPolicyVariableRefOrValue,
    val failOnMissingKey: Boolean? = null,
    val failOnExistingKey: Boolean? = null,
    val failOnNullSource: Boolean? = null,
    val castNullSourceToArray: Boolean? = null,
) : IPolicyAction {
  override val type: ActionTypeEnum = ActionTypeEnum.JSON_PATCH

  @Transient private val logger = LoggerFactory.getLogger(this::class.java)

  @Transient private val marker = MarkerFactory.getMarker("PolicyAction")

  @Transient private val idVer: String = if (version != null) "$id:$version" else id ?: ""
  @Transient private var _childRefs: MutableSet<PolicyEntityRefItem> = mutableSetOf()

  init {
    this.validateId()
    labels?.also { require(it.isNotEmpty()) { "$idVer:Labels must not be empty array" } }
    require(key.isNotBlank()) { "$idVer:Key must not be blank" }

    when (source) {
      is PolicyVariableRef ->
          _childRefs.add(
              PolicyEntityRefItem(
                  PolicyEntityRefEnum.POLICY_VARIABLE_REF, source.id, source.version))
      is IPolicyVariable -> source.childRefs()?.also { c -> _childRefs.addAll(c) }
      else -> error("$idVer: Unsupported type for source ${source::class.java.simpleName}")
    }
    when (patch) {
      is PolicyVariableRef ->
          _childRefs.add(
              PolicyEntityRefItem(PolicyEntityRefEnum.POLICY_VARIABLE_REF, patch.id, patch.version))
      is IPolicyVariable -> patch.childRefs()?.also { c -> _childRefs.addAll(c) }
      else -> error("$idVer: Unsupported type for merge ${patch::class.java.simpleName}")
    }
  }

  override fun childRefs(): Set<PolicyEntityRefItem>? =
      if (_childRefs.isNotEmpty()) _childRefs.toSet() else null

  override fun identity(): String = idVer

  /**
   * Runs the JSON patch action on the data store.
   *
   * This action applies a JSON patch to a value in the data store, identified by the [key]
   * property. The source value to be patched is retrieved from the [source] property, and the patch
   * itself is retrieved from the [patch] property.
   *
   * The action can be configured to fail on various conditions, such as the key not existing in the
   * data store, the source value being null, or the patch being invalid. These conditions are
   * controlled by the various boolean properties on the action.
   *
   * If the patch is successfully applied, the new value is added to the data store under the [key]
   * property.
   *
   * @param context The current execution context.
   * @param policyCatalog The policy catalog, used to resolve references.
   * @return True if the patch was successfully applied, false otherwise.
   */
  override fun run(context: Context, policyCatalog: PolicyCatalog): Boolean {
    val idVerPath = context.getFullPath(idVer)
    logger.debug(marker, "${context.id}->$idVerPath:Running PolicyActionJsonPatch")
    val dataStore = context.dataStore()

    if (dataStore.containsKey(key)) {
      if (failOnExistingKey ?: FAIL_ON_EXISTING_KEY_DEFAULT) {
        // add to event
        context.event.add(
            context.id,
            PolicyEntityEnum.POLICY_ACTION_JSON_PATCH,
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
            PolicyEntityEnum.POLICY_ACTION_JSON_PATCH,
            idVerPath,
            false,
            null,
            false,
            "Missing key: $key")
        // log
        logger.error(marker, "${context.id}->$idVerPath:Key is missing from data store")
        return false
      }
    }

    // get source value
    context.addToPath("source")
    var sourceVariableValue =
        getRefValueFromCatalog<IPolicyVariableRefOrValue, IPolicyVariable, PolicyVariableRef>(
                source,
                policyCatalog,
                idVerPath,
                context,
                PolicyEntityEnum.POLICY_ACTION_JSON_PATCH,
                logger,
                marker)
            ?.resolve(context, policyCatalog) ?: NullVariableValue()
    context.removeLastFromPath()
    if ((failOnNullSource ?: FAIL_ON_NULL_SOURCE_DEFAULT) &&
        sourceVariableValue.type == VariableRuntimeTypeEnum.NULL) {
      // add to event
      context.event.add(
          context.id,
          PolicyEntityEnum.POLICY_ACTION_JSON_PATCH,
          idVerPath,
          false,
          null,
          false,
          "Null source value")
      // log
      logger.error(marker, "${context.id}->$idVerPath:Null source value")
      return false
    }
    if (sourceVariableValue.type !in
        listOf(
            VariableRuntimeTypeEnum.NULL,
            VariableRuntimeTypeEnum.JSON_NODE,
            VariableRuntimeTypeEnum.ARRAY_NODE,
            VariableRuntimeTypeEnum.OBJECT_NODE)) {
      logger.debug(marker, "${context.id}->$idVerPath:Casting sourceVariableValue to JsonNode")
      try {
        sourceVariableValue =
            cast(VariableRuntimeTypeEnum.JSON_NODE, sourceVariableValue, context.options)
      } catch (e: Exception) {
        // add to event
        context.event.add(
            context.id,
            PolicyEntityEnum.POLICY_ACTION_JSON_PATCH,
            idVerPath,
            false,
            null,
            false,
            "${e::class.java.name}:${e.message}")
        // log
        logger.error(
            marker,
            "${context.id}->$idVerPath:Casting source failed with exception: {}",
            e.message,
            e)
        return false
      }
    }
    val sourceValue: JsonNode =
        if (sourceVariableValue.type == VariableRuntimeTypeEnum.NULL)
            if (castNullSourceToArray ?: CAST_NULL_SOURCE_TO_ARRAY_DEFAULT)
                context.options.objectMapper.createArrayNode()
            else context.options.objectMapper.createObjectNode()
        else sourceVariableValue.body as JsonNode

    // find patch value
    context.addToPath("patch")
    var patchVariableValue =
        getRefValueFromCatalog<IPolicyVariableRefOrValue, IPolicyVariable, PolicyVariableRef>(
                patch,
                policyCatalog,
                idVerPath,
                context,
                PolicyEntityEnum.POLICY_ACTION_JSON_PATCH,
                logger,
                marker)
            ?.resolve(context, policyCatalog) ?: NullVariableValue()
    context.removeLastFromPath()
    if (patchVariableValue.type != VariableRuntimeTypeEnum.ARRAY_NODE) {
      logger.debug(marker, "${context.id}->$idVerPath:Casting patchVariableValue to ArrayNode")
      try {
        patchVariableValue =
            cast(VariableRuntimeTypeEnum.ARRAY_NODE, patchVariableValue, context.options)
      } catch (e: Exception) {
        // add to event
        context.event.add(
            context.id,
            PolicyEntityEnum.POLICY_ACTION_JSON_PATCH,
            idVerPath,
            false,
            null,
            false,
            "${e::class.java.name}:${e.message}")
        // log
        logger.error(
            marker,
            "${context.id}->$idVerPath:Casting patch failed with exception: {}",
            e.message,
            e)
        return false
      }
    }
    val patchValue: JsonNode = patchVariableValue.body as ArrayNode

    try {
      val patch = JsonPatch.fromJson(patchValue)
      val newValue: JsonNode = patch.apply(sourceValue)
      // add to event
      context.event.add(
          context.id, PolicyEntityEnum.POLICY_ACTION_JSON_PATCH, idVerPath, true, newValue)
      dataStore[key] = newValue
      // log
      logger.debug(marker, "${context.id}->$idVerPath:PolicyActionJsonPatch completed")
      logger.trace(marker, "${context.id}->$idVerPath:PolicyActionJsonPatch {}:{}", key, newValue)
      return true
    } catch (e: Exception) {
      // add to event
      context.event.add(
          context.id,
          PolicyEntityEnum.POLICY_ACTION_JSON_PATCH,
          idVerPath,
          false,
          null,
          false,
          "${e::class.java.name}:${e.message}")
      // log
      logger.error(
          marker, "${context.id}->$idVerPath:Patch action failed with exception: {}", e.message, e)
      return false
    }
  }
}
