package com.example.bankimukhonde

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bankimukhonde.data.AdminDao
import com.example.bankimukhonde.data.AppDatabase
import com.example.bankimukhonde.data.LoanDao
import com.example.bankimukhonde.data.MemberDao
import com.example.bankimukhonde.data.RepaymentDao
import com.example.bankimukhonde.data.SavingsDao
import com.example.bankimukhonde.model.AdminAccount
import com.example.bankimukhonde.model.Loan
import com.example.bankimukhonde.model.Member
import com.example.bankimukhonde.model.Repayment
import com.example.bankimukhonde.model.SavingsEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var memberDao: MemberDao
    private lateinit var savingsDao: SavingsDao
    private lateinit var loanDao: LoanDao
    private lateinit var repaymentDao: RepaymentDao
    private lateinit var adminDao: AdminDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        memberDao = db.memberDao()
        savingsDao = db.savingsDao()
        loanDao = db.loanDao()
        repaymentDao = db.repaymentDao()
        adminDao = db.adminDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetAllMembers() = runBlocking {
        val member1 = Member(name = "Grace Banda", phone = "0999123456", dateJoined = "2026-01-10")
        val member2 = Member(name = "Alice Phiri", phone = "0888654321", dateJoined = "2026-01-12")

        memberDao.insert(member1)
        memberDao.insert(member2)

        val members = memberDao.getAll().first()
        assertEquals(2, members.size)
        // Should be ordered alphabetically by name
        assertEquals("Alice Phiri", members[0].name)
        assertEquals("Grace Banda", members[1].name)
    }

    @Test
    fun updateAndDeleteMember() = runBlocking {
        val member = Member(name = "John Tembo", phone = "0999000111", dateJoined = "2026-01-01")
        memberDao.insert(member)

        val retrieved = memberDao.getAll().first().first()
        val updated = retrieved.copy(phone = "0888000111")
        memberDao.update(updated)

        val afterUpdate = memberDao.getAll().first().first()
        assertEquals("0888000111", afterUpdate.phone)

        memberDao.delete(afterUpdate)
        val emptyList = memberDao.getAll().first()
        assertEquals(0, emptyList.size)
    }

    @Test
    fun insertAndRetrieveSavings() = runBlocking {
        val member = Member(name = "Chisomo", phone = "0999111222", dateJoined = "2026-01-01")
        memberDao.insert(member)

        val savings = SavingsEntry(memberId = member.id, amount = 15000.0, date = "2026-02-01")
        savingsDao.insert(savings)

        val list = savingsDao.getAll().first()
        assertEquals(1, list.size)
        assertEquals(15000.0, list[0].amount, 0.001)
        assertEquals(member.id, list[0].memberId)
    }

    @Test
    fun insertAndUpdateLoanBalance() = runBlocking {
        val member = Member(name = "Mwayi", phone = "0999333444", dateJoined = "2026-01-01")
        memberDao.insert(member)

        val loan = Loan(
            memberId = member.id,
            amount = 50000.0,
            interestRatePercent = 10.0,
            dateIssued = "2026-02-01",
        )
        loanDao.insert(loan)

        val issuedLoan = loanDao.getAll().first().first()
        assertEquals(55000.0, issuedLoan.outstandingBalance, 0.001)

        val repaymentAmount = 20000.0
        val updatedLoan = issuedLoan.copy(outstandingBalance = issuedLoan.outstandingBalance - repaymentAmount)
        loanDao.update(updatedLoan)

        val afterRepayment = loanDao.getAll().first().first()
        assertEquals(35000.0, afterRepayment.outstandingBalance, 0.001)
    }

    @Test
    fun insertRepayment() = runBlocking {
        val repayment = Repayment(loanId = "loan_1", amount = 10000.0, date = "2026-02-15")
        repaymentDao.insert(repayment)

        val repayments = repaymentDao.getAll().first()
        assertEquals(1, repayments.size)
        assertEquals(10000.0, repayments[0].amount, 0.001)
    }

    @Test
    fun adminDao_saveAndRetrieveAccount() = runBlocking {
        assertNull(adminDao.getAccount())

        val admin = AdminAccount(username = "admin", password = "password123")
        adminDao.save(admin)

        val saved = adminDao.getAccount()
        assertNotNull(saved)
        assertEquals("admin", saved?.username)
        assertEquals("password123", saved?.password)

        // Overwrite admin account with new credentials
        val newAdmin = AdminAccount(username = "chairperson", password = "newpassword")
        adminDao.save(newAdmin)

        val updated = adminDao.getAccount()
        assertEquals("chairperson", updated?.username)
        assertEquals("newpassword", updated?.password)
    }
}