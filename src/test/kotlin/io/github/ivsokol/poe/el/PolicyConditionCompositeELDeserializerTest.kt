package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.condition.*
import io.github.ivsokol.poe.variable.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PolicyConditionCompositeELDeserializerTest :
    DescribeSpec({
      describe("basic parsing") {
        it("should parse correct condition") {
          val given = """*all(#ref(pcr1,1.2.3),#ref(pcr2))"""
          val expected =
              PolicyConditionComposite(
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions =
                      listOf(
                          PolicyConditionRef("pcr1", SemVer(1, 2, 3)), PolicyConditionRef("pcr2")))
          val actual = PEELParser(given).parseCondition()
          actual shouldBe expected
        }
        it("should parse correct condition with saved positions") {
          val given =
              """*any(#ref(pcr1,1.2.3), *gt(#ref(pvd1), #ref(pvd2)),*all(#ref(pcr1,1.2.3),#ref(pcr2)),#true())"""
          val polCond1 = PolicyConditionRef("pcr1", SemVer(1, 2, 3))
          val polCond2 =
              PolicyConditionAtomic(
                  operation = OperationEnum.GREATER_THAN,
                  args =
                      listOf(
                          PolicyVariableRef(id = "pvd1"),
                          PolicyVariableRef(id = "pvd2"),
                      ))
          val polCond3 =
              PolicyConditionComposite(
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                  conditions =
                      listOf(
                          PolicyConditionRef("pcr1", SemVer(1, 2, 3)), PolicyConditionRef("pcr2")))
          val polCond4 = PolicyConditionDefault(true)
          val expected =
              PolicyConditionComposite(
                  conditionCombinationLogic = ConditionCombinationLogicEnum.ANY_OF,
                  conditions = listOf(polCond1, polCond2, polCond3, polCond4))
          val actual = PEELParser(given).parseCondition() as? PolicyConditionComposite
          actual shouldBe expected
          if (actual != null) {
            actual.conditions[0] shouldBe polCond1
            actual.conditions[1] shouldBe polCond2
            actual.conditions[2] shouldBe polCond3
            actual.conditions[3] shouldBe polCond4
          }
        }

        it("should throw exception on no close command") {
          val given = """*all(#ref(pcr1,1.2.3),#ref(pcr2)"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
          actual.message shouldContain "Command not completed on position 0"
        }

        it("should throw exception on content") {
          val given = """*all(#ref(pcr1,1.2.3),content)"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
          actual.message shouldContain "PolicyConditionCompositeDeserializer can not have contents"
        }

        it("should throw exception on bad command") {
          val given = """*all(#int(1), #int(2), #int(3))"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
          actual.message shouldContain
              "Child command type mismatch on position 5 for command '*all'. Expected: 'CONDITION_ATOMIC, CONDITION_COMPOSITE, CONDITION_DEFAULT, REFERENCE', actual: 'VARIABLE_STATIC'"
        }

        it("should throw exception on no child command") {
          val given = """*all(#opts(id=cond))"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
          actual.message shouldContain
              "Not enough arguments on position 0 for command '*all'. Expected: 1, actual: 0"
        }

        it("should throw exception on short command") {
          val given = """*g"""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parseCondition() }
          actual.message shouldContain "Command too short on position 0"
        }
        it("should throw exception on bad command start ") {
          val given = """*all{(#ref(pcr1,1.2.3),#ref(pcr2))"""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parseCondition() }
          actual.message shouldContain "Unknown command *all{ on position 0"
        }
      }
      describe("variations") {
        describe("all") {
          it("should parse unmanaged all") {
            val given = """*all(#ref(pcr1,1.2.3),#ref(pcr2))"""
            val expected =
                PolicyConditionComposite(
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions =
                        listOf(
                            PolicyConditionRef("pcr1", SemVer(1, 2, 3)),
                            PolicyConditionRef("pcr2")))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed all") {
            val given =
                """*all(#ref(pcr1,1.2.3),#ref(pcr2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))"""
            val expected =
                PolicyConditionComposite(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ALL_OF,
                    conditions =
                        listOf(
                            PolicyConditionRef("pcr1", SemVer(1, 2, 3)),
                            PolicyConditionRef("pcr2")))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
        }
        describe("any") {
          it("should parse unmanaged any") {
            val given = """*any(#ref(pcr1,1.2.3),#ref(pcr2))"""
            val expected =
                PolicyConditionComposite(
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ANY_OF,
                    conditions =
                        listOf(
                            PolicyConditionRef("pcr1", SemVer(1, 2, 3)),
                            PolicyConditionRef("pcr2")))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed any") {
            val given =
                """*any(#ref(pcr1,1.2.3),#ref(pcr2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,strictCheck))"""
            val expected =
                PolicyConditionComposite(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    strictCheck = true,
                    conditionCombinationLogic = ConditionCombinationLogicEnum.ANY_OF,
                    conditions =
                        listOf(
                            PolicyConditionRef("pcr1", SemVer(1, 2, 3)),
                            PolicyConditionRef("pcr2")))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
        }
        describe("not") {
          it("should parse unmanaged all") {
            val given = """*not(#ref(pcr1,1.2.3))"""
            val expected =
                PolicyConditionComposite(
                    conditionCombinationLogic = ConditionCombinationLogicEnum.NOT,
                    conditions = listOf(PolicyConditionRef("pcr1", SemVer(1, 2, 3))))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed not") {
            val given =
                """*not(#ref(pcr1,1.2.3),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))"""
            val expected =
                PolicyConditionComposite(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    conditionCombinationLogic = ConditionCombinationLogicEnum.NOT,
                    conditions =
                        listOf(
                            PolicyConditionRef("pcr1", SemVer(1, 2, 3)),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*not(#ref(pcr1,1.2.3),#ref(pcr2))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*not'. Expected: 1, actual: 2"
          }
        }
        describe("nOf") {
          it("should parse unmanaged nOf") {
            val given = """*nOf(#ref(pcr1,1.2.3),#ref(pcr2),#opts(minimumConditions=1))"""
            val expected =
                PolicyConditionComposite(
                    conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                    conditions =
                        listOf(
                            PolicyConditionRef("pcr1", SemVer(1, 2, 3)),
                            PolicyConditionRef("pcr2")),
                    minimumConditions = 1)

            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed nOf") {
            val given =
                """*nOf(#ref(pcr1,1.2.3),#ref(pcr2),#opts(minimumConditions=1,id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,strictCheck,optimize))"""
            val expected =
                PolicyConditionComposite(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    strictCheck = true,
                    optimizeNOfRun = true,
                    minimumConditions = 1,
                    conditionCombinationLogic = ConditionCombinationLogicEnum.N_OF,
                    conditions =
                        listOf(
                            PolicyConditionRef("pcr1", SemVer(1, 2, 3)),
                            PolicyConditionRef("pcr2")))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on no options") {
            val given = """*nOf(#ref(pcr1,1.2.3),#ref(pcr2))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain "Options must be provided for command '*nOf'"
          }
          it("should fail on no minimum conditions") {
            val given = """*nOf(#ref(pcr1,1.2.3),#ref(pcr2),#opts(id=cond))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "#opts(minimumConditions=?) must be provided for command '*nOf'"
          }
        }
      }
    })
