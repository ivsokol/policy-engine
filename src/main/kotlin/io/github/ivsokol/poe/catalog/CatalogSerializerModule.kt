package io.github.ivsokol.poe.catalog

import io.github.ivsokol.poe.action.actionSerializersModule
import io.github.ivsokol.poe.condition.conditionSerializersModule
import io.github.ivsokol.poe.policy.policySerializersModule
import io.github.ivsokol.poe.variable.variableSerializersModule
import kotlinx.serialization.modules.plus

val catalogSerializersModule =
    variableSerializersModule +
        conditionSerializersModule +
        actionSerializersModule +
        policySerializersModule
