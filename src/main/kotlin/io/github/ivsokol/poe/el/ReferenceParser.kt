package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.el.ParserRegistry.policyRefRegistry

/**
 * Parses a reference command from the input string and returns the list of entries and the end
 * position of the reference command.
 *
 * @param position the starting position of the reference command in the input string
 * @param input the input string containing the reference command
 * @return a pair containing the list of entries and the end position of the reference command
 * @throws IllegalArgumentException if the provided command is not a valid reference command
 * @throws IllegalStateException if the reference command does not have at least one entry or has
 *   more than two entries
 */
internal fun parseRef(position: Int, input: String): Pair<List<String>, Int> {
  // end position will be after end of ref cmd
  var pos = position
  val cmd = parseCmdName(position, input)
  require(cmd.entryType == EntryTypeEnum.REF) {
    "Bad command provided for Reference parser: '${cmd.command}'"
  }
  pos += cmd.command.length
  pos = skipNonParseableChars(pos, input)
  val entries = mutableListOf<String>()
  var line: String? = null
  do {
    val chr = input[pos]
    if (chr == CMD_START) {
      pos++
      continue
    }
    if (chr == CMD_END) {
      if (!line.isNullOrEmpty()) {
        entries.add(line)
      }
      pos++
      break
    }
    if (chr == DELIMITER) {
      // save parsed value
      if (!line.isNullOrEmpty()) {
        entries.add(line)
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
  check(entries.isNotEmpty()) { "Reference command should have at least one entry" }
  check(entries.size <= 2) { "Reference command should have at most two entries" }

  return entries to pos
}

/**
 * Deserializes a reference from a list of string entries.
 *
 * @param parentEntityType The type of the parent entity that the reference belongs to.
 * @param entries The list of string entries that make up the reference.
 * @return The deserialized reference object.
 * @throws IllegalArgumentException if the parent entity type is not supported.
 */
internal fun deserializeRef(parentEntityType: PolicyEntityEnum, entries: List<String>): Any {
  val entityRef =
      when (parentEntityType) {
        PolicyEntityEnum.VARIABLE_DYNAMIC -> PolicyEntityRefEnum.POLICY_VARIABLE_RESOLVER_REF
        PolicyEntityEnum.CONDITION_ATOMIC -> PolicyEntityRefEnum.POLICY_VARIABLE_REF
        PolicyEntityEnum.CONDITION_COMPOSITE -> PolicyEntityRefEnum.POLICY_CONDITION_REF
        PolicyEntityEnum.POLICY_ACTION -> PolicyEntityRefEnum.POLICY_VARIABLE_REF
        PolicyEntityEnum.POLICY -> PolicyEntityRefEnum.POLICY_CONDITION_REF
        PolicyEntityEnum.POLICY_SET -> PolicyEntityRefEnum.POLICY_REF
        PolicyEntityEnum.CONSTRAINT -> PolicyEntityRefEnum.POLICY_CONDITION_REF
        PolicyEntityEnum.ACTION_RELATIONSHIP -> PolicyEntityRefEnum.POLICY_ACTION_REF
        PolicyEntityEnum.POLICY_RELATIONSHIP -> PolicyEntityRefEnum.POLICY_REF
        else ->
            throw IllegalArgumentException(
                "Unsupported parent entity type: $parentEntityType for ReferenceDeserializer")
      }
  return ReferenceELDeserializer.deserialize(
      policyRefRegistry[0], emptyList(), entries, null, null, entityRef)
}
