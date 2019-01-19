package com.revolut.trx

import com.revolut.dao.DaoFactory.accountDao
import com.revolut.dao.DaoFactory.customerDao
import com.revolut.dao.DaoFactory.transactionsDao
import com.revolut.response.ErrorCode
import com.revolut.filter.AccountByCustomerIdAndCurrencyFilter
import com.revolut.model.MoneyTransaction
import com.revolut.model.TransactionStatus
import com.revolut.utils.AccountMath
import com.revolut.utils.loggerFor

/**
 * Implements transaction on money transfer
 *
 * Created by yaroslav
 */

class MoneyTransfer(private val moneyTransaction: MoneyTransaction): Transaction() {

    override fun begin(): Boolean {
        transactionsDao.update(moneyTransaction.apply{ status = TransactionStatus.LOCK })

        val receiverResponse = customerDao.read(moneyTransaction.receiverId)
        if (receiverResponse.error != ErrorCode.OK) {
            return false
        }
        if (receiverResponse.entity?.isBlocked == true) {
            return false
        }

        val receiverAccounts = accountDao.select(
                AccountByCustomerIdAndCurrencyFilter(moneyTransaction.receiverId, moneyTransaction.currency)
        )
        if (receiverAccounts.isEmpty() || receiverAccounts.size > 1) {
            return false
        }
        val receiverAccountSelected = receiverAccounts.last()

        val receiverAmountUpdated = AccountMath.deposit(receiverAccountSelected.amount, moneyTransaction.amount)
        val receiverUpdateResult = accountDao.update(receiverAccountSelected.apply { amount = receiverAmountUpdated })

        return receiverUpdateResult.error == ErrorCode.OK
    }

    override fun commit() {
        transactionsDao.update(moneyTransaction.apply{ status = TransactionStatus.COMMIT })
    }

    override fun rollback() {
        val senderAccounts = accountDao.select(
                AccountByCustomerIdAndCurrencyFilter(moneyTransaction.senderId, moneyTransaction.currency)
        )
        if (senderAccounts.isEmpty()) {
            log.error("Unable to rollback transaction ${moneyTransaction.id}: senderAccount not found")
            return
        }
        val senderAccount = senderAccounts.last()

        val senderAccountUpdated = AccountMath.deposit(senderAccount.amount, moneyTransaction.amount)

        val senderUpdateResult = accountDao.update(senderAccount.apply { amount = senderAccountUpdated })
        if (senderUpdateResult.error != ErrorCode.OK) {
            log.error("Rollback transaction ${moneyTransaction.id} error: ${senderUpdateResult.error}")
            return
        }
        transactionsDao.update(moneyTransaction.apply{ status = TransactionStatus.ROLLBACK })
    }

    companion object {
        val log = loggerFor(MoneyTransfer::class.java)
    }
}
