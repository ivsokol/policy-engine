package io.github.ivsokol.poe.variable

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.*

class JsonExtensionsTest :
    StringSpec({
      "should return null for null input string" {
        val result = (null as String?).toJsonAny()
        result shouldBe null
      }

      "should return the string itself for blank input string" {
        val result = "   ".toJsonAny()
        result shouldBe "   "
      }

      "should return the string itself for empty input string" {
        val result = "".toJsonAny()
        result shouldBe ""
      }

      "should parse a valid JSON string to a corresponding Any value" {
        val jsonString =
            """{"key1": "value1", "key2": 2, "key3": true, "key4": null, "key5": [1, 2, 3]}"""
        val result = jsonString.toJsonAny() as Map<*, *>
        result["key1"] shouldBe "value1"
        result["key2"] shouldBe 2
        result["key3"] shouldBe true
        result["key4"] shouldBe null
        result["key5"] shouldBe listOf(1, 2, 3)
      }

      "should parse a JsonElement to a corresponding Any value" {
        val jsonElement =
            Json.parseToJsonElement(
                """{"key1": "value1", "key2": 2, "key3": true, "key4": null, "key5": [1, 2, 3]}""")
        val result = parseStringToJsonAny(jsonElement) as Map<*, *>
        result["key1"] shouldBe "value1"
        result["key2"] shouldBe 2
        result["key3"] shouldBe true
        result["key4"] shouldBe null
        result["key5"] shouldBe listOf(1, 2, 3)
      }

      "should determine the correct JsonElementKind and value for JsonPrimitive" {
        val jsonPrimitiveString = JsonPrimitive("test")
        val jsonPrimitiveInt = JsonPrimitive(123)
        val jsonPrimitiveLong = JsonPrimitive(1234567890123456789L)
        val jsonPrimitiveBoolean = JsonPrimitive(true)
        val jsonPrimitiveNumber = JsonPrimitive(-568.452)

        jsonPrimitiveString.kind() shouldBe Pair(JsonElementKind.STRING, "test")
        jsonPrimitiveInt.kind() shouldBe Pair(JsonElementKind.INT, 123)
        jsonPrimitiveBoolean.kind() shouldBe Pair(JsonElementKind.BOOLEAN, true)
        jsonPrimitiveNumber.kind() shouldBe Pair(JsonElementKind.NUMBER, -568.452)
        jsonPrimitiveLong.kind() shouldBe Pair(JsonElementKind.INT, 1234567890123456789L)
      }

      "should determine the correct JsonElementKind and value for JsonObject and JsonArray" {
        val jsonObject = JsonObject(mapOf("key" to JsonPrimitive("value")))
        val jsonArray = JsonArray(listOf(JsonPrimitive(1), JsonPrimitive(2), JsonPrimitive(3)))

        jsonObject.kind() shouldBe Pair(JsonElementKind.OBJECT, jsonObject)
        jsonArray.kind() shouldBe Pair(JsonElementKind.ARRAY, jsonArray)
      }

      "should handle nested JSON objects and arrays" {
        val jsonString =
            """
            {
                "key1": {
                    "nestedKey1": "nestedValue1",
                    "nestedKey2": [1, 2, {"deepNestedKey": "deepNestedValue"}]
                },
                "key2": [
                    {"arrayObjKey1": "arrayObjValue1"},
                    {"arrayObjKey2": 123}
                ]
            }
        """
        val result = jsonString.toJsonAny() as Map<*, *>
        val key1 = result["key1"] as Map<*, *>
        val nestedKey2 = key1["nestedKey2"] as List<*>
        val deepNested = nestedKey2[2] as Map<*, *>

        key1["nestedKey1"] shouldBe "nestedValue1"
        nestedKey2[0] shouldBe 1
        nestedKey2[1] shouldBe 2
        deepNested["deepNestedKey"] shouldBe "deepNestedValue"

        val key2 = result["key2"] as List<*>
        val arrayObj1 = key2[0] as Map<*, *>
        val arrayObj2 = key2[1] as Map<*, *>

        arrayObj1["arrayObjKey1"] shouldBe "arrayObjValue1"
        arrayObj2["arrayObjKey2"] shouldBe 123
      }

      "should handle complex JSON structures with mixed types" {
        val jsonString =
            """
            {
                "stringKey": "stringValue",
                "intKey": 42,
                "booleanKey": false,
                "nullKey": null,
                "emptyArrayKey": [],
                "emptyObjectKey": {},
                "arrayKey": [1, "two", 3.0, {"nestedArrayKey": "nestedValue", "nestedNullValue": null}],
                "objectKey": {
                    "nestedStringKey": "nestedStringValue",
                    "nestedArrayKey": [true, false, null]
                }
            }
        """
        val result = jsonString.toJsonAny() as Map<*, *>
        result["stringKey"] shouldBe "stringValue"
        result["intKey"] shouldBe 42
        result["booleanKey"] shouldBe false
        result["nullKey"] shouldBe null

        result["emptyArrayKey"] shouldBe emptyList<Any>()
        result["emptyObjectKey"] shouldBe emptyMap<Any, Any>()

        val arrayKey = result["arrayKey"] as List<*>
        arrayKey[0] shouldBe 1
        arrayKey[1] shouldBe "two"
        arrayKey[2] shouldBe 3.0
        val nestedArrayKey = arrayKey[3] as Map<*, *>
        nestedArrayKey["nestedArrayKey"] shouldBe "nestedValue"
        nestedArrayKey["nestedNullValue"] shouldBe null

        val objectKey = result["objectKey"] as Map<*, *>
        objectKey["nestedStringKey"] shouldBe "nestedStringValue"
        val nestedArray = objectKey["nestedArrayKey"] as List<*>
        nestedArray[0] shouldBe true
        nestedArray[1] shouldBe false
        nestedArray[2] shouldBe null
      }
    })
