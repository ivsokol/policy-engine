---
title: Expression Language
description: Introduction to Policy Engine Expression Language - PEEL
nav_order: 9
---
# Policy Engine Expression Language - PEEL
{: .no_toc }

<details open markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

**Policy Engine Expression Language - PEEL** is a representation of a Policy Engine entities in String format. 
Policy Engine is capable of lexing and parsing PEEL strings and converting them to Policy Engine entities.

## PEEL Syntax

Every PEEL string contains a command that represents one of Policy Engine entities. Command format is always same: 

```<command>(<content|command>{0,},<options>?) -> <PolicyEngineEntity>```

where every command has the following parameters:

- **command**: String that represents Policy Engine entity and starts with `*` character for dynamic commands and `#`
  character for static commands
- command parameter borders are always `(` and `)`
- **parameters**: Comma separated list of parameters that are passed to Policy Engine entity. They can be either a content or another
  command.
- **options**: Comma separated list of options that are passed to Policy Engine entity.

**Example of PEEL string**
```
*permit(#ref(cond1))
```
This is a Permit Policy that references a PolicyCondition with id `cond1`.

## Content parsing

PEEL only parses following types of characters:
- letters - both lowercase and uppercase
- numbers - 0-9
- special characters - ()[]{},.-+*/=$#`"_

All other characters are skipped unless they are part of command content or if they are escaped.
Command content is considered to be everything between brackets, delimited by comma.
Content can be a string, another command or options placeholder.

When PEEL string contains special characters, they must be escaped. Following character are considered special:

- `(`
- `)`
- `,`
- `#`
- `*`
- `=`

Escaping can be done by putting string content between following characters:

- " - double quotes - ASCII 34
- ` - backtick - ASCII 96
- """ - triple quotes - ASCII 34 three times

Same character must be used for escaping and unescaping.

**Example of PEEL string with escaped characters**

```
#time("14/30/00",#opts(timeFormat="HH/mm/ss"))
```

In this case, both command content and option value are escaped.
This example could also be written with different escape characters as:

```
#time(`14/30/00`,#opts(timeFormat="""HH/mm/ss"""))
```

### Options

Options are optional and can be omitted. Options are always in format `#opts(<key=value>{1,})` and are separated by
comma. Boolean options can contain only name of the option, while other options must contain name and value.

**Example of PEEL string with options**

```
*permit(#ref(cond1),#opts(id="policy1"))
```

There are common options that can be used in every command:

