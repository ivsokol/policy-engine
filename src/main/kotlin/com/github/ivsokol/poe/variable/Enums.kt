/* (C)2024 */
package com.github.ivsokol.poe.variable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enum representing the possible context stores for a variable.
 * - `REQUEST`: The variable is stored in the current request context.
 * - `ENVIRONMENT`: The variable is stored in the environment context.
 * - `SUBJECT`: The variable is stored in the subject context.
 * - `DATA`: The variable is stored in the data context.
 */
@Serializable
enum class ContextStoreEnum {
  @SerialName("request") REQUEST,
  @SerialName("environment") ENVIRONMENT,
  @SerialName("subject") SUBJECT,
  @SerialName("data") DATA,
}

/**
 * Enum representing the possible types of variable values.
 * - `STRING`: The variable value is a string.
 * - `INT`: The variable value is an integer.
 * - `NUMBER`: The variable value is a number (float or double).
 * - `BOOLEAN`: The variable value is a boolean.
 * - `OBJECT`: The variable value is a JSON object.
 * - `ARRAY`: The variable value is a JSON array.
 */
@Serializable
enum class VariableValueTypeEnum {
  @SerialName("string") STRING,
  @SerialName("int") INT,
  @SerialName("number") NUMBER,
  @SerialName("boolean") BOOLEAN,
  @SerialName("object") OBJECT,
  @SerialName("array") ARRAY,
}

/**
 * Enum representing the possible formats for variable values.
 * - `DATE`: The variable value is a date.
 * - `DATE_TIME`: The variable value is a date and time.
 * - `TIME`: The variable value is a time.
 * - `PERIOD`: The variable value is a period of time.
 * - `DURATION`: The variable value is a duration of time.
 * - `LONG`: The variable value is a long integer.
 * - `DOUBLE`: The variable value is a double-precision floating-point number.
 * - `FLOAT`: The variable value is a single-precision floating-point number.
 * - `BIG_DECIMAL`: The variable value is a big decimal number.
 * - `JSON`: The variable value is a JSON object.
 */
@Serializable
enum class VariableValueFormatEnum {
  @SerialName("date") DATE,
  @SerialName("date-time") DATE_TIME,
  @SerialName("time") TIME,
  @SerialName("period") PERIOD,
  @SerialName("duration") DURATION,
  @SerialName("long") LONG,
  @SerialName("double") DOUBLE,
  @SerialName("float") FLOAT,
  @SerialName("big-decimal") BIG_DECIMAL,
  @SerialName("JSON") JSON,
}

/**
 * Enum representing the possible engines for resolving policy variables.
 * - `JMES_PATH`: The variable value is resolved using the JMESPath query language.
 * - `JQ`: The variable value is resolved using the jq command-line JSON processor.
 * - `KEY`: The variable value is resolved by looking up a key in the variable data.
 */
@Serializable
enum class PolicyVariableResolverEngineEnum {
  @SerialName("JMESPath") JMES_PATH,
  @SerialName("JQ") JQ,
  @SerialName("key") KEY,
}

/**
 * Enum representing the possible runtime types for variable values.
 * - `NULL`: The variable value is null.
 * - `UNKNOWN`: The variable value type is unknown.
 * - `STRING`: The variable value is a string.
 * - `DATE`: The variable value is a date.
 * - `DATE_TIME`: The variable value is a date and time.
 * - `TIME`: The variable value is a time.
 * - `PERIOD`: The variable value is a period of time.
 * - `DURATION`: The variable value is a duration of time.
 * - `LONG`: The variable value is a long integer.
 * - `INT`: The variable value is an integer.
 * - `DOUBLE`: The variable value is a double-precision floating-point number.
 * - `FLOAT`: The variable value is a single-precision floating-point number.
 * - `BIG_DECIMAL`: The variable value is a big decimal number.
 * - `BOOLEAN`: The variable value is a boolean.
 * - `JSON_NODE`: The variable value is a JSON node.
 * - `OBJECT_NODE`: The variable value is a JSON object node.
 * - `ARRAY_NODE`: The variable value is a JSON array node.
 * - `ARRAY`: The variable value is an array.
 */
enum class VariableRuntimeTypeEnum {
  NULL,
  UNKNOWN,
  STRING,
  DATE,
  DATE_TIME,
  TIME,
  PERIOD,
  DURATION,
  LONG,
  INT,
  DOUBLE,
  FLOAT,
  BIG_DECIMAL,
  BOOLEAN,
  JSON_NODE,
  OBJECT_NODE,
  ARRAY_NODE,
  ARRAY,
}
