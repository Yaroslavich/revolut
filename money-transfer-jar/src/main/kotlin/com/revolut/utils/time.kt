package com.revolut.utils

import java.time.Instant

/**
 * time utils
 */

fun getNow(): Long {
    return Instant.now().toEpochMilli()
}