package com.revolut.trx

import com.revolut.filter.TransactionStatusFilter
import com.revolut.model.MoneyTransaction
import com.revolut.model.TransactionStatus
import com.revolut.service.ServiceFactory.moneyTransferService
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Pending transactions processed periodically by this job
 *
 * Created by yaroslav
 */


object PeriodicJob {

    private val acceptOnlyPending = TransactionStatusFilter(TransactionStatus.PENDING)
    private val sortByTimeAscending = SortByTimeAscending<MoneyTransaction>()

    fun process() {
        val transactions: List<MoneyTransaction> = moneyTransferService.select(acceptOnlyPending, sortByTimeAscending)
        val lock = ReentrantLock()
        for (transaction in transactions) {
            lock.withLock {
                MoneyTransfer(transaction).process()
            }
        }
    }
}