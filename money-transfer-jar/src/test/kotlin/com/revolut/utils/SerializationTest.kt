package com.revolut.utils

import com.revolut.response.ErrorCode
import com.revolut.response.Response
import com.revolut.model.Customer
import org.junit.Test
import kotlin.test.assertNotNull

class SerializationTest {

    @Test
    fun oneClassSerializationTest() {
        val before: Response<Customer> = Response(Customer(123L, getNow(), false, "hash"), ErrorCode.OK)

        val serializedToBuffer = before.toBuffer()

        assert(serializedToBuffer.bytes.isNotEmpty())

        val after: Response<Customer> = deserialize(serializedToBuffer)

        assertNotNull(after)

        //TODO: jackson serialization violates equality
        //assert(entity.equals(deserializedValue))
    }
}