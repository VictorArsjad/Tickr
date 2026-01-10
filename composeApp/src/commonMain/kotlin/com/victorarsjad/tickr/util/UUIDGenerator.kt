package com.victorarsjad.tickr.util

import com.benasher44.uuid.uuid4

class UUIDGenerator {
    fun generate(): String = uuid4().toString()
}
