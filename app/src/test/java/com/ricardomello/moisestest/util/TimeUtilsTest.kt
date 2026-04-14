package com.ricardomello.moisestest.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilsTest {

    @Test
    fun `toTimeString formats seconds into m ss`() {
        assertEquals("0:00", 0.toTimeString())
        assertEquals("0:09", 9.toTimeString())
        assertEquals("1:00", 60.toTimeString())
        assertEquals("2:05", 125.toTimeString())
    }

    @Test
    fun `remainingTimeString formats remaining duration`() {
        assertEquals("-0:30", remainingTimeString(progress = 30, duration = 60))
        assertEquals("-1:05", remainingTimeString(progress = 55, duration = 120))
    }
}

