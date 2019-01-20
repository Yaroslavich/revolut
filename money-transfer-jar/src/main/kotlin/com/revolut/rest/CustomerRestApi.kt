package com.revolut.rest

import com.revolut.request.*
import com.revolut.rest.RestTools.post
import com.revolut.rest.RestTools.get
import com.revolut.service.ServiceFactory.accountService
import com.revolut.service.ServiceFactory.customerService
import com.revolut.service.ServiceFactory.moneyTransferService
import io.vertx.core.Vertx
import io.vertx.ext.web.Router

/**
 * REST Api
 *
 * To add new rest api route you need to:
 * 1. Choose path of the route
 * 2. Choose service handling the request or implement a new one of Service<E: Entity> interface
 * 3. Describe the request as implementation of Request<Entity, Service>
 *
 * Created by yaroslav
 */

object CustomerRestApi {
    // Rest Api versioning for future needs
    private const val apiVersion = "v1"

    fun attach(vertx: Vertx, router: Router) {
        val routerOptions = RouterOptions(vertx, router, apiVersion, "")

        get(routerOptions.copy(path = "account"), accountService)
        post(routerOptions.copy(path = "account/create"), accountService, CreateAccountRequest::class.java)
        post(routerOptions.copy(path = "account/find"), accountService, FindAccountByCustomerIdRequest::class.java)
        post(routerOptions.copy(path = "account/delete"), accountService, DeleteAccountRequest::class.java)

        post(routerOptions.copy(path = "customer/create"), customerService, CreateCustomerRequest::class.java)
        post(routerOptions.copy(path = "customer/block"), customerService, BlockCustomerRequest::class.java)
        post(routerOptions.copy(path = "transfer"), moneyTransferService, MoneyTransferRequest::class.java)
        post(routerOptions.copy(path = "deposit"), accountService, MoneyDepositRequest::class.java)
        post(routerOptions.copy(path = "withdraw"), accountService, MoneyWithdrawRequest::class.java)
    }
}