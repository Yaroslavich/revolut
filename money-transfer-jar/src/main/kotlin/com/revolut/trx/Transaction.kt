package com.revolut.trx

/**
 * Abstract transaction
 */

abstract class Transaction {

    fun process() {
        if (begin()) {
            commit()
        } else {
            rollback()
        }
    }

    abstract fun begin(): Boolean
    abstract fun commit()
    abstract fun rollback()
}