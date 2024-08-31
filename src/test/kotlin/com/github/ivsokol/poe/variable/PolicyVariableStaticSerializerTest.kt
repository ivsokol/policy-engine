package com.github.ivsokol.poe.variable

import com.github.ivsokol.poe.SemVer
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PolicyVariableStaticSerializerTest :
    FunSpec({
      val json = Json {
        serializersModule = variableSerializersModule
        explicitNulls = false
      }

      test("should serialize minimal PolicyVariableStatic") {
        val given =
            PolicyVariableStatic(
                value = "value",
            )
        val expected = """{"value":"value","type":"string"}"""

        val actual = json.encodeToString(given)

        actual shouldEqualJson expected
      }

      test("should deserialize minimal PolicyVariableStatic") {
        val given = """{"value":"value"}"""
        val expected =
            PolicyVariableStatic(
                value = "value",
            )
        val actual: PolicyVariableStatic = json.decodeFromString(given)
        actual shouldBeEqual expected
      }

      test("should deserialize minimal PolicyVariableStatic of type number") {
        val given = """{"value":42}"""
        val expected = PolicyVariableStatic(value = 42)
        val actual: PolicyVariableStatic = json.decodeFromString(given)
        actual shouldBeEqual expected
      }

      test("should serialize PolicyVariableStatic with all fields") {
        val given =
            PolicyVariableStatic(
                id = "id",
                version = SemVer(1, 2, 3),
                description = "description",
                labels = listOf("label1", "label2"),
                value = LocalDate.parse("2024-01-23"),
                type = VariableValueTypeEnum.STRING,
                format = VariableValueFormatEnum.DATE,
                dateFormat = "yyyy-MM-dd",
            )
        val expected =
            """{"id": "id","version": "1.2.3","description": "description","labels": ["label1","label2"],"value": "2024-01-23","type": "string","format": "date", "dateFormat": "yyyy-MM-dd"}"""
        val actual = json.encodeToString(given)
        actual shouldEqualJson expected
      }

      test("should deserialize PolicyVariableStatic with all fields") {
        val expected =
            PolicyVariableStatic(
                id = "id",
                version = SemVer(1, 2, 3),
                description = "description",
                labels = listOf("label1", "label2"),
                value = "2024-01-23",
                type = VariableValueTypeEnum.STRING,
                format = VariableValueFormatEnum.DATE,
                dateFormat = "yyyy-MM-dd",
            )
        val given =
            """{"id": "id","version": "1.2.3","description": "description","labels": ["label1","label2"],"value": "2024-01-23","type": "string","format": "date", "dateFormat": "yyyy-MM-dd"}"""
        val actual = json.decodeFromString<PolicyVariableStatic>(given)
        actual shouldBe expected
      }

      test("should serialize PolicyVariableStatic with time field") {
        val given =
            PolicyVariableStatic(
                value = LocalTime.parse("09:00:01"),
                type = VariableValueTypeEnum.STRING,
                format = VariableValueFormatEnum.TIME,
                timeFormat = "HH:mm:ss")
        val expected =
            """{"value": "09:00:01","type": "string","format": "time", "timeFormat": "HH:mm:ss"}"""
        val actual = json.encodeToString(given)
        actual shouldEqualJson expected
      }
      test("should deserialize PolicyVariableStatic with time field") {
        val given =
            """{"value": "09:00:01","type": "string","format": "time", "timeFormat": "HH:mm:ss"}"""
        val expected =
            PolicyVariableStatic(
                value = "09:00:01",
                type = VariableValueTypeEnum.STRING,
                format = VariableValueFormatEnum.TIME,
                timeFormat = "HH:mm:ss")
        val actual = json.decodeFromString<PolicyVariableStatic>(given)
        actual shouldBe expected
      }

      test("should serialize PolicyVariableStatic with dateTime field") {
        val given =
            PolicyVariableStatic(
                value = OffsetDateTime.parse("2024-01-23T09:00:01+01:00"),
                type = VariableValueTypeEnum.STRING,
                format = VariableValueFormatEnum.DATE_TIME,
                dateTimeFormat = "yyyy-MM-dd HH:mm:ss")
        val expected =
            """{"value": "2024-01-23T09:00:01+01:00","type": "string","format": "date-time", "dateTimeFormat": "yyyy-MM-dd HH:mm:ss"}"""
        val actual = json.encodeToString(given)
        actual shouldEqualJson expected
      }

      test("should deserialize PolicyVariableStatic with dateTime field") {
        val given =
            """{"value": "2024-01-23T09:00:01+01:00","type": "string","format": "date-time", "dateTimeFormat": "yyyy-MM-dd HH:mm:ss"}"""
        val expected =
            PolicyVariableStatic(
                value = "2024-01-23T09:00:01+01:00",
                type = VariableValueTypeEnum.STRING,
                format = VariableValueFormatEnum.DATE_TIME,
                dateTimeFormat = "yyyy-MM-dd HH:mm:ss")
        val actual = json.decodeFromString<PolicyVariableStatic>(given)
        actual shouldBe expected
      }

      test("deserialization should fail if timeFormatter used without time format") {
        val given =
            """{"value": "2023-10-10","type": "string","format": "date", "timeFormat": "HH:mm:ss"}"""
        val exception =
            shouldThrow<IllegalStateException> {
              json.decodeFromString<PolicyVariableStatic>(given)
            }
        exception.message shouldContain "format must be 'time' when timeFormat is populated"
      }

      test("deserialization should fail if dateFormatter used without date format") {
        val given =
            """{"value": "09:00:01","type": "string","format": "time", "dateFormat": "yyyy-MM-dd"}"""
        val exception =
            shouldThrow<IllegalStateException> {
              json.decodeFromString<PolicyVariableStatic>(given)
            }
        exception.message shouldContain "format must be 'date' when dateFormat is populated"
      }

      test("deserialization should fail if dateTimeFormatter used without dateTime format") {
        val given =
            """{"value": "09:00:01","type": "string","format": "time", "dateTimeFormat": "yyyy-MM-dd HH:mm:ss"}"""
        val exception =
            shouldThrow<IllegalStateException> {
              json.decodeFromString<PolicyVariableStatic>(given)
            }
        exception.message shouldContain
            "format must be 'date-time' when dateTimeFormat is populated"
      }

      test("deserialization should fail if timeFormat is not used with string") {
        val given = """{"value": 22,"type": "int", "timeFormat": "HH"}"""
        val exception =
            shouldThrow<IllegalStateException> {
              json.decodeFromString<PolicyVariableStatic>(given)
            }
        exception.message shouldContain "type must be 'string' when timeFormat is populated"
      }

      test("deserialization should fail if dateFormat is not used with string") {
        val given = """{"value": 22,"type": "int", "dateFormat": "yyyy-MM-dd"}"""
        val exception =
            shouldThrow<IllegalStateException> {
              json.decodeFromString<PolicyVariableStatic>(given)
            }
        exception.message shouldContain "type must be 'string' when dateFormat is populated"
      }

      test("deserialization should fail if dateTimeFormat is not used with string") {
        val given = """{"value": 22,"type": "int", "dateTimeFormat": "yyyy-MM-dd"}"""
        val exception =
            shouldThrow<IllegalStateException> {
              json.decodeFromString<PolicyVariableStatic>(given)
            }
        exception.message shouldContain "type must be 'string' when dateTimeFormat is populated"
      }
    })
