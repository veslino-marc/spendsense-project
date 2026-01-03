# Quick Reference Guide - Budget Plan Feature

## File Structure
```
app/src/main/java/com/example/spendsense/
├── budgetplan/
│   ├── BudgetPlanStep1Activity.kt      (Schedule selection)
│   ├── BudgetPlanStep2Activity.kt      (Budget input)
│   ├── BudgetPlanStep3Activity.kt      (Breakdown allocation)
│   └── data/
│       └── BudgetPlan.kt               (Data models)
├── DashboardActivity.kt                (Updated with budget button)
└── ... other activities

app/src/main/res/layout/
├── activity_budget_plan_step1.xml      (Schedule UI)
├── activity_budget_plan_step2.xml      (Budget input UI)
├── activity_budget_plan_step3.xml      (Breakdown UI)
└── ... other layouts
```

## How It Works

### From Dashboard
1. User taps "Create a budget plan" button
2. Navigates to Step 1 (Schedule Selection)

### Step 1: Schedule Selection
```kotlin
// User selects: Weekly, Bi-weekly, Monthly, or Custom
// Then taps "Next" button
// Passes selectedSchedule to Step 2
```

### Step 2: Budget Input
```kotlin
// User enters total monthly budget (e.g., 12000)
// Input validation ensures positive number
// Passes budget amount and schedule to Step 3
```

### Step 3: Budget Breakdown
```kotlin
// User allocates budget into 3 categories:
// - Needs (essentials)
// - Savings (future security)
// - Wants (discretionary)
// 
// Total must equal the input budget
// Percentages calculated automatically
// Data saved to SharedPreferences on "Finish"
```

## Data Model

```kotlin
data class BudgetPlan(
    val id: String,                    // Unique ID
    val userId: String,                // User reference
    val totalBudget: Double,           // Total amount
    val schedule: String,              // Weekly/Bi-weekly/Monthly
    val budgetBreakdown: BudgetBreakdown
)

data class BudgetBreakdown(
    val needs: Double,                 // Essential expenses
    val savings: Double,               // Savings goal
    val wants: Double                  // Discretionary spending
)
```

## SharedPreferences Keys

```kotlin
// Budget data stored with these keys:
"total_budget"      // Double (stored as String)
"needs"             // Double (stored as String)
"savings"           // Double (stored as String)
"wants"             // Double (stored as String)
"schedule"          // String
"latest_budget_plan" // Full BudgetPlan object
```

## Retrieving Saved Budget Data

```kotlin
val sharedPref = context.getSharedPreferences("budget_plans", Context.MODE_PRIVATE)

val totalBudget = sharedPref.getString("total_budget", "0.0")?.toDouble() ?: 0.0
val needs = sharedPref.getString("needs", "0.0")?.toDouble() ?: 0.0
val savings = sharedPref.getString("savings", "0.0")?.toDouble() ?: 0.0
val wants = sharedPref.getString("wants", "0.0")?.toDouble() ?: 0.0
val schedule = sharedPref.getString("schedule", "")
```

## Customization Points

### Adding Custom Categories
In `BudgetPlanStep3Activity`, the `addMoreCategory()` method is ready for expansion:
```kotlin
private fun addMoreCategory() {
    // Add custom category logic here
    Toast.makeText(this, "Custom categories coming soon", Toast.LENGTH_SHORT).show()
}
```

### Changing Colors
Edit `app/src/main/res/values/colors.xml`:
```xml
<color name="primary_green">#a3d400</color>
<color name="dark_bg">#0D1601</color>
<color name="input_field">#f4ffb5</color>
```

### Modifying Schedule Options
In `BudgetPlanStep1Activity.setupScheduleButtons()`:
```kotlin
val scheduleNames = listOf("Weekly", "Bi-weekly", "Monthly", "Custom")
// Modify this list or add new schedule options
```

## Validation Rules

### Step 1
- ✓ At least one schedule must be selected

### Step 2
- ✓ Budget input cannot be empty
- ✓ Budget must be a valid decimal number
- ✓ Budget must be greater than 0

### Step 3
- ✓ All three categories (Needs, Savings, Wants) must have values
- ✓ All values must be greater than 0
- ✓ Sum of all categories must exactly equal total budget

## Testing the Feature

1. Run the app and reach the Dashboard
2. Tap "Create a budget plan"
3. Select a schedule → Tap Next
4. Enter budget amount (e.g., 12000) → Tap Next
5. Allocate amounts:
   - Needs: 6000
   - Savings: 2400
   - Wants: 3600
   - (Total: 12000) → Tap Finish

Success message appears and data is saved!

## Common Issues & Solutions

### "Unresolved reference 'R'"
- Solution: Import `com.example.spendsense.R` in activity files

### "Budget allocation doesn't match"
- Ensure the sum of Needs + Savings + Wants equals the total budget exactly

### Data not persisting
- Check SharedPreferences is saving correctly
- Verify keys match when retrieving data

## Integration with Other Features

### Connecting to Expense Tracking
When users add expenses, compare against their budget breakdown:
```kotlin
// Example logic
if (expenseAmount > userBudget.wants) {
    // Show warning: exceeding wants allocation
}
```

### Dashboard Display
Show current budget status on Dashboard:
```kotlin
val totalBudget = sharedPref.getString("total_budget", "0.0")?.toDouble()
val used = calculateSpentAmount() // From expenses
val percentage = (used / totalBudget) * 100
```

## Code Style & Conventions

- ✓ Full Kotlin with proper null safety
- ✓ Clear function names and comments
- ✓ Proper error handling with Toast feedback
- ✓ Resource strings/colors for easy maintenance
- ✓ MVC pattern with Activities as controllers

## Performance Considerations

- Light SharedPreferences storage
- Ready for Room Database migration
- Efficient UI updates with TextWatcher
- No unnecessary network calls

---
**Last Updated:** January 3, 2026
**Status:** Implementation Complete & Build Successful

