package com.github.ivsokol.poe.variable

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.DefaultObjectMapper
import com.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.*

private data class BarDynamicJMESPath(val a2: String)

private data class FooDynamicJMESPath(val a1: BarDynamicJMESPath)

private val fullObjectBody =
    mapOf(
        "str" to "a",
        "i" to 1,
        "b" to true,
        "n" to null,
        "num" to 1.1,
        "date" to LocalDate.parse("2024-12-19"),
        "dateTime" to OffsetDateTime.parse("2024-12-19T12:00:00Z"),
        "time" to LocalTime.parse("12:46:52.12456789"),
        "period" to Period.parse("P1D"),
        "duration" to Duration.parse("PT1H"),
        "bigD" to BigDecimal("3.14"),
        "array" to listOf(1, 2, 3),
        "object" to mapOf("a1" to "b1"),
        "strObj" to """{"a2":"b2"}""",
        "foo" to FooDynamicJMESPath(BarDynamicJMESPath("b1")))

private val subjectStore = mapOf("foo" to FooDynamicJMESPath(BarDynamicJMESPath("b2")))

private data class PolicyVariableDynamicJMESPathTestData(
    val given: PolicyVariableDynamic,
    val expected: VariableValue
)

class PolicyVariableDynamicJMESPathTest :
    FunSpec({
      val context = Context(request = fullObjectBody, subject = subjectStore)

      val objectMapper = DefaultObjectMapper()

      withData(
          nameFn = { "RuntimeTypeAnyTest:JMESPath-${it.expected.type}:${it.expected.body}" },
          listOf(
              PolicyVariableDynamicJMESPathTestData(
                  PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                                  path = "str")),
                      type = VariableValueTypeEnum.STRING),
                  VariableValue(VariableRuntimeTypeEnum.STRING, "a")),
              PolicyVariableDynamicJMESPathTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH, path = "i")),
                      type = VariableValueTypeEnum.INT)),
                  VariableValue(VariableRuntimeTypeEnum.INT, 1)),
              PolicyVariableDynamicJMESPathTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH, path = "b")),
                      type = VariableValueTypeEnum.BOOLEAN)),
                  VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true)),
              PolicyVariableDynamicJMESPathTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH, path = "n")),
                      type = VariableValueTypeEnum.STRING)),
                  NullVariableValue()),
              PolicyVariableDynamicJMESPathTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                                  path = "num")),
                      type = VariableValueTypeEnum.NUMBER)),
                  VariableValue(VariableRuntimeTypeEnum.DOUBLE, 1.1)),
              PolicyVariableDynamicJMESPathTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                                  path = "date")),
                      type = VariableValueTypeEnum.STRING,
                      format = VariableValueFormatEnum.DATE)),
                  VariableValue(VariableRuntimeTypeEnum.DATE, LocalDate.parse("2024-12-19"))),
              PolicyVariableDynamicJMESPathTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                                  path = "dateTime")),
                      type = VariableValueTypeEnum.STRING,
                      format = VariableValueFormatEnum.DATE_TIME)),
                  VariableValue(
                      VariableRuntimeTypeEnum.DATE_TIME,
                      OffsetDateTime.parse("2024-12-19T12:00:00Z"))),
              PolicyVariableDynamicJMESPathTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                                  path = "time")),
                      type = VariableValueTypeEnum.STRING,
                      format = VariableValueFormatEnum.TIME)),
                  VariableValue(
                      VariableRuntimeTypeEnum.TIME, LocalTime.parse("12:46:52.12456789"))),
              PolicyVariableDynamicJMESPathTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                                  path = "period")),
                      type = VariableValueTypeEnum.STRING,
                      format = VariableValueFormatEnum.PERIOD)),
                  VariableValue(VariableRuntimeTypeEnum.PERIOD, Period.parse("P1D"))),
              PolicyVariableDynamicJMESPathTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                                  path = "duration")),
                      type = VariableValueTypeEnum.STRING,
                      format = VariableValueFormatEnum.DURATION)),
                  VariableValue(VariableRuntimeTypeEnum.DURATION, Duration.parse("PT1H"))),
              PolicyVariableDynamicJMESPathTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                                  path = "bigD")),
                      type = VariableValueTypeEnum.NUMBER,
                      format = VariableValueFormatEnum.BIG_DECIMAL)),
                  VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal("3.14"))),
              PolicyVariableDynamicJMESPathTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                                  path = "array")),
                      type = VariableValueTypeEnum.ARRAY,
                      format = VariableValueFormatEnum.JSON)),
                  VariableValue(
                      VariableRuntimeTypeEnum.ARRAY_NODE, objectMapper.readTree("[1,2,3]"))),
              PolicyVariableDynamicJMESPathTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                                  path = "object")),
                      type = VariableValueTypeEnum.OBJECT,
                      format = VariableValueFormatEnum.JSON)),
                  VariableValue(
                      VariableRuntimeTypeEnum.OBJECT_NODE,
                      objectMapper.readTree("""{"a1":"b1"}"""))),
              PolicyVariableDynamicJMESPathTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                                  path = "strObj.a2")),
                      type = VariableValueTypeEnum.OBJECT,
                      format = VariableValueFormatEnum.JSON)),
                  NullVariableValue()),
              PolicyVariableDynamicJMESPathTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                                  path = "foo.a1.a2")),
                      type = VariableValueTypeEnum.STRING,
                  )),
                  VariableValue(VariableRuntimeTypeEnum.STRING, "b1")),
              PolicyVariableDynamicJMESPathTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                                  key = "foo",
                                  path = "a1.a2")),
                      type = VariableValueTypeEnum.STRING,
                  )),
                  VariableValue(VariableRuntimeTypeEnum.STRING, "b1")),
              PolicyVariableDynamicJMESPathTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                                  key = "foo",
                                  source = ContextStoreEnum.SUBJECT,
                                  path = "a1.a2")),
                      type = VariableValueTypeEnum.STRING,
                  )),
                  VariableValue(VariableRuntimeTypeEnum.STRING, "b2")),
          )) { (given, expected) ->
            val actual = given.resolve(context, EmptyPolicyCatalog())
            actual shouldBe expected
          }
    })
