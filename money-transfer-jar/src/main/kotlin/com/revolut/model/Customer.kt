package com.revolut.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Customer
 *
 * Created by yaroslav
 */

class Customer
@JsonCreator constructor(
        /**
         *   Unique id of customer
         */
        @JsonProperty("id")
        override val id: Long,

        /**
         *   time of creation in unix epoch
         */
        @JsonProperty("timestamp")
        override val timestamp: Long,

        /**
         *   true if customer is temporarily blocked
         */
        @JsonProperty("isBlocked")
        var isBlocked: Boolean = false,

        /**
         *   hash of all personal data for customer
         *   due to the law we do not store any personal data such as name, phone, email directly
         */
        @JsonProperty("dataHash")
        val dataHash: String
): Entity