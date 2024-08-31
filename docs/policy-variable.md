---
title: Policy Variable
description: Description of static and dynamic Policy Variables
nav_order: 4
---
# Policy Variable
{: .no_toc }

<details markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>


**PolicyVariable** is an entity that represents a variable value within the Policy engine. 
It is defined by the _**IPolicyVariable**_ interface, which has the following key properties and methods:

| field          | type   | cardinality | description                                                                                                                      | 
|----------------|--------|-------------|----------------------------------------------------------------------------------------------------------------------------------|
| type           | String | optional    | PolicyVariable data type                                                                                                         |
| format         | String | optional    | PolicyVariable data format                                                                                                       |
| timeFormat     | String | optional    | `DateTimeFormatter` definition for Time variables. Overrides Context.options.timeFormatter definition for this Variable.         |
| dateFormat     | String | optional    | `DateTimeFormatter` definition for Date variables. Overrides Context.options.dateFormatter definition for this Variable.         |
| dateTimeFormat | String | optional    | `DateTimeFormatter` definition for DateTime variables. Overrides Context.options.dateTimeFormatter definition for this Variable. |

IPolicyVariable interface implements [IManaged](managed-vs-embedded.md#imanaged-interface) interface.
The IPolicyVariable interface has two concrete implementations: [PolicyVariableStatic](#policyvariablestatic) and [PolicyVariableDynamic](#policyvariabledynamic).

### IPolicyVariable data types and formats

The IPolicyVariable interface can define following types and formats:

* **string**
  * date-time - ISO 8601 date-time format
  * date - ISO 8601 date format
  * time - ISO 8601 time format
  * period - ISO 8601 period format 
  * duration - ISO 8601 period format 
  * JSON - parses JSON as string
* **int**
  * long - number serialized as Java Long
* **number**
  * double - number serialized as Java Double
  * float - number serialized as Java Float
  * big-decimal - number serialized as Java BigDecimal
  * JSON - parses JSON as number
* **boolean**
  * JSON - parses JSON as boolean
* **object**
  * JSON - string serialized as JSON object
* **array**
  * JSON - string serialized as JSON array

### Variable parsing

When a variable is parsed, it is cast to one of the following runtime types:
- String
- Date
- DateTime
- Time
- Period
- Duration
- Integer
- Long
- Double
- Float
- BigDecimal
- Boolean
- JSON
- JsonObject
- JsonArray
- Array
- Null
- Unknown

Explicit runtime type is determined by the _type_ and _format_ properties of the PolicyVariable. This is **the fastest** approach to determine runtime type. If type or format are omitted from variable definition, engine will try to determine runtime type based on actual value. This can lead to unexpected results, so it is always recommended to define expected type and format properties. <br />
Variables can be additionally casted to a specific type as explained in [PolicyCondition](policy-condition.md#smart-casting) smart casting chapter.

## PolicyVariableStatic

This entity represents a PolicyVariable with a static value. Actual value is directly stored in the value property. Represents a static variable value that is usually used to compare with dynamic variable values or to set a specific result in data store.

| field  | type   | cardinality | description                | 
|--------|--------|-------------|----------------------------|
| value  | Any    | mandatory   | Static value of a variable |

`resolve()` method in PolicyVariableStatic returns the value of the variable. If the variable is managed and is found in the cache, the cached value is returned. Otherwise, the value is parsed and coerced to the specified type, if present. The parsed value is then cached and returned. If an exception occurs during parsing, a null is returned.

### Examples

**Minimal PolicyVariableStatic**

```json
{
    "value": 42
}
```

**Embedded PolicyVariableStatic with defined type and format**

```json
{
    "type": "string",
    "format": "date",
    "value": "2024-01-23"
}
```

**Managed PolicyVariableStatic with defined type and format**

```json
{
    "id": "polVal1",
    "version": "1.2.3",
    "description": "This is a managed PolicyVariableStatic",
    "labels": [
        "label1",
        "label2"
    ],
    "type": "string",
    "format": "duration",
    "value": "PT1H"
}
```

## PolicyVariableDynamic

This entity represents a PolicyVariable with a dynamic value that is resolved using one or more resolvers. The resolvers are defined by the resolvers property, which is a list of [PolicyVariableResolver](#policyvariableresolver) objects.

| field     | type                     | cardinality | description                                                                                                                                                                                                                         | 
|-----------|--------------------------|-------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| resolvers | PolicyVariableResolver[] | mandatory   | List of either PolicyVariableResolvers (embedded) or PolicyVariableResolverRefs (reference to managed PolicyVariableResolver). It can be combination of both. Resolvers are run sequentially, ordered by the position in this list. |

`resolve()` method in PolicyVariableDynamic returns the value of the variable. If the variable is managed and is found
in the cache, the cached value is returned. Otherwise, the value is resolved by calling the resolvers in the order they
are positioned in the list. The first resolved non-null value is then cached and returned. If an exception occurs during resolution, a null is returned.

### Examples

**Minimal PolicyVariableDynamic with embedded PolicyVariableResolver**

```json
{
    "resolvers": [
        {
            "key": "foo"
        }
    ]
}
```

**PolicyVariableDynamic with reference to managed PolicyVariableResolver**

```json
{
    "resolvers": [
        {
            "id": "polVarRes1",
            "version": "1.2.3",
            "refType": "PolicyVariableResolverRef"
        }
    ]
}
```

**Managed PolicyVariableDynamic with multiple resolvers**

```json
{
    "id": "polVal1",
    "version": "1.2.3",
    "description": "This is a managed PolicyVariableDynamic",
    "labels": [
        "label1",
        "label2"
    ],
    "resolvers": [
        {
            "id": "polVarRes1",
            "version": "1.2.3",
            "refType": "PolicyVariableResolverRef"
        },
        {
            "key": "duration"
        }
    ],
    "type": "string",
    "format": "duration"
}
```

## PolicyVariableResolver

The PolicyVariableResolver defines how the dynamic variable's value should be resolved. Every resolver contains following fields.

| field  | type                              | cardinality                                     | description                                                                                                     | 
|--------|-----------------------------------|-------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| source | [ContextStore](context.md#stores) | optional                                        | source store to use as an input in resolver (request, environment, subject, data). Default value is **request** |
| engine | String                            | optional                                        | engine to use in resolver (JMESPath, JQ, key). Default value is **key**                                         |
| key    | String                            | mandatory when engine is **key**                | source store key to use as a full filter for key engine or prefilter for other engines.                         |
| path   | String                            | mandatory when engine is **JQ** or **JMESPath** | parser instruction for JQ or JMESPath engine                                                                    |

### PolicyVariableResolver engines

Following engines are supported:
* key - key engine is used to resolve the value of a variable from a key in a context store. The key engine is used by
  default. This is the fastest engine, as it just pulls data from the stores, which are basically maps
* [JQ](https://jqlang.github.io/jq/) - JQ is used to resolve the value of a variable from a JSON document. Path field contains parsing instruction. Key field can be used to prefilter store content. Implementation engine that is used is [Java-JQ engine](https://github.com/arakelian/java-jq)
* [JMESPath](https://jmespath.org/) - JMESPath is used to resolve the value of a variable from a JSON document. Path field contains parsing instruction. Key field can be used to prefilter store content. Implementation engine that is used is [JMESPath-Java engine](https://github.com/burtcorp/jmespath-java)

### Examples

**Key PolicyVariableResolver**

```json
{
    "key": "foo"
}
```

**JQ PolicyVariableResolver**

```json
{
    "path": ".foo.a1.a2",
    "engine": "JQ"
}
```

**JQ PolicyVariableResolver with prefilter and subject store**

```json
{
    "source": "subject",
    "key": "foo",
    "path": ".a1.a2",
    "engine": "JQ"
}
```

**JMESPath PolicyVariableResolver**

```json
{
    "source": "subject",
    "key": "foo",
    "path": "a1.a2",
    "engine": "JMESPath"
}
```

**Managed PolicyVariableResolver**

```json
{
    "id": "polValRes1",
    "version": "1.2.3",
    "description": "This is a managed PolicyVariableResolver",
    "labels": [
        "label1",
        "label2"
    ],
    "source": "subject",
    "key": "foo",
    "path": "a1.a2",
    "engine": "JMESPath"
}
```

## PolicyVariableRef

PolicyVariableRef is entity that references a PolicyVariable (static or dynamic). It contains following fields:

| field   | type   | cardinality | description                                                                                                        |
|---------|--------|-------------|--------------------------------------------------------------------------------------------------------------------|
| id      | String | mandatory   | id of the managed PolicyVariable                                                                                   |
| version | String | optional    | SemVer of the referenced PolicyVariable. If it is left blank, latest version of a PolicyVariable will be populated |
| refType | String | mandatory   | discriminator that is used when deserializing catalog from JSON file. It has constant value of `PolicyVariableRef` |

## PolicyVariableResolverRef

PolicyVariableRef is entity that references a PolicyVariableResolver. It contains following fields:

| field   | type   | cardinality | description                                                                                                                        |
|---------|--------|-------------|------------------------------------------------------------------------------------------------------------------------------------|
| id      | String | mandatory   | id of the managed PolicyVariableResolver                                                                                           |
| version | String | optional    | SemVer of the referenced PolicyVariableResolver. If it is left blank, latest version of a PolicyVariableResolver will be populated |
| refType | String | mandatory   | discriminator that is used when deserializing catalog from JSON file. It has constant value of `PolicyVariableResolverRef`         |