package com.revolut.rest

import com.revolut.model.Entity
import com.revolut.request.Request
import com.revolut.service.Service
import com.revolut.utils.deserialize
import com.revolut.utils.loggerFor
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.launch

/**
 * Some useful tools for building REST Api
 *
 * Created by yaroslav
 */

object RestTools {
    private val log = loggerFor(javaClass)

    val emptyJson = "{\"error\":404,\"desc\":\"\"}".toByteArray()

    private const val originAllowed = "*"
    private const val methodsAllowed = "GET, PUT, OPTIONS"
    private const val headersAllowed = "content-type,x-locale,x-token"
    private const val contentType = "application/json;charset=utf-8"
    private const val allowCredentials = "true"
    private const val idParamDefaultValue:Long = 0L

    fun buildOkResponse(response: HttpServerResponse, body: Buffer) {
        putCommonHeaders(response).end(body)
    }

    private fun putCommonHeaders(response: HttpServerResponse): HttpServerResponse {
        return response
                .putHeader(HttpHeaders.CONTENT_TYPE, contentType)
                .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, originAllowed)
                .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, methodsAllowed)
                .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, headersAllowed)
                .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, allowCredentials)
    }

    private fun buildErrorResponse(response: HttpServerResponse, statusCode: Int) {
        putCommonHeaders(response).setStatusCode(statusCode).end(Buffer.buffer(emptyJson))
    }

    fun enableCors(router: Router) {
        router.route().handler(CorsHandler
                .create("*")
                .allowedHeader("x-requested-with")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("origin")
                .allowedHeader("Content-Type")
                .allowedHeader("accept")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.OPTIONS)
        )
    }

    fun getSafeId(ctx: RoutingContext, idParamName:String): Long {
        val paramValue = ctx.queryParams().get(idParamName)
        if (paramValue.isNullOrEmpty()) {
            return idParamDefaultValue
        }
        return paramValue.toLongOrNull()?: idParamDefaultValue
    }

    inline fun <reified E: Entity, reified S: Service<E>, reified R: Request<E, S>> get(options: RouterOptions, service: S) {
        val defaultDispatcher = options.vertx.dispatcher()

        options.router.get("/${options.apiVersion}/${options.path}").handler { ctx ->
            processRequest(defaultDispatcher, ctx) {
                val id = getSafeId(ctx, "id")
                val response = service.getEntity(id)
                buildOkResponse(ctx.response(), response.toBuffer())
            }
        }
    }

    inline fun <reified E: Entity, reified S: Service<E>, reified R: Request<E, S>> post(options: RouterOptions, service: S) {
        val defaultDispatcher = options.vertx.dispatcher()

        options.router.post("/${options.apiVersion}/${options.path}").handler { ctx ->
            processRequest(defaultDispatcher, ctx) {
                val request: R = deserialize(ctx.body)
                val response = request.process(service)
                buildOkResponse(ctx.response(), response.toBuffer())
            }
        }
    }

    fun processRequest(dispatcher: CoroutineDispatcher, ctx: RoutingContext, requestHandler: () -> Unit) {
        launch(dispatcher) {
            try {
                requestHandler()
            } catch(e: Exception) {
                log.error("Request ${ctx.currentRoute().path}  body = ${ctx.bodyAsString}", e)
                buildErrorResponse(ctx.response(), 404)
            }
        }
    }
}