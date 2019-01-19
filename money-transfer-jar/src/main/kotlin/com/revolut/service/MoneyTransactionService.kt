package com.revolut.service

import com.revolut.dao.DaoFactory.transactionsDao
import com.revolut.response.ErrorCode
import com.revolut.response.Response
import com.revolut.filter.Filter
import com.revolut.model.*
import com.revolut.service.ServiceFactory.accountService
import com.revolut.service.ServiceFactory.customerService
import com.revolut.trx.SortByTimeAscending
import com.revolut.utils.AccountMath
import com.revolut.utils.getNow
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicLong
import java.util.Comparator

/**
 * MoneyTransactionService
 *
 * Created by yaroslav
 */

class MoneyTransactionService: Service<MoneyTransaction> {
    private val nextId: AtomicLong = AtomicLong(0)

    override fun getEntity(id: Long): Response<MoneyTransaction> {
        return transactionsDao.read(id)
    }

    fun transfer(senderId: Long, receiverId: Long, currency: CurrencyCode, amount: BigDecimal): Response<MoneyTransaction> {
        val errorCode = checkTransferPreconditions(senderId, receiverId, currency, amount)
        if (errorCode != ErrorCode.OK) {
            return Response(
                    MoneyTransaction(UNDEFINED_ID, getNow(), TransactionStatus.REJECTED, senderId, receiverId, currency, amount),
                    errorCode
            )
        }
        val response = accountService.withdraw(senderId, currency, amount)
        if (response.error != ErrorCode.OK) {
            return Response(
                    MoneyTransaction(UNDEFINED_ID, getNow(), TransactionStatus.REJECTED, senderId, receiverId, currency, amount),
                    response.error
            )
        }
        // enqueue operation
        return transactionsDao.create(
                MoneyTransaction(generateId(), getNow(), TransactionStatus.PENDING, senderId, receiverId, currency, amount)
        )
    }

    private fun checkTransferPreconditions(senderId: Long, receiverId: Long, currency: CurrencyCode, amount: BigDecimal): ErrorCode {
        if (senderId == receiverId) {
            return ErrorCode.TRANSFER_TO_SELF_NOT_ALLOWED
        }
        if (amount <= AccountMath.zero) {
            return ErrorCode.INVALID_AMOUNT
        }

        val senderResponse = customerService.read(senderId)
        if (senderResponse.error != ErrorCode.OK) {
            return ErrorCode.CUSTOMER_NOT_FOUND
        }
        val sender: Customer = senderResponse.entity
                ?: return ErrorCode.CUSTOMER_NOT_FOUND
        if (sender.isBlocked) {
            return ErrorCode.CUSTOMER_IS_BLOCKED
        }

        val receiverResponse = customerService.read(receiverId)
        if (receiverResponse.error != ErrorCode.OK) {
            return ErrorCode.CUSTOMER_NOT_FOUND
        }
        val receiver: Customer = receiverResponse.entity
                ?: return ErrorCode.CUSTOMER_NOT_FOUND
        if (receiver.isBlocked) {
            return ErrorCode.CUSTOMER_IS_BLOCKED
        }
        val receiverAcounts = accountService.findByCustomerId(receiverId, currency)
        if (receiverAcounts.error != ErrorCode.OK) {
            return ErrorCode.ACCOUNT_NOT_EXISTS
        }

        return ErrorCode.OK
    }

    fun select(filter: Filter<MoneyTransaction>, comparator: Comparator<MoneyTransaction> = defaultOrder): List<MoneyTransaction> {
        return transactionsDao.select(filter, comparator)
    }

    private fun generateId(): Long {
        return nextId.incrementAndGet()
    }

    companion object {
        val defaultOrder = SortByTimeAscending<MoneyTransaction>()
    }
}