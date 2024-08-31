/* (C)2024 */
package com.github.ivsokol.poe.variable

import com.github.ivsokol.poe.*
import com.github.ivsokol.poe.cache.PolicyStoreCacheEnum
import com.github.ivsokol.poe.catalog.PolicyCatalog
import java.time.format.DateTimeFormatter
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

/**
 * Represents a static variable value that is usually used to compare with dynamic variable values
 * or to set a specific result.
 *
 * @property id The optional variable ID.
 * @property version The optional variable version in SemVer format.
 * @property description The optional variable description.
 * @property labels The optional variable labels used for searching.
 * @property value The variable value.
 * @property type The optional variable type.
 * @property format The optional variable format.
 */
@Serializable(with = PolicyVariableStaticSerializer::class)
@SerialName("PolicyVariableStatic")
data class PolicyVariableStatic(
    override val id: String? = null,
    override val version: SemVer? = null,
    override val description: String? = null,
    override val labels: List<String>? = null,
    val value: @Contextual Any,
    override val type: VariableValueTypeEnum? = null,
    override val format: VariableValueFormatEnum? = null,
    override val timeFormat: String? = null,
    override val dateFormat: String? = null,
    override val dateTimeFormat: String? = null
) : IPolicyVariable {
  @Transient private val logger = LoggerFactory.getLogger(this::class.java)
  @Transient private val marker = MarkerFactory.getMarker("PolicyVariable")
  @Transient private val idVer: String = if (version != null) "$id:$version" else id ?: ""

  init {
    this.validateId()
    labels?.also { require(it.isNotEmpty()) { "$idVer:Labels must not be empty array" } }
    format?.also {
      require(type != null) { "$idVer:type must not be null when format is populated" }
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
  }

  /**
   * Resolves the value of this [PolicyVariableStatic] instance.
   *
   * If the variable has an identity and is found in the cache, the cached value is returned.
   * Otherwise, the value is parsed and coerced to the specified type, if present. The parsed value
   * is then cached and returned. If an exception occurs during parsing, a [NullVariableValue] is
   * returned and the exception is logged.
   *
   * @param context The [Context] to use for resolving the variable.
   * @param policyCatalog The [PolicyCatalog] to use for resolving the variable.
   * @return The resolved [VariableValue] for this [PolicyVariableStatic] instance.
   */
  override fun resolve(context: Context, policyCatalog: PolicyCatalog): VariableValue {
    val idVerPath = context.getFullPath(idVer)
    logger.debug(marker, "${context.id}->$idVerPath:Resolving PolicyVariableStatic")
    // if it has identity and is found in cache, return from cache
    if (idVer.isNotBlank() && context.cache.hasKey(PolicyStoreCacheEnum.VARIABLE, idVer)) {
      val cached = context.cache.get(PolicyStoreCacheEnum.VARIABLE, idVer)
      // add to event
      context.event.add(context.id, PolicyEntityEnum.VARIABLE_STATIC, idVerPath, true, cached, true)
      // log
      logger.debug(marker, "${context.id}->$idVerPath:PolicyVariableStatic found in cache")
      logger.trace(marker, "${context.id}->$idVerPath:PolicyVariableStatic cache value: {}", cached)
      return cached as VariableValue
    }
    // if no type coercions, return value as is
    if (type == null)
        return VariableValue(value).also {
          // save in cache
          if (idVer.isNotBlank()) context.cache.put(PolicyStoreCacheEnum.VARIABLE, idVer, it)
          // add to event
          context.event.add(context.id, PolicyEntityEnum.VARIABLE_STATIC, idVerPath, true, it)
          // log
          logger.debug(marker, "${context.id}->$idVerPath:PolicyVariableStatic resolved")
          logger.trace(marker, "${context.id}->$idVerPath:PolicyVariableStatic value: {}", it)
        }
    // try to coerce value
    return try {
      parseValue(
              value = value,
              type = type,
              format = format,
              options = context.options,
              timeFormat = timeFormat,
              dateFormat = dateFormat,
              dateTimeFormat = dateTimeFormat)
          .also {
            // save in cache
            if (idVer.isNotBlank()) context.cache.put(PolicyStoreCacheEnum.VARIABLE, idVer, it)
            // add to event
            context.event.add(context.id, PolicyEntityEnum.VARIABLE_STATIC, idVerPath, true, it)
            // log
            logger.debug(
                marker, "${context.id}->$idVerPath:PolicyVariableStatic resolved and parsed")
            logger.trace(marker, "${context.id}->$idVerPath:PolicyVariableStatic value: {}", it)
          }
    } catch (e: Exception) {
      // save in cache
      if (idVer.isNotBlank())
          context.cache.put(PolicyStoreCacheEnum.VARIABLE, idVer, NullVariableValue())
      // add to event
      context.event.add(
          context.id,
          PolicyEntityEnum.VARIABLE_STATIC,
          idVerPath,
          false,
          null,
          false,
          "${e::class.java.name}:${e.message}")
      logger.error(
          marker, "${context.id}->$idVerPath:Exception while parsing PolicyVariableStatic", e)
      NullVariableValue()
    }
  }

  /** Returns `null` as this class does not have any child references. */
  override fun childRefs(): Set<PolicyEntityRefItem>? = null

  /**
   * Returns the unique identifier of this [PolicyVariableStatic] instance, which is a combination
   * of the PolicyContext.id and the variable path.
   */
  override fun identity(): String = idVer
}