- `id` - unique identifier of the Policy Engine entity
- `ver` - version of the Policy Engine entity. Must be in [SemVer](https://semver.org/) format
- `desc` - description of the Policy Engine entity
- `labels` - labels of the Policy Engine entity. They must be separated by vertical line `|` character (ASCII 124). If labels contain special characters,
  they must be escaped.

**Example of PEEL string with options**

```
*permit(#ref(cond1),#opts(id=policy1,ver=1.0.0,desc="This is a policy",labels=label1|"label 2"))
```

## Reference

Template: ```#ref(<content>{1,2})``` <br>
Reference expression can only contain 2 content parameters:

- `id` - string, mandatory, contains id of a referred entity
- `version` - string, optional, contains version of a referred entity. Must be in [SemVer](https://semver.org/) format

Reference expression will be deserialized to specific entity Ref depending on the command it is used in. If it is used in PolicyVariableDynamic,
it will be deserialized to PolicyVariableResolverRef, and if used in Policy it will be deserialized to PolicyConditionRef.

Reference mapping table

| Position                 | MappedTo                  |
|--------------------------|---------------------------|
| PolicyVariableDynamic    | PolicyVariableResolverRef |
| PolicyConditionAtomic    | PolicyVariableRef         |
| PolicyConditionComposite | PolicyConditionRef        |
| PolicyAction             | PolicyVariableRef         |
| Policy                   | PolicyConditionRef        |
| PolicySet                | PolicyRef                 |
| PolicyConstraint         | PolicyConditionRef        |
| PolicyActionRelationship | PolicyActionRef           |
| PolicyRelationship       | PolicyRef                 |


Minimal example:
```
*permit(*all(#ref(pcr1),#ref(pcr2)))
```

Full example with options:
```
*permit(*all(#ref(pcr1,1.2.3),#ref(pcr2, 4.5.6)))
```

## Full PEEL command table

| Command     | Type                     | format                                                                                                           | options                                                                                                                                                               |
|-------------|--------------------------|------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| #str        | PolicyVariableStatic     | #str(\<content\>{1,1},#opts?)                                                                                    | isJson(\*)                                                                                                                                                            |
| #date       | PolicyVariableStatic     | #date(\<content\>{1,1},#opts?)                                                                                   | dateFormat                                                                                                                                                            |
| #dTime      | PolicyVariableStatic     | #dTime(\<content\>{1,1},#opts?)                                                                                  | dateTimeFormat                                                                                                                                                        |
| #time       | PolicyVariableStatic     | #time(\<content\>{1,1},#opts?)                                                                                   | timeFormat                                                                                                                                                            |
| #per        | PolicyVariableStatic     | #per(\<content\>{1,1},#opts?)                                                                                    |                                                                                                                                                                       |
| #dur        | PolicyVariableStatic     | #dur(\<content\>{1,1},#opts?)                                                                                    |                                                                                                                                                                       |
| #int        | PolicyVariableStatic     | #int(\<content\>{1,1},#opts?)                                                                                    |                                                                                                                                                                       |
| #long       | PolicyVariableStatic     | #long(\<content\>{1,1},#opts?)                                                                                   |                                                                                                                                                                       |
| #num        | PolicyVariableStatic     | #num(\<content\>{1,1},#opts?)                                                                                    |                                                                                                                                                                       |
| #float      | PolicyVariableStatic     | #float(\<content\>{1,1},#opts?)                                                                                  |                                                                                                                                                                       |
| #bigD       | PolicyVariableStatic     | #bigD(\<content\>{1,1},#opts?)                                                                                   |                                                                                                                                                                       |
| #bool       | PolicyVariableStatic     | #bool(\<content\>{1,1},#opts?)                                                                                   |                                                                                                                                                                       |
| #obj        | PolicyVariableStatic     | #obj(\<content\>{1,1},#opts?)                                                                                    |                                                                                                                                                                       |
| #arr        | PolicyVariableStatic     | #arr(\<content\>{1,1},#opts?)                                                                                    |                                                                                                                                                                       |
| *dyn        | PolicyVariableDynamic    | \*dyn(<PolicyVariableResolver\|Reference>{1,},#opts?)                                                            | type, format, timeFormat, dateFormat, dateTimeFormat                                                                                                                  |
| *key        | PolicyVariableResolver   | \*key(\<content\>{1,1},#opts?)                                                                                   | source                                                                                                                                                                |
| *path       | PolicyVariableResolver   | \*path(\<content\>{1,1},#opts?)                                                                                  | source, key                                                                                                                                                           |
| *jq         | PolicyVariableResolver   | \*jq(\<content\>{1,1},#opts?)                                                                                    | source, key                                                                                                                                                           |
| *gt         | PolicyConditionAtomic    | \*gt(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                                  | negateResult(\*), stringIgnoreCase(\*)                                                                                                                                |
| *gte        | PolicyConditionAtomic    | \*gte(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                                 | negateResult(\*), stringIgnoreCase(\*)                                                                                                                                |
| *lt         | PolicyConditionAtomic    | \*lt(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                                  | negateResult(\*), stringIgnoreCase(\*)                                                                                                                                |
| *lte        | PolicyConditionAtomic    | \*lte(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                                 | negateResult(\*), stringIgnoreCase(\*)                                                                                                                                |
| *isNull     | PolicyConditionAtomic    | \*isNull(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                              | negateResult(\*)                                                                                                                                                      |
| *notNull    | PolicyConditionAtomic    | \*notNull(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                             | negateResult(\*)                                                                                                                                                      |
| *isEmpty    | PolicyConditionAtomic    | \*isEmpty(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                             | negateResult(\*)                                                                                                                                                      |
| *notEmpty   | PolicyConditionAtomic    | \*notEmpty(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                            | negateResult(\*)                                                                                                                                                      |
| *isBlank    | PolicyConditionAtomic    | \*isBlank(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                             | negateResult(\*)                                                                                                                                                      |
| *notBlank   | PolicyConditionAtomic    | \*notBlank(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                            | negateResult(\*)                                                                                                                                                      |
| *sw         | PolicyConditionAtomic    | \*sw(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                                  | negateResult(\*), stringIgnoreCase(\*), fieldsStrictCheck(\*), arrayOrderStrictCheck(\*)                                                                              |
| *ew         | PolicyConditionAtomic    | \*ew(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                                  | negateResult(\*), stringIgnoreCase(\*), fieldsStrictCheck(\*), arrayOrderStrictCheck(\*)                                                                              |
| *contains   | PolicyConditionAtomic    | \*contains(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                            | negateResult(\*), stringIgnoreCase(\*), fieldsStrictCheck(\*), arrayOrderStrictCheck(\*)                                                                              |
| *isIn       | PolicyConditionAtomic    | \*isIn(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                                | negateResult(\*), stringIgnoreCase(\*), fieldsStrictCheck(\*), arrayOrderStrictCheck(\*)                                                                              |
| *eq         | PolicyConditionAtomic    | \*eq(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                                  | negateResult(\*), stringIgnoreCase(\*), fieldsStrictCheck(\*), arrayOrderStrictCheck(\*)                                                                              |
| *pos        | PolicyConditionAtomic    | \*pos(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                                 | negateResult(\*)                                                                                                                                                      |
| *neg        | PolicyConditionAtomic    | \*neg(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                                 | negateResult(\*)                                                                                                                                                      |
| *zero       | PolicyConditionAtomic    | \*zero(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                                | negateResult(\*)                                                                                                                                                      |
| *past       | PolicyConditionAtomic    | \*past(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                                | negateResult(\*)                                                                                                                                                      |
| *future     | PolicyConditionAtomic    | \*future(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                              | negateResult(\*)                                                                                                                                                      |
| *regexp     | PolicyConditionAtomic    | \*regexp(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                              | negateResult(\*)                                                                                                                                                      |
| *hasKey     | PolicyConditionAtomic    | \*hasKey(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                              | negateResult(\*)                                                                                                                                                      |
| *unique     | PolicyConditionAtomic    | \*unique(\<PolicyVariable\|Reference\>{1,1},#opts?)                                                              | negateResult(\*)                                                                                                                                                      |
| *schema     | PolicyConditionAtomic    | \*schema(\<PolicyVariable\|Reference\>{2,2},#opts?)                                                              | negateResult(\*)                                                                                                                                                      |
| *any        | PolicyConditionComposite | \*any(\<PolicyCondition\|Reference\>{1,},#opts?)                                                                 | negateResult(\*), strictCheck(\*)                                                                                                                                     |
| *all        | PolicyConditionComposite | \*all(\<PolicyCondition\|Reference\>{1,},#opts?)                                                                 | negateResult(\*), strictCheck(\*)                                                                                                                                     |
| *not        | PolicyConditionComposite | \*not(\<PolicyCondition\|Reference\>{1,1},#opts?)                                                                | negateResult(\*)                                                                                                                                                      |
| *nOf        | PolicyConditionComposite | \*nOf(\<PolicyCondition\|Reference\>{1,},#opts)                                                                  | minimumConditions(**), negateResult(\*), strictCheck(\*), optimize(\*)                                                                                                |
| #true       | PolicyConditionDefault   | #true(\<\>{0,0})                                                                                                 |                                                                                                                                                                       |
| #false      | PolicyConditionDefault   | #false(\<\>{0,0})                                                                                                |                                                                                                                                                                       |
| #null       | PolicyConditionDefault   | #null(\<\>{0,0})                                                                                                 |                                                                                                                                                                       |
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
| *save       | PolicyAction             | \*save(\<content\|PolicyVariable\|Reference\>{2,2},#opts?)                                                       | failOnMissingKey(\*), failOnExistingKey(\*), failOnNullSource(\*)                                                                                                     |
| *clear      | PolicyAction             | \*clear(\<content\>{1,1},#opts?)                                                                                 | failOnMissingKey(\*)                                                                                                                                                  |
| *patch      | PolicyAction             | \*patch((\<content\|PolicyVariable\|Reference\>{3,3},#opts?)                                                     | failOnMissingKey(\*), failOnExistingKey(\*), failOnNullSource(\*), castNullSourceToArray(\*)                                                                          |
| *merge      | PolicyAction             | \*merge((\<content\|PolicyVariable\|Reference\>{3,3},#opts?)                                                     | failOnMissingKey(\*), failOnExistingKey(\*), failOnNullSource(\*), failOnNullMerge(\*), type, format                                                                  |
| *constraint | PolicyConstraint         | \*constraint(\<PolicyCondition\|Reference\>{1,1})                                                                |                                                                                                                                                                       |
| *act        | PolicyActionRelationship | \*act(\<PolicyAction\|Reference\>{1,1},*constraint?,#opts?)                                                      | executionMode, priority                                                                                                                                               |
| *pol        | PolicyRelationship       | \*pol(\<Policy\|Reference\>{1,1},*constraint?,#opts?)                                                            | runAction(\*), priority                                                                                                                                               |
| #ref        | Reference                | #ref(\<content\>{1,2})                                                                                           |                                                                                                                                                                       |

(\*) - Boolean condition <br>
(\*\*) - Mandatory option
