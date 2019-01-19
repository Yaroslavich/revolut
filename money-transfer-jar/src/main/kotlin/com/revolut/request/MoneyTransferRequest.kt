package com.revolut.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.revolut.response.Response
import com.revolut.model.CurrencyCode
import com.revolut.model.MoneyTransaction
import com.revolut.rest.BufferSerializable
import com.revolut.service.MoneyTransactionService
import java.math.BigDecimal

class MoneyTransferRequest
@JsonCreator constructor(
        @JsonProperty("senderId")
        val senderId: Long,

        @JsonProperty("receiverId")
        val receiverId: Long,

        @JsonProperty("currency")
        val currency: CurrencyCode,

        @JsonProperty("amount")
        val amount: BigDecimal

): Request<MoneyTransaction, MoneyTransactionService>, BufferSerializable() {
    override fun process(service: MoneyTransactionService): Response<MoneyTransaction> {
        return service.transfer(senderId, receiverId, currency, amount)
    }
}