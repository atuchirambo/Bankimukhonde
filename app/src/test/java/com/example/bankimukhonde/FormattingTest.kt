package com.example.bankimukhonde

import com.example.bankimukhonde.data.money
import com.example.bankimukhonde.data.plain
import org.junit.Assert.assertEquals
import org.junit.Test

class FormattingTest {

    @Test
    fun money_wholeNumber_hasSeparatorsAndNoDecimals() {
        assertEquals("MK 15,000", money(15000.0))
        assertEquals("MK 1,250,000", money(1250000.0))
    }

    @Test
    fun money_fraction_showsTwoDecimals() {
        assertEquals("MK 1,250.50", money(1250.5))
    }

    @Test
    fun money_zero() {
        assertEquals("MK 0", money(0.0))
    }

    @Test
    fun plain_hasNoSeparators() {
        assertEquals("15000", plain(15000.0))
        assertEquals("1250.50", plain(1250.5))
    }
}