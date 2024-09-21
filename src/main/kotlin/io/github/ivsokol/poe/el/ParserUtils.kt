package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.el.ParserRegistry.maxCmdLength
import io.github.ivsokol.poe.el.ParserRegistry.minCmdLength
import io.github.ivsokol.poe.el.ParserRegistry.registry

/**
 * Checks if the given character is parseable in the context of the parser.
 *
 * Parseable characters include letters, digits, and a set of special characters defined in the
 * `SPECIAL_CHARS_DICTIONARY`.
 *
 * @param char The character to check.
 * @return `true` if the character is parseable, `false` otherwise.
 */
internal fun isParseableChar(char: Char): Boolean {
  return char.isLetterOrDigit() || SPECIAL_CHARS_DICTIONARY.contains(char)
}

/**
 * Determines the type of wrapper (if any) that surrounds the character at the given position in the
 * input string.
 *
 * The function checks for the following types of wrappers:
 * - Backtick (`)
 * - Triple quotes (`"""`)
 * - Double quotes (`"`)
 *
 * If a wrapper is found, the function returns the wrapper string. Otherwise, it returns `null`.
 *
 * @param pos The position in the input string to check for a wrapper.
 * @param input The input string to search.
 * @return The wrapper string, if found, or `null` if no wrapper is present.
 */
internal fun getWrapper(pos: Int, input: String): String? {
  if (input[pos] == '`') {
    return "`"
  }
  if ((input.length - pos >= 3) && input.substring(pos, pos + 3) == "\"\"\"") {
    return "\"\"\""
  }
  if (input[pos] == '"') {
    return "\""
  }
  return null
}

/**
 * Wraps the current string in double quotes if it is not already wrapped.
 *
 * This function is a utility for converting a string to a valid JSON string representation. If the
 * input string already starts and ends with double quotes, it is returned as-is. Otherwise, the
 * function adds double quotes around the string.
 *
 * @return The input string wrapped in double quotes, if it was not already wrapped.
 */
internal fun String.wrapToJsonString(): String =
    if (this.startsWith("\"") && this.endsWith("\"")) this else "\"$this\""

/**
 * Determines the type of character in the context of the parser.
 *
 * This function classifies a given character into one of the following types:
 * - `CharTypeEnum.CMD`: The character is a command character (# or *)
 * - `CharTypeEnum.CMD_START`: The character is the start of a command (CMD_START)
 * - `CharTypeEnum.CMD_END`: The character is the end of a command (CMD_END)
 * - `CharTypeEnum.DELIMITER`: The character is a delimiter (DELIMITER)
 * - `CharTypeEnum.NON_PARSABLE_CHAR`: The character is not parseable (based on the
 *   `isParseableChar` function)
 * - `CharTypeEnum.CONTENT`: The character is part of the content (not a special character)
 *
 * @param char The character to classify.
 * @return The type of the character in the context of the parser.
 */
internal fun charType(char: Char): CharTypeEnum {
  return when {
    char == '#' || char == '*' -> CharTypeEnum.CMD
    char == CMD_START -> CharTypeEnum.CMD_START
    char == CMD_END -> CharTypeEnum.CMD_END
    char == DELIMITER -> CharTypeEnum.DELIMITER
    isParseableChar(char).not() -> CharTypeEnum.NON_PARSABLE_CHAR
    else -> CharTypeEnum.CONTENT
  }
}

/**
 * Parses the name of a command from the input string.
 *
 * This function takes the current position in the input string and the full input string, and
 * attempts to parse the name of a command from the input. It searches the command registry to find
 * a matching command, and returns the corresponding [RegistryEntry].
 *
 * If the command is too short or the command is not found in the registry, an
 * [IllegalArgumentException] is thrown.
 *
 * @param pos The current position in the input string.
 * @param input The full input string.
 * @return The [RegistryEntry] for the parsed command.
 * @throws IllegalArgumentException If the command is too short or not found in the registry.
 */
internal fun parseCmdName(pos: Int, input: String): RegistryEntry {
  val charsUntilEnd = input.length - pos
  if (charsUntilEnd < minCmdLength) {
    throw IllegalArgumentException("Command too short on position $pos")
  }
  val command: String =
      input.substring(pos, pos + minOf(maxCmdLength, charsUntilEnd)).let {
        if (it.contains(CMD_START)) it.substring(0, it.indexOf(CMD_START)) else it
      }
  val commandWithStart = command + CMD_START

  val commandType =
      registry
          .find { it.command.startsWith(command) && commandWithStart.startsWith(it.command) }
          ?.also {
            var nextPos = pos + it.command.length
            nextPos = skipNonParseableChars(nextPos, input)
            if (input[nextPos] != CMD_START) {
              throw IllegalArgumentException(
                  "Expected command start after command on position $nextPos")
            }
          }
  return commandType ?: throw IllegalArgumentException("Unknown command $command on position $pos")
}

