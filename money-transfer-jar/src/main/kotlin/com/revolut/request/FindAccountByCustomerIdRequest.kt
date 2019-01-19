package com.revolut.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.revolut.response.Response
import com.revolut.model.Account
import com.revolut.model.CurrencyCode
import com.revolut.rest.BufferSerializable
import com.revolut.service.AccountService

class FindAccountByCustomerIdRequest
@JsonCreator constructor(
        @JsonProperty("customerId")
        val customerId: Long,

        @JsonProperty("currency")
        val currency: CurrencyCode

): Request<Account, AccountService>, BufferSerializable() {

    override fun process(service: AccountService): Response<Account> {
        return service.findByCustomerId(customerId, currency)
    }
}