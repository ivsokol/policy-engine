---
title: Policy Condition
description: Introduction to PolicyCondition entities
nav_order: 3
---
# Policy Condition
{: .no_toc }

<details markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

**PolicyCondition** represents an entity that applies defined operation over provided [Policy Variables](policy-variable.md) and returns a Boolean or null. Boolean is returned if input variables are valid and compliant with selected operation and if operation is run successfully.

It is defined by _**IPolicyCondition**_ interface, which has the following fields:

| field        | type    | cardinality | description                                                                                                                                                                              | 
|--------------|---------|-------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| negateResult | Boolean | optional    | Flag that indicates if the result of the Condition should be negated (true becomes false and false becomes true). If result is null, negation is not applied. Default value is **false** |

IPolicyCondition interface implements [IManaged](managed-vs-embedded.md#imanaged-interface) interface.
The IPolicyCondition interface has three implementations: 
- [PolicyConditionAtomic](#policyconditionatomic)
- [PolicyConditionComposite](#policyconditioncomposite)
- [PolicyConditionDefault](#policyconditiondefault)

## PolicyConditionAtomic

This entity represents atomic Policy condition in the Policy engine. It runs selected operation over provided input variables and returns result

| field                 | type                                                         | cardinality | description                                                                                                                                                                                              | 
|-----------------------|--------------------------------------------------------------|-------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| operation             | [OperationEnum](#supported-policyconditionatomic-operations) | mandatory   | Logical operation in condition                                                                                                                                                                           |
| args                  | ```IPolicyVariableRefOrValue[]```                            | mandatory   | List of input [PolicyVariables](policy-variable.md)                                                                                                                                                      |
| stringIgnoreCase      | Boolean                                                      | optional    | Flag that determines should String operations ignore case. Default value is **false**                                                                                                                    |
| fieldsStrictCheck     | Boolean                                                      | optional    | Flag that determines should Object operations* do a strict field check by setting `CompareMode.JSON_OBJECT_NON_EXTENSIBLE` and `CompareMode.JSON_ARRAY_NON_EXTENSIBLE` flags. Default value is **false** |
| arrayOrderStrictCheck | Boolean                                                      | optional    | should Array operations* do a strict item position check by setting `CompareMode.JSON_ARRAY_STRICT_ORDER` flag. Default value is **false**                                                               |

* Object and Array JSON operations are executed using [JSON Compare](https://github.com/fslev/json-compare) library

`check()` method of PolicyConditionAtomic entity returns Boolean result or null. If PolicyConditionAtomic is managed, then result of operation will be cached. If any input parameter is not resolved, method will return null as a result. If any input parameter is not compliant with selected operation, method will return null as a result. 

### Supported PolicyConditionAtomic operations

| operation            | first parameter                                                                                                                                                                                                               | second parameter                                                                                                                                                                                                              | description                                                                                                                                                                                                                                                                                                                                                                                                    | 
|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Equals**           | * String <br/>* Date <br/>* DateTime <br/>* Time <br/>* Period <br/>* Duration <br/>* Long <br/>* Int <br/>* Double <br/>* Float <br/>* BigDecimal <br/>* Boolean <br/>* JSON <br/>* JsonArray <br/>* JsonObject <br/>* Array | * String <br/>* Date <br/>* DateTime <br/>* Time <br/>* Period <br/>* Duration <br/>* Long <br/>* Int <br/>* Double <br/>* Float <br/>* BigDecimal <br/>* Boolean <br/>* JSON <br/>* JsonArray <br/>* JsonObject <br/>* Array | Compares parameters to check if first parameter is equal to the second parameter. [Smart cast](#smart-casting) is applied on the second parameter. Unknown and null runtime values are not supported. JSON, JsonObject, JsonArray and Array (cast to JsonArray using options provided Jackson mapper `valueToTree()` method) are compared using [JSON Compare](https://github.com/fslev/json-compare) library. |
| **GreaterThan**      | * String <br/>* Date <br/>* DateTime <br/>* Time <br/>* Period <br/>* Duration <br/>* Long <br/>* Int <br/>* Double <br/>* Float <br/>* BigDecimal                                                                            | * String <br/>* Date <br/>* DateTime <br/>* Time <br/>* Period <br/>* Duration <br/>* Long <br/>* Int <br/>* Double <br/>* Float <br/>* BigDecimal                                                                            | Compares parameters to check if first parameter is greater than the second parameter. [Smart cast](#smart-casting) is applied on the second parameter.                                                                                                                                                                                                                                                         |
| **GreaterThanEqual** | * String <br/>* Date <br/>* DateTime <br/>* Time <br/>* Period <br/>* Duration <br/>* Long <br/>* Int <br/>* Double <br/>* Float <br/>* BigDecimal                                                                            | * String <br/>* Date <br/>* DateTime <br/>* Time <br/>* Period <br/>* Duration <br/>* Long <br/>* Int <br/>* Double <br/>* Float <br/>* BigDecimal                                                                            | Compares parameters to check if first parameter is greater or equal to the second parameter. [Smart cast](#smart-casting) is applied on the second parameter.                                                                                                                                                                                                                                                  |
| **LessThan**         | * String <br/>* Date <br/>* DateTime <br/>* Time <br/>* Period <br/>* Duration <br/>* Long <br/>* Int <br/>* Double <br/>* Float <br/>* BigDecimal                                                                            | * String <br/>* Date <br/>* DateTime <br/>* Time <br/>* Period <br/>* Duration <br/>* Long <br/>* Int <br/>* Double <br/>* Float <br/>* BigDecimal                                                                            | Compares parameters to check if first parameter is lesser than the second parameter. [Smart cast](#smart-casting) is applied on the second parameter.                                                                                                                                                                                                                                                          |
| **LessThanEqual**    | * String <br/>* Date <br/>* DateTime <br/>* Time <br/>* Period <br/>* Duration <br/>* Long <br/>* Int <br/>* Double <br/>* Float <br/>* BigDecimal                                                                            | * String <br/>* Date <br/>* DateTime <br/>* Time <br/>* Period <br/>* Duration <br/>* Long <br/>* Int <br/>* Double <br/>* Float <br/>* BigDecimal                                                                            | Compares parameters to check if first parameter is lesser or equal to the second parameter. [Smart cast](#smart-casting) is applied on the second parameter.                                                                                                                                                                                                                                                   |
| **IsNull**           | * Any                                                                                                                                                                                                                         | X                                                                                                                                                                                                                             | Checks if runtime value is null                                                                                                                                                                                                                                                                                                                                                                                |
| **IsNotNull**        | * Any                                                                                                                                                                                                                         | X                                                                                                                                                                                                                             | Checks if runtime value is not null                                                                                                                                                                                                                                                                                                                                                                            |
| **IsBlank**          | * String                                                                                                                                                                                                                      | X                                                                                                                                                                                                                             | Checks if String is blank                                                                                                                                                                                                                                                                                                                                                                                      |
| **IsNotBlank**       | * String                                                                                                                                                                                                                      | X                                                                                                                                                                                                                             | Checks if String is not blank                                                                                                                                                                                                                                                                                                                                                                                  |
| **IsEmpty**          | * String <br/>* JSON <br/>* JsonArray <br/>* Array                                                                                                                                                                            | X                                                                                                                                                                                                                             | Checks if String or Array is empty                                                                                                                                                                                                                                                                                                                                                                             |
| **IsNotEmpty**       | * String <br/>* JSON <br/>* JsonArray <br/>* Array                                                                                                                                                                            | X                                                                                                                                                                                                                             | Checks if String or Array is not empty                                                                                                                                                                                                                                                                                                                                                                         |
| **StartsWith**       | * String <br/>* JsonArray <br/>* Array                                                                                                                                                                                        | * Any                                                                                                                                                                                                                         | Checks if String or Array starts with specific string or item. In case of an array, first item is compared with second argument using _Equals_ operation. [Smart cast](#smart-casting) to String is applied on the second parameter if first parameter is String.                                                                                                                                              |
| **EndsWith**         | * String <br/>* JsonArray <br/>* Array                                                                                                                                                                                        | * Any                                                                                                                                                                                                                         | Checks if String or Array ends with specific string or item. In case of an array, last item is compared with second argument using _Equals_ operation. [Smart cast](#smart-casting) to String is applied on the second parameter if first parameter is String.                                                                                                                                                 |
| **Contains**         | * String <br/>* JsonArray <br/>* Array                                                                                                                                                                                        | * Any                                                                                                                                                                                                                         | Checks if String or Array contains specific string or item. In case of an array, `any()` kotlin method is applied to check if there is at least one item that is _Equal_ to second argument. [Smart cast](#smart-casting) to String is applied on the second parameter if first parameter is String.                                                                                                           |
| **IsIn**             | * Any                                                                                                                                                                                                                         | * String <br/>* JsonArray <br/>* Array                                                                                                                                                                                        | Checks if String or item is in another string or array. This method under the hood invokes Contains method logic, but with switched parameters. In case of an array, `any()` kotlin method is applied to check if there is at least one item that is _Equal_ to first argument. [Smart cast](#smart-casting) to String is applied on the first parameter if second parameter is String.                        |
| **IsPositive**       | * Int <br/>* Long <br/>* Double <br/>* Float <br/>* BigDecimal <br/>* Period <br/>* Duration                                                                                                                                  | X                                                                                                                                                                                                                             | Checks if numerical or duration value is positive.                                                                                                                                                                                                                                                                                                                                                             |
| **IsNegative**       | * Int <br/>* Long <br/>* Double <br/>* Float <br/>* BigDecimal <br/>* Period <br/>* Duration                                                                                                                                  | X                                                                                                                                                                                                                             | Checks if numerical or duration value is negative.                                                                                                                                                                                                                                                                                                                                                             |
| **IsZero**           | * Int <br/>* Long <br/>* Double <br/>* Float <br/>* BigDecimal <br/>* Period <br/>* Duration                                                                                                                                  | X                                                                                                                                                                                                                             | Checks if numerical or duration value is equal to 0.                                                                                                                                                                                                                                                                                                                                                           |
| **IsFuture**         | * DateTime <br/>* Date <br/>* Time                                                                                                                                                                                            | X                                                                                                                                                                                                                             | Checks if temporal value is in the future. Comparison is done with currentDateTime, currentDate and currentTime [environment store](context.md#default-environment) values or from provided options clock parameter.                                                                                                                                                                                           |
| **IsPast**           | * DateTime <br/>* Date <br/>* Time                                                                                                                                                                                            | X                                                                                                                                                                                                                             | Checks if temporal value is in the past. Comparison is done with currentDateTime, currentDate and currentTime [environment store](context.md#default-environment) values or from provided options clock parameter.                                                                                                                                                                                             |
| **RegexpMatch**      | * Any                                                                                                                                                                                                                         | * String                                                                                                                                                                                                                      | Checks if first parameter is matching Regex provided in second parameter. First value is cast to String.                                                                                                                                                                                                                                                                                                       |
| **SchemaMatch**      | * Any                                                                                                                                                                                                                         | * String <br/>* JsonObject                                                                                                                                                                                                    | Checks if first parameter is matching JSONSchema provided in second parameter. First value is cast to String. Schema matching is executed using [Vert.x Json Schema](https://github.com/eclipse-vertx/vertx-json-schema) library                                                                                                                                                                               |
| **IsUnique**         | * JsonArray <br/>* Array                                                                                                                                                                                                      | X                                                                                                                                                                                                                             | Checks if Array has unique elements. Comparison is done by comparing size of provided Array and same Array with distinct parameters.                                                                                                                                                                                                                                                                           |
| **HasKey**           | * JsonObject                                                                                                                                                                                                                  | * String                                                                                                                                                                                                                      | Checks Object provided in first argument contains key provided in second argument.                                                                                                                                                                                                                                                                                                                             |

### Examples

**Minimal PolicyConditionAtomic**

```json
{
    "operation": "GreaterThan",
    "args": [
        {
            "id": "polVar1",
            "refType": "PolicyVariableRef"
        },
        {
            "type": "int",
            "value": 42
        }
    ]
}
```

**Minimal PolicyConditionAtomic with unary operation**

```json
{
    "operation": "IsBlank",
    "args": [
        {
            "id": "polVar1",
            "refType": "PolicyVariableRef"
        }
    ]
}
```

**PolicyConditionAtomic with params**

```json
{
    "operation": "Equals",
    "args": [
        {
            "id": "polVar1",
            "refType": "PolicyVariableRef"
        },
        {
            "type": "string",
            "value": "fooBar"
        }
    ],
    "stringIgnoreCase": true
}
```

**Managed PolicyConditionAtomic**

```json
{
    "id": "polCond1",
    "version": "1.2.3",
    "description": "This is a managed PolicyConditionAtomic",
    "labels": [
        "label1"
    ],
    "operation": "Equals",
    "args": [
        {
            "id": "polVar1",
            "refType": "PolicyVariableRef"
        },
        {
            "type": "string",
            "value": "fooBar"
        }
    ],
    "stringIgnoreCase": true
}
```

### Smart casting

Engine can cast second argument to the type of first argument if they are compliant with following rules:

| target type | sourceType                                                                                                                                                                                                                                                                                                                                                                                                                        | 
|-------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| String      | * Boolean <br/>* Date - using options.dateFormatter <br/>* DateTime - using options.dateTimeFormatter <br/>* Time - using options.timeFormatter <br/>* Period <br/>* Duration <br/>* Long <br/>* Int <br/>* Double <br/>* Float <br/>* BigDecimal <br/>* JSON - using options.objectMapper <br/>* JsonObject - using options.objectMapper <br/>* JsonArray - using options.objectMapper <br/>* Array - using options.objectMapper | 
| Date        | * String - using options.dateFormatter <br/>* DateTime - using kotlin `toLocaldate()` method <br/>* JSON - using options.objectMapper and then options.dateFormatter                                                                                                                                                                                                                                                              | 
| DateTime    | * String - using options.dateTimeFormatter <br/>* Date - using kotlin `OffsetDateTime().atStartOfDay().atZone(options.zoneId)` method <br/>* JSON - using options.objectMapper and then options.dateTimeFormatter                                                                                                                                                                                                                 | 
| Time        | * String - using options.timeFormatter <br/>* DateTime - using kotlin `OffsetDateTime().toLocalTime()` method <br/>* JSON - using options.objectMapper and then options.timeFormatter                                                                                                                                                                                                                                             | 
| Period      | * String - using `Period.parse()` <br/>* Duration - using `Period.ofDays($duration.toDays().toInt())` <br/>* JSON - using options.objectMapper and then `Period.parse()`                                                                                                                                                                                                                                                          | 
| Duration    | * String - using `Duration.parse()` <br/>* JSON - using options.objectMapper and then `Duration.parse()`                                                                                                                                                                                                                                                                                                                          | 
| Long        | * String <br/>* Int <br/>* Double <br/>* Float <br/>* BigDecimal <br/>* JSON - using options.objectMapper                                                                                                                                                                                                                                                                                                                         |
| Int         | * String <br/>* Long <br/>* Double <br/>* Float <br/>* BigDecimal <br/>* JSON - using options.objectMapper                                                                                                                                                                                                                                                                                                                        |
| Double      | * String <br/>* Long <br/>* Int <br/>* Float <br/>* BigDecimal <br/>* JSON - using options.objectMapper                                                                                                                                                                                                                                                                                                                           |
| Float       | * String <br/>* Long <br/>* Int <br/>* Double <br/>* BigDecimal <br/>* JSON - using options.objectMapper                                                                                                                                                                                                                                                                                                                          |
| BigDecimal  | * String <br/>* Long <br/>* Int <br/>* Double <br/>* Float <br/>* JSON - using options.objectMapper                                                                                                                                                                                                                                                                                                                               |
| Boolean     | * String <br/>* JSON - using options.objectMapper                                                                                                                                                                                                                                                                                                                                                                                 | 
| JSON        | * JsonObject <br/>* JsonArray <br/>* Null - cast to `NullNode()`                                                                                                                                                                                                                                                                                                                                                                  | 
| JsonObject  | * String - only if it is valid JSON Object <br/>* JSON - only if it is valid JSON Object                                                                                                                                                                                                                                                                                                                                          | 
| JsonArray   | * String - only if it is valid JSON Array <br/>* JSON - only if it is valid JSON Array <br/>* Array - only if it is valid JSON Array                                                                                                                                                                                                                                                                                              | 

## PolicyConditionComposite

This entity represents combination of other Policy Conditions (atomic, composite or references). It calculates result based on condition combination logic.

| field                     | type                                                        | cardinality | description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   | 
|---------------------------|-------------------------------------------------------------|-------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| conditionCombinationLogic | [ConditionCombinationLogicEnum](#conditioncombinationlogic) | mandatory   | Combination operation in composite PolicyCondition                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| conditions                | ```IPolicyConditionRefOrValue[]```                          | mandatory   | List of PolicyConditions (embedded), PolicyConditionRefs (managed) or DefaultPolicyConditions                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| minimumConditions         | Int                                                         | optional*   | Minimal number of conditions in `nOf` ConditionCombinationlogic that must resolve to `true`. Mandatory parameter only in that case, ignored in other cases.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| optimizeNOfRun            | Boolean                                                     | optional    | should `nOf` calculation run through all conditions in case when result `true` cannot be achieved. <br/>For example, if there are 5 conditions in a list, minimumConditions is set to 3 and first three conditions resolve to combination of 2 false and 1 null result. <br/>Result `true` thus cannot be achieved, even if other conditions resolve to `true`. Optimized run will stop on 3rd execution and return `null`. Non optimized run will check all 5 conditions and return either `null` or `false`, depending on all results. <br/>In some cases, optimized run could return `null` instead of `false`. Default value is **false** |
| strictCheck               | Boolean                                                     | optional    | should `allOf` and `anyOf` return null if there is at least one null result and no positive results. Default value is **true**.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |

`check()` method of PolicyConditionComposite entity returns Boolean result or null. If PolicyConditionComposite is managed, then result of operation will be cached. List of conditions must contain at least one condition. In case of `not` logic, number of conditions must be exactly 1.

### ConditionCombinationLogic

##### not

It runs first (and only) condition from the conditions list and negates the result (if result is not null). Same effect can be achieved by setting `negateResult` flag to `true` in the underlying condition, but this logic is added in scenarios when that is not possible.

##### anyOf

Returns `true` if any of the conditions in the conditions list resolves to `true`. It will run in optimized manner, when there is first `true` result, it will stop processing other conditions. Conditions are processed sequentially, in order they are listed. 

If there is no `true` result, then final result depend on strictCheck flag.
If strictCheck flag is set to true, and there is at least one `null` result, final result will be `null`. If that flag is not set, final result will be `false`.
If all conditions resolved to `false`, final result will be `false` and strictCheck flag is ignored.

##### allOf

Returns `true` if all the conditions in the conditions list resolves to `true`. It will run in optimized manner, when there is first `false` result, it will stop processing other conditions and return `false`. Conditions are processed sequentially, in order they are listed. 

If there is no `false` result, then final result depend on strictCheck flag.
If strictCheck flag is set to true, and there is at least one `null` result, final result will be `null`. If that flag is not set, final result will be `true`.
If all conditions resolved to `true`, final result will be `true` and strictCheck flag is ignored.

##### nOf

Returns `true` if at least `minimumConditions` from the conditions list resolves to `true`. It will run in optimized manner, when count of positive result reaches `minimumConditions` value, it will stop processing other conditions and return `true`. Conditions are processed sequentially, in order they are listed. Number of conditions in the conditions list must be equal or higher than minimumConditions value.

If there are more `false` results than `conditions.size() - minimumConditions`, final result will be `false`.
If there are more `null` results than `conditions.size() - minimumConditions`, final result will be `null`.

If _optimizeNOfRun_ flag is set to true, and sum of `null` and `false` results is higher than `conditions.size() - minimumConditions`, final result will be `null` and processing will stop. If that flag is not set, processing will continue and final result will depend on upper rules.

### Examples

**PolicyConditionComposite not logic**

```json
{
    "conditionCombinationLogic": "not",
    "conditions": [
        {
            "operation": "IsBlank",
            "args": [
                {
                    "id": "polVar1",
                    "refType": "PolicyVariableRef"
                }
            ]
        }
    ]
}
```

**PolicyConditionComposite allOf logic**

```json
{
    "conditionCombinationLogic": "allOf",
    "conditions": [
        {
            "operation": "IsBlank",
            "args": [
                {
                    "id": "polVar1",
                    "refType": "PolicyVariableRef"
                }
            ]
        },
        {
            "id": "polCond1",
            "refType": "PolicyConditionRef"
        }
    ],
    "strictCheck": false
}
```

**PolicyConditionComposite anyOf logic**

```json
{
    "conditionCombinationLogic": "anyOf",
    "conditions": [
        {
            "operation": "IsBlank",
            "args": [
                {
                    "id": "polVar1",
                    "refType": "PolicyVariableRef"
                }
            ]
        },
        {
            "id": "polCond1",
            "refType": "PolicyConditionRef"
        },
        {
            "default": true
        }
    ]
}
```

**PolicyConditionComposite nOf logic**

```json
{
    "conditionCombinationLogic": "nOf",
    "conditions": [
        {
            "operation": "IsBlank",
            "args": [
                {
                    "id": "polVar1",
                    "refType": "PolicyVariableRef"
                }
            ]
        },
        {
            "operation": "IsEmpty",
            "args": [
                {
                    "id": "polVar2",
                    "refType": "PolicyVariableRef"
                }
            ]
        },
        {
            "id": "polCond1",
            "refType": "PolicyConditionRef"
        }
    ],
    "minimumConditions": 2,
    "optimizeNOfRun": false
}
```

**Managed PolicyConditionComposite**

```json
{
    "id": "polCond1",
    "version": "1.2.3",
    "description": "This is a managed PolicyConditionComposite",
    "labels": [
        "label1"
    ],
    "conditionCombinationLogic": "not",
    "conditions": [
        {
            "operation": "IsBlank",
            "args": [
                {
                    "id": "polVar1",
                    "refType": "PolicyVariableRef"
                }
            ]
        }
    ]
}
```

## PolicyConditionDefault

Represents a default policy condition that always returns a specified boolean value or null. It can be used as a fallback PolicyCondition or in tests. As result is static, it is not cached. It can be used as embedded or as a reference, where id in such reference is one of the following:
- `$true`
- `$false`
- `$null`


| field   | type    | cardinality | description                                                     | 
|---------|---------|-------------|-----------------------------------------------------------------|
| default | Boolean | optional    | Static Boolean result value or null. Default value is **null**. |

**Null PolicyConditionDefault**

```json
{
    "default": null
}
```

**True PolicyConditionDefault**

```json
{
    "default": true
}
```

**False PolicyConditionDefault**

```json
{
    "default": false
}
```

**PolicyConditionRef to PolicyConditionDefault true**

```json
{
    "id": "$true",
    "refType": "PolicyConditionRef"
}
```

## PolicyConditionRef

PolicyConditionRef is entity that references a PolicyCondition (atomic, composite or default). It contains following fields:

| field   | type   | cardinality | description                                                                                                          |
|---------|--------|-------------|----------------------------------------------------------------------------------------------------------------------|
| id      | String | mandatory   | id of the managed PolicyCondition                                                                                    |
| version | String | optional    | SemVer of the referenced PolicyCondition. If it is left blank, latest version of a PolicyCondition will be populated |
| refType | String | mandatory   | discriminator that is used when deserializing catalog from JSON file. It has constant value of `PolicyConditionRef`  |

## Policy constraints

Policy constraints are nothing more than PolicyConditions that are used to check if a [Policy](policy.md), [PolicyRelationship](policy.md#policyrelationship) and [PolicyActionRelationship](policy.md#policyactionrelationship) is applicable to a given context. 

If they are resolved to true:
- Policy is evaluated
- Child Policy in PolicySet is evaluated
- PolicyAction in Policy is executed

If they are resolved to false, Policy evaluation or PolicyAction execution is skipped.\
If they are resolved to null, appropriate flag is determining what should be the case. This is explained in details on [Policy](policy.md) page.