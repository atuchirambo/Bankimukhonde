package com.example.bankimukhonde.data

import java.security.MessageDigest
import java.security.SecureRandom

/** Stores passwords as "salt:hash" (both hex) so the real password is never saved. */
object PasswordHasher {

    private val hashedFormat = Regex("^[0-9a-f]{32}:[0-9a-f]{64}$")

    private fun toHex(bytes: ByteArray) = bytes.joinToString("") { "%02x".format(it) }

    private fun fromHex(s: String) =
        ByteArray(s.length / 2) { s.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    private fun digest(salt: ByteArray, password: String): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        return md.digest(password.toByteArray(Charsets.UTF_8))
    }

    fun hash(password: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return toHex(salt) + ":" + toHex(digest(salt, password))
    }

    fun isHashed(stored: String) = hashedFormat.matches(stored)

    fun verify(password: String, stored: String): Boolean {
        // Old accounts saved before this change are still plain text
        if (!isHashed(stored)) return password == stored
        val (saltHex, hashHex) = stored.split(":")
        return MessageDigest.isEqual(digest(fromHex(saltHex), password), fromHex(hashHex))
    }
}