package com.revolut.dao.impl

import com.revolut.dao.Crud
import com.revolut.response.Response
import com.revolut.db.DatabaseMock
import com.revolut.filter.Filter
import com.revolut.model.Entity

/**
 * Basic implementation of Create-Read-Update-Delete interface
 * with simple select functionality
 *
 * Generifies work with collections of entities
 *
 * Created by yaroslav
 */

class CrudDaoImpl<E: Entity>: Crud<E> {
    private val databaseMock = DatabaseMock<E>()

    override fun create(entity: E): Response<E> {
        return databaseMock.create(entity)
    }

    override fun read(id: Long): Response<E> {
        return databaseMock.read(id)
    }

    override fun update(entity: E): Response<E> {
        return databaseMock.update(entity)
    }

    override fun delete(id: Long): Response<E> {
        return databaseMock.delete(id)
    }

    override fun select(filter: Filter<E>, comparator: Comparator<E>): List<E> {
        return databaseMock.select(filter, comparator)
    }
}