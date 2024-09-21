package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.action.PolicyActionClear
import io.github.ivsokol.poe.action.PolicyActionJsonMerge
import io.github.ivsokol.poe.action.PolicyActionJsonPatch
import io.github.ivsokol.poe.action.PolicyActionSave
import io.github.ivsokol.poe.variable.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PolicyActionELDeserializerTest :
    DescribeSpec({
      describe("basic parsing") {
        it("should parse correct action") {
          val given = """*save(foo,#ref(pvd1))"""
          val expected =
              PolicyActionSave(
                  key = "foo",
                  value = PolicyVariableRef(id = "pvd1"),
              )

          val actual = PEELParser(given).parseAction()
          actual shouldBe expected
        }
        it("should parse correct action for static variable") {
          val given = """*save(foo,#str(bar))"""
          val expected =
              PolicyActionSave(
                  key = "foo",
                  value = PolicyVariableStatic(value = "bar", type = VariableValueTypeEnum.STRING))

          val actual = PEELParser(given).parseAction()
          actual shouldBe expected
        }
        it("should parse correct action for dynamic variable") {
          val given = """*save(foo,*dyn(#ref(pvr1)))"""
          val expected =
              PolicyActionSave(
                  key = "foo",
                  value =
                      PolicyVariableDynamic(resolvers = listOf(PolicyVariableResolverRef("pvr1"))))

          val actual = PEELParser(given).parseAction()
          actual shouldBe expected
        }

        it("should throw exception on no close command") {
          val given = """*save(foo,#ref(pvd1)"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseAction() }
          actual.message shouldContain "Command not completed on position 0"
        }

        it("should throw exception on no content") {
          val given = """*save(#ref(pvd2),#ref(pvd1))"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseAction() }
          actual.message shouldContain "PolicyActionELDeserializer must have content"
        }

        it("should throw exception on bad command") {
          val given = """*save(foo,*gt(#int(1),#int(2)))"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseAction() }
          actual.message shouldContain
              "Child command type mismatch on position 10 for command '*save'. Expected: 'VARIABLE_DYNAMIC, VARIABLE_STATIC, REFERENCE, CONTENT', actual: 'CONDITION_ATOMIC'"
        }

        it("should throw exception on no child command") {
          val given = """*save(foo,bar)"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseAction() }
          actual.message shouldContain "PolicyActionELDeserializer must have only one content"
        }

        it("should throw exception on short command") {
          val given = """*g"""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parseAction() }
          actual.message shouldContain "Command too short on position 0"
        }
        it("should throw exception on bad command start ") {
          val given = """*save{(foo,#ref(pvd1))"""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parseAction() }
          actual.message shouldContain "Unknown command *save{ on position 0"
        }
      }
      describe("variations") {
        describe("save") {
          it("should parse unmanaged save") {
            val given = """*save(foo,#ref(pvd1))"""
            val expected =
                PolicyActionSave(
                    key = "foo",
                    value = PolicyVariableRef(id = "pvd1"),
                )

            val actual = PEELParser(given).parseAction()
            actual shouldBe expected
          }
          it("should parse managed save") {
            val given =
                """*save(foo,#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,failOnMissingKey,failOnExistingKey,failOnNullSource))"""
            val expected =
                PolicyActionSave(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    key = "foo",
                    value = PolicyVariableRef(id = "pvd1"),
                    failOnMissingKey = true,
                    failOnExistingKey = true,
                    failOnNullSource = true)
            val actual = PEELParser(given).parseAction()
            actual shouldBe expected
          }
          it("should throw exception on bad params") {
            val given = """*save(foo)"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseAction() }
            actual.message shouldContain
                "Not enough arguments on position 0 for command '*save'. Expected: 2, actual: 1"
          }
          it("should throw exception on bad params 2") {
            val given = """*save(foo,#ref(pvd1),#ref(pvd2))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseAction() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*save'. Expected: 2, actual: 3"
          }
        }
        describe("clear") {
          it("should parse unmanaged clear") {
            val given = """*clear(foo)"""
            val expected =
                PolicyActionClear(
                    key = "foo",
                )

            val actual = PEELParser(given).parseAction()
            actual shouldBe expected
          }
          it("should parse managed clear") {
            val given =
                """*clear(foo,#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,failOnMissingKey))"""
            val expected =
                PolicyActionClear(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    key = "foo",
                    failOnMissingKey = true,
                )
            val actual = PEELParser(given).parseAction()
            actual shouldBe expected
          }
          it("should throw exception on bad params") {
            val given = """*clear()"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseAction() }
            actual.message shouldContain
                "Not enough arguments on position 0 for command '*clear'. Expected: 1, actual: 0"
          }
          it("should throw exception on bad params 2") {
            val given = """*clear(foo,#ref(pvd1))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseAction() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*clear'. Expected: 1, actual: 2"
          }
          it("should throw exception on bad params 3") {
            val given = """*clear(foo,bar)"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseAction() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*clear'. Expected: 1, actual: 2"
          }
        }
        describe("merge") {
          it("should parse unmanaged merge") {
            val given = """*merge(foo,#ref(pvd1),#ref(pvd2))"""
            val expected =
                PolicyActionJsonMerge(
                    key = "foo",
                    source = PolicyVariableRef(id = "pvd1"),
                    merge = PolicyVariableRef(id = "pvd2"),
                )

            val actual = PEELParser(given).parseAction()
            actual shouldBe expected
          }
          it("should parse managed merge") {
            val given =
                """*merge(foo,#ref(pvd1),#ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,failOnMissingKey,failOnExistingKey,failOnNullSource,failOnNullMerge,type=string,format=time))"""
            val expected =
                PolicyActionJsonMerge(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    key = "foo",
                    source = PolicyVariableRef(id = "pvd1"),
                    merge = PolicyVariableRef(id = "pvd2"),
                    failOnMissingKey = true,
                    failOnExistingKey = true,
                    failOnNullSource = true,
                    failOnNullMerge = true,
                    destinationType = VariableValueTypeEnum.STRING,
                    destinationFormat = VariableValueFormatEnum.TIME)
            val actual = PEELParser(given).parseAction()
            actual shouldBe expected
          }
          it("should throw exception on bad params") {
            val given = """*merge()"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseAction() }
            actual.message shouldContain
                "Not enough arguments on position 0 for command '*merge'. Expected: 3, actual: 0"
          }
          it("should throw exception on bad params 2") {
            val given = """*merge(#ref(pvd0),#ref(pvd1),#ref(pvd2),#ref(pvd3))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseAction() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*merge'. Expected: 3, actual: 4"
          }
          it("should throw exception on bad params 3") {
            val given = """*merge(foo,bar,baz,qux)"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseAction() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*merge'. Expected: 3, actual: 4"
          }
        }
        describe("patch") {
          it("should parse unmanaged patch") {
            val given = """*patch(foo,#ref(pvd1),#ref(pvd2))"""
            val expected =
                PolicyActionJsonPatch(
                    key = "foo",
                    source = PolicyVariableRef(id = "pvd1"),
                    patch = PolicyVariableRef(id = "pvd2"),
                )

            val actual = PEELParser(given).parseAction()
            actual shouldBe expected
          }
          it("should parse managed patch") {
            val given =
                """*patch(foo,#ref(pvd1),#ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,failOnMissingKey,failOnExistingKey,failOnNullSource,castNullSourceToArray))"""
            val expected =
                PolicyActionJsonPatch(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    key = "foo",
                    source = PolicyVariableRef(id = "pvd1"),
                    patch = PolicyVariableRef(id = "pvd2"),
                    failOnMissingKey = true,
                    failOnExistingKey = true,
                    failOnNullSource = true,
                    castNullSourceToArray = true)
            val actual = PEELParser(given).parseAction()
            actual shouldBe expected
          }
          it("should throw exception on bad params") {
            val given = """*patch()"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseAction() }
            actual.message shouldContain
                "Not enough arguments on position 0 for command '*patch'. Expected: 3, actual: 0"
          }
          it("should throw exception on bad params 2") {
            val given = """*patch(#ref(pvd0),#ref(pvd1),#ref(pvd2),#ref(pvd3))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseAction() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*patch'. Expected: 3, actual: 4"
          }
          it("should throw exception on bad params 3") {
            val given = """*patch(foo,bar,baz,qux)"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseAction() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*patch'. Expected: 3, actual: 4"
          }
        }
      }
    })
