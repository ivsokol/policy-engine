---
title: Home
description: Introduction to Policy engine
nav_order: 1
permalink: /
---

# Policy Engine - PoE

<br />
<br />

PolicyEngine - PoE is a catalog driven policy engine written in Kotlin. It provides a flexible framework for defining and evaluating
policies and conditions based on the provided context.

PoE is designed to be used as a library in various enterprise applications, for scenarios as [access control](docs/examples/access-control.md), authorization, credit scoring, price calculation or any other use
case where policies and conditions need to be evaluated. Catalog definition is defined in JSON format.

It is written in Kotlin and can be used in any Java or Kotlin JVM project. As library deals with potentially complex definitions, all parts of the engine are extensively tested (code is covered with more than 2000 tests).

[PolicyEngine](docs/policy-engine.md) is a catalog driven policy engine, so in order to use it, you need to define a catalog. [PolicyCatalog](docs/policy-catalog.md) can be defined as a Class or as a JSON string. JSON string can be pulled from a remote location or from a local file (not part of the engine). One engine instance can only contain one catalog, but it is possible to create multiple PolicyEngine instances at the same time.

One instance of PolicyEngine can be used to evaluate multiple policies or conditions in parallel (it can be used as a singleton).
PolicyEngine is doing variable extraction over a provided [Context](docs/context.md). Engine cannot communicate with external systems, so
it is up to the client to provide all necessary data to the engine as part of the Context stores.

Engine also ships with expression language support (PEEL - [PolicyEngine Expression Language](docs/expression-language/index)). It allows users to define custom conditions and policies through strings that can be evaluated by the engine.

### Inspiration

PoE was inspired by [eXtensible Access Control Markup Language](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html). Even though it
is not a direct implementation of XACML, it shares the same principles and concepts.

### Engine capabilities

* **Condition check** - PoE can check if a condition is true or false. Conditions can be atomic or composite.
* **Policy evaluation** - PoE can evaluate a policy and return a result. Policy can be simple Policy or PolicySet.
* **PolicyAction execution** - PoE can execute a PolicyAction as a side effect of a Policy evaluation. PolicyAction produces custom evaluation output data
* **Entity selection and ordering** - PoE can select conditions, policies and actions that will be executed based on labels or constraints. Ordering is possible by defining entity priority.
* **Dynamic variable extraction** - PoE can extract variables from a context and use them
* **Variable smart casting** - PoE can cast variables to a specific type and format
* **PolicyCatalog validation** - PoE can validate a PolicyCatalog for cycle dependencies and missing references

### Engine entities

PolicyEngine is based on following entities:
* **[PolicyCatalog](docs/policy-catalog.md)** - a collection of all engine entities.
* **[Policy](docs/policy.md)** - entity that evaluates a condition or other Policies and returns result with possible side effects.
* **[PolicyCondition](docs/policy-condition.md)** - entity that evaluates a condition and returns boolean result.
* **[PolicyVariable](docs/policy-variable.md)** - entity that represents a dynamic or static variable that can be used in a Policy or PolicyCondition. 
* **[PolicyAction](docs/policy-action.md)** - entity that executes a side effect of a Policy evaluation.

## Getting Started

In order to use PoE in your project, you need to add the following dependency to your project:
 
```kotlin
implementation("io.github.ivsokol:poe:1.1.0")
```

After that you need to define a PolicyCatalog and instantiate a PolicyEngine.

```kotlin
val catalog = "..."
val engine = PolicyEngine(catalog)
```

Once PolicyEngine is instantiated, you can start evaluating policies and conditions by defining a Context. What can be provided in Context is up to the client. Usually request data is put in request store (it can contain body of a request, headers, metadata, ...), 
security data (username, roles, ...) is put in subject store and server related data is put in the environment store. 
Context stores are maps, as explained in [Context stores](docs/context.md#stores) page.
One context should be created for each evaluation.

```kotlin
val context = Context(
    request = mapOf("body" to requestData),
    subject = mapOf("principal" to userData),
)
```

Then you can evaluate a policy by calling `evaluate` method or condition by calling `check` method on PolicyEngine. There are different methods defined, where you can execute method by id, Reference, label, list of ids or references. You can also execute all Conditions
or Policies in the catalog. All possible methods are described in [PolicyEngine](docs/policy-engine.md) page.

**PolicyCondition check by id**
```kotlin
val result = engine.check("isAdmin", context)
// result value is a Boolean?
// there are no PolicyAction values, as conditions don't have actions
```

**Policy evaluation by id**

```kotlin
val result = engine.evaluatePolicy("checkAccess", context)
// result value is a Pair<PolicyResultEnum, ActionResult?>
// policy result (permit/deny) is stored in the first value of the pair
val policyResult = result.first
// action execution success flag is stored in the second value of the pair
val actionsExecutedSuccessfully = result.second
// data produced as a part of action execution is stored in context data store and can be accessed
val msg = context.dataStore()["message"]
```
It is also possible to evaluate custom policies and conditions not defined in the Catalog. Custom policies and
conditions must be defined as respective classes and not as strings.

**Custom Condition check**
```kotlin
val result =
  PolicyEngine()
      .checkCondition(
          PolicyConditionAtomic(
              operation = OperationEnum.IS_PAST,
              args =
                  listOf(
                      PolicyVariableStatic(
                          value = OffsetDateTime.now().minusYears(1),
                          type = VariableValueTypeEnum.STRING,
                          format = VariableValueFormatEnum.DATE_TIME))),
          Context())
// result value will always be true as condition argument is always in the past
```

**Custom Condition check by expression language**
```kotlin
val policyConditionStr = """
*past(
    #dTime(15.05.2023 14:30:00.123 +01:00,
        #opts(dateTimeFormat="dd.MM.yyyy HH:mm:ss.SSS XXX")
    )
)
""".trimIndent()
val context = Context()
val result =
    PolicyEngine().checkCondition(PEELParser(policyConditionStr).parseCondition(), context)
// result value will always be true as condition argument is always in the past
```

**Custom Policy evaluation**

```kotlin
val context = Context()
val result =
  PolicyEngine()
      .evaluatePolicy(
          Policy(
              targetEffect = PolicyTargetEffectEnum.PERMIT,
              condition =
                  PolicyConditionAtomic(
                      operation = OperationEnum.IS_PAST,
                      args =
                          listOf(
                              PolicyVariableStatic(
                                  value = OffsetDateTime.now().minusYears(1),
                                  type = VariableValueTypeEnum.STRING,
                                  format = VariableValueFormatEnum.DATE_TIME))),
              actions =
                  listOf(
                      PolicyActionRelationship(
                          action =
                              PolicyActionSave(
                                  key = "foo",
                                  value = PolicyVariableStatic(value = "bar"))))),
          context)
// result value will always be 'permit' as condition always evaluates to true
val policyResult = result.first
// action execution success flag (custom output data) is stored in the second value of the pair and will be true
val actionsExecutedSuccessfully = result.second
// data produced as a part of action execution is stored in context data store and can be accessed
val msg = context.dataStore()["foo"]
// msg will be "bar"
```

You can check provided tests for more examples.

## License

This project is available as open source under the terms of [Apache 2.0 License](https://opensource.org/licenses/Apache-2.0).
