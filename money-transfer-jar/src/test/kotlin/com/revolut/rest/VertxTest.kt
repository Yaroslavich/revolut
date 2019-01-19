package com.revolut.rest

import com.revolut.response.ErrorCode
import com.revolut.response.Response
import com.revolut.model.*
import com.revolut.request.*
import com.revolut.utils.AccountMath
import com.revolut.trx.PeriodicJob
import com.revolut.utils.deserialize
import org.junit.After
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.DeploymentOptions
import io.vertx.core.buffer.Buffer
import io.vertx.ext.unit.TestContext
import java.io.IOException
import org.junit.Before
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import java.net.ServerSocket
import kotlin.test.assertNotNull


@RunWith(VertxUnitRunner::class)
class VertxTest {

    private val apiVersion = "v1"

    private val firstCustomerData = "R2-D2"
    private val secondCustomerData = "C-3PO"

    /**
     *   negative:
     *
     *   transfer to same user
     *
     *   One customer tries to send money to himself. Transfer early fails with ErrorCode.TRANSFER_TO_SELF_NOT_ALLOWED
     *   and no changes to customers account
     */
    @Test
    fun transferToSameCustomerTest(context: TestContext) {
        val customerId = ensureCreateCustomer(context, firstCustomerData)
        assertNotNull(customerId)
        val accountId = ensureCreateAccount(context, customerId!!, CurrencyCode.RUR)
        assertNotNull(accountId)

        ensureDeposit(context, customerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, customerId, AccountMath.hundred, CurrencyCode.RUR)

        transfer(context, customerId, customerId, AccountMath.hundred, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<MoneyTransaction> = deserialize(responseBody)
            context.assertEquals(ErrorCode.TRANSFER_TO_SELF_NOT_ALLOWED, moneyTransferResult.error)
        }
        ensureAccountAmount(context, customerId, AccountMath.hundred, CurrencyCode.RUR)
        processAllTransactions()
        ensureAccountAmount(context, customerId, AccountMath.hundred, CurrencyCode.RUR)
    }

    /**
     *   negative:
     *
     *   transfer to invalid customer
     *
     *   One customer tries to send money to invalid customer id. Transfer early fails with ErrorCode.CUSTOMER_NOT_FOUND
     *   and no changes to customers account
     */
    @Test
    fun transferToInvalidCustomerTest(context: TestContext) {
        val customerId = ensureCreateCustomer(context, firstCustomerData)
        assertNotNull(customerId)
        val accountId = ensureCreateAccount(context, customerId!!, CurrencyCode.RUR)
        assertNotNull(accountId)

        ensureDeposit(context, customerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, customerId, AccountMath.hundred, CurrencyCode.RUR)

        val invalidCustomerId = Long.MAX_VALUE

        transfer(context, customerId, invalidCustomerId, AccountMath.hundred, CurrencyCode.RUR) { responseBody ->
            val moneyTransferResult: Response<MoneyTransaction> = deserialize(responseBody)
            context.assertEquals(ErrorCode.CUSTOMER_NOT_FOUND, moneyTransferResult.error)
        }
        ensureAccountAmount(context, customerId, AccountMath.hundred, CurrencyCode.RUR)
        processAllTransactions()
        ensureAccountAmount(context, customerId, AccountMath.hundred, CurrencyCode.RUR)
    }