/**
 * Skips over any non-parseable characters in the input string starting from the given position.
 *
 * This function is used to advance the parsing position past any characters that are not part of
 * the parseable content, such as whitespace or other non-content characters.
 *
 * @param position The starting position in the input string to begin skipping non-parseable
 *   characters.
 * @param input The full input string to parse.
 * @return The new position in the input string after skipping any non-parseable characters.
 */
internal fun skipNonParseableChars(position: Int, input: String): Int {
  var pos = position
  while (pos < input.length && !isParseableChar(input[pos])) {
    pos++
  }
  return pos
}

/**
 * Parses the content from the input string, handling both wrapped and unwrapped content.
 *
 * This function takes the current position in the input string, the full input string, and an
 * optional delimiter character. It will parse the content from the input string, handling both
 * wrapped content (content enclosed in a specific wrapper) and unwrapped content (content delimited
 * by a specific character or the end of the command).
 *
 * If wrapped content is found, the function will parse the content within the wrapper and return
 * the parsed content and the new position in the input string. If no wrapper is found, the function
 * will parse the content delimited by the specified delimiter character or the end of the command,
 * and return the parsed content and the new position.
 *
 * @param position The current position in the input string to start parsing from.
 * @param input The full input string to parse.
 * @param delimiter The delimiter character to use for unwrapped content (default is DELIMITER).
 * @return A Pair containing the parsed content and the new position in the input string.
 * @throws IllegalArgumentException If the content is not properly wrapped or delimited.
 */
internal fun parseContent(
    position: Int,
    input: String,
    delimiter: Char = DELIMITER
): Pair<String, Int> {
  var pos = position
  // final position will be on delimiter or command end
  val content: String
  // check if wrappedContent
  val wrapper = getWrapper(pos, input)
  if (wrapper != null) {
    val result = parseWrappedContent(pos, input, wrapper)
    content = result.first
    pos = result.second

    if (input[pos] != delimiter && input[pos] != CMD_END) {
      throw IllegalArgumentException("Expected delimiter or command end after position $pos")
    }
    // wrapped body is not trimmed
    // position will be on delimiter or command end
    return Pair(content, pos)
  }
  // no wrapper
  val endIdxDelim = input.indexOf(delimiter, pos).let { if (it == -1) Int.MAX_VALUE else it }
  val endIdxCmd = input.indexOf(CMD_END, pos).let { if (it == -1) Int.MAX_VALUE else it }
  if (endIdxDelim == Int.MAX_VALUE && endIdxCmd == Int.MAX_VALUE) {
    throw IllegalArgumentException("Expected command end or delimiter after position $pos")
  }
  val endIdx = minOf(endIdxDelim, endIdxCmd)
  content = input.substring(pos, endIdx)
  pos = endIdx

  return Pair(content.trim(), pos)
}

/**
 * Parses the content within a wrapped string in the input.
 *
 * This function is used to parse content that is enclosed within a specific wrapper in the input
 * string. It will extract the content within the wrapper and return it along with the new position
 * in the input string after the wrapper.
 *
 * @param position The current position in the input string to start parsing from.
 * @param input The full input string to parse.
 * @param wrapper The wrapper string that encloses the content to be parsed.
 * @return A Pair containing the parsed content and the new position in the input string.
 * @throws IllegalArgumentException If the content is not properly wrapped.
 */
private fun parseWrappedContent(position: Int, input: String, wrapper: String): Pair<String, Int> {
  var pos = position + wrapper.length
  // final position will be on delimiter or command end
  var body: String
  val endIdxWrapper = input.indexOf(wrapper, pos)
  if (endIdxWrapper == -1) {
    // no end wrapper found
    throw IllegalArgumentException("Expected wrapper end after position $pos")
  }
  body = input.substring(pos, endIdxWrapper)
  pos = endIdxWrapper
  // move index to next char after wrapper
  pos += wrapper.length
  // adjust position if " on """ wrapper end
  if (wrapper.length == 3) {
    var counter = 0
    while (input[pos] == '"') {
      pos++
      counter++
    }
    body = body.padEnd(body.length + counter, '"')
  }
  // move index to next parseable char
  pos = skipNonParseableChars(pos, input)
  return Pair(body, pos)
}
