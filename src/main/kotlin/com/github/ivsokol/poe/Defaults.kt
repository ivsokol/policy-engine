/* (C)2024 */
package com.github.ivsokol.poe

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Creates a default Jackson [ObjectMapper] instance with the following configuration:
 * - Registers the [JavaTimeModule] to handle Java 8 date/time types
 * - Sets the serialization inclusion to [JsonInclude.Include.NON_EMPTY] to exclude empty values
 * - Configures the [JsonGenerator] to write BigDecimal values as plain text
 * - Disables writing dates as timestamps, nanosecond timestamps, and durations as timestamps
 * - Disables failing on unknown properties during deserialization
 * - Enables accepting empty strings and arrays as null objects during deserialization
 * - Disables adjusting dates to the context time zone during deserialization
 *
 * @return a configured [ObjectMapper] instance
 */
internal fun DefaultObjectMapper(): ObjectMapper {
  val objectMapper = ObjectMapper()
  objectMapper.registerModule(JavaTimeModule())
  objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
  objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
  objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
  objectMapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
  objectMapper.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
  objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
  objectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
  objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
  objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
  objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
  return objectMapper
}

/** List of keys used in default environment calculation */
object DefaultEnvironmentKey {
  const val CURRENT_DATE_TIME = "currentDateTime"
  const val LOCAL_DATE_TIME = "localDateTime"
  const val CURRENT_DATE = "currentDate"
  const val CURRENT_TIME = "currentTime"
  const val UTC_DATE_TIME = "utcDateTime"
  const val UTC_DATE = "utcDate"
  const val UTC_TIME = "utcTime"
  const val YEAR = "year"
  const val MONTH = "month"
  const val DAY = "day"
  const val DAY_OF_WEEK = "dayOfWeek"
  const val DAY_OF_YEAR = "dayOfYear"
  const val LOCAL_TIME = "localTime"
  const val HOUR = "hour"
  const val MINUTE = "minute"
  const val SECOND = "second"
  const val NANO = "nano"
  const val OFFSET = "offset"
}

/**
 * Creates a default [ContextStore] containing various date and time-related values based on the
 * provided [Options].
 *
 * The [ContextStore] includes the following keys:
 * - [DefaultEnvironmentKey.CURRENT_DATE_TIME]: The current date and time in the specified time
 *   zone.
 * - [DefaultEnvironmentKey.LOCAL_DATE_TIME]: The current local date and time.
 * - [DefaultEnvironmentKey.CURRENT_DATE]: The current date in the specified time zone.
 * - [DefaultEnvironmentKey.CURRENT_TIME]: The current time in the specified time zone.
 * - [DefaultEnvironmentKey.LOCAL_TIME]: The current local time.
 * - [DefaultEnvironmentKey.UTC_DATE_TIME]: The current date and time in UTC.
 * - [DefaultEnvironmentKey.UTC_DATE]: The current date in UTC.
 * - [DefaultEnvironmentKey.UTC_TIME]: The current time in UTC.
 * - [DefaultEnvironmentKey.YEAR]: The current year.
 * - [DefaultEnvironmentKey.MONTH]: The current month.
 * - [DefaultEnvironmentKey.DAY]: The current day of the month.
 * - [DefaultEnvironmentKey.DAY_OF_WEEK]: The current day of the week.
 * - [DefaultEnvironmentKey.DAY_OF_YEAR]: The current day of the year.
 * - [DefaultEnvironmentKey.HOUR]: The current hour.
 * - [DefaultEnvironmentKey.MINUTE]: The current minute.
 * - [DefaultEnvironmentKey.SECOND]: The current second.
 * - [DefaultEnvironmentKey.NANO]: The current nanosecond.
 * - [DefaultEnvironmentKey.OFFSET]: The current time zone offset.
 *
 * @param options The [Options] object containing the clock and time zone information.
 * @return A [ContextStore] containing the default date and time-related values.
 */
internal fun DefaultEnvironment(options: Options): ContextStore {
  val currentDateTime =
      if (options.clock != null) OffsetDateTime.now(options.clock)
      else OffsetDateTime.now(options.zoneId)
  val currentDate = currentDateTime.toLocalDate()
  val currentTime = currentDateTime.toOffsetTime()
  val defaults: ContextStore =
      mutableMapOf(
          DefaultEnvironmentKey.CURRENT_DATE_TIME to currentDateTime,
          DefaultEnvironmentKey.LOCAL_DATE_TIME to currentDateTime.toLocalDateTime(),
          DefaultEnvironmentKey.CURRENT_DATE to currentDate,
          DefaultEnvironmentKey.CURRENT_TIME to currentTime,
          DefaultEnvironmentKey.LOCAL_TIME to currentTime.toLocalTime(),
          DefaultEnvironmentKey.UTC_DATE_TIME to
              currentDateTime.withOffsetSameInstant(ZoneOffset.UTC),
          DefaultEnvironmentKey.UTC_DATE to
              currentDateTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDate(),
          DefaultEnvironmentKey.UTC_TIME to
              currentDateTime.withOffsetSameInstant(ZoneOffset.UTC).toOffsetTime(),
          DefaultEnvironmentKey.YEAR to currentDate.year,
          DefaultEnvironmentKey.MONTH to currentDate.month.value,
          DefaultEnvironmentKey.DAY to currentDate.dayOfMonth,
          DefaultEnvironmentKey.DAY_OF_WEEK to currentDate.dayOfWeek.value,
          DefaultEnvironmentKey.DAY_OF_YEAR to currentDate.dayOfYear,
          DefaultEnvironmentKey.HOUR to currentTime.hour,
          DefaultEnvironmentKey.MINUTE to currentTime.minute,
          DefaultEnvironmentKey.SECOND to currentTime.second,
          DefaultEnvironmentKey.NANO to currentTime.nano,
          DefaultEnvironmentKey.OFFSET to currentTime.offset.toString())
  return defaults
}
