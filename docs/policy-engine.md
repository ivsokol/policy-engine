---
title: Policy Engine
description: Introduction to Policy Engine
nav_order: 7
---
# Policy Engine
{: .no_toc }

<details markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

**PolicyEngine** is a powerful tool for evaluation of conditions and policies. It provides a flexible and
extensible framework for defining and evaluating policies based on various conditions and actions.
It has a catalog driven approach, where policies are defined in a catalog and can be evaluated against a given context.
Policy engine can evaluate one, multiple policies or all policies or conditions in a catalog.

Main constructor in PolicyEngine is:

`class PolicyEngine(private val policyCatalog: PolicyCatalog = EmptyPolicyCatalog())`

If no PolicyCatalog is provided, PolicyEngine will use [EmptyPolicyCatalog](policy-catalog.md#empty-policycatalog) as a default.
Supporting constructor can take PolicyCatalog as a string parameter, in which case it will be parsed and validated.

`fun PolicyEngine(policyCatalog: String): PolicyEngine`

All PolicyEngine methods have [Context](context.md) as a parameter. Context is a mutable object that can be used to store and retrieve
data during policy evaluation. It can also be used to retrieve data from check or evaluation run, so it is suggested that Context is always 
created as a variable before engine is invoked.

As Context is used as a holder of all data during Policy evaluation, one PolicyEngine instance can be used to evaluate
multiple policies or conditions in parallel. In other words, one PolicyEngine instance can be used as a **Singleton** for
all Policy or PolicyCondition evaluation.

#### Example

In this example PolicyEngine was created with a catalog in Json format that contains a policies and conditions related to access control. As this is part of a test, test instance of a Clock was provided in options parameter. Subject store was also defined with two parameters: role and username. Event handler was defined with all details.

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

Context object is invoked after evaluation to retrieve events, cache and data store. More detailed description of the result can be found in [Examples](examples/index.md) page.

## PolicyCondition methods

### Check condition by id

`fun checkCondition(id: String, context: Context, version: SemVer? = null): Boolean?`

This method takes a condition ID, a context, and an optional version of the condition and checks it, returning a Boolean
value as a result. PolicyCondition is pulled from PolicyCatalog by id and version. If version is not provided, latest
version is used.

### Check custom condition

`fun checkCondition(condition: IPolicyCondition, context: Context): Boolean?`

This method takes a condition and a context, and checks it, returning a Boolean value as a result. Custom condition can reference other PolicyConditions and PolicyVariables defined in provided PolicyCatalog.

### Check conditions by multiple IDs

`fun checkConditionsByIds(ids: Set<String>, context: Context): Map<String, Boolean?>`

This method takes a list of condition IDs, a context, and checks them, returning a map of condition IDs to Boolean
values as a result. PolicyConditions are pulled from PolicyCatalog by IDs and implicit latest version.

### Check conditions by set of Reference objects

`fun checkConditionsByRefs(refs: Set<Reference>, context: Context): Map<String, Boolean?>`

This method takes a list of Reference objects, a context, and checks them, returning a map of condition IDs to Boolean 
values as a result. Reference objects contain ID and version of the condition. If version is not provided, latest
version is used.

**Reference** data class
```
data class Reference(val id: String, val version: SemVer? = null) 
```

### Check conditions by labels

`fun checkConditionsByLabels(labels: Set<String>, logic: LabelSearchLogicEnum, context: Context): Map<String, Boolean?>`

This method takes a set of labels, search logic, a context, and checks them, returning a map of condition IDs to Boolean values as a
result. Search logic is an enum that can take two values:
* `ANY_OF` - if any of the labels is present in the context, the condition is checked
* `ALL_OF` - if all the labels are present in the context, the condition is checked

### Check all conditions in the catalog

`fun checkAllConditions(context: Context): Map<String, Boolean?>`

This method takes a context, and checks all latest versions of conditions in the catalog, returning a map of condition IDs to Boolean
values as a result.

## Policy methods

All policy methods are returning `Pair<PolicyResultEnum, ActionResult?>` as the response, where first element is a
[PolicyResultEnum](policy.md#policyresult), and second element is a Boolean type alias (`typealias ActionResult = Boolean`) that contains information were all actions (if there were any) on selected Policy had successful execution or not.

When policies are evaluated, they can be checked for [constraints](policy.md#constraints). If a policy has constraints, they will be checked
before the policy is actually evaluated.

### Evaluate policy by id

`fun evaluatePolicy(id: String, context: Context, version: SemVer? = null): Pair<PolicyResultEnum, ActionResult?>`

This method takes a policy ID, a context, and an optional version of the policy and evaluates it, returning a policy evaluation result.
Policy is pulled from PolicyCatalog by id and version. If version is not provided, latest version is used.

### Evaluate custom Policy

`fun evaluatePolicy(policy: IPolicy, context: Context): Pair<PolicyResultEnum, ActionResult?>`

This method takes a policy and a context, and evaluates it, returning a policy evaluation result. Policy can reference
other entities defined in provided PolicyCatalog.

### Evaluate policy by multiple IDs

`fun evaluatePoliciesByIds(ids: Set<String>, context: Context): Map<String, Pair<PolicyResultEnum, ActionResult?>>`

This method takes a set of policy IDs, a context, and evaluates them, returning a map of policy IDs to policy
evaluation results.

### Evaluate policies by set of Reference objects

`fun evaluatePoliciesByRefs(refs: Set<Reference>, context: Context): Map<String, Pair<PolicyResultEnum, ActionResult?>>`

This method takes a set of Reference objects, a context, and evaluates them, returning a map of policy IDs to policy 
evaluation results. Reference objects contain ID and version of the policy. If version is not provided, latest version
is used.

**Reference** data class
```
data class Reference(val id: String, val version: SemVer? = null) 
```

### Evaluate policies by labels

`fun evaluatePoliciesByLabels(labels: Set<String>, logic: LabelSearchLogicEnum, context: Context): Map<String, Pair<PolicyResultEnum, ActionResult?>>`

This method takes a set of labels, search logic, a context, and evaluates them, returning a map of policy IDs to policy
evaluation results. Search logic is an enum that can take two values:
* `ANY_OF` - if any of the labels is present in the context, the policy is evaluated
* `ALL_OF` - if all the labels are present in the context, the policy is evaluated

Policies are evaluated by descending priority order. If two policies have the same priority, they will be evaluated in
the order they were added to the catalog.

### Evaluate all policies in the catalog

`fun evaluateAllPolicies(context: Context): Map<String, Pair<PolicyResultEnum, ActionResult?>>`

This method takes a context, and evaluates all policies in the catalog, returning a map of policy IDs to policy 
evaluation results. 

Policies are evaluated by descending priority order. If two policies have the same priority, they will be evaluated in
the order they were added to the catalog.