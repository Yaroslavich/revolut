package com.revolut.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * Basic interface for all data stored in database
 * Yeah, this @JsonTypeInfo looks ugly and implements an antipattern of polimorphism
 * But it's a quick solution with less code
 *
 * Created by yaroslav
 */

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = Customer::class, name = "customer"),
        JsonSubTypes.Type(value = Account::class, name = "account"),
        JsonSubTypes.Type(value = MoneyTransaction::class, name = "transfer")
)
interface Entity {
    val id: Long
    val timestamp: Long
}

const val UNDEFINED_ID = -1L