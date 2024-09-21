package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.PolicyEntityRefEnum

internal interface CommandDeserializer {
  fun deserialize(
      command: RegistryEntry,
      childCommands: List<Any>,
      contents: List<String>,
      options: ELOptions?,
      constraint: Any? = null,
      refType: PolicyEntityRefEnum? = null
  ): Any
}

enum class EntryTypeEnum {
  // DSL,
  OPTIONS,
  CONSTRAINT,
  ACTION_RELATIONSHIP,
  POLICY_RELATIONSHIP,
  // Reference
  REF,

  // PolicyVariableStatic
  STRING,
  DATE,
  DATE_TIME,
  TIME,
  PERIOD,
  DURATION,
  INT,
  LONG,
  NUM,
  FLOAT,
  BIG_DECIMAL,
  BOOLEAN,
  OBJECT_NODE,
  ARRAY_NODE,
  ARRAY,

  // PolicyVariableResolver
  JMES_PATH,
  JQ,
  KEY,

  // PolicyVariableDynamic
  DYN,

  // PolicyConditionAtomic
  GT,
  GTE,
  LT,
  LTE,
  IS_NULL,
  IS_NOT_NULL,
  IS_EMPTY,
  IS_NOT_EMPTY,
  IS_BLANK,
  IS_NOT_BLANK,
  STARTS_WITH,
  ENDS_WITH,
  CONTAINS,
  IS_IN,
  EQ,
  POS,
  NEG,
  ZERO,
  PAST,
  FUTURE,
  REGEXP,
  HAS_KEY,
  UNIQUE,
  SCHEMA,

  // PolicyConditionComposite
  ANY_OF,
  ALL_OF,
  NOT,
  N_OF,

  // PolicyConditionDefault
  DEFAULT_TRUE,
  DEFAULT_FALSE,
  DEFAULT_NULL,

  // Policy
  PERMIT,
  DENY,

  // PolicySet
  DENY_OVERRIDES,
  PERMIT_OVERRIDES,
  DENY_UNLESS_PERMIT,
  PERMIT_UNLESS_DENY,
  FIRST_APPLICABLE,
  ONLY_ONE_APPLICABLE,

  // PolicyDefault
  DEFAULT_PERMIT,
  DEFAULT_DENY,
  DEFAULT_INDETERMINATE_DENY_PERMIT,
  DEFAULT_INDETERMINATE_DENY,
  DEFAULT_INDETERMINATE_PERMIT,
  DEFAULT_NOT_APPLICABLE,

  // PolicyAction
  SAVE,
  CLEAR,
  PATCH,
  MERGE
}

data class RegistryEntry(
    val entryType: EntryTypeEnum,
    val command: String,
    val entityType: PolicyEntityEnum,
    val childCmdTypes: List<PolicyEntityEnum>,
    val argsCount: Pair<Int, Int?>,
)

internal const val CMD_START = '('
internal const val CMD_END = ')'
internal const val DELIMITER = ','
internal const val OPTION_KEY_VALUE_DELIMITER = '='
internal const val SPECIAL_CHARS_DICTIONARY = "()[]{},.-+*/=$#`\"_"

internal enum class CharTypeEnum {
  CMD_START,
  CMD_END,
  DELIMITER,
  CMD,
  NON_PARSABLE_CHAR,
  CONTENT
}

/**
 * The ParserRegistry object contains the registry of all the DSL commands, policy rules, policy
 * sets, policy defaults, and policy actions that are supported by the policy engine.
 *
 * The registry is defined as a set of [RegistryEntry] objects, each representing a specific DSL
 * command or policy entity. The registry is used to parse and validate the policy definitions
 * written in the DSL.
 *
 * The registry includes various types of policy entities, such as:
 * - DSL commands (e.g. `#opts`, `*constraint`, `*act`, `*pol`)
 * - Policy rules (e.g. `*permit`, `*deny`)
 * - Policy sets (e.g. `*DOverrides`, `*POverrides`, `*DUnlessP`, `*PUnlessD`, `*firstAppl`)
 * - Policy defaults (e.g. `#permit`, `#deny`, `#NA`, `#indDP`, `#indD`, `#indP`)
 * - Policy actions (e.g. `*save`, `*clear`, `*patch`, `*merge`)
 * - Policy variables (static and dynamic)
 * - Policy conditions (atomic and composite)
 *
 * The registry also includes utility functions and constants related to parsing and processing the
 * policy DSL.
 */
