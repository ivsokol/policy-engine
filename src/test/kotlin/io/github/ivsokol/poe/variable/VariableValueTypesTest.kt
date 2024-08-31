package io.github.ivsokol.poe.variable

import io.github.ivsokol.poe.DefaultObjectMapper
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

private data class RuntimeTypeAnyTestData(val given: Any?, val expected: VariableRuntimeTypeEnum)

private data class RuntimeTypeFromTypeFormatTestData(
    val givenType: VariableValueTypeEnum,
    val givenFormat: VariableValueFormatEnum?,
    val expected: VariableRuntimeTypeEnum
)

private data class TypeFormatFromRuntimeTestData(
    val given: VariableRuntimeTypeEnum,
    val expected: Pair<VariableValueTypeEnum?, VariableValueFormatEnum?>?
)

private data class RuntimeTypeFromJsonElementTestData(
    val given: JsonElement,
    val expected: VariableRuntimeTypeEnum
)

private data class Foo(val a: Int)

class VariableValueTypesTest :
    FunSpec({
      val objectMapper = DefaultObjectMapper()
      val json = Json { serializersModule = variableSerializersCoreModule }

      withData(
          nameFn = {
            "RuntimeTypeAnyTest:${if (it.given != null) it.given::class.java.canonicalName else "null"}-${it.expected}"
          },
          listOf(
              RuntimeTypeAnyTestData(true, VariableRuntimeTypeEnum.BOOLEAN),
              RuntimeTypeAnyTestData(null, VariableRuntimeTypeEnum.NULL),
              RuntimeTypeAnyTestData("1", VariableRuntimeTypeEnum.STRING),
              RuntimeTypeAnyTestData(Int.MAX_VALUE, VariableRuntimeTypeEnum.INT),
              RuntimeTypeAnyTestData(Long.MAX_VALUE, VariableRuntimeTypeEnum.LONG),
              RuntimeTypeAnyTestData(Float.MAX_VALUE, VariableRuntimeTypeEnum.FLOAT),
              RuntimeTypeAnyTestData(Double.MAX_VALUE, VariableRuntimeTypeEnum.DOUBLE),
              RuntimeTypeAnyTestData(BigDecimal.ONE, VariableRuntimeTypeEnum.BIG_DECIMAL),
              RuntimeTypeAnyTestData(LocalDate.now(), VariableRuntimeTypeEnum.DATE),
              RuntimeTypeAnyTestData(OffsetDateTime.now(), VariableRuntimeTypeEnum.DATE_TIME),
              RuntimeTypeAnyTestData(LocalTime.now(), VariableRuntimeTypeEnum.TIME),
              RuntimeTypeAnyTestData(Period.ofDays(1), VariableRuntimeTypeEnum.PERIOD),
              RuntimeTypeAnyTestData(Duration.ofDays(1), VariableRuntimeTypeEnum.DURATION),
              RuntimeTypeAnyTestData(
                  objectMapper.valueToTree(Foo(1)), VariableRuntimeTypeEnum.OBJECT_NODE),
              RuntimeTypeAnyTestData(
                  objectMapper.valueToTree(listOf(Foo(1))), VariableRuntimeTypeEnum.ARRAY_NODE),
              RuntimeTypeAnyTestData(
                  objectMapper.valueToTree("1"), VariableRuntimeTypeEnum.JSON_NODE),
              RuntimeTypeAnyTestData(Foo(1), VariableRuntimeTypeEnum.UNKNOWN),
              RuntimeTypeAnyTestData(listOf(Foo(1)), VariableRuntimeTypeEnum.ARRAY),
              RuntimeTypeAnyTestData(IntArray(1), VariableRuntimeTypeEnum.ARRAY),
          )) { (given, expected) ->
            val actual = determineRuntimeType(given)
            actual shouldBe expected
          }

      withData(
          nameFn = {
            "TypeFormatFromRuntimeTest:${it.given}->${it.expected?.first}-${it.expected?.second}"
          },
          listOf(
              TypeFormatFromRuntimeTestData(
                  VariableRuntimeTypeEnum.BOOLEAN, Pair(VariableValueTypeEnum.BOOLEAN, null)),
              TypeFormatFromRuntimeTestData(
                  VariableRuntimeTypeEnum.STRING, Pair(VariableValueTypeEnum.STRING, null)),
              TypeFormatFromRuntimeTestData(
                  VariableRuntimeTypeEnum.INT, Pair(VariableValueTypeEnum.INT, null)),
              TypeFormatFromRuntimeTestData(
                  VariableRuntimeTypeEnum.LONG,
                  Pair(VariableValueTypeEnum.INT, VariableValueFormatEnum.LONG)),
              TypeFormatFromRuntimeTestData(
                  VariableRuntimeTypeEnum.FLOAT,
                  Pair(VariableValueTypeEnum.NUMBER, VariableValueFormatEnum.FLOAT)),
              TypeFormatFromRuntimeTestData(
                  VariableRuntimeTypeEnum.DOUBLE,
                  Pair(VariableValueTypeEnum.NUMBER, VariableValueFormatEnum.DOUBLE)),
              TypeFormatFromRuntimeTestData(
                  VariableRuntimeTypeEnum.BIG_DECIMAL,
                  Pair(VariableValueTypeEnum.NUMBER, VariableValueFormatEnum.BIG_DECIMAL)),
              TypeFormatFromRuntimeTestData(
                  VariableRuntimeTypeEnum.DATE,
                  Pair(VariableValueTypeEnum.STRING, VariableValueFormatEnum.DATE)),
              TypeFormatFromRuntimeTestData(
                  VariableRuntimeTypeEnum.DATE_TIME,
                  Pair(VariableValueTypeEnum.STRING, VariableValueFormatEnum.DATE_TIME)),
              TypeFormatFromRuntimeTestData(
                  VariableRuntimeTypeEnum.TIME,
                  Pair(VariableValueTypeEnum.STRING, VariableValueFormatEnum.TIME)),
              TypeFormatFromRuntimeTestData(
                  VariableRuntimeTypeEnum.PERIOD,
                  Pair(VariableValueTypeEnum.STRING, VariableValueFormatEnum.PERIOD)),
              TypeFormatFromRuntimeTestData(
                  VariableRuntimeTypeEnum.DURATION,
                  Pair(VariableValueTypeEnum.STRING, VariableValueFormatEnum.DURATION)),
              TypeFormatFromRuntimeTestData(
                  VariableRuntimeTypeEnum.OBJECT_NODE,
                  Pair(VariableValueTypeEnum.OBJECT, VariableValueFormatEnum.JSON)),
              TypeFormatFromRuntimeTestData(
                  VariableRuntimeTypeEnum.ARRAY_NODE,
                  Pair(VariableValueTypeEnum.ARRAY, VariableValueFormatEnum.JSON)),
              TypeFormatFromRuntimeTestData(
                  VariableRuntimeTypeEnum.ARRAY, Pair(VariableValueTypeEnum.ARRAY, null)),
              TypeFormatFromRuntimeTestData(
                  VariableRuntimeTypeEnum.JSON_NODE, Pair(null, VariableValueFormatEnum.JSON)),
              TypeFormatFromRuntimeTestData(VariableRuntimeTypeEnum.UNKNOWN, null),
              TypeFormatFromRuntimeTestData(VariableRuntimeTypeEnum.NULL, null),
          )) { (given, expected) ->
            val actual = typeAndFormatFromRuntimeType(given)
            actual shouldBe expected
          }

      withData(
          nameFn = {
            "RuntimeTypeFromTypeFormatTest:${it.givenType}-${it.givenFormat}-${it.expected}"
          },
          listOf(
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.BOOLEAN, null, VariableRuntimeTypeEnum.BOOLEAN),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.BOOLEAN,
                  VariableValueFormatEnum.JSON,
                  VariableRuntimeTypeEnum.JSON_NODE),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.BOOLEAN,
                  VariableValueFormatEnum.DOUBLE,
                  VariableRuntimeTypeEnum.BOOLEAN),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.INT,
                  VariableValueFormatEnum.LONG,
                  VariableRuntimeTypeEnum.LONG),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.INT,
                  VariableValueFormatEnum.JSON,
                  VariableRuntimeTypeEnum.JSON_NODE),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.INT,
                  VariableValueFormatEnum.DOUBLE,
                  VariableRuntimeTypeEnum.INT),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.INT, null, VariableRuntimeTypeEnum.INT),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.STRING,
                  VariableValueFormatEnum.DATE,
                  VariableRuntimeTypeEnum.DATE),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.STRING,
                  VariableValueFormatEnum.DATE_TIME,
                  VariableRuntimeTypeEnum.DATE_TIME),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.STRING,
                  VariableValueFormatEnum.TIME,
                  VariableRuntimeTypeEnum.TIME),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.STRING,
                  VariableValueFormatEnum.PERIOD,
                  VariableRuntimeTypeEnum.PERIOD),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.STRING,
                  VariableValueFormatEnum.DURATION,
                  VariableRuntimeTypeEnum.DURATION),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.STRING,
                  VariableValueFormatEnum.JSON,
                  VariableRuntimeTypeEnum.JSON_NODE),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.STRING, null, VariableRuntimeTypeEnum.STRING),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.STRING,
                  VariableValueFormatEnum.DOUBLE,
                  VariableRuntimeTypeEnum.STRING),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.NUMBER,
                  VariableValueFormatEnum.DOUBLE,
                  VariableRuntimeTypeEnum.DOUBLE),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.NUMBER,
                  VariableValueFormatEnum.FLOAT,
                  VariableRuntimeTypeEnum.FLOAT),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.NUMBER,
                  VariableValueFormatEnum.BIG_DECIMAL,
                  VariableRuntimeTypeEnum.BIG_DECIMAL),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.NUMBER,
                  VariableValueFormatEnum.JSON,
                  VariableRuntimeTypeEnum.JSON_NODE),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.NUMBER, null, VariableRuntimeTypeEnum.DOUBLE),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.NUMBER,
                  VariableValueFormatEnum.DATE_TIME,
                  VariableRuntimeTypeEnum.DOUBLE),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.ARRAY,
                  VariableValueFormatEnum.JSON,
                  VariableRuntimeTypeEnum.ARRAY_NODE),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.ARRAY, null, VariableRuntimeTypeEnum.ARRAY),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.ARRAY,
                  VariableValueFormatEnum.DATE_TIME,
                  VariableRuntimeTypeEnum.ARRAY),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.OBJECT,
                  VariableValueFormatEnum.JSON,
                  VariableRuntimeTypeEnum.OBJECT_NODE),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.OBJECT, null, VariableRuntimeTypeEnum.UNKNOWN),
              RuntimeTypeFromTypeFormatTestData(
                  VariableValueTypeEnum.OBJECT,
                  VariableValueFormatEnum.DURATION,
                  VariableRuntimeTypeEnum.UNKNOWN),
          )) { (givenType, givenFormat, expected) ->
            val actual = runtimeTypeFromTypeAndFormat(givenType, givenFormat)
            actual shouldBe expected
          }

      withData(
          nameFn = { "RuntimeTypeFromJsonElementTest:${it.expected}" },
          listOf(
              RuntimeTypeFromJsonElementTestData(
                  json.parseToJsonElement("true"), VariableRuntimeTypeEnum.BOOLEAN),
              RuntimeTypeFromJsonElementTestData(
                  json.parseToJsonElement("${Int.MAX_VALUE}"), VariableRuntimeTypeEnum.INT),
              RuntimeTypeFromJsonElementTestData(
                  json.parseToJsonElement("${Double.MAX_VALUE}"), VariableRuntimeTypeEnum.DOUBLE),
              RuntimeTypeFromJsonElementTestData(
                  json.parseToJsonElement("${Float.MAX_VALUE}"), VariableRuntimeTypeEnum.DOUBLE),
              RuntimeTypeFromJsonElementTestData(
                  json.parseToJsonElement("${Long.MAX_VALUE}"), VariableRuntimeTypeEnum.LONG),
              RuntimeTypeFromJsonElementTestData(
                  json.parseToJsonElement(""""${BigDecimal.ONE}""""),
                  VariableRuntimeTypeEnum.BIG_DECIMAL),
              RuntimeTypeFromJsonElementTestData(
                  json.parseToJsonElement(""""${LocalDate.now()}""""),
                  VariableRuntimeTypeEnum.DATE),
              RuntimeTypeFromJsonElementTestData(
                  json.parseToJsonElement(""""${OffsetDateTime.now()}""""),
                  VariableRuntimeTypeEnum.DATE_TIME),
              RuntimeTypeFromJsonElementTestData(
                  json.parseToJsonElement(""""${LocalTime.now()}""""),
                  VariableRuntimeTypeEnum.TIME),
              RuntimeTypeFromJsonElementTestData(
                  json.parseToJsonElement(""""${Period.ofDays(1)}""""),
                  VariableRuntimeTypeEnum.PERIOD),
              RuntimeTypeFromJsonElementTestData(
                  json.parseToJsonElement(""""${Duration.ofDays(1)}""""),
                  VariableRuntimeTypeEnum.DURATION),
              RuntimeTypeFromJsonElementTestData(
                  json.parseToJsonElement("""{"foo": "bar"}"""),
                  VariableRuntimeTypeEnum.OBJECT_NODE),
              RuntimeTypeFromJsonElementTestData(
                  json.parseToJsonElement("""[{"foo": "bar"}]"""),
                  VariableRuntimeTypeEnum.ARRAY_NODE),
              RuntimeTypeFromJsonElementTestData(
                  json.parseToJsonElement(""""${"Hello"}""""), VariableRuntimeTypeEnum.STRING),
              RuntimeTypeFromJsonElementTestData(JsonNull, VariableRuntimeTypeEnum.NULL),
          )) { (given, expected) ->
            val actual = runtimeTypeFromJsonElement(given)
            actual shouldBe expected
          }
    })
