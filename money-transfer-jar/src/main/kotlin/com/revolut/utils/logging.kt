package com.revolut.utils

import org.slf4j.LoggerFactory

/**
 * logging utils
 */

fun <T> loggerFor(clazz: Class<T>) = LoggerFactory.getLogger(clazz)!!