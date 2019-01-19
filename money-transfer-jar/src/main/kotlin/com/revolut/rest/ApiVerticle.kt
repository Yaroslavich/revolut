package com.revolut.rest

import com.revolut.trx.PeriodicJob
import com.revolut.utils.loggerFor
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle

/**
 * vert.x implementation of REST-api
 *
 * Created by yaroslav
 */

class ApiVerticle: CoroutineVerticle() {
    private val log = loggerFor(javaClass)

    private var port = 8888
    private var address = "0.0.0.0"

    private var trxProcessingPeriod = 100L

    override suspend fun start() {
        log.info("starting api ...")

        init(config)

        val router = Router.router(vertx)

        RestTools.enableCors(router)
        router.route().handler(BodyHandler.create())

        CustomerRestApi.attach(vertx, router)

        val httpOptions = HttpServerOptions()
                .setIdleTimeout(360) // 6 minutes
        vertx.createHttpServer(httpOptions)
                .requestHandler { router.accept(it) }
                .listen(port, address) { ar ->
                    log.info("Rest api started on port " + ar.result().actualPort())
                }

        if (trxProcessingPeriod > 0) {
            vertx.setPeriodic(trxProcessingPeriod) {
                PeriodicJob.process()
            }
        }
    }

    override suspend fun stop() {
    }

    private fun init(config: JsonObject) {
        address = config.getString("address", address)
        port = config.getInteger("port", port)!!
        trxProcessingPeriod = config.getLong("trxProcessingPeriod", trxProcessingPeriod)
    }
}