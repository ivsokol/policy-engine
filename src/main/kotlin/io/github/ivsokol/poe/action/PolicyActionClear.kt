package io.github.ivsokol.poe.action

import io.github.ivsokol.poe.*
import io.github.ivsokol.poe.catalog.PolicyCatalog
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

/** The default value for the `failOnMissingKey` property of the [PolicyActionClear] class. */
private const val FAIL_ON_MISSING_KEY_DEFAULT = false

/**
 * Represents a policy action that clears a key from the data store.
 *
 * @property id The unique identifier for this policy action.
 * @property version The semantic version of this policy action.
 * @property description A description of this policy action.
 * @property labels A list of labels associated with this policy action.
 * @property key The key to be cleared from the data store.
 * @property failOnMissingKey If true, the action will fail if the specified key is missing from the
 *   data store. If false, the action will simply remove the key from the data store if it exists.
 *   Default value is false.
 */
@Serializable
@SerialName("PolicyActionClear")
data class PolicyActionClear(
    override val id: String? = null,
    override val version: SemVer? = null,
    override val description: String? = null,
    override val labels: List<String>? = null,
    val key: String,
    val failOnMissingKey: Boolean? = null,
) : IPolicyAction {
  override val type: ActionTypeEnum = ActionTypeEnum.CLEAR

  @Transient private val logger = LoggerFactory.getLogger(this::class.java)

  @Transient private val marker = MarkerFactory.getMarker("PolicyAction")

  @Transient private val idVer: String = if (version != null) "$id:$version" else id ?: ""

  init {
    this.validateId()
    labels?.also { require(it.isNotEmpty()) { "$idVer:Labels must not be empty array" } }
    require(key.isNotBlank()) { "$idVer:Key must not be blank" }
  }

  override fun childRefs(): Set<PolicyEntityRefItem>? = null

  override fun identity(): String = idVer

  /**
   * Runs the PolicyActionClear, which clears a key from the data store.
   *
   * @param context The current execution context.
   * @param policyCatalog The policy catalog.
   * @return True if the key was successfully removed from the data store, false otherwise.
   */
  override fun run(context: Context, policyCatalog: PolicyCatalog): Boolean {
    val idVerPath = context.getFullPath(idVer)
    logger.debug(marker, "${context.id}->$idVerPath:Running PolicyActionClear")
    val dataStore = context.dataStore()
    if ((failOnMissingKey ?: FAIL_ON_MISSING_KEY_DEFAULT) && !dataStore.containsKey(key)) {
      // add to event
      context.event.add(
          context.id,
          PolicyEntityEnum.POLICY_ACTION_CLEAR,
          idVerPath,
          false,
          null,
          false,
          "Missing key: $key")
      // log
      logger.error(marker, "${context.id}->$idVerPath:Key $key is missing from data store")
      return false
    }
    dataStore.remove(key)
    // add to event
    context.event.add(context.id, PolicyEntityEnum.POLICY_ACTION_CLEAR, idVerPath, true)
    // log
    logger.debug(marker, "${context.id}->$idVerPath:Key $key removed from data store")
    return true
  }
}
