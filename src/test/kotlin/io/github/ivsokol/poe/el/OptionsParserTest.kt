package io.github.ivsokol.poe.el

import io.github.ivsokol.poe.SemVerSerializer
import io.github.ivsokol.poe.policy.ActionExecutionModeEnum
import io.github.ivsokol.poe.policy.ActionExecutionStrategyEnum
import io.github.ivsokol.poe.variable.ContextStoreEnum
import io.github.ivsokol.poe.variable.VariableValueFormatEnum
import io.github.ivsokol.poe.variable.VariableValueTypeEnum
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

class OptionsParserTest :
    FunSpec({
      val json = Json { serializersModule = SerializersModule { contextual(SemVerSerializer) } }

      test("parseOptions should parse valid options") {
        val input =
            """#opts(id=test-id, 
            ver=1.0.0, 
            desc="Test description", 
            labels=label1|label2, 
            executionMode=onDeny|onPermit, 
            priority=1, 
            isJson=true,
            dateFormat=yyyy-MM-dd,
            timeFormat=HH:mm:ss,
            dateTimeFormat=yyyy-MM-dd HH:mm:ss,
            type=string,
            format=date-time,
            key=testKey,
            source=request,
            negateResult=true,
            stringIgnoreCase=true,
            fieldsStrictCheck=true,
            arrayOrderStrictCheck=true,
            strictCheck=true,
            minimumConditions=2,
            optimize=true,
            lenientConstraints=true,
            actionExecutionStrategy=runAll,
            ignoreErrors=true,
            strictTargetEffect=true,
            skipCache=true,
            runChildActions=true,
            runAction=true,
            indeterminateOnActionFail=true,
            strictUnlessLogic=true,
            failOnMissingKey=true,
            failOnExistingKey=true,
            failOnNullSource=true,
            castNullSourceToArray=true,
            failOnNullMerge=true,
            )"""
                .trimMargin()
        val (options, endPosition) = parseOptions(0, input)

        options.id shouldBe "test-id"
        options.version shouldBe json.decodeFromString(SemVerSerializer, "\"1.0.0\"")
        options.description shouldBe "Test description"
        options.labels shouldBe listOf("label1", "label2")
        options.executionMode shouldBe
            setOf(ActionExecutionModeEnum.ON_DENY, ActionExecutionModeEnum.ON_PERMIT)
        options.priority shouldBe 1
        options.isJson shouldBe true
        options.dateFormat shouldBe "yyyy-MM-dd"
        options.timeFormat shouldBe "HH:mm:ss"
        options.dateTimeFormat shouldBe "yyyy-MM-dd HH:mm:ss"
        options.type shouldBe VariableValueTypeEnum.STRING
        options.format shouldBe VariableValueFormatEnum.DATE_TIME
        options.key shouldBe "testKey"
        options.source shouldBe ContextStoreEnum.REQUEST
        options.negateResult shouldBe true
        options.stringIgnoreCase shouldBe true
        options.fieldsStrictCheck shouldBe true
        options.arrayOrderStrictCheck shouldBe true
        options.strictCheck shouldBe true
        options.minimumConditions shouldBe 2
        options.optimizeNOfRun shouldBe true
        options.lenientConstraints shouldBe true
        options.actionExecutionStrategy shouldBe ActionExecutionStrategyEnum.RUN_ALL
        options.ignoreErrors shouldBe true
        options.strictTargetEffect shouldBe true
        options.skipCache shouldBe true
        options.runChildActions shouldBe true
        options.runAction shouldBe true
        options.indeterminateOnActionFail shouldBe true
        options.strictUnlessLogic shouldBe true
        options.failOnMissingKey shouldBe true
        options.failOnExistingKey shouldBe true
        options.failOnNullSource shouldBe true
        options.castNullSourceToArray shouldBe true
        options.failOnNullMerge shouldBe true

        endPosition shouldBe input.length
      }

      test("should parse default boolean") {
        val input = "#opts(id=test, isJson, negateResult)"
        val (options, endPosition) = parseOptions(0, input)
        options.id shouldBe "test"
        options.isJson shouldBe true
        options.negateResult shouldBe true
        options.failOnNullMerge shouldBe null
        endPosition shouldBe input.length
      }

      test("should parse extra null params") {
        val input = "#opts(id=test, isJson,,negateResult, ,)"
        val (options, endPosition) = parseOptions(0, input)
        options.id shouldBe "test"
        options.isJson shouldBe true
        options.negateResult shouldBe true
        options.failOnNullMerge shouldBe null
        endPosition shouldBe input.length
      }
      test("should parse unknown params") {
        val input = "#opts(id=test, isJson,,negateResult, foo2=bar)"
        val (options, endPosition) = parseOptions(0, input)
        options.id shouldBe "test"
        options.isJson shouldBe true
        options.negateResult shouldBe true
        options.failOnNullMerge shouldBe null
        endPosition shouldBe input.length
      }

      test("should override params") {
        val input = "#opts(id=test, isJson,negateResult, negateResult=false)"
        val (options, endPosition) = parseOptions(0, input)
        options.id shouldBe "test"
        options.isJson shouldBe true
        options.negateResult shouldBe false
        options.failOnNullMerge shouldBe null
        endPosition shouldBe input.length
      }

      test("should fail on bad command") {
        val input = "#abc(id=test)"
        shouldThrow<IllegalArgumentException> { parseOptions(0, input) }.message shouldBe
            "Unknown command #abc on position 0"
      }
      test("should fail on wrong command") {
        val input = "*gt(id=test)"
        shouldThrow<IllegalArgumentException> { parseOptions(0, input) }.message shouldBe
            "Bad command provided for Options parser: '*gt'"
      }
      test("should fail on bad option entry key") {
        val input = "#opts(id=test, =,)"
        shouldThrow<IllegalStateException> { parseOptions(0, input) }.message shouldBe
            "Option key is empty for entry '='"
      }
      test("should fail on bad version format") {
        val input = "#opts(ver=abc)"
        shouldThrow<IllegalArgumentException> { parseOptions(0, input) }.message shouldBe
            "Bad input for SemVer 'abc'"
      }
      test("should fail on no value for non boolean entry") {
        val input = "#opts(ver=abc,id)"
        shouldThrow<IllegalArgumentException> { parseOptions(0, input) }.message shouldBe
            "Option value is empty for entry 'id'"
      }
      test("should not fail on empty labels") {
        val input = "#opts(id=1,labels=|)"
        val (options, endPosition) = parseOptions(0, input)
        options.id shouldBe "1"
        options.labels shouldBe null
        endPosition shouldBe input.length
      }
      test("should fail on bad execution mode enum") {
        val input = "#opts(executionMode=foo)"
        shouldThrow<IllegalArgumentException> { parseOptions(0, input) }.message shouldContain
            "ActionExecutionModeEnum does not contain element with name 'foo'"
      }
      test("should fail on bad action execution strategy enum") {
        val input = "#opts(actionExecutionStrategy=foo)"
        shouldThrow<IllegalArgumentException> { parseOptions(0, input) }.message shouldContain
            "ActionExecutionStrategyEnum does not contain element with name 'foo'"
      }
      test("should fail on bad variable value type enum") {
        val input = "#opts(type=foo)"
        shouldThrow<IllegalArgumentException> { parseOptions(0, input) }.message shouldContain
            "VariableValueTypeEnum does not contain element with name 'foo'"
      }
      test("should fail on bad format enum") {
        val input = "#opts(format=foo)"
        shouldThrow<IllegalArgumentException> { parseOptions(0, input) }.message shouldContain
            "VariableValueFormatEnum does not contain element with name 'foo'"
      }
      test("should fail on bad source enum") {
        val input = "#opts(source=foo)"
        shouldThrow<IllegalArgumentException> { parseOptions(0, input) }.message shouldContain
            "ContextStoreEnum does not contain element with name 'foo'"
      }
    })
