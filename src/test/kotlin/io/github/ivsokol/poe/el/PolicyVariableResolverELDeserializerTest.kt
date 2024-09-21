package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.variable.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PolicyVariableResolverELDeserializerTest :
    DescribeSpec({
      describe("basic parsing") {
        it("should parse correct resolver") {
          val given = """*key(foo)"""
          val expected =
              PolicyVariableResolver(key = "foo", engine = PolicyVariableResolverEngineEnum.KEY)

          val actual = PEELParser(given).parseVariableResolver()
          actual shouldBe expected
        }

        it("should parse correct resolver with spaces") {
          val given = """*key(    foo    )"""
          val expected =
              PolicyVariableResolver(key = "foo", engine = PolicyVariableResolverEngineEnum.KEY)
          val actual = PEELParser(given).parseVariableResolver()
          actual shouldBe expected
        }
        it("should parse correct resolver with saved spaces") {
          val given = """*key("    foo    ")"""
          val expected =
              PolicyVariableResolver(
                  key = "    foo    ", engine = PolicyVariableResolverEngineEnum.KEY)
          val actual = PEELParser(given).parseVariableResolver()
          actual shouldBe expected
        }
        it("should parse correct wrapped resolver 1") {
          val given = """*key("foo")"""
          val expected =
              PolicyVariableResolver(key = "foo", engine = PolicyVariableResolverEngineEnum.KEY)
          val actual = PEELParser(given).parseVariableResolver()
          actual shouldBe expected
        }
        it("should parse correct wrapped resolver 2") {
          val given = """*key(`foo`)"""
          val expected =
              PolicyVariableResolver(key = "foo", engine = PolicyVariableResolverEngineEnum.KEY)
          val actual = PEELParser(given).parseVariableResolver()
          actual shouldBe expected
        }
        it("should parse correct wrapped resolver 3") {
          val given = "*key(" + getWrapper() + "foo" + getWrapper() + ")"
          val expected =
              PolicyVariableResolver(key = "foo", engine = PolicyVariableResolverEngineEnum.KEY)
          val actual = PEELParser(given).parseVariableResolver()
          actual shouldBe expected
        }

        it("should throw exception on no close command") {
          val given = """*key(foo"""
          val actual =
              shouldThrow<IllegalArgumentException> { PEELParser(given).parseVariableResolver() }
          actual.message shouldContain "Expected command end"
        }

        it("should throw exception on multiple content") {
          val given = """*key("foo","bar",#opts(id=stat))"""
          val actual =
              shouldThrow<IllegalStateException> { PEELParser(given).parseVariableResolver() }
          actual.message shouldContain
              "Too many arguments on position 0 for command '*key'. Expected: 1, actual: 2"
        }

        it("should throw on non dictionary chars") {
          val given = """*key&/(    foo    )"""
          val actual =
              shouldThrow<IllegalArgumentException> { PEELParser(given).parseVariableResolver() }
          actual.message shouldBe "Unknown command *key&/ on position 0"
        }

        it("should throw exception on bad command") {
          val given = """*gt(22"""
          val actual =
              shouldThrow<IllegalArgumentException> { PEELParser(given).parseVariableResolver() }
          actual.message shouldContain
              "Root entity type mismatch for command '*gt'. Expected: 'VALUE_RESOLVER', actual: 'CONDITION_ATOMIC'"
        }

        it("should throw exception on child command") {
          val given = """*key(*gt(22))"""
          val actual =
              shouldThrow<IllegalStateException> { PEELParser(given).parseVariableResolver() }
          actual.message shouldContain "Child command type mismatch on position"
        }

        it("should throw exception on short command") {
          val given = """*g"""
          val actual =
              shouldThrow<IllegalArgumentException> { PEELParser(given).parseVariableResolver() }
          actual.message shouldContain "Command too short on position 0"
        }

        it("should throw exception on bad command start ") {
          val given = """*key{(foo)"""
          val actual =
              shouldThrow<IllegalArgumentException> { PEELParser(given).parseVariableResolver() }
          actual.message shouldContain "Unknown command *key{ on position 0"
        }
      }
      describe("options parsing") {
        it("should parse correct resolver with no options") {
          val given = """*key(foo)"""
          val expected =
              PolicyVariableResolver(key = "foo", engine = PolicyVariableResolverEngineEnum.KEY)

          val actual = PEELParser(given).parseVariableResolver()
          actual shouldBe expected
        }
        it("should parse correct managed options") {
          val given =
              """*key(foo,#opts(id=stat,ver=1.2.3,desc="This is description with spaces",labels=foo|bar|a b))"""
          val expected =
              PolicyVariableResolver(
                  id = "stat",
                  version = SemVer(1, 2, 3),
                  description = "This is description with spaces",
                  labels = listOf("foo", "bar", "a b"),
                  key = "foo",
                  engine = PolicyVariableResolverEngineEnum.KEY)

          val actual = PEELParser(given).parseVariableResolver()
          actual shouldBe expected
        }
        it("should parse correct full options") {
          val given =
              """*key(foo,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo|bar,source=subject))"""
          val expected =
              PolicyVariableResolver(
                  id = "stat",
                  version = SemVer(1, 2, 3, "alpha", "label1"),
                  description = "This is description with spaces",
                  labels = listOf("foo", "bar"),
                  key = "foo",
                  engine = PolicyVariableResolverEngineEnum.KEY,
                  source = ContextStoreEnum.SUBJECT)

          val actual = PEELParser(given).parseVariableResolver()
          actual shouldBe expected
        }
      }
      describe("resolver variations") {
        it("should parse key resolver") {
          val given = """*key(.object.a1)"""
          val expected =
              PolicyVariableResolver(
                  key = ".object.a1", engine = PolicyVariableResolverEngineEnum.KEY)
          val actual = PEELParser(given).parseVariableResolver()
          actual shouldBe expected
        }
        it("should parse jq resolver") {
          val given = """*jq(.object.a1)"""
          val expected =
              PolicyVariableResolver(
                  path = ".object.a1", engine = PolicyVariableResolverEngineEnum.JQ)
          val actual = PEELParser(given).parseVariableResolver()
          actual shouldBe expected
        }
        it("should parse jq resolver with options") {
          val given =
              """*jq(.object.a1,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo,source=subject,key=k))"""
          val expected =
              PolicyVariableResolver(
                  id = "stat",
                  version = SemVer(1, 2, 3, "alpha", "label1"),
                  description = "This is description with spaces",
                  labels = listOf("foo"),
                  path = ".object.a1",
                  key = "k",
                  engine = PolicyVariableResolverEngineEnum.JQ,
                  source = ContextStoreEnum.SUBJECT)
          val actual = PEELParser(given).parseVariableResolver()
          actual shouldBe expected
        }
        it("should parse jmespath resolver") {
          val given = """*path(object.a1)"""
          val expected =
              PolicyVariableResolver(
                  path = "object.a1", engine = PolicyVariableResolverEngineEnum.JMES_PATH)
          val actual = PEELParser(given).parseVariableResolver()
          actual shouldBe expected
        }
        it("should parse jmespath resolver with options") {
          val given =
              """*path(object.a1,#opts(id=stat,ver=1.2.3-alpha+label1,desc="This is description with spaces",labels=foo,source=subject,key=k))"""
          val expected =
              PolicyVariableResolver(
                  id = "stat",
                  version = SemVer(1, 2, 3, "alpha", "label1"),
                  description = "This is description with spaces",
                  labels = listOf("foo"),
                  path = "object.a1",
                  key = "k",
                  engine = PolicyVariableResolverEngineEnum.JMES_PATH,
                  source = ContextStoreEnum.SUBJECT)
          val actual = PEELParser(given).parseVariableResolver()
          actual shouldBe expected
        }
      }
    })
