package com.revolut.response

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.revolut.model.Entity
import com.revolut.rest.BufferSerializable

/**
 * Holder for all responses on every operation with data
 *
 * Created by yaroslav
 */

class Response<T: Entity>
@JsonCreator constructor(
        /**
         *   Result state of data affected by some operation
         */
        @JsonProperty("entity")
        val entity: T?,

        /**
         *   Operation result
         *   OK if operation succeeded
         */
        @JsonProperty("error")
        val error: ErrorCode
): BufferSerializable()
