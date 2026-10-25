package com.example.bankimukhonde

import com.example.bankimukhonde.data.isValidDate
import com.example.bankimukhonde.data.phoneError
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {

    @Test
    fun validDates_areAccepted() {
        assertTrue(isValidDate("2026-10-24"))
        assertTrue(isValidDate("2028-02-29"))
    }

    @Test
    fun invalidDates_areRejected() {
        assertFalse(isValidDate(""))
        assertFalse(isValidDate("24/10/2026"))
        assertFalse(isValidDate("2026-13-01"))
        assertFalse(isValidDate("2026-02-30"))
    }

    @Test
    fun validPhones_areAccepted() {
        assertNull(phoneError(""))
        assertNull(phoneError("0999123456"))
        assertNull(phoneError("088-865-4321"))
        assertNull(phoneError("+265 999 123 456"))
        assertNull(phoneError("+265888654321"))
    }

    @Test
    fun invalidPhones_areRejected() {
        assertNotNull(phoneError("0000000000"))        // second digit is 0
        assertNotNull(phoneError("0099123456"))        // second digit is 0
        assertNotNull(phoneError("099912345"))         // only 9 digits
        assertNotNull(phoneError("09991234567"))       // 11 digits
        assertNotNull(phoneError("09991abc56"))        // letters
        assertNotNull(phoneError("+265099123456"))     // 0 after +265
        assertNotNull(phoneError("+26599912345"))      // 8 digits after +265
        assertNotNull(phoneError("+2659991234567"))    // 10 digits after +265
        assertNotNull(phoneError("999123456"))         // no 0 or +265 at the start
        assertNotNull(phoneError("+"))
    }
}