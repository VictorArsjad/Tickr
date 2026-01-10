package com.victorarsjad.tickr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration

class ComposeAppCommonTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun assertTimerFormat() {
        assertEquals("00.01,00", formatTime(Duration.parse("1s")))
        assertEquals("00.20,00", formatTime(Duration.parse("20s")))
    }

    @Test
    fun assertTimerFormat2() {
        assertEquals("01.15,00", formatTime(Duration.parse("1m15s")))
    }

    @Test
    fun assertTimerFormat3() {
        assertEquals("181.15,00", formatTime(Duration.parse("3h1m15s")))
    }
}