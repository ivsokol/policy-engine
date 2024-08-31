package com.github.ivsokol.poe.catalog

import com.github.ivsokol.poe.action.actionSerializersModule
import com.github.ivsokol.poe.condition.conditionSerializersModule
import com.github.ivsokol.poe.policy.policySerializersModule
import com.github.ivsokol.poe.variable.variableSerializersModule
import kotlinx.serialization.modules.plus

val catalogSerializersModule =
    variableSerializersModule +
        conditionSerializersModule +
        actionSerializersModule +
        policySerializersModule
