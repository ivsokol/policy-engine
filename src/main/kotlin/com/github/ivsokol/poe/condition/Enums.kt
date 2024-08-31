package com.github.ivsokol.poe.condition

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Possible Condition operations */
@Serializable
enum class OperationEnum {
  @SerialName("GreaterThan") GREATER_THAN,
  @SerialName("GreaterThanEqual") GREATER_THAN_EQUAL,
  @SerialName("LessThan") LESS_THAN,
  @SerialName("LessThanEqual") LESS_THAN_EQUAL,
  @SerialName("IsNull") IS_NULL,
  @SerialName("IsNotNull") IS_NOT_NULL,
  @SerialName("IsEmpty") IS_EMPTY,
  @SerialName("IsNotEmpty") IS_NOT_EMPTY,
  @SerialName("IsBlank") IS_BLANK,
  @SerialName("IsNotBlank") IS_NOT_BLANK,
  @SerialName("StartsWith") STARTS_WITH,
  @SerialName("EndsWith") ENDS_WITH,
  @SerialName("Contains") CONTAINS,
  @SerialName("IsIn") IS_IN,
  @SerialName("Equals") EQUALS,
  @SerialName("IsPositive") IS_POSITIVE,
  @SerialName("IsNegative") IS_NEGATIVE,
  @SerialName("IsZero") IS_ZERO,
  @SerialName("IsPast") IS_PAST,
  @SerialName("IsFuture") IS_FUTURE,
  @SerialName("RegexpMatch") REGEXP_MATCH,
  @SerialName("HasKey") HAS_KEY,
  @SerialName("IsUnique") IS_UNIQUE,
  @SerialName("SchemaMatch") SCHEMA_MATCH
}

/** Possible Condition combinations */
@Serializable
enum class ConditionCombinationLogicEnum {
  @SerialName("anyOf") ANY_OF,
  @SerialName("allOf") ALL_OF,
  @SerialName("not") NOT,
  @SerialName("nOf") N_OF,
}
