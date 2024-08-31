/* (C)2024 */
package com.github.ivsokol.poe

import java.time.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A data class representing a calendar version (CalVer) with year, month, day, and optional
 * revision.
 *
 * CalVer is a versioning scheme that uses the current date as the version number, in the format
 * YYYY-MM-DD[-R], where R is an optional revision number.
 *
 * This class provides methods for serializing and deserializing CalVer values, as well as comparing
 * CalVer instances.
 *
 * @param year The year component of the CalVer, must be between 2024 and 2999.
 * @param month The month component of the CalVer, must be between 1 and 12.
 * @param day The day component of the CalVer, must be between 1 and 31.
 * @param revision The optional revision component of the CalVer, must be a positive integer or
 *   null.
 */
@Serializable(CalVerSerializer::class)
data class CalVer(
    val year: Int,
    val month: Int,
    val day: Int,
    val revision: Int? = null,
) : Comparable<CalVer> {
  init {
    require(year in 2024..2999) { "Year must be >= 2024" }
    require(month in 1..12) { "Month must be between 1 and 12" }
    require(day in 1..31) { "Day must be between 1 and 31" }
    require(revision == null || revision > 0) { "Revision must be positive integer" }
  }

  /**
   * Returns a string representation of the [CalVer] instance in the format "YYYY-MM-DD[-R]", where
   * "R" is the optional revision number.
   */
  override fun toString(): String {
    return "$year-${if (month < 10) "0$month" else month}-${if (day < 10) "0$day" else day}${revision?.let { "-$it" } ?: ""}"
  }

  /**
   * Compares this [CalVer] instance to the [other] [CalVer] instance.
   *
   * The comparison is done in the following order:
   * 1. Year
   * 2. Month
   * 3. Day
   * 4. Revision (if present)
   *
   * If the years, months, and days are the same, the revisions are compared. If one revision is
   * `null`, it is considered smaller than the non-null revision.
   *
   * @param other The [CalVer] instance to compare this instance to.
   * @return A negative integer if this instance is less than [other], zero if they are equal, and a
   *   positive integer if this instance is greater than [other].
   */
  override fun compareTo(other: CalVer): Int {
    if (year != other.year) return year.compareTo(other.year)
    if (month != other.month) return month.compareTo(other.month)
    if (day != other.day) return day.compareTo(other.day)
    if (revision != other.revision) {
      if (revision == null) return -1
      if (other.revision == null) return 1
      return revision.compareTo(other.revision)
    }
    return 0
  }
}

/**
 * Returns a [CalVer] instance representing the current local date.
 *
 * This function creates a new [CalVer] instance using the current year, month, and day of the local
 * date.
 *
 * @return a [CalVer] instance representing the current local date.
 */
fun DefaultCalVer(): CalVer = LocalDate.now().let { CalVer(it.year, it.monthValue, it.dayOfMonth) }

/**
 * A KSerializer implementation for the CalVer class, which provides serialization and
 * deserialization functionality for the CalVer type.
 *
 * The serializer encodes a CalVer instance as a string in the format "YYYY-MM-DD[-R]", where "R" is
 * the optional revision number. The deserializer decodes a string in this format back into a CalVer
 * instance.
 */
object CalVerSerializer : KSerializer<CalVer> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("CalVer", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: CalVer) {
    val string = value.toString()
    encoder.encodeString(string)
  }

  override fun deserialize(decoder: Decoder): CalVer {
    val string = decoder.decodeString()
    return stringCalVerDecoder(string)
  }
}

private val destructedRegex =
    """^(2\d{3})-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])(?:-(\d+))?${'$'}""".toRegex()

/**
 * Decodes a string representation of a [CalVer] instance.
 *
 * This function takes a string in the format "YYYY-MM-DD[-R]", where "R" is the optional revision
 * number, and returns a [CalVer] instance with the corresponding year, month, day, and revision (if
 * present).
 *
 * @param text The string to decode.
 * @return A [CalVer] instance representing the decoded date and revision.
 * @throws IllegalArgumentException if the input string is not in the expected format.
 */
internal fun stringCalVerDecoder(text: String): CalVer {
  return destructedRegex.matchEntire(text)?.destructured?.let { (year, month, day, revision) ->
    CalVer(year.toInt(), month.toInt(), day.toInt(), revision.let { it.ifBlank { null } }?.toInt())
  } ?: throw IllegalArgumentException("Bad input for CalVer '$text'")
}
