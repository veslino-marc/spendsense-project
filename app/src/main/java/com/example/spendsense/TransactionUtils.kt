package com.example.spendsense

import android.content.Context
import org.json.JSONArray

object TransactionUtils {

    data class Transaction(
        val category: String,
        val amount: Double,
        val timestampMs: Long
    )

    fun readTransactions(context: Context): List<Transaction> {
        val prefs = context.getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val raw = prefs.getString("items", "[]") ?: "[]"

        val result = mutableListOf<Transaction>()

        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val category = obj.optString("category", "").trim()
                val amount = obj.optDouble("amount", 0.0)
                val ts = obj.optLong("timestampMs", 0L)

                if (category.isNotEmpty() && ts > 0L) {
                    result.add(Transaction(category = category, amount = amount, timestampMs = ts))
                }
            }
        } catch (_: Exception) {
            return emptyList()
        }

        return result.sortedByDescending { it.timestampMs }
    }
}
