package com.revolut.filter

import com.revolut.model.Account
import com.revolut.model.CurrencyCode

/**
 *   Filter answers one simple question: whether an entity should be filtered or not.
 *   If entity is acceptable then returns true and false otherwise.
 *
 *   According to SOLID principles any filtering algorithms should implement this interface.
 */

class AccountByCustomerIdAndCurrencyFilter(private val customerId: Long, private val currency: CurrencyCode): Filter<Account> {
    override fun isAcceptable(entity: Account): Boolean {
        return (entity.customerId == customerId && entity.currency == currency)
    }
}