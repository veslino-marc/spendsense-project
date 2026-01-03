package com.example.spendsense.budgetplan.data

/**
 * Data class representing a monthly budget plan
 */
data class BudgetPlan(
    val id: String = "",
    val userId: String = "",
    val totalBudget: Double = 0.0,
    val schedule: String = "", // e.g., "Weekly", "Monthly", "Bi-weekly"
    val budgetBreakdown: BudgetBreakdown = BudgetBreakdown(),
    val createdDate: Long = System.currentTimeMillis()
)

/**
 * Data class representing budget breakdown into categories
 */
data class BudgetBreakdown(
    val needs: Double = 0.0,
    val savings: Double = 0.0,
    val wants: Double = 0.0
) {
    fun getTotal(): Double = needs + savings + wants
}

/**
 * Data class for budget category allocation
 */
data class BudgetCategory(
    val name: String = "",
    val amount: Double = 0.0,
    val percentage: Double = 0.0
)

