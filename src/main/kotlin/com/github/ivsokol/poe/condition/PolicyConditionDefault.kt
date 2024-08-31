package com.github.ivsokol.poe.condition

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.PolicyEntityEnum
import com.github.ivsokol.poe.PolicyEntityRefItem
import com.github.ivsokol.poe.SemVer
import com.github.ivsokol.poe.catalog.PolicyCatalog
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

/**
 * Represents a default policy condition that always returns a specified boolean value.
 *
 * @property default The default boolean value to return when checking this condition.
 * @property version The semantic version of this condition.
 * @property description A description of this condition.
 * @property labels A list of labels associated with this condition.
 * @property negateResult A flag indicating whether the result of this condition should be negated.
 */
@Serializable(with = PolicyConditionDefaultSerializer::class)
@SerialName("PolicyConditionDefault")
data class PolicyConditionDefault(val default: Boolean? = null) : IPolicyCondition {
  override val id: String = "${'$'}${default ?: "null"}"
  override val version: SemVer? = null
  override val description: String? = null
  override val labels: List<String>? = null
  override val negateResult: Boolean? = null

  @Transient private val logger = LoggerFactory.getLogger(this::class.java)
  @Transient private val marker = MarkerFactory.getMarker("PolicyCondition")

  override fun identity(): String = id

  override fun check(context: Context, policyCatalog: PolicyCatalog): Boolean? =
      default.also {
        val idVerPath = context.getFullPath(identity())
        logger.debug(marker, "${context.id}->$idVerPath:Checking PolicyConditionDefault")
        context.event.add(
            context.id, PolicyEntityEnum.CONDITION_DEFAULT, idVerPath, true, default, false)
        logger.debug(marker, "${context.id}->$idVerPath:${this.javaClass.simpleName} response")
        logger.trace(
            marker,
            "${context.id}->$idVerPath:${this.javaClass.simpleName} response value: {}",
            default)
      }

  override fun childRefs(): Set<PolicyEntityRefItem>? = null
}
