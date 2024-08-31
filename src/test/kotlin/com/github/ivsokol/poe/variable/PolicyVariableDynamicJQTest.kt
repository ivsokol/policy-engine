package com.github.ivsokol.poe.variable

import com.github.ivsokol.poe.Context
import com.github.ivsokol.poe.DefaultObjectMapper
import com.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.*

private data class BarDynamicJQ(val a2: String)

private data class FooDynamicJQ(val a1: BarDynamicJQ)

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
        "foo" to FooDynamicJQ(BarDynamicJQ("b1")))

private val subjectStore = mapOf("foo" to FooDynamicJQ(BarDynamicJQ("b2")))

private data class PolicyVariableDynamicJQTestData(
    val given: PolicyVariableDynamic,
    val expected: VariableValue
)

class PolicyVariableDynamicJQTest :
    FunSpec({
      val context = Context(request = fullObjectBody, subject = subjectStore)

      val objectMapper = DefaultObjectMapper()

      withData(
          nameFn = { "RuntimeTypeAnyTest:JQ-${it.expected.type}:${it.expected.body}" },
          listOf(
              PolicyVariableDynamicJQTestData(
                  PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".str", engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.STRING),
                  VariableValue(VariableRuntimeTypeEnum.STRING, "a")),
              PolicyVariableDynamicJQTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".i", engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.INT)),
                  VariableValue(VariableRuntimeTypeEnum.INT, 1)),
              PolicyVariableDynamicJQTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".b", engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.BOOLEAN)),
                  VariableValue(VariableRuntimeTypeEnum.BOOLEAN, true)),
              PolicyVariableDynamicJQTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".n", engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.STRING)),
                  NullVariableValue()),
              PolicyVariableDynamicJQTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".num", engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.NUMBER)),
                  VariableValue(VariableRuntimeTypeEnum.DOUBLE, 1.1)),
              PolicyVariableDynamicJQTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".date", engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.STRING,
                      format = VariableValueFormatEnum.DATE)),
                  VariableValue(VariableRuntimeTypeEnum.DATE, LocalDate.parse("2024-12-19"))),
              PolicyVariableDynamicJQTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".dateTime",
                                  engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.STRING,
                      format = VariableValueFormatEnum.DATE_TIME)),
                  VariableValue(
                      VariableRuntimeTypeEnum.DATE_TIME,
                      OffsetDateTime.parse("2024-12-19T12:00:00Z"))),
              PolicyVariableDynamicJQTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".time", engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.STRING,
                      format = VariableValueFormatEnum.TIME)),
                  VariableValue(
                      VariableRuntimeTypeEnum.TIME, LocalTime.parse("12:46:52.12456789"))),
              PolicyVariableDynamicJQTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".period", engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.STRING,
                      format = VariableValueFormatEnum.PERIOD)),
                  VariableValue(VariableRuntimeTypeEnum.PERIOD, Period.parse("P1D"))),
              PolicyVariableDynamicJQTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".duration",
                                  engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.STRING,
                      format = VariableValueFormatEnum.DURATION)),
                  VariableValue(VariableRuntimeTypeEnum.DURATION, Duration.parse("PT1H"))),
              PolicyVariableDynamicJQTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".bigD", engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.NUMBER,
                      format = VariableValueFormatEnum.BIG_DECIMAL)),
                  VariableValue(VariableRuntimeTypeEnum.BIG_DECIMAL, BigDecimal("3.14"))),
              PolicyVariableDynamicJQTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".array", engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.ARRAY,
                      format = VariableValueFormatEnum.JSON)),
                  VariableValue(
                      VariableRuntimeTypeEnum.ARRAY_NODE, objectMapper.readTree("[1,2,3]"))),
              PolicyVariableDynamicJQTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".object", engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.OBJECT,
                      format = VariableValueFormatEnum.JSON)),
                  VariableValue(
                      VariableRuntimeTypeEnum.OBJECT_NODE,
                      objectMapper.readTree("""{"a1":"b1"}"""))),
              PolicyVariableDynamicJQTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".strObj.a2",
                                  engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.OBJECT,
                      format = VariableValueFormatEnum.JSON)),
                  NullVariableValue()),
              PolicyVariableDynamicJQTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".foo.a1.a2",
                                  engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.STRING,
                  )),
                  VariableValue(VariableRuntimeTypeEnum.STRING, "b1")),
              PolicyVariableDynamicJQTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".a1.a2",
                                  key = "foo",
                                  engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.STRING,
                  )),
                  VariableValue(VariableRuntimeTypeEnum.STRING, "b1")),
              PolicyVariableDynamicJQTestData(
                  (PolicyVariableDynamic(
                      resolvers =
                          listOf(
                              PolicyVariableResolver(
                                  path = ".a1.a2",
                                  key = "foo",
                                  source = ContextStoreEnum.SUBJECT,
                                  engine = PolicyVariableResolverEngineEnum.JQ)),
                      type = VariableValueTypeEnum.STRING,
                  )),
                  VariableValue(VariableRuntimeTypeEnum.STRING, "b2")),
          )) { (given, expected) ->
            val actual = given.resolve(context, EmptyPolicyCatalog())
            actual shouldBe expected
          }
    })
