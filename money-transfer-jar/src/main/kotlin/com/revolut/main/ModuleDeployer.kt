package com.revolut.main

import com.revolut.utils.*
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory

/**
 * Deployer of Verticles
 * Vertx related thing
 *
 * Created by yaroslav
 */
class ModuleDeployer {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val configLoader = ConfigLoader()

    fun start() {
        logger.info("starting main ...")

        val config = configLoader.loadBlocking("config.json")
        logger.info("config: " + config.encode())

        val vertx = Vertx.vertx(VertxOptions(config.getJsonObjectOrEmpty("options")))

        config.getJsonArray("verticles", JsonArray()).onEachJsonObject {
            deployVerticle(vertx, it)
        }
    }

    private fun deployVerticle(vertx: Vertx, deployConfig: JsonObject) {
        if (deployConfig.getBooleanOrFalse("deploy")) {
            val verticleName = deployConfig.getStringOrEmpty("verticleName")

            launch(vertx.dispatcher()) {
                try {
                    logger.info("deploying {}", verticleName)

                    val verticleConfig = configLoader.load(vertx, deployConfig.getStringOrEmpty("configFile"))
                    val deploymentOptions = DeploymentOptions(deployConfig.getJsonObjectOrEmpty("options"))
                            .setConfig(verticleConfig)

                    val deploymentId = awaitResult<String> {
                        vertx.deployVerticle(verticleName, deploymentOptions, it)
                    }

                    logger.info("successfully deployed {} with id: {}", verticleName, deploymentId)
                } catch (e: Throwable) {
                    logger.error("failed to deploy $verticleName", e)
                }
            }
        }
    }
}