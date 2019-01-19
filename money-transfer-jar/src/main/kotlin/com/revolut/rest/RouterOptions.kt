package com.revolut.rest

import io.vertx.core.Vertx
import io.vertx.ext.web.Router

/**
 * Simplifies building REST Api
 *
 * Created by yaroslav
 */

data class RouterOptions(
        val vertx: Vertx,
        val router: Router,
        val apiVersion: String,
        val path: String
)