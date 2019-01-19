package com.revolut.dao

import com.revolut.filter.Filter
import com.revolut.model.Entity
import com.revolut.response.Response
import com.revolut.trx.SortByTimeAscending

/**
 * Basic interface for working with collections of entities
 *
 * Created by yaroslav
 */

interface Crud<E: Entity> {
    fun create(entity: E): Response<E>
    fun read(id: Long): Response<E>
    fun update(entity: E): Response<E>
    fun delete(id: Long): Response<E>

    fun select(filter: Filter<E>, comparator: Comparator<E> = SortByTimeAscending()): List<E>
}