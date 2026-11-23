package com.example.bankimukhonde.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun memberDao(): MemberDao
    abstract fun savingsDao(): SavingsDao
    abstract fun loanDao(): LoanDao
    abstract fun repaymentDao(): RepaymentDao
    abstract fun adminDao(): AdminDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Version 1 -> 2: loans get a repayment term (in months). Existing loans get 0. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Loan ADD COLUMN termMonths INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** Version 2 -> 3: Add offline sync/soft delete fields & remove stored outstandingBalance from Loan. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val tables = listOf("Member", "SavingsEntry", "Repayment", "AdminAccount")
                for (t in tables) {
                    db.execSQL("ALTER TABLE `$t` ADD COLUMN `groupId` TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE `$t` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE `$t` ADD COLUMN `isDeleted` INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE `$t` ADD COLUMN `dirty` INTEGER NOT NULL DEFAULT 1")
                }

                // Re-create Loan table without outstandingBalance and with new sync fields
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `Loan_new` (" +
                            "`id` TEXT NOT NULL, " +
                            "`memberId` TEXT NOT NULL, " +
                            "`amount` REAL NOT NULL, " +
                            "`interestRatePercent` REAL NOT NULL, " +
                            "`dateIssued` TEXT NOT NULL, " +
                            "`termMonths` INTEGER NOT NULL DEFAULT 0, " +
                            "`groupId` TEXT NOT NULL DEFAULT '', " +
                            "`updatedAt` INTEGER NOT NULL DEFAULT 0, " +
                            "`isDeleted` INTEGER NOT NULL DEFAULT 0, " +
                            "`dirty` INTEGER NOT NULL DEFAULT 1, " +
                            "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "INSERT INTO `Loan_new` (`id`, `memberId`, `amount`, `interestRatePercent`, `dateIssued`, `termMonths`) " +
                            "SELECT `id`, `memberId`, `amount`, `interestRatePercent`, `dateIssued`, `termMonths` FROM `Loan`"
                )
                db.execSQL("DROP TABLE `Loan` ")
                db.execSQL("ALTER TABLE `Loan_new` RENAME TO `Loan` ")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bankimukhonde.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        fun closeInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}
