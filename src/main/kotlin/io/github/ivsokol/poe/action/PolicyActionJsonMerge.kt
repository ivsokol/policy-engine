package io.github.ivsokol.poe.action

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.NullNode
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch
import io.github.ivsokol.poe.*
import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.catalog.getRefValueFromCatalog
import io.github.ivsokol.poe.variable.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

/** The default value for the `failOnMissingKey` property of a `PolicyActionJsonMerge` instance. */
private const val FAIL_ON_MISSING_KEY_DEFAULT = false

/** The default value for the `failOnExistingKey` property of a `PolicyActionJsonMerge` instance. */
private const val FAIL_ON_EXISTING_KEY_DEFAULT = false

/** The default value for the `failOnNullSource` property of a `PolicyActionJsonMerge` instance. */
private const val FAIL_ON_NULL_SOURCE_DEFAULT = false

/** The default value for the `failOnNullMerge` property of a `PolicyActionJsonMerge` instance. */
private const val FAIL_ON_NULL_MERGE_DEFAULT = false

/**
 * Represents a policy action that merges a JSON value from a source into a data store key.
 *
 * The `PolicyActionJsonMerge` class is responsible for merging a JSON value from a source into a
 * data store key. It supports various configuration options to control the behavior of the merge
 * operation, such as handling missing or existing keys, and handling null source or merge values.
 *
 * The class implements the `IPolicyAction` interface, which means it can be used as a policy action
 * in the policy engine. The `run` method is the main entry point for executing the policy action,
 * which performs the actual merge operation.
 *
 * @property id The unique identifier of the policy action.
 * @property version The version of the policy action.
 * @property description The description of the policy action.
 * @property labels The labels associated with the policy action.
 * @property key The key to use when saving the value in the data store.
 * @property source The source value to be merged to.
 * @property merge The merge value that will be merged into the source value.
 * @property failOnMissingKey Whether to fail the action if the key is missing from the data store.
 *   Default value is `false`.
 * @property failOnExistingKey Whether to fail the action if the key already exists in the data
 *   store. Default value is `false`.
 * @property failOnNullSource Whether to fail the action if the source value is null. Default value
 *   is `false`.
 * @property failOnNullMerge Whether to fail the action if the merge value is null. Default value is
 *   `false`.
 * @property destinationType The type of the result value
 * @property destinationFormat The format of the result value
 */
