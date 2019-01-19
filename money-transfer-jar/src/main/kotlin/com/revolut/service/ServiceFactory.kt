package com.revolut.service

/**
 * Very simple substitute for factory functionality
 * Just to keep single instance of every service
 *
 * Created by yaroslav
 */

object ServiceFactory {
    val accountService = AccountService()
    val customerService = CustomerService()
    val moneyTransferService = MoneyTransactionService()
}