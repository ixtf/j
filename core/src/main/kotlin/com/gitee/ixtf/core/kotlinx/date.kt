package com.gitee.ixtf.core.kotlinx

import cn.hutool.core.date.DateUtil
import com.gitee.ixtf.core.J
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAccessor
import java.util.*

fun Date.betweenMs(endDate: Date): Long = DateUtil.betweenMs(this, endDate)

fun Date.betweenDay(endDate: Date, isReset: Boolean = false): Long =
    DateUtil.betweenDay(this, endDate, isReset)

fun Date.ldt(): LocalDateTime = J.ldt(this)

fun Date.ld(): LocalDate = J.ld(this)

fun Date.lt(): LocalTime = J.lt(this)

fun TemporalAccessor.date() = J.date(this)
