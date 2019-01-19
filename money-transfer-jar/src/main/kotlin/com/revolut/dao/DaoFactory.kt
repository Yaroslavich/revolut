package com.revolut.dao

import com.revolut.dao.impl.CrudDaoImpl
import com.revolut.model.Account
import com.revolut.model.Customer
import com.revolut.model.MoneyTransaction

/**
 * Very simple substitute of factory functionality
 *
 * Created by yaroslav
 */

object DaoFactory {
    val customerDao = CrudDaoImpl<Customer>()
    val accountDao = CrudDaoImpl<Account>()
    val transactionsDao = CrudDaoImpl<MoneyTransaction>()
}