package com.github.ivsokol.poe.variable

import com.arakelian.jq.ImmutableJqLibrary
import com.arakelian.jq.ImmutableJqRequest
import com.arakelian.jq.JqLibrary
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import com.github.ivsokol.poe.*
import com.github.ivsokol.poe.cache.PolicyStoreCacheEnum
import io.burt.jmespath.Expression
import io.burt.jmespath.JmesPath
import io.burt.jmespath.jackson.JacksonRuntime
import kotlin.properties.Delegates
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

private val DEFAULT_ENGINE = PolicyVariableResolverEngineEnum.KEY
private val DEFAULT_SOURCE = ContextStoreEnum.REQUEST

/**
 * Policy variable resolver
 *
 * @param id Variable ID, optional
 * @param version Variable version in SemVer format, optional
 * @param description Variable description, optional
 * @param labels Variable labels used for searching, optional
 * @property source source store to use as input in resolver. Default value is Request store
 * @property key source store key to use as a full filter for key engine or prefilter for other
 *   engines
 * @property path parser instruction, mandatory for JMESPath and JQ engines
 * @property engine selected parsing engine. Possible values are key (default), JMESPath and JQ
 */
@Serializable
@SerialName("PolicyVariableResolver")
data class PolicyVariableResolver(
    override val id: String? = null,
    override val version: SemVer? = null,
    override val description: String? = null,
    override val labels: List<String>? = null,
    val source: ContextStoreEnum? = null,
    val key: String? = null,
    val path: String? = null,
    val engine: PolicyVariableResolverEngineEnum? = null
) : IManaged, IPolicyVariableResolverRefOrValue {
  @Transient private val logger = LoggerFactory.getLogger(this::class.java)
  @Transient private val marker = MarkerFactory.getMarker("PolicyVariableResolver")
  @Transient private var library: JqLibrary? = null

  @Transient private var expression: Expression<JsonNode>? = null

  @Transient private val idVer: String = if (version != null) "$id:$version" else id ?: ""

  private var _engine by Delegates.notNull<PolicyVariableResolverEngineEnum>()
  private var _source by Delegates.notNull<ContextStoreEnum>()

  init {
    this.validateId()
    labels?.also { require(it.isNotEmpty()) { "$idVer:Labels must not be empty array" } }
    _engine = engine ?: DEFAULT_ENGINE
    _source = source ?: DEFAULT_SOURCE

    when (_engine) {
      PolicyVariableResolverEngineEnum.KEY -> {
        require(!key.isNullOrBlank()) { "Key in $idVer cannot be blank" }
      }
      PolicyVariableResolverEngineEnum.JMES_PATH -> {
        require(!path.isNullOrBlank()) { "Path in $idVer cannot be blank" }
        val jmespath: JmesPath<JsonNode> = JacksonRuntime()
        expression = jmespath.compile(path)
      }
      PolicyVariableResolverEngineEnum.JQ -> {
        require(!path.isNullOrBlank()) { "Path in $idVer cannot be blank" }
        library = ImmutableJqLibrary.of()
      }
    }
  }

  /**
   * Resolves value from context. If value is not found then null result is returned. JMESPath
   * returns JSON_NODE VariableValues and JQ returns STRING VariableValues. Caching and type
   * coercion happens at [PolicyVariableDynamic] level
   *
   * @param context input context
   * @return value or null
   */
  fun resolve(context: Context): Any? {
    val idVerPath = context.getFullPath(idVer)
    logger.debug(marker, "${context.id}->$idVerPath:Resolving PolicyVariableResolver")
    try {
      // resolve source
      val resolvedSource: ContextStore? = context.store(_source)
      if (resolvedSource.isNullOrEmpty()) {
        // add to event
        context.event.add(
            context.id,
            PolicyEntityEnum.VALUE_RESOLVER,
            idVerPath,
            false,
            null,
            false,
            "No source found")
        logger.error(marker, "${context.id}->$idVerPath:No source found for $idVer")
        return null
      }
      // resolve key value. For key engine this will always go to else block
      val keyValue =
          if (key.isNullOrBlank()) context.store(_source) else context.store(_source)!![key]

      if (keyValue == null || keyValue is NullNode) {
        // add to event
        context.event.add(
            context.id,
            PolicyEntityEnum.VALUE_RESOLVER,
            idVerPath,
            false,
            null,
            false,
            "No value found")
        logger.warn(marker, "${context.id}->$idVerPath:No value found")
        return null
      }
      // return parsing result based on engine
      return when (_engine) {
        PolicyVariableResolverEngineEnum.KEY -> keyValue
        PolicyVariableResolverEngineEnum.JMES_PATH -> jmesPathResolver(keyValue, context, idVerPath)
        PolicyVariableResolverEngineEnum.JQ -> jqResolver(keyValue, context, idVerPath)
      }?.also {
        // add to event
        context.event.add(context.id, PolicyEntityEnum.VALUE_RESOLVER, idVerPath, true, it)
      }
    } catch (e: Exception) {
      // add to event
      context.event.add(
          context.id,
          PolicyEntityEnum.VALUE_RESOLVER,
          idVerPath,
          false,
          null,
          false,
          "${e::class.java.name}:${e.message}")
      logger.error(marker, "${context.id}->$idVerPath:Error resolving value:", e)
    }
    return null
  }

  /**
   * JQ resolver
   *
   * @param keyValue resolved source keyValue.
   * @param context execution context
   * @param idVerPath identity reference
   * @return value or null
   * @see <a href="https://jqlang.github.io/jq/">JQ</a>
   * @see <a href="https://github.com/arakelian/java-jq">Java JQ</a>
   */
  private fun jqResolver(keyValue: Any, context: Context, idVerPath: String): Any? {
    // translate keyValue to String
    val input: String =
        when (keyValue) {
          is String -> keyValue
          else -> {
            // try to find in cache
            val cacheKey = getKeyValueCacheKey(_source, key, path!!)
            val hasKey: Boolean =
                context.cache.hasKey(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, cacheKey)
            if (hasKey) context.cache.getStringKeyValue(cacheKey)!!
            else {
              // cache miss, convert to string
              val converted = context.options.objectMapper.writeValueAsString(keyValue)
              // cache value
              context.cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_STRING, cacheKey, converted)
              converted
            }
          }
        }
    logger.trace(marker, "${context.id}->$idVerPath: resolved input: {}", input)
    // prepare JQ
    val request = ImmutableJqRequest.builder().lib(library!!).input(input).filter(path!!).build()
    // execute JQ
    val response = request.execute()
    if (response.hasErrors()) {
      context.event.add(
          context.id,
          PolicyEntityEnum.VALUE_RESOLVER,
          idVerPath,
          false,
          null,
          false,
          "JQ error: ${response.errors}")
      logger.error(marker, "${context.id}->$idVerPath: JQ error: {}", response.errors)

      return null
    }
    // handle null result
    if (response.output == "null") {
      context.event.add(
          context.id,
          PolicyEntityEnum.VALUE_RESOLVER,
          idVerPath,
          false,
          null,
          false,
          "JQ: No value found")
      logger.warn(marker, "${context.id}->$idVerPath:No value found")
      return null
    }
    // handle ok result
    val rawResult = getRaw(response.output)
    return rawResult.also {
      logger.debug(marker, "${context.id}->$idVerPath: resolved value")
      logger.trace(marker, "${context.id}->$idVerPath: value: {}", it)
    }
  }

  /**
   * calculates keyValue cache key
   *
   * @param source context store
   * @param key context store key, optional
   * @param path parser instruction
   * @return cache key
   */
  private fun getKeyValueCacheKey(source: ContextStoreEnum, key: String?, path: String): String =
      "$source:${key ?: ""}:$path"

  /**
   * As JJQ wraps results in double quotes, without possibility to send --raw-output option, it is
   * necessary to unwrap it
   *
   * @param output response output
   * @return unwrapped string
   * @see <a href="https://github.com/arakelian/java-jq/issues/9">GitHub ticket</a>
   */
  private fun getRaw(output: String?): String? {
    if (output.isNullOrBlank()) return null
    return if (output.length >= 2 && output.startsWith("\"") && output.endsWith("\""))
        output.substring(1, output.length - 1)
    else output
  }

  /**
   * JMESPath resolver
   *
   * @param keyValue resolved source keyValue
   * @param context execution context
   * @param idVerPath identity reference
   * @return value or null
   * @see <a href="https://jmespath.org/">JMESPath</a>
   * @see <a href="https://github.com/burtcorp/jmespath-java">Java JMESPath</a>
   */
  private fun jmesPathResolver(keyValue: Any, context: Context, idVerPath: String): Any? {
    // translate keyValue to Jackson JsonNode
    val input: JsonNode =
        when (keyValue) {
          is JsonNode -> keyValue
          else -> {
            // try to find in cache
            val cacheKey = getKeyValueCacheKey(_source, key, path!!)
            val hasKey: Boolean =
                context.cache.hasKey(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, cacheKey)
            if (hasKey) context.cache.getJsonNodeKeyValue(cacheKey)!!
            else {
              // cache miss, convert to JsonNode
              val converted: JsonNode =
                  if (keyValue is String) context.options.objectMapper.readTree(keyValue)
                  else context.options.objectMapper.valueToTree(keyValue)
              // cache value
              context.cache.put(PolicyStoreCacheEnum.KEY_VALUE_AS_JSON_NODE, cacheKey, converted)
              converted
            }
          }
        }
    logger.trace(marker, "${context.id}->$idVerPath: resolved input: {}", input)
    // apply jmesPath expression
    val foundValue = expression!!.search(input)
    if (foundValue is NullNode)
        return null.also {
          context.event.add(
              context.id,
              PolicyEntityEnum.VALUE_RESOLVER,
              idVerPath,
              false,
              null,
              false,
              "JSONPath: No value found")
        }
    return foundValue.also {
      logger.debug(marker, "${context.id}->$idVerPath: resolved value")
      logger.trace(marker, "${context.id}->$idVerPath: value: {}", it)
    }
  }

  override fun identity(): String = idVer
}
