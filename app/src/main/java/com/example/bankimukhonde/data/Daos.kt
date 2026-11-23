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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(member: Member)

    @Query("SELECT * FROM Member WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAll(): Flow<List<Member>>

    @Query("SELECT * FROM Member WHERE dirty = 1")
    suspend fun getDirty(): List<Member>

    @Query("SELECT * FROM Member WHERE id = :id")
    suspend fun getById(id: String): Member?
}

@Dao
interface SavingsDao {
    @Insert
    suspend fun insert(entry: SavingsEntry)

    @Update
    suspend fun update(entry: SavingsEntry)

    @Delete
    suspend fun delete(entry: SavingsEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: SavingsEntry)

    @Query("SELECT * FROM SavingsEntry WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAll(): Flow<List<SavingsEntry>>

    @Query("SELECT * FROM SavingsEntry WHERE dirty = 1")
    suspend fun getDirty(): List<SavingsEntry>

    @Query("SELECT * FROM SavingsEntry WHERE id = :id")
    suspend fun getById(id: String): SavingsEntry?
}

@Dao
interface LoanDao {
    @Insert
    suspend fun insert(loan: Loan)

    @Update
    suspend fun update(loan: Loan)

    @Delete
    suspend fun delete(loan: Loan)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(loan: Loan)

    @Query("SELECT * FROM Loan WHERE isDeleted = 0 ORDER BY dateIssued DESC")
    fun getAll(): Flow<List<Loan>>

    @Query("SELECT * FROM Loan WHERE dirty = 1")
    suspend fun getDirty(): List<Loan>

    @Query("SELECT * FROM Loan WHERE id = :id")
    suspend fun getById(id: String): Loan?
}

@Dao
interface RepaymentDao {
    @Insert
    suspend fun insert(repayment: Repayment)

    @Update
    suspend fun update(repayment: Repayment)

    @Delete
    suspend fun delete(repayment: Repayment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(repayment: Repayment)

    @Query("SELECT * FROM Repayment WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAll(): Flow<List<Repayment>>

    @Query("SELECT * FROM Repayment WHERE dirty = 1")
    suspend fun getDirty(): List<Repayment>

    @Query("SELECT * FROM Repayment WHERE id = :id")
    suspend fun getById(id: String): Repayment?
}

@Dao
interface AdminDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(account: AdminAccount)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(account: AdminAccount)

    @Query("SELECT * FROM AdminAccount WHERE id = 0")
    suspend fun getAccount(): AdminAccount?

    @Query("SELECT * FROM AdminAccount WHERE dirty = 1")
    suspend fun getDirty(): List<AdminAccount>
}
