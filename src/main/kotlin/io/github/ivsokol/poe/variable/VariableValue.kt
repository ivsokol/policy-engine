/* (C)2024 */
package io.github.ivsokol.poe.variable

/** Data class that contains variable value and its runtime type. */
data class VariableValue(val type: VariableRuntimeTypeEnum, val body: Any?)

/**
 * Constructor function for [VariableValue] that contains only variable value. Type is determined by
 * invoking determineRuntimeType function.
 */
internal fun VariableValue(body: Any?): VariableValue {
  if (body == null) return NullVariableValue()
  return VariableValue(determineRuntimeType(body), body)
}

/**
 * Constructor function for [VariableValue] that contains null variable value. Type is
 * [VariableRuntimeTypeEnum.NULL].
 */
internal fun NullVariableValue(): VariableValue {
  return VariableValue(VariableRuntimeTypeEnum.NULL, null)
}
