package com.github.ixtf.core.kotlinx

import cn.hutool.core.date.DateUtil
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAccessor
import java.util.*

fun Date.betweenMs(endDate: Date): Long = DateUtil.betweenMs(this, endDate)

fun Date.betweenDay(endDate: Date, isReset: Boolean = false): Long =
    DateUtil.betweenDay(this, endDate, isReset)

fun Date.ldt(): LocalDateTime = DateUtil.toLocalDateTime(this)

fun Date.ld(): LocalDate = DateUtil.toLocalDateTime(this).toLocalDate()

fun Date.lt(): LocalTime = DateUtil.toLocalDateTime(this).toLocalTime()

fun TemporalAccessor.date(): Date = DateUtil.date(this)
