---
title: Context
description: Description of Policy engine Context
nav_order: 8
---
# Context
{: .no_toc }

<details markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

## Overview

`Context` represents the payload of a request in the Policy Engine execution process. It manages various aspects of the request context, including request data, environment information, subject data, options needed for processing, current processing path, cache implementation, event handler and result collection. It also enables rollback to previous data state.

## Key Components

Following are the key components of the `Context` class:
- stores - `request`, `environment`, `subject`, `data`, defined below
- `cache`: _ICache_ - Caching mechanism (default: HashMapCache)
- `options`: _Options_ - Configuration options
- `event`: _IEvent_ - Event handling mechanism (default: InMemoryEventHandler)
- `id`: _String_ - Unique identifier for the context
- `path`: _MutableList`<String>`_ - Represents the current path of the context
- `result`: _EngineResult_ - Stores results of conditions and policies

### Stores

The `Context` contains four data stores:

- `request`: Represents the request data
- `environment`: Represents the environment data, such as date,time, etc... [DefaultEnvironment](#default-environment) is automatically populated, but can be overridden by user defined values with same keys
- `subject`: Represents the subject data, such as principal, roles, etc...
- `data`: Stores results of Policy evaluation process as defined by [PolicyActions](policy-action.md)

Each store is a `Map<String, Any?>` that can be accessed by key.

### Default Environment

Default environment contains following keys:
- **currentDateTime**: The current date and time in the specified time zone
- **localDateTime**: The current local date and time
- **currentDate**: The current date in the specified time zone
- **currentTime**: The current time in the specified time zone
- **localTime**: The current local time
- **utcDateTime**: The current date and time in UTC
- **utcDate**: The current date in UTC
- **utcTime**: The current time in UTC
- **year**: The current year
- **month**: The current month
- **day**: The current day of the month
- **dayOfWeek**: The current day of the week
- **dayOfYear**: The current day of the year
- **hour**: The current hour
- **minute**: The current minute
- **second**: The current second
- **nano**: The current nanosecond
- **offset**: The current time zone offset

CurrentDateTime is calculated based on clock provided by [Options](#options). 
If no clock is provided, then ```OffsetDateTime.now(options.zoneId)``` is used to generate current date and time.

### Options

Options contains configuration settings that can be provided in [Context] instance. All option settings can be overridden:

- **objectMapper** - Jackson ObjectMapper instance. Default instance contains following settings:
  -  Registers the JavaTimeModule to handle Java 8 date/time types 
  - Sets the serialization inclusion to exclude empty values
  - Configures the JsonGenerator to write BigDecimal values as plain text
  - Disables writing dates, times and durations as timestamps
  - Disables failing on unknown properties during deserialization
  - Enables accepting empty strings and arrays as null objects during deserialization
  - Disables adjusting dates to the context time zone during deserialization 
- **dateTimeFormatter** - sets DateTimeFormatter. Default value is ```DateTimeFormatter.ISO_OFFSET_DATE_TIME```
- **dateFormatter** - sets DateFormatter. Default value is ```DateTimeFormatter.ISO_LOCAL_DATE```
- **timeFormatter** - sets TimeFormatter. Default value is ```DateTimeFormatter.ISO_LOCAL_TIME```
- **zoneId** - sets time zone. Default value is ```ZoneId.systemDefault()```
- **clock** - sets Clock instance that can be used in DefaultEnvironment. Usually used for testing purposes. Default value is ```null```
- **defaultSchemaUri** - sets default schema used in VertX Json Schema validator. Default value is ```https://github.com/ivsokol```

### Cache

Cache is used to store results of variable resolvers, variables, conditions and policies. All cache implementation must implement _ICache_
interface. There are currently two cache implementations: `HashMapCache` (default) and `NoCache`.

**_ICache_** interface contains following methods:

| method                                                       | description                                                                            | 
|--------------------------------------------------------------|----------------------------------------------------------------------------------------|
| `put(store: PolicyStoreCacheEnum, key: String, value: Any?)` | saves value to the defined cache                                                       |
| `get(store: PolicyStoreCacheEnum, key: String): Any?`        | retrieves value from the defined cache                                                 |
| `hasKey(store: PolicyStoreCacheEnum, key: String): Boolean`  | checks if key exists in the cache                                                      |
| `putVariable(key: String, value: VariableValue)`             | saves PolicyVariable value to the Variable cache                                       |
| `putCondition(key: String, value: Boolean?)`                 | saves PolicyCondition result value to the Condition cache                              |
| `putPolicy(key: String, value: PolicyResultEnum)`            | saves Policy result value to the Policy cache                                          |
| `getVariable(key: String): VariableValue?`                   | retrieves PolicyVariable result value from the Variable cache                          |
| `getCondition(key: String): Boolean?`                        | retrieves PolicyCondition result value from the Condition cache                        |
| `getPolicy(key: String): PolicyResultEnum?`                  | retrieves Policy result value from the Policy cache                                    |
| `getJsonNodeKeyValue(key: String): JsonNode?`                | retrieves JsonNode value from dedicated cache. Used in JMESPath PolicyVariableResolver |
| `getStringKeyValue(key: String): String?`                    | retrieves String value from the dedicated cache. Used in JQ  PolicyVariableResolver    |
| `clear()`                                                    | clears all cache                                                                       |


### Event Handler

Event handler is used to store events related to Policy execution. It stores data related to the processing of all Policy entities.
All event handler implementation must implement _IEventHandler_ interface. There is currently only one event handler
implementations: `InMemoryEventHandler` and it is a default one.

Following data is stored in each event:
- timestamp
- context ID
- entity type
- entity ID
- entity value
- success/failure flag
- fromCache flag
- event reason

There are 3 levels of event handling:

- **NONE** - No events are stored
- **BASIC** - Events are stored, but without event value
- **DETAILS** - Events are stored with event value

### Evaluation results

Contains results of Policy evaluation process. Depending on which engine method is invoked, it can contain map of `Booleans?` (for Condition evaluation, where key is condition ID) or map of ```Pair<PolicyResultEnum, ActionResult?>>``` (for Policy evaluation where key is policy ID). 

_PolicyResultEnum_ is an enum that contains possible [results](policy.md#policyresult) of Policy evaluation.
_ActionResult_ is a `Boolean` that contains information is PolicyAction successfully executed, if there was PolicyAction on evaluated Policy.

### Path

Contains current position in Policy evaluation process. It is a list of Strings that contains entity IDs and their full path. Example of path template is ```{policyID}/condition({conditionId})/args/{policyVariableId}```. 

Root entities must have an ID, and child entities will have their IDs populated in brackets (for managed entities) or they will have their index in child collection (for embedded entities). If there is a composite Policy Condition that has two atomic Policy Conditions, one managed, one embedded, paths for those 
conditions will be ```{compositeConditionId}/conditions/0({atomicConditionId})``` and ```{compositeConditionId}/conditions/1```.


Following entity paths are possible:
- policies - for policies referenced in PolicySet
- condition - for condition reference in Policy
- conditions - for condition references in Composite Condition
- args - for variables referenced in Conditions
- resolvers - for dynamic variable resolvers
- constraint - for constraint handlers
- actions - for actions referenced in Policy
- source - for variables read in PolicyActions
- merge - for variables written in PolicyActionJsonMerge
- patch - for variables written in PolicyActionJsonPatch
