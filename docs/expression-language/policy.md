---
title: Policy
parent: Expression Language
description: Policy Expression Language commands
nav_order: 3
---
# Policy expressions
{: .no_toc }

<details open markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

Policy expressions are used to reference Policies. They can return Policy, PolicySet and PolicyDefault entities.
Parser command to be invoked is `PEELParser(str).parsePolicy()` and it returns IPolicy entity.
This parser also parses Policy related entities: PolicyConstraint, PolicyRelationship and PolicyActionRelationship.

PolicyConstraint must always be put inside PolicyConstraint command. <br>
PolicyRelationship and PolicyActionRelationship can be put in main Policy command, unless they need to have some additional relationship parameter or if they are defined as Reference.

Warning
{: .label .label-red }
As parameters of Policy expressions can contain references toward PolicyActions and Policies, if PolicyActionRef needs to be defined, it should be put in PolicyActionRelationship command.

This will be parsed as PolicyRef with id equals to `id1` and another PolicyRef with id equals to `id2`:

```
*permit(#ref(id1),#ref(id2))
```

This will be parsed as PolicyRef with id equals to `id1` and PolicyActionRef with id equal to `id2`:

```
*permit(#ref(id1),*act(#ref(id2)))
```

## Command table

| Command     | Type                     | format                                                                                                           | options                                                                                                                                                               |
|-------------|--------------------------|------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| *permit     | Policy                   | \*permit(\<PolicyCondition\|Reference\>{1,1},\<PolicyAction\|PolicyActionRelationship\>{0,},*constraint?,#opts?) | lenientConstraints(\*), actionExecutionStrategy, ignoreErrors(\*), priority, strictTargetEffect(\*)                                                                   |
| *deny       | Policy                   | \*deny(\<PolicyCondition\|Reference\>{1,1},\<PolicyAction\|PolicyActionRelationship\>{0,},*constraint?,#opts?)   | lenientConstraints(\*), actionExecutionStrategy, ignoreErrors(\*), priority, strictTargetEffect(\*)                                                                   |
| *DOverrides | PolicySet                | \*DOverrides(\<Policy\|Reference\>{1,},\<PolicyAction\|PolicyActionRelationship\>{0,},*constraint?,#opts?)       | lenientConstraints(\*), actionExecutionStrategy, ignoreErrors(\*), priority, skipCache(\*), runChildActions(\*), indeterminateOnActionFail(\*)                        |
| *POverrides | PolicySet                | \*POverrides(\<Policy\|Reference\>{1,},\<PolicyAction\|PolicyActionRelationship\>{0,},*constraint?,#opts?)       | lenientConstraints(\*), actionExecutionStrategy, ignoreErrors(\*), priority, skipCache(\*), runChildActions(\*), indeterminateOnActionFail(\*)                        |
| *DUnlessP   | PolicySet                | \*DUnlessP(\<Policy\|Reference\>{1,},\<PolicyAction\|PolicyActionRelationship\>{0,},*constraint?,#opts?)         | lenientConstraints(\*), actionExecutionStrategy, ignoreErrors(\*), priority, skipCache(\*), runChildActions(\*), indeterminateOnActionFail(\*), strictUnlessLogic(\*) |
| *PUnlessD   | PolicySet                | \*PUnlessD(\<Policy\|Reference\>{1,},\<PolicyAction\|PolicyActionRelationship\>{0,},*constraint?,#opts?)         | lenientConstraints(\*), actionExecutionStrategy, ignoreErrors(\*), priority, skipCache(\*), runChildActions(\*), indeterminateOnActionFail(\*), strictUnlessLogic(\*) |
| *firstAppl  | PolicySet                | \*firstAppl(\<Policy\|Reference\>{1,},\<PolicyAction\|PolicyActionRelationship\>{0,},*constraint?,#opts?)        | lenientConstraints(\*), actionExecutionStrategy, ignoreErrors(\*), priority, skipCache(\*), runChildActions(\*), indeterminateOnActionFail(\*)                        |
| #permit     | PolicyDefault            | #permit(\<PolicyAction\|PolicyActionRelationship\>{0,},*constraint?,#opts?)                                      | lenientConstraints(\*), actionExecutionStrategy, ignoreErrors(\*), priority                                                                                           |
| #deny       | PolicyDefault            | #deny(\<PolicyAction\|PolicyActionRelationship\>{0,},*constraint?,#opts?)                                        | lenientConstraints(\*), actionExecutionStrategy, ignoreErrors(\*), priority                                                                                           |
| #NA         | PolicyDefault            | #NA(\<PolicyAction\|PolicyActionRelationship\>{0,},*constraint?,#opts?)                                          | lenientConstraints(\*), actionExecutionStrategy, ignoreErrors(\*), priority                                                                                           |
| #indDP      | PolicyDefault            | #indDP(\<PolicyAction\|PolicyActionRelationship\>{0,},*constraint?,#opts?)                                       | lenientConstraints(\*), actionExecutionStrategy, ignoreErrors(\*), priority                                                                                           |
| #indD       | PolicyDefault            | #indD(\<PolicyAction\|PolicyActionRelationship\>{0,},*constraint?,#opts?)                                        | lenientConstraints(\*), actionExecutionStrategy, ignoreErrors(\*), priority                                                                                           |
| #indP       | PolicyDefault            | #indP(\<PolicyAction\|PolicyActionRelationship\>{0,},*constraint?,#opts?)                                        | lenientConstraints(\*), actionExecutionStrategy, ignoreErrors(\*), priority                                                                                           |
| *constraint | PolicyConstraint         | \*constraint(\<PolicyCondition\|Reference\>{1,1})                                                                |                                                                                                                                                                       |
| *act        | PolicyActionRelationship | \*act(\<PolicyAction\|Reference\>{1,1},*constraint?,#opts?)                                                      | executionMode, priority                                                                                                                                               |
| *pol        | PolicyRelationship       | \*pol(\<Policy\|Reference\>{1,1},*constraint?,#opts?)                                                            | runAction(\*), priority                                                                                                                                               |

(\*) - Boolean condition <br>

## Policy

Policy expressions can only contain content that is deserialized to PolicyCondition or Reference entity.

### Permit

Template: ```*permit(<PolicyCondition|Reference>{1,1},<PolicyAction|PolicyActionRelationship>{0,},*constraint?,#opts?)``` <br>
Command starts with `*permit` <br>
It can contain:<br>
- exactly one parameter of type PolicyCondition or Reference that represents `condition` field
- optional parameters of type PolicyAction or PolicyActionRelationship 
- one optional parameter of type PolicyConstraint

Supported options:
- `lenientConstraints` - boolean, default value is null. Will return `NotApplicable` result if condition returns null
- `actionExecutionStrategy` - String enum, defined in [Policy](../policy.md) page. 
- `ignoreErrors` - boolean, default value is null. On true will ignore any error that happens during execution of a PolicyAction with runAll strategy
- `priority` - integer, default value is null. Sets Policy priority
- `strictTargetEffect` - boolean, default value is null. Defines behaviour when underlying PolicyCondition resolves to false.

Minimal example:
```
*permit(#ref(cond1))
```

Full example with options, constraint and actions:
```
*permit(#ref(cond1),*act(#ref(pas1)),*save(foo,#str(bar)),*constraint(*gt(#ref(var1), #int(2))),#opts(lenientConstraints,ignoreErrors,priority=10,actionExecutionStrategy=untilSuccess,strictTargetEffect))
```

### Deny

Template: ```*deny(<PolicyCondition|Reference>{1,1},<PolicyAction|PolicyActionRelationship>{0,},*constraint?,#opts?)``` <br>
Command starts with `*deny` <br>
It can contain:<br>
- exactly one parameter of type PolicyCondition or Reference that represents `condition` field
- optional parameters of type PolicyAction or PolicyActionRelationship
- one optional parameter of type PolicyConstraint

Supported options:
- `lenientConstraints` - boolean, default value is null. Will return `NotApplicable` result if condition returns null
- `actionExecutionStrategy` - String enum, defined in [Policy](../policy.md) page.
- `ignoreErrors` - boolean, default value is null. On true will ignore any error that happens during execution of a PolicyAction with runAll strategy
- `priority` - integer, default value is null. Sets Policy priority
- `strictTargetEffect` - boolean, default value is null. Defines behaviour when underlying PolicyCondition resolves to false.

Minimal example:
```
*deny(#ref(cond1))
```

Full example with options, constraint and actions:
```
*deny(#ref(cond1),*act(#ref(pas1)),*save(foo,#str(bar)),*constraint(*gt(#ref(var1), #int(2))),#opts(lenientConstraints,ignoreErrors,priority=10,actionExecutionStrategy=untilSuccess,strictTargetEffect))
```

## PolicySet

PolicySet expressions can only contain content that is deserialized to Policy or Reference entity.

### DenyOverrides

Template: ```*DOverrides(<Policy|Reference>{1,},<PolicyAction|PolicyActionRelationship>{0,},*constraint?,#opts?)``` <br>
Command starts with `*DOverrides` <br>
It can contain:<br>
- one or more parameters of type Policy or Reference that represents `policies` field
- optional parameters of type PolicyAction or PolicyActionRelationship
- one optional parameter of type PolicyConstraint

Supported options:
- `lenientConstraints` - boolean, default value is null. Will return `NotApplicable` result if condition returns null
- `actionExecutionStrategy` - String enum, defined in [Policy](../policy.md) page.
- `ignoreErrors` - boolean, default value is null. On true will ignore any error that happens during execution of a PolicyAction with runAll strategy
- `priority` - integer, default value is null. Sets Policy priority
- `skipCache` - boolean, default value is null. Will skip cache if set to true.
- `runChildActions` - boolean, default value is null. Will run child Policy actions if set to true.
- `indeterminateOnActionFail` - boolean, default value is null. Will set result to indeterminate if actions fail when set to true.

Minimal example:
```
*DOverrides(#ref(pol1))
```

Full example with options, constraint and actions:
```
*DOverrides(#ref(pol1),*act(#ref(pas1)),*save(foo,#str(bar)),*constraint(*gt(#ref(var1), #int(2))),#opts(id=pol,ver=1.2.3,desc="some desc",labels=a,lenientConstraints,actionExecutionStrategy=untilSuccess,ignoreErrors,priority=10,skipCache,runChildActions,indeterminateOnActionFail))
```

### PermitOverrides

Template: ```*POverrides(<Policy|Reference>{1,},<PolicyAction|PolicyActionRelationship>{0,},*constraint?,#opts?)``` <br>
Command starts with `*POverrides` <br>
It can contain:<br>
- one or more parameters of type Policy or Reference that represents `policies` field
- optional parameters of type PolicyAction or PolicyActionRelationship
- one optional parameter of type PolicyConstraint

Supported options:
- `lenientConstraints` - boolean, default value is null. Will return `NotApplicable` result if condition returns null
- `actionExecutionStrategy` - String enum, defined in [Policy](../policy.md) page.
- `ignoreErrors` - boolean, default value is null. On true will ignore any error that happens during execution of a PolicyAction with runAll strategy
- `priority` - integer, default value is null. Sets Policy priority
- `skipCache` - boolean, default value is null. Will skip cache if set to true.
- `runChildActions` - boolean, default value is null. Will run child Policy actions if set to true.
- `indeterminateOnActionFail` - boolean, default value is null. Will set result to indeterminate if actions fail when set to true.

Minimal example:
```
*POverrides(#ref(pol1))
```

Full example with options, constraint and actions:
```
*POverrides(#ref(pol1),*act(#ref(pas1)),*save(foo,#str(bar)),*constraint(*gt(#ref(var1), #int(2))),#opts(id=pol,ver=1.2.3,desc="some desc",labels=a,lenientConstraints,actionExecutionStrategy=untilSuccess,ignoreErrors,priority=10,skipCache,runChildActions,indeterminateOnActionFail))
```
### DenyUnlessPermit

Template: ```*DUnlessP(<Policy|Reference>{1,},<PolicyAction|PolicyActionRelationship>{0,},*constraint?,#opts?)``` <br>
Command starts with `*DUnlessP` <br>
It can contain:<br>
- one or more parameters of type Policy or Reference that represents `policies` field
- optional parameters of type PolicyAction or PolicyActionRelationship
- one optional parameter of type PolicyConstraint

Supported options:
- `lenientConstraints` - boolean, default value is null. Will return `NotApplicable` result if condition returns null
- `actionExecutionStrategy` - String enum, defined in [Policy](../policy.md) page.
- `ignoreErrors` - boolean, default value is null. On true will ignore any error that happens during execution of a PolicyAction with runAll strategy
- `priority` - integer, default value is null. Sets Policy priority
- `skipCache` - boolean, default value is null. Will skip cache if set to true.
- `runChildActions` - boolean, default value is null. Will run child Policy actions if set to true.
- `indeterminateOnActionFail` - boolean, default value is null. Will set result to indeterminate if actions fail when set to true.
- `strictUnlessLogic` - boolean, default value is null. Will set result to indeterminate if any of the underlying policies resolve to result different from permit or deny.

Minimal example:
```
*DUnlessP(#ref(pol1))
```

Full example with options, constraint and actions:
```
*DUnlessP(#ref(pol1),*act(#ref(pas1)),*save(foo,#str(bar)),*constraint(*gt(#ref(var1), #int(2))),#opts(id=pol,ver=1.2.3,desc="some desc",labels=a,lenientConstraints,actionExecutionStrategy=untilSuccess,ignoreErrors,priority=10,skipCache,runChildActions,indeterminateOnActionFail,strictUnlessLogic))
```

### PermitUnlessDeny

Template: ```*PUnlessD(<Policy|Reference>{1,},<PolicyAction|PolicyActionRelationship>{0,},*constraint?,#opts?)``` <br>
Command starts with `*PUnlessD` <br>
It can contain:<br>
- one or more parameters of type Policy or Reference that represents `policies` field
- optional parameters of type PolicyAction or PolicyActionRelationship
- one optional parameter of type PolicyConstraint

Supported options:
- `lenientConstraints` - boolean, default value is null. Will return `NotApplicable` result if condition returns null
- `actionExecutionStrategy` - String enum, defined in [Policy](../policy.md) page.
- `ignoreErrors` - boolean, default value is null. On true will ignore any error that happens during execution of a PolicyAction with runAll strategy
- `priority` - integer, default value is null. Sets Policy priority
- `skipCache` - boolean, default value is null. Will skip cache if set to true.
- `runChildActions` - boolean, default value is null. Will run child Policy actions if set to true.
- `indeterminateOnActionFail` - boolean, default value is null. Will set result to indeterminate if actions fail when set to true.
- `strictUnlessLogic` - boolean, default value is null. Will set result to indeterminate if any of the underlying policies resolve to result different from permit or deny.

Minimal example:
```
*PUnlessD(#ref(pol1))
```

Full example with options, constraint and actions:
```
*PUnlessD(#ref(pol1),*act(#ref(pas1)),*save(foo,#str(bar)),*constraint(*gt(#ref(var1), #int(2))),#opts(id=pol,ver=1.2.3,desc="some desc",labels=a,lenientConstraints,actionExecutionStrategy=untilSuccess,ignoreErrors,priority=10,skipCache,runChildActions,indeterminateOnActionFail,strictUnlessLogic))
```

### FirstApplicable

Template: ```*firstAppl(<Policy|Reference>{1,},<PolicyAction|PolicyActionRelationship>{0,},*constraint?,#opts?)``` <br>
Command starts with `*firstAppl` <br>
It can contain:<br>
- one or more parameters of type Policy or Reference that represents `policies` field
- optional parameters of type PolicyAction or PolicyActionRelationship
- one optional parameter of type PolicyConstraint

Supported options:
- `lenientConstraints` - boolean, default value is null. Will return `NotApplicable` result if condition returns null
- `actionExecutionStrategy` - String enum, defined in [Policy](../policy.md) page.
- `ignoreErrors` - boolean, default value is null. On true will ignore any error that happens during execution of a PolicyAction with runAll strategy
- `priority` - integer, default value is null. Sets Policy priority
- `skipCache` - boolean, default value is null. Will skip cache if set to true.
- `runChildActions` - boolean, default value is null. Will run child Policy actions if set to true.
- `indeterminateOnActionFail` - boolean, default value is null. Will set result to indeterminate if actions fail when set to true.

Minimal example:
```
*firstAppl(#ref(pol1))
```

Full example with options, constraint and actions:
```
*firstAppl(#ref(pol1),*act(#ref(pas1)),*save(foo,#str(bar)),*constraint(*gt(#ref(var1), #int(2))),#opts(id=pol,ver=1.2.3,desc="some desc",labels=a,lenientConstraints,actionExecutionStrategy=untilSuccess,ignoreErrors,priority=10,skipCache,runChildActions,indeterminateOnActionFail))
```

## PolicyDefault

PolicyDefault expressions can not have any content. They can only have constraints and PolicyActions.

### Permit

Template: ```#permit(<PolicyAction|PolicyActionRelationship>{0,},*constraint?,#opts?)``` <br>
Command starts with `#permit` <br>
It can contain:<br>
- optional parameters of type PolicyAction or PolicyActionRelationship
- one optional parameter of type PolicyConstraint

Supported options:
- `lenientConstraints` - boolean, default value is null. Will return `NotApplicable` result if condition returns null
- `actionExecutionStrategy` - String enum, defined in [Policy](../policy.md) page.
- `ignoreErrors` - boolean, default value is null. On true will ignore any error that happens during execution of a PolicyAction with runAll strategy
- `priority` - integer, default value is null. Sets Policy priority

Minimal example:
```
#permit()
```

Full example with options, constraint and actions:
```
#permit(*act(#ref(pas1)),*save(foo,#str(bar)),*constraint(*gt(#ref(var1), #int(2))),#opts(lenientConstraints,ignoreErrors,priority=10,actionExecutionStrategy=untilSuccess))
```

### Deny

Template: ```#deny(<PolicyAction|PolicyActionRelationship>{0,},*constraint?,#opts?)``` <br>
Command starts with `#deny` <br>
It can contain:<br>
- optional parameters of type PolicyAction or PolicyActionRelationship
- one optional parameter of type PolicyConstraint

Supported options:
- `lenientConstraints` - boolean, default value is null. Will return `NotApplicable` result if condition returns null
- `actionExecutionStrategy` - String enum, defined in [Policy](../policy.md) page.
- `ignoreErrors` - boolean, default value is null. On true will ignore any error that happens during execution of a PolicyAction with runAll strategy
- `priority` - integer, default value is null. Sets Policy priority

Minimal example:
```
#deny()
```

Full example with options, constraint and actions:
```
#deny(*act(#ref(pas1)),*save(foo,#str(bar)),*constraint(*gt(#ref(var1), #int(2))),#opts(lenientConstraints,ignoreErrors,priority=10,actionExecutionStrategy=untilSuccess))
```

### NotApplicable

Template: ```#NA(<PolicyAction|PolicyActionRelationship>{0,},*constraint?,#opts?)``` <br>
Command starts with `#NA` <br>
It can contain:<br>
- optional parameters of type PolicyAction or PolicyActionRelationship
- one optional parameter of type PolicyConstraint

Supported options:
- `lenientConstraints` - boolean, default value is null. Will return `NotApplicable` result if condition returns null
- `actionExecutionStrategy` - String enum, defined in [Policy](../policy.md) page.
- `ignoreErrors` - boolean, default value is null. On true will ignore any error that happens during execution of a PolicyAction with runAll strategy
- `priority` - integer, default value is null. Sets Policy priority

Minimal example:
```
#NA()
```

Full example with options, constraint and actions:
```
#NA(*act(#ref(pas1)),*save(foo,#str(bar)),*constraint(*gt(#ref(var1), #int(2))),#opts(lenientConstraints,ignoreErrors,priority=10,actionExecutionStrategy=untilSuccess))
```

### Indeterminate

Template: ```#indDP(<PolicyAction|PolicyActionRelationship>{0,},*constraint?,#opts?)``` <br>
Command starts with `#indDP` <br>
It can contain:<br>
- optional parameters of type PolicyAction or PolicyActionRelationship
- one optional parameter of type PolicyConstraint

Supported options:
- `lenientConstraints` - boolean, default value is null. Will return `NotApplicable` result if condition returns null
- `actionExecutionStrategy` - String enum, defined in [Policy](../policy.md) page.
- `ignoreErrors` - boolean, default value is null. On true will ignore any error that happens during execution of a PolicyAction with runAll strategy
- `priority` - integer, default value is null. Sets Policy priority

Minimal example:
```
#indDP()
```

Full example with options, constraint and actions:
```
#indDP(*act(#ref(pas1)),*save(foo,#str(bar)),*constraint(*gt(#ref(var1), #int(2))),#opts(lenientConstraints,ignoreErrors,priority=10,actionExecutionStrategy=untilSuccess))
```

### IndeterminateDeny

Template: ```#indD(<PolicyAction|PolicyActionRelationship>{0,},*constraint?,#opts?)``` <br>
Command starts with `#indD` <br>
It can contain:<br>
- optional parameters of type PolicyAction or PolicyActionRelationship
- one optional parameter of type PolicyConstraint

Supported options:
- `lenientConstraints` - boolean, default value is null. Will return `NotApplicable` result if condition returns null
- `actionExecutionStrategy` - String enum, defined in [Policy](../policy.md) page.
- `ignoreErrors` - boolean, default value is null. On true will ignore any error that happens during execution of a PolicyAction with runAll strategy
- `priority` - integer, default value is null. Sets Policy priority

Minimal example:
```
#indD()
```

Full example with options, constraint and actions:
```
#indD(*act(#ref(pas1)),*save(foo,#str(bar)),*constraint(*gt(#ref(var1), #int(2))),#opts(lenientConstraints,ignoreErrors,priority=10,actionExecutionStrategy=untilSuccess))
```

### IndeterminatePermit

Template: ```#indP(<PolicyAction|PolicyActionRelationship>{0,},*constraint?,#opts?)``` <br>
Command starts with `#indP` <br>
It can contain:<br>
- optional parameters of type PolicyAction or PolicyActionRelationship
- one optional parameter of type PolicyConstraint

Supported options:
- `lenientConstraints` - boolean, default value is null. Will return `NotApplicable` result if condition returns null
- `actionExecutionStrategy` - String enum, defined in [Policy](../policy.md) page.
- `ignoreErrors` - boolean, default value is null. On true will ignore any error that happens during execution of a PolicyAction with runAll strategy
- `priority` - integer, default value is null. Sets Policy priority

Minimal example:
```
#indP()
```

Full example with options, constraint and actions:
```
#indP(*act(#ref(pas1)),*save(foo,#str(bar)),*constraint(*gt(#ref(var1), #int(2))),#opts(lenientConstraints,ignoreErrors,priority=10,actionExecutionStrategy=untilSuccess))
```

## PolicyConstraint

PolicyConstraint expressions is a wrapper around PolicyCondition or Reference. It is never deserialized directly, only as a part of a Policy, PolicyActionRelationship and PolicyRelationship.

Template: ```*constraint(<PolicyCondition|Reference>{1,1})``` <br>
Command starts with `*constraint` <br>
It can contain:<br>
- one parameter of type PolicyCondition or Reference

Supported options:
- none

Example on PolicyDefault with Reference:
```
#permit(*constraint(#ref(pca1)))
```

Example on PolicyDefault with PolicyConditionAtomic:
```
permit(*constraint(*gt(#int(1), #int(2))))
```

## PolicyRelationship

PolicyRelationship expressions is a wrapper around Policy or Reference. It is never deserialized directly, only as a part of a PolicySet. If used without a wrapper (as a Policy or PolicySet), then underlying Policy/PolicySet will be put inside PolicyRelationship without any additional parameters.

Template: ```*pol(<Policy|Reference>{1,1},*constraint?,#opts?)``` <br>
Command starts with `*pol` <br>
It can contain:<br>
- one parameter of type Policy/PolicySet or Reference
- one optional parameter of type PolicyConstraint

Supported options:
- `priority` - integer, default value is null. Sets Policy priority
- `runAction` - boolean, default value is null. Will run related Policy actions if set to true.

Example on implicit PolicyRelationship:
```
*DOverrides(#ref(pol1))
```

Example on explicit PolicyRelationship:
```
*DOverrides(*pol(#ref(pol1)))
```

Example on mixed implicit and explicit PolicyRelationship:
```
*DOverrides(#ref(pol1),*pol(#ref(pol2)))
```

Example on PolicyRelationship with options and constraint:
```
*DOverrides(*pol(*permit(#ref(cond1)),*constraint(*gt(#ref(id1), #int(2))),#opts(runAction,priority=10)))
```

## PolicyActionRelationship

PolicyActionRelationship expressions is a wrapper around PolicyAction or Reference. It is never deserialized directly, only as a part of a Policy, PolicySet or PolicyDefault. If used without a wrapper (as a PolicyAction), then underlying PolicyAction will be put inside PolicyActionRelationship without any additional parameters.

Template: ```*act(<PolicyAction|Reference>{1,1},*constraint?,#opts?)``` <br>
Command starts with `*act` <br>
It can contain:<br>
- one parameter of type PolicyAction or Reference
- one optional parameter of type PolicyConstraint

Supported options:
- `priority` - integer, default value is null. Sets PolicyAction priority
- `executionMode` - set of strings describing [execution mode](../policy.md#policyactionrelationship), delimited by vertical bar - `|`

Example on implicit PolicyActionRelationship:
```
#permit(*save(foo,#str(bar)))
```

Example on explicit PolicyActionRelationship:
```
#permit(*act(#ref(pas1)))
```

Example on mixed implicit and explicit PolicyActionRelationship:
```
*DOverrides(#ref(pol1),*save(foo,#str(bar)),*act(#ref(pas1)))
```

Example on PolicyActionRelationship with options and constraint:
```
#permit(*act(#ref(pas1),*constraint(*gt(#ref(id1), #int(2))),#opts(executionMode=onDeny|onPermit,priority=10)))
```
