package io.github.ivsokol.poe

/**
 * Represents a reference to an entity, optionally with a version.
 *
 * @param id The unique identifier of the referenced entity.
 * @param version The version of the referenced entity, if applicable.
 */
data class Reference(val id: String, val version: SemVer? = null) {
  override fun toString(): String {
    return if (version != null) "$id:$version" else id
  }
}
