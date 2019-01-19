package com.revolut.filter

import com.revolut.model.MoneyTransaction
import com.revolut.model.TransactionStatus

class TransactionStatusFilter(private val status: TransactionStatus): Filter<MoneyTransaction> {
    override fun isAcceptable(entity: MoneyTransaction): Boolean {
        return entity.status == status
    }
}