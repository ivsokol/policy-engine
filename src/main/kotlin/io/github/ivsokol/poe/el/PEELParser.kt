package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.action.*
import io.github.ivsokol.poe.condition.*
import io.github.ivsokol.poe.policy.IPolicy
import io.github.ivsokol.poe.policy.Policy
import io.github.ivsokol.poe.policy.PolicyDefault
import io.github.ivsokol.poe.policy.PolicySet
import io.github.ivsokol.poe.variable.IPolicyVariable
import io.github.ivsokol.poe.variable.PolicyVariableDynamic
import io.github.ivsokol.poe.variable.PolicyVariableResolver
import io.github.ivsokol.poe.variable.PolicyVariableStatic
import org.slf4j.LoggerFactory

/**
 * Parses the input string and returns the corresponding policy variable resolver, variable,
 * condition, action, or policy object.
 *
 * This class is responsible for parsing the input string, which is expected to be in a specific
 * domain-specific language (DSL) format, and returning the appropriate object based on the parsed
 * content. The parsing process involves identifying the type of entity (variable, condition,
 * action, policy, etc.) and then deserializing the input into the corresponding object.
 *
 * The parser supports various types of entities, including static and dynamic variables, atomic and
 * composite conditions, different types of actions (save, clear, merge, patch), and policies
 * (single, set, default). It also handles DSL-specific constructs like constraints, action
 * relationships, and policy relationships.
 *
 * The parser is designed to be robust and handle various input formats, with detailed error
 * handling and validation to ensure the input is well-formed and matches the expected structure.
 */
class PEELParser(internal val input: String) {
  private val log = LoggerFactory.getLogger(this::class.java)

  init {
    require(input.isNotBlank()) { "Input must not be blank" }
  }

  /**
   * Parses the input string and returns the corresponding [PolicyVariableResolver] object.
   *
   * This method is responsible for parsing the input string and deserializing it into a
   * [PolicyVariableResolver] object. The parsing process involves identifying the type of entity
   * (in this case, a value resolver) and then deserializing the input into the corresponding
   * object.
   *
   * @return the parsed [PolicyVariableResolver] object
   * @throws IllegalArgumentException if the input string is not a valid value resolver
   */
  fun parseVariableResolver(): PolicyVariableResolver =
      parse(PolicyEntityEnum.VALUE_RESOLVER) as PolicyVariableResolver

  /**
   * Parses the input string and returns the corresponding [IPolicyVariable] object.
   *
   * This method is responsible for parsing the input string and deserializing it into a
   * [IPolicyVariable] object. The parsing process involves identifying the type of entity (in this
   * case, a variable) and then deserializing the input into the corresponding object, which can be
   * either a [PolicyVariableStatic] or a [PolicyVariableDynamic].
   *
   * @return the parsed [IPolicyVariable] object
   * @throws IllegalArgumentException if the input string is not a valid variable command
   */
  fun parseVariable(): IPolicyVariable {
    val cmd = parseCmdName(0, input)
    return when (cmd.entityType) {
      PolicyEntityEnum.VARIABLE_STATIC ->
          parse(PolicyEntityEnum.VARIABLE_STATIC) as PolicyVariableStatic
      PolicyEntityEnum.VARIABLE_DYNAMIC ->
          parse(PolicyEntityEnum.VARIABLE_DYNAMIC) as PolicyVariableDynamic
      else -> throw IllegalArgumentException("Invalid variable command: ${cmd.command}")
    }
  }