object ParserRegistry {
  private val content = listOf(PolicyEntityEnum.CONTENT)
  // managedOpts -> id: String?,ver: SemVer?,desc: String?,labels: List<String>? ; labels -> '|'
  // delimited strings: lab1|lab2|"space lab3"

  private val dslRegistry =
      listOf(
          // #opts(key=value,key2=value2,...)
          RegistryEntry(
              EntryTypeEnum.OPTIONS, "#opts", PolicyEntityEnum.DSL, content, Pair(1, null)),
          // *constraint(cond|ref)
          RegistryEntry(
              EntryTypeEnum.CONSTRAINT,
              "*constraint",
              PolicyEntityEnum.DSL,
              listOf(
                  PolicyEntityEnum.REFERENCE,
                  PolicyEntityEnum.CONDITION_ATOMIC,
                  PolicyEntityEnum.CONDITION_COMPOSITE,
                  PolicyEntityEnum.CONDITION_DEFAULT),
              Pair(1, 1)),
          // *act(act|ref, $cstr?, #opts?) -> opt => executionMode: String? = null, priority: Int? =
          // null
          RegistryEntry(
              EntryTypeEnum.ACTION_RELATIONSHIP,
              "*act",
              PolicyEntityEnum.DSL,
              listOf(
                  PolicyEntityEnum.POLICY_ACTION_SAVE,
                  PolicyEntityEnum.POLICY_ACTION_CLEAR,
                  PolicyEntityEnum.POLICY_ACTION_JSON_MERGE,
                  PolicyEntityEnum.POLICY_ACTION_JSON_PATCH,
                  PolicyEntityEnum.REFERENCE),
              Pair(1, 1)),
          // *pol(pol|ref, $cstr?, #opts?) -> opt => runAction: Boolean? = null, priority: Int? =
          // null
          RegistryEntry(
              EntryTypeEnum.POLICY_RELATIONSHIP,
              "*pol",
              PolicyEntityEnum.DSL,
              listOf(
                  PolicyEntityEnum.POLICY,
                  PolicyEntityEnum.POLICY_SET,
                  PolicyEntityEnum.POLICY_DEFAULT,
                  PolicyEntityEnum.REFERENCE),
              Pair(1, 1)),
      )

