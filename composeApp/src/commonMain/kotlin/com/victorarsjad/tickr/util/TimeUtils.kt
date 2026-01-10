package com.victorarsjad.tickr.util

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class TimeUtils {
    companion object {
        @OptIn(ExperimentalTime::class)
        fun getTimeMillis(): Long {
            return Clock.System.now().toEpochMilliseconds()
        }
    }
}