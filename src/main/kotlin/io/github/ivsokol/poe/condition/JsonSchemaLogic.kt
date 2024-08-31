package io.github.ivsokol.poe.condition

import com.fasterxml.jackson.databind.node.*
import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.Options
import io.github.ivsokol.poe.variable.NullVariableValue
import io.github.ivsokol.poe.variable.VariableRuntimeTypeEnum
import io.github.ivsokol.poe.variable.VariableValue
import io.github.ivsokol.poe.variable.cast
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.json.schema.*
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

private val logger = LoggerFactory.getLogger(PolicyConditionAtomic::class.java)
private val marker = MarkerFactory.getMarker("PolicyCondition")

/**
 * Checks if the provided JSON data matches the given JSON schema.
 *
 * @param json The JSON data to validate.
 * @param schema The JSON schema to validate the data against.
 * @param context The context containing options for the validation.
 * @param conditionId The ID of the condition being validated.
 * @return `true` if the JSON data matches the schema, `false` otherwise.
 */
internal fun matchesSchema(
    json: VariableValue,
    schema: VariableValue,
    context: Context,
    conditionId: String
): Boolean {
  // if schema is string, check is it json object
  if (schema.type == VariableRuntimeTypeEnum.STRING) {
    // cast will throw if schema is not json object
    cast(VariableRuntimeTypeEnum.OBJECT_NODE, schema, context.options)
  }
  val preparedSchema = cast(VariableRuntimeTypeEnum.STRING, schema, context.options)
  val validator = schemaToValidator(preparedSchema.body as String, context.options)
  val preparedBody = prepareForValidation(json, context.options, conditionId)
  val validationResult = validator.validate(preparedBody.body)
  return if (validationResult.valid) {
    true
  } else {
    val errors =
        validationResult.errors.mapNotNull { e -> e.error }.toList().joinToString(separator = ",")
    logger.debug(marker, "$conditionId -> Validation returned errors: {}", errors)
    false
  }
}

/**
 * Converts the provided JSON schema string into a [Validator] instance.
 *
 * @param schema The JSON schema string to convert.
 * @param options The options to use for the validator configuration.
 * @return A [Validator] instance that can be used to validate JSON data against the provided
 *   schema.
 */
private fun schemaToValidator(schema: String, options: Options): Validator {
  val jsonObject = JsonObject(schema)
  val jsonSchema = JsonSchema.of(jsonObject)
  return Validator.create(
      jsonSchema,
      JsonSchemaOptions()
          .setDraft(Draft.DRAFT202012)
          .setBaseUri(options.defaultSchemaUri)
          .setOutputFormat(OutputFormat.Basic),
  )
}

/**
 * Prepares the provided [VariableValue] input for validation against a JSON schema.
 *
 * This function handles the various types of [VariableValue] and performs the necessary conversions
 * to ensure the input is in a format that can be validated against the schema.
 *
 * @param input The [VariableValue] to prepare for validation.
 * @param options The [Options] to use for the validation.
 * @param conditionId The ID of the condition being validated.
 * @return The prepared [VariableValue] that can be used for validation.
 * @throws IllegalArgumentException if the input [VariableValue] has an unsupported type.
 */
private fun prepareForValidation(
    input: VariableValue,
    options: Options,
    conditionId: String
): VariableValue =
    when (input.type) {
      VariableRuntimeTypeEnum.BIG_DECIMAL,
      VariableRuntimeTypeEnum.LONG,
      VariableRuntimeTypeEnum.INT,
      VariableRuntimeTypeEnum.DOUBLE,
      VariableRuntimeTypeEnum.FLOAT,
      VariableRuntimeTypeEnum.BOOLEAN,
      VariableRuntimeTypeEnum.STRING -> input
      VariableRuntimeTypeEnum.NULL -> NullVariableValue()
      VariableRuntimeTypeEnum.UNKNOWN ->
          throw IllegalArgumentException(
              "$conditionId -> Variable type UNKNOWN is not supported in condition")
      VariableRuntimeTypeEnum.DATE,
      VariableRuntimeTypeEnum.DATE_TIME,
      VariableRuntimeTypeEnum.TIME,
      VariableRuntimeTypeEnum.PERIOD,
      VariableRuntimeTypeEnum.DURATION -> cast(VariableRuntimeTypeEnum.STRING, input, options)
      VariableRuntimeTypeEnum.ARRAY ->
          castArrayNode(
              cast(VariableRuntimeTypeEnum.ARRAY_NODE, input, options).body as ArrayNode, options)
      VariableRuntimeTypeEnum.ARRAY_NODE -> castArrayNode(input.body as ArrayNode, options)
      VariableRuntimeTypeEnum.JSON_NODE -> castJsonNode(input, options, conditionId)
      VariableRuntimeTypeEnum.OBJECT_NODE -> castObjectNode(input.body as ObjectNode, options)
    }

