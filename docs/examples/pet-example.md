---
title: Pet example
parent: Examples
description: Pet example PolicyEngine example
nav_order: 1
---
# Pet example
{: .no_toc }

<details markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

In this example, we want to create a simple policy to check if a user who is trying to do an action (read or update) on Pet object is allowed to do that.

Example will cover following variations:

* only pet owner can update pet information
* only pet owner can update pet information - variation where data is provided as Pet data class
* only pet owner can update pet information - variation where additional message is provided in the result
* everyone can read pet information, only owner can update pet information - realized through composite conditions
* everyone can read pet information, only owner can update pet information - realized through policy sets
* only pet owner can update pet information - variation where two kind of pet objects are allowed 


As this is simple example, we will define Policy entities through Policy Engine Expression Language (PEEL). All examples code can be found in the [repository](https://github.com/ivsokol/policy-engine/blob/main/src/test/kotlin/io/github/ivsokol/poe/examples/catalog/PetTest.kt).

## Pet owner can update pet information

In this example, we want to create a simple policy to check if a user who is trying to do an update action on
Pet object is allowed to do that. Input data will be provided as string.

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

Policy is defined as Permit with one condition that checks if username from subject store is equal to owner of the pet. As username is a simple 
string, we can use key resolver to get it from subject store.
Pet is a complex object so we are using JQ to resolve owner information.
After extracting these values, we can compare them using equals condition and return result.

## Pet owner can update pet information - data class variation

This is same example as previous one, but this time we are using data class to represent pet object and map to store 
subject data.

```kotlin
      @Serializable 
      data class Pet(val kind: String, val name: String, val owner: String)

      test("only pet owner can update pet information - object variant") {
        // define owner in subject store, object version
        val owner = mapOf("username" to "jDoe")
        // prepare request payload, object variant
        val request = mapOf("pet" to Pet("dog", "Fido", "jDoe"), "action" to "update")
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

## Pet owner can update pet information - variation with additional message

In this example we want to add additional message to the result. If user is the owner, message will be a dynamic welcome string in format `"Hi " + .owner + ", how is " + .name + " today?"`. If user is not the owner, message will be static string `"Sorry, this is not your pet."`.

```kotlin
test("only pet owner can update pet information with message") {
  // define owner in subject store
  val owner = json.decodeFromString<Map<String, String?>>("""{"username" : "jDoe"}""")
  // prepare request payload
  val request =
      mapOf(
          "pet" to """{"kind": "dog", "name" : "Fido","owner" : "jDoe"}""".toJsonAny(),
          "action" to "update")
  // define policy with static and dynamic messages
  val policy =
      """
          *permit(
              *eq(
                  *dyn(*key(username,#opts(source=subject))),
                  *dyn(*jq(.pet.owner))
              ),
              *act(
                  *save(message,
                      *dyn(
                              *jq(`"Hi " + .owner + ", how is " + .name + " today?"`,#opts(key = pet))
                           )
                      ),
              #opts(executionMode=onPermit)
              ),
              *act(*save(message,#str("Sorry, this is not your pet.")),#opts(executionMode=onDeny)),
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
  // should print 'Hi jDoe, how is Fido today?'
  println(context.dataStore()["message"])
  // extract message from context data store
  context.dataStore()["message"] shouldBe "Hi jDoe, how is Fido today?"

  // now let's try to update pet info without being owner
  val notOwner = json.decodeFromString<Map<String, String?>>("""{"username" : "mSmith"}""")
  val newContext = Context(subject = notOwner, request = request)
  val resultNotOwner =
      PolicyEngine().evaluatePolicy(PEELParser(policy).parsePolicy(), newContext)
  // will print 'deny'
  println(resultNotOwner.first)
  resultNotOwner.first shouldBe PolicyResultEnum.DENY
  // should print 'Sorry, this is not your pet.'
  println(newContext.dataStore()["message"])
  newContext.dataStore()["message"] shouldBe "Sorry, this is not your pet."
}
```

In this example we can see two actions, one executed on permit result that returns dynamic message based on request data and calculated through JQ,
and another message that is executed on deny result and returns static string.

Here we can also see how policies can quickly become more complex. This is why it is crucial for such policies to be defined in a separate entity (Policy Administration Point - PAP) that handles Policy Catalog administration (but not execution).

## Pet owner can update pet information, other users can read pet information - condition variation

In this example we want to allow all users to read pet information, but only owner can update pet information.

```kotlin
test("everyone can see pets, only owner can update info") {
  // define owner in subject store
  val owner = json.decodeFromString<Map<String, String?>>("""{"username" : "jDoe"}""")
  // define another user in subject store
  val notOwner = json.decodeFromString<Map<String, String?>>("""{"username" : "mSmith"}""")
  // prepare request payload for update
  val requestUpdate =
      mapOf(
          "pet" to """{"kind": "dog", "name" : "Fido","owner" : "jDoe"}""".toJsonAny(),
          "action" to "update")
  // prepare request payload for read
  val requestRead =
      mapOf(
          "pet" to """{"kind": "dog", "name" : "Fido","owner" : "jDoe"}""".toJsonAny(),
          "action" to "read")
  // define policy, condition variant
  val policy =
      """
          *permit(
              *any(
                  *eq(
                      #str(read),
                      *dyn(*key(action))
                  ),
                  *all(
                      *eq(
                          *dyn(*key(username,#opts(source=subject))),
                          *dyn(*jq(.pet.owner))
                      ),
                      *eq(
                          #str(update),
                          *dyn(*key(action))
                      ),
                  )
              ),
              #opts(strictTargetEffect)
          )
      """
          .trimIndent()
  // let's try to get pet info for owner
  val context = Context(subject = owner, request = requestRead)
  val resultOwner = PolicyEngine().evaluatePolicy(PEELParser(policy).parsePolicy(), context)
  // will print 'permit'
  println(resultOwner.first)
  resultOwner.first shouldBe PolicyResultEnum.PERMIT

  // let's try to get pet info for not owner
  val newContextNotOwnerRead = Context(subject = notOwner, request = requestRead)
  val resultNotOwnerRead =
      PolicyEngine().evaluatePolicy(PEELParser(policy).parsePolicy(), newContextNotOwnerRead)
  // will print 'permit'
  println(resultNotOwnerRead.first)
  resultNotOwnerRead.first shouldBe PolicyResultEnum.PERMIT

  // let's try to update pet info for owner
  val newContext = Context(subject = owner, request = requestUpdate)
  val resultOwnerUpdate =
      PolicyEngine().evaluatePolicy(PEELParser(policy).parsePolicy(), newContext)
  // will print 'permit'
  println(resultOwnerUpdate.first)
  resultOwnerUpdate.first shouldBe PolicyResultEnum.PERMIT

  // let's try to update pet info for not owner
  val newContextNotOwnerUpdate = Context(subject = notOwner, request = requestUpdate)
  val resultNotOwnerUpdate =
      PolicyEngine()
          .evaluatePolicy(PEELParser(policy).parsePolicy(), newContextNotOwnerUpdate)
  // will print 'deny'
  println(resultNotOwnerUpdate.first)
  resultNotOwnerUpdate.first shouldBe PolicyResultEnum.DENY
}
```

Policy was still realized as a single policy, but now we have a composite condition that is checking if **any** of the following conditions is true:

- action is read
- action is update **and** username is the same as pet owner

## Pet owner can update pet information, other users can read pet information - policy set variation

In this example we want to allow all users to read pet information, but only owner can update pet information, this time to be realized through PolicySet.

```kotlin
test("everyone can see pets, only owner can update info - policy set version") {
  // define owner in subject store
  val owner = json.decodeFromString<Map<String, String?>>("""{"username" : "jDoe"}""")
  // define another user in subject store
  val notOwner = json.decodeFromString<Map<String, String?>>("""{"username" : "mSmith"}""")
  // prepare request payload for update
  val requestUpdate =
      mapOf(
          "pet" to """{"kind": "dog", "name" : "Fido","owner" : "jDoe"}""".toJsonAny(),
          "action" to "update")
  // prepare request payload for read
  val requestRead =
      mapOf(
          "pet" to """{"kind": "dog", "name" : "Fido","owner" : "jDoe"}""".toJsonAny(),
          "action" to "read")
  // define policy, policySet variant
  val policy =
      """
      *firstAppl(   
          *permit(
              *eq(
                      #str(read),
                      *dyn(*key(action))
                  )
          ),
          *permit(
              *all(
                  *eq(
                      *dyn(*key(username,#opts(source=subject))),
                      *dyn(*jq(.pet.owner))
                  ),
                  *eq(
                      #str(update),
                      *dyn(*key(action))
                  ),
              )
          ),
          #deny()
      )    
      """
          .trimIndent()
  // let's try to get pet info for owner
  val context = Context(subject = owner, request = requestRead)
  val resultOwner = PolicyEngine().evaluatePolicy(PEELParser(policy).parsePolicy(), context)
  // will print 'permit'
  println(resultOwner.first)
  resultOwner.first shouldBe PolicyResultEnum.PERMIT

  // let's try to get pet info for not owner
  val newContextNotOwnerRead = Context(subject = notOwner, request = requestRead)
  val resultNotOwnerRead =
      PolicyEngine().evaluatePolicy(PEELParser(policy).parsePolicy(), newContextNotOwnerRead)
  // will print 'permit'
  println(resultNotOwnerRead.first)
  resultNotOwnerRead.first shouldBe PolicyResultEnum.PERMIT

  // let's try to update pet info for owner
  val newContext = Context(subject = owner, request = requestUpdate)
  val resultOwnerUpdate =
      PolicyEngine().evaluatePolicy(PEELParser(policy).parsePolicy(), newContext)
  // will print 'permit'
  println(resultOwnerUpdate.first)
  resultOwnerUpdate.first shouldBe PolicyResultEnum.PERMIT

  // let's try to update pet info for not owner
  val newContextNotOwnerUpdate = Context(subject = notOwner, request = requestUpdate)
  val resultNotOwnerUpdate =
      PolicyEngine()
          .evaluatePolicy(PEELParser(policy).parsePolicy(), newContextNotOwnerUpdate)
  // will print 'deny'
  println(resultNotOwnerUpdate.first)
  resultNotOwnerUpdate.first shouldBe PolicyResultEnum.DENY
}
```

This policy is realized through PolicySet of type **First Applicable**, which runs through all child policies and returns first one that resolves to permit or deny. Child policies don't have `strictTargetEffect` flag, so they will return `NotApplicable`
result if underlying conditions are not met.
There are three child policies in this policy set:

- first policy checks if action is read
- second policy checks if action is update **and** username is the same as pet owner
- third policy is `deny` policy, which will be returned if none of the above conditions are met

## Pet owner can update pet information, two variations of Pet object

In this example we want to handle two variations of Pet object, one with `owner` property is at the top level and one where owner information is moved to `ownerInfo` property.

```kotlin
test("should work with two kind of pet objects") {
  // define owner in subject store
  val owner = json.decodeFromString<Map<String, String?>>("""{"username" : "jDoe"}""")
  // define request payload
  val request =
      mapOf(
          "pet" to """{"kind": "dog", "name" : "Fido","owner" : "jDoe"}""".toJsonAny(),
          "action" to "update")
  // define request payload with upgraded pet object
  val requestV2 =
      mapOf(
          "pet" to
              """{"kind": "dog", "name" : "Fido","ownerInfo" : { "username" : "jDoe", "name" : "John Doe"}}"""
                  .toJsonAny(),
          "action" to "update")
  // define policy where policy variable resolovers are working with both pet objects
  val policy =
      """
          *permit(
              *eq(
                  *dyn(*key(username,#opts(source=subject))),
                  *dyn(*jq(.pet.ownerInfo.username),*jq(.pet.owner))
              ),
              #opts(strictTargetEffect)
          )
      """
          .trimIndent()
  val context = Context(subject = owner, request = request)
  val resultOwner = PolicyEngine().evaluatePolicy(PEELParser(policy).parsePolicy(), context)
  // will print 'permit'
  println(resultOwner.first)
  resultOwner.first shouldBe PolicyResultEnum.PERMIT

  // now let's try to send new pet object
  val newContext = Context(subject = owner, request = requestV2)
  val resultOwnerV2 =
      PolicyEngine().evaluatePolicy(PEELParser(policy).parsePolicy(), newContext)
  // will print 'permit'
  println(resultOwnerV2.first)
  resultOwnerV2.first shouldBe PolicyResultEnum.PERMIT
}
```

This example is same to the first example, but with one difference: there is another request payload with upgraded pet
object. We can see that slightly upgraded policy (where there are two resolvers for owner information: one looking at `pet.owner` field and another looking at `pet.ownerInfo.username` field) is able to handle both pet objects.

Resolvers work in a way that non-null result of any resolver is returned as a value, and other resolvers are then skipped.

This shows general concept where data model can be changed, and policy can be adjusted to handle new data model without restarting Policy Engine app, as only new definitions are needed to be loaded into engine.