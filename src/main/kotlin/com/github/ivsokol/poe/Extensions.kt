package com.github.ivsokol.poe

import com.github.ivsokol.poe.action.*
import com.github.ivsokol.poe.catalog.allOfAll
import com.github.ivsokol.poe.catalog.anyOfAny
import com.github.ivsokol.poe.condition.IPolicyCondition
import com.github.ivsokol.poe.condition.PolicyConditionAtomic
import com.github.ivsokol.poe.condition.PolicyConditionComposite
import com.github.ivsokol.poe.condition.PolicyConditionDefault
import com.github.ivsokol.poe.policy.IPolicy
import com.github.ivsokol.poe.policy.Policy
import com.github.ivsokol.poe.policy.PolicyDefault
import com.github.ivsokol.poe.policy.PolicySet
import com.github.ivsokol.poe.variable.IPolicyVariable
import com.github.ivsokol.poe.variable.PolicyVariableDynamic
import com.github.ivsokol.poe.variable.PolicyVariableResolver
import com.github.ivsokol.poe.variable.PolicyVariableStatic

private val ID_REGEX = Regex("^[a-zA-Z0-9_.\\-\\[\\]]+$")

/**
 * Converts a list of [IPolicyVariable] instances to a map, where the keys are the variable IDs and
 * the values are lists of the variables. If a variable's identity is blank, it is assigned a
 * default ID in the format ```"variables[index]"```. The resulting map is sorted using the
 * [sortCatalog] function.
 *
 * @return A map where the keys are the variable IDs and the values are lists of the variables.
 * @receiver The list of [IPolicyVariable] instances to be converted.
 */
internal fun List<IPolicyVariable>.toCatalogMapVariable(): Map<String, List<IPolicyVariable>> =
    this.mapIndexed { index, item ->
          when (item) {
            is PolicyVariableStatic ->
                if (item.identity().isBlank()) item.copy(id = "variables[$index]") else item
            is PolicyVariableDynamic ->
                if (item.identity().isBlank()) item.copy(id = "variables[$index]") else item
            else -> error("Unsupported variable type: ${item.javaClass.name} in catalog mapping")
          }
        }
        .sortCatalog()

/**
 * Converts a list of [PolicyVariableResolver] instances to a map, where the keys are the resolver
 * IDs and the values are lists of the resolvers. If a resolver's identity is blank, it is assigned
 * a default ID in the format ```"resolvers[index]"```. The resulting map is sorted using the
 * [sortCatalog] function.
 *
 * @return A map where the keys are the resolver IDs and the values are lists of the resolvers.
 * @receiver The list of [PolicyVariableResolver] instances to be converted.
 */
internal fun List<PolicyVariableResolver>.toCatalogMapResolver():
    Map<String, List<PolicyVariableResolver>> =
    this.mapIndexed { index, item ->
          if (item.identity().isBlank()) item.copy(id = "resolvers[$index]") else item
        }
        .sortCatalog()

/**
 * Converts a list of [IPolicyCondition] instances to a map, where the keys are the condition IDs
 * and the values are lists of the conditions. If a condition's identity is blank, it is assigned a
 * default ID in the format ```"conditions[index]"```. The resulting map is sorted using the
 * [sortCatalog] function.
 *
 * @return A map where the keys are the condition IDs and the values are lists of the conditions.
 * @receiver The list of [IPolicyCondition] instances to be converted.
 */
internal fun List<IPolicyCondition>.toCatalogMapCondition(): Map<String, List<IPolicyCondition>> =
    this.mapIndexed { index, item ->
          when (item) {
            is PolicyConditionAtomic ->
                if (item.identity().isBlank()) item.copy(id = "conditions[$index]") else item
            is PolicyConditionComposite ->
                if (item.identity().isBlank()) item.copy(id = "conditions[$index]") else item
            is PolicyConditionDefault -> item
            else -> error("Unsupported condition type: ${item.javaClass.name} in catalog mapping")
          }
        }
        .sortCatalog()

/**
 * Converts a list of [IPolicyAction] instances to a map, where the keys are the action IDs and the
 * values are lists of the actions. If an action's identity is blank, it is assigned a default ID in
 * the format ```"actions[index]"```. The resulting map is sorted using the [sortCatalog] function.
 *
 * @return A map where the keys are the action IDs and the values are lists of the actions.
 * @receiver The list of [IPolicyAction] instances to be converted.
 */
internal fun List<IPolicyAction>.toCatalogMapAction(): Map<String, List<IPolicyAction>> =
    this.mapIndexed { index, item ->
          when (item) {
            is PolicyActionSave ->
                if (item.identity().isBlank()) item.copy(id = "actions[$index]") else item
            is PolicyActionClear ->
                if (item.identity().isBlank()) item.copy(id = "actions[$index]") else item
            is PolicyActionJsonMerge ->
                if (item.identity().isBlank()) item.copy(id = "actions[$index]") else item
            is PolicyActionJsonPatch ->
                if (item.identity().isBlank()) item.copy(id = "actions[$index]") else item
            else -> error("Unsupported condition type: ${item.javaClass.name} in catalog mapping")
          }
        }
        .sortCatalog()

