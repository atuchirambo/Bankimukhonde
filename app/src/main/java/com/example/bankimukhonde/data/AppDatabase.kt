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

@Database(
    entities = [
        Member::class,
        SavingsEntry::class,
        Loan::class,
        Repayment::class,
        AdminAccount::class
    ],
    version = 1,
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
                    // instead of crashing. Remove this before real users depend on the data.
                    .fallbackToDestructiveMigration()
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