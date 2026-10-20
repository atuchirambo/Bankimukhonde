package com.example.bankimukhonde.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Member(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val dateJoined: String
)

@Entity
data class SavingsEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val amount: Double,
    val date: String
)

@Entity
data class Loan(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val amount: Double,
    val interestRatePercent: Double,
    val dateIssued: String,
    val outstandingBalance: Double = amount + (amount * interestRatePercent / 100)
)

@Entity
data class Repayment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val loanId: String,
    val amount: Double,
    val date: String
)

/**
 * The single administrator login for this device. There is only ever
 * one row, so the id is always 0.
 */
@Entity
data class AdminAccount(
    @PrimaryKey val id: Int = 0,
    val username: String,
    val password: String
)