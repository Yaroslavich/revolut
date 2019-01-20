package com.revolut.utils

import com.revolut.utils.AccountMath.hundred
import com.revolut.utils.AccountMath.minusOne
import com.revolut.utils.AccountMath.one
import com.revolut.utils.AccountMath.thousand
import com.revolut.utils.AccountMath.zero
import org.junit.Test
import java.math.BigDecimal
import kotlin.test.assertEquals

/**
 *   Test coverage for AccountMath
 *
 *   Created by yaroslav
 */

class AccountMathUtilsTest {

    /**
     *   deposit
     */
    @Test
    fun depositTest() {
        assertEquals(zero, AccountMath.deposit(zero, zero))
        assertEquals(AccountMath.deposit(thousand, hundred), AccountMath.deposit(hundred, thousand))
        assertEquals(zero, AccountMath.deposit(one, minusOne))

        val ten = thousand.div(hundred)
        val oneTenth = hundred.div(thousand)

        assertEquals(ten.add(oneTenth), AccountMath.deposit(ten, oneTenth))
    }

    /**
     *   withdraw
     */
    @Test
    fun withdrawTest() {
        assertEquals(zero, AccountMath.withdraw(zero, zero))
        assertEquals(one, AccountMath.withdraw(one, zero))
        assertEquals(zero, AccountMath.withdraw(one, one))

        val ten = thousand.div(hundred)
        val oneTenth = hundred.div(thousand)

        assertEquals(ten.subtract(oneTenth), AccountMath.withdraw(ten, oneTenth))
    }

    /**
     *   fractional part remains
     */
    @Test
    fun fractionalTest() {
        val o9999 = BigDecimal("0.9999")
        val oOOO1 = BigDecimal("0.0001")

        assertEquals(one, AccountMath.deposit(o9999, oOOO1))
        assertEquals(oOOO1, AccountMath.withdraw(one, o9999))
    }
}