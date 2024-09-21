---
title: Policy Conditions
parent: Expression Language
description: Policy Condition Expression Language commands
nav_order: 2
---
# PolicyCondition expressions
{: .no_toc }

<details open markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

PolicyCondition expressions are used to reference Policy Conditions. They can return PolicyConditionAtomic, PolicyConditionComposite and PolicyConditionDefault entities.
Parser command to be invoked is `PEELParser(str).parseCondition()` and it returns IPolicyCondition entity.


## Command table

| Command     | Type                     | format                                                                                                          | options                                                                                                                                                               |
|-------------|--------------------------|-----------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| *gt         | PolicyConditionAtomic    | \*gt(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                                 | negateResult(\*), stringIgnoreCase(\*)                                                                                                                                |
| *gte        | PolicyConditionAtomic    | \*gte(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                                | negateResult(\*), stringIgnoreCase(\*)                                                                                                                                |
| *lt         | PolicyConditionAtomic    | \*lt(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                                 | negateResult(\*), stringIgnoreCase(\*)                                                                                                                                |
| *lte        | PolicyConditionAtomic    | \*lte(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                                | negateResult(\*), stringIgnoreCase(\*)                                                                                                                                |
| *isNull     | PolicyConditionAtomic    | \*isNull(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                             | negateResult(\*)                                                                                                                                                      |
| *notNull    | PolicyConditionAtomic    | \*notNull(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                            | negateResult(\*)                                                                                                                                                      |
| *isEmpty    | PolicyConditionAtomic    | \*isEmpty(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                            | negateResult(\*)                                                                                                                                                      |
| *notEmpty   | PolicyConditionAtomic    | \*notEmpty(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                           | negateResult(\*)                                                                                                                                                      |
| *isBlank    | PolicyConditionAtomic    | \*isBlank(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                            | negateResult(\*)                                                                                                                                                      |
| *notBlank   | PolicyConditionAtomic    | \*notBlank(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                           | negateResult(\*)                                                                                                                                                      |
| *sw         | PolicyConditionAtomic    | \*sw(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                                 | negateResult(\*), stringIgnoreCase(\*), fieldsStrictCheck(\*), arrayOrderStrictCheck(\*)                                                                              |
| *ew         | PolicyConditionAtomic    | \*ew(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                                 | negateResult(\*), stringIgnoreCase(\*), fieldsStrictCheck(\*), arrayOrderStrictCheck(\*)                                                                              |
| *contains   | PolicyConditionAtomic    | \*contains(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                           | negateResult(\*), stringIgnoreCase(\*), fieldsStrictCheck(\*), arrayOrderStrictCheck(\*)                                                                              |
| *isIn       | PolicyConditionAtomic    | \*isIn(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                               | negateResult(\*), stringIgnoreCase(\*), fieldsStrictCheck(\*), arrayOrderStrictCheck(\*)                                                                              |
| *eq         | PolicyConditionAtomic    | \*eq(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                                 | negateResult(\*), stringIgnoreCase(\*), fieldsStrictCheck(\*), arrayOrderStrictCheck(\*)                                                                              |
| *pos        | PolicyConditionAtomic    | \*pos(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                                | negateResult(\*)                                                                                                                                                      |
| *neg        | PolicyConditionAtomic    | \*neg(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                                | negateResult(\*)                                                                                                                                                      |
| *zero       | PolicyConditionAtomic    | \*zero(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                               | negateResult(\*)                                                                                                                                                      |
| *past       | PolicyConditionAtomic    | \*past(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                               | negateResult(\*)                                                                                                                                                      |
| *future     | PolicyConditionAtomic    | \*future(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                             | negateResult(\*)                                                                                                                                                      |
| *regexp     | PolicyConditionAtomic    | \*regexp(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                             | negateResult(\*)                                                                                                                                                      |
| *hasKey     | PolicyConditionAtomic    | \*hasKey(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                             | negateResult(\*)                                                                                                                                                      |
| *unique     | PolicyConditionAtomic    | \*unique(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                             | negateResult(\*)                                                                                                                                                      |
| *schema     | PolicyConditionAtomic    | \*schema(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                             | negateResult(\*)                                                                                                                                                      |
| *any        | PolicyConditionComposite | \*any(\<PolicyCondition\|Reference\>{1,},#opts?)                                                                | negateResult(\*), strictCheck(\*)                                                                                                                                     |
| *all        | PolicyConditionComposite | \*all(\<PolicyCondition\|Reference\>{1,},#opts?)                                                                | negateResult(\*), strictCheck(\*)                                                                                                                                     |
| *not        | PolicyConditionComposite | \*not(\<PolicyCondition\|Reference\>{1,1},#opts?)                                                               | negateResult(\*)                                                                                                                                                      |
| *nOf        | PolicyConditionComposite | \*nOf(\<PolicyCondition\|Reference\>{1,},#opts)                                                                 | minimumConditions(**), negateResult(\*), strictCheck(\*), optimize(\*)                                                                                                |
| #true       | PolicyConditionDefault   | #true(\<\>{0,0})                                                                                                |                                                                                                                                                                       |
| #false      | PolicyConditionDefault   | #false(\<\>{0,0})                                                                                               |                                                                                                                                                                       |
| #null       | PolicyConditionDefault   | #null(\<\>{0,0})                                                                                                |                                                                                                                                                                       | |

(\*) - Boolean condition <br>
(\*\*) - Mandatory option

## PolicyConditionAtomic

PolicyConditionAtomic expressions can only contain PolicyVariable commands or references to PolicyVariables

### GreaterThan

Template: ```*gt(<PolicyVariable|Reference>{2,2},#opts?)``` <br>
Command starts with `*gt` <br>
It contains exactly two parameters of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result
- `stringIgnoreCase` - boolean, default value is null. Ignores casing when comparing strings

Minimal example:
```
*gt(#ref(pvd1), #int(0))
```

Full example with options:
```
*gt(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase))
```

### GreaterThanEqual

Template: ```*gte(<PolicyVariable|Reference>{2,2},#opts?)``` <br>
Command starts with `*gte` <br>
It contains exactly two parameters of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result
- `stringIgnoreCase` - boolean, default value is null. Ignores casing when comparing strings

Minimal example:
```
*gte(#ref(pvd1), #int(0))
```

Full example with options:
```
*gte(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase))
```

### LessThan

Template: ```*lt(<PolicyVariable|Reference>{2,2},#opts?)``` <br>
Command starts with `*lt` <br>
It contains exactly two parameters of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result
- `stringIgnoreCase` - boolean, default value is null. Ignores casing when comparing strings

Minimal example:
```
*lt(#ref(pvd1), #int(0))
```

Full example with options:
```
*lt(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase))
```

### LessThanEqual

Template: ```*lte(<PolicyVariable|Reference>{2,2},#opts?)``` <br>
Command starts with `*lte` <br>
It contains exactly two parameters of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result
- `stringIgnoreCase` - boolean, default value is null. Ignores casing when comparing strings

Minimal example:
```
*lte(#ref(pvd1), #int(0))
```

Full example with options:
```
*lte(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase))
```

### IsNull

Template: ```*isNull(<PolicyVariable|Reference>{1,1},#opts?)``` <br>
Command starts with `*isNull` <br>
It contains exactly one parameter of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result

Minimal example:
```
*isNull(#ref(pvd1))
```

Full example with options:
```
*isNull(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))
```

### NotNull

Template: ```*notNull(<PolicyVariable|Reference>{1,1},#opts?)``` <br>
Command starts with `*notNull` <br>
It contains exactly one parameter of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result

Minimal example:
```
*notNull(#ref(pvd1))
```

Full example with options:
```
*notNull(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))
```

### IsEmpty

Template: ```*isEmpty(<PolicyVariable|Reference>{1,1},#opts?)``` <br>
Command starts with `*isEmpty` <br>
It contains exactly one parameter of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result

Minimal example:
```
*isEmpty(#ref(pvd1))
```

Full example with options:
```
*isEmpty(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))
```

### NotEmpty

Template: ```*notEmpty(<PolicyVariable|Reference>{1,1},#opts?)``` <br>
Command starts with `*notEmpty` <br>
It contains exactly one parameter of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result

Minimal example:
```
*notEmpty(#ref(pvd1))
```

Full example with options:
```
*notEmpty(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))
```

### IsBlank

Template: ```*isBlank(<PolicyVariable|Reference>{1,1},#opts?)``` <br>
Command starts with `*isBlank` <br>
It contains exactly one parameter of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result

Minimal example:
```
*isBlank(#ref(pvd1))
```

Full example with options:
```
*isBlank(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))
```

### NotBlank

Template: ```*notBlank(<PolicyVariable|Reference>{1,1},#opts?)``` <br>
Command starts with `*isNull` <br>
It contains exactly one parameter of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result

Minimal example:
```
*notBlank(#ref(pvd1))
```

Full example with options:
```
*notBlank(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))
```

### StartsWith

Template: ```*sw(<PolicyVariable|Reference>{2,2},#opts?)``` <br>
Command starts with `*sw` <br>
It contains exactly two parameters of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result
- `stringIgnoreCase` - boolean, default value is null. Ignores casing when comparing strings
- `fieldsStrictCheck` - boolean, default value is null. Strict checking for object keys
- `arrayOrderStrictCheck` - boolean, default value is null. Strict checking for array order

Minimal example:
```
*sw(#ref(pvd1), #ref(pvd2))
```

Full example with options:
```
*sw(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase,fieldsStrictCheck,arrayOrderStrictCheck))
```

### EndsWith

Template: ```*ew(<PolicyVariable|Reference>{2,2},#opts?)``` <br>
Command starts with `*ew` <br>
It contains exactly two parameters of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result
- `stringIgnoreCase` - boolean, default value is null. Ignores casing when comparing strings
- `fieldsStrictCheck` - boolean, default value is null. Strict checking for object keys
- `arrayOrderStrictCheck` - boolean, default value is null. Strict checking for array order

Minimal example:
```
*ew(#ref(pvd1), #ref(pvd2))
```

Full example with options:
```
*ew(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase,fieldsStrictCheck,arrayOrderStrictCheck))
```

### Contains

Template: ```*contains(<PolicyVariable|Reference>{2,2},#opts?)``` <br>
Command starts with `*contains` <br>
It contains exactly two parameters of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result
- `stringIgnoreCase` - boolean, default value is null. Ignores casing when comparing strings
- `fieldsStrictCheck` - boolean, default value is null. Strict checking for object keys
- `arrayOrderStrictCheck` - boolean, default value is null. Strict checking for array order

Minimal example:
```
*contains(#ref(pvd1), #ref(pvd2))
```

Full example with options:
```
*contains(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase,fieldsStrictCheck,arrayOrderStrictCheck))
```

### IsIn

Template: ```*isIn(<PolicyVariable|Reference>{2,2},#opts?)``` <br>
Command starts with `*isIn` <br>
It contains exactly two parameters of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result
- `stringIgnoreCase` - boolean, default value is null. Ignores casing when comparing strings
- `fieldsStrictCheck` - boolean, default value is null. Strict checking for object keys
- `arrayOrderStrictCheck` - boolean, default value is null. Strict checking for array order

Minimal example:
```
*isIn(#ref(pvd1), #ref(pvd2))
```

Full example with options:
```
*isIn(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase,fieldsStrictCheck,arrayOrderStrictCheck))
```

### Equals

Template: ```*eq(<PolicyVariable|Reference>{2,2},#opts?)``` <br>
Command starts with `*eq` <br>
It contains exactly two parameters of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result
- `stringIgnoreCase` - boolean, default value is null. Ignores casing when comparing strings
- `fieldsStrictCheck` - boolean, default value is null. Strict checking for object keys
- `arrayOrderStrictCheck` - boolean, default value is null. Strict checking for array order

Minimal example:
```
*eq(#ref(pvd1), #ref(pvd2))
```

Full example with options:
```
*eq(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase,fieldsStrictCheck,arrayOrderStrictCheck))
```

### Positive

Template: ```*pos(<PolicyVariable|Reference>{1,1},#opts?)``` <br>
Command starts with `*pos` <br>
It contains exactly one parameter of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result

Minimal example:
```
*pos(#ref(pvd1))
```

Full example with options:
```
*pos(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))
```

### Negative

Template: ```*neg(<PolicyVariable|Reference>{1,1},#opts?)``` <br>
Command starts with `*neg` <br>
It contains exactly one parameter of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result

Minimal example:
```
*neg(#ref(pvd1))
```

Full example with options:
```
*neg(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))
```

### Zero

Template: ```*zero(<PolicyVariable|Reference>{1,1},#opts?)``` <br>
Command starts with `*pos` <br>
It contains exactly one parameter of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result

Minimal example:
```
*zero(#ref(pvd1))
```

Full example with options:
```
*zero(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))
```

### Past

Template: ```*past(<PolicyVariable|Reference>{1,1},#opts?)``` <br>
Command starts with `*past` <br>
It contains exactly one parameter of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result

Minimal example:
```
*past(#ref(pvd1))
```

Full example with options:
```
*past(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))
```

### Future

Template: ```*future(<PolicyVariable|Reference>{1,1},#opts?)``` <br>
Command starts with `*future` <br>
It contains exactly one parameter of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result

Minimal example:
```
*future(#ref(pvd1))
```

Full example with options:
```
*future(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))
```

### Regexp

Template: ```*regexp(<PolicyVariable|Reference>{2,2},#opts?)``` <br>
Command starts with `*regexp` <br>
It contains exactly two parameters of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result

Minimal example:
```
*regexp(#ref(pvd1), #ref(pvd2))
```

Full example with options:
```
*regexp(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))
```

### HasKey

Template: ```*hasKey(<PolicyVariable|Reference>{2,2},#opts?)``` <br>
Command starts with `*hasKey` <br>
It contains exactly two parameters of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result

Minimal example:
```
*hasKey(#ref(pvd1), #ref(pvd2))
```

Full example with options:
```
*hasKey(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))
```

### Unique

Template: ```*unique(<PolicyVariable|Reference>{1,1},#opts?)``` <br>
Command starts with `*unique` <br>
It contains exactly one parameter of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result

Minimal example:
```
*unique(#ref(pvd1))
```

Full example with options:
```
*unique(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))
```

### Schema

Template: ```*schema(<PolicyVariable|Reference>{2,2},#opts?)``` <br>
Command starts with `*schema` <br>
It contains exactly two parameters of type PolicyVariable or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result

Minimal example:
```
*schema(#ref(pvd1), #ref(pvd2))
```

Full example with options:
```
*schema(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))
```

## PolicyConditionComposite

PolicyConditionComposite expressions can only contain PolicyCondition commands or references to PolicyConditions

### Any

Template: ```*any(<PolicyCondition|Reference>{1,},#opts?)``` <br>
Command starts with `*any` <br>
It contains at least one parameter of type PolicyCondition or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result
- `strictCheck` - boolean, default value is null. Checks all conditions

Minimal example:
```
*any(#ref(pcr1,1.2.3),#ref(pcr2))
```

Full example with options:
```
*any(#ref(pcr1,1.2.3),#ref(pcr2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,strictCheck))
```

### All

Template: ```*all(<PolicyCondition|Reference>{1,},#opts?)``` <br>
Command starts with `*all` <br>
It contains at least one parameter of type PolicyCondition or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result
- `strictCheck` - boolean, default value is null. Checks all conditions

Minimal example:
```
*all(#ref(pcr1,1.2.3),#ref(pcr2))
```

Full example with options:
```
*all(#ref(pcr1,1.2.3),#ref(pcr2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,strictCheck))
```

### Not

Template: ```*not(<PolicyCondition|Reference>{1,1},#opts?)``` <br>
Command starts with `*all` <br>
It contains exactly one parameter of type PolicyCondition or Reference <br>
Supported options:
- `negateResult` - boolean, default value is null. Negates condition result

Minimal example:
```
*not(#ref(pcr1,1.2.3))
```

Full example with options:
```
*not(#ref(pcr1,1.2.3),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))
```

### NOf

Template: ```*nOf(<PolicyCondition|Reference>{1,},#opts)``` <br>
Command starts with `*nOf` <br>
It contains at least one parameter of type PolicyCondition or Reference <br>
Supported options:
- `minimumConditions` - **Mandatory** Int, no default value. Defines minimum number of conditions that should resolve to true
- `negateResult` - boolean, default value is null. Negates condition result
- `strictCheck` - boolean, default value is null. Checks all conditions
- `optimize` - boolean, default value is null. Runs check in optimized manner

Minimal example:
```
*nOf(#ref(pcr1,1.2.3),#ref(pcr2),#opts(minimumConditions=1))
```

Full example with options:
```
*nOf(#ref(pcr1,1.2.3),#ref(pcr2),#opts(minimumConditions=1,id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,strictCheck,optimize))
```

## PolicyConditionDefault

PolicyConditionDefault expressions are without parameters or options

### True

Template: ```#true(<>{0,0})``` <br>
Command starts with `#true` <br>
It cannot contain any parameter <br>
Supported options:
- none

Example:
```
#true()
```

### False

Template: ```#false(<>{0,0})``` <br>
Command starts with `#false` <br>
It cannot contain any parameter <br>
Supported options:
- none

Example:
```
#false()
```

### Null

Template: ```#null(<>{0,0})``` <br>
Command starts with `#null` <br>
It cannot contain any parameter <br>
Supported options:
- none

Example:
```
#null()
```
