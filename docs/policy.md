---
title: Policy
description: Introduction to Policy entities
nav_order: 2
---
# Policy
{: .no_toc }

<details markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

**Policy** defines an entity that evaluates a set of [PolicyConditions](policy-condition.md) or other Policies and returns result with possible side effects realized through [PolicyActions](policy-action.md).
PolicyAction is mutating [Context data store](context.md#stores) where items can be added, removed or modified.

It is defined by _**IPolicy**_ interface, which has the following fields:

| field                   | type                             | cardinality | description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                | 
|-------------------------|----------------------------------|-------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| constraint              | `IPolicyConditionRefOrValue`     | optional    | Represents [constraint](policy-condition.md#policy-constraints) which is checking if Policy should be evaluated or not. On PolicySet it is run only on top level, as child Policies in PolicySet have constraints on PolicyRelationship level. This is designed on purpose, as same Policy defined in two PolicySets can have different constraints.                                                                                                                                                                                                                                                                                                       |
| actions                 | ```PolicyActionRelationship[]``` | optional    | Represents list of [PolicyActions](policy-action.md) executed after Policy evaluation. PolicyActions are executed by priority, where higher number is executed first.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| actionExecutionStrategy | String                           | optional    | Represents PolicyAction execution strategy. It can be one of the following: <br/>* **runAll** - All PolicyActions are executed. If errors happen, they could be ignored or execution could stop <br/>* **untilSuccess** - Execution stops on first successful PolicyAction execution <br/>* **stopOnFailure** - Execution runs until first PolicyAction failure. Data modified up to this point in PolicyAction execution is retained <br/>* **rollbackOnFailure** - Execution runs until first PolicyAction failure. Data modified up to this point in PolicyAction execution on Policy level is removed and data store is returned to the previous state |
| lenientConstraints      | Boolean                          | optional    | If set to `true`, it will return `NotApplicable` Policy result if that particular Policy constraint returns `null`. If set to `false`, it will return `Indeterminate` result. Default value is **true**.                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| ignoreErrors            | Boolean                          | optional    | If set to `true`, it will ignore any error that happens during execution of a PolicyAction with `runAll` strategy (it applies only on this strategy). If set to `false`, it will stop PolicyAction execution for that Policy. Default value is **true**.                                                                                                                                                                                                                                                                                                                                                                                                   |
| priority                | Int                              | optional    | Used to sort selected policies that are about to be evaluated. Higher number means Policy will be evaluated sooner. If no priority is set, or Policies have same priority, evaluation is done in order policies are listed in the PolicyCatalog. This is applicable only when Policy is invoked directly, and not as a child in the PolicySet. In that case, priority in the `policies` list determines evaluation order. This is designed intentionally, as same child Policy can have different priority for different PolicySets. Default value is **0**.                                                                                               |

Interface defines two important methods:
- `evaluate()` - evaluates Policy and returns Policy result
- `runActions()` - runs PolicyAction based on evaluation result

Policy engine is entity responsible to run these two methods in correct order.


**_IPolicy_** interface implements [IManaged](managed-vs-embedded.md#imanaged-interface) interface.
The IPolicy interface has three implementations:
- [Policy](#policy-1)
- [PolicySet](#policyset)
- [PolicyDefault](#policydefault)

### PolicyResult

Policy results can be one of the following:
- **Permit** - Policy result is positive and request is granted
- **Deny** - Policy result is negative and request is denied
- **NotApplicable** - Policy is not applicable to the given context
- **Indeterminate** - Policy result is indeterminate and request is denied or granted based on other policies.
- **IndeterminatePermit** - Policy result is indeterminate, but could have evaluated to **Permit**
- **IndeterminateDeny** - Policy result is indeterminate, but could have evaluated to **Deny**

## Policy

This entity represents single rule that is evaluated over a [PolicyCondition](policy-condition.md) and applies target effect (permit or deny) if condition resolves to true.

| field              | type                         | cardinality | description                                                                                                                                                                                                                                                                                                                                                                      | 
|--------------------|------------------------------|-------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| targetEffect       | String                       | mandatory   | Defines Policy result when underlying PolicyCondition resolves to `true`. It can have two possible values: <br/>* **permit** <br/>* **deny**                                                                                                                                                                                                                                     | 
| condition          | `IPolicyConditionRefOrValue` | mandatory   | Defines underlying PolicyCondition. It can be a reference to the managed PolicyCondition or an embedded one.                                                                                                                                                                                                                                                                     |
| strictTargetEffect | Boolean                      | optional    | Defines behaviour when underlying PolicyCondition resolves to **false**. <br/>If this flag is set to `true`, Policy with targetEffect `permit` will return `deny` as a result, and Policy with targetEffect `deny` will return `permit` as a result. <br/>If this flag is set to `false`, Policy result in that case will always be `notApplicable`. Default value is **false**. |

When Policy evaluation is run, following steps are taken:
- If Policy is a managed one, it is checked if PolicyResult already exists in the cache. If it exists, PolicyResult is returned
- If there is a constraint defined in Policy, it is checked. Depending on result of this check, `notApplicable` or `indeterminate` result can be returned immediately. More details in constraints content [below](#constraints).
- PolicyCondition is checked. If condition result is:
  - `true` - then defined targetEffect is returned
  - `false` - returns opposite targetEffect or `notApplicable`, depending on _strictTargetEffect_ flag
  - `null` - returns `indeterminatePermit` if targetEffect is set to `permit` or `indeterminateDeny` if targetEffect is set to `deny`
- PolicyResult is put to cache if Policy is a managed one

#### Examples

**Policy with permit targetEffect and PolicyConditionRef**

```json
{
    "targetEffect": "permit",
    "condition": {
        "id": "polCond1",
        "refType": "PolicyConditionRef"
    }
}
```

**Policy with deny targetEffect and embedded PolicyCondition**

```json
{
    "constraint": {
        "id": "customerScoringEvent",
        "refType": "PolicyConditionRef"
    },
    "actions": [
        {
            "executionMode": [
                "onDeny"
            ],
            "action": {
                "id": "setCustomerAsMinor",
                "refType": "PolicyActionRef"
            }
        }
    ],
    "actionExecutionStrategy": "runAll",
    "ignoreErrors": false,
    "targetEffect": "deny",
    "condition": {
        "operation": "LessThan",
        "args": [
            {
                "resolvers": [
                    {
                        "id": "birthdayResolver",
                        "refType": "PolicyVariableResolverRef"
                    }
                ]
            },
            {
                "type": "int",
                "value": 18
            }
        ]
    }
}
```

**Managed Policy**

```json
{
    "id": "isCustomerMinor",
    "version": "1.0.0",
    "description": "Denys customers below 18 years old",
    "labels": [
        "customer",
        "scoring"
    ],
    "constraint": {
        "id": "customerScoringEvent",
        "refType": "PolicyConditionRef"
    },
    "actions": [
        {
            "executionMode": [
                "onDeny"
            ],
            "action": {
                "id": "setCustomerAsMinor",
                "refType": "PolicyActionRef"
            }
        }
    ],
    "actionExecutionStrategy": "runAll",
    "ignoreErrors": false,
    "targetEffect": "deny",
    "condition": {
        "operation": "LessThan",
        "args": [
            {
                "resolvers": [
                    {
                        "id": "birthdayResolver",
                        "refType": "PolicyVariableResolverRef"
                    }
                ]
            },
            {
                "type": "int",
                "value": 18
            }
        ]
    }
}
```

## PolicySet

Represents a collection of individual policies that are evaluated together using a specified policy combination logic.

| field                     | type                                        | cardinality | description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            | 
|---------------------------|---------------------------------------------|-------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| policyCombinationLogic    | String                                      | mandatory   | Defines Policy combination logic. It can be one of the following: <br/>* **denyOverrides** <br/>* **permitOverrides** <br/>* **denyUnlessPermit** <br/>* **permitUnlessDeny** <br/>* **firstApplicable** <br/>* **onlyOneApplicable** <br/> Detailed description below.                                                                                                                                                                                                                                                | 
| policies                  | [PolicyRelationship](#policyrelationship)[] | mandatory   | List of Policies, PolicySets or PolicyRefs that are run together with relationship metadata. Cannot be empty.                                                                                                                                                                                                                                                                                                                                                                                                          |
| runChildActions           | Boolean                                     | optional    | Flag that determines the default behavior for whether child actions should be run when executing a PolicySet. If `true`, child actions will be run by default when executing the PolicySet. If `false`, child actions will not be run by default. If this flag is set to `true`, some Policies can still be skipped when running actions by setting PolicyRelationship level `runAction` flag to `false`. Default value is **false**                                                                                   |
| strictUnlessLogic         | Boolean                                     | optional    | Applicable only on `permitUnlessDeny` and `denyUnlessPermit` combination logic. If set to `true`, it will return `indeterminate` result if any of the underlying Policies returns something other than `permit` or `deny` result.<br/> If set to `false` it will return only `permit`  or `deny` as a result. Details are described below. Default value is **false**                                                                                                                                                  |
| indeterminateOnActionFail | Boolean                                     | optional    | Defines what happens if actions on child Policies fail. If it is set to `true`, when child Policy action fails, it will change Policy result to `indeterminate`. <br/>If set to `false` it will not modify Policy result. Default value is **false**                                                                                                                                                                                                                                                                   |
| skipCache                 | Boolean                                     | optional    | Defines that cache should be skipped on this PolicySet. Reason for this is that for PolicySet child actions are run immediately after particular child Policy evaluation. If cache is hit on parent PolicySet, then actions on child Policies will not be run again. They should be run first time this PolicySet was invoked and put to cache. This flag could potentially lead to undesired results, if there are multiple levels of PolicySets, each with different skipCache flags set. Default value is **false** |

When PolicySet evaluation is run, following steps are taken:
- If PolicySet is a managed one, it is checked if PolicyResult already exists in the cache. If it exists, PolicyResult is returned. Important to note here is that PolicyAction defined on PolicySet will still be run (as that is orchestrated by the engine), but child Policy actions will not be (as that is orchestrated by evaluate method). This behaviour can be changed by setting `skipCache` property to `true`. In that case, cache will be skipped for this PolicySet and will invoke evaluation and action running on all underlying Policies.
- If there is a constraint defined in PolicySet, it is checked. Depending on result of this check, `notApplicable` or `indeterminate` result can be returned immediately. More details in constraints content [below](#constraints).
- Depending on defined PolicyCombinationLogic, specific logic is invoked. As soon as result of child Policy is returned, action of that child policy is executed.
- PolicyResult is put to cache if PolicySet is a managed one

### PolicyCombinationLogic

##### DenyOverrides

This logic evaluates a set of policies and determines the overall policy result based on following logic:
- as soon as there is first Policy that returns `deny`, it breaks the loop and returns `deny` as a final result
- if there was any `indeterminate` result, it will return `indeterminate`
- if there was any `indeterminateDeny` and (any `indeterminatePermit` or  `permit`) results, it will return `indeterminate`
- if there was any `indeterminateDeny` it will return `indeterminateDeny`
- if there was any `permit` it will return `permit`
- if there was any `indeterminatePermit` it will return `indeterminatePermit`
- otherwise it will return `notApplicable`

Successful execution returns `deny`

##### PermitOverrides

This logic evaluates a set of policies and determines the overall policy result based on following logic:
- as soon as there is first Policy that returns `permit`, it breaks the loop and returns `permit` as a final result
- if there was any `indeterminate` result, it will return `indeterminate`
- if there was any `indeterminatePermit` and (any `indeterminateDeny` or  `deny`) results, it will return `indeterminate`
- if there was any `indeterminatePermit` it will return `indeterminatePermit`
- if there was any `deny` it will return `deny`
- if there was any `indeterminateDeny` it will return `indeterminateDeny`
- otherwise it will return `notApplicable`

Successful execution returns `permit`

##### DenyUnlessPermit

This logic evaluates a set of policies and determines the overall policy result based on following logic:
- as soon as there is first Policy that returns `permit`, it breaks the loop and returns `permit` as a final result
- if there was any `indeterminate` or `notApplicable` result and strictUnlessLogic flag is set to `true`, it will return `indeterminate`. Additionally, if strictUnlessLogic is set to true, first result that is not `deny` or `permit` will break the loop.
- otherwise it will return `deny`

Successful execution returns `deny`

##### PermitUnlessDeny

This logic evaluates a set of policies and determines the overall policy result based on following logic:
- as soon as there is first Policy that returns `deny`, it breaks the loop and returns `deny` as a final result
- if there was any `indeterminate` or `notApplicable` result and strictUnlessLogic flag is set to `true`, it will return `indeterminate`. Additionally, if strictUnlessLogic is set to true, first result that is not `deny` or `permit` will break the loop.
- otherwise it will return `permit`

Successful execution returns `permit`

##### FirstApplicable

This logic evaluates a set of policies and determines the overall policy result based on following logic:
- as soon as there is first Policy that returns `deny` or `permit`, it breaks the loop and returns result of that child Policy as a final result
- if there was any `indeterminate` result, it will return `indeterminate`
- otherwise it will return `notApplicable`

Successful execution returns `permit` or `deny`
##### OnlyOneApplicable

This logic evaluates a set of policies and determines the overall policy result based on following logic:
- as soon as there is first Policy that returns `deny` or `permit`, it internally sets applicable counter to 1 and continues the loop. If in the end there was only one applicable result, it will return that result as the final one.
- if there was any additional `deny` or `permit` result, it will break the loop and return `indeterminate`
- if there was any `indeterminate` result, it will return `indeterminate`
- otherwise it will return `notApplicable`

Successful execution returns `permit` or `deny`

### Examples

**PolicySet with managed Policies**

```json
{
    "policyCombinationLogic": "permitUnlessDeny",
    "policies": [
        {
            "policy": {
                "id": "isCustomerMinor",
                "refType": "PolicyRef"
            }
        },
        {
            "policy": {
                "id": "isCustomerInFraudList",
                "refType": "PolicyRef"
            }
        }
    ]
}
```

**Managed PolicySet**

```json
{
    "id": "isScoringPositive",
    "version": "1.0.0",
    "description": "Denys customers below 18 years old",
    "labels": [
        "customer",
        "scoring"
    ],
    "policyCombinationLogic": "permitUnlessDeny",
    "policies": [
        {
            "policy": {
                "id": "isCustomerMinor",
                "refType": "PolicyRef"
            }
        },
        {
            "policy": {
                "id": "isCustomerInFraudList",
                "refType": "PolicyRef"
            }
        }
    ]
}
```

## PolicyDefault

Represents a default Policy that always returns a specified [PolicyResult](#policyresult). It can be used as a fallback Policy or in tests. As result is static, it is not cached. It can be used as embedded or as a reference, where id in such reference is one of the following:
- `$permit`
- `$deny`
- `$indeterminateDeny`
- `$indeterminatePermit`
- `$indeterminate`
- `$notApplicable`

| field   | type   | cardinality | description               | 
|---------|--------|-------------|---------------------------|
| default | String | mandatory   | Static PolicyResult value |

**Permit PolicyDefault**

```json
{
    "default": "permit",
    "id": "$permit"
}
```

**Deny PolicyDefault**

```json
{
    "default": "deny",
    "id": "$deny"
}
```

**IndeterminatePermit PolicyDefault**

```json
{
    "default": "indeterminatePermit",
    "id": "$indeterminatePermit"
}
```

**IndeterminateDeny PolicyDefault**

```json
{
    "default": "indeterminateDeny",
    "id": "$indeterminateDeny"
}
```

**Indeterminate PolicyDefault**

```json
{
    "default": "indeterminate",
    "id": "$indeterminate"
}
```

**NotApplicable PolicyDefault**

```json
{
    "default": "notApplicable",
    "id": "$notApplicable"
}
```

**PolicyRef to premit PolicyDefault**

```json
{
    "id": "$permit",
    "refType": "PolicyRef"
}
```

## Constraints

If Policy or PolicySet is invoked directly or as a filtered Policy list (when evaluating Policies by label or when evaluating all Policies in the catalog), constraints are checked. Constraint are ensuring that Policies that shouldn't be run for a specific context are skipped. 
Usually constraints (which are basically PolicyConditions) are defined over a context stores and can return `false` result if context input doesn't contain certain values.

This means one [PolicyCatalog](policy-catalog.md) can contain different Policies that can be run independently based on provided context, as Policies that are not related to a given context will be skipped.

`lenientConstraint` flag is additionally driving this behaviour by allowing result of such Policy to be either `notApplicable` if set to true, or `indeterminate` if set to false.

## PolicyActionRelationship

Represents a relationship between a Policy and a PolicyAction, including any constraints or execution modes.

| field         | type                         | cardinality | description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              | 
|---------------|------------------------------|-------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| constraint    | `IPolicyConditionRefOrValue` | optional    | Determines should this PolicyAction be executed or not. More details in [constraints](#constraints) chapter                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| executionMode | String[]                     | optional    | Determines on which PolicyResults should this Action be executed. Possible values are: <br/>* **onPermit** <br/>* **onDeny** <br/>* **onIndeterminate**<br/>* **onNotApplicable** <br/>It is possible to set multiple execution modes. If there is no executionMode in the list, action will be run if PolicyResult is deemed successful. <br/>Policy is deemed successful if PolicyResult aligns with target effect. <br/>PolicySet is deemed successful depending on description in combination logic. <br/>PolicyDefault is always deemed successful. |
| priority      | Int                          | optional    | Determines when this PolicyAction should be run relative to other policy actions in the list. Higher value means it will be run sooner. If two or more policy action relationships have same priority value, they will be run in order they are positioned in the list. Default value is **0**.                                                                                                                                                                                                                                                          |
| action        | `IPolicyActionRefOrValue`    | mandatory   | Reference to managed [PolicyAction](policy-action.md) or the embedded one.                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |

## PolicyRelationship

Represents a relationship between a PolicySet and a child Policy, including any constraints or execution modes.

| field      | type                         | cardinality | description                                                                                                                                                                                                                                                                                         | 
|------------|------------------------------|-------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| constraint | `IPolicyConditionRefOrValue` | optional    | Determines should this child Policy be executed or not. More details in [constraints](#constraints) chapter                                                                                                                                                                                         |
| runAction  | Boolean                      | optional    | Determines should PolicyAction on this child Policy execute or not. This is per Policy flag that can be turn on or off on parent PolicySet level with `runChildActions` flag. Default value is **true**.                                                                                            |
| priority   | Int                          | optional    | Determines when this particular policy should be evaluated relative to other policies in the list. Higher value means it will be evaluated sooner. If two or more policy relationships have same priority value, they will be run in order they are positioned in the list. Default value is **0**. |
| policy     | `IPolicyRefOrValue`          | mandatory   | Reference to managed policy or the embedded one.                                                                                                                                                                                                                                                    |

## PolicyRef

PolicyRef is entity that references a Policy (_Policy_, _PolicySet_ or _PolicyDefault_). It contains following fields:

| field   | type   | cardinality | description                                                                                                |
|---------|--------|-------------|------------------------------------------------------------------------------------------------------------|
| id      | String | mandatory   | id of the managed Policy                                                                                   |
| version | String | optional    | SemVer of the referenced Policy. If it is left blank, latest version of a Policy will be populated         |
| refType | String | mandatory   | discriminator that is used when deserializing catalog from JSON file. It has constant value of `PolicyRef` |
