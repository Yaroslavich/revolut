package com.revolut.utils

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

/**
 * config utils
 */
fun JsonObject.getBooleanOrFalse(name: String): Boolean = getBoolean(name) ?: false

fun JsonObject.getStringOrEmpty(name: String): String = getString(name) ?: ""

fun JsonObject.getJsonObjectOrEmpty(name: String): JsonObject = getJsonObject(name) ?: JsonObject()

inline fun JsonArray.onEachJsonObject(body: (JsonObject) -> Unit) {
    for (pos in 0 until size()) {
        body(getJsonObject(pos))
    }
}