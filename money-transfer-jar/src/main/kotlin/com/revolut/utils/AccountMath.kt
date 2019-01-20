package com.revolut.utils

import java.math.BigDecimal
import java.math.MathContext

/**
 *   Account math
 *
 *   Constants and functions for correct and precise calculations with money amounts
 */

object AccountMath {
    private const val defaultScale = 4
    private const val defaultDigits = 10
    private val mathContext: MathContext = MathContext(defaultDigits + defaultScale)

    val thousand = fromInt(1000)
    val hundred = fromInt(100)
    val one = fromInt(1)
    val zero = fromInt(0)

    val minusOne = fromInt(-1)
    val aLot = fromInt(1000000)

    private fun fromInt(amount: Int): BigDecimal {
        return BigDecimal(amount, mathContext).setScale(defaultScale)
    }

    fun withdraw(from: BigDecimal, amount: BigDecimal): BigDecimal {
        return from.setScale(defaultScale).subtract(amount, mathContext)
    }

    fun deposit(from: BigDecimal, amount: BigDecimal): BigDecimal {
        return from.setScale(defaultScale).add(amount, mathContext)
    }

    fun sum(amounts: List<BigDecimal>): BigDecimal {
        if (amounts.isEmpty()) {
            return zero
        }
        return amounts.fold(zero.setScale(defaultScale), BigDecimal::add)
    }
}