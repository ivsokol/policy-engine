package io.github.ivsokol.poe.examples.catalog

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.PolicyEngine
import io.github.ivsokol.poe.el.PEELParser
import io.github.ivsokol.poe.policy.PolicyResultEnum
import io.github.ivsokol.poe.variable.toJsonAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class PetTest :
    FunSpec({
      val json = Json {
        explicitNulls = false
        encodeDefaults = true
        prettyPrint = true
      }

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

      @Serializable data class Pet(val kind: String, val name: String, val owner: String)

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
    })
