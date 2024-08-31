package io.github.ivsokol.poe.action

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.IManaged
import io.github.ivsokol.poe.PolicyEntityRefItem
import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.catalog.PolicyCatalog
import kotlinx.serialization.Serializable

@Serializable(with = IPolicyActionRefOrValueSerializer::class) interface IPolicyActionRefOrValue

/**
 * Represents an action that can be performed as part of a policy.
 *
 * @property id The unique identifier for this policy action.
 * @property version The semantic version of this policy action.
 * @property description A description of this policy action.
 * @property labels Any labels associated with this policy action.
 * @property type The type of action this represents.
 * @see IPolicyActionRefOrValue
 * @see IManaged
 */
@Serializable(with = IPolicyActionSerializer::class)
interface IPolicyAction : IManaged, IPolicyActionRefOrValue {
  override val id: String?
  override val version: SemVer?
  override val description: String?
  override val labels: List<String>?

  val type: ActionTypeEnum

  fun run(context: Context, policyCatalog: PolicyCatalog): Boolean

  fun childRefs(): Set<PolicyEntityRefItem>?
}
