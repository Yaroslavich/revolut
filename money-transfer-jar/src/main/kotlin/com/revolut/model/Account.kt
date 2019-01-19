package com.revolut.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * Account
 *
 * Created by yaroslav
 */

class Account
@JsonCreator constructor(
        /**
         *   Unique id of account
         */
        @JsonProperty("id")
        override val id: Long,

        /**
         *   time of creation in unix epoch
         */
        @JsonProperty("timestamp")
        override val timestamp: Long,

        /**
         *   Currency of this account
         */
        @JsonProperty("currency")
        val currency: CurrencyCode,

        /**
         *   Amount of money on account
         */
        @JsonProperty("amount")
        var amount: BigDecimal,

        /**
         *   Owner of this account
         */
        @JsonProperty("customerId")
        val customerId: Long
): Entity