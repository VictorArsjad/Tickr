package com.victorarsjad.tickr.domain.model

data class CountValue(
    val id: String,
    val tickrId: String,
    val value: Int,
    val periodStartAt: Long,
    val periodEndAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
)
