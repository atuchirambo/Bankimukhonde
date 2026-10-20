package com.example.bankimukhonde

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bankimukhonde.data.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppViewModelTest {

    private lateinit var viewModel: AppViewModel

    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        viewModel = AppViewModel(application)
    }

    @Test
    fun testAdminWorkflow() = runBlocking {
        // Wait for initial admin check
        while (!viewModel.adminLoaded) {
            delay(50)
        }

        // Setup new admin
        viewModel.setupAdmin("admin_user", "pass123")
        delay(200)

        assertTrue(viewModel.isAdminSetUp)
        assertTrue(viewModel.checkLogin("admin_user", "pass123"))
        assertFalse(viewModel.checkLogin("admin_user", "wrong_pass"))

        // Change password
        val changed = viewModel.changePassword("pass123", "new_pass456")
        assertTrue(changed)
        assertTrue(viewModel.checkLogin("admin_user", "new_pass456"))
        assertFalse(viewModel.checkLogin("admin_user", "pass123"))
    }

    @Test
    fun testMemberAndSavingsFlow() = runBlocking {
        while (!viewModel.adminLoaded) {
            delay(50)
        }

        val initialSavings = viewModel.totalSavings()

        viewModel.addMember("Blessings Phiri", "0888123456", "2026-01-01")
        delay(200)

        val member = viewModel.members.find { it.name == "Blessings Phiri" }
        assertNotNull(member)

        member?.let { m ->
            viewModel.recordSavings(m.id, 25000.0, "2026-01-15")
            delay(200)

            val memberSavings = viewModel.savingsFor(m.id)
            assertEquals(1, memberSavings.size)
            assertEquals(25000.0, memberSavings[0].amount, 0.001)

            assertEquals(initialSavings + 25000.0, viewModel.totalSavings(), 0.001)
        }
    }

    @Test
    fun testLoanAndRepaymentFlow() = runBlocking {
        while (!viewModel.adminLoaded) {
            delay(50)
        }

        viewModel.addMember("Kondwani", "0999888777", "2026-01-01")
        delay(200)

        val member = viewModel.members.find { it.name == "Kondwani" }
        assertNotNull(member)

        member?.let { m ->
            // Issue loan of 100,000 at 10% interest -> 110,000 balance
            viewModel.issueLoan(m.id, 100000.0, 10.0, "2026-02-01")
            delay(200)

            val memberLoans = viewModel.loansFor(m.id)
            assertEquals(1, memberLoans.size)
            val loan = memberLoans[0]
            assertEquals(110000.0, loan.outstandingBalance, 0.001)

            // Record repayment of 40,000
            viewModel.recordRepayment(loan.id, 40000.0, "2026-02-10")
            delay(200)

            val updatedLoan = viewModel.loansFor(m.id)[0]
            assertEquals(70000.0, updatedLoan.outstandingBalance, 0.001)

            val repayments = viewModel.repaymentsFor(loan.id)
            assertEquals(1, repayments.size)
            assertEquals(40000.0, repayments[0].amount, 0.001)
        }
    }
}