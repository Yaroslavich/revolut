package com.revolut.filter

import com.revolut.model.Entity

/**
 *   Filter answers one simple question: whether an entity should be filtered or not.
 *   If entity is acceptable then returns true and false otherwise.
 *
 *   According to SOLID principles any filtering algorithms should implement this interface.
 */

interface Filter<E: Entity> {
    fun isAcceptable(entity: E): Boolean
}