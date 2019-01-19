package com.revolut.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.revolut.response.Response
import com.revolut.model.Account
import com.revolut.model.CurrencyCode
import com.revolut.rest.BufferSerializable
import com.revolut.service.AccountService
import java.math.BigDecimal

class MoneyWithdrawRequest
@JsonCreator constructor(
        @JsonProperty("receiverId")
        val receiverId: Long,

        @JsonProperty("currency")
        val currency: CurrencyCode,

        @JsonProperty("amount")
        val amount: BigDecimal

): Request<Account, AccountService>, BufferSerializable() {

    override fun process(service: AccountService): Response<Account> {
        return service.withdraw(receiverId, currency, amount)
    }
}