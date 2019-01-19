package com.revolut.rest

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
 * Created by yaroslav
 */

object CustomerRestApi {
    // Rest Api versioning for future needs
    private const val apiVersion = "v1"

    fun attach(vertx: Vertx, router: Router) {
        val routerOptions = RouterOptions(vertx, router, apiVersion, "")

        get(routerOptions.copy(path = "account"), accountService)
        post(routerOptions.copy(path = "account/create"), accountService)
        post(routerOptions.copy(path = "account/find"), accountService)
        post(routerOptions.copy(path = "account/delete"), accountService)

        post(routerOptions.copy(path = "customer/create"), customerService)
        post(routerOptions.copy(path = "customer/block"), customerService)
        post(routerOptions.copy(path = "transfer"), moneyTransferService)
        post(routerOptions.copy(path = "deposit"), accountService)
        post(routerOptions.copy(path = "withdraw"), accountService)
    }
}