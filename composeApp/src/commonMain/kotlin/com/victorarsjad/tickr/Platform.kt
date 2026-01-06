package com.victorarsjad.tickr

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform