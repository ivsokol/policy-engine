package io.github.ivsokol.poe.catalog

internal fun List<String>.anyOfAny(other: List<String>): Boolean {
  return this.any { it in other }
}

internal fun List<String>.allOfAll(other: List<String>): Boolean {
  return other.all { it in this }
}
