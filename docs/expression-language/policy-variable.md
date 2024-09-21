---
title: Policy Variables
parent: Expression Language
description: Policy Variable Expression Language commands
nav_order: 1
---
# PolicyVariable expressions
{: .no_toc }

<details open markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

Policy Variable expressions are used to reference Policy Variables. They can return PolicyVariableStatic and PolicyVariableDynamic entities.
Parser command to be invoked is `PEELParser(str).parseVariable()` and it returns IPolicyVariable entity.


## PolicyVariable command table

| Command     | Type                     | format                                                                                                          | options                                                                                                                                                               |
|-------------|--------------------------|-----------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| #str        | PolicyVariableStatic     | #str(\<content\>{1,1},#opts?)                                                                                   | isJson(\*)                                                                                                                                                            |
| #date       | PolicyVariableStatic     | #date(\<content\>{1,1},#opts?)                                                                                  | dateFormat                                                                                                                                                            |
| #dTime      | PolicyVariableStatic     | #dTime(\<content\>{1,1},#opts?)                                                                                 | dateTimeFormat                                                                                                                                                        |
| #time       | PolicyVariableStatic     | #time(\<content\>{1,1},#opts?)                                                                                  | timeFormat                                                                                                                                                            |
| #per        | PolicyVariableStatic     | #per(\<content\>{1,1},#opts?)                                                                                   |                                                                                                                                                                       |
| #dur        | PolicyVariableStatic     | #dur(\<content\>{1,1},#opts?)                                                                                   |                                                                                                                                                                       |
| #int        | PolicyVariableStatic     | #int(\<content\>{1,1},#opts?)                                                                                   |                                                                                                                                                                       |
| #long       | PolicyVariableStatic     | #long(\<content\>{1,1},#opts?)                                                                                  |                                                                                                                                                                       |
| #num        | PolicyVariableStatic     | #num(\<content\>{1,1},#opts?)                                                                                   |                                                                                                                                                                       |
| #float      | PolicyVariableStatic     | #float(\<content\>{1,1},#opts?)                                                                                 |                                                                                                                                                                       |
| #bigD       | PolicyVariableStatic     | #bigD(\<content\>{1,1},#opts?)                                                                                  |                                                                                                                                                                       |
| #bool       | PolicyVariableStatic     | #bool(\<content\>{1,1},#opts?)                                                                                  |                                                                                                                                                                       |
| #obj        | PolicyVariableStatic     | #obj(\<content\>{1,1},#opts?)                                                                                   |                                                                                                                                                                       |
| #arr        | PolicyVariableStatic     | #arr(\<content\>{1,1},#opts?)                                                                                   |                                                                                                                                                                       |
| *dyn        | PolicyVariableDynamic    | \*dyn(<PolicyVariableResolver\|Reference>{1,},#opts?)                                                           | type, format, timeFormat, dateFormat, dateTimeFormat                                                                                                                  |
| *key        | PolicyVariableResolver   | \*key(\<content\>{1,1},#opts?)                                                                                  | source                                                                                                                                                                |
| *path       | PolicyVariableResolver   | \*path(\<content\>{1,1},#opts?)                                                                                 | source, key                                                                                                                                                           |
| *jq         | PolicyVariableResolver   | \*jq(\<content\>{1,1},#opts?)                                                                                   | source, key                                                                                                                                                           |

(\*) - Boolean condition <br>


## PolicyVariableStatic

PolicyVariableStatic expressions can only contain content that is deserialized to PolicyVariableStatic entity.

### String

Template: ```#str(<content>{1,1},#opts?)``` <br>
Command starts with `#str` <br>
It can contain any string content <br>
Supported options:
- `isJson` - boolean, default value is null. It will create PolicyVariableStatic with value of type String and format of
  JSON.

Minimal example:
```
#str(22)
```

Full example with options:
```
#str(22,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|"label 2",isJson))
```

### Date

Template: ```#date(<content>{1,1},#opts?)``` <br>
Command starts with `#date` <br>
It can contain any string content parsable to date<br>
Supported options:
- `dateFormat` - string, default value is null. Provides custom date format for provided content. In format is not provided, ISO
  8601 format is used.

Minimal example:
```
#date(2023-05-15)
```

Full example with options:
```
#date(15.05.2023,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|"label 2",dateFormat="dd.MM.yyyy"))
```

### DateTime

Template: ```#dTime(<content>{1,1},#opts?)``` <br>
Command starts with `#dTime` <br>
It can contain any string content parsable to date-time<br>
Supported options:
- `dateTimeFormat` - string, default value is null. Provides custom dateTime format for provided content. In format is not provided, ISO
  8601 format is used.

Minimal example:
```
#dTime(2023-05-15T14:30:00+01:00)
```

Full example with options:
```
#dTime(15.05.2023 14:30:00.123 +01:00,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|"label 2",dateTimeFormat="dd.MM.yyyy HH:mm:ss.SSS XXX"))
```

### Time

Template: ```#time(<content>{1,1},#opts?)``` <br>
Command starts with `#time` <br>
It can contain any string content parsable to time<br>
Supported options:
- `timeFormat` - string, default value is null. Provides custom time format for provided content. In format is not provided, ISO
  8601 format is used.

Minimal example:
```
#time(14:30:00)
```

Full example with options:
```
#time("14/30/00",#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|"label 2",timeFormat="HH/mm/ss"))
```

### Period

Template: ```#per(<content>{1,1},#opts?)``` <br>
Command starts with `#per` <br>
It can contain any string content in ISO-8601 format for Period<br>
Supported options:
- none

Minimal example:
```
#per(P1Y2M3D)
```

Full example with options:
```
#per(P1Y2M3D,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|"label 2"))
```

### Duration

Template: ```#dur(<content>{1,1},#opts?)``` <br>
Command starts with `#dur` <br>
It can contain any string content in ISO-8601 format for Duration<br>
Supported options:
- none

Minimal example:
```
#dur(PT1H30M)
```

Full example with options:
```
#dur(PT1H30M,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|"label 2"))
```

### Integer

Template: ```#int(<content>{1,1},#opts?)``` <br>
Command starts with `#int` <br>
It can contain any string content convertible to Integer<br>
Supported options:
- none

Minimal example:
```
#int(-21)
```

Full example with options:
```
#int(-2147483647,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|"label 2"))
```

### Long

Template: ```#long(<content>{1,1},#opts?)``` <br>
Command starts with `#long` <br>
It can contain any string content convertible to Long<br>
Supported options:
- none

Minimal example:
```
#long(9223372036854775807)
```

Full example with options:
```
#long(9223372036854775807,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|"label 2"))
```

### Number

Template: ```#num(<content>{1,1},#opts?)``` <br>
Command starts with `#num` <br>
It can contain any string content convertible to Double<br>
Supported options:
- none

Minimal example:
```
#num(-3.14159)
```

Full example with options:
```
#num(-3.14159,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|"label 2"))
```

### Float

Template: ```#float(<content>{1,1},#opts?)``` <br>
Command starts with `#float` <br>
It can contain any string content convertible to Float<br>
Supported options:
- none

Minimal example:
```
#float(3.14159)
```

Full example with options:
```
#float(3.14159,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|"label 2"))
```

### BigDecimal

Template: ```#bigD(<content>{1,1},#opts?)``` <br>
Command starts with `#bigD` <br>
It can contain any string content convertible to BigDecimal<br>
Supported options:
- none

Minimal example:
```
#bigD(123456789.987654321)
```

Full example with options:
```
#bigD(123456789.987654321,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|"label 2"))
```

### Boolean

Template: ```#bool(<content>{1,1},#opts?)``` <br>
Command starts with `#bool` <br>
It can contain `true` or `false` strings<br>
Supported options:
- none

Minimal example:
```
#bool(true)
```

Full example with options:
```
#bool(true,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|"label 2"))
```

### Object

Template: ```#obj(<content>{1,1},#opts?)``` <br>
Command starts with `#obj` <br>
It can contain string in JSON format of type object<br>
Supported options:
- none

**Tip:** Escape JSON strings with a backtick
{: .fs-3 }

Minimal example:
```
#obj(`{"key": "value", "foo": "bar"}`)
```

Full example with options:
```
#obj(`{"key": "value", "foo": "bar"}`,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|"label 2"))
```

### Array

Template: ```#arr(<content>{1,1},#opts?)``` <br>
Command starts with `#arr` <br>
It can contain string in JSON format of type array<br>
Supported options:
- none

**Tip:** Escape JSON strings with a backtick
{: .fs-3 }

Minimal example:
```
#arr(`[1, 2, 3]`)
```

Full example with options:
```
#arr(`[1, 2, 3]`,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|"label 2"))
```

## PolicyVariableDynamic

PolicyVariableDynamic expression can only contain commands ot type PolicyVariableResolver or Reference that are deserialized to PolicyVariableDynamic entity.

Template: ```*dyn(<PolicyVariableResolver|Reference>{1,},#opts?)``` <br>
Command starts with `*dyn` <br>
It can contain at least one command of type PolicyVariableResolver or Reference. Multiple commands are provided as comma separated values <br>
Supported options:
- `type` - string enum, default value is null. It can contain possible variable [types](../policy-variable.md#ipolicyvariable-data-types-and-formats) to which parsed value will be cast to
- `format` - string enum, default value is null. It can contain possible variable [formats](../policy-variable.md#ipolicyvariable-data-types-and-formats) to which parsed value will be cast to
- `timeFormat` - string, default value is null. Provides custom time format for provided content. In format is not provided, ISO
  8601 format is used.
- `dateFormat` - string, default value is null. Provides custom date format for provided content. In format is not provided, ISO
  8601 format is used.
- `dateTimeFormat` - string, default value is null. Provides custom dateTime format for provided content. In format is not provided, ISO
  8601 format is used.

Minimal example:
```
*dyn(*key(foo),*jq(foo),*path(foo),#ref(pvr1),#ref(pvr1,1.2.3))
```

Full example with options:
```
*dyn(*key(foo),*jq(foo),*path(foo),#ref(pvr1),#ref(pvr1,1.2.3),#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|bar,type=string,format=date,dateFormat=dd.MM.yyyy))
```

## PolicyVariableResolver

PolicyVariableResolver expression can only contain content that is deserialized to PolicyVariableResolver entity.
Parser command to be invoked is `PEELParser(str).parseVariableResolver()` and it returns PolicyVariableResolver entity.

### Key resolver

Template: ```*key(<content>{1,1},#opts?)``` <br>
Command starts with `*key` <br>
It can contain any string content <br>
Supported options:
- `source` - string enum, default value is null. It can contain possible [store](context.md/#stores) to fetch variable from

Minimal example:
```
*key(foo)
```

Full example with options:
```
*key(foo,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|bar,source=subject))
```

### JMESPath resolver

Template: ```*path(<content>{1,1},#opts?)``` <br>
Command starts with `*path` <br>
It can contain any valid [JMESPath](https://jmespath.org/) expression <br>
Supported options:
- `source` - string enum, default value is null. It can contain possible [store](context.md/#stores) to fetch variable from
- `key` - string. Contains prefilter value when parsing a variable.

Minimal example:
```
*path(object.a1)
```

Full example with options:
```
*path(object.a1,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo,source=subject,key=k))
```

### JQ resolver

Template: ```*jq(<content>{1,1},#opts?)``` <br>
Command starts with `*jq` <br>
It can contain any valid [JQ](https://jqlang.github.io/jq/) expression <br>
Supported options:
- `source` - string enum, default value is null. It can contain possible [store](context.md/#stores) to fetch variable from
- `key` - string. Contains prefilter value when parsing a variable.

Minimal example:
```
*jq(.object.a1)
```

Full example with options:
```
*jq(.object.a1,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo,source=subject,key=k))
```