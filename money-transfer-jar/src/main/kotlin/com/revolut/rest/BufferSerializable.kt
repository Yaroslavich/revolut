package com.revolut.rest

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import io.vertx.core.buffer.Buffer

/**
 * Serialisation is not dealing with strings but uses bute buffers directly
 * For sake of optimisation
 *
 * Created by yaroslav
 */

open class BufferSerializable {
    fun toBuffer(): Buffer {
        return Buffer.buffer(mapper.writeValueAsBytes(this) ?: RestTools.errorJsonAsBytes)
    }

    companion object {
        val mapper = ObjectMapper()
                .registerModule(SimpleModule())!!
                .enable(DeserializationFeature.USE_LONG_FOR_INTS)!!
    }
}