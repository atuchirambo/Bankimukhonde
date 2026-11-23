package com.example.bankimukhonde.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Member(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val dateJoined: String,
    val groupId: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val dirty: Boolean = true
)

@Entity
data class SavingsEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val amount: Double,
    val date: String,
    val groupId: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val dirty: Boolean = true
)

@Entity
data class Loan(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val amount: Double,
    val interestRatePercent: Double,
    val dateIssued: String,
    @ColumnInfo(defaultValue = "0") val termMonths: Int = 0,
    val groupId: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val dirty: Boolean = true
)

/** Amount plus interest: what the member has to pay back in total. */
fun Loan.totalOwed(): Double = amount + (amount * interestRatePercent / 100)

/** Monthly instalment, or null if no term was set. */
fun Loan.monthlyInstalment(): Double? = if (termMonths > 0) totalOwed() / termMonths else null

/** Computed outstanding balance derived from total owed minus non-deleted repayments. */
fun Loan.outstandingBalance(repayments: List<Repayment>): Double {
    val totalRepaid = repayments.filter { it.loanId == id && !it.isDeleted }.sumOf { it.amount }
    return (totalOwed() - totalRepaid).coerceAtLeast(0.0)
}

@Entity
data class Repayment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val loanId: String,
    val amount: Double,
    val date: String,
    val groupId: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val dirty: Boolean = true
)

/**
 * The single administrator login for this device.
 */
@Entity
data class AdminAccount(
    @PrimaryKey val id: Int = 0,
    val username: String,
    val password: String,
    val email: String = "",
    val role: String = "Chairperson",
    val groupName: String = "",
    val inviteCode: String = "",
    val groupId: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val dirty: Boolean = true
)
