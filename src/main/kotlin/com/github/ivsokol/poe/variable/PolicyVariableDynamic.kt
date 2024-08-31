/* (C)2024 */
package com.github.ivsokol.poe.variable

import com.github.ivsokol.poe.*
import com.github.ivsokol.poe.cache.PolicyStoreCacheEnum
import com.github.ivsokol.poe.catalog.PolicyCatalog
import com.github.ivsokol.poe.catalog.getRefValueFromCatalog
import java.time.format.DateTimeFormatter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

/**
 * Represents a dynamic policy variable that can be resolved using a list of resolvers.
 *
 * @property id The unique identifier of the policy variable.
 * @property version The semantic version of the policy variable.
 * @property description The description of the policy variable.
 * @property labels The labels associated with the policy variable.
 * @property resolvers The list of resolvers used to resolve the value of the policy variable.
 * @property type The type of the policy variable value.
 * @property format The format of the policy variable value.
 */
@Serializable
@SerialName("PolicyVariableDynamic")
data class PolicyVariableDynamic(
    override val id: String? = null,
    override val version: SemVer? = null,
    override val description: String? = null,
    override val labels: List<String>? = null,
    val resolvers: List<IPolicyVariableResolverRefOrValue>,
    override val type: VariableValueTypeEnum? = null,
    override val format: VariableValueFormatEnum? = null,
    override val timeFormat: String? = null,
    override val dateFormat: String? = null,
    override val dateTimeFormat: String? = null
) : IPolicyVariable {
  @Transient private val logger = LoggerFactory.getLogger(this::class.java)
  @Transient private val marker = MarkerFactory.getMarker("PolicyVariable")
  @Transient private val idVer: String = if (version != null) "$id:$version" else id ?: ""

  @Transient private var _childRefs: MutableSet<PolicyEntityRefItem> = mutableSetOf()

  init {
    this.validateId()
    resolvers.also { require(it.isNotEmpty()) { "$idVer:Resolvers must not be empty array" } }
    labels?.also { require(it.isNotEmpty()) { "$idVer:Labels must not be empty array" } }
    format?.also {
      require(type != null) { "$idVer:Type must not be null when format is populated" }
    }

    checkTypeAndFormatCompliance(type, format)
    timeFormat?.also {
      check(type == VariableValueTypeEnum.STRING) {
        "$idVer:type must be 'string' when timeFormat is populated"
      }
      check(format == VariableValueFormatEnum.TIME) {
        "$idVer:format must be 'time' when timeFormat is populated"
      }
      DateTimeFormatter.ofPattern(it)
    }
    dateFormat?.also {
      check(type == VariableValueTypeEnum.STRING) {
        "$idVer:type must be 'string' when dateFormat is populated"
      }
      check(format == VariableValueFormatEnum.DATE) {
        "$idVer:format must be 'date' when dateFormat is populated"
      }
      DateTimeFormatter.ofPattern(it)
    }
    dateTimeFormat?.also {
      check(type == VariableValueTypeEnum.STRING) {
        "$idVer:type must be 'string' when dateTimeFormat is populated"
      }
      check(format == VariableValueFormatEnum.DATE_TIME) {
        "$idVer:format must be 'date-time' when dateTimeFormat is populated"
      }
      DateTimeFormatter.ofPattern(it)
    }

    _childRefs =
        resolvers
            .filterIsInstance<PolicyVariableResolverRef>()
            .map {
              PolicyEntityRefItem(
                  PolicyEntityRefEnum.POLICY_VARIABLE_RESOLVER_REF, it.id, it.version)
            }
            .toMutableSet()
  }

  /**
   * Resolves the value of the dynamic policy variable using the list of resolvers.
   *
   * This method first checks if the variable value is cached, and returns the cached value if
   * found. If not cached, it iterates through the list of resolvers and calls `resolve()` on each
   * one to get the variable value. If a non-null value is returned, it is returned as the final
   * variable value. If the variable has a defined type, the value is coerced to that type before
   * being returned. If any exceptions occur during the resolution or type coercion, a
   * `NullVariableValue` is returned and the exception is logged.
   *
   * @param context The current context for resolving the variable.
   * @param policyCatalog The policy catalog used to resolve any referenced entities.
   * @return The resolved value of the dynamic policy variable.
   */
  override fun resolve(context: Context, policyCatalog: PolicyCatalog): VariableValue {
    val idVerPath = context.getFullPath(idVer)
    logger.debug(marker, "${context.id}->$idVerPath:Resolving PolicyVariableDynamic")
    // if found in cache, return from cache
    if (idVer.isNotBlank() && context.cache.hasKey(PolicyStoreCacheEnum.VARIABLE, idVer)) {
      val cached = context.cache.get(PolicyStoreCacheEnum.VARIABLE, idVer)
      // add to event
      context.event.add(
          context.id, PolicyEntityEnum.VARIABLE_DYNAMIC, idVerPath, true, cached, true)
      // log
      logger.debug(marker, "${context.id}->$idVerPath:PolicyVariableDynamic found in cache")
      logger.trace(
          marker, "${context.id}->$idVerPath:PolicyVariableDynamic cached value: {}", cached)
      return cached as VariableValue
    }
    // get value from resolvers
    context.addToPath("resolvers")
    val value: Any? = getFromResolvers(context, policyCatalog, idVerPath)
    // remove 'resolvers'
    context.removeLastFromPath()

    // if no type coercions or value is null, return value as is
    if (type == null || value == null)
        return VariableValue(value).also {
          // save in cache
          if (idVer.isNotBlank()) context.cache.put(PolicyStoreCacheEnum.VARIABLE, idVer, it)
          // add to event
          context.event.add(context.id, PolicyEntityEnum.VARIABLE_DYNAMIC, idVerPath, true, it)
          // log
          logger.debug(marker, "${context.id}->$idVerPath:PolicyVariableDynamic resolved")
          logger.trace(marker, "${context.id}->$idVerPath:PolicyVariableDynamic value: {}", it)
        }
    // try to coerce value
    return try {
      parseValue(value, type, format, context.options).also {
        // save in cache
        if (idVer.isNotBlank()) context.cache.put(PolicyStoreCacheEnum.VARIABLE, idVer, it)
        // add to event
        context.event.add(context.id, PolicyEntityEnum.VARIABLE_DYNAMIC, idVerPath, true, it)
        // log
        logger.debug(marker, "${context.id}->$idVerPath:PolicyVariableDynamic resolved and parsed")
        logger.trace(marker, "${context.id}->$idVerPath:PolicyVariableDynamic value: {}", it)
      }
    } catch (e: Exception) {
      // save in cache
      if (idVer.isNotBlank())
          context.cache.put(PolicyStoreCacheEnum.VARIABLE, idVer, NullVariableValue())
      // add to event
      context.event.add(
          context.id,
          PolicyEntityEnum.VARIABLE_DYNAMIC,
          idVerPath,
          false,
          null,
          false,
          "${e::class.java.name}:${e.message}")
      logger.error(
          marker, "${context.id}->$idVerPath:Exception while parsing PolicyVariableDynamic", e)
      NullVariableValue()
    }
  }

  /**
   * Returns a set of [PolicyEntityRefItem] representing the child references of this
   * [PolicyVariableDynamic] instance, or `null` if there are no child references.
   */
  override fun childRefs(): Set<PolicyEntityRefItem>? =
      if (_childRefs.isNotEmpty()) _childRefs.toSet() else null

  /**
   * Retrieves the resolved value from the configured resolvers for the given [idVerPath].
   *
   * @param context The current [Context] instance.
   * @param policyCatalog The [PolicyCatalog] instance.
   * @param idVerPath The identifier and version path for the dynamic variable.
   * @return The resolved value from the first non-null resolver, or `null` if no resolver returned
   *   a non-null value.
   */
  private fun getFromResolvers(
      context: Context,
      policyCatalog: PolicyCatalog,
      idVerPath: String
  ): Any? =
      resolvers
          .mapIndexed { idx, it ->
            context.addToPath("$idx")
            val refValueResolved =
                getRefValueFromCatalog<
                        IPolicyVariableResolverRefOrValue,
                        PolicyVariableResolver,
                        PolicyVariableResolverRef>(
                        it,
                        policyCatalog,
                        idVerPath,
                        context,
                        PolicyEntityEnum.VARIABLE_DYNAMIC,
                        logger,
                        marker)
                    ?.resolve(context)
            context.removeLastFromPath()
            refValueResolved
          }
          .firstOrNull { it != null }

  /** Returns the identifier and version path for this [PolicyVariableDynamic] instance. */
  override fun identity(): String = idVer
}