@Serializable
@SerialName("PolicyActionJsonMerge")
data class PolicyActionJsonMerge(
    override val id: String? = null,
    override val version: SemVer? = null,
    override val description: String? = null,
    override val labels: List<String>? = null,
    val key: String,
    val source: IPolicyVariableRefOrValue,
    val merge: IPolicyVariableRefOrValue,
    val failOnMissingKey: Boolean? = null,
    val failOnExistingKey: Boolean? = null,
    val failOnNullSource: Boolean? = null,
    val failOnNullMerge: Boolean? = null,
    val destinationType: VariableValueTypeEnum? = null,
    val destinationFormat: VariableValueFormatEnum? = null
) : IPolicyAction {
  override val type: ActionTypeEnum = ActionTypeEnum.JSON_MERGE

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
    when (merge) {
      is PolicyVariableRef ->
          _childRefs.add(
              PolicyEntityRefItem(PolicyEntityRefEnum.POLICY_VARIABLE_REF, merge.id, merge.version))
      is IPolicyVariable -> merge.childRefs()?.also { c -> _childRefs.addAll(c) }
      else -> error("$idVer: Unsupported type for merge ${merge::class.java.simpleName}")
    }
  }

  override fun childRefs(): Set<PolicyEntityRefItem>? =
      if (_childRefs.isNotEmpty()) _childRefs.toSet() else null

  override fun identity(): String = idVer

  /**
   * Runs the JSON merge policy action.
   *
   * This method merges the source JSON value with the merge JSON value and stores the result in the
   * data store under the specified key. It performs various checks and validations to ensure the
   * merge operation is successful, such as checking for existing or missing keys, and handling null
   * source or merge values. If any errors occur during the merge operation, the method logs the
   * error and adds it to the event.
   *
   * @param context the current execution context
   * @param policyCatalog the policy catalog
   * @return true if the merge operation was successful, false otherwise
   */
  override fun run(context: Context, policyCatalog: PolicyCatalog): Boolean {
    val idVerPath = context.getFullPath(idVer)
    logger.debug(marker, "${context.id}->$idVerPath:Running PolicyActionJsonMerge")

    val dataStore = context.dataStore()
    if (dataStore.containsKey(key)) {
      if (failOnExistingKey ?: FAIL_ON_EXISTING_KEY_DEFAULT) {
        // add to event
        context.event.add(
            context.id,
            PolicyEntityEnum.POLICY_ACTION_JSON_MERGE,
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
            PolicyEntityEnum.POLICY_ACTION_JSON_MERGE,
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
    var sourceVariableValue =
        getRefValueFromCatalog<IPolicyVariableRefOrValue, IPolicyVariable, PolicyVariableRef>(
                source,
                policyCatalog,
                idVerPath,
                context,
                PolicyEntityEnum.POLICY_ACTION_JSON_MERGE,
                logger,
                marker)
            ?.resolve(context, policyCatalog) ?: NullVariableValue()
    context.removeLastFromPath()
    if ((failOnNullSource ?: FAIL_ON_NULL_SOURCE_DEFAULT) &&
        sourceVariableValue.type == VariableRuntimeTypeEnum.NULL) {
      // add to event
      context.event.add(
          context.id,
          PolicyEntityEnum.POLICY_ACTION_JSON_MERGE,
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
            PolicyEntityEnum.POLICY_ACTION_JSON_MERGE,
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
        if (sourceVariableValue.type == VariableRuntimeTypeEnum.NULL) NullNode.instance
        else sourceVariableValue.body as JsonNode

    // find merge value
    context.addToPath("merge")
    var mergeVariableValue =
        getRefValueFromCatalog<IPolicyVariableRefOrValue, IPolicyVariable, PolicyVariableRef>(
                merge,
                policyCatalog,
                idVerPath,
                context,
                PolicyEntityEnum.POLICY_ACTION_JSON_MERGE,
                logger,
                marker)
            ?.resolve(context, policyCatalog) ?: NullVariableValue()
    context.removeLastFromPath()
    if ((failOnNullMerge ?: FAIL_ON_NULL_MERGE_DEFAULT) &&
        mergeVariableValue.type == VariableRuntimeTypeEnum.NULL) {
      // add to event
      context.event.add(
          context.id,
          PolicyEntityEnum.POLICY_ACTION_JSON_MERGE,
          idVerPath,
          false,
          null,
          false,
          "Null merge value")
      // log
      logger.error(marker, "${context.id}->$idVerPath:Null merge value")
      return false
    }
    if (mergeVariableValue.type !in
        listOf(
            VariableRuntimeTypeEnum.NULL,
            VariableRuntimeTypeEnum.JSON_NODE,
            VariableRuntimeTypeEnum.ARRAY_NODE,
            VariableRuntimeTypeEnum.OBJECT_NODE)) {
      logger.debug(marker, "${context.id}->$idVerPath:Casting mergeVariableValue to JsonNode")
      try {
        mergeVariableValue =
            cast(VariableRuntimeTypeEnum.JSON_NODE, mergeVariableValue, context.options)
      } catch (e: Exception) {
        // add to event
        context.event.add(
            context.id,
            PolicyEntityEnum.POLICY_ACTION_JSON_MERGE,
            idVerPath,
            false,
            null,
            false,
            "${e::class.java.name}:${e.message}")
        // log
        logger.error(
            marker,
            "${context.id}->$idVerPath:Casting merge failed with exception: {}",
            e.message,
            e)
        return false
      }
    }
    val mergeValue: JsonNode =
        if (mergeVariableValue.type == VariableRuntimeTypeEnum.NULL) NullNode.instance
        else mergeVariableValue.body as JsonNode

    try {
      val patch = JsonMergePatch.fromJson(mergeValue)
      val newValue: JsonNode = patch.apply(sourceValue)
      var castValue: Any? = null
      if (destinationType != null) {
        val runtimeType = runtimeTypeFromTypeAndFormat(destinationType, destinationFormat)
        castValue =
            cast(
                    runtimeType,
                    VariableValue(VariableRuntimeTypeEnum.JSON_NODE, newValue),
                    context.options)
                .body
      }
      val valueToStore =
          castValue ?: if (newValue is NullNode || newValue is MissingNode) null else newValue
      dataStore[key] = valueToStore

      // add to event
      context.event.add(
          context.id, PolicyEntityEnum.POLICY_ACTION_JSON_MERGE, idVerPath, true, valueToStore)
      // log
      logger.debug(marker, "${context.id}->$idVerPath:PolicyActionJsonMerge completed")
      logger.trace(
          marker, "${context.id}->$idVerPath:PolicyActionJsonMerge {}:{}", key, valueToStore)
      return true
    } catch (e: Exception) {
      // add to event
      context.event.add(
          context.id,
          PolicyEntityEnum.POLICY_ACTION_JSON_MERGE,
          idVerPath,
          false,
          null,
          false,
          "${e::class.java.name}:${e.message}")
      // log
      logger.error(
          marker, "${context.id}->$idVerPath:Merge action failed with exception: {}", e.message, e)
      return false
    }
  }
}