  /**
   * Parses the input string and returns the corresponding [IPolicyCondition] object.
   *
   * This method is responsible for parsing the input string and deserializing it into a
   * [IPolicyCondition] object. The parsing process involves identifying the type of condition
   * (atomic, composite, or default) and then deserializing the input into the corresponding object.
   *
   * @return the parsed [IPolicyCondition] object
   * @throws IllegalArgumentException if the input string is not a valid condition command
   */
  fun parseCondition(): IPolicyCondition {
    val cmd = parseCmdName(0, input)
    return when (cmd.entityType) {
      PolicyEntityEnum.CONDITION_ATOMIC ->
          parse(PolicyEntityEnum.CONDITION_ATOMIC) as PolicyConditionAtomic
      PolicyEntityEnum.CONDITION_COMPOSITE ->
          parse(PolicyEntityEnum.CONDITION_COMPOSITE) as PolicyConditionComposite
      PolicyEntityEnum.CONDITION_DEFAULT ->
          parse(PolicyEntityEnum.CONDITION_DEFAULT) as PolicyConditionDefault
      else -> throw IllegalArgumentException("Invalid condition command: ${cmd.command}")
    }
  }

  /**
   * Parses the input string and returns the corresponding [IPolicyAction] object.
   *
   * This method is responsible for parsing the input string and deserializing it into an
   * [IPolicyAction] object. The parsing process involves identifying the type of action (save,
   * clear, merge, or patch) and then deserializing the input into the corresponding object.
   *
   * @return the parsed [IPolicyAction] object
   * @throws IllegalArgumentException if the input string is not a valid action command
   */
  fun parseAction(): IPolicyAction {
    val cmd = parseCmdName(0, input)
    return when (cmd.entryType) {
      EntryTypeEnum.SAVE -> parse(PolicyEntityEnum.POLICY_ACTION) as PolicyActionSave
      EntryTypeEnum.CLEAR -> parse(PolicyEntityEnum.POLICY_ACTION) as PolicyActionClear
      EntryTypeEnum.MERGE -> parse(PolicyEntityEnum.POLICY_ACTION) as PolicyActionJsonMerge
      EntryTypeEnum.PATCH -> parse(PolicyEntityEnum.POLICY_ACTION) as PolicyActionJsonPatch
      else -> throw IllegalArgumentException("Invalid condition command: ${cmd.command}")
    }
  }

  /**
   * Parses the input string and returns the corresponding [IPolicy] object.
   *
   * This method is responsible for parsing the input string and deserializing it into an [IPolicy]
   * object. The parsing process involves identifying the type of policy (policy, policy set, or
   * default policy) and then deserializing the input into the corresponding object.
   *
   * @return the parsed [IPolicy] object
   * @throws IllegalArgumentException if the input string is not a valid policy command
   */
  fun parsePolicy(): IPolicy {
    val cmd = parseCmdName(0, input)
    return when (cmd.entityType) {
      PolicyEntityEnum.POLICY -> parse(PolicyEntityEnum.POLICY) as Policy
      PolicyEntityEnum.POLICY_SET -> parse(PolicyEntityEnum.POLICY_SET) as PolicySet
      PolicyEntityEnum.POLICY_DEFAULT -> parse(PolicyEntityEnum.POLICY_DEFAULT) as PolicyDefault
      else -> throw IllegalArgumentException("Invalid condition command: ${cmd.command}")
    }
  }

  /**
   * Parses the input string and returns the corresponding object of the specified
   * [PolicyEntityEnum] type.
   *
   * This method is a helper function that calls [parseCmd] to parse the input string and return the
   * first element of the returned pair, which represents the parsed object.
   *
   * @param rootEntityType the expected root entity type of the parsed object, or `null` if any type
   *   is allowed
   * @return the parsed object of the specified [PolicyEntityEnum] type
   * @throws IllegalArgumentException if the parsed object does not match the expected root entity
   *   type
   */
  private fun parse(rootEntityType: PolicyEntityEnum? = null): Any =
      parseCmd(0, rootEntityType).first

  /**
   * Determines the [PolicyEntityEnum] type for a given DSL command.
   *
   * This function takes a [RegistryEntry] object representing a DSL command and returns the
   * appropriate [PolicyEntityEnum] type based on the command's entry type.
   *
   * @param cmd the [RegistryEntry] object representing the DSL command
   * @return the [PolicyEntityEnum] type for the given DSL command
   * @throws IllegalArgumentException if the command has an invalid entry type
   */
  private fun getEntityTypeForDSL(cmd: RegistryEntry): PolicyEntityEnum =
      if (cmd.entityType == PolicyEntityEnum.DSL) {
        when (cmd.entryType) {
          EntryTypeEnum.CONSTRAINT -> PolicyEntityEnum.CONSTRAINT
          EntryTypeEnum.ACTION_RELATIONSHIP -> PolicyEntityEnum.ACTION_RELATIONSHIP
          EntryTypeEnum.POLICY_RELATIONSHIP -> PolicyEntityEnum.POLICY_RELATIONSHIP
          else -> throw IllegalArgumentException("Invalid command: ${cmd.command}")
        }
      } else cmd.entityType

  /**
   * Parses the input string and returns the corresponding object of the specified
   * [PolicyEntityEnum] type.
   *
   * This method is a helper function that calls [parseCmd] to parse the input string and return the
   * first element of the returned pair, which represents the parsed object.
   *
   * @param rootEntityType the expected root entity type of the parsed object, or `null` if any type
   *   is allowed
   * @return the parsed object of the specified [PolicyEntityEnum] type
   * @throws IllegalArgumentException if the parsed object does not match the expected root entity
   *   type
   */
  private fun parseCmd(
      position: Int = 0,
      rootEntityType: PolicyEntityEnum? = null
  ): Pair<Any, Int> {
    log.debug("Parsing command at position: {}", position)
    var pos = position
    val cmd = parseCmdName(pos, input)
    log.trace("Parsed command: {}", cmd)
    pos += cmd.command.length
    if (rootEntityType != null) {
      require(cmd.entityType == rootEntityType) {
        "Root entity type mismatch for command '${cmd.command}'. Expected: '${rootEntityType.name}', actual: '${cmd.entityType.name}'"
      }
    }
    val contents = mutableListOf<String>()
    var options: ELOptions? = null
    val commands = mutableListOf<Any>()
    var constraint: IPolicyConditionRefOrValue? = null
    var cmdStarted = false
    var cmdCompleted = false
    do {
      val chr = input[pos]
      val chrType = charType(chr)
      when (chrType) {
        CharTypeEnum.CMD -> {
          // get child command
          val childCmd = parseCmdName(pos, input)
          // handle #opts
          if (childCmd.entryType == EntryTypeEnum.OPTIONS) {
            val result = parseOptions(pos, input)
            options = result.first
            pos = result.second
            continue
          }
          // handle *ref
          if (childCmd.entryType == EntryTypeEnum.REF) {
            val result = parseRef(pos, input)
            val refEntityType = getEntityTypeForDSL(cmd)
            commands.add(deserializeRef(refEntityType, result.first))
            pos = result.second
            continue
          }
          // handle *constraint
          if (childCmd.entryType == EntryTypeEnum.CONSTRAINT) {
            val result = parseCmd(pos, PolicyEntityEnum.DSL)
            constraint = result.first as IPolicyConditionRefOrValue
            pos = result.second
            continue
          }
          // validate child command
          check(getEntityTypeForDSL(childCmd) in cmd.childCmdTypes) {
            "Child command type mismatch on position $pos for command '${cmd.command}'. Expected: '${
                            cmd.childCmdTypes.joinToString(
                                ", "
                            )
                        }', actual: '${childCmd.entityType.name}'"
          }
          val result = parseCmd(pos, childCmd.entityType)
          commands.add(result.first)
          pos = result.second
          continue
        }
        CharTypeEnum.CMD_START -> {
          cmdStarted = true
          pos++
          continue
        }
        CharTypeEnum.CMD_END -> {
          cmdCompleted = true
          pos++
          pos = skipNonParseableChars(pos, input)
          break
        }
        CharTypeEnum.DELIMITER -> {
          pos++
          continue
        }
        CharTypeEnum.NON_PARSABLE_CHAR -> {
          pos++
          continue
        }
        CharTypeEnum.CONTENT -> {
          val result = parseContent(pos, input, DELIMITER)
          pos = result.second
          contents.add(result.first)
          continue
        }
      }
    } while (pos < input.length)
    log.trace("Position after parsing command: {}", pos)
    log.trace("Parsed contents: {}", contents.joinToString(", "))
    log.trace("Parsed child command size: {}", commands.size)
    log.trace("Parsed constraint: {}", constraint)
    log.trace("Parsed options: {}", options)
    // validate
    check(cmdStarted) { "Command not started on position $position" }
    check(cmdCompleted) { "Command not completed on position $position" }
    if (cmd.argsCount.first > 0) {
      check(contents.size + commands.size >= cmd.argsCount.first) {
        "Not enough arguments on position $position for command '${cmd.command}'. Expected: ${cmd.argsCount.first}, actual: ${contents.size + commands.size}"
      }
    }
    if (cmd.argsCount.second != null) {
      val argsLimit: Int = cmd.argsCount.second!!
      check(contents.size + commands.size <= argsLimit) {
        "Too many arguments on position $position for command '${cmd.command}'. Expected: $argsLimit, actual: ${contents.size + commands.size}"
      }
    }
    // deserialize
    val deserializedCommand =
        when (cmd.entityType) {
          PolicyEntityEnum.VARIABLE_STATIC ->
              PolicyVariableStaticELDeserializer.deserialize(
                  cmd, commands.toList(), contents.toList(), options)
          PolicyEntityEnum.VALUE_RESOLVER ->
              PolicyVariableResolverELDeserializer.deserialize(
                  cmd, commands.toList(), contents.toList(), options)
          PolicyEntityEnum.VARIABLE_DYNAMIC -> {
            PolicyVariableDynamicELDeserializer.deserialize(
                cmd, commands.toList(), contents.toList(), options)
          }
          PolicyEntityEnum.CONDITION_ATOMIC -> {
            PolicyConditionAtomicELDeserializer.deserialize(
                cmd, commands.toList(), contents.toList(), options)
          }
          PolicyEntityEnum.CONDITION_COMPOSITE -> {
            PolicyConditionCompositeELDeserializer.deserialize(
                cmd, commands.toList(), contents.toList(), options)
          }
          PolicyEntityEnum.CONDITION_DEFAULT -> {
            PolicyConditionDefaultELDeserializer.deserialize(
                cmd, commands.toList(), contents.toList(), options)
          }
          PolicyEntityEnum.POLICY_ACTION -> {
            PolicyActionELDeserializer.deserialize(
                cmd, commands.toList(), contents.toList(), options)
          }
          PolicyEntityEnum.DSL -> {
            when (cmd.entryType) {
              EntryTypeEnum.CONSTRAINT -> {
                PolicyConstraintELDeserializer.deserialize(
                    cmd, commands.toList(), contents.toList(), options)
              }
              EntryTypeEnum.ACTION_RELATIONSHIP -> {
                PolicyActionRelationshipELDeserializer.deserialize(
                    cmd, commands.toList(), contents.toList(), options, constraint)
              }
              EntryTypeEnum.POLICY_RELATIONSHIP -> {
                PolicyRelationshipELDeserializer.deserialize(
                    cmd, commands.toList(), contents.toList(), options, constraint)
              }
              else ->
                  throw IllegalArgumentException(
                      "Unknown entry type for DSL entity type: ${cmd.entryType}")
            }
          }
          PolicyEntityEnum.POLICY -> {
            PolicyELDeserializer.deserialize(
                cmd, commands.toList(), contents.toList(), options, constraint)
          }
          PolicyEntityEnum.POLICY_SET -> {
            PolicySetELDeserializer.deserialize(
                cmd, commands.toList(), contents.toList(), options, constraint)
          }
          PolicyEntityEnum.POLICY_DEFAULT -> {
            PolicyDefaultELDeserializer.deserialize(
                cmd, commands.toList(), contents.toList(), options, constraint)
          }
          else -> throw IllegalArgumentException("Unsupported entity type: ${cmd.entityType}")
        }
    log.debug("Deserialized command: {}", deserializedCommand)

    log.trace("Position after deserialization: {}", pos)
    return deserializedCommand to pos
  }
}
