package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.condition.PolicyConditionDefault
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PolicyConditionDefaultELDeserializerTest :
    DescribeSpec({
      describe("basic parsing") {
        it("should parse correct condition - true") {
          val given = """#true()"""
          val expected = PolicyConditionDefault(true)
          val actual = PEELParser(given).parseCondition()
          actual shouldBe expected
        }
        it("should parse correct condition - false") {
          val given = """#false()"""
          val expected = PolicyConditionDefault(false)
          val actual = PEELParser(given).parseCondition()
          actual shouldBe expected
        }
        it("should parse correct condition - null") {
          val given = """#null()"""
          val expected = PolicyConditionDefault(null)
          val actual = PEELParser(given).parseCondition()
          actual shouldBe expected
        }

        it("should throw exception on no close command") {
          val given = """#true("""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
          actual.message shouldContain "Command not completed on position 0"
        }

        it("should throw exception on content") {
          val given = """#true(content)"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
          actual.message shouldContain
              "Too many arguments on position 0 for command '#true'. Expected: 0, actual: 1"
        }

        it("should throw exception on child command") {
          val given = """#true(*gt(#int(1), #int(2)))"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
          actual.message shouldContain
              "Child command type mismatch on position 6 for command '#true'. Expected: '', actual: 'CONDITION_ATOMIC'"
        }

        it("should throw exception on options") {
          val given = """#true(#opts(id=cond))"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
          actual.message shouldContain "PolicyConditionDefaultDeserializer can not have options"
        }

        it("should throw exception on bad command start ") {
          val given = """#true{()"""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parseCondition() }
          actual.message shouldContain "Unknown command #true{ on position 0"
        }
      }
    })
