package com.revolut.utils

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.awaitResult
import java.io.File

/**
 *
 */
internal class ConfigLoader {
    private val logger = loggerFor(javaClass)

    fun loadBlocking(fileName: String): JsonObject {
        try {
            val file = File(fileName)
            return if (file.exists()) {
                logger.debug("loading config from file \"{}\"", fileName)

                file.reader().use {
                    JsonObject(it.readText())
                }
            } else {
                logger.debug("loading config from resources \"{}\"", fileName)

                javaClass.getResourceAsStream("/$fileName").reader().use {
                    JsonObject(it.readText())
                }
            }
        } catch (e: Throwable) {
            logger.warn("failed to load config \"$fileName\"", e)
            return JsonObject()
        }
    }

    suspend fun load(vertx: Vertx, fileName: String): JsonObject {
        try {
            logger.debug("loading config \"{}\"", fileName)

            return awaitResult<Buffer> {
                vertx.fileSystem().readFile(fileName, it)
            }.toJsonObject()
        } catch (e: Throwable) {
            logger.warn("failed to load config \"$fileName\"", e)
            return JsonObject()
        }
    }
}