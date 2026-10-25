package com.example.bankimukhonde

import com.example.bankimukhonde.model.AdminAccount
import com.example.bankimukhonde.model.Loan
import com.example.bankimukhonde.model.Member
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import com.example.bankimukhonde.model.monthlyInstalment
import com.example.bankimukhonde.model.totalOwed
import org.junit.Assert.assertNull

class ModelTest {



    @Test
    fun loan_startingBalance_includesInterest() {
        val loan = Loan(memberId = "m1", amount = 1000.0, interestRatePercent = 10.0, dateIssued = "2026-10-01")
        assertEquals(1100.0, loan.outstandingBalance, 0.001)
    }

    @Test
    fun loan_withZeroInterest_balanceEqualsAmount() {
        val loan = Loan(memberId = "m1", amount = 500.0, interestRatePercent = 0.0, dateIssued = "2026-10-01")
        assertEquals(500.0, loan.outstandingBalance, 0.001)
    }

    @Test
    fun loan_copy_changesOnlyTheBalance() {
        val loan = Loan(memberId = "m1", amount = 1000.0, interestRatePercent = 10.0, dateIssued = "2026-10-01")
        val afterRepayment = loan.copy(outstandingBalance = 600.0)

        assertEquals(600.0, afterRepayment.outstandingBalance, 0.001)
        assertEquals(loan.id, afterRepayment.id)
        assertEquals(loan.amount, afterRepayment.amount, 0.001)
        assertEquals(loan.memberId, afterRepayment.memberId)
    }

    @Test
    fun newMembers_getDifferentIds() {
        val a = Member(name = "Mary", phone = "0888000001", dateJoined = "2026-01-01")
        val b = Member(name = "Jane", phone = "0888000002", dateJoined = "2026-01-02")
        assertNotEquals(a.id, b.id)
    }

    @Test
    fun adminAccount_usesIdZero() {
        val account = AdminAccount(username = "chair", password = "secret")
        assertEquals(0, account.id)
    }
}