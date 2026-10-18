package com.daymark.app

import com.daymark.app.data.ActivityEntity
import com.daymark.app.domain.ActivityRules
import com.daymark.app.domain.ParseResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ActivityRulesTest {
    private val zone = ZoneId.of("Asia/Kolkata")
    private val day = LocalDate.of(2026, 10, 18)

    private fun range(s: String, e: String) = ActivityRules.parseRange(day, zone, s, e)

    @Test fun validRangeParses() {
        val r = range("09:00", "10:30") as ParseResult.Ok
        assertEquals(90L * 60_000, r.endMillis - r.startMillis)
    }

    @Test fun singleDigitHourAccepted() {
        assertTrue(range("9:00", "10:00") is ParseResult.Ok)
    }

    @Test fun endBeforeStartRejected() {
        assertTrue(range("10:00", "09:00") is ParseResult.Error)
        assertTrue(range("10:00", "10:00") is ParseResult.Error)
    }

    @Test fun garbageRejected() {
        assertTrue(range("abc", "10:00") is ParseResult.Error)
    }

    @Test fun overlapDetectionIsHalfOpen() {
        val a = ActivityEntity(description = "A", startMillis = 0, endMillis = 100, zoneId = "UTC")
        assertTrue(ActivityRules.overlaps(100, 200, listOf(a)).isEmpty()) // touching is not overlap
        assertEquals(1, ActivityRules.overlaps(99, 200, listOf(a)).size)
    }

    @Test fun durationFormatting() {
        assertEquals("45m", ActivityRules.formatDuration(45))
        assertEquals("2h", ActivityRules.formatDuration(120))
        assertEquals("1h 5m", ActivityRules.formatDuration(65))
    }
}