/**
 * Converts a list of [IPolicy] instances to a map, where the keys are the policy IDs and the values
 * are lists of the policies. If a policy's identity is blank, it is assigned a default ID in the
 * format ```"policies[index]"```. The resulting map is sorted using the [sortCatalog] function.
 *
 * @return A map where the keys are the policy IDs and the values are lists of the policies.
 * @receiver The list of [IPolicy] instances to be converted.
 */
internal fun List<IPolicy>.toCatalogMapPolicy(): Map<String, List<IPolicy>> =
    this.mapIndexed { index, item ->
          when (item) {
            is Policy -> if (item.identity().isBlank()) item.copy(id = "policies[$index]") else item
            is PolicySet ->
                if (item.identity().isBlank()) item.copy(id = "policies[$index]") else item
            is PolicyDefault -> item
            else -> error("Unsupported policy type: ${item.javaClass.name} in catalog mapping")
          }
        }
        .sortCatalog()

/**
 * Sorts a list of [IManaged] instances by their ID and version, and returns a map where the keys
 * are the IDs and the values are lists of the sorted instances. If multiple instances have the same
 * ID, they are sorted in descending order by their version.
 *
 * @return A map where the keys are the IDs and the values are lists of the sorted instances.
 * @receiver The list of [IManaged] instances to be sorted.
 */
private fun <T> List<T>.sortCatalog(): Map<String, List<T>> where T : IManaged =
    this.groupBy { it.id!! }
        .mapValues { entry ->
          entry.value.distinctBy { it.version }.sortedByDescending { it.version }
        }

/**
 * Returns the last [IManaged] instance in the list, or the first instance if the last instance has
 * a null version.
 *
 * This function is used to retrieve either unversioned or latest version of an [IManaged] instance
 * from a list of instances.
 *
 * @return The unversioned or latest [IManaged] instance, or `null` if the list is empty.
 * @receiver The list of [IManaged] instances to search.
 */
internal fun <T> List<T>.unversionedOrLatest(): T? where T : IManaged =
    if (this.last().version == null) this.last() else this.firstOrNull()

/**
 * Finds the [IManaged] instance with the specified ID and optional version from the map.
 *
 * If a version is provided, the function will return the instance with the matching version. If no
 * version is provided, the function will return the unversioned or latest instance.
 *
 * @param id The ID of the [IManaged] instance to find.
 * @param version The optional version of the [IManaged] instance to find.
 * @return The [IManaged] instance with the specified ID and version, or `null` if not found.
 */
internal fun <T> Map<String, List<T>>.findByIdAndVersion(
    id: String,
    version: SemVer? = null
): T? where T : IManaged =
    this[id]?.let {
      if (version != null) return it.firstOrNull { t -> t.version == version }
      return it.unversionedOrLatest()
    }

/**
 * Searches the provided map of [IManaged] instances by the given labels, using the specified label
 * search logic.
 *
 * If the [labels] set is empty, an [IllegalArgumentException] will be thrown.
 *
 * @param labels The set of labels to search for.
 * @param logic The label search logic to use (ANY_OF or ALL_OF).
 * @return A list of [IManaged] instances that match the search criteria, or `null` if the map is
 *   `null`.
 */
internal fun <T> Map<String, List<T>>?.searchByLabels(
    labels: Set<String>,
    logic: LabelSearchLogicEnum
): List<T>? where T : IManaged {
  require(labels.isNotEmpty()) { "Labels must not be empty" }
  val labelList = labels.toList()
  return this
      // pick latest and check if label matches
      ?.map { entry ->
        when (logic) {
          LabelSearchLogicEnum.ANY_OF ->
              entry.value.first().takeIf { c ->
                !c.labels.isNullOrEmpty() && c.labels!!.anyOfAny(labelList)
              }
          LabelSearchLogicEnum.ALL_OF ->
              entry.value.first().takeIf { c ->
                !c.labels.isNullOrEmpty() && c.labels!!.allOfAll(labelList)
              }
        }
      }
      ?.filterNotNull()
      ?.toList()
}

/**
 * Validates the ID of the [IManaged] instance.
 *
 * This function checks the following conditions:
 * - If the ID is `null`, it throws an [IllegalArgumentException] if the version is not `null`.
 * - If the ID is empty, it throws an [IllegalArgumentException].
 * - If the ID is blank, it throws an [IllegalArgumentException].
 * - If the ID does not match the `ID_REGEX` pattern, it throws an [IllegalArgumentException] with a
 *   message describing the allowed characters.
 *
 * @throws IllegalArgumentException if any of the above conditions are not met.
 */
internal fun IManaged.validateId() =
    when {
      this.id == null -> require(this.version == null) { "Version cannot be populated without id" }
      this.id!!.isEmpty() -> throw IllegalArgumentException("Id must not be empty")
      this.id!!.isBlank() -> throw IllegalArgumentException("Id must not be blank")
      !ID_REGEX.matches(this.id!!) ->
          throw IllegalArgumentException(
              "Id allowed characters are letters, numbers, square brackets, underscore, dot and hyphen")
      else -> Unit
    }
