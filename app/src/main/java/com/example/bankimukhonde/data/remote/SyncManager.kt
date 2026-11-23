package com.example.bankimukhonde.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.example.bankimukhonde.data.AppDatabase
import com.example.bankimukhonde.model.AdminAccount
import com.example.bankimukhonde.model.Loan
import com.example.bankimukhonde.model.Member
import com.example.bankimukhonde.model.Repayment
import com.example.bankimukhonde.model.SavingsEntry
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Date

class SyncManager(
    private val context: Context,
    private val db: AppDatabase = AppDatabase.getInstance(context),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
) {

    companion object {
        private const val PREFS_NAME = "sync_prefs"
        private const val KEY_LAST_PULLED = "last_pulled_timestamp"
        private const val BATCH_LIMIT = 500
    }

    suspend fun sync() {
        push()
        pull()
    }

    /**
     * Pushes local dirty changes to Firestore, parent tables before child tables:
     * 1. AdminAccount
     * 2. Member
     * 3. Loan (depends on Member)
     * 4. SavingsEntry (depends on Member)
     * 5. Repayment (depends on Loan)
     */
    suspend fun push() {
        pushAdminAccounts()
        pushMembers()
        pushLoans()
        pushSavings()
        pushRepayments()
    }

    private suspend fun pushAdminAccounts() {
        val dirtyItems = db.adminDao().getDirty()
        if (dirtyItems.isEmpty()) return

        dirtyItems.chunked(BATCH_LIMIT).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { item ->
                val docRef = firestore.collection("AdminAccount").document(item.id.toString())
                val data = mapOf(
                    "id" to item.id,
                    "username" to item.username,
                    "password" to item.password,
                    "email" to item.email,
                    "role" to item.role,
                    "groupName" to item.groupName,
                    "inviteCode" to item.inviteCode,
                    "groupId" to item.groupId,
                    "updatedAt" to item.updatedAt,
                    "isDeleted" to item.isDeleted,
                    "dirty" to false,
                    "serverUpdatedAt" to FieldValue.serverTimestamp()
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            chunk.forEach { item ->
                db.adminDao().upsert(item.copy(dirty = false))
            }
        }
    }

    private suspend fun pushMembers() {
        val dirtyItems = db.memberDao().getDirty()
        if (dirtyItems.isEmpty()) return

        dirtyItems.chunked(BATCH_LIMIT).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { item ->
                val docRef = firestore.collection("Member").document(item.id)
                val data = mapOf(
                    "id" to item.id,
                    "name" to item.name,
                    "phone" to item.phone,
                    "dateJoined" to item.dateJoined,
                    "groupId" to item.groupId,
                    "updatedAt" to item.updatedAt,
                    "isDeleted" to item.isDeleted,
                    "dirty" to false,
                    "serverUpdatedAt" to FieldValue.serverTimestamp()
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            chunk.forEach { item ->
                db.memberDao().upsert(item.copy(dirty = false))
            }
        }
    }

    private suspend fun pushLoans() {
        val dirtyItems = db.loanDao().getDirty()
        if (dirtyItems.isEmpty()) return

        dirtyItems.chunked(BATCH_LIMIT).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { item ->
                val docRef = firestore.collection("Loan").document(item.id)
                val data = mapOf(
                    "id" to item.id,
                    "memberId" to item.memberId,
                    "amount" to item.amount,
                    "interestRatePercent" to item.interestRatePercent,
                    "dateIssued" to item.dateIssued,
                    "termMonths" to item.termMonths,
                    "groupId" to item.groupId,
                    "updatedAt" to item.updatedAt,
                    "isDeleted" to item.isDeleted,
                    "dirty" to false,
                    "serverUpdatedAt" to FieldValue.serverTimestamp()
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            chunk.forEach { item ->
                db.loanDao().upsert(item.copy(dirty = false))
            }
        }
    }

    private suspend fun pushSavings() {
        val dirtyItems = db.savingsDao().getDirty()
        if (dirtyItems.isEmpty()) return

        dirtyItems.chunked(BATCH_LIMIT).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { item ->
                val docRef = firestore.collection("SavingsEntry").document(item.id)
                val data = mapOf(
                    "id" to item.id,
                    "memberId" to item.memberId,
                    "amount" to item.amount,
                    "date" to item.date,
                    "groupId" to item.groupId,
                    "updatedAt" to item.updatedAt,
                    "isDeleted" to item.isDeleted,
                    "dirty" to false,
                    "serverUpdatedAt" to FieldValue.serverTimestamp()
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            chunk.forEach { item ->
                db.savingsDao().upsert(item.copy(dirty = false))
            }
        }
    }

    private suspend fun pushRepayments() {
        val dirtyItems = db.repaymentDao().getDirty()
        if (dirtyItems.isEmpty()) return

        dirtyItems.chunked(BATCH_LIMIT).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { item ->
                val docRef = firestore.collection("Repayment").document(item.id)
                val data = mapOf(
                    "id" to item.id,
                    "loanId" to item.loanId,
                    "amount" to item.amount,
                    "date" to item.date,
                    "groupId" to item.groupId,
                    "updatedAt" to item.updatedAt,
                    "isDeleted" to item.isDeleted,
                    "dirty" to false,
                    "serverUpdatedAt" to FieldValue.serverTimestamp()
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            chunk.forEach { item ->
                db.repaymentDao().upsert(item.copy(dirty = false))
            }
        }
    }

    /**
     * Pulls remote changes updated after lastPulled from Firestore.
     * Upserts into Room with dirty = false if remote updatedAt is newer than local updatedAt.
     * Saves the new lastPulled timestamp to SharedPreferences upon completion.
     */
    suspend fun pull() {
        val lastPulledMillis = prefs.getLong(KEY_LAST_PULLED, 0L)
        val pullStartTime = Timestamp.now()

        pullAdminAccounts(lastPulledMillis)
        pullMembers(lastPulledMillis)
        pullLoans(lastPulledMillis)
        pullSavings(lastPulledMillis)
        pullRepayments(lastPulledMillis)

        prefs.edit().putLong(KEY_LAST_PULLED, pullStartTime.toDate().time).apply()
    }

    private suspend fun pullAdminAccounts(lastPulledMillis: Long) {
        val collection = firestore.collection("AdminAccount")
        val snapshot = if (lastPulledMillis > 0L) {
            collection.whereGreaterThan("serverUpdatedAt", Timestamp(Date(lastPulledMillis))).get().await()
        } else {
            collection.get().await()
        }

        for (doc in snapshot.documents) {
            val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
            val local = db.adminDao().getAccount()
            if (local == null || remoteUpdatedAt > local.updatedAt) {
                val remote = AdminAccount(
                    id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: 0,
                    username = doc.getString("username") ?: "",
                    password = doc.getString("password") ?: "",
                    email = doc.getString("email") ?: "",
                    role = doc.getString("role") ?: "Chairperson",
                    groupName = doc.getString("groupName") ?: "",
                    inviteCode = doc.getString("inviteCode") ?: "",
                    groupId = doc.getString("groupId") ?: "",
                    updatedAt = remoteUpdatedAt,
                    isDeleted = doc.getBoolean("isDeleted") ?: false,
                    dirty = false
                )
                db.adminDao().upsert(remote)
            }
        }
    }

    private suspend fun pullMembers(lastPulledMillis: Long) {
        val collection = firestore.collection("Member")
        val snapshot = if (lastPulledMillis > 0L) {
            collection.whereGreaterThan("serverUpdatedAt", Timestamp(Date(lastPulledMillis))).get().await()
        } else {
            collection.get().await()
        }

        for (doc in snapshot.documents) {
            val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
            val id = doc.getString("id") ?: doc.id
            val local = db.memberDao().getById(id)
            if (local == null || remoteUpdatedAt > local.updatedAt) {
                val remote = Member(
                    id = id,
                    name = doc.getString("name") ?: "",
                    phone = doc.getString("phone") ?: "",
                    dateJoined = doc.getString("dateJoined") ?: "",
                    groupId = doc.getString("groupId") ?: "",
                    updatedAt = remoteUpdatedAt,
                    isDeleted = doc.getBoolean("isDeleted") ?: false,
                    dirty = false
                )
                db.memberDao().upsert(remote)
            }
        }
    }

    private suspend fun pullLoans(lastPulledMillis: Long) {
        val collection = firestore.collection("Loan")
        val snapshot = if (lastPulledMillis > 0L) {
            collection.whereGreaterThan("serverUpdatedAt", Timestamp(Date(lastPulledMillis))).get().await()
        } else {
            collection.get().await()
        }

        for (doc in snapshot.documents) {
            val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
            val id = doc.getString("id") ?: doc.id
            val local = db.loanDao().getById(id)
            if (local == null || remoteUpdatedAt > local.updatedAt) {
                val remote = Loan(
                    id = id,
                    memberId = doc.getString("memberId") ?: "",
                    amount = doc.getDouble("amount") ?: 0.0,
                    interestRatePercent = doc.getDouble("interestRatePercent") ?: 0.0,
                    dateIssued = doc.getString("dateIssued") ?: "",
                    termMonths = doc.getLong("termMonths")?.toInt() ?: 0,
                    groupId = doc.getString("groupId") ?: "",
                    updatedAt = remoteUpdatedAt,
                    isDeleted = doc.getBoolean("isDeleted") ?: false,
                    dirty = false
                )
                db.loanDao().upsert(remote)
            }
        }
    }

    private suspend fun pullSavings(lastPulledMillis: Long) {
        val collection = firestore.collection("SavingsEntry")
        val snapshot = if (lastPulledMillis > 0L) {
            collection.whereGreaterThan("serverUpdatedAt", Timestamp(Date(lastPulledMillis))).get().await()
        } else {
            collection.get().await()
        }

        for (doc in snapshot.documents) {
            val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
            val id = doc.getString("id") ?: doc.id
            val local = db.savingsDao().getById(id)
            if (local == null || remoteUpdatedAt > local.updatedAt) {
                val remote = SavingsEntry(
                    id = id,
                    memberId = doc.getString("memberId") ?: "",
                    amount = doc.getDouble("amount") ?: 0.0,
                    date = doc.getString("date") ?: "",
                    groupId = doc.getString("groupId") ?: "",
                    updatedAt = remoteUpdatedAt,
                    isDeleted = doc.getBoolean("isDeleted") ?: false,
                    dirty = false
                )
                db.savingsDao().upsert(remote)
            }
        }
    }

    private suspend fun pullRepayments(lastPulledMillis: Long) {
        val collection = firestore.collection("Repayment")
        val snapshot = if (lastPulledMillis > 0L) {
            collection.whereGreaterThan("serverUpdatedAt", Timestamp(Date(lastPulledMillis))).get().await()
        } else {
            collection.get().await()
        }

        for (doc in snapshot.documents) {
            val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
            val id = doc.getString("id") ?: doc.id
            val local = db.repaymentDao().getById(id)
            if (local == null || remoteUpdatedAt > local.updatedAt) {
                val remote = Repayment(
                    id = id,
                    loanId = doc.getString("loanId") ?: "",
                    amount = doc.getDouble("amount") ?: 0.0,
                    date = doc.getString("date") ?: "",
                    groupId = doc.getString("groupId") ?: "",
                    updatedAt = remoteUpdatedAt,
                    isDeleted = doc.getBoolean("isDeleted") ?: false,
                    dirty = false
                )
                db.repaymentDao().upsert(remote)
            }
        }
    }
}
