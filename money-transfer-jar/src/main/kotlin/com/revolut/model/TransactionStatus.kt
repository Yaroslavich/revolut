package com.revolut.model

/**
 * TransactionStatus
 *
 * Lifecycle of transaction:
 *
 * (REJECTED | PENDING) -> (LOCK) -> (COMMIT | ROLLBACK)
 *
 * Created by yaroslav
 */

enum class TransactionStatus {
    /**
    *    Transaction rejected immediately before placing it in queue to process
     *   It happens when business logic checks customer requests
     */
    REJECTED,

    /**
     *   Transaction successfully placed in queue to process
     *   That means no changes made yet to customer accounts
     */
    PENDING,

    /**
     *   Transaction is in processing now
     *   Status remains until it becomes COMMIT or ROLLBACK
     */
    LOCK,

    /**
     *   Transaction has been successfully committed
     *   All changes are made to accounts with no errors
     */
    COMMIT,

    /**
     *   Due to error while processed transaction has been rolled back
     *   All changes to accounts returned to their original states with no errors
     */
    ROLLBACK
}