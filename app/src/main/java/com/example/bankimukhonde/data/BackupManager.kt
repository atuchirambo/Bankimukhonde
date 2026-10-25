package com.example.bankimukhonde.data

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.example.bankimukhonde.model.Loan
import com.example.bankimukhonde.model.Member
import com.example.bankimukhonde.model.Repayment
import com.example.bankimukhonde.model.SavingsEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Backup, restore and CSV export. All file work runs off the main thread. */
object BackupManager {

    private const val DB_NAME = "bankimukhonde.db"
    // Must match the version in @Database. Change it when the schema version changes.
    private const val DB_VERSION = 1

    /** Copies the whole database file to the file the user chose. */
    suspend fun backup(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            // Flush anything still waiting in the write-ahead log into the main file
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }

            val dbFile = context.getDatabasePath(DB_NAME)
            val out = context.contentResolver.openOutputStream(uri) ?: return@withContext false
            out.use { output -> dbFile.inputStream().use { it.copyTo(output) } }
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Checks the chosen file really is a BANKIMUKHONDE backup, then replaces the database. */
    suspend fun restore(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val temp = File(context.cacheDir, "restore_temp.db")
        try {
            val input = context.contentResolver.openInputStream(uri) ?: return@withContext false
            input.use { source -> temp.outputStream().use { source.copyTo(it) } }

            if (!isValidBackup(temp)) {
                temp.delete()
                return@withContext false
            }

            AppDatabase.closeInstance()
            val target = context.getDatabasePath(DB_NAME)
            File(target.path + "-wal").delete()
            File(target.path + "-shm").delete()
            temp.copyTo(target, overwrite = true)
            temp.delete()
            true
        } catch (e: Exception) {
            temp.delete()
            false
        }
    }

    private fun isValidBackup(file: File): Boolean = try {
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            val tables = mutableSetOf<String>()
            db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'", null).use { c ->
                while (c.moveToNext()) tables.add(c.getString(0))
            }
            val required = listOf("Member", "SavingsEntry", "Loan", "Repayment", "AdminAccount")
            tables.containsAll(required) && db.version == DB_VERSION
        }
    } catch (e: Exception) {
        false
    }

    /** Restarts the app so everything reopens on the restored database. */
    fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    suspend fun writeText(context: Context, uri: Uri, text: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val out = context.contentResolver.openOutputStream(uri) ?: return@withContext false
            out.use { it.write(text.toByteArray(Charsets.UTF_8)) }
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Every transaction in one table, plus the group totals. Opens cleanly in Excel. */
    fun buildCsv(
        members: List<Member>,
        savings: List<SavingsEntry>,
        loans: List<Loan>,
        repayments: List<Repayment>
    ): String {
        fun esc(s: String) = "\"" + s.replace("\"", "\"\"") + "\""
        val names = members.associate { it.id to it.name }
        val loanOwner = loans.associate { it.id to it.memberId }

        val sb = StringBuilder("\uFEFF")
        sb.append("Type,Member,Date,Amount (MK),Details\n")
        savings.forEach {
            sb.append("Savings,${esc(names[it.memberId] ?: "Unknown")},${it.date},${it.amount},\n")
        }
        loans.forEach {
            val details = "Interest ${it.interestRatePercent}%, balance ${it.outstandingBalance}"
            sb.append("Loan,${esc(names[it.memberId] ?: "Unknown")},${it.dateIssued},${it.amount},${esc(details)}\n")
        }
        repayments.forEach {
            val owner = names[loanOwner[it.loanId]] ?: "Unknown"
            sb.append("Repayment,${esc(owner)},${it.date},${it.amount},\n")
        }
        sb.append("\n")
        sb.append("Total savings,,,${savings.sumOf { it.amount }},\n")
        sb.append("Total loans issued,,,${loans.sumOf { it.amount }},\n")
        sb.append("Total repayments,,,${repayments.sumOf { it.amount }},\n")
        return sb.toString()
    }
}