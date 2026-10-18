package com.daymark.app.domain

import com.daymark.app.data.ActivityEntity
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeParseException

sealed interface ParseResult {
    data class Ok(val startMillis: Long, val endMillis: Long) : ParseResult
    data class Error(val message: String) : ParseResult
}

object ActivityRules {
    /** Parses "HH:mm" start/end on [date]. Milestone 1 does not support ranges crossing midnight. */
    fun parseRange(date: LocalDate, zone: ZoneId, startText: String, endText: String): ParseResult {
        val start = parseTime(startText) ?: return ParseResult.Error("Start time must look like 09:30")
        val end = parseTime(endText) ?: return ParseResult.Error("End time must look like 10:45")
        return try {
            val s = date.atTime(start).atZone(zone).toInstant().toEpochMilli()
            val e = date.atTime(end).atZone(zone).toInstant().toEpochMilli()
            if (e <= s) ParseResult.Error("End must be after start (ranges across midnight aren't supported yet)")
            else ParseResult.Ok(s, e)
        } catch (ex: DateTimeException) {
            ParseResult.Error("That time doesn't exist on this date")
        }
    }

    /** Returns existing activities that overlap [start, end). Never modifies anything. */
    fun overlaps(start: Long, end: Long, existing: List<ActivityEntity>): List<ActivityEntity> =
        existing.filter { it.startMillis < end && start < it.endMillis }

    fun totalMinutes(items: List<ActivityEntity>): Long = items.sumOf { it.durationMillis } / 60_000

    fun formatDuration(minutes: Long): String {
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0L -> "${m}m"
            m == 0L -> "${h}h"
            else -> "${h}h ${m}m"
        }
    }

    private fun parseTime(text: String): LocalTime? = try {
        LocalTime.parse(text.trim().let { if (it.length == 4 && it[1] == ':') "0$it" else it })
    } catch (e: DateTimeParseException) {
        null
    }
}
