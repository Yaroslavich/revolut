package com.revolut.db

import com.revolut.dao.Crud
import com.revolut.response.ErrorCode
import com.revolut.response.Response
import com.revolut.filter.Filter
import com.revolut.model.Entity
import java.util.concurrent.atomic.AtomicLong

/**
 * Generified database mock
 *
 * Created by yaroslav
 */

class DatabaseMock<T: Entity>: Crud<T> {
    private val collection: MutableMap<Long, T> = mutableMapOf()

    private val nextId = AtomicLong(0)

    override fun create(entity: T): Response<T> {
        collection[entity.id] = entity
        return Response(entity, ErrorCode.OK)
    }

    override fun read(id: Long): Response<T> {
        val entity = collection[id]
                ?: return Response(null, ErrorCode.DB_ENTITY_NOT_FOUND)
        return Response(entity, ErrorCode.OK)
    }

    override fun update(entity: T): Response<T> {
        collection[entity.id] = entity
        return Response(entity, ErrorCode.OK)
    }

    override fun delete(id: Long): Response<T> {
        val removedEntity = collection.remove(id)
        return Response(removedEntity, ErrorCode.OK)
    }

    override fun select(filter: Filter<T>, comparator: Comparator<T>): List<T> {
        val entitiesFound: MutableList<T> = mutableListOf()
        for (entity in collection.values) {
            if (filter.isAcceptable(entity)) {
                entitiesFound.add(entity)
            }
        }
        entitiesFound.sortWith(comparator)

        return entitiesFound
    }
}