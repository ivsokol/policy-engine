---
title: Policy Action
parent: Expression Language
description: Policy Action Language commands
nav_order: 4
---
# PolicyAction expressions
{: .no_toc }

<details open markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

Policy Action expressions are used to reference Policy Actions. They can return PolicyActionSave, PolicyActionClear, PolicyActionJsonMerge and PolicyActionJsonPatch entities.
Parser command to be invoked is `PEELParser(str).parseAction()` and it returns IPolicyAction entity.


## Command table

| Command     | Type                     | format                                                                                                          | options                                                                                              |
|-------------|--------------------------|-----------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| *save       | PolicyAction             | \*save(\<content\|PolicyVariable\|Reference\>{2,2},#opts?)                                                      | failOnMissingKey(\*), failOnExistingKey(\*), failOnNullSource(\*)                                    |
| *clear      | PolicyAction             | \*clear(\<content\>{1,1},#opts?)                                                                                | failOnMissingKey(\*)                                                                                 |
| *patch      | PolicyAction             | \*patch((\<content\|PolicyVariable\|Reference\>{3,3},#opts?)                                                    | failOnMissingKey(\*), failOnExistingKey(\*), failOnNullSource(\*), castNullSourceToArray(\*)         |
| *merge      | PolicyAction             | \*merge((\<content\|PolicyVariable\|Reference\>{3,3},#opts?)                                                    | failOnMissingKey(\*), failOnExistingKey(\*), failOnNullSource(\*), failOnNullMerge(\*), type, format |

(\*) - Boolean condition <br>

### PolicyActionSave

Template: ```*save(<content|PolicyVariable|Reference>{2,2},#opts?)``` <br>
Command starts with `*save` <br>
It can contain:<br>
- one parameter of type content that represents `key` field
- one parameter of type PolicyVariable or Reference that represents `value` field

Supported options:
- `failOnMissingKey` - boolean, default value is null. It will fail if key cannot be found
- `failOnExistingKey` - boolean, default value is null. It will fail if key exists
- `failOnNullSource` - boolean, default value is null. It will fail if value resolves to null

Minimal example:
```
*save(foo,#ref(pvd1))
```

Full example with options:
```
*save(foo,#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,failOnMissingKey,failOnExistingKey,failOnNullSource))
```

### PolicyActionClear

Template: ```*clear(<content>{1,1},#opts?)``` <br>
Command starts with `*clear` <br>
It can contain:<br>
- one parameter of type content that represents `key` field

Supported options:
- `failOnMissingKey` - boolean, default value is null. It will fail if key cannot be found

Minimal example:
```
*clear(foo)
```

Full example with options:
```
*clear(foo,#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,failOnMissingKey))
```

### PolicyActionPatch

Template: ```*patch((<content|PolicyVariable|Reference>{3,3},#opts?)``` <br>
Command starts with `*patch` <br>
It can contain:<br>
- one parameter of type content that represents `key` field
- two parameters of type PolicyVariable or Reference where:
  - first parameter represents `source` field
  - second parameter represents `patch` field

Supported options:
- `failOnMissingKey` - boolean, default value is null. It will fail if key cannot be found
- `failOnExistingKey` - boolean, default value is null. It will fail if key exists
- `failOnNullSource` - boolean, default value is null. It will fail if source resolves to null
- `castNullSourceToArray` - boolean, default value is null. It will fail if value resolves to null

Minimal example:
```
*patch(foo,#ref(pvd1),#ref(pvd2))
```

Full example with options:
```
*patch(foo,#ref(pvd1),#ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,failOnMissingKey,failOnExistingKey,failOnNullSource,castNullSourceToArray))
```

### PolicyActionMerge

Template: ```*merge((<content|PolicyVariable|Reference>{3,3},#opts?)``` <br>
Command starts with `*merge` <br>
It can contain:<br>
- one parameter of type content that represents `key` field
- two parameters of type PolicyVariable or Reference where:
  - first parameter represents `source` field
  - second parameter represents `merge` field

Supported options:
- `failOnMissingKey` - boolean, default value is null. It will fail if key cannot be found
- `failOnExistingKey` - boolean, default value is null. It will fail if key exists
- `failOnNullSource` - boolean, default value is null. It will fail if source resolves to null
- `failOnNullmerge` - boolean, default value is null. It will fail if merge resolves to null
- `type` - string enum, default value is null. It can contain possible variable [types](../policy-variable.md#ipolicyvariable-data-types-and-formats) to which parsed value will be cast to
- `format` - string enum, default value is null. It can contain possible variable [formats](../policy-variable.md#ipolicyvariable-data-types-and-formats) to which parsed value will be cast to

Minimal example:
```
*merge(foo,#ref(pvd1),#ref(pvd2))
```

Full example with options:
```
*merge(foo,#ref(pvd1),#ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,failOnMissingKey,failOnExistingKey,failOnNullSource,failOnNullMerge,type=string,format=time))
```
