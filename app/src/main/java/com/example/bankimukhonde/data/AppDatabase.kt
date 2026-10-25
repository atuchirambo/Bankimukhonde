package com.example.bankimukhonde.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bankimukhonde.model.AdminAccount
import com.example.bankimukhonde.model.Loan
import com.example.bankimukhonde.model.Member
import com.example.bankimukhonde.model.Repayment
import com.example.bankimukhonde.model.SavingsEntry
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Member::class,
        SavingsEntry::class,
        Loan::class,
        Repayment::class,
        AdminAccount::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun memberDao(): MemberDao
    abstract fun savingsDao(): SavingsDao
    abstract fun loanDao(): LoanDao
    abstract fun repaymentDao(): RepaymentDao
    abstract fun adminDao(): AdminDao

    companion object {
        // @Volatile makes sure every part of the app sees the latest value of INSTANCE.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Version 1 -> 2: loans get a repayment term (in months). Existing loans get 0. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Loan ADD COLUMN termMonths INTEGER NOT NULL DEFAULT 0")
            }
        }
        /**
         * Returns the one shared database for the whole app, creating it the
         * first time it is asked for. Opening the database is expensive, so
         * we only ever want one copy.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bankimukhonde.db"
                )
                    // While developing, if the table design changes, wipe and rebuild
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
        /** Closes the database and forgets it. Used before a restore replaces the file. */
        fun closeInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}