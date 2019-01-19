package com.revolut.response

enum class ErrorCode {
    /**
     *   Operation succeeded
     */
    OK,
    /**
     *   Asked entity not found in database
     */
    DB_ENTITY_NOT_FOUND,
    /**
     *   Customer tried to transfer money to himself
     */
    TRANSFER_TO_SELF_NOT_ALLOWED,
    /**
     *   Customer trying to create more than one account of same currency
     */
    ACCOUNT_ALREADY_EXISTS,
    /**
     *   Asked account not found in database
     */
    ACCOUNT_NOT_EXISTS,
    /**
     *   More than one entity with same id found in database
     */
    DUPLICATED_ACCOUNTS_FOUND,
    /**
     *   Customer trying to withdraw more than he has
     */
    INSUFFICIENT_FUNDS,
    /**
     *   Customer not found in database
     */
    CUSTOMER_NOT_FOUND,
    /**
     *   Customer is temporarily blocked
     */
    CUSTOMER_IS_BLOCKED,
    /**
     *   Customer trying to transfer invalid amount of money
     */
    INVALID_AMOUNT
}
