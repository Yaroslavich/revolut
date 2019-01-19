package com.revolut.request

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.revolut.response.Response
import com.revolut.model.Entity
import com.revolut.service.Service

/**
 * Request from Customer on some operation with Entity
 *
 * Created by yaroslav
 */

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = CreateCustomerRequest::class, name = "createCustomer"),
        JsonSubTypes.Type(value = BlockCustomerRequest::class, name = "blockCustomer"),
        JsonSubTypes.Type(value = MoneyTransferRequest::class, name = "transfer"),
        JsonSubTypes.Type(value = MoneyDepositRequest::class, name = "deposit"),
        JsonSubTypes.Type(value = MoneyWithdrawRequest::class, name = "withdraw"),
        JsonSubTypes.Type(value = CreateAccountRequest::class, name = "createAccount"),
        JsonSubTypes.Type(value = DeleteAccountRequest::class, name = "deleteAccount"),
        JsonSubTypes.Type(value = FindAccountByCustomerIdRequest::class, name = "findAccount")
)
interface Request<E: Entity, S: Service<E>> {
    fun process(service: S): Response<E>
}