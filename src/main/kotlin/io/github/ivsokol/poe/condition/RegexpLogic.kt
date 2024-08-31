package io.github.ivsokol.poe.condition

import io.github.ivsokol.poe.Options
import io.github.ivsokol.poe.variable.VariableRuntimeTypeEnum
import io.github.ivsokol.poe.variable.VariableValue
import io.github.ivsokol.poe.variable.cast

/**
 * Checks if the provided [value] matches the given regular expression [regexp].
 *
 * @param value The value to be matched against the regular expression.
 * @param regexp The regular expression to match against the value.
 * @param options The options to be used during the matching process.
 * @return `true` if the value matches the regular expression, `false` otherwise.
 */
internal fun matchesRegexp(value: VariableValue, regexp: VariableValue, options: Options): Boolean {
  // cast value to string
  val preparedValue = cast(VariableRuntimeTypeEnum.STRING, value, options)
  return (preparedValue.body as String).matches((regexp.body as String).toRegex())
}
