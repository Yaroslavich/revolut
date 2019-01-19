package com.revolut.utils

import java.math.BigDecimal
import java.math.MathContext

/**
 *   Account math
 *
 *   Constants and functions for correct and precise calculations with money amounts
 */

object AccountMath {
    private val mathContext: MathContext = MathContext(4)

    val thousand = BigDecimal(1000, mathContext)
    val hundred = BigDecimal(100, mathContext)
    val one = BigDecimal(1, mathContext)
    val zero = BigDecimal(0, mathContext)

    val minusOne = BigDecimal(-1, mathContext)
    val aLot = BigDecimal(1000000, mathContext)

    fun withdraw(from: BigDecimal, amount: BigDecimal): BigDecimal {
        return from.subtract(amount, mathContext)
    }

    fun deposit(from: BigDecimal, amount: BigDecimal): BigDecimal {
        return from.add(amount, mathContext)
    }

    fun sum(amounts: List<BigDecimal>): BigDecimal {
        if (amounts.isEmpty()) {
            return zero
        }
        return amounts.fold(zero, BigDecimal::add)
    }
}