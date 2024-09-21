package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.SemVerSerializer
import io.github.ivsokol.poe.policy.ActionExecutionModeEnum
import io.github.ivsokol.poe.policy.ActionExecutionStrategyEnum
import io.github.ivsokol.poe.variable.ContextStoreEnum
import io.github.ivsokol.poe.variable.VariableValueFormatEnum
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import kotlinx.serialization.json.Json

internal data class ELOptions(
    val id: String? = null,
    val version: SemVer? = null,
    val description: String? = null,
    val labels: List<String>? = null,
    val executionMode: Set<ActionExecutionModeEnum>? = null,
    val priority: Int? = null,
    val isJson: Boolean? = null,
    val dateFormat: String? = null,
    val timeFormat: String? = null,
    val dateTimeFormat: String? = null,
    val type: VariableValueTypeEnum? = null,
    val format: VariableValueFormatEnum? = null,
    val key: String? = null,
    val source: ContextStoreEnum? = null,
    val negateResult: Boolean? = null,
    val stringIgnoreCase: Boolean? = null,
    val fieldsStrictCheck: Boolean? = null,
    val arrayOrderStrictCheck: Boolean? = null,
    val strictCheck: Boolean? = null,
    val minimumConditions: Int? = null,
    val optimizeNOfRun: Boolean? = null,
    val lenientConstraints: Boolean? = null,
    val actionExecutionStrategy: ActionExecutionStrategyEnum? = null,
    val ignoreErrors: Boolean? = null,
    val strictTargetEffect: Boolean? = null,
    val skipCache: Boolean? = null,
    val runChildActions: Boolean? = null,
    val runAction: Boolean? = null,
    val indeterminateOnActionFail: Boolean? = null,
    val strictUnlessLogic: Boolean? = null,
    val failOnMissingKey: Boolean? = null,
    val failOnExistingKey: Boolean? = null,
    val failOnNullSource: Boolean? = null,
    val castNullSourceToArray: Boolean? = null,
    val failOnNullMerge: Boolean? = null,
)

/**
 * Checks if the given input string represents a boolean option.
 *
 * @param input the input string to check
 * @return `true` if the input string represents a boolean option, `false` otherwise
 */
private fun isOptionBoolean(input: String): Boolean {
  return input in
      listOf(
          "isJson",
          "negateResult",
          "stringIgnoreCase",
          "fieldsStrictCheck",
          "arrayOrderStrictCheck",
          "strictCheck",
          "optimize",
          "lenientConstraints",
          "ignoreErrors",
          "strictTargetEffect",
          "skipCache",
          "runChildActions",
          "runAction",
          "indeterminateOnActionFail",
          "strictUnlessLogic",
          "failOnMissingKey",
          "failOnExistingKey",
          "failOnNullSource",
          "castNullSourceToArray",
          "failOnNullMerge")
}

/**
 * Parses the options from the input string and returns an [ELOptions] instance along with the
 * position in the input string where the parsing ended.
 *
 * @param position the starting position in the input string to begin parsing
 * @param input the input string containing the options to be parsed
 * @return a Pair containing the parsed [ELOptions] instance and the position in the input string
 *   where the parsing ended
 * @throws IllegalArgumentException if the input string does not contain a valid options command
 */
