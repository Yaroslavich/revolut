package com.revolut.trx

import com.revolut.model.Entity

class SortByTimeAscending<E: Entity>: Comparator<E> {
    override fun compare(o1: E, o2: E): Int {
        return o1.timestamp.compareTo(o2.timestamp)
    }
}