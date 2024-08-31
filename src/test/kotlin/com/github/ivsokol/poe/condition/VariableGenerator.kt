package com.github.ivsokol.poe.condition

import com.github.ivsokol.poe.variable.PolicyVariableRef
import com.github.ivsokol.poe.variable.PolicyVariableStatic
import com.github.ivsokol.poe.variable.VariableValueFormatEnum
import com.github.ivsokol.poe.variable.VariableValueTypeEnum
import java.math.BigDecimal
import java.time.*

internal fun ref(id: String) = PolicyVariableRef(id = id)

internal fun int(a: Int) = PolicyVariableStatic(value = a, type = VariableValueTypeEnum.INT)

internal fun long(a: Long) =
    PolicyVariableStatic(
        value = a, type = VariableValueTypeEnum.INT, format = VariableValueFormatEnum.LONG)

internal fun double(a: Double) =
    PolicyVariableStatic(
        value = a, type = VariableValueTypeEnum.NUMBER, format = VariableValueFormatEnum.DOUBLE)

internal fun float(a: Float) =
    PolicyVariableStatic(
        value = a, type = VariableValueTypeEnum.NUMBER, format = VariableValueFormatEnum.FLOAT)

internal fun bigDecimal(a: BigDecimal) =
    PolicyVariableStatic(
        value = a.toString(),
        type = VariableValueTypeEnum.NUMBER,
        format = VariableValueFormatEnum.BIG_DECIMAL)

internal fun bool(a: Boolean) =
    PolicyVariableStatic(value = a, type = VariableValueTypeEnum.BOOLEAN)

internal fun string(a: String) =
    PolicyVariableStatic(value = a, type = VariableValueTypeEnum.STRING)

internal fun date(a: LocalDate) =
    PolicyVariableStatic(
        value = a.toString(),
        type = VariableValueTypeEnum.STRING,
        format = VariableValueFormatEnum.DATE)

internal fun dateTime(a: OffsetDateTime) =
    PolicyVariableStatic(
        value = a.toString(),
        type = VariableValueTypeEnum.STRING,
        format = VariableValueFormatEnum.DATE_TIME)

internal fun time(a: LocalTime) =
    PolicyVariableStatic(
        value = a.toString(),
        type = VariableValueTypeEnum.STRING,
        format = VariableValueFormatEnum.TIME)

internal fun period(a: Period) =
    PolicyVariableStatic(
        value = a.toString(),
        type = VariableValueTypeEnum.STRING,
        format = VariableValueFormatEnum.PERIOD)

internal fun duration(a: Duration) =
    PolicyVariableStatic(
        value = a.toString(),
        type = VariableValueTypeEnum.STRING,
        format = VariableValueFormatEnum.DURATION)