internal fun parseOptions(position: Int, input: String): Pair<ELOptions, Int> {
  // end position will be after end of opts cmd
  var pos = position
  val cmd = parseCmdName(position, input)
  require(cmd.entryType == EntryTypeEnum.OPTIONS) {
    "Bad command provided for Options parser: '${cmd.command}'"
  }
  pos += cmd.command.length
  pos = skipNonParseableChars(pos, input)
  val optionEntries = mutableMapOf<String, String>()
  var line: String? = null
  do {
    val chr = input[pos]
    if (chr == CMD_START) {
      pos++
      continue
    }
    if (chr == CMD_END) {
      if (!line.isNullOrEmpty()) {
        val parsedLine = parseOptionLine(line)
        optionEntries[parsedLine.first] = parsedLine.second
      }
      pos++
      break
    }
    if (chr == DELIMITER) {
      // split parsed line into key and value
      if (!line.isNullOrEmpty()) {
        val parsedLine = parseOptionLine(line)
        optionEntries[parsedLine.first] = parsedLine.second
        line = null
      }
      pos++
      continue
    }
    if (!isParseableChar(chr)) {
      pos++
      continue
    }
    val endIdx = input.indexOf(CMD_END, pos).let { if (it == -1) Int.MAX_VALUE else it }
    val delimIdx = input.indexOf(DELIMITER, pos).let { if (it == -1) Int.MAX_VALUE else it }
    val content = parseContent(pos, input, if (endIdx < delimIdx) CMD_END else DELIMITER)
    line = content.first
    pos = content.second
  } while (pos < input.length)

  return Pair(
      ELOptions(
          id = optionEntries["id"],
          version =
              optionEntries["ver"]?.let {
                Json.decodeFromString(SemVerSerializer, it.wrapToJsonString())
              },
          description = optionEntries["desc"],
          labels =
              optionEntries["labels"]
                  ?.split("|")
                  ?.filter { it.isNotBlank() }
                  ?.takeIf { it.isNotEmpty() },
          executionMode =
              optionEntries["executionMode"]
                  ?.split("|")
                  ?.filter { it.isNotBlank() }
                  ?.map { em ->
                    Json.decodeFromString(
                        ActionExecutionModeEnum.serializer(), em.wrapToJsonString())
                  }
                  ?.toSet(),
          priority = optionEntries["priority"]?.toIntOrNull(),
          isJson = optionEntries["isJson"]?.toBooleanStrictOrNull(),
          dateFormat = optionEntries["dateFormat"],
          timeFormat = optionEntries["timeFormat"],
          dateTimeFormat = optionEntries["dateTimeFormat"],
          type =
              optionEntries["type"]?.let {
                Json.decodeFromString(VariableValueTypeEnum.serializer(), it.wrapToJsonString())
              },
          format =
              optionEntries["format"]?.let {
                Json.decodeFromString(VariableValueFormatEnum.serializer(), it.wrapToJsonString())
              },
          key = optionEntries["key"],
          source =
              optionEntries["source"]?.let {
                Json.decodeFromString(ContextStoreEnum.serializer(), it.wrapToJsonString())
              },
          negateResult = optionEntries["negateResult"]?.toBooleanStrictOrNull(),
          stringIgnoreCase = optionEntries["stringIgnoreCase"]?.toBooleanStrictOrNull(),
          fieldsStrictCheck = optionEntries["fieldsStrictCheck"]?.toBooleanStrictOrNull(),
          arrayOrderStrictCheck = optionEntries["arrayOrderStrictCheck"]?.toBooleanStrictOrNull(),
          strictCheck = optionEntries["strictCheck"]?.toBooleanStrictOrNull(),
          minimumConditions = optionEntries["minimumConditions"]?.toIntOrNull(),
          optimizeNOfRun = optionEntries["optimize"]?.toBooleanStrictOrNull(),
          lenientConstraints = optionEntries["lenientConstraints"]?.toBooleanStrictOrNull(),
          actionExecutionStrategy =
              optionEntries["actionExecutionStrategy"]?.let {
                Json.decodeFromString(
                    ActionExecutionStrategyEnum.serializer(), it.wrapToJsonString())
              },
          ignoreErrors = optionEntries["ignoreErrors"]?.toBooleanStrictOrNull(),
          strictTargetEffect = optionEntries["strictTargetEffect"]?.toBooleanStrictOrNull(),
          skipCache = optionEntries["skipCache"]?.toBooleanStrictOrNull(),
          runChildActions = optionEntries["runChildActions"]?.toBooleanStrictOrNull(),
          runAction = optionEntries["runAction"]?.toBooleanStrictOrNull(),
          indeterminateOnActionFail =
              optionEntries["indeterminateOnActionFail"]?.toBooleanStrictOrNull(),
          strictUnlessLogic = optionEntries["strictUnlessLogic"]?.toBooleanStrictOrNull(),
          failOnMissingKey = optionEntries["failOnMissingKey"]?.toBooleanStrictOrNull(),
          failOnExistingKey = optionEntries["failOnExistingKey"]?.toBooleanStrictOrNull(),
          failOnNullSource = optionEntries["failOnNullSource"]?.toBooleanStrictOrNull(),
          castNullSourceToArray = optionEntries["castNullSourceToArray"]?.toBooleanStrictOrNull(),
          failOnNullMerge = optionEntries["failOnNullMerge"]?.toBooleanStrictOrNull()),
      pos)
}

/**
 * Parses an option line into a key-value pair.
 *
 * The option line is expected to be in the format "key=value". If the key is a boolean, the value
 * is assumed to be "true" if the key is present without a value.
 *
 * @param line The option line to parse.
 * @return A Pair containing the parsed key and value.
 * @throws IllegalArgumentException if the option line is empty, the key is empty, or the value is
 *   empty.
 */
private fun parseOptionLine(line: String): Pair<String, String> {
  require(line.isNotEmpty()) { "Option line is empty" }
  val adjustedLine = line + CMD_END
  val keyParsed = parseContent(0, adjustedLine, OPTION_KEY_VALUE_DELIMITER)
  val nextPos = keyParsed.second + 1
  val key = keyParsed.first.trim()
  check(key.isNotBlank()) { "Option key is empty for entry '$line'" }
  if (isOptionBoolean(key) && nextPos >= line.length) {
    return Pair(key, "true")
  }
  if (nextPos >= line.length) {
    throw IllegalArgumentException("Option value is empty for entry '$line'")
  }
  val valueParsed = parseContent(nextPos, adjustedLine, CMD_END)
  val value = valueParsed.first
  return Pair(key, value)
}