/**
 * Casts the provided [VariableValue] input to the appropriate type based on the underlying
 * JsonNode.
 *
 * This function handles the various types of JsonNode and performs the necessary conversions to
 * ensure the input is in a format that can be used for further processing.
 *
 * @param input The [VariableValue] to cast.
 * @param options The [Options] to use for the casting.
 * @param conditionId The ID of the condition being processed.
 * @return The cast [VariableValue] that can be used for further processing.
 * @throws IllegalArgumentException if the input [VariableValue] has an unsupported JsonNode type.
 */
private fun castJsonNode(
    input: VariableValue,
    options: Options,
    conditionId: String
): VariableValue =
    when (input.body) {
      is ObjectNode -> castObjectNode(input.body, options)
      is ArrayNode -> castArrayNode(input.body, options)
      is NullNode -> NullVariableValue()
      is ValueNode -> castValueNode(input.body)
      else -> throw IllegalArgumentException("$conditionId -> JsonNode casting failed")
    }

/**
 * Casts the provided [ValueNode] to the appropriate [VariableValue] type based on the underlying
 * node type.
 *
 * This function handles the various types of [ValueNode] and performs the necessary conversions to
 * ensure the input is in a format that can be used for further processing.
 *
 * @param body The [ValueNode] to cast.
 * @return The cast [VariableValue] that can be used for further processing.
 */
private fun castValueNode(body: ValueNode): VariableValue =
    when (body) {
      is BooleanNode -> VariableValue(VariableRuntimeTypeEnum.BOOLEAN, body.asBoolean())
      is DoubleNode -> VariableValue(VariableRuntimeTypeEnum.DOUBLE, body.asDouble())
      is DecimalNode -> VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, body.decimalValue())
      is LongNode -> VariableValue(VariableRuntimeTypeEnum.LONG, body.asLong())
      is FloatNode -> VariableValue(VariableRuntimeTypeEnum.FLOAT, body.asDouble())
      is IntNode -> VariableValue(VariableRuntimeTypeEnum.INT, body.asInt())
      else -> VariableValue(VariableRuntimeTypeEnum.STRING, body.asText())
    }

/**
 * Casts the provided [ObjectNode] to a [VariableValue] with an UNKNOWN runtime type.
 *
 * This function converts the [ObjectNode] to a JSON string representation using the provided
 * [Options.objectMapper] and wraps it in a [VariableValue] with the UNKNOWN runtime type.
 *
 * @param body The [ObjectNode] to cast.
 * @param options The [Options] to use for the casting.
 * @return The cast [VariableValue] with an UNKNOWN runtime type.
 */
private fun castObjectNode(body: ObjectNode, options: Options): VariableValue =
    VariableValue(
        VariableRuntimeTypeEnum.UNKNOWN, JsonObject(options.objectMapper.writeValueAsString(body)))

/**
 * Casts the provided [ArrayNode] to a [VariableValue] with an UNKNOWN runtime type.
 *
 * This function converts the [ArrayNode] to a JSON string representation using the provided
 * [Options.objectMapper] and wraps it in a [VariableValue] with the UNKNOWN runtime type.
 *
 * @param body The [ArrayNode] to cast.
 * @param options The [Options] to use for the casting.
 * @return The cast [VariableValue] with an UNKNOWN runtime type.
 */
private fun castArrayNode(body: ArrayNode, options: Options): VariableValue =
    VariableValue(
        VariableRuntimeTypeEnum.UNKNOWN, JsonArray(options.objectMapper.writeValueAsString(body)))
