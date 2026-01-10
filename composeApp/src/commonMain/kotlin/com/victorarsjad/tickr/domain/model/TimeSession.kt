package com.victorarsjad.tickr.domain.model

import com.victorarsjad.tickr.util.TimeUtils

data class TimeSession(
    val id: String,
    val tickrId: String,
    val startedAt: Long,
    val endedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
) {
    val duration: Long
        get() = (endedAt ?: TimeUtils.getTimeMillis()) - startedAt
}