    /**
     *   negative:
     *
     *   transfer to another blocked account
     *
     *   One customer tries to send money to another blocked customer. Transfer early fails with ErrorCode.CUSTOMER_IS_BLOCKED
     *   and no changes to customers account
     */
    @Test
    fun transferToBlockedCustomerTest(context: TestContext) {
        val firstCustomerId = ensureCreateCustomer(context, firstCustomerData)
        assertNotNull(firstCustomerId)
        val firstAccountId = ensureCreateAccount(context, firstCustomerId!!, CurrencyCode.RUR)
        assertNotNull(firstAccountId)

        ensureDeposit(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)

        val secondCustomerId = ensureCreateCustomer(context, secondCustomerData)
        assertNotNull(secondCustomerId)
        val secondAccountId = ensureCreateAccount(context, secondCustomerId!!, CurrencyCode.RUR)
        assertNotNull(secondAccountId)

        ensureBlockCustomer(context, secondCustomerId)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        transfer(context, firstCustomerId, secondCustomerId, AccountMath.hundred, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<MoneyTransaction> = deserialize(responseBody)
            context.assertEquals(AccountMath.hundred, moneyTransferResult.entity?.amount)
            context.assertEquals(ErrorCode.CUSTOMER_IS_BLOCKED, moneyTransferResult.error)
        }
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        processAllTransactions()

        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)
    }

    /**
     *   negative:
     *
     *   transfer to another account while it becomes blocked
     *
     *   One customer tries to send money to another customer. Instantly after transfer request second customer becomes blocked.
     *   Transfer fails on processing with rollback so first customer receives back his transfer
     */
    @Test
    fun transferToLatelyBlockedCustomerTest(context: TestContext) {
        val firstCustomerId = ensureCreateCustomer(context, firstCustomerData)
        assertNotNull(firstCustomerId)
        val firstAccountId = ensureCreateAccount(context, firstCustomerId!!, CurrencyCode.RUR)
        assertNotNull(firstAccountId)

        ensureDeposit(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)

        val secondCustomerId = ensureCreateCustomer(context, secondCustomerData)
        assertNotNull(secondCustomerId)
        val secondAccountId = ensureCreateAccount(context, secondCustomerId!!, CurrencyCode.RUR)
        assertNotNull(secondAccountId)

        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        transfer(context, firstCustomerId, secondCustomerId, AccountMath.hundred, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<MoneyTransaction> = deserialize(responseBody)
            context.assertEquals(AccountMath.hundred, moneyTransferResult.entity?.amount)
            context.assertEquals(ErrorCode.OK, moneyTransferResult.error)
        }
        ensureBlockCustomer(context, secondCustomerId)
        ensureAccountAmount(context, firstCustomerId, AccountMath.zero, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        processAllTransactions()

        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)
    }

    /**
     *   negative:
     *
     *   receiver delete account after send
     *
     *   One customer tries to send money to another customer.
     *   Instantly after requesting transfer another customer deletes his account.
     *   Transfer fails on processing with rollback so first customer receives back his transfer
     */
    @Test
    fun failedTransferReturnsMoneyTest(context: TestContext) {
        val firstCustomerId = ensureCreateCustomer(context, firstCustomerData)
        assertNotNull(firstCustomerId)
        val firstAccountId = ensureCreateAccount(context, firstCustomerId!!, CurrencyCode.RUR)
        assertNotNull(firstAccountId)

        ensureDeposit(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)

        val secondCustomerId = ensureCreateCustomer(context, secondCustomerData)
        assertNotNull(secondCustomerId)
        val secondAccountId = ensureCreateAccount(context, secondCustomerId!!, CurrencyCode.RUR)
        assertNotNull(secondAccountId)

        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        transfer(context, firstCustomerId, secondCustomerId, AccountMath.hundred, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<MoneyTransaction> = deserialize(responseBody)
            context.assertEquals(ErrorCode.OK, moneyTransferResult.error)
        }
        ensureAccountAmount(context, firstCustomerId, AccountMath.zero, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        ensureDeleteAccount(context, secondCustomerId, CurrencyCode.RUR)

        processAllTransactions()

        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
    }

    /**
     *   negative:
     *
     *   transfer from blocked account
     *
     *   One blocked customer tries to send money to another customer. Transfer early fails with ErrorCode.CUSTOMER_IS_BLOCKED
     *   and no changes to customers account
     */
    @Test
    fun transferFromBlockedCustomerTest(context: TestContext) {
        val firstCustomerId = ensureCreateCustomer(context, firstCustomerData)
        assertNotNull(firstCustomerId)
        val firstAccountId = ensureCreateAccount(context, firstCustomerId!!, CurrencyCode.RUR)
        assertNotNull(firstAccountId)

        ensureDeposit(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)

        val secondCustomerId = ensureCreateCustomer(context, secondCustomerData)
        assertNotNull(secondCustomerId)
        val secondAccountId = ensureCreateAccount(context, secondCustomerId!!, CurrencyCode.RUR)
        assertNotNull(secondAccountId)

        ensureBlockCustomer(context, firstCustomerId)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        transfer(context, firstCustomerId, secondCustomerId, AccountMath.hundred, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<MoneyTransaction> = deserialize(responseBody)
            context.assertEquals(AccountMath.hundred, moneyTransferResult.entity?.amount)
            context.assertEquals(ErrorCode.CUSTOMER_IS_BLOCKED, moneyTransferResult.error)
        }
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        processAllTransactions()

        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)
    }

    /**
     *   negative:
     *
     *   transfer negative amount
     *
     *   One customer tries to send negative amount of money to another customer. Transfer early fails with ErrorCode.INVALID_AMOUNT
     *   and no changes to customers account
     */
    @Test
    fun transferNegativeAmountTest(context: TestContext) {
        val firstCustomerId = ensureCreateCustomer(context, firstCustomerData)
        assertNotNull(firstCustomerId)
        val firstAccountId = ensureCreateAccount(context, firstCustomerId!!, CurrencyCode.RUR)
        assertNotNull(firstAccountId)

        ensureDeposit(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)

        val secondCustomerId = ensureCreateCustomer(context, secondCustomerData)
        assertNotNull(secondCustomerId)
        val secondAccountId = ensureCreateAccount(context, secondCustomerId!!, CurrencyCode.RUR)
        assertNotNull(secondAccountId)

        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        transfer(context, firstCustomerId, secondCustomerId, AccountMath.minusOne, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<MoneyTransaction> = deserialize(responseBody)
            context.assertEquals(AccountMath.minusOne, moneyTransferResult.entity?.amount)
            context.assertEquals(ErrorCode.INVALID_AMOUNT, moneyTransferResult.error)
        }
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        processAllTransactions()

        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)
    }

    /**
     *   negative:
     *
     *   transfer zero amount
     *
     *   One customer tries to send zero amount of money to another customer. Transfer early fails with ErrorCode.INVALID_AMOUNT
     *   and no changes to customers account
     */
    @Test
    fun transferZeroAmountTest(context: TestContext) {
        val firstCustomerId = ensureCreateCustomer(context, firstCustomerData)
        assertNotNull(firstCustomerId)
        val firstAccountId = ensureCreateAccount(context, firstCustomerId!!, CurrencyCode.RUR)
        assertNotNull(firstAccountId)

        ensureDeposit(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)

        val secondCustomerId = ensureCreateCustomer(context, secondCustomerData)
        assertNotNull(secondCustomerId)
        val secondAccountId = ensureCreateAccount(context, secondCustomerId!!, CurrencyCode.RUR)
        assertNotNull(secondAccountId)

        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        transfer(context, firstCustomerId, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<MoneyTransaction> = deserialize(responseBody)
            context.assertEquals(AccountMath.zero, moneyTransferResult.entity?.amount)
            context.assertEquals(ErrorCode.INVALID_AMOUNT, moneyTransferResult.error)
        }
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        processAllTransactions()

        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)
    }

    /**
     *   negative:
     *
     *   transfer from account with insufficient fund
     *
     *   One customer tries to send more money than he has to another customer. Transfer early fails with ErrorCode.INSUFFICIENT_FUNDS
     *   and no changes to customers account
     */
    @Test
    fun transferMoreThanSenderHasTest(context: TestContext) {
        val firstCustomerId = ensureCreateCustomer(context, firstCustomerData)
        assertNotNull(firstCustomerId)
        val firstAccountId = ensureCreateAccount(context, firstCustomerId!!, CurrencyCode.RUR)
        assertNotNull(firstAccountId)

        ensureDeposit(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)

        val secondCustomerId = ensureCreateCustomer(context, secondCustomerData)
        assertNotNull(secondCustomerId)
        val secondAccountId = ensureCreateAccount(context, secondCustomerId!!, CurrencyCode.RUR)
        assertNotNull(secondAccountId)

        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        transfer(context, firstCustomerId, secondCustomerId, AccountMath.thousand, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<MoneyTransaction> = deserialize(responseBody)
            context.assertEquals(AccountMath.thousand, moneyTransferResult.entity?.amount)
            context.assertEquals(ErrorCode.INSUFFICIENT_FUNDS, moneyTransferResult.error)
        }
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        processAllTransactions()

        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)
    }

    /**
     *   negative:
     *
     *   withdraw money from given account with insufficient fund
     *
     *   One customer tries to withdraw more money from his account than he have. Withdraw early fails with ErrorCode.INSUFFICIENT_FUNDS
     *   and no changes to customers account
     */
    @Test
    fun withdrawFromAccountWithInsufficientFundsTest(context: TestContext) {
        val customerId = ensureCreateCustomer(context, firstCustomerData)
        assertNotNull(customerId)

        val accountId = ensureCreateAccount(context, customerId!!, CurrencyCode.RUR)
        assertNotNull(accountId)

        ensureDeposit(context, customerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, customerId, AccountMath.hundred, CurrencyCode.RUR)

        withdraw(context, customerId, AccountMath.thousand, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<Account> = deserialize(responseBody)
            context.assertEquals(AccountMath.hundred, moneyTransferResult.entity?.amount)
            context.assertEquals(CurrencyCode.RUR, moneyTransferResult.entity?.currency)
            context.assertEquals(ErrorCode.INSUFFICIENT_FUNDS, moneyTransferResult.error)
        }
    }

    /**
     *   negative:
     *
     *   transfer to another account in different currency
     *
     *   One customer tries to send money to customer who has no account in same currency. Transfer early fails with ErrorCode.ACCOUNT_NOT_EXISTS
     *   and no changes to customers account
     */
    @Test
    fun transferToAnotherCurrencyAccountTest(context: TestContext) {
        val firstCustomerId = ensureCreateCustomer(context, firstCustomerData)
        assertNotNull(firstCustomerId)
        val firstAccountId = ensureCreateAccount(context, firstCustomerId!!, CurrencyCode.RUR)
        assertNotNull(firstAccountId)

        ensureDeposit(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)

        val secondCustomerId = ensureCreateCustomer(context, secondCustomerData)
        assertNotNull(secondCustomerId)
        val secondAccountId = ensureCreateAccount(context, secondCustomerId!!, CurrencyCode.USD)
        assertNotNull(secondAccountId)

        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.USD)

        transfer(context, firstCustomerId, secondCustomerId, AccountMath.hundred, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<MoneyTransaction> = deserialize(responseBody)
            context.assertEquals(AccountMath.hundred, moneyTransferResult.entity?.amount)
            context.assertEquals(ErrorCode.ACCOUNT_NOT_EXISTS, moneyTransferResult.error)
        }
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.USD)

        processAllTransactions()

        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.USD)
    }

    /**
     *   negative:
     *
     *   receiver creates appropriate account after send
     *
     *   One customer tries to send money to another customer.
     *   Another customer has no account of same currency but manages to create such account before transaction is processed.
     *   Transfer early fails with ErrorCode.ACCOUNT_NOT_EXISTS and no changes to customers account
     */
    @Test
    fun transferToAccountCreatedAfterSendTest(context: TestContext) {
        val firstCustomerId = ensureCreateCustomer(context, firstCustomerData)
        assertNotNull(firstCustomerId)
        val firstAccountId = ensureCreateAccount(context, firstCustomerId!!, CurrencyCode.RUR)
        assertNotNull(firstAccountId)

        ensureDeposit(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)

        val secondCustomerId = ensureCreateCustomer(context, secondCustomerData)
        assertNotNull(secondCustomerId)

        transfer(context, firstCustomerId, secondCustomerId!!, AccountMath.hundred, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<MoneyTransaction> = deserialize(responseBody)
            context.assertEquals(ErrorCode.ACCOUNT_NOT_EXISTS, moneyTransferResult.error)
        }
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)

        val secondAccountId = ensureCreateAccount(context, secondCustomerId, CurrencyCode.RUR)
        assertNotNull(secondAccountId)

        processAllTransactions()

        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)
    }

    /**
     *   positive:
     *
     *   deposit money to given account
     *
     *   One customer tries to deposit money to his account. Deposit succeeds
     */
    @Test
    fun depositToAccountTest(context: TestContext) {
        val customerId = ensureCreateCustomer(context, firstCustomerData)
        assertNotNull(customerId)

        val accountId = ensureCreateAccount(context, customerId!!, CurrencyCode.RUR)
        assertNotNull(accountId)

        deposit(context, customerId, AccountMath.hundred, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<Account> = deserialize(responseBody)
            context.assertEquals(AccountMath.hundred, moneyTransferResult.entity?.amount)
            context.assertEquals(CurrencyCode.RUR, moneyTransferResult.entity?.currency)
            context.assertEquals(ErrorCode.OK, moneyTransferResult.error)
        }
    }

    /**
     *   positive:
     *
     *   withdraw money from given account
     *
     *   One customer tries to withdraw money from his account that contains enough. Withdraw succeeds
     */
    @Test
    fun withdrawFromAccountTest(context: TestContext) {
        val customerId = ensureCreateCustomer(context, firstCustomerData)
        assertNotNull(customerId)

        val accountId = ensureCreateAccount(context, customerId!!, CurrencyCode.RUR)
        assertNotNull(accountId)

        ensureDeposit(context, customerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, customerId, AccountMath.hundred, CurrencyCode.RUR)

        withdraw(context, customerId, AccountMath.hundred, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<Account> = deserialize(responseBody)
            context.assertEquals(AccountMath.zero, moneyTransferResult.entity?.amount)
            context.assertEquals(CurrencyCode.RUR, moneyTransferResult.entity?.currency)
            context.assertEquals(ErrorCode.OK, moneyTransferResult.error)
        }
    }

    /**
     *   positive:
     *
     *   transfer to another customer
     *
     *   One customer tries to transfer money to another customer. Transfer succeeds
     */
    @Test
    fun transferToAnotherCustomerTest(context: TestContext) {
        val firstCustomerId = ensureCreateCustomer(context, firstCustomerData)
        assertNotNull(firstCustomerId)
        val firstAccountId = ensureCreateAccount(context, firstCustomerId!!, CurrencyCode.RUR)
        assertNotNull(firstAccountId)

        ensureDeposit(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureAccountAmount(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)

        val secondCustomerId = ensureCreateCustomer(context, secondCustomerData)
        assertNotNull(secondCustomerId)
        val secondAccountId = ensureCreateAccount(context, secondCustomerId!!, CurrencyCode.RUR)
        assertNotNull(secondAccountId)

        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        transfer(context, firstCustomerId, secondCustomerId, AccountMath.hundred, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<MoneyTransaction> = deserialize(responseBody)
            context.assertEquals(AccountMath.hundred, moneyTransferResult.entity?.amount)
            context.assertEquals(ErrorCode.OK, moneyTransferResult.error)
        }
        ensureAccountAmount(context, firstCustomerId, AccountMath.zero, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        processAllTransactions()

        ensureAccountAmount(context, firstCustomerId, AccountMath.zero, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.hundred, CurrencyCode.RUR)
    }

    /**
     *   positive:
     *
     *   transfer several times in a raw
     *
     *   One customer tries to transfer money to another customer three times: 100, 1000 and 1. 
     *   Transfer succeeds with correct sum
     */
    @Test
    fun transferSeveralTimesTest(context: TestContext) {
        val firstCustomerId = ensureCreateCustomer(context, firstCustomerData)
        assertNotNull(firstCustomerId)
        val firstAccountId = ensureCreateAccount(context, firstCustomerId!!, CurrencyCode.RUR)
        assertNotNull(firstAccountId)

        ensureDeposit(context, firstCustomerId, AccountMath.thousand, CurrencyCode.RUR)
        ensureDeposit(context, firstCustomerId, AccountMath.hundred, CurrencyCode.RUR)
        ensureDeposit(context, firstCustomerId, AccountMath.one, CurrencyCode.RUR)

        val expectedSum = AccountMath.sum(listOf(AccountMath.thousand, AccountMath.hundred, AccountMath.one))
        ensureAccountAmount(context, firstCustomerId, expectedSum, CurrencyCode.RUR)

        val secondCustomerId = ensureCreateCustomer(context, secondCustomerData)
        assertNotNull(secondCustomerId)
        val secondAccountId = ensureCreateAccount(context, secondCustomerId!!, CurrencyCode.RUR)
        assertNotNull(secondAccountId)

        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        transfer(context, firstCustomerId, secondCustomerId, AccountMath.hundred, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<MoneyTransaction> = deserialize(responseBody)
            context.assertEquals(AccountMath.hundred, moneyTransferResult.entity?.amount)
            context.assertEquals(ErrorCode.OK, moneyTransferResult.error)
        }
        transfer(context, firstCustomerId, secondCustomerId, AccountMath.one, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<MoneyTransaction> = deserialize(responseBody)
            context.assertEquals(AccountMath.one, moneyTransferResult.entity?.amount)
            context.assertEquals(ErrorCode.OK, moneyTransferResult.error)
        }
        transfer(context, firstCustomerId, secondCustomerId, AccountMath.thousand, CurrencyCode.RUR)  { responseBody ->
            val moneyTransferResult: Response<MoneyTransaction> = deserialize(responseBody)
            context.assertEquals(AccountMath.thousand, moneyTransferResult.entity?.amount)
            context.assertEquals(ErrorCode.OK, moneyTransferResult.error)
        }
        ensureAccountAmount(context, firstCustomerId, AccountMath.zero, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, AccountMath.zero, CurrencyCode.RUR)

        processAllTransactions()

        ensureAccountAmount(context, firstCustomerId, AccountMath.zero, CurrencyCode.RUR)
        ensureAccountAmount(context, secondCustomerId, expectedSum, CurrencyCode.RUR)
    }

    private fun ensureAccountAmount(context: TestContext, customerId: Long, amount: BigDecimal, currency: CurrencyCode) {
        findAccount(context, customerId, currency) { responseBody ->
            val response: Response<Account> = deserialize(responseBody)

            context.assertEquals(ErrorCode.OK, response.error)
            context.assertNotNull(response.entity)
            context.assertNotNull(response.entity?.id)
            context.assertNotNull(response.entity?.currency)
            context.assertNotNull(response.entity?.amount)

            context.assertEquals(amount, response.entity?.amount)
            context.assertEquals(currency, response.entity?.currency)
            context.assertNotEquals(UNDEFINED_ID, response.entity?.id)
        }
    }

    private fun ensureCreateAccount(context: TestContext, customerId: Long, currency: CurrencyCode): Long? {
        var accountId: Long? = UNDEFINED_ID
        createAccount(context, customerId, currency) { responseBody ->
            val response: Response<Account> = deserialize(responseBody)

            accountId = response.entity?.id

            context.assertNotNull(response.entity)
            context.assertNotNull(accountId)
            context.assertNotNull(response.entity?.customerId)
            context.assertEquals(customerId, response.entity?.customerId)
            context.assertNotEquals(UNDEFINED_ID, response.entity?.id)
        }
        return accountId
    }

    private fun ensureDeleteAccount(context: TestContext, customerId: Long, currency: CurrencyCode): Long? {
        var accountId: Long? = UNDEFINED_ID
        deleteAccount(context, customerId, currency) { responseBody ->
            val response: Response<Account> = deserialize(responseBody)

            accountId = response.entity?.id

            context.assertNotNull(response.entity)
            context.assertNotNull(accountId)
            context.assertNotNull(response.entity?.customerId)
            context.assertEquals(customerId, response.entity?.customerId)
            context.assertNotEquals(UNDEFINED_ID, response.entity?.id)
        }
        return accountId
    }

    private fun ensureCreateCustomer(context: TestContext, personalData: String): Long? {
        var customerId: Long? = UNDEFINED_ID
        createCustomer(context, personalData) { responseBody ->
            val response: Response<Customer> = deserialize(responseBody)

            customerId = response.entity?.id

            context.assertNotNull(customerId)
            context.assertNotNull(response.entity?.dataHash)
            context.assertNotEquals(UNDEFINED_ID, customerId)
        }
        return customerId
    }

    private fun ensureBlockCustomer(context: TestContext, customerId: Long) {
        blockCustomer(context, customerId) { responseBody ->
            val response: Response<Customer> = deserialize(responseBody)

            context.assertNotNull(response.entity?.id)
            context.assertTrue(response.entity?.isBlocked ?: false)
            context.assertNotEquals(UNDEFINED_ID, customerId)
        }
    }

    private fun ensureDeposit(context: TestContext, customerId: Long, amount: BigDecimal, currency: CurrencyCode) {
        deposit(context, customerId, amount, currency)  { responseBody ->
            val moneyDepositResult: Response<Account> = deserialize(responseBody)
            context.assertEquals(ErrorCode.OK, moneyDepositResult.error)
        }
    }

    private fun ensureWithdraw(context: TestContext, customerId: Long, amount: BigDecimal, currency: CurrencyCode) {
        withdraw(context, customerId, amount, currency)  { responseBody ->
            val moneyDepositResult: Response<Account> = deserialize(responseBody)
            context.assertEquals(ErrorCode.OK, moneyDepositResult.error)
        }
    }

    private fun findAccount(context: TestContext, customerId: Long, currency: CurrencyCode, onResponse: (Buffer) -> TestContext) {
        val findAccountRequest = FindAccountByCustomerIdRequest(customerId, currency)
        httpPost(context, "account/find", findAccountRequest.toBuffer(), onResponse)
    }

    private fun createAccount(context: TestContext, customerId: Long, currency: CurrencyCode, onResponse: (Buffer) -> TestContext) {
        val createAccountRequest = CreateAccountRequest(customerId, currency)
        httpPost(context, "account/create", createAccountRequest.toBuffer(), onResponse)
    }

    private fun deleteAccount(context: TestContext, customerId: Long, currency: CurrencyCode, onResponse: (Buffer) -> TestContext) {
        val deleteAccountRequest = DeleteAccountRequest(customerId, currency)
        httpPost(context, "account/delete", deleteAccountRequest.toBuffer(), onResponse)
    }

    private fun createCustomer(context: TestContext, personalData: String, onResponse: (Buffer) -> TestContext) {
        val createCustomerRequest = CreateCustomerRequest(personalData)
        httpPost(context, "customer/create", createCustomerRequest.toBuffer(), onResponse)
    }

    private fun blockCustomer(context: TestContext, customerId: Long, onResponse: (Buffer) -> TestContext) {
        val createCustomerRequest = BlockCustomerRequest(customerId)
        httpPost(context, "customer/block", createCustomerRequest.toBuffer(), onResponse)
    }

    private fun transfer(context: TestContext, customerId: Long, customerId2: Long, amount: BigDecimal, currency: CurrencyCode, onResponse: (Buffer) -> TestContext) {
        val moneyTransferRequest = MoneyTransferRequest(customerId, customerId2, currency, amount)
        httpPost(context, "transfer", moneyTransferRequest.toBuffer(), onResponse)
    }

    private fun deposit(context: TestContext, customerId: Long, amount: BigDecimal, currency: CurrencyCode, onResponse: (Buffer) -> TestContext) {
        val moneyDepositRequest = MoneyDepositRequest(customerId, currency, amount)
        httpPost(context, "deposit", moneyDepositRequest.toBuffer(), onResponse)
    }

    private fun withdraw(context: TestContext, customerId: Long, amount: BigDecimal, currency: CurrencyCode, onResponse: (Buffer) -> TestContext) {
        val moneyDepositRequest = MoneyWithdrawRequest(customerId, currency, amount)
        httpPost(context, "withdraw", moneyDepositRequest.toBuffer(), onResponse)
    }

    private fun httpGet(context: TestContext, path: String, param: Long, onResponse: (Buffer) -> TestContext) {
        val async = context.async()
        val client = vertx.createHttpClient()
        client.getNow(port, "localhost", "/$apiVersion/$path?id=$param") { response ->
            response.bodyHandler { body ->
                onResponse(body)
                client.close()
                async.complete()
            }
        }
        async.awaitSuccess()
    }

    private fun httpPost(context: TestContext, path: String, requestBody: Buffer, onResponse: (Buffer) -> TestContext) {
        val async = context.async()
        val client = vertx.createHttpClient()

        client.post(port, "localhost", "/$apiVersion/$path") { response ->
            response.bodyHandler { responseBody ->
                onResponse(responseBody)
                client.close()
                async.complete()
            }
        }.end(requestBody)
        async.awaitSuccess()
    }

    private fun processAllTransactions() {
        PeriodicJob.process()
    }

    private fun pickRandomPort(): Int {
        val socket = ServerSocket(0)
        val randomPort = socket.localPort
        socket.close()

        return randomPort
    }

    private lateinit var vertx: Vertx
    private var port: Int = 0

    @Before
    @Throws(IOException::class)
    fun before(context: TestContext) {
        port = pickRandomPort()

        val options = DeploymentOptions()
                .setConfig(
                        JsonObject()
                                .put("port", port)
                                .put("trxProcessingPeriod", 0L)
                )

        vertx = Vertx.vertx()
        vertx.deployVerticle(ApiVerticle::class.java.name, options, context.asyncAssertSuccess())
    }

    @After
    fun after(context: TestContext) {
        vertx.close(context.asyncAssertSuccess())
    }
}