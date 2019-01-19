package com.revolut.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import io.vertx.core.buffer.Buffer

/**
 * serialization utils
 */

val mapper = ObjectMapper()
        .registerModule(SimpleModule())
        .enable(DeserializationFeature.USE_LONG_FOR_INTS)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)!!

inline fun <reified C> deserialize(buffer: Buffer): C {
    return mapper.readValue(buffer.bytes, C::class.java)
}