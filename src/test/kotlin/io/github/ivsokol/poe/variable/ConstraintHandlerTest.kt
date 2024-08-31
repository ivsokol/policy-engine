package io.github.ivsokol.poe.variable

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.PolicyEntityEnum
import io.github.ivsokol.poe.catalog.EmptyPolicyCatalog
import io.github.ivsokol.poe.catalog.PolicyCatalog
import io.github.ivsokol.poe.condition.OperationEnum
import io.github.ivsokol.poe.condition.PolicyConditionAtomic
import io.github.ivsokol.poe.condition.PolicyConditionDefault
import io.github.ivsokol.poe.condition.PolicyConditionRef
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

class ConstraintHandlerTest :
    FunSpec({
      val logger = LoggerFactory.getLogger("test")
      val marker = MarkerFactory.getMarker("marker")

      test("check null should return null") {
        checkConstraint(
            PolicyConditionRef("11"),
            Context(),
            EmptyPolicyCatalog(),
            "pol1",
            PolicyEntityEnum.POLICY,
            logger,
            marker) shouldBe null
      }

      test("check true should return true") {
        checkConstraint(
            PolicyConditionDefault(true),
            Context(),
            EmptyPolicyCatalog(),
            "pol1",
            PolicyEntityEnum.POLICY,
            logger,
            marker) shouldBe true
      }

      test("check from catalog should return true") {
        checkConstraint(
            PolicyConditionRef("cond1"),
            Context(),
            PolicyCatalog(
                id = "test-catalog",
                policyConditions =
                    listOf(
                        PolicyConditionAtomic(
                            id = "cond1",
                            operation = OperationEnum.EQUALS,
                            args =
                                listOf(
                                    PolicyVariableStatic(value = "1"),
                                    PolicyVariableStatic(value = "1"))))),
            "pol1",
            PolicyEntityEnum.POLICY,
            logger,
            marker) shouldBe true
      }
    })
