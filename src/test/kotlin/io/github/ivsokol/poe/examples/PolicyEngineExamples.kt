package io.github.ivsokol.poe.examples

import io.github.ivsokol.poe.Context
import io.github.ivsokol.poe.PolicyEngine
import io.github.ivsokol.poe.el.PEELParser
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PolicyEngineExamples :
    FunSpec({
      test("PEEL custom condition") {
        val policyConditionStr =
            """
            *past(
                #dTime(15.05.2023 14:30:00.123 +01:00,
                    #opts(dateTimeFormat="dd.MM.yyyy HH:mm:ss.SSS XXX")
                )
            )
        """
                .trimIndent()
        val context = Context()
        val result =
            PolicyEngine().checkCondition(PEELParser(policyConditionStr).parseCondition(), context)
        result shouldBe true
        context.event.list().size shouldBe 4
      }
    })
