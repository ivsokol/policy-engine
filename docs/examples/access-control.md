---
title: Access Control
parent: Examples
description: Access control PolicyEngine example
nav_order: 1
---
# Access Control
{: .no_toc }

<details markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

In this example, we will create a simple access control policy based on following demands:

* if user role is "user", then allow access only on working hours (9:00 - 17:00) at working days (Monday - Friday)
* if user role is "admin", then allow access on any day and time
* after successfull Policy execution, provide following message:
  * "Access has been granted for {username}" if user has been granted access
  * "Access has been denied for {username}" if user has been denied access

To resolve such demand, we need to extract user role from the context and check if current time is within working hours. Working hour check should be done only if user role is "user". Depending on policy result resolution we shall build message and save it to data store.

# Policy Catalog

In order to achieve this, we will create a PolicyCatalog with following entities:

## Policy Variables

We need 4 PolicyVariables:
* role - to find out user role
* currentTime - to find out current time
* dayOfWeek - to find out current day of the week
* username - to find out user username in order to build a message

### role

To find a user role, we need a dynamic variable that will extract role from the context. Security related properties are usually defined in the subject store, so we shall create 
a variable that will extract role from the subject store and related PolicyVariable resolver. Resolver could have been created as an embedded entity, but for the sake of an example, we will create it as a managed entity.

**role PolicyVariable**
```json
{
  "id": "role",
  "description": "Provided role",
  "resolvers": [
    {
      "id": "roleResolver",
      "refType": "PolicyVariableResolverRef"
    }
  ],
  "type": "string"
}
```

**role PolicyVariableResolver**
```json
{
  "id": "roleResolver",
  "description": "Extracts role from subject store",
  "source": "subject",
  "key": "role"
}
```

### currentTime

Current time is a variable saved in Environment store, under key `localTime`. We will create a variable that will
extract current time from the environment with embedded PolicyVariableResolver. 
Variable type in runtime will be LocalTime.

**currentTime PolicyVariable**
```json
{
  "id": "currentTime",
  "description": "Current time",
  "resolvers": [
    {
      "source": "environment",
      "key": "localTime"
    }
  ],
  "type": "string",
  "format": "time"
}
```

### dayOfWeek

Current day in the week is a variable saved in Environment store, under key `dayOfWeek`. 
We will create a variable that will extract day of week from the environment with embedded
PolicyVariableResolver. Variable type in runtime will be Int. Possible values are 1-7.

**currentTime PolicyVariable**
```json
{
  "id": "dayOfWeek",
  "description": "Current day of week",
  "resolvers": [
    {
      "source": "environment",
      "key": "dayOfWeek"
    }
  ],
  "type": "int"
}
```

### username

