package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.PolicyEntityRefEnum
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import io.github.ivsokol.poe.variable.VariableValueFormatEnum
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import java.time.*
import java.time.format.DateTimeFormatter

internal object PolicyVariableStaticELDeserializer : CommandDeserializer {
  /**
   * Deserializes a policy variable static command from a registry entry.
   *
   * This deserializer can only be used with the 'VARIABLE_STATIC' command type. It expects a single
   * content string and no child commands.
   *
   * The deserializer supports various value types, including string, date, time, date-time, period,
   * duration, integer, long, number, float, big decimal, and boolean. It also supports JSON and
   * array formats.
   *
   * @param command The registry entry containing the command to deserialize.
   * @param childCommands The list of child commands, which must be empty for a policy variable
   *   static.
   * @param contents The list of content strings, which must contain a single element.
   * @param options Optional EL options, which may contain formatting information.
   * @param constraint An optional constraint object.
   * @param refType An optional policy entity reference type.
   * @return The deserialized policy variable static.
   */
  override fun deserialize(
      command: RegistryEntry,
      childCommands: List<Any>,
      contents: List<String>,
      options: ELOptions?,
      constraint: Any?,
      refType: PolicyEntityRefEnum?
  ): Any {
    check(command.entityType == PolicyEntityEnum.VARIABLE_STATIC) {
      "PolicyVariableStaticDeserializer can only be used with '${command.command}' command"
    }
    check(contents.size == 1) { "PolicyVariableStatic can only have one parsed content" }
    check(childCommands.isEmpty()) { "PolicyVariableStatic can not have child commands" }

    val body = contents.first()
    return when (command.entryType) {
      // string
      EntryTypeEnum.STRING ->
          PolicyVariableStatic(
              value = if (options?.isJson == true) body.wrapToJsonString() else body,
              type = VariableValueTypeEnum.STRING,
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              format = if (options?.isJson == true) VariableValueFormatEnum.JSON else null,
          )
      EntryTypeEnum.DATE ->
          PolicyVariableStatic(
              value =
                  LocalDate.parse(
                      body,
                      options?.dateFormat?.let { DateTimeFormatter.ofPattern(it) }
                          ?: DateTimeFormatter.ISO_LOCAL_DATE),
              type = VariableValueTypeEnum.STRING,
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              dateFormat = options?.dateFormat,
              format = VariableValueFormatEnum.DATE,
          )
      EntryTypeEnum.TIME ->
          PolicyVariableStatic(
              value =
                  LocalTime.parse(
                      body,
                      options?.timeFormat?.let { DateTimeFormatter.ofPattern(it) }
                          ?: DateTimeFormatter.ISO_LOCAL_TIME),
              type = VariableValueTypeEnum.STRING,
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              timeFormat = options?.timeFormat,
              format = VariableValueFormatEnum.TIME,
          )
      EntryTypeEnum.DATE_TIME ->
          PolicyVariableStatic(
              value =
                  OffsetDateTime.parse(
                      body,
                      options?.dateTimeFormat?.let { DateTimeFormatter.ofPattern(it) }
                          ?: DateTimeFormatter.ISO_OFFSET_DATE_TIME),
              type = VariableValueTypeEnum.STRING,
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              dateTimeFormat = options?.dateTimeFormat,
              format = VariableValueFormatEnum.DATE_TIME,
          )
      EntryTypeEnum.PERIOD ->
          PolicyVariableStatic(
              value = Period.parse(body),
              type = VariableValueTypeEnum.STRING,
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              format = VariableValueFormatEnum.PERIOD,
          )
      EntryTypeEnum.DURATION ->
          PolicyVariableStatic(
              value = Duration.parse(body),
              type = VariableValueTypeEnum.STRING,
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              format = VariableValueFormatEnum.DURATION,
          )

      // int
      EntryTypeEnum.INT ->
          PolicyVariableStatic(
              value = body.toInt(),
              type = VariableValueTypeEnum.INT,
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              format = null,
          )
      EntryTypeEnum.LONG ->
          PolicyVariableStatic(
              value = body.toLong(),
              type = VariableValueTypeEnum.INT,
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              format = VariableValueFormatEnum.LONG,
          )
      // number
      EntryTypeEnum.NUM ->
          PolicyVariableStatic(
              value = body.toDouble(),
              type = VariableValueTypeEnum.NUMBER,
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              format = null,
          )
      EntryTypeEnum.FLOAT ->
          PolicyVariableStatic(
              value = body.toFloat(),
              type = VariableValueTypeEnum.NUMBER,
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              format = VariableValueFormatEnum.FLOAT,
          )
      EntryTypeEnum.BIG_DECIMAL ->
          PolicyVariableStatic(
              value = body.toBigDecimal(),
              type = VariableValueTypeEnum.NUMBER,
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              format = VariableValueFormatEnum.BIG_DECIMAL,
          )
      // boolean
      EntryTypeEnum.BOOLEAN ->
          PolicyVariableStatic(
              value = body.toBoolean(),
              type = VariableValueTypeEnum.BOOLEAN,
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              format = null,
          )
      EntryTypeEnum.OBJECT_NODE ->
          PolicyVariableStatic(
              value = body,
              type = VariableValueTypeEnum.OBJECT,
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              format = VariableValueFormatEnum.JSON,
          )
      EntryTypeEnum.ARRAY_NODE,
      EntryTypeEnum.ARRAY ->
          PolicyVariableStatic(
              value = body,
              type = VariableValueTypeEnum.ARRAY,
              id = options?.id,
              version = options?.version,
              description = options?.description,
              labels = options?.labels,
              format = VariableValueFormatEnum.JSON,
          )
      // others
      else -> throw IllegalArgumentException("Unsupported type: ${command.entryType}")
    }
  }
}
