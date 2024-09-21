package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.SemVer
import io.github.ivsokol.poe.condition.OperationEnum
import io.github.ivsokol.poe.condition.PolicyConditionAtomic
import io.github.ivsokol.poe.variable.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PolicyConditionAtomicELDeserializerTest :
    DescribeSpec({
      describe("basic parsing") {
        it("should parse correct condition") {
          val given = """*gt(#int(1), #int(2))"""
          val expected =
              PolicyConditionAtomic(
                  operation = OperationEnum.GREATER_THAN,
                  args =
                      listOf(
                          PolicyVariableStatic(value = 1, type = VariableValueTypeEnum.INT),
                          PolicyVariableStatic(value = 2, type = VariableValueTypeEnum.INT),
                      ))
          val actual = PEELParser(given).parseCondition()
          actual shouldBe expected
        }
        it("should parse correct condition with saved positions") {
          val given = """*gt(#ref(pvd2,1.2.3), #ref(pvd1))"""
          val expected =
              PolicyConditionAtomic(
                  operation = OperationEnum.GREATER_THAN,
                  args =
                      listOf(
                          PolicyVariableRef(id = "pvd2", SemVer(1, 2, 3)),
                          PolicyVariableRef(id = "pvd1"),
                      ))
          val actual = PEELParser(given).parseCondition() as? PolicyConditionAtomic
          actual shouldBe expected
          if (actual != null) {
            actual.args[0] shouldBe PolicyVariableRef(id = "pvd2", SemVer(1, 2, 3))
            actual.args[1] shouldBe PolicyVariableRef(id = "pvd1")
          }
        }

        it("should parse variables") {
          val given = """*gt(#int(1), *dyn(*key(foo)))"""
          val expected =
              PolicyConditionAtomic(
                  operation = OperationEnum.GREATER_THAN,
                  args =
                      listOf(
                          PolicyVariableStatic(value = 1, type = VariableValueTypeEnum.INT),
                          PolicyVariableDynamic(
                              resolvers =
                                  listOf(
                                      PolicyVariableResolver(
                                          key = "foo",
                                          engine = PolicyVariableResolverEngineEnum.KEY))),
                      ))
          val actual = PEELParser(given).parseCondition()
          actual shouldBe expected
        }

        it("should throw exception on extra parameters") {
          val given = """*gt(#int(1), #int(2), #int(3))"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
          actual.message shouldContain
              "Too many arguments on position 0 for command '*gt'. Expected: 2, actual: 3"
        }

        it("should throw exception on no close command") {
          val given = """*gt(#int(1), #int(2)"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
          actual.message shouldContain "Command not completed on position 0"
        }

        it("should throw exception on content") {
          val given = """*gt(#int(1), content)"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
          actual.message shouldContain "PolicyConditionAtomicDeserializer can not have contents"
        }

        it("should throw exception on bad command") {
          val given = """*gt(*gt(#ref(pvd1), #ref(pvd2)), #int(2))"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
          actual.message shouldContain
              "Child command type mismatch on position 4 for command '*gt'. Expected: 'VARIABLE_DYNAMIC, VARIABLE_STATIC, REFERENCE', actual: 'CONDITION_ATOMIC'"
        }

        it("should throw exception on no child command") {
          val given = """*gt(#opts(strictCheck))"""
          val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
          actual.message shouldContain
              "Not enough arguments on position 0 for command '*gt'. Expected: 2, actual: 0"
        }

        it("should throw exception on short command") {
          val given = """*g"""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parseCondition() }
          actual.message shouldContain "Command too short on position 0"
        }
        it("should throw exception on bad command start ") {
          val given = """*gt{(#int(1), #int(2))"""
          val actual = shouldThrow<IllegalArgumentException> { PEELParser(given).parseCondition() }
          actual.message shouldContain "Unknown command *gt{ on position 0"
        }
      }
      describe("variations") {
        describe("gt") {
          it("should parse unmanaged gt") {
            val given = """*gt(#ref(pvd1), #ref(pvd2))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.GREATER_THAN,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed gt") {
            val given =
                """*gt(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    stringIgnoreCase = true,
                    operation = OperationEnum.GREATER_THAN,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*gt(#ref(pvd1), #ref(pvd2), #ref(pvd3))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*gt'. Expected: 2, actual: 3"
          }
          it("should fail on bad number of params 2") {
            val given = """*gt(#ref(pvd1))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Not enough arguments on position 0 for command '*gt'. Expected: 2, actual: 1"
          }
        }
        describe("gte") {
          it("should parse unmanaged gte") {
            val given = """*gte(#ref(pvd1), #ref(pvd2))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.GREATER_THAN_EQUAL,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed gte") {
            val given =
                """*gte(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    stringIgnoreCase = true,
                    operation = OperationEnum.GREATER_THAN_EQUAL,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*gte(#ref(pvd1), #ref(pvd2), #ref(pvd3))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*gte'. Expected: 2, actual: 3"
          }
          it("should fail on bad number of params 2") {
            val given = """*gte(#ref(pvd1))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Not enough arguments on position 0 for command '*gte'. Expected: 2, actual: 1"
          }
        }
        describe("lt") {
          it("should parse unmanaged lt") {
            val given = """*lt(#ref(pvd1), #ref(pvd2))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.LESS_THAN,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed lt") {
            val given =
                """*lt(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    stringIgnoreCase = true,
                    operation = OperationEnum.LESS_THAN,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*lt(#ref(pvd1), #ref(pvd2), #ref(pvd3))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*lt'. Expected: 2, actual: 3"
          }
          it("should fail on bad number of params 2") {
            val given = """*lt(#ref(pvd1))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Not enough arguments on position 0 for command '*lt'. Expected: 2, actual: 1"
          }
        }
        describe("lte") {
          it("should parse unmanaged lte") {
            val given = """*lte(#ref(pvd1), #ref(pvd2))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.LESS_THAN_EQUAL,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed lte") {
            val given =
                """*lte(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    stringIgnoreCase = true,
                    operation = OperationEnum.LESS_THAN_EQUAL,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*lte(#ref(pvd1), #ref(pvd2), #ref(pvd3))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*lte'. Expected: 2, actual: 3"
          }
          it("should fail on bad number of params 2") {
            val given = """*lte(#ref(pvd1))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Not enough arguments on position 0 for command '*lte'. Expected: 2, actual: 1"
          }
        }
        describe("isNull") {
          it("should parse unmanaged isNull") {
            val given = """*isNull(#ref(pvd1))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.IS_NULL,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed isNull") {
            val given =
                """*isNull(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    operation = OperationEnum.IS_NULL,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*isNull(#ref(pvd1), #ref(pvd2))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*isNull'. Expected: 1, actual: 2"
          }
        }
        describe("notNull") {
          it("should parse unmanaged notNull") {
            val given = """*notNull(#ref(pvd1))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.IS_NOT_NULL,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed notNull") {
            val given =
                """*notNull(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    operation = OperationEnum.IS_NOT_NULL,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*notNull(#ref(pvd1), #ref(pvd2))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*notNull'. Expected: 1, actual: 2"
          }
        }
        describe("isEmpty") {
          it("should parse unmanaged isEmpty") {
            val given = """*isEmpty(#ref(pvd1))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.IS_EMPTY,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed isEmpty") {
            val given =
                """*isEmpty(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    operation = OperationEnum.IS_EMPTY,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*isEmpty(#ref(pvd1), #ref(pvd2))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*isEmpty'. Expected: 1, actual: 2"
          }
        }
        describe("notEmpty") {
          it("should parse unmanaged notEmpty") {
            val given = """*notEmpty(#ref(pvd1))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.IS_NOT_EMPTY,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed notEmpty") {
            val given =
                """*notEmpty(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    operation = OperationEnum.IS_NOT_EMPTY,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*notEmpty(#ref(pvd1), #ref(pvd2))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*notEmpty'. Expected: 1, actual: 2"
          }
        }
        describe("isBlank") {
          it("should parse unmanaged isBlank") {
            val given = """*isBlank(#ref(pvd1))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.IS_BLANK,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed isBlank") {
            val given =
                """*isBlank(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    operation = OperationEnum.IS_BLANK,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*isBlank(#ref(pvd1), #ref(pvd2))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*isBlank'. Expected: 1, actual: 2"
          }
        }
        describe("notBlank") {
          it("should parse unmanaged notBlank") {
            val given = """*notBlank(#ref(pvd1))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.IS_NOT_BLANK,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed notBlank") {
            val given =
                """*notBlank(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    operation = OperationEnum.IS_NOT_BLANK,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*notBlank(#ref(pvd1), #ref(pvd2))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*notBlank'. Expected: 1, actual: 2"
          }
        }
        describe("sw") {
          it("should parse unmanaged sw") {
            val given = """*sw(#ref(pvd1), #ref(pvd2))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.STARTS_WITH,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed sw") {
            val given =
                """*sw(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.4,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase,fieldsStrictCheck,arrayOrderStrictCheck))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 4),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    stringIgnoreCase = true,
                    fieldsStrictCheck = true,
                    arrayOrderStrictCheck = true,
                    operation = OperationEnum.STARTS_WITH,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*sw(#ref(pvd1), #ref(pvd2), #ref(pvd3))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*sw'. Expected: 2, actual: 3"
          }
          it("should fail on bad number of params 2") {
            val given = """*sw(#ref(pvd1))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Not enough arguments on position 0 for command '*sw'. Expected: 2, actual: 1"
          }
        }
        describe("ew") {
          it("should parse unmanaged ew") {
            val given = """*ew(#ref(pvd1), #ref(pvd2))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.ENDS_WITH,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed ew") {
            val given =
                """*ew(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase,fieldsStrictCheck,arrayOrderStrictCheck))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    stringIgnoreCase = true,
                    fieldsStrictCheck = true,
                    arrayOrderStrictCheck = true,
                    operation = OperationEnum.ENDS_WITH,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*ew(#ref(pvd1), #ref(pvd2), #ref(pvd3))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*ew'. Expected: 2, actual: 3"
          }
          it("should fail on bad number of params 2") {
            val given = """*ew(#ref(pvd1))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Not enough arguments on position 0 for command '*ew'. Expected: 2, actual: 1"
          }
        }
        describe("contains") {
          it("should parse unmanaged contains") {
            val given = """*contains(#ref(pvd1), #ref(pvd2))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.CONTAINS,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed contains") {
            val given =
                """*contains(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase,fieldsStrictCheck,arrayOrderStrictCheck))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    stringIgnoreCase = true,
                    fieldsStrictCheck = true,
                    arrayOrderStrictCheck = true,
                    operation = OperationEnum.CONTAINS,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*contains(#ref(pvd1), #ref(pvd2), #ref(pvd3))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*contains'. Expected: 2, actual: 3"
          }
          it("should fail on bad number of params 2") {
            val given = """*contains(#ref(pvd1))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Not enough arguments on position 0 for command '*contains'. Expected: 2, actual: 1"
          }
        }
        describe("isIn") {
          it("should parse unmanaged isIn") {
            val given = """*isIn(#ref(pvd1), #ref(pvd2))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.IS_IN,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed isIn") {
            val given =
                """*isIn(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase,fieldsStrictCheck,arrayOrderStrictCheck))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    stringIgnoreCase = true,
                    fieldsStrictCheck = true,
                    arrayOrderStrictCheck = true,
                    operation = OperationEnum.IS_IN,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*isIn(#ref(pvd1), #ref(pvd2), #ref(pvd3))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*isIn'. Expected: 2, actual: 3"
          }
          it("should fail on bad number of params 2") {
            val given = """*isIn(#ref(pvd1))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Not enough arguments on position 0 for command '*isIn'. Expected: 2, actual: 1"
          }
        }
        describe("eq") {
          it("should parse unmanaged eq") {
            val given = """*eq(#ref(pvd1), #ref(pvd2))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.EQUALS,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed eq") {
            val given =
                """*eq(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult,stringIgnoreCase,fieldsStrictCheck,arrayOrderStrictCheck))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    stringIgnoreCase = true,
                    fieldsStrictCheck = true,
                    arrayOrderStrictCheck = true,
                    operation = OperationEnum.EQUALS,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*eq(#ref(pvd1), #ref(pvd2), #ref(pvd3))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*eq'. Expected: 2, actual: 3"
          }
          it("should fail on bad number of params 2") {
            val given = """*eq(#ref(pvd1))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Not enough arguments on position 0 for command '*eq'. Expected: 2, actual: 1"
          }
        }
        describe("pos") {
          it("should parse unmanaged pos") {
            val given = """*pos(#ref(pvd1))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.IS_POSITIVE,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed pos") {
            val given =
                """*pos(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    operation = OperationEnum.IS_POSITIVE,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*pos(#ref(pvd1), #ref(pvd2))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*pos'. Expected: 1, actual: 2"
          }
        }
        describe("neg") {
          it("should parse unmanaged neg") {
            val given = """*neg(#ref(pvd1))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.IS_NEGATIVE,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed neg") {
            val given =
                """*neg(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    operation = OperationEnum.IS_NEGATIVE,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*neg(#ref(pvd1), #ref(pvd2))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*neg'. Expected: 1, actual: 2"
          }
        }
        describe("zero") {
          it("should parse unmanaged zero") {
            val given = """*zero(#ref(pvd1))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.IS_ZERO,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed zero") {
            val given =
                """*zero(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    operation = OperationEnum.IS_ZERO,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*zero(#ref(pvd1), #ref(pvd2))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*zero'. Expected: 1, actual: 2"
          }
        }
        describe("past") {
          it("should parse unmanaged past") {
            val given = """*past(#ref(pvd1))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.IS_PAST,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed past") {
            val given =
                """*past(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    operation = OperationEnum.IS_PAST,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*past(#ref(pvd1), #ref(pvd2))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*past'. Expected: 1, actual: 2"
          }
        }
        describe("future") {
          it("should parse unmanaged future") {
            val given = """*future(#ref(pvd1))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.IS_FUTURE,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed future") {
            val given =
                """*future(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    operation = OperationEnum.IS_FUTURE,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*future(#ref(pvd1), #ref(pvd2))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*future'. Expected: 1, actual: 2"
          }
        }
        describe("regexp") {
          it("should parse unmanaged regexp") {
            val given = """*regexp(#ref(pvd1), #ref(pvd2))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.REGEXP_MATCH,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed regexp") {
            val given =
                """*regexp(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    operation = OperationEnum.REGEXP_MATCH,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*regexp(#ref(pvd1), #ref(pvd2), #ref(pvd3))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*regexp'. Expected: 2, actual: 3"
          }
          it("should fail on bad number of params 2") {
            val given = """*regexp(#ref(pvd1))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Not enough arguments on position 0 for command '*regexp'. Expected: 2, actual: 1"
          }
        }
        describe("hasKey") {
          it("should parse unmanaged hasKey") {
            val given = """*hasKey(#ref(pvd1), #ref(pvd2))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.HAS_KEY,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed hasKey") {
            val given =
                """*hasKey(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    operation = OperationEnum.HAS_KEY,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*hasKey(#ref(pvd1), #ref(pvd2), #ref(pvd3))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*hasKey'. Expected: 2, actual: 3"
          }
          it("should fail on bad number of params 2") {
            val given = """*hasKey(#ref(pvd1))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Not enough arguments on position 0 for command '*hasKey'. Expected: 2, actual: 1"
          }
        }
        describe("unique") {
          it("should parse unmanaged unique") {
            val given = """*unique(#ref(pvd1))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.IS_UNIQUE,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed unique") {
            val given =
                """*unique(#ref(pvd1),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    operation = OperationEnum.IS_UNIQUE,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*unique(#ref(pvd1), #ref(pvd2))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*unique'. Expected: 1, actual: 2"
          }
        }
        describe("schema") {
          it("should parse unmanaged schema") {
            val given = """*schema(#ref(pvd1), #ref(pvd2))"""
            val expected =
                PolicyConditionAtomic(
                    operation = OperationEnum.SCHEMA_MATCH,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ))
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should parse managed schema") {
            val given =
                """*schema(#ref(pvd1), #ref(pvd2),#opts(id=cond,ver=1.2.3,desc="Some desc",labels=foo|bar,negateResult))"""
            val expected =
                PolicyConditionAtomic(
                    id = "cond",
                    version = SemVer(1, 2, 3),
                    description = "Some desc",
                    labels = listOf("foo", "bar"),
                    negateResult = true,
                    operation = OperationEnum.SCHEMA_MATCH,
                    args =
                        listOf(
                            PolicyVariableRef(id = "pvd1"),
                            PolicyVariableRef(id = "pvd2"),
                        ),
                )
            val actual = PEELParser(given).parseCondition()
            actual shouldBe expected
          }
          it("should fail on bad number of params") {
            val given = """*schema(#ref(pvd1), #ref(pvd2), #ref(pvd3))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Too many arguments on position 0 for command '*schema'. Expected: 2, actual: 3"
          }
          it("should fail on bad number of params 2") {
            val given = """*schema(#ref(pvd1))"""
            val actual = shouldThrow<IllegalStateException> { PEELParser(given).parseCondition() }
            actual.message shouldContain
                "Not enough arguments on position 0 for command '*schema'. Expected: 2, actual: 1"
          }
        }
      }
    })
