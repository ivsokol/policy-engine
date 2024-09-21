package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.variable.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PolicyVariableDynamicELDeserializerTest :
    DescribeSpec({
      val keyResolver =
          PolicyVariableResolver(key = "foo", engine = PolicyVariableResolverEngineEnum.KEY)
      val jqResolver =
          PolicyVariableResolver(path = "foo", engine = PolicyVariableResolverEngineEnum.JQ)
      val pathResolver =
          PolicyVariableResolver(path = "foo", engine = PolicyVariableResolverEngineEnum.JMES_PATH)
      val resolverRef = PolicyVariableResolverRef(id = "pvr1")
      val resolverRef2 = PolicyVariableResolverRef(id = "pvr1", version = SemVer(1, 2, 3))
      describe("basic parsing") {
        it("should parse correct dynamic variable") {
          val given = """*dyn(*key(foo))"""
          val expected = PolicyVariableDynamic(resolvers = listOf(keyResolver))

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }

        it("should parse dynamic variable with correct resolver positions") {
          val given = """*dyn(*key(foo),*jq(foo),*path(foo),#ref(pvr1),#ref(pvr1,1.2.3))"""
          val expected =
              PolicyVariableDynamic(
                  resolvers =
                      listOf(keyResolver, jqResolver, pathResolver, resolverRef, resolverRef2))
          val actual = PEELParser(given).parseVariable() as PolicyVariableDynamic
          actual shouldBe expected
          actual.resolvers[0] shouldBe keyResolver
          actual.resolvers[1] shouldBe jqResolver
          actual.resolvers[2] shouldBe pathResolver
          actual.resolvers[3] shouldBe resolverRef
          actual.resolvers[4] shouldBe resolverRef2
        }

        it("should throw exception on no close command") {
          val given = """*dyn(*key(foo)"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseVariable() }
          actual.message shouldContain "Command not completed on position 0"
        }

        it("should throw exception on content") {
          val given = """*dyn("foo",*key(foo),#opts(id=stat))"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseVariable() }
          actual.message shouldContain "PolicyVariableDynamicDeserializer can not have contents"
        }

        it("should throw exception on bad command") {
          val given = """*gt(22"""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parseVariable() }
          actual.message shouldContain "Invalid variable command: *gt"
        }

        it("should throw exception on no child command") {
          val given = """*dyn()"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseVariable() }
          actual.message shouldContain
              "Not enough arguments on position 0 for command '*dyn'. Expected: 1, actual: 0"
        }

        it("should throw exception on short command") {
          val given = """*g"""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parseVariable() }
          actual.message shouldContain "Command too short on position 0"
        }

        it("should throw exception on bad command start ") {
          val given = """*key{(*key(foo))"""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parseVariable() }
          actual.message shouldContain "Unknown command *key{ on position 0"
        }
      }
      describe("options parsing") {
        it("should parse correct dynamic variable with no options") {
          val given = """*dyn(*key(foo))"""
          val expected = PolicyVariableDynamic(resolvers = listOf(keyResolver))

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse correct managed options") {
          val given =
              """*dyn(*key(foo),#opts(id=stat,ver=1.2.3,desc="This is description with spaces",labels=foo|bar|a b))"""
          val expected =
              PolicyVariableDynamic(
                  id = "stat",
                  version = SemVer(1, 2, 3),
                  description = "This is description with spaces",
                  labels = listOf("foo", "bar", "a b"),
                  resolvers = listOf(keyResolver))

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse correct full options") {
          val given =
              """*dyn(*key(foo),#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|bar|a b,type=string,format=date,dateFormat=dd.MM.yyyy))"""
          val expected =
              PolicyVariableDynamic(
                  id = "stat",
                  version = SemVer(1, 2, 3, "alpha", "label1"),
                  description = "This is description with spaces",
                  labels = listOf("foo", "bar", "a b"),
                  resolvers = listOf(keyResolver),
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.DATE,
                  dateFormat = "dd.MM.yyyy")

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse correct full options 2") {
          val given =
              """*dyn(*key(foo),#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|bar,type=string,format=time,timeFormat=HH:mm:ss))"""
          val expected =
              PolicyVariableDynamic(
                  id = "stat",
                  version = SemVer(1, 2, 3, "alpha", "label1"),
                  description = "This is description with spaces",
                  labels = listOf("foo", "bar"),
                  resolvers = listOf(keyResolver),
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.TIME,
                  timeFormat = "HH:mm:ss")

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
        it("should parse correct full options 3") {
          val given =
              """*dyn(*key(foo),#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|bar,type=string,format=date-time,dateTimeFormat="dd.MM.yyyy HH:mm:ss"))"""
          val expected =
              PolicyVariableDynamic(
                  id = "stat",
                  version = SemVer(1, 2, 3, "alpha", "label1"),
                  description = "This is description with spaces",
                  labels = listOf("foo", "bar"),
                  resolvers = listOf(keyResolver),
                  type = VariableValueTypeEnum.STRING,
                  format = VariableValueFormatEnum.DATE_TIME,
                  dateTimeFormat = "dd.MM.yyyy HH:mm:ss")

          val actual = PEELParser(given).parseVariable()
          actual shouldBe expected
        }
      }
    })
