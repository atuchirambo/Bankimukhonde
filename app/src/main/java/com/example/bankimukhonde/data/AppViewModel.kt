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
    val adminUsername: String
        get() = adminAccount?.username ?: "Administrator"

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
        val account = AdminAccount(username = username.trim(), password = PasswordHasher.hash(password.trim()))
        adminAccount = account            // set immediately so login works right away
        viewModelScope.launch { adminDao.save(account) }
    }

    fun checkLogin(username: String, password: String): Boolean {
        val account = adminAccount ?: return false
        if (!username.trim().equals(account.username, ignoreCase = true)) return false
        if (!PasswordHasher.verify(password.trim(), account.password)) return false

        // Upgrade an old plain-text password to a hash on the first successful login
        if (!PasswordHasher.isHashed(account.password)) {
            val upgraded = account.copy(password = PasswordHasher.hash(password.trim()))
            adminAccount = upgraded
            viewModelScope.launch { adminDao.save(upgraded) }
        }
        return true
    }

    fun changePassword(currentPassword: String, newPassword: String): Boolean {
        val account = adminAccount ?: return false
        if (!PasswordHasher.verify(currentPassword.trim(), account.password)) return false

        val updated = account.copy(password = PasswordHasher.hash(newPassword.trim()))
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

    fun issueLoan(memberId: String, amount: Double, interestRatePercent: Double, date: String, termMonths: Int = 0) {
        viewModelScope.launch {
            loanDao.insert(
                Loan(
                    memberId = memberId,
                    amount = amount,
                    interestRatePercent = interestRatePercent,
                    dateIssued = date,
                    termMonths = termMonths
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
    // ---------- Edit and delete (FR9) ----------

    fun updateMember(member: Member, name: String, phone: String, dateJoined: String) {
        val updated = member.copy(name = name, phone = phone, dateJoined = dateJoined)
        if (selectedMember?.id == member.id) selectedMember = updated
        viewModelScope.launch { memberDao.update(updated) }
    }

    /** Deletes the member and everything that belongs to them. */
    fun deleteMember(member: Member) {
        selectedMember = null
        viewModelScope.launch {
            loans.filter { it.memberId == member.id }.forEach { loan ->
                repayments.filter { it.loanId == loan.id }.forEach { repaymentDao.delete(it) }
                loanDao.delete(loan)
            }
            savingsEntries.filter { it.memberId == member.id }.forEach { savingsDao.delete(it) }
            memberDao.delete(member)
        }
    }

    fun updateSavings(entry: SavingsEntry, amount: Double, date: String) {
        viewModelScope.launch { savingsDao.update(entry.copy(amount = amount, date = date)) }
    }

    fun deleteSavings(entry: SavingsEntry) {
        viewModelScope.launch { savingsDao.delete(entry) }
    }

    /** Recalculates the balance: new amount + interest, minus what was already repaid. */
    fun updateLoan(loan: Loan, amount: Double, interestRatePercent: Double, date: String, termMonths: Int) {
        val alreadyRepaid = repaymentsFor(loan.id).sumOf { it.amount }
        val newTotal = amount + (amount * interestRatePercent / 100)
        viewModelScope.launch {
            loanDao.update(
                loan.copy(
                    amount = amount,
                    interestRatePercent = interestRatePercent,
                    dateIssued = date,
                    termMonths = termMonths,
                    outstandingBalance = (newTotal - alreadyRepaid).coerceAtLeast(0.0)
                )
            )
        }
    }

    /** Deletes the loan and all of its repayments. */
    fun deleteLoan(loan: Loan) {
        viewModelScope.launch {
            repayments.filter { it.loanId == loan.id }.forEach { repaymentDao.delete(it) }
            loanDao.delete(loan)
        }
    }

    /** Deletes a repayment and puts its amount back onto the loan balance. */
    fun deleteRepayment(repayment: Repayment) {
        viewModelScope.launch {
            val loan = loans.find { it.id == repayment.loanId }
            if (loan != null) {
                val totalOwed = loan.amount + (loan.amount * loan.interestRatePercent / 100)
                loanDao.update(
                    loan.copy(
                        outstandingBalance = (loan.outstandingBalance + repayment.amount).coerceAtMost(totalOwed)
                    )
                )
            }
            repaymentDao.delete(repayment)
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