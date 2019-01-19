package com.revolut.service

import com.revolut.response.Response
import com.revolut.model.Entity

/**
 * Basic interface for all services
 * Mostly a marker interface
 *
 * Created by yaroslav
 */

interface Service<E: Entity> {
    fun getEntity(id: Long): Response<E>
}