package com.github.ivsokol.poe.variable

import java.time.*
import java.time.format.DateTimeFormatter

/**
 * Tries to parse string to [LocalDate] or null if it fails.
 *
 * @return [LocalDate] or null
 */
fun String.toLocalDateOrNull(): LocalDate? =
    try {
      LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: Exception) {
      null
    }

/**
 * Tries to parse string to [OffsetDateTime] or null if it fails.
 *
 * @return [OffsetDateTime] or null
 */
fun String.toOffsetDateTimeOrNull(): OffsetDateTime? =
    try {
      OffsetDateTime.parse(this, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    } catch (e: Exception) {
      null
    }

/**
 * Tries to parse string to [LocalTime] or null if it fails.
 *
 * @return [LocalTime] or null
 */
fun String.toLocalTimeOrNull(): LocalTime? =
    try {
      LocalTime.parse(this, DateTimeFormatter.ISO_LOCAL_TIME)
    } catch (e: Exception) {
      null
    }

/**
 * Tries to parse string to [Period] or null if it fails.
 *
 * @return [Period] or null
 */
fun String.toPeriodOrNull(): Period? =
    try {
      Period.parse(this)
    } catch (e: Exception) {
      null
    }

/**
 * Tries to parse string to [Duration] or null if it fails.
 *
 * @return [Duration] or null
 */
fun String.toDurationOrNull(): Duration? =
    try {
      Duration.parse(this)
    } catch (e: Exception) {
      null
    }
