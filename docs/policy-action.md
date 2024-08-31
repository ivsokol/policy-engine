---
title: Policy Action
description: Introduction to PolicyAction entities
nav_order: 5
---
# Policy Action
{: .no_toc }

<details markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

**PolicyAction** represents an entity that applies side effect to the Policy evaluation that is realized through the modification of the [Context data store](context.md#stores).

It is defined by _**IPolicyAction**_ interface, which has the following fields:

| field | type   | cardinality | description                                                                                                                     | 
|-------|--------|-------------|---------------------------------------------------------------------------------------------------------------------------------|
| type  | String | mandatory   | Determines type of PolicyAction. Possible values are:<br/>* **save** <br/>* **clear** <br/>* **jsonMerge** <br/>* **jsonPatch** |

IPolicyAction interface implements [IManaged](managed-vs-embedded.md#imanaged-interface) interface.
The IPolicyAction interface has four implementations:
- [PolicyActionSave](#policyactionsave)
- [PolicyActionClear](#policyactionclear)
- [PolicyActionJsonMerge](#policyactionjsonmerge)
- [PolicyActionJsonPatch](#policyactionjsonpatch)

## PolicyActionSave

This entity represents an action that saves a value to the Context data store. 

| field             | type                            | cardinality | description                                                                                                                                                                     | 
|-------------------|---------------------------------|-------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| key               | String                          | mandatory   | The key to use when saving the value in the data store.                                                                                                                         |
| value             | ```IPolicyVariableRefOrValue``` | mandatory   | Definition of a [PolicyVariable](policy-variable.md). It is used as a source of a value that will be stored in the provided key                                                 |
| failOnMissingKey  | Boolean                         | optional    | Flag that determines should key already be defined in Context data store. Used for purposes when PolicyAction is strictly overriding existing value. Default value is **false** | 
| failOnExistingKey | Boolean                         | optional    | Flag that determines if key shouldn't be defined in Context data store. Used for purposes when PolicyAction is strictly setting up new value. Default value is **false**        | 
| failOnNullSource  | Boolean                         | optional    | Flag that determines if action should fail if source PolicyVariable resolves to null. Default value is **false**                                                                | 

#### Examples

**PolicyActionSave with static field**

```json
{
    "key": "foo",
    "value": {
        "type": "string",
        "value": "bar"
    },
    "failOnExistingKey": true,
    "type": "save"
}
```

**PolicyActionSave with dynamic field**

```json
{
    "key": "foo",
    "value": {
        "resolvers": [
            {
                "id": "birthdayResolver",
                "refType": "PolicyVariableResolverRef"
            }
        ]
    },
    "failOnMissingKey": true,
    "failOnNullSource": true,
    "type": "save"
}
```

**Managed PolicyActionSave**

```json
{
    "id": "polAct1",
    "version": "1.2.3",
    "description": "This is a managed PolicyActionSave",
    "labels": [
        "label1"
    ],
    "key": "foo",
    "value": {
        "resolvers": [
            {
                "id": "birthdayResolver",
                "refType": "PolicyVariableResolverRef"
            }
        ]
    },
    "failOnMissingKey": true,
    "failOnNullSource": true,
    "type": "save"
}
```
## PolicyActionClear

This entity represents an action that deletes a value to the Context data store.

| field             | type                            | cardinality | description                                                                                                                                                                                              | 
|-------------------|---------------------------------|-------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| key               | String                          | mandatory   | The key to be cleared from the data store.                                                                                                                                                               |
| failOnMissingKey  | Boolean                         | optional    | Flag that determines should key already be defined in Context data store. Used for purposes when PolicyAction needs to fail if key that is about to be deleted doesn't exist. Default value is **false** |

#### Examples

**PolicyActionClear**

```json
{
    "key": "foo",
    "failOnMissingKey": true,
    "type": "clear"
}
```

**Managed PolicyActionClear**

```json
{
    "id": "polAct1",
    "version": "1.2.3",
    "description": "This is a managed PolicyActionClear",
    "labels": [
        "label1"
    ],
    "key": "foo",
    "failOnMissingKey": true,
    "type": "clear"
}
```

## PolicyActionJsonMerge

This entity represents an action that [merges](https://datatracker.ietf.org/doc/html/rfc7386) a JSON values from PolicyVariables into a Context data store key. [JsonPatch](https://github.com/java-json-tools/json-patch?tab=readme-ov-file#json-merge-patch) library is used to merge provided PolicyVariables.

| field             | type                            | cardinality | description                                                                                                                                                                                                                                                                                 | 
|-------------------|---------------------------------|-------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| key               | String                          | mandatory   | The key to use when saving the result value in the data store.                                                                                                                                                                                                                              |
| source            | ```IPolicyVariableRefOrValue``` | mandatory   | Definition of a [PolicyVariable](policy-variable.md). It is used as a source of the Json merge operation. Source value must be one of the following types (or castable to that type):<br/>* Null <br/>* JsonNode<br/>* JsonObject<br/>* JsonArray                                           |
| merge             | ```IPolicyVariableRefOrValue``` | mandatory   | Definition of a [PolicyVariable](policy-variable.md). It is used as a target value of the Json merge operation that will merged to the source value. Merge value must be one of the following types (or castable to that type):<br/>* Null <br/>* JsonNode<br/>* JsonObject<br/>* JsonArray |
| failOnMissingKey  | Boolean                         | optional    | Flag that determines should key already be defined in Context data store. Used for purposes when PolicyAction is strictly overriding existing value. Default value is **false**                                                                                                             | 
| failOnExistingKey | Boolean                         | optional    | Flag that determines if key shouldn't be defined in Context data store. Used for purposes when PolicyAction is strictly setting up new value. Default value is **false**                                                                                                                    | 
| failOnNullSource  | Boolean                         | optional    | Flag that determines if action should fail if source PolicyVariable resolves to null. Default value is **false**                                                                                                                                                                            | 
| failOnNullMerge   | Boolean                         | optional    | Flag that determines if action should fail if merge PolicyVariable resolves to null. Default value is **false**                                                                                                                                                                             | 
| destinationType   | String                          | optional    | Sets [value type](policy-variable.md#ipolicyvariable-data-types-and-formats) of the result variable.                                                                                                                                                                                        | 
| destinationFormat | String                          | optional    | Sets [value format](policy-variable.md#ipolicyvariable-data-types-and-formats) of the result variable                                                                                                                                                                                       | 

#### Examples

**PolicyActionJsonMerge**

```json
{
    "key": "someJson",
    "source": {
        "type": "string",
        "value": "{\"foo\":\"bar\"}"
    },
    "merge": {
        "type": "string",
        "value": "{\"foo\":\"baz\"}"
    },
    "type": "jsonMerge"
}
```

**Managed PolicyActionJsonMerge**

```json
{
    "id": "polAct1",
    "version": "1.2.3",
    "description": "This is a managed PolicyActionJsonMerge",
    "labels": [
        "label1"
    ],
    "key": "someJson",
    "source": {
        "id": "polVar1",
        "refType": "PolicyVariableRef"
    },
    "merge": {
        "type": "string",
        "value": "{\"foo\":\"baz\"}"
    },
    "failOnMissingKey": true,
    "failOnNullSource": true,
    "type": "jsonMerge"
}
```

## PolicyActionJsonPatch

This entity represents an action that [patches](https://datatracker.ietf.org/doc/html/rfc6902) a JSON value from PolicyVariables into a Context data store key. [JsonPatch](https://github.com/java-json-tools/json-patch?tab=readme-ov-file#json-patch) library is used to patch provided PolicyVariable with defined JsonPatch definition.

| field                 | type                            | cardinality | description                                                                                                                                                                                                                                       | 
|-----------------------|---------------------------------|-------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| key                   | String                          | mandatory   | The key to use when saving the result value in the data store.                                                                                                                                                                                    |
| source                | ```IPolicyVariableRefOrValue``` | mandatory   | Definition of a [PolicyVariable](policy-variable.md). It is used as a source of the Json patch operation. Source value must be one of the following types (or castable to that type):<br/>* Null <br/>* JsonNode<br/>* JsonObject<br/>* JsonArray |
| patch                 | ```IPolicyVariableRefOrValue``` | mandatory   | Definition of a [PolicyVariable](policy-variable.md). It is used as a definition of the Json patch operation that will applied to the source value. Patch value must be JsonArray or Array type                                                   |
| failOnMissingKey      | Boolean                         | optional    | Flag that determines should key already be defined in Context data store. Used for purposes when PolicyAction is strictly overriding existing value. Default value is **false**                                                                   | 
| failOnExistingKey     | Boolean                         | optional    | Flag that determines if key shouldn't be defined in Context data store. Used for purposes when PolicyAction is strictly setting up new value. Default value is **false**                                                                          | 
| failOnNullSource      | Boolean                         | optional    | Flag that determines if action should fail if source PolicyVariable resolves to null. Default value is **false**                                                                                                                                  | 
| castNullSourceToArray | Boolean                         | optional    | Flag that determines if null source value should be casted to the empty array. Default value is **false**                                                                                                                                         |

#### Examples

**PolicyActionJsonPatch**

```json
{
    "key": "someJson",
    "source": {
        "type": "string",
        "value": "{\"foo\":[\"bar\",\"baz\"],\"foo2\":\"baz\"}"
    },
    "patch": {
        "type": "string",
        "value": "[{\"op\":\"replace\",\"path\":\"/foo\",\"value\":[\"bar\"]}]"
    },
    "type": "jsonPatch"
}
```

**Managed PolicyActionJsonPatch**

```json
{
    "id": "polAct1",
    "version": "1.2.3",
    "description": "This is a managed PolicyActionJsonPatch",
    "labels": [
        "label1"
    ],
    "key": "someJson",
    "source": {
        "id": "polVar1",
        "refType": "PolicyVariableRef"
    },
    "patch": {
        "type": "string",
        "value": "[{\"op\":\"replace\",\"path\":\"/foo\",\"value\":[\"bar\"]}]"
    },
    "failOnMissingKey": true,
    "failOnNullSource": true,
    "type": "jsonPatch"
}
```

## PolicyActionRef

PolicyActionRef is entity that references a PolicyAction of any type. It contains following fields:

| field   | type   | cardinality | description                                                                                                      |
|---------|--------|-------------|------------------------------------------------------------------------------------------------------------------|
| id      | String | mandatory   | id of the managed PolicyAction                                                                                   |
| version | String | optional    | SemVer of the referenced PolicyAction. If it is left blank, latest version of a PolicyAction will be populated   |
| refType | String | mandatory   | discriminator that is used when deserializing catalog from JSON file. It has constant value of `PolicyActionRef` |