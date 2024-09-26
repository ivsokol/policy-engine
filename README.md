# Policy Engine - PoE
<br>

![Policies banner](./assets/policies-banner.jpeg "banner")

<br>

**PolicyEngine** - PoE is a catalog driven policy engine written in Kotlin. It provides a flexible framework for defining and evaluating
policies and conditions based on the provided context.

PoE is designed to be used as a library in various enterprise applications, for scenarios as [access control](https://ivsokol.github.io/policy-engine/docs/examples/access-control.md), authorization, credit scoring, price calculation or any other use
case where policies and conditions need to be evaluated. Catalog definition is defined in JSON format.

It is written in Kotlin and can be used in any Java or Kotlin JVM project. As library deals with potentially complex definitions, all parts of the engine are extensively tested (code is covered with more than 2000 tests).

[PolicyEngine](https://ivsokol.github.io/policy-engine/docs/policy-engine.md) is a catalog driven policy engine, so in order to use it, you need to define a catalog. [PolicyCatalog](https://ivsokol.github.io/policy-engine/docs/policy-catalog.md) can be defined as a Class or as a JSON string. JSON string can be pulled from a remote location or from a local file (not part of the engine). One engine instance can only contain one catalog, but it is possible to create multiple PolicyEngine instances at the same time.

One instance of PolicyEngine can be used to evaluate multiple policies or conditions in parallel (it can be used as a singleton).
PolicyEngine is doing variable extraction over a provided [Context](https://ivsokol.github.io/policy-engine/docs/context.md). Engine cannot communicate with external systems, so
it is up to the client to provide all necessary data to the engine as part of the Context stores.

Engine also ships with expression language support (PEEL - [PolicyEngine Expression Language](https://ivsokol.github.io/policy-engine/docs/expression-language.md)). It allows users to define custom conditions and policies through strings that can be evaluated by the engine.

## Simple example

**Only pet owner can update pet information**

We want to allow only pet owners to update pet information, and deny for others.

```kotlin
test("only pet owner can update pet information") {
    // define owner in subject store
    val owner = json.decodeFromString<Map<String, String?>>("""{"username" : "jDoe"}""")
    // prepare request payload
    val request =
        mapOf(
            "pet" to """{"kind": "dog", "name" : "Fido","owner" : "jDoe"}""".toJsonAny(),
            "action" to "update")
    // define policy
    val policy =
        """
                *permit(
                    *eq(
                        *dyn(*key(username,#opts(source=subject))),
                        *dyn(*jq(.pet.owner))
                    ),
                    #opts(strictTargetEffect)
                )
            """
            .trimIndent()
    // prepare context
    val context = Context(subject = owner, request = request)
    // evaluate policy
    val resultOwner = PolicyEngine().evaluatePolicy(PEELParser(policy).parsePolicy(), context)
    // will print 'permit'
    println(resultOwner.first)
    resultOwner.first shouldBe PolicyResultEnum.PERMIT

    // now let's try to update pet info without being owner
    val notOwner = json.decodeFromString<Map<String, String?>>("""{"username" : "mSmith"}""")

    val newContext = Context(subject = notOwner, request = request)
    val resultNotOwner =
        PolicyEngine().evaluatePolicy(PEELParser(policy).parsePolicy(), newContext)
    // will print 'deny'
    println(resultNotOwner.first)
    resultNotOwner.first shouldBe PolicyResultEnum.DENY
}
```

This example shows how easy it is to define a policy and evaluate it by using PolicyEngine expression language. It is simple example that can evolve into more complex policies, depending on the requirements.

Engine can support multiple variations on input stores (strings, maps, objects). It can also work with old and new object versions at the same time, if data model was upgraded.

Business requirements can grow more complex over time, like covering read action to be permitted for everyone or providing welcome or warning message for users.

All these examples are covered in the [Pet Example](https://ivsokol.github.io/policy-engine/docs/examples/pet-example/)
page, and in repository [PetTest](https://github.com/ivsokol/policy-engine/blob/main/src/test/kotlin/io/github/ivsokol/poe/examples/catalog/PetTest.kt).

## Competitive advantage over other libraries

Looking at the simple example above, one can arrive at the conclusion that it is easier to implement the same
functionality by using simple conditional statements in code. And that is correct. If you need to define static policies,
then it is easier and faster to write the code.

Problem with static code arises when you need to define policies that can be changed over time. In that case, if you write static code, every time 
you need to change the policy, you need to recompile the code and deploy your application.

There are ways to solve this problem by using dynamic languages, like Groovy or JavaScript, but that leads to the restriction that 
only individuals experienced in those languages can create or change the policy.

Main goal of this library is to provide an engine that evaluates policies based on provided policy catalog (it serves as Policy Decision Point - **PDP**; check [here](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047068) and [here](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047088) for more details).

Policy definitions (hundreds or thousands of them) can be defined in a separate entity called Policy Administration
Point - **PAP**. This entity can be a separate application, with its own GUI and API optimized for Policy creation. It can be adjusted for business users, to allow them to create policies or conditions by populating predefined forms for such entities, or by using AI to generate policies.

For all other simple cases, Policy Engine Expression Language can be used to define policies in a declarative way.

By having this kind of architecture, you can define policies in a declarative way, and then use the same policies in
different applications and change them in runtime (for example by providing a catalog through a REST API or pulling it from a database). In that way you can deploy new catalog version 
without redeploying your application.

Another advantage is that policies are not only providing a result (permit/deny), but also additional information, if such information is needed. In the [variation](https://ivsokol.github.io/policy-engine/docs/examples/pet-example#pet-owner-can-update-pet-information-two-variations-of-pet-object) of simple pet example, it is possible to define a message (static or dynamic) for the end user, and such message can be pulled from the context data store after evaluation.

To summarize, these are advantages of using PoE:

* **Catalog driven** - Policy Engine runs on provided Policy Catalog definitions
* **Dynamic behaviour** - Policy Catalog can be defined in a separate application, and then loaded into the engine, thus supporting dynamic change without application redeployment.
* **Declarative** - Policy Catalog can be defined in a declarative way, using Policy Engine Expression Language.
* **Additional information** - Policy Engine can provide additional information, like message for the end user, or a
  list of resources that are permitted or denied together with Policy evaluation result.
* **Policy and condition evaluation** - Policy Engine can evaluate both policy and condition expressions.
* **Adaptable to any input data model** - Policy Engine can work with any data model, as long as it is possible to convert it to a string or a map.
* **Multi-tenancy** - Policy Engine can work with multiple tenants, by using different Policy Catalogs for each tenant.
* **Customizable** - Policy Engine can be customized to fit your needs. Every Policy Engine entity has a set of options that 
  can be defined, thus adjusting execution behaviour of an entity.

## Documentation

Full documentation can be found on [ivsokol.github.io/policy-engine](https://ivsokol.github.io/policy-engine) page.

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
* **[PolicyCatalog](https://ivsokol.github.io/policy-engine/docs/policy-catalog.md)** - a collection of all engine entities.
* **[Policy](https://ivsokol.github.io/policy-engine/docs/policy.md)** - entity that evaluates a condition or other Policies and returns result with possible side effects.
* **[PolicyCondition](https://ivsokol.github.io/policy-engine/docs/policy-condition.md)** - entity that evaluates a condition and returns boolean result.
* **[PolicyVariable](https://ivsokol.github.io/policy-engine/docs/policy-variable.md)** - entity that represents a dynamic or static variable that can be used in a Policy or PolicyCondition.
* **[PolicyAction](https://ivsokol.github.io/policy-engine/docs/policy-action.md)** - entity that executes a side effect of a Policy evaluation.

## Getting Started

In order to use PoE in your project, you need to add the following dependency to your project:

```kotlin
implementation("io.github.ivsokol:poe:1.2.0")
```

After that you need to define a PolicyCatalog and instantiate a PolicyEngine.

Examples below are **code snippets** and not full examples. If you want to check full examples with explanations, please
visit [examples](https://ivsokol.github.io/policy-engine/docs/examples/) page,
or check catalog tests in [repository](https://github.com/ivsokol/policy-engine/tree/main/src/test/kotlin/io/github/ivsokol/poe/examples/catalog).

```kotlin
val catalog = "..."
val engine = PolicyEngine(catalog)
```

Once PolicyEngine is instantiated, you can start evaluating policies and conditions by defining a Context. What can be provided in Context is up to the client. Usually request data is put in request store (it can contain body of a request, headers, metadata, ...),
security data (username, roles, ...) is put in subject store and server related data is put in the environment store.
Context stores are maps, as explained in [Context stores](https://ivsokol.github.io/policy-engine/docs/context.md#stores) page.
One context should be created for each evaluation.

```kotlin
val context = Context(
    request = mapOf("body" to requestData),
    subject = mapOf("principal" to userData),
)
```

Then you can evaluate a policy by calling `evaluate` method or condition by calling `check` method on PolicyEngine. There are different methods defined, where you can execute method by id, Reference, label, list of ids or references. You can also execute all Conditions
or Policies in the catalog. All possible methods are described in [PolicyEngine](https://ivsokol.github.io/policy-engine/docs/policy-engine.md) page.

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
