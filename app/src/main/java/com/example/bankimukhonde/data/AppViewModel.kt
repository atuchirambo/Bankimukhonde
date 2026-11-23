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
import com.example.bankimukhonde.model.outstandingBalance
import com.example.bankimukhonde.data.remote.SyncWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import java.util.UUID

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val memberDao = db.memberDao()
    private val savingsDao = db.savingsDao()
    private val loanDao = db.loanDao()
    private val repaymentDao = db.repaymentDao()
    private val adminDao = db.adminDao()

    var members by mutableStateOf<List<Member>>(emptyList())
        private set
    var savingsEntries by mutableStateOf<List<SavingsEntry>>(emptyList())
        private set
    var loans by mutableStateOf<List<Loan>>(emptyList())
        private set
    var repayments by mutableStateOf<List<Repayment>>(emptyList())
        private set

    var selectedMember: Member? by mutableStateOf(null)

    var adminAccount: AdminAccount? by mutableStateOf(null)
        private set

    var adminLoaded by mutableStateOf(false)
        private set

    val isAdminSetUp: Boolean
        get() = adminAccount != null
    val adminUsername: String
        get() = adminAccount?.username ?: "Administrator"

    init {
        viewModelScope.launch { memberDao.getAll().collect { members = it } }
        viewModelScope.launch { savingsDao.getAll().collect { savingsEntries = it } }
        viewModelScope.launch { loanDao.getAll().collect { loans = it } }
        viewModelScope.launch { repaymentDao.getAll().collect { repayments = it } }

        viewModelScope.launch {
            adminAccount = adminDao.getAccount()
            adminLoaded = true
        }
    }

    private fun now(): Long = System.currentTimeMillis()

    fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    fun setupAdmin(username: String, password: String) {
        val account = AdminAccount(
            username = username.trim(),
            password = PasswordHasher.hash(password.trim()),
            dirty = true,
            updatedAt = now()
        )
        adminAccount = account
        viewModelScope.launch { adminDao.save(account) }
    }

    fun setupAdminGroup(
        username: String,
        email: String,
        password: String,
        role: String,
        groupName: String,
        inviteCode: String,
        isCreatingGroup: Boolean,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val cleanEmail = if (email.contains("@")) email.trim() else "${username.trim()}@bankimukhonde.app"
        val hashedPassword = PasswordHasher.hash(password.trim())
        val actualGroupId = if (isCreatingGroup) "group_${UUID.randomUUID().toString().take(8)}" else "group_${inviteCode.trim().uppercase()}"
        val actualInviteCode = if (isCreatingGroup && inviteCode.isBlank()) generateInviteCode() else inviteCode.trim().uppercase()

        val account = AdminAccount(
            username = username.trim(),
            password = hashedPassword,
            email = cleanEmail,
            role = role,
            groupName = groupName.trim(),
            inviteCode = actualInviteCode,
            groupId = actualGroupId,
            dirty = true,
            updatedAt = now()
        )

        adminAccount = account
        viewModelScope.launch {
            adminDao.save(account)
        }

        try {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(cleanEmail, password.trim())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = task.result?.user?.uid ?: ""
                        val groupData = mapOf(
                            "groupId" to actualGroupId,
                            "groupName" to groupName,
                            "inviteCode" to actualInviteCode,
                            "updatedAt" to now()
                        )
                        FirebaseFirestore.getInstance().collection("groups").document(actualGroupId)
                            .set(groupData, SetOptions.merge())

                        val userData = mapOf(
                            "uid" to uid,
                            "username" to username,
                            "email" to cleanEmail,
                            "role" to role,
                            "groupId" to actualGroupId,
                            "updatedAt" to now()
                        )
                        FirebaseFirestore.getInstance().collection("users").document(uid)
                            .set(userData, SetOptions.merge())

                        onResult(true, "Account created successfully!")
                    } else {
                        onResult(true, "Created locally for offline use.")
                    }
                }
        } catch (e: Exception) {
            onResult(true, "Created locally for offline use.")
        }
    }

    fun checkLogin(username: String, password: String): Boolean {
        val account = adminAccount ?: return false
        val entered = username.trim()
        if (!entered.equals(account.username, ignoreCase = true) && !entered.equals(account.email, ignoreCase = true)) {
            return false
        }

        val isLocalValid = PasswordHasher.verify(password.trim(), account.password)

        try {
            val emailToUse = if (account.email.isNotBlank()) account.email else "${account.username}@bankimukhonde.app"
            FirebaseAuth.getInstance().signInWithEmailAndPassword(emailToUse, password.trim())
        } catch (e: Exception) {
            // Offline fallback
        }

        return isLocalValid
    }

    fun changePassword(currentPassword: String, newPassword: String): Boolean {
        val account = adminAccount ?: return false
        if (!PasswordHasher.verify(currentPassword.trim(), account.password)) return false

        val updated = account.copy(
            password = PasswordHasher.hash(newPassword.trim()),
            dirty = true,
            updatedAt = now()
        )
        adminAccount = updated
        viewModelScope.launch { adminDao.save(updated) }

        try {
            val user = FirebaseAuth.getInstance().currentUser
            user?.updatePassword(newPassword.trim())
        } catch (e: Exception) {
            // Offline fallback
        }

        return true
    }

    private fun triggerSync() {
        try {
            SyncWorker.syncNow(getApplication())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addMember(name: String, phone: String, dateJoined: String) {
        viewModelScope.launch {
            memberDao.insert(
                Member(
                    name = name,
                    phone = phone,
                    dateJoined = dateJoined,
                    groupId = adminAccount?.groupId ?: "",
                    dirty = true,
                    updatedAt = now()
                )
            )
            triggerSync()
        }
    }

    fun recordSavings(memberId: String, amount: Double, date: String) {
        viewModelScope.launch {
            savingsDao.insert(
                SavingsEntry(
                    memberId = memberId,
                    amount = amount,
                    date = date,
                    groupId = adminAccount?.groupId ?: "",
                    dirty = true,
                    updatedAt = now()
                )
            )
            triggerSync()
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
                    termMonths = termMonths,
                    groupId = adminAccount?.groupId ?: "",
                    dirty = true,
                    updatedAt = now()
                )
            )
            triggerSync()
        }
    }

    fun recordRepayment(loanId: String, amount: Double, date: String) {
        viewModelScope.launch {
            repaymentDao.insert(
                Repayment(
                    loanId = loanId,
                    amount = amount,
                    date = date,
                    groupId = adminAccount?.groupId ?: "",
                    dirty = true,
                    updatedAt = now()
                )
            )
            triggerSync()
        }
    }

    fun updateMember(member: Member, name: String, phone: String, dateJoined: String) {
        val updated = member.copy(
            name = name,
            phone = phone,
            dateJoined = dateJoined,
            dirty = true,
            updatedAt = now()
        )
        if (selectedMember?.id == member.id) selectedMember = updated
        viewModelScope.launch {
            memberDao.update(updated)
            triggerSync()
        }
    }

    fun deleteMember(member: Member) {
        selectedMember = null
        val timestamp = now()
        viewModelScope.launch {
            loans.filter { it.memberId == member.id }.forEach { loan ->
                repayments.filter { it.loanId == loan.id }.forEach {
                    repaymentDao.update(it.copy(isDeleted = true, dirty = true, updatedAt = timestamp))
                }
                loanDao.update(loan.copy(isDeleted = true, dirty = true, updatedAt = timestamp))
            }
            savingsEntries.filter { it.memberId == member.id }.forEach {
                savingsDao.update(it.copy(isDeleted = true, dirty = true, updatedAt = timestamp))
            }
            memberDao.update(member.copy(isDeleted = true, dirty = true, updatedAt = timestamp))
            triggerSync()
        }
    }

    fun updateSavings(entry: SavingsEntry, amount: Double, date: String) {
        viewModelScope.launch {
            savingsDao.update(entry.copy(amount = amount, date = date, dirty = true, updatedAt = now()))
            triggerSync()
        }
    }

    fun deleteSavings(entry: SavingsEntry) {
        viewModelScope.launch {
            savingsDao.update(entry.copy(isDeleted = true, dirty = true, updatedAt = now()))
            triggerSync()
        }
    }

    fun updateLoan(loan: Loan, amount: Double, interestRatePercent: Double, date: String, termMonths: Int) {
        viewModelScope.launch {
            loanDao.update(
                loan.copy(
                    amount = amount,
                    interestRatePercent = interestRatePercent,
                    dateIssued = date,
                    termMonths = termMonths,
                    dirty = true,
                    updatedAt = now()
                )
            )
            triggerSync()
        }
    }

    fun deleteLoan(loan: Loan) {
        val timestamp = now()
        viewModelScope.launch {
            repayments.filter { it.loanId == loan.id }.forEach {
                repaymentDao.update(it.copy(isDeleted = true, dirty = true, updatedAt = timestamp))
            }
            loanDao.update(loan.copy(isDeleted = true, dirty = true, updatedAt = timestamp))
            triggerSync()
        }
    }

    fun deleteRepayment(repayment: Repayment) {
        viewModelScope.launch {
            repaymentDao.update(repayment.copy(isDeleted = true, dirty = true, updatedAt = now()))
            triggerSync()
        }
    }

    fun outstandingBalance(loan: Loan): Double = loan.outstandingBalance(repayments)

    fun totalSavings(): Double = savingsEntries.sumOf { it.amount }
    fun totalLoansIssued(): Double = loans.sumOf { it.amount }
    fun totalRepayments(): Double = repayments.sumOf { it.amount }

    fun savingsFor(memberId: String): List<SavingsEntry> = savingsEntries.filter { it.memberId == memberId }
    fun loansFor(memberId: String): List<Loan> = loans.filter { it.memberId == memberId }
    fun repaymentsFor(loanId: String): List<Repayment> = repayments.filter { it.loanId == loanId }
    fun repaymentsForMember(memberId: String): List<Repayment> = loansFor(memberId).flatMap { repaymentsFor(it.id) }
}
