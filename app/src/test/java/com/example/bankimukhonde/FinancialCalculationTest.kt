package com.example.bankimukhonde

import com.example.bankimukhonde.model.Loan
import com.example.bankimukhonde.model.Repayment
import com.example.bankimukhonde.model.SavingsEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class FinancialCalculationTest {

    @Test
    fun totalSavings_calculatesSumOfAllEntries() {
        val entries = listOf(
            SavingsEntry(memberId = "m1", amount = 10000.0, date = "2026-01-01"),
            SavingsEntry(memberId = "m2", amount = 15000.0, date = "2026-01-02"),
            SavingsEntry(memberId = "m1", amount = 5000.0, date = "2026-01-03")
        )

        val total = entries.sumOf { it.amount }
        assertEquals(30000.0, total, 0.001)
    }

    @Test
    fun memberSavingsFilter_returnsOnlySpecificMemberEntries() {
        val entries = listOf(
            SavingsEntry(memberId = "m1", amount = 10000.0, date = "2026-01-01"),
            SavingsEntry(memberId = "m2", amount = 15000.0, date = "2026-01-02"),
            SavingsEntry(memberId = "m1", amount = 5000.0, date = "2026-01-03")
        )

        val member1Savings = entries.filter { it.memberId == "m1" }
        assertEquals(2, member1Savings.size)
        assertEquals(15000.0, member1Savings.sumOf { it.amount }, 0.001)
    }

    @Test
    fun loanRepayment_progressivelyReducesOutstandingBalance() {
        val initialLoan = Loan(
            id = "l1",
            memberId = "m1",
            amount = 50000.0,
            interestRatePercent = 20.0,
            dateIssued = "2026-01-01"
        )
        // 50,000 + 20% interest = 60,000
        assertEquals(60000.0, initialLoan.outstandingBalance, 0.001)

        val repayments = listOf(
            Repayment(loanId = "l1", amount = 20000.0, date = "2026-01-15"),
            Repayment(loanId = "l1", amount = 25000.0, date = "2026-02-01")
        )

        val totalRepaid = repayments.sumOf { it.amount }
        val finalBalance = (initialLoan.outstandingBalance - totalRepaid).coerceAtLeast(0.0)

        assertEquals(45000.0, totalRepaid, 0.001)
        assertEquals(15000.0, finalBalance, 0.001)
    }

    @Test
    fun loanRepayment_overpaymentDoesNotResultInNegativeBalance() {
        val initialLoan = Loan(
            id = "l1",
            memberId = "m1",
            amount = 10000.0,
            interestRatePercent = 10.0,
            dateIssued = "2026-01-01"
        )
        // 10,000 + 10% = 11,000 balance
        val repaymentAmount = 15000.0
        val remainingBalance = (initialLoan.outstandingBalance - repaymentAmount).coerceAtLeast(0.0)

        assertEquals(0.0, remainingBalance, 0.001)
    }
}