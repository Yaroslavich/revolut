package com.revolut.service

import com.revolut.dao.DaoFactory.accountDao
import com.revolut.response.ErrorCode
import com.revolut.response.Response
import com.revolut.filter.AccountByCustomerIdAndCurrencyFilter
import com.revolut.model.Account
import com.revolut.model.CurrencyCode
import com.revolut.model.UNDEFINED_ID
import com.revolut.utils.AccountMath
import com.revolut.utils.AccountMath.zero
import com.revolut.utils.getNow
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicLong

/**
 * AccountService
 *
 * Created by yaroslav
 */

class AccountService: Service<Account> {
    private val nextId: AtomicLong = AtomicLong(0)

    override fun getEntity(id: Long): Response<Account> {
        return accountDao.read(id)
    }

    fun create(customerId: Long, currency: CurrencyCode): Response<Account> {
        val accountsFound = accountDao.select(AccountByCustomerIdAndCurrencyFilter(customerId, currency))
        if (!accountsFound.isEmpty()) {
            return Response(accountsFound.last(), ErrorCode.ACCOUNT_ALREADY_EXISTS)
        }
        return accountDao.create(Account(
                id = generateId(),
                timestamp = getNow(),
                customerId = customerId,
                amount = zero,
                currency = currency
        ))
    }

    fun read(id: Long): Response<Account> {
        return accountDao.read(id)
    }

    fun update(customerId: Long, amountUfterUpdate: BigDecimal, currency: CurrencyCode): Response<Account> {
        val accountsFound = accountDao.select(AccountByCustomerIdAndCurrencyFilter(customerId, currency))
        if (accountsFound.isEmpty()) {
            return Response(Account(UNDEFINED_ID, getNow(), currency, amountUfterUpdate, customerId), ErrorCode.ACCOUNT_NOT_EXISTS)
        }
        if (accountsFound.size > 1) {
            return Response(accountsFound.last(), ErrorCode.DUPLICATED_ACCOUNTS_FOUND)
        }
        return accountDao.update(accountsFound.last().apply { amount = amountUfterUpdate })
    }

    fun delete(id: Long): Response<Account> {
        return accountDao.delete(id)
    }

    fun deleteByCustomerId(customerId: Long, currency: CurrencyCode): Response<Account> {
        val accountsFound = accountDao.select(AccountByCustomerIdAndCurrencyFilter(customerId, currency))
        if (accountsFound.isEmpty()) {
            return Response(null, ErrorCode.ACCOUNT_NOT_EXISTS)
        }
        if (accountsFound.size > 1) {
            return Response(accountsFound.last(), ErrorCode.DUPLICATED_ACCOUNTS_FOUND)
        }
        return accountDao.delete(accountsFound.last().id)
    }

    fun findByCustomerId(customerId: Long, currency: CurrencyCode): Response<Account> {
        val accountsFound = accountDao.select(AccountByCustomerIdAndCurrencyFilter(customerId, currency))
        if (accountsFound.isEmpty()) {
            return Response(null, ErrorCode.ACCOUNT_NOT_EXISTS)
        }
        return Response(accountsFound.last(), ErrorCode.OK)
    }

    /**
     *   This method for internal use only
     *   To change accounts use {@link MoneyTransactionService#transfer(Long, Long, CurrencyCode, BigDecimal), transfer}
     */
    fun deposit(customerId: Long, currency: CurrencyCode, amountToDeposit: BigDecimal): Response<Account> {
        val accountsFound = accountDao.select(AccountByCustomerIdAndCurrencyFilter(customerId, currency))
        if (accountsFound.isEmpty()) {
            return Response(Account(UNDEFINED_ID, getNow(), currency, amountToDeposit, customerId), ErrorCode.ACCOUNT_NOT_EXISTS)
        }
        if (accountsFound.size > 1) {
            return Response(accountsFound.last(), ErrorCode.DUPLICATED_ACCOUNTS_FOUND)
        }
        return accountDao.update(accountsFound.last().apply { amount = AccountMath.deposit(amount, amountToDeposit) })
    }

    /**
     *   This method for internal use only
     *   To change accounts use {@link MoneyTransactionService#transfer(Long, Long, CurrencyCode, BigDecimal), transfer}
     */
    fun withdraw(customerId: Long, currency: CurrencyCode, amountToWithdraw: BigDecimal): Response<Account> {
        val accountsFound = accountDao.select(AccountByCustomerIdAndCurrencyFilter(customerId, currency))
        if (accountsFound.isEmpty()) {
            return Response(Account(UNDEFINED_ID, getNow(), currency, amountToWithdraw, customerId), ErrorCode.ACCOUNT_NOT_EXISTS)
        }
        if (accountsFound.size > 1) {
            return Response(accountsFound.last(), ErrorCode.DUPLICATED_ACCOUNTS_FOUND)
        }
        val account = accountsFound.last()
        if (account.amount < amountToWithdraw) {
            return Response(account, ErrorCode.INSUFFICIENT_FUNDS)
        }
        return accountDao.update(account.apply { amount = AccountMath.withdraw(amount, amountToWithdraw) })
    }

    private fun generateId(): Long {
        return nextId.incrementAndGet()
    }
}