Username as a variable is not defined by itself, but as a part of JQ definition to build a `message` key in data store. It is created as an embedded variable inside [PolicyAction](#policy-actions) definition.
In order to test JQ command manually, you can use any JQ online tester and apply JQ commands below on the following JSON:

```json
{
  "role": "user",
  "username": "user1"
}
```
This JSON is in shape of Subject store from the context.

**accept message**
```json
{
  "resolvers": [
    {
      "source": "subject",
      "path": "\"Access has been granted for \" + .username",
      "engine": "JQ"
    }
  ],
  "type": "string"
}
```

**deny message**
```json
{
  "resolvers": [
    {
      "source": "subject",
      "path": "\"Access has been denied for \" + .username",
      "engine": "JQ"
    }
  ],
  "type": "string"
}
```

## Policy Conditions

In order to check if user is the admin, we need only one atomic condition that checks if user role is "admin".

If we want to check if user is a regular user, we need to check if user role is "user", if current day is a working day and if current time is within working hours.

We could also check if current day is public holiday, but for the sake of simplicity, we will not implement it. It could be done by setting public holiday dates
in the environment store and then creating one more condition to check if currentDay exists in public holiday list.

### isAdmin

This condition will compare static PolicyVariable of value "admin" with dynamic PolicyVariable defined in **role** PolicyVariable [definition](#policy-variables) for equality. StringIgnoreCase is set to true to make comparison case-insensitive.

**isAdmin PolicyCondition**
```json
{
  "id": "isAdmin",
  "description": "Checks if provided role is equal to 'admin'",
  "operation": "Equals",
  "args": [
    {
      "type": "string",
      "value": "admin"
    },
    {
      "id": "role",
      "refType": "PolicyVariableRef"
    }
  ],
  "stringIgnoreCase": true
}
```

### isUser

This condition will compare static PolicyVariable of value "user" with dynamic PolicyVariable defined in **role** PolicyVariable [definition](#policy-variables) for equality. StringIgnoreCase is set to true to make comparison case-insensitive.

**isUser PolicyCondition**
```json
{
  "id": "isUser",
  "description": "Checks if provided role is equal to 'user'",
  "operation": "Equals",
  "args": [
    {
      "type": "string",
      "value": "user"
    },
    {
      "id": "role",
      "refType": "PolicyVariableRef"
    }
  ],
  "stringIgnoreCase": true
}
```

### isWorkingDay

This condition will compare dynamic PolicyVariable defined in **dayOfWeek** PolicyVariable [definition](#policy-variables) to check if it is lower or equal to 5 (which will be defined as a static PolicyVariable of type int). If dayOfWeek variable has a value 
of 1-5, it will be considered as a working day.

**isWorkingDay PolicyCondition**
```json
{
  "id": "isWorkingDay",
  "description": "Checks if it is working day currently (Mon-Fri)",
  "operation": "LessThanEqual",
  "args": [
    {
      "id": "dayOfWeek",
      "refType": "PolicyVariableRef"
    },
    {
      "type": "int",
      "value": 5
    }
  ]
}
```

### isWorkingHour

This composite condition will compare dynamic PolicyVariable defined in **currentTime** PolicyVariable [definition](#policy-variables) to check if it is higher or equal to "09:00" (which will be defined as a static PolicyVariable of type string, format time and custom timeFormat value) and lower or equal to "17:00" (which will be defined as a static PolicyVariable of type string, format time and custom timeFormat value). 

This composite PolicyCondition will resolve to true only if both embedded PolicyConditions resolve to true, as defined in conditionCombinationLogic value `allOf`.

**isWorkingHour PolicyCondition**
```json
{
  "id": "isWorkingHour",
  "description": "Checks if it is working hour currently (09:00-17:00)",
  "conditionCombinationLogic": "allOf",
  "conditions": [
    {
      "operation": "GreaterThanEqual",
      "args": [
        {
          "id": "currentTime",
          "refType": "PolicyVariableRef"
        },
        {
          "type": "string",
          "format": "time",
          "timeFormat": "HH:mm",
          "value": "09:00"
        }
      ]
    },
    {
      "operation": "LessThanEqual",
      "args": [
        {
          "id": "currentTime",
          "refType": "PolicyVariableRef"
        },
        {
          "type": "string",
          "format": "time",
          "timeFormat": "HH:mm",
          "value": "17:00"
        }
      ]
    }
  ]
}
```

### regularUserAccess

This composite PolicyCondition will take upper conditions and combine them using conditionCombinationLogic value
`allOf` to check if user role is "user", if current day is a working day and if current time is within working hours.

**regularUserAccess PolicyCondition**
```json
{
  "id": "regularUserAccess",
  "description": "Checks if user has role 'user' and if it is a working day and working hour",
  "conditionCombinationLogic": "allOf",
  "conditions": [
    {
      "id": "isUser",
      "refType": "PolicyConditionRef"
    },
    {
      "id": "isWorkingDay",
      "refType": "PolicyConditionRef"
    },
    {
      "id": "isWorkingHour",
      "refType": "PolicyConditionRef"
    }
  ]
}
```

## Policies

Three Policy definitions should be defined in the PolicyCatalog

### userAccess

This Policy will check `regularUserAccess` PolicyCondition. If it resolves to true, it will evaluate to `permit`. 
If it resolves to false, it will evaluate to `deny`, as strictTargetEffect flag is set to true.
Note that this Policy doesn't have any PolicyAction defined.

**userAccess Policy**
```json
{
  "id": "userAccess",
  "description": "Allows access to regular user if it is working day and working hour",
  "targetEffect": "permit",
  "condition": {
    "id": "regularUserAccess",
    "refType": "PolicyConditionRef"
  },
  "strictTargetEffect": true
}
```

### adminAccess

This Policy will check `isAdmin` PolicyCondition. If it resolves to true, it will evaluate to `permit`.
If it resolves to false, it will evaluate to `deny`, as strictTargetEffect flag is set to true.
Note that this Policy doesn't have any PolicyAction defined.

**adminAccess Policy**
```json
{
  "id": "adminAccess",
  "description": "Allows access to admin user",
  "targetEffect": "permit",
  "condition": {
    "id": "isAdmin",
    "refType": "PolicyConditionRef"
  },
  "strictTargetEffect": true
}
```

### checkAccess

This PolicySet will take upper Policies and combine them using policyCombinationLogic `denyUnlessPermit` to check
if user is either admin or if user is regular user and it is working day and working hour. It could also be possible that user role is "guest", in which case it will resolve to deny by default.

"adminAccess" Policy will be evaluated first (as priority is set to 10), then "userAccess" Policy will be evaluated (with default priority of 0). This is optimization, to avoid 
unnecessary evaluation of "userAccess" Policy that has more conditions to check if "adminAccess" Policy resolves to permit.

PolicyActions are defined in this PolicySet, one to execute if Policy resolves to permit, and another to execute if
Policy resolves to deny.

**checkAccess Policy**
```json
{
  "id": "checkAccess",
  "description": "Checks if user has access",
  "actions": [
    {
      "executionMode": [
        "onDeny"
      ],
      "action": {
        "id": "setForbiddenMessage",
        "refType": "PolicyActionRef"
      }
    },
    {
      "executionMode": [
        "onPermit"
      ],
      "action": {
        "id": "setAllowedMessage",
        "refType": "PolicyActionRef"
      }
    }
  ],
  "policyCombinationLogic": "denyUnlessPermit",
  "policies": [
    {
      "policy": {
        "id": "userAccess",
        "refType": "PolicyRef"
      }
    },
    {
      "priority": 10,
      "policy": {
        "id": "adminAccess",
        "refType": "PolicyRef"
      }
    }
  ]
}
```

## Policy Actions

Two PolicyActions are defined in the PolicyCatalog, one to set forbidden message and another to set allowed message.

### setForbiddenMessage

This PolicyActionSave will set `message` variable to `"Access has been denied for {username}"` template by using JQ parser.

**setForbiddenMessage PolicyAction**

```json
{
  "id": "setForbiddenMessage",
  "description": "Sets message for user for which access has been denied",
  "key": "message",
  "value": {
    "resolvers": [
      {
        "source": "subject",
        "path": "\"Access has been denied for \" + .username",
        "engine": "JQ"
      }
    ],
    "type": "string"
  },
  "type": "save"
}
```

### setAllowedMessage

This PolicyActionSave will set `message` variable to `"Access has been granted for {username}"` template by using JQ parser.

```json
{
  "id": "setAllowedMessage",
  "description": "Sets message for user who has been granted access",
  "key": "message",
  "value": {
    "resolvers": [
      {
        "source": "subject",
        "path": "\"Access has been granted for \" + .username",
        "engine": "JQ"
      }
    ],
    "type": "string"
  },
  "type": "save"
}
```

# PolicyEngine execution

PolicyEngine will be instantiated with PolicyCatalog containing previously defined entities. In order to provide consistent tests, mock clock will
be used to provide exact date and time.
Subject store in Context will be populated with "role" and "username" fields as a Map.
Event handler will be set to provide details of each event. Events presented below will be manually formatted for better
readability.

All tests will have similar pattern for PolicyEngine execution.

```kotlin
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
```

### Access granted for user

In this test current time will be set to working hour and user role will be set to "user".

```kotlin
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
```

After execution of PolicyEngine, result will be pair `permit,true`, which means that `checkAccess` Policy resolved to permit and all actions were executed 
successfully.

Context data store will contain one entry with key `message` and value `"Access has been granted for user1"`.

Context cache will contain following entries:

```kotlin
HashMapCache(
  policyStore={
    adminAccess=deny, 
    userAccess=permit, 
    checkAccess=permit},
  valueStore={
    role=VariableValue(type=STRING, body=user), 
    dayOfWeek=VariableValue(type=INT, body=5), 
    currentTime=VariableValue(type=TIME, body=13:42:56)
  },
  conditionStore={
    isAdmin=false, 
    isUser=true, 
    isWorkingDay=true, 
    isWorkingHour=true, 
    regularUserAccess=true
  },
  keyValueAsJsonNode={},
  keyValueAsString={SUBJECT::"Access has been granted for " + .username={"role":"user","username":"user1"}})
```

`keyValueAsString` cache entry is optimization for JQ processing.

Context event will contain following entries:

```
entity=ENGINE_START, entityId=access-control:2024-02-17, message=null, success=true, fromCache=false
entity=VARIABLE_STATIC, entityId=checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/0, message=VariableValue(type=STRING, body=admin), success=true, fromCache=false
entity=VALUE_RESOLVER, entityId=checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/1(role)/resolvers/0(roleResolver), message=user, success=true, fromCache=false
entity=VARIABLE_DYNAMIC, entityId=checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/1(role), message=VariableValue(type=STRING, body=user), success=true, fromCache=false
entity=CONDITION_ATOMIC, entityId=checkAccess/policies/1(adminAccess)/condition(isAdmin), message=false, success=true, fromCache=false
entity=POLICY, entityId=checkAccess/policies/1(adminAccess), message=deny, success=false, fromCache=false
entity=VARIABLE_STATIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/0(isUser)/args/0, message=VariableValue(type=STRING, body=user), success=true, fromCache=false
entity=VARIABLE_DYNAMIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/0(isUser)/args/1(role), message=VariableValue(type=STRING, body=user), success=true, fromCache=true
entity=CONDITION_ATOMIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/0(isUser), message=true, success=true, fromCache=false
entity=VALUE_RESOLVER, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/1(isWorkingDay)/args/0(dayOfWeek)/resolvers/0, message=5, success=true, fromCache=false
entity=VARIABLE_DYNAMIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/1(isWorkingDay)/args/0(dayOfWeek), message=VariableValue(type=INT, body=5), success=true, fromCache=false
entity=VARIABLE_STATIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/1(isWorkingDay)/args/1, message=VariableValue(type=INT, body=5), success=true, fromCache=false
entity=CONDITION_ATOMIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/1(isWorkingDay), message=true, success=true, fromCache=false
entity=VALUE_RESOLVER, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/0/args/0(currentTime)/resolvers/0, message=13:42:56, success=true, fromCache=false
entity=VARIABLE_DYNAMIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/0/args/0(currentTime), message=VariableValue(type=TIME, body=13:42:56), success=true, fromCache=false
entity=VARIABLE_STATIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/0/args/1, message=VariableValue(type=TIME, body=09:00), success=true, fromCache=false
entity=CONDITION_ATOMIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/0, message=true, success=true, fromCache=false
entity=VARIABLE_DYNAMIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/1/args/0(currentTime), message=VariableValue(type=TIME, body=13:42:56), success=true, fromCache=true
entity=VARIABLE_STATIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/1/args/1, message=VariableValue(type=TIME, body=17:00), success=true, fromCache=false
entity=CONDITION_ATOMIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/1, message=true, success=true, fromCache=false
entity=CONDITION_COMPOSITE, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour), message=true, success=true, fromCache=false
entity=CONDITION_COMPOSITE, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess), message=true, success=true, fromCache=false
entity=POLICY, entityId=checkAccess/policies/0(userAccess), message=permit, success=true, fromCache=false
entity=POLICY_SET, entityId=checkAccess, message=permit, success=false, fromCache=false
entity=VALUE_RESOLVER, entityId=checkAccess/actions/1(setAllowedMessage)/source/resolvers/0, message=Access has been granted for user1, success=true, fromCache=false
entity=VARIABLE_DYNAMIC, entityId=checkAccess/actions/1(setAllowedMessage)/source, message=VariableValue(type=STRING, body=Access has been granted for user1), success=true, fromCache=false
entity=POLICY_ACTION_SAVE, entityId=checkAccess/actions/1(setAllowedMessage), message=Access has been granted for user1, success=true, fromCache=false
entity=POLICY_ACTION, entityId=checkAccess, message=true, success=true, fromCache=false
entity=ENGINE_END, entityId=access-control:2024-02-17, message=(permit, true), success=true, fromCache=false
```

In this event flow we can see following steps:
* PolicyEngine starts
* "admin" static PolicyVariable is defined on path `checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/0`
* "role" resolver is invoked to fetch value of "role" PolicyVariable on path
  `checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/1(role)/resolvers/0(roleResolver)`
* "role" PolicyVariable is defined on path `checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/1(role)`
* "isAdmin" PolicyCondition is checked and resolves to false on path `checkAccess/policies/1(adminAccess)/condition(isAdmin)`
* "adminAccess" Policy resolves to deny on path `checkAccess/policies/1(adminAccess)`
* "user" static PolicyVariable is defined on path `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/0(isUser)/args/0`
* "role" PolicyVariable on path `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/0(isUser)/args/1(role)` is pulled from **cache**
* "isUser" PolicyCondition is checked and resolves to true on path
  `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/0(isUser)`
* "dayOfWeek" resolver is invoked on path `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/1(isWorkingDay)/args/0(dayOfWeek)/resolvers/0`
* "dayOfWeek" PolicyVariable is defined on path
  `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/1(isWorkingDay)/args/0(dayOfWeek)`
* 5 as a static PolicyVariable is defined on path `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/1(isWorkingDay)/args/1`
* "isWorkingDay" PolicyCondition is checked and resolves to true on path 
  `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/1(isWorkingDay)`
* "currentTime" resolver is invoked on path
  `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/0/args/0(currentTime)/resolvers/0`
* "currentTime" PolicyVariable is defined on path 
  `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/0/args/0(currentTime)`
* "09:00" static PolicyVariable is defined on path `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/0/args/1`
* "isWorkingHour" first sub-condition is checked and resolves to true on path `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/0`
* "currentTime" PolicyVariable is pulled from **cache** on path
  `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/1/args/0(currentTime)`
* "17:00" static PolicyVariable is defined on path
  `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/1/args/1`
* "isWorkingHour" second sub-condition is checked and resolves to true on path `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/1`
* "isWorkingHour" PolicyCondition is checked and resolves to true on path `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)`
* "regularUserAccess" PolicyCondition is checked and resolves to true on path
  `checkAccess/policies/0(userAccess)/condition(regularUserAccess)`
* "userAccess" Policy resolves to permit on path `checkAccess/policies/0(userAccess)`
* "checkAccess" PolicySet resolves to permit on path `checkAccess`
* Allowed message PolicyVariableResolver is invoked on path `checkAccess/actions/1(setAllowedMessage)/source/resolvers/0` and contains value "Access has been granted for user1"
* Dynamic PolicyVariable on path `checkAccess/actions/1(setAllowedMessage)/source` is set to "Access has been granted for user1"
* "setAllowedMessage" PolicyActionSave is invoked on path `checkAccess/actions/1(setAllowedMessage)`
* "checkAccess" PolicyActions are completed on path `checkAccess`
* PolicyEngine stops

### Access denied for user

In this test current time will be set outside of working hours and user role will be set to "user".

```kotlin
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
```

After execution of PolicyEngine, result will be pair `deny,true`, which means that `checkAccess` Policy resolved to deny and all actions were executed
successfully.

Context data store will contain one entry with key `message` and value `"Access has been denied for user1"`.

Context cache will contain following entries:

```kotlin
HashMapCache(
  policyStore={
    adminAccess=deny, 
    userAccess=deny, 
    checkAccess=deny},
  valueStore={
    role=VariableValue(type=STRING, body=user), 
    dayOfWeek=VariableValue(type=INT, body=5), 
    currentTime=VariableValue(type=TIME, body=23:42:56)
  },
  conditionStore={
    isAdmin=false, 
    isUser=true, 
    isWorkingDay=true, 
    isWorkingHour=false, 
    regularUserAccess=false
  },
  keyValueAsJsonNode={},
  keyValueAsString={SUBJECT::"Access has been denied for " + .username={"role":"user","username":"user1"}})
```

`keyValueAsString` cache entry is optimization for JQ processing.

Context event will contain following entries:

```
entity=ENGINE_START, entityId=access-control:2024-02-17, message=null, success=true, fromCache=false
entity=VARIABLE_STATIC, entityId=checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/0, message=VariableValue(type=STRING, body=admin), success=true, fromCache=false
entity=VALUE_RESOLVER, entityId=checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/1(role)/resolvers/0(roleResolver), message=user, success=true, fromCache=false
entity=VARIABLE_DYNAMIC, entityId=checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/1(role), message=VariableValue(type=STRING, body=user), success=true, fromCache=false
entity=CONDITION_ATOMIC, entityId=checkAccess/policies/1(adminAccess)/condition(isAdmin), message=false, success=true, fromCache=false
entity=POLICY, entityId=checkAccess/policies/1(adminAccess), message=deny, success=false, fromCache=false
entity=VARIABLE_STATIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/0(isUser)/args/0, message=VariableValue(type=STRING, body=user), success=true, fromCache=false
entity=VARIABLE_DYNAMIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/0(isUser)/args/1(role), message=VariableValue(type=STRING, body=user), success=true, fromCache=true
entity=CONDITION_ATOMIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/0(isUser), message=true, success=true, fromCache=false
entity=VALUE_RESOLVER, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/1(isWorkingDay)/args/0(dayOfWeek)/resolvers/0, message=5, success=true, fromCache=false
entity=VARIABLE_DYNAMIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/1(isWorkingDay)/args/0(dayOfWeek), message=VariableValue(type=INT, body=5), success=true, fromCache=false
entity=VARIABLE_STATIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/1(isWorkingDay)/args/1, message=VariableValue(type=INT, body=5), success=true, fromCache=false
entity=CONDITION_ATOMIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/1(isWorkingDay), message=true, success=true, fromCache=false
entity=VALUE_RESOLVER, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/0/args/0(currentTime)/resolvers/0, message=23:42:56, success=true, fromCache=false
entity=VARIABLE_DYNAMIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/0/args/0(currentTime), message=VariableValue(type=TIME, body=23:42:56), success=true, fromCache=false
entity=VARIABLE_STATIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/0/args/1, message=VariableValue(type=TIME, body=09:00), success=true, fromCache=false
entity=CONDITION_ATOMIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/0, message=true, success=true, fromCache=false
entity=VARIABLE_DYNAMIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/1/args/0(currentTime), message=VariableValue(type=TIME, body=23:42:56), success=true, fromCache=true
entity=VARIABLE_STATIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/1/args/1, message=VariableValue(type=TIME, body=17:00), success=true, fromCache=false
entity=CONDITION_ATOMIC, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/1, message=false, success=true, fromCache=false
entity=CONDITION_COMPOSITE, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour), message=false, success=true, fromCache=false
entity=CONDITION_COMPOSITE, entityId=checkAccess/policies/0(userAccess)/condition(regularUserAccess), message=false, success=true, fromCache=false
entity=POLICY, entityId=checkAccess/policies/0(userAccess), message=deny, success=false, fromCache=false
entity=POLICY_SET, entityId=checkAccess, message=deny, success=true, fromCache=false
entity=VALUE_RESOLVER, entityId=checkAccess/actions/0(setForbiddenMessage)/source/resolvers/0, message=Access has been denied for user1, success=true, fromCache=false
entity=VARIABLE_DYNAMIC, entityId=checkAccess/actions/0(setForbiddenMessage)/source, message=VariableValue(type=STRING, body=Access has been denied for user1), success=true, fromCache=false
entity=POLICY_ACTION_SAVE, entityId=checkAccess/actions/0(setForbiddenMessage), message=Access has been denied for user1, success=true, fromCache=false
entity=POLICY_ACTION, entityId=checkAccess, message=true, success=true, fromCache=false
entity=ENGINE_END, entityId=access-control:2024-02-17, message=(deny, true), success=true, fromCache=false
```

In this event flow we can see following steps:
* PolicyEngine starts
* "admin" static PolicyVariable is defined on path `checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/0`
* "role" resolver is invoked to fetch value of "role" PolicyVariable on path
  `checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/1(role)/resolvers/0(roleResolver)`
* "role" PolicyVariable is defined on path `checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/1(role)`
* "isAdmin" PolicyCondition is checked and resolves to false on path `checkAccess/policies/1(adminAccess)/condition(isAdmin)`
* "adminAccess" Policy resolves to deny on path `checkAccess/policies/1(adminAccess)`
* "user" static PolicyVariable is defined on path `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/0(isUser)/args/0`
* "role" PolicyVariable on path `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/0(isUser)/args/1(role)` is pulled from **cache**
* "isUser" PolicyCondition is checked and resolves to true on path
  `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/0(isUser)`
* "dayOfWeek" resolver is invoked on path `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/1(isWorkingDay)/args/0(dayOfWeek)/resolvers/0`
* "dayOfWeek" PolicyVariable is defined on path
  `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/1(isWorkingDay)/args/0(dayOfWeek)`
* 5 as a static PolicyVariable is defined on path `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/1(isWorkingDay)/args/1`
* "isWorkingDay" PolicyCondition is checked and resolves to true on path
  `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/1(isWorkingDay)`
* "currentTime" resolver is invoked on path
  `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/0/args/0(currentTime)/resolvers/0`
* "currentTime" PolicyVariable is defined on path
  `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/0/args/0(currentTime)`
* "09:00" static PolicyVariable is defined on path `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/0/args/1`
* "isWorkingHour" first sub-condition is checked and resolves to true on path `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/0`
* "currentTime" PolicyVariable is pulled from **cache** on path
  `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/1/args/0(currentTime)`
* "17:00" static PolicyVariable is defined on path
  `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/1/args/1`
* "isWorkingHour" second sub-condition is checked and resolves to false on path `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)/conditions/1`
* "isWorkingHour" PolicyCondition is checked and resolves to false on path `checkAccess/policies/0(userAccess)/condition(regularUserAccess)/conditions/2(isWorkingHour)`
* "regularUserAccess" PolicyCondition is checked and resolves to false on path
  `checkAccess/policies/0(userAccess)/condition(regularUserAccess)`
* "userAccess" Policy resolves to deny on path `checkAccess/policies/0(userAccess)`
* "checkAccess" PolicySet resolves to deny on path `checkAccess`
* Allowed message PolicyVariableResolver is invoked on path `checkAccess/actions/0(setForbiddenMessage)/source/resolvers/0` and contains value "Access has been denied for user1"
* Dynamic PolicyVariable on path `checkAccess/actions/0(setForbiddenMessage)/source` is set to "Access has been denied for user1"
* "setForbiddenMessage" PolicyActionSave is invoked on path `checkAccess/actions/0(setForbiddenMessage)`
* "checkAccess" PolicyActions are completed on path `checkAccess`
* PolicyEngine stops

### Access granted for admin

In this test current time will be outside of working hour and user role will be set to "admin".

```kotlin
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
```

After execution of PolicyEngine, result will be pair `permit,true`, which means that `checkAccess` Policy resolved to permit and all actions were executed
successfully.

Context data store will contain one entry with key `message` and value `"Access has been granted for admin1"`.

Context cache will contain following entries:

```kotlin
HashMapCache(
  policyStore={
    adminAccess=permit,
    checkAccess=permit},
  valueStore={
    role=VariableValue(type=STRING, body=admin)
  },
  conditionStore={
    isAdmin=true
  },
  keyValueAsJsonNode={},
  keyValueAsString={SUBJECT::"Access has been granted for " + .username={"role":"admin","username":"admin1"}})
```

`keyValueAsString` cache entry is optimization for JQ processing.

Context event will contain following entries:

```
entity=ENGINE_START, entityId=access-control:2024-02-17, message=null, success=true, fromCache=false 
entity=VARIABLE_STATIC, entityId=checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/0, message=VariableValue(type=STRING, body=admin), success=true, fromCache=false 
entity=VALUE_RESOLVER, entityId=checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/1(role)/resolvers/0(roleResolver), message=admin, success=true, fromCache=false 
entity=VARIABLE_DYNAMIC, entityId=checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/1(role), message=VariableValue(type=STRING, body=admin), success=true, fromCache=false 
entity=CONDITION_ATOMIC, entityId=checkAccess/policies/1(adminAccess)/condition(isAdmin), message=true, success=true, fromCache=false 
entity=POLICY, entityId=checkAccess/policies/1(adminAccess), message=permit, success=true, fromCache=false 
entity=POLICY_SET, entityId=checkAccess, message=permit, success=false, fromCache=false 
entity=VALUE_RESOLVER, entityId=checkAccess/actions/1(setAllowedMessage)/source/resolvers/0, message=Access has been granted for admin1, success=true, fromCache=false 
entity=VARIABLE_DYNAMIC, entityId=checkAccess/actions/1(setAllowedMessage)/source, message=VariableValue(type=STRING, body=Access has been granted for admin1), success=true, fromCache=false 
entity=POLICY_ACTION_SAVE, entityId=checkAccess/actions/1(setAllowedMessage), message=Access has been granted for admin1, success=true, fromCache=false 
entity=POLICY_ACTION, entityId=checkAccess, message=true, success=true, fromCache=false 
entity=ENGINE_END, entityId=access-control:2024-02-17, message=(permit, true), success=true, fromCache=false
```

In this event flow we can see following steps:
* PolicyEngine starts
* "admin" static PolicyVariable is defined on path `checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/0`
* "role" resolver is invoked to fetch value of "role" PolicyVariable on path
  `checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/1(role)/resolvers/0(roleResolver)`
* "role" PolicyVariable is defined on path `checkAccess/policies/1(adminAccess)/condition(isAdmin)/args/1(role)`
* "isAdmin" PolicyCondition is checked and resolves to true on path `checkAccess/policies/1(adminAccess)/condition(isAdmin)`
* "adminAccess" Policy resolves to permit on path `checkAccess/policies/1(adminAccess)`
* "checkAccess" PolicySet resolves to permit on path `checkAccess`
* Allowed message PolicyVariableResolver is invoked on path `checkAccess/actions/1(setAllowedMessage)/source/resolvers/0` and contains value "Access has been granted for admin1"
* Dynamic PolicyVariable on path `checkAccess/actions/1(setAllowedMessage)/source` is set to "Access has been granted for admin1"
* "setAllowedMessage" PolicyActionSave is invoked on path `checkAccess/actions/1(setAllowedMessage)`
* "checkAccess" PolicyActions are completed on path `checkAccess`
* PolicyEngine stops

We can see in these list of events that PolicyEngine works in optimized way and skips unnecessary calculations. In this case, it is calculation of "userAccess" Policy, as "adminAccess" Policy resolved to permit first (it had higher priority in "checkAccess" PolicySet).