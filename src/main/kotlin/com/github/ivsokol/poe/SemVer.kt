/* (C)2024 */
package com.github.ivsokol.poe

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A data class representing a Semantic Version (SemVer) number.
 *
 * This class provides a way to represent and compare SemVer numbers, which are commonly used in
 * software versioning. The class has properties for the major, minor, and patch version numbers, as
 * well as optional label and metadata strings. The class also provides a [toString] function to
 * generate a string representation of the SemVer number in the standard format.
 *
 * @param major the major version number, must be >= 0
 * @param minor the minor version number, must be >= 0
 * @param patch the patch version number, must be >= 0
 * @param label the optional label string, such as "alpha" or "beta"
 * @param meta the optional metadata string
 */
@Serializable(SemVerSerializer::class)
data class SemVer(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val label: String? = null,
    val meta: String? = null
) : Comparable<SemVer> {
  init {
    require(major >= 0) { "Major version must be >= 0" }
    require(minor >= 0) { "Minor version must be >= 0" }
    require(patch >= 0) { "Patch version must be >= 0" }
    require(!(major == 0 && minor == 0 && patch == 0)) { "Version must be > 0.0.0" }
  }

  /**
   * Returns a string representation of this [SemVer] instance in the SemVer format.
   *
   * The format is: `major.minor.patch[-label][+meta]`, where:
   * - `major`, `minor`, and `patch` are the corresponding version numbers
   * - `label` is the optional label string, prefixed with a hyphen if present
   * - `meta` is the optional metadata string, prefixed with a plus sign if present
   *
   * For example, `1.2.3-alpha+build123`.
   *
   * @return the string representation of this [SemVer] instance
   */
  override fun toString(): String {
    return "$major.$minor.$patch${label?.let { if (it.isNotBlank()) "-$it" else ""} ?: ""}${meta?.let { if (it.isNotBlank()) "+$it" else "" } ?: ""}"
  }

  /**
   * Compares this [SemVer] instance to the given [other] [SemVer] instance.
   *
   * The comparison is done in the following order:
   * 1. Compare the major version numbers
   * 2. If the major versions are equal, compare the minor version numbers
   * 3. If the minor versions are equal, compare the patch version numbers
   * 4. If the patch versions are equal, compare the label strings (null is considered greater than
   *    non-null)
   * 5. If the labels are equal, compare the meta strings (null is considered greater than non-null)
   * 6. If all the above comparisons are equal, the two [SemVer] instances are considered equal
   *
   * @param other the [SemVer] instance to compare to
   * @return a negative integer if this instance is less than [other], zero if they are equal, and a
   *   positive integer if this instance is greater than [other]
   */
  override fun compareTo(other: SemVer): Int {
    if (major != other.major) return major.compareTo(other.major)
    if (minor != other.minor) return minor.compareTo(other.minor)
    if (patch != other.patch) return patch.compareTo(other.patch)
    if (label != other.label) {
      if (label == null) return 1
      if (other.label == null) return -1
      return label.compareTo(other.label)
    }
    if (meta != other.meta) {
      if (meta == null) return 1
      if (other.meta == null) return -1
      return meta.compareTo(other.meta)
    }
    return 0
  }
}

/** Default SemVer object 0.1.0-SNAPSHOT */
fun DefaultSemVer() = SemVer(0, 1, 0, "SNAPSHOT")

/**
 * A Kotlin serializer for the [SemVer] class.
 *
 * This serializer allows [SemVer] instances to be serialized and deserialized using Kotlin's
 * serialization framework. It uses the string representation of the [SemVer] instance as the
 * serialized form.
 */
object SemVerSerializer : KSerializer<SemVer> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("SemVer", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: SemVer) {
    val string = value.toString()
    encoder.encodeString(string)
  }

  override fun deserialize(decoder: Decoder): SemVer {
    val string = decoder.decodeString()
    return stringSemVerDecoder(string)
  }
}

private val destructedRegex =
    """^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?${'$'}"""
        .toRegex()

/**
 * Decodes a string representation of a [SemVer] instance.
 *
 * This function uses a regular expression to parse the string and create a new [SemVer] instance
 * with the parsed values.
 *
 * @param text the string representation of the [SemVer] instance to decode
 * @return a new [SemVer] instance with the parsed values
 * @throws IllegalArgumentException if the input string is not a valid SemVer string
 */
internal fun stringSemVerDecoder(text: String): SemVer {
  return destructedRegex.matchEntire(text)?.destructured?.let { (major, minor, prefix, label, meta)
    ->
    SemVer(
        major.toInt(),
        minor.toInt(),
        prefix.toInt(),
        label.let { it.ifBlank { null } },
        meta.let { it.ifBlank { null } })
  } ?: throw IllegalArgumentException("Bad input for SemVer '$text'")
}
