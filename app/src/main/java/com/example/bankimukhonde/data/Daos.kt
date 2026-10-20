package com.example.bankimukhonde.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bankimukhonde.model.AdminAccount
import com.example.bankimukhonde.model.Loan
import com.example.bankimukhonde.model.Member
import com.example.bankimukhonde.model.Repayment
import com.example.bankimukhonde.model.SavingsEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Insert
    suspend fun insert(member: Member)

    @Update
    suspend fun update(member: Member)

    @Delete
    suspend fun delete(member: Member)

    @Query("SELECT * FROM Member ORDER BY name ASC")
    fun getAll(): Flow<List<Member>>
}

@Dao
interface SavingsDao {
    @Insert
    suspend fun insert(entry: SavingsEntry)

    @Update
    suspend fun update(entry: SavingsEntry)

    @Delete
    suspend fun delete(entry: SavingsEntry)

    @Query("SELECT * FROM SavingsEntry ORDER BY date DESC")
    fun getAll(): Flow<List<SavingsEntry>>
}

@Dao
interface LoanDao {
    @Insert
    suspend fun insert(loan: Loan)

    @Update
    suspend fun update(loan: Loan)

    @Delete
    suspend fun delete(loan: Loan)

    @Query("SELECT * FROM Loan ORDER BY dateIssued DESC")
    fun getAll(): Flow<List<Loan>>
}

@Dao
interface RepaymentDao {
    @Insert
    suspend fun insert(repayment: Repayment)

    @Delete
    suspend fun delete(repayment: Repayment)

    @Query("SELECT * FROM Repayment ORDER BY date DESC")
    fun getAll(): Flow<List<Repayment>>
}

@Dao
interface AdminDao {
    // REPLACE means: if an account already exists (id 0), overwrite it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(account: AdminAccount)

    // Returns null if no administrator has been set up yet.
    @Query("SELECT * FROM AdminAccount WHERE id = 0")
    suspend fun getAccount(): AdminAccount?
}