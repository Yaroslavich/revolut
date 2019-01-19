package com.revolut.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.revolut.response.Response
import com.revolut.model.Customer
import com.revolut.rest.BufferSerializable
import com.revolut.service.CustomerService

class BlockCustomerRequest
@JsonCreator constructor(
        @JsonProperty("personalDataHash")
        val customerId: Long
): Request<Customer, CustomerService>, BufferSerializable() {
    override fun process(service: CustomerService): Response<Customer> {
        return service.block(customerId)
    }
}