  // *ref(id,ver?)
  internal val policyRefRegistry =
      listOf(
          RegistryEntry(EntryTypeEnum.REF, "#ref", PolicyEntityEnum.REFERENCE, content, Pair(1, 2)),
      )
  private val policyVariableStaticRegistry =
      listOf(
          // #str(value,#opts?) -> opt => isJson: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.STRING, "#str", PolicyEntityEnum.VARIABLE_STATIC, content, Pair(1, 1)),
          // #date(value,#opts?) -> opt => dateFormat: String? = null
          RegistryEntry(
              EntryTypeEnum.DATE, "#date", PolicyEntityEnum.VARIABLE_STATIC, content, Pair(1, 1)),
          // #dTime(value,#opts?) -> opt => dateTimeFormat: String? = null
          RegistryEntry(
              EntryTypeEnum.DATE_TIME,
              "#dTime",
              PolicyEntityEnum.VARIABLE_STATIC,
              content,
              Pair(1, 1)),
          // #time(value,#opts?) -> opt => dateTimeFormat: String? = null
          RegistryEntry(
              EntryTypeEnum.TIME, "#time", PolicyEntityEnum.VARIABLE_STATIC, content, Pair(1, 1)),
          // #per(value,#opts?)
          RegistryEntry(
              EntryTypeEnum.PERIOD, "#per", PolicyEntityEnum.VARIABLE_STATIC, content, Pair(1, 1)),
          // #dur(value,#opts?)
          RegistryEntry(
              EntryTypeEnum.DURATION,
              "#dur",
              PolicyEntityEnum.VARIABLE_STATIC,
              content,
              Pair(1, 1)),
          // #int(value,#opts?)
          RegistryEntry(
              EntryTypeEnum.INT, "#int", PolicyEntityEnum.VARIABLE_STATIC, content, Pair(1, 1)),
          // #long(value,#opts?)
          RegistryEntry(
              EntryTypeEnum.LONG, "#long", PolicyEntityEnum.VARIABLE_STATIC, content, Pair(1, 1)),
          // #num(value,#opts?)
          RegistryEntry(
              EntryTypeEnum.NUM, "#num", PolicyEntityEnum.VARIABLE_STATIC, content, Pair(1, 1)),
          // #float(value,#opts?)
          RegistryEntry(
              EntryTypeEnum.FLOAT, "#float", PolicyEntityEnum.VARIABLE_STATIC, content, Pair(1, 1)),
          // #bigD(value,#opts?)
          RegistryEntry(
              EntryTypeEnum.BIG_DECIMAL,
              "#bigD",
              PolicyEntityEnum.VARIABLE_STATIC,
              content,
              Pair(1, 1)),
          // #bool(value,#opts?)
          RegistryEntry(
              EntryTypeEnum.BOOLEAN,
              "#bool",
              PolicyEntityEnum.VARIABLE_STATIC,
              content,
              Pair(1, 1)),
          // #obj(value,#opts?)
          RegistryEntry(
              EntryTypeEnum.OBJECT_NODE,
              "#obj",
              PolicyEntityEnum.VARIABLE_STATIC,
              content,
              Pair(1, 1)),
          // #arr(value,#opts?)
          RegistryEntry(
              EntryTypeEnum.ARRAY, "#arr", PolicyEntityEnum.VARIABLE_STATIC, content, Pair(1, 1)),
      )
  private val policyVariableDynamicRegistry =
      // *dyn(...resolver|ref,#opts?) -> opt => type: String? = null; format: String? = null;
      // timeFormat: String? = null; dateFormat: String? = null; dateTimeFormat: String? = null;
      listOf(
          RegistryEntry(
              EntryTypeEnum.DYN,
              "*dyn",
              PolicyEntityEnum.VARIABLE_DYNAMIC,
              listOf(PolicyEntityEnum.VALUE_RESOLVER, PolicyEntityEnum.REFERENCE),
              Pair(1, null)),
      )
  private val policyVariableRegistry =
      policyVariableStaticRegistry + policyVariableDynamicRegistry + policyRefRegistry

  private val policyVariableResolverRegistry =
      listOf(
          // *path(path,#opts) -> opt : key: String = null, source: String = null
          RegistryEntry(
              EntryTypeEnum.JMES_PATH,
              "*path",
              PolicyEntityEnum.VALUE_RESOLVER,
              content,
              Pair(1, 1)),
          // *jq(path,#opts) -> opt : key: String = null, source: String = null
          RegistryEntry(
              EntryTypeEnum.JQ, "*jq", PolicyEntityEnum.VALUE_RESOLVER, content, Pair(1, 1)),
          // *key(key,#opts) -> opt : source: String = null
          RegistryEntry(
              EntryTypeEnum.KEY, "*key", PolicyEntityEnum.VALUE_RESOLVER, content, Pair(1, 1)),
      )

  private val conditionAtomicContent =
      listOf(
          PolicyEntityEnum.VARIABLE_DYNAMIC,
          PolicyEntityEnum.VARIABLE_STATIC,
          PolicyEntityEnum.REFERENCE)
  private val policyConditionAtomicRegistry =
      listOf(
          // *gt(var|ref,var|ref,#opts?) -> opt : negateResult: Boolean? = null, stringIgnoreCase:
          // Boolean? = null
          RegistryEntry(
              EntryTypeEnum.GT,
              "*gt",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(2, 2)),
          // *gte(var|ref,var|ref,#opts?) -> opt : negateResult: Boolean? = null, stringIgnoreCase:
          // Boolean? = null
          RegistryEntry(
              EntryTypeEnum.GTE,
              "*gte",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(2, 2)),
          // *lt(var|ref,var|ref,#opts?) -> opt : negateResult: Boolean? = null, stringIgnoreCase:
          // Boolean? = null
          RegistryEntry(
              EntryTypeEnum.LT,
              "*lt",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(2, 2)),
          // *lte(var|ref,var|ref,#opts?) -> opt : negateResult: Boolean? = null, stringIgnoreCase:
          // Boolean? = null
          RegistryEntry(
              EntryTypeEnum.LTE,
              "*lte",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(2, 2)),
          // *isNull(var|ref,#opts?) -> opt : negateResult: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.IS_NULL,
              "*isNull",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(1, 1)),
          // *notNull(var|ref,#opts?) -> opt : negateResult: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.IS_NOT_NULL,
              "*notNull",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(1, 1)),
          // *isEmpty(var|ref,#opts?) -> opt : negateResult: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.IS_EMPTY,
              "*isEmpty",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(1, 1)),
          // *notEmpty(var|ref,#opts?) -> opt : negateResult: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.IS_NOT_EMPTY,
              "*notEmpty",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(1, 1)),
          // *isBlank(var|ref,#opts?) -> opt : negateResult: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.IS_BLANK,
              "*isBlank",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(1, 1)),
          // *notBlank(var|ref,#opts?) -> opt : negateResult: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.IS_NOT_BLANK,
              "*notBlank",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(1, 1)),
          // *sw(var|ref,var|ref,#opts?) -> opt : negateResult: Boolean? = null, stringIgnoreCase:
          // Boolean? = null, fieldsStrictCheck: Boolean? = null, arrayOrderStrictCheck: Boolean? =
          // null
          RegistryEntry(
              EntryTypeEnum.STARTS_WITH,
              "*sw",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(2, 2)),
          // *ew(var|ref,var|ref,#opts?) -> opt : negateResult: Boolean? = null, stringIgnoreCase:
          // Boolean? = null, fieldsStrictCheck: Boolean? = null, arrayOrderStrictCheck: Boolean? =
          // null
          RegistryEntry(
              EntryTypeEnum.ENDS_WITH,
              "*ew",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(2, 2)),
          // *contains(var|ref,var|ref,#opts?) -> opt : negateResult: Boolean? = null,
          // stringIgnoreCase: Boolean? = null, fieldsStrictCheck: Boolean? = null,
          // arrayOrderStrictCheck: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.CONTAINS,
              "*contains",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(2, 2)),
          // *isIn(var|ref,var|ref,#opts?) -> opt : negateResult: Boolean? = null, stringIgnoreCase:
          // Boolean? = null, fieldsStrictCheck: Boolean? = null, arrayOrderStrictCheck: Boolean? =
          // null
          RegistryEntry(
              EntryTypeEnum.IS_IN,
              "*isIn",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(2, 2)),
          // *eq(var|ref,var|ref,#opts?) -> opt : negateResult: Boolean? = null, stringIgnoreCase:
          // Boolean? = null, fieldsStrictCheck: Boolean? = null, arrayOrderStrictCheck: Boolean? =
          // null
          RegistryEntry(
              EntryTypeEnum.EQ,
              "*eq",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(2, 2)),
          // *pos(var|ref,#opts?) -> opt : negateResult: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.POS,
              "*pos",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(1, 1)),
          // *neg(var|ref,#opts?) -> opt : negateResult: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.NEG,
              "*neg",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(1, 1)),
          // *zero(var|ref,#opts?) -> opt : negateResult: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.ZERO,
              "*zero",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(1, 1)),
          // *past(var|ref,#opts?) -> opt : negateResult: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.PAST,
              "*past",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(1, 1)),
          // *future(var|ref,#opts?) -> opt : negateResult: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.FUTURE,
              "*future",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(1, 1)),
          // *regexp(var|ref,var|ref,#opts?) -> opt : negateResult: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.REGEXP,
              "*regexp",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(2, 2)),
          // *hasKey(var|ref,var|ref,#opts?) -> opt : negateResult: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.HAS_KEY,
              "*hasKey",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(2, 2)),
          // *unique(var|ref,#opts?) -> opt : negateResult: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.UNIQUE,
              "*unique",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(1, 1)),
          // *schema(var|ref,var|ref,#opts?) -> opt : negateResult: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.SCHEMA,
              "*schema",
              PolicyEntityEnum.CONDITION_ATOMIC,
              conditionAtomicContent,
              Pair(2, 2)),
      )
  private val conditionCompositeContent =
      listOf(
          PolicyEntityEnum.CONDITION_ATOMIC,
          PolicyEntityEnum.CONDITION_COMPOSITE,
          PolicyEntityEnum.CONDITION_DEFAULT,
          PolicyEntityEnum.REFERENCE)
  private val policyConditionCompositeRegistry =
      listOf(
          // *any(...cond|ref,#opts?) -> opt : negateResult: Boolean? = null, strictCheck: Boolean?
          // = null
          RegistryEntry(
              EntryTypeEnum.ANY_OF,
              "*any",
              PolicyEntityEnum.CONDITION_COMPOSITE,
              conditionCompositeContent,
              Pair(1, null)),
          // *all(...cond|ref,#opts?) -> opt : negateResult: Boolean? = null, strictCheck: Boolean?
          // = null
          RegistryEntry(
              EntryTypeEnum.ALL_OF,
              "*all",
              PolicyEntityEnum.CONDITION_COMPOSITE,
              conditionCompositeContent,
              Pair(1, null)),
          // *not(cond|ref,#opts?) -> opt : negateResult: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.NOT,
              "*not",
              PolicyEntityEnum.CONDITION_COMPOSITE,
              conditionCompositeContent,
              Pair(1, 1)),
          // *nOf(...cond|ref,#opts) -> opt : minimumConditions:Int!!; negateResult: Boolean? =
          // null,
          // strictCheck: Boolean? = null, optimize: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.N_OF,
              "*nOf",
              PolicyEntityEnum.CONDITION_COMPOSITE,
              conditionCompositeContent,
              Pair(1, null)),
      )
  private val policyConditionDefaultRegistry =
      listOf(
          RegistryEntry(
              EntryTypeEnum.DEFAULT_TRUE,
              "#true",
              PolicyEntityEnum.CONDITION_DEFAULT,
              emptyList(),
              Pair(0, 0)),
          RegistryEntry(
              EntryTypeEnum.DEFAULT_FALSE,
              "#false",
              PolicyEntityEnum.CONDITION_DEFAULT,
              emptyList(),
              Pair(0, 0)),
          RegistryEntry(
              EntryTypeEnum.DEFAULT_NULL,
              "#null",
              PolicyEntityEnum.CONDITION_DEFAULT,
              emptyList(),
              Pair(0, 0)),
      )
  private val policyConditionRegistry =
      policyConditionAtomicRegistry +
          policyConditionCompositeRegistry +
          policyConditionDefaultRegistry +
          policyRefRegistry

  private val policyContent =
      listOf(
          PolicyEntityEnum.CONDITION_ATOMIC,
          PolicyEntityEnum.CONDITION_COMPOSITE,
          PolicyEntityEnum.CONDITION_DEFAULT,
          PolicyEntityEnum.REFERENCE,
          PolicyEntityEnum.ACTION_RELATIONSHIP,
          PolicyEntityEnum.POLICY_ACTION,
      )

  // common options lenientConstraints: Boolean? = null, actionExecutionStrategy: String? = null,
  // ignoreErrors: Boolean? = null, priority: Int? = null
  private val policyRuleRegistry =
      listOf(
          // *permit(cond|ref,constr?,act|actRel?*,opts?) -> opt : strictTargetEffect: Boolean? =
          // null
          RegistryEntry(
              EntryTypeEnum.PERMIT,
              "*permit",
              PolicyEntityEnum.POLICY,
              policyContent,
              Pair(1, null)),
          // *deny(cond|ref,constr?,act|actRel?*,opts?) -> opt : strictTargetEffect: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.DENY, "*deny", PolicyEntityEnum.POLICY, policyContent, Pair(1, null)),
      )
  private val policySetContent =
      listOf(
          PolicyEntityEnum.POLICY,
          PolicyEntityEnum.POLICY_SET,
          PolicyEntityEnum.POLICY_DEFAULT,
          PolicyEntityEnum.REFERENCE,
          PolicyEntityEnum.POLICY_RELATIONSHIP,
          PolicyEntityEnum.ACTION_RELATIONSHIP,
          PolicyEntityEnum.POLICY_ACTION)
  // common options lenientConstraints: Boolean? = null, actionExecutionStrategy: String? = null,
  // ignoreErrors: Boolean? = null, priority: Int? = null, skipCache: Boolean? = null,
  // runChildActions: Boolean? = null, indeterminateOnActionFail: Boolean? = null

  private val policySetRegistry =
      listOf(
          // *DOverrides(pol|ref|polRel*,constr?,act|actRel?*,opts?)
          RegistryEntry(
              EntryTypeEnum.DENY_OVERRIDES,
              "*DOverrides",
              PolicyEntityEnum.POLICY_SET,
              policySetContent,
              Pair(1, null)),
          // *POverrides(pol|ref|polRel*,constr?,act|actRel?*,opts?)
          RegistryEntry(
              EntryTypeEnum.PERMIT_OVERRIDES,
              "*POverrides",
              PolicyEntityEnum.POLICY_SET,
              policySetContent,
              Pair(1, null)),
          // *DUnlessP(pol|ref|polRel*,constr?,act|actRel?*,opts?) -> opt: strictUnlessLogic:
          // Boolean? = null
          RegistryEntry(
              EntryTypeEnum.DENY_UNLESS_PERMIT,
              "*DUnlessP",
              PolicyEntityEnum.POLICY_SET,
              policySetContent,
              Pair(1, null)),
          // *PunlessD(pol|ref|polRel*,constr?,act|actRel?*,opts?) -> opt: strictUnlessLogic:
          // Boolean? = null
          RegistryEntry(
              EntryTypeEnum.PERMIT_UNLESS_DENY,
              "*PUnlessD",
              PolicyEntityEnum.POLICY_SET,
              policySetContent,
              Pair(1, null)),
          // *firstAppl(pol|ref|polRel*,constr?,act|actRel?*,opts?)
          RegistryEntry(
              EntryTypeEnum.FIRST_APPLICABLE,
              "*firstAppl",
              PolicyEntityEnum.POLICY_SET,
              policySetContent,
              Pair(1, null)),
      )
  private val policyDefaultContent =
      listOf(PolicyEntityEnum.ACTION_RELATIONSHIP, PolicyEntityEnum.POLICY_ACTION)
  // common options lenientConstraints: Boolean? = null, actionExecutionStrategy: String? = null,
  // ignoreErrors: Boolean? = null, priority: Int? = null

  private val policyDefaultRegistry =
      listOf(
          // #permit(constr?,act|actRel?*,opts?)
          RegistryEntry(
              EntryTypeEnum.DEFAULT_PERMIT,
              "#permit",
              PolicyEntityEnum.POLICY_DEFAULT,
              policyDefaultContent,
              Pair(0, null)),
          // #deny(constr?,act|actRel?*,opts?)
          RegistryEntry(
              EntryTypeEnum.DEFAULT_DENY,
              "#deny",
              PolicyEntityEnum.POLICY_DEFAULT,
              policyDefaultContent,
              Pair(0, null)),
          // #NA(constr?,act|actRel?*,opts?)
          RegistryEntry(
              EntryTypeEnum.DEFAULT_NOT_APPLICABLE,
              "#NA",
              PolicyEntityEnum.POLICY_DEFAULT,
              policyDefaultContent,
              Pair(0, null)),
          // #indDP(constr?,act|actRel?*,opts?)
          RegistryEntry(
              EntryTypeEnum.DEFAULT_INDETERMINATE_DENY_PERMIT,
              "#indDP",
              PolicyEntityEnum.POLICY_DEFAULT,
              policyDefaultContent,
              Pair(0, null)),
          // #indD(constr?,act|actRel?*,opts?)
          RegistryEntry(
              EntryTypeEnum.DEFAULT_INDETERMINATE_DENY,
              "#indD",
              PolicyEntityEnum.POLICY_DEFAULT,
              policyDefaultContent,
              Pair(0, null)),
          // #indP(constr?,act|actRel?*,opts?)
          RegistryEntry(
              EntryTypeEnum.DEFAULT_INDETERMINATE_PERMIT,
              "#indP",
              PolicyEntityEnum.POLICY_DEFAULT,
              policyDefaultContent,
              Pair(0, null)),
      )
  private val policyRegistry =
      policyRuleRegistry + policyDefaultRegistry + policySetRegistry + policyRefRegistry

  private val policyActionContent =
      listOf(
          PolicyEntityEnum.VARIABLE_DYNAMIC,
          PolicyEntityEnum.VARIABLE_STATIC,
          PolicyEntityEnum.REFERENCE,
          PolicyEntityEnum.CONTENT,
      )
  private val policyActionRegistry =
      listOf(
          // *save(key,var|ref,#opts?) -> opt: failOnMissingKey: Boolean? = null, failOnExistingKey:
          // Boolean? = null, failOnNullSource: Boolean? = null,
          RegistryEntry(
              EntryTypeEnum.SAVE,
              "*save",
              PolicyEntityEnum.POLICY_ACTION,
              policyActionContent,
              Pair(2, 2)),
          // *clear(key,#opts?) -> opt: failOnMissingKey: Boolean? = null
          RegistryEntry(
              EntryTypeEnum.CLEAR,
              "*clear",
              PolicyEntityEnum.POLICY_ACTION,
              listOf(PolicyEntityEnum.CONTENT),
              Pair(1, 1)),
          // *patch(key,var|ref,var|ref,#opts?) -> opt: failOnMissingKey: Boolean? = null,
          // failOnExistingKey: Boolean? = null, failOnNullSource: Boolean? = null,
          // castNullSourceToArray: Boolean? = null,
          RegistryEntry(
              EntryTypeEnum.PATCH,
              "*patch",
              PolicyEntityEnum.POLICY_ACTION,
              listOf(
                  PolicyEntityEnum.CONTENT,
                  PolicyEntityEnum.VARIABLE_STATIC,
                  PolicyEntityEnum.VARIABLE_DYNAMIC,
                  PolicyEntityEnum.REFERENCE),
              Pair(3, 3)),
          // *merge(key,var|ref,var|ref,#opts?) -> opt: failOnMissingKey: Boolean? = null,
          // failOnExistingKey: Boolean? = null, failOnNullSource: Boolean? = null, failOnNullMerge:
          // Boolean? = null, destinationType: string? = null, destinationFormat: String? = null
          RegistryEntry(
              EntryTypeEnum.MERGE,
              "*merge",
              PolicyEntityEnum.POLICY_ACTION,
              listOf(
                  PolicyEntityEnum.CONTENT,
                  PolicyEntityEnum.VARIABLE_STATIC,
                  PolicyEntityEnum.VARIABLE_DYNAMIC,
                  PolicyEntityEnum.REFERENCE),
              Pair(3, 3)),
      )

  internal val registry =
      (dslRegistry +
              policyVariableRegistry +
              policyVariableResolverRegistry +
              policyConditionRegistry +
              policyRegistry +
              policyActionRegistry)
          .toSet()

  val minCmdLength = registry.map { it.command }.minByOrNull { it.length }!!.length
  val maxCmdLength = registry.map { it.command }.maxByOrNull { it.length }!!.length
}
