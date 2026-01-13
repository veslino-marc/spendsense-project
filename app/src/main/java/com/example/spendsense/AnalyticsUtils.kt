package com.example.spendsense

import android.content.Context

object AnalyticsUtils {

    data class AnalyticsResult(
        val budgetUsed: Double,
        val budgetTotal: Double,
        val budgetPercentUsed: Int,
        val needsPercent: Double,
        val savingsPercent: Double,
        val wantsPercent: Double,
        val needsValue: Float,
        val savingsValue: Float,
        val wantsValue: Float,
        val hasPlan: Boolean
    )

    fun compute(context: Context): AnalyticsResult {
        val prefs = context.getSharedPreferences("budget_plans", Context.MODE_PRIVATE)

        val totalBudget = prefs.getString("total_budget", null)?.toDoubleOrNull()
        val needs = prefs.getString("needs", null)?.toDoubleOrNull()
        val savings = prefs.getString("savings", null)?.toDoubleOrNull()
        val wants = prefs.getString("wants", null)?.toDoubleOrNull()

        val hasPlan = totalBudget != null && needs != null && savings != null && wants != null && totalBudget > 0

        val used = 0.0

        if (!hasPlan) {
            return AnalyticsResult(
                budgetUsed = 0.0,
                budgetTotal = 0.0,
                budgetPercentUsed = 0,
                needsPercent = 0.0,
                savingsPercent = 0.0,
                wantsPercent = 0.0,
                needsValue = 0f,
                savingsValue = 0f,
                wantsValue = 0f,
                hasPlan = false
            )
        }

        val percentUsed = ((used / totalBudget!!) * 100).toInt().coerceIn(0, 100)

        val needsPct = (needs!! / totalBudget) * 100.0
        val savingsPct = (savings!! / totalBudget) * 100.0
        val wantsPct = (wants!! / totalBudget) * 100.0

        return AnalyticsResult(
            budgetUsed = used,
            budgetTotal = totalBudget,
            budgetPercentUsed = percentUsed,
            needsPercent = needsPct,
            savingsPercent = savingsPct,
            wantsPercent = wantsPct,
            needsValue = needs.toFloat(),
            savingsValue = savings.toFloat(),
            wantsValue = wants.toFloat(),
            hasPlan = true
        )
    }
}
