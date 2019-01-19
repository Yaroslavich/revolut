package com.revolut.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * MoneyTransaction
 * Temporarily blocked money to transfer to another account of same currency
 *
 * Created by yaroslav
 */

class MoneyTransaction
@JsonCreator constructor(
        /**
         *   Unique id of transaction
         */
        @JsonProperty("id")
        override val id: Long,

        /**
         *   time of creation in unix epoch
         */
        @JsonProperty("timestamp")
        override val timestamp: Long,

        /**
         *   current status of transaction
         *   subject to change while transaction is being processed
         */
        @JsonProperty("status")
        var status: TransactionStatus,

        /**
         *   customer who is sending money
         */
        @JsonProperty("senderId")
        val senderId: Long,

        /**
         *   customer who is receiving money
         */
        @JsonProperty("receiverId")
        val receiverId: Long,

        /**
         *   currency of transaction
         *   currency conversion not supported for now
         */
        @JsonProperty("currency")
        val currency: CurrencyCode,

        /**
         *   amount of money blocked from sender
         *   if transaction fails this amount should be returned to sender
         */
        @JsonProperty("amount")
        val amount: BigDecimal
): Entity