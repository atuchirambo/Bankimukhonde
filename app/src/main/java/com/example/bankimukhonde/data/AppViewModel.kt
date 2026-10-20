package com.example.bankimukhonde.data

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankimukhonde.model.AdminAccount
import com.example.bankimukhonde.model.Loan
import com.example.bankimukhonde.model.Member
import com.example.bankimukhonde.model.Repayment
import com.example.bankimukhonde.model.SavingsEntry
import kotlinx.coroutines.launch

/**
 * Now backed by the Room database. AndroidViewModel (instead of ViewModel)
 * gives us the Application, which Room needs to open the database file.
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val memberDao = db.memberDao()
    private val savingsDao = db.savingsDao()
    private val loanDao = db.loanDao()
    private val repaymentDao = db.repaymentDao()
    private val adminDao = db.adminDao()

    // These look like plain lists to the screens, but they are refreshed
    // automatically from the database whenever its data changes.
    var members by mutableStateOf<List<Member>>(emptyList())
        private set
    var savingsEntries by mutableStateOf<List<SavingsEntry>>(emptyList())
        private set
    var loans by mutableStateOf<List<Loan>>(emptyList())
        private set
    var repayments by mutableStateOf<List<Repayment>>(emptyList())
        private set

    var selectedMember: Member? by mutableStateOf(null)

    // --- Administrator account (stored in the database) ---
    private var adminAccount: AdminAccount? by mutableStateOf(null)

    // False until we have finished checking the database for an admin account.
    var adminLoaded by mutableStateOf(false)
        private set

    val isAdminSetUp: Boolean
        get() = adminAccount != null

    init {
        // Each launch starts a live feed: when the table changes, the list updates.
        viewModelScope.launch { memberDao.getAll().collect { members = it } }
        viewModelScope.launch { savingsDao.getAll().collect { savingsEntries = it } }
        viewModelScope.launch { loanDao.getAll().collect { loans = it } }
        viewModelScope.launch { repaymentDao.getAll().collect { repayments = it } }

        // One-time check: has an administrator been set up on this device?
        viewModelScope.launch {
            adminAccount = adminDao.getAccount()
            adminLoaded = true
        }
    }

    fun setupAdmin(username: String, password: String) {
        viewModelScope.launch {
            val account = AdminAccount(username = username, password = password)
            adminDao.save(account)
            adminAccount = account
        }
    }

    fun checkLogin(username: String, password: String): Boolean {
        val account = adminAccount ?: return false
        return username.trim().equals(account.username, ignoreCase = true) &&
                password.trim() == account.password
    }

    fun changePassword(currentPassword: String, newPassword: String): Boolean {
        val account = adminAccount ?: return false
        if (currentPassword.trim() != account.password) return false

        val updated = account.copy(password = newPassword)
        adminAccount = updated
        viewModelScope.launch { adminDao.save(updated) }
        return true
    }

    fun addMember(name: String, phone: String, dateJoined: String) {
        viewModelScope.launch {
            memberDao.insert(Member(name = name, phone = phone, dateJoined = dateJoined))
        }
    }

    fun recordSavings(memberId: String, amount: Double, date: String) {
        viewModelScope.launch {
            savingsDao.insert(SavingsEntry(memberId = memberId, amount = amount, date = date))
        }
    }

    fun issueLoan(memberId: String, amount: Double, interestRatePercent: Double, date: String) {
        viewModelScope.launch {
            loanDao.insert(
                Loan(
                    memberId = memberId,
                    amount = amount,
                    interestRatePercent = interestRatePercent,
                    dateIssued = date
                )
            )
        }
    }

    fun recordRepayment(loanId: String, amount: Double, date: String) {
        viewModelScope.launch {
            val loan = loans.find { it.id == loanId } ?: return@launch
            loanDao.update(
                loan.copy(outstandingBalance = (loan.outstandingBalance - amount).coerceAtLeast(0.0))
            )
            repaymentDao.insert(Repayment(loanId = loanId, amount = amount, date = date))
        }
    }

    fun totalSavings(): Double = savingsEntries.sumOf { it.amount }
    fun totalLoansIssued(): Double = loans.sumOf { it.amount }
    fun totalRepayments(): Double = repayments.sumOf { it.amount }

    fun savingsFor(memberId: String): List<SavingsEntry> = savingsEntries.filter { it.memberId == memberId }
    fun loansFor(memberId: String): List<Loan> = loans.filter { it.memberId == memberId }
    fun repaymentsFor(loanId: String): List<Repayment> = repayments.filter { it.loanId == loanId }
    fun repaymentsForMember(memberId: String): List<Repayment> = loansFor(memberId).flatMap { repaymentsFor(it.id) }
}