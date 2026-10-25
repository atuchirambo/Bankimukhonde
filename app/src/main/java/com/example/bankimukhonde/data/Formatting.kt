package com.example.bankimukhonde.data

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private fun isWhole(amount: Double) = Math.abs(amount - Math.round(amount).toDouble()) < 0.005

/** For display: MK 15,000 or MK 1,250.50 */
fun money(amount: Double): String {
    val pattern = if (isWhole(amount)) "#,##0" else "#,##0.00"
    return "MK " + DecimalFormat(pattern, DecimalFormatSymbols(Locale.US)).format(amount)
}

/** Clean number with no separators, for edit fields and CSV: 15000 or 1250.50 */
fun plain(amount: Double): String =
    if (isWhole(amount)) Math.round(amount).toString() else String.format(Locale.US, "%.2f", amount)