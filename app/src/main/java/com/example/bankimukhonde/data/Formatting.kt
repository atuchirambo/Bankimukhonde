package com.example.bankimukhonde.data

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

/** Today's date as 2026-10-24 */
fun todayString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

/** True only for a real date written as YYYY-MM-DD. */
fun isValidDate(text: String): Boolean {
    if (!Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(text)) return false
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        format.isLenient = false
        format.parse(text) != null
    } catch (e: Exception) {
        false
    }
}
/**
 * Returns an error message, or null if the phone number is fine. Malawi rules:
 *  - blank is allowed
 *  - local format: 0 + 9 digits (10 digits in total), second digit not 0
 *  - international format: +265 + exactly 9 digits, first of them not 0
 * Spaces and dashes are ignored.
 */
fun phoneError(phone: String): String? {
    val cleaned = phone.trim().replace(" ", "").replace("-", "")
    if (cleaned.isEmpty()) return null

    return when {
        cleaned.startsWith("+265") -> {
            val rest = cleaned.substring(4)
            when {
                !rest.all { it.isDigit() } -> "Phone number can only contain digits after +265"
                rest.length != 9 -> "After +265 there must be exactly 9 digits (e.g. +265999123456)"
                rest.startsWith("0") -> "Do not put a 0 after +265"
                else -> null
            }
        }
        cleaned.startsWith("0") -> when {
            !cleaned.all { it.isDigit() } -> "Phone number can only contain digits"
            cleaned.length != 10 -> "A number starting with 0 must have exactly 10 digits (e.g. 0999123456)"
            cleaned[1] == '0' -> "The second digit cannot be 0"
            else -> null
        }
        else -> "Phone number must start with 0 or +265"
    }
}

private fun isWhole(amount: Double) = Math.abs(amount - Math.round(amount).toDouble()) < 0.005

/** For display: MK 15,000 or MK 1,250.50 */
fun money(amount: Double): String {
    val pattern = if (isWhole(amount)) "#,##0" else "#,##0.00"
    return "MK " + DecimalFormat(pattern, DecimalFormatSymbols(Locale.US)).format(amount)
}

/** Clean number with no separators, for edit fields and CSV: 15000 or 1250.50 */
fun plain(amount: Double): String =
    if (isWhole(amount)) Math.round(amount).toString() else String.format(Locale.US, "%.2f", amount)