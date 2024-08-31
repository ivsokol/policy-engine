package io.github.ivsokol.poe.examples.catalog

import io.github.ivsokol.poe.CalVer
import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.Options
import io.github.ivsokol.poe.PolicyEngine
import io.github.ivsokol.poe.action.PolicyActionRef
import io.github.ivsokol.poe.action.PolicyActionSave
import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.catalog.catalogSerializersModule
import io.github.ivsokol.poe.condition.*
import io.github.ivsokol.poe.event.EventLevelEnum
import io.github.ivsokol.poe.event.InMemoryEventHandler
import io.github.ivsokol.poe.examples.ExampleWriter
import io.github.ivsokol.poe.policy.*
import io.github.ivsokol.poe.variable.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class AccessControlTest :
    FunSpec({
      val logger = LoggerFactory.getLogger("test")
      val json = Json {
        serializersModule = catalogSerializersModule
        explicitNulls = false
        encodeDefaults = true
        prettyPrint = true
      }
      val folder = "policy-catalog"
      val catalog =
          PolicyCatalog(
              id = "access-control",
              version = CalVer(2024, 2, 17),
              policyConditions =
                  listOf(
                      PolicyConditionAtomic(
                          id = "isAdmin",
                          description = "Checks if provided role is equal to 'admin'",
                          operation = OperationEnum.EQUALS,
                          stringIgnoreCase = true,
                          args =
                              listOf(
                                  PolicyVariableStatic(
                                      value = "admin", type = VariableValueTypeEnum.STRING),
                                  PolicyVariableRef("role"))),
                      PolicyConditionAtomic(
                          id = "isUser",
                          description = "Checks if provided role is equal to 'user'",
                          operation = OperationEnum.EQUALS,
                          stringIgnoreCase = true,
                          args =
                              listOf(
                                  PolicyVariableStatic(
                                      value = "user", type = VariableValueTypeEnum.STRING),
                                  PolicyVariableRef("role"))),
                      PolicyConditionAtomic(
                          id = "isWorkingDay",
                          description = "Checks if it is working day currently (Mon-Fri)",
                          operation = OperationEnum.LESS_THAN_EQUAL,
                          args =
                              listOf(
                                  PolicyVariableRef("dayOfWeek"),
                                  PolicyVariableStatic(
                                      value = 5, type = VariableValueTypeEnum.INT))),
                      PolicyConditionComposite(
                          id = "isWorkingHour",
                          description = "Checks if it is working hour currently (09:00-17:00)",
                          conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                          conditions =
                              listOf(
                                  PolicyConditionAtomic(
                                      operation = OperationEnum.GREATER_THAN_EQUAL,
                                      args =
                                          listOf(
                                              PolicyVariableRef("currentTime"),
                                              PolicyVariableStatic(
                                                  value = LocalTime.of(9, 0),
                                                  type = VariableValueTypeEnum.STRING,
                                                  format = VariableValueFormatEnum.TIME,
                                                  timeFormat = "HH:mm"))),
                                  PolicyConditionAtomic(
                                      operation = OperationEnum.LESS_THAN_EQUAL,
                                      args =
                                          listOf(
                                              PolicyVariableRef("currentTime"),
                                              PolicyVariableStatic(
                                                  value = LocalTime.of(17, 0),
                                                  type = VariableValueTypeEnum.STRING,
                                                  format = VariableValueFormatEnum.TIME,
                                                  timeFormat = "HH:mm"))))),
                      PolicyConditionComposite(
                          id = "regularUserAccess",
                          description =
                              "Checks if user has role 'user' and if it is a working day and working hour",
                          conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                          conditions =
                              listOf(
                                  PolicyConditionRef("isUser"),
                                  PolicyConditionRef("isWorkingDay"),
                                  PolicyConditionRef("isWorkingHour"),
                              ))),
              policyVariables =
                  listOf(
                      PolicyVariableDynamic(
                          id = "role",
                          description = "Provided role",
                          resolvers = listOf(PolicyVariableResolverRef("roleResolver")),
                          type = VariableValueTypeEnum.STRING),
                      PolicyVariableDynamic(
                          id = "currentTime",
                          description = "Current time",
                          resolvers =
                              listOf(
                                  PolicyVariableResolver(
                                      key = "localTime", source = ContextStoreEnum.ENVIRONMENT)),
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.TIME),
                      PolicyVariableDynamic(
                          id = "dayOfWeek",
                          description = "Current day of week",
                          resolvers =
                              listOf(
                                  PolicyVariableResolver(
                                      key = "dayOfWeek", source = ContextStoreEnum.ENVIRONMENT)),
                          type = VariableValueTypeEnum.INT)),
              policyVariableResolvers =
                  listOf(
                      PolicyVariableResolver(
                          id = "roleResolver",
                          description = "Extracts role from subject store",
                          key = "role",
                          source = ContextStoreEnum.SUBJECT),
                  ),
              policies =
                  listOf(
                      Policy(
                          id = "userAccess",
                          description =
                              "Allows access to regular user if it is working day and working hour",
                          targetEffect = PolicyTargetEffectEnum.PERMIT,
                          strictTargetEffect = true,
                          condition = PolicyConditionRef("regularUserAccess")),
                      Policy(
                          id = "adminAccess",
                          description = "Allows access to admin user",
                          targetEffect = PolicyTargetEffectEnum.PERMIT,
                          strictTargetEffect = true,
                          condition = PolicyConditionRef("isAdmin")),
                      PolicySet(
                          id = "checkAccess",
                          description = "Checks if user has access",
                          policyCombinationLogic = PolicyCombinationLogicEnum.DENY_UNLESS_PERMIT,
                          policies =
                              listOf(
                                  PolicyRelationship(policy = PolicyRef("userAccess")),
                                  PolicyRelationship(
                                      policy = PolicyRef("adminAccess"), priority = 10),
                              ),
                          actions =
                              listOf(
                                  PolicyActionRelationship(
                                      executionMode = setOf(ActionExecutionModeEnum.ON_DENY),
                                      action = PolicyActionRef("setForbiddenMessage")),
                                  PolicyActionRelationship(
                                      executionMode = setOf(ActionExecutionModeEnum.ON_PERMIT),
                                      action = PolicyActionRef("setAllowedMessage"))))),
              policyActions =
                  listOf(
                      PolicyActionSave(
                          id = "setForbiddenMessage",
                          description = "Sets message for user for which access has been denied",
                          key = "message",
                          value =
                              PolicyVariableDynamic(
                                  type = VariableValueTypeEnum.STRING,
                                  resolvers =
                                      listOf(
                                          PolicyVariableResolver(
                                              engine = PolicyVariableResolverEngineEnum.JQ,
                                              source = ContextStoreEnum.SUBJECT,
                                              path =
                                                  """"Access has been denied for " + .username""")))),
                      PolicyActionSave(
                          id = "setAllowedMessage",
                          description = "Sets message for user who has been granted access",
                          key = "message",
                          value =
                              PolicyVariableDynamic(
                                  type = VariableValueTypeEnum.STRING,
                                  resolvers =
                                      listOf(
                                          PolicyVariableResolver(
                                              engine = PolicyVariableResolverEngineEnum.JQ,
                                              source = ContextStoreEnum.SUBJECT,
                                              path =
                                                  """"Access has been granted for " + .username"""))))))
      val catalogJson = json.encodeToString(catalog)
      test("should write catalog to examples folder") {
        ExampleWriter.save(folder, "policy-catalog-access-control", catalogJson)
      }

      test("should allow user in working hours") {
        val engine = PolicyEngine(catalogJson)
        val instant = Instant.parse("2024-08-23T13:42:56+00:00")
        val clock = Clock.fixed(instant, ZoneOffset.ofHours(0))
        val options = Options(clock = clock)
        val context =
            Context(
                subject = mapOf("role" to "user", "username" to "user1"),
                options = options,
                event = InMemoryEventHandler(EventLevelEnum.DETAILS))
        val result = engine.evaluatePolicy("checkAccess", context = context)
        context.id shouldNotBe null
        result.first shouldBe PolicyResultEnum.PERMIT
        context.dataStore().containsKey("message") shouldBe true
        context.dataStore()["message"] shouldBe "Access has been granted for user1"

        logger.info("result: $result")
        logger.info("context events:\n{}", context.event.list())
        logger.info("context cache:\n{}", context.cache)
        logger.info("context data store:\n{}", context.dataStore())
      }
      test("should forbid user outside of working hours") {
        val engine = PolicyEngine(catalogJson)
        val instant = Instant.parse("2024-08-23T23:42:56+00:00")
        val clock = Clock.fixed(instant, ZoneOffset.ofHours(0))
        val options = Options(clock = clock)
        val context =
            Context(
                subject = mapOf("role" to "user", "username" to "user1"),
                options = options,
                event = InMemoryEventHandler(EventLevelEnum.DETAILS))
        val result = engine.evaluatePolicy("checkAccess", context = context)
        context.id shouldNotBe null
        result.first shouldBe PolicyResultEnum.DENY
        context.dataStore().containsKey("message") shouldBe true
        context.dataStore()["message"] shouldBe "Access has been denied for user1"

        logger.info("result: $result")
        logger.info("context events:\n{}", context.event.list())
        logger.info("context cache:\n{}", context.cache)
        logger.info("context data store:\n{}", context.dataStore())
      }
      test("should allow admin in working hours") {
        val engine = PolicyEngine(catalogJson)
        val instant = Instant.parse("2024-08-23T13:42:56+00:00")
        val clock = Clock.fixed(instant, ZoneOffset.ofHours(0))
        val options = Options(clock = clock)
        val context =
            Context(
                subject = mapOf("role" to "admin", "username" to "admin1"),
                options = options,
                event = InMemoryEventHandler(EventLevelEnum.DETAILS))
        val result = engine.evaluatePolicy("checkAccess", context = context)
        context.id shouldNotBe null
        result.first shouldBe PolicyResultEnum.PERMIT
        context.dataStore().containsKey("message") shouldBe true
        context.dataStore()["message"] shouldBe "Access has been granted for admin1"
      }
      test("should allow admin outside of working hours") {
        val engine = PolicyEngine(catalogJson)
        val instant = Instant.parse("2024-08-23T23:42:56+00:00")
        val clock = Clock.fixed(instant, ZoneOffset.ofHours(0))
        val options = Options(clock = clock)
        val context =
            Context(
                subject = mapOf("role" to "admin", "username" to "admin1"),
                options = options,
                event = InMemoryEventHandler(EventLevelEnum.DETAILS))
        val result = engine.evaluatePolicy("checkAccess", context = context)
        context.id shouldNotBe null
        result.first shouldBe PolicyResultEnum.PERMIT
        context.dataStore().containsKey("message") shouldBe true
        context.dataStore()["message"] shouldBe "Access has been granted for admin1"

        logger.info("result: $result")
        logger.info("context events:\n{}", context.event.list())
        logger.info("context cache:\n{}", context.cache)
        logger.info("context data store:\n{}", context.dataStore())
      }

      test("should call all policies") {
        val engine = PolicyEngine(catalogJson)
        val instant = Instant.parse("2024-08-23T23:42:56+00:00")
        val clock = Clock.fixed(instant, ZoneOffset.ofHours(0))
        val options = Options(clock = clock)
        val context =
            Context(
                subject = mapOf("role" to "user", "username" to "user1"),
                options = options,
                event = InMemoryEventHandler(EventLevelEnum.DETAILS))
        val result = engine.evaluateAllPolicies(context = context)
        context.id shouldNotBe null
        result.size shouldBe 3
        context.dataStore().containsKey("message") shouldBe true
        context.dataStore()["message"] shouldBe "Access has been denied for user1"
      }
    })
