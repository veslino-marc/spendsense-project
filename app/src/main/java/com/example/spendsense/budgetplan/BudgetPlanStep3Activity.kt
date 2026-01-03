package com.example.spendsense.budgetplan

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.spendsense.R
import com.example.spendsense.DashboardActivity
import com.example.spendsense.budgetplan.data.BudgetPlan
import com.example.spendsense.budgetplan.data.BudgetBreakdown
import java.text.DecimalFormat

/**
 * Activity for Step 3: Budget Breakdown (Needs, Savings, Wants)
 * User allocates budget into three categories
 */
class BudgetPlanStep3Activity : AppCompatActivity() {

    private var selectedSchedule: String = ""
    private var totalBudget: Double = 0.0
    private var editMode: Boolean = false

    // Draft cache for create mode
    private var draftNeeds: Double? = null
    private var draftSavings: Double? = null
    private var draftWants: Double? = null

    // UI Components
    private lateinit var needsInput: EditText
    private lateinit var savingsInput: EditText
    private lateinit var wantsInput: EditText
    private lateinit var totalBudgetDisplay: TextView
    private lateinit var addCategoryBtn: ImageView
    private lateinit var customCategoriesContainer: LinearLayout

    // Summary TextViews
    private lateinit var needsPercentage: TextView
    private lateinit var savingsPercentage: TextView
    private lateinit var wantsPercentage: TextView

    // Custom categories list (key: category name, value: EditText for amount)
    private val customCategories = mutableMapOf<String, EditText>()

    private val decimalFormat = DecimalFormat("0.00")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_plan_step3)

        // Get data from previous activity
        selectedSchedule = intent.getStringExtra("schedule") ?: ""
        totalBudget = intent.getDoubleExtra("totalBudget", 0.0)
        editMode = intent.getBooleanExtra("editMode", false)

        if (!editMode) {
            // Load draft if present
            val prefs = getSharedPreferences("budget_session", MODE_PRIVATE)
            if (selectedSchedule.isEmpty()) {
                selectedSchedule = prefs.getString("session_schedule", "") ?: ""
            }
            if (totalBudget == 0.0) {
                totalBudget = prefs.getString("session_total", "0.0")?.toDoubleOrNull() ?: 0.0
            }
            draftNeeds = prefs.getString("session_needs", null)?.toDoubleOrNull()
            draftSavings = prefs.getString("session_savings", null)?.toDoubleOrNull()
            draftWants = prefs.getString("session_wants", null)?.toDoubleOrNull()
        }

        initializeUI()

        // Apply draft values after UI init
        if (!editMode) {
            draftNeeds?.let { needsInput.setText(it.toString()) }
            draftSavings?.let { savingsInput.setText(it.toString()) }
            draftWants?.let { wantsInput.setText(it.toString()) }
        }

        prefillIfEditing()
        loadSavedCustomCategories()
        setupInputListeners()
        setupNavigationButtons()
    }

    private fun initializeUI() {
        // Initialize EditTexts
        needsInput = findViewById(R.id.needsInput)
        savingsInput = findViewById(R.id.savingsInput)
        wantsInput = findViewById(R.id.wantsInput)

        // Initialize TextViews
        totalBudgetDisplay = findViewById(R.id.totalBudgetDisplay)
        needsPercentage = findViewById(R.id.needsPercentage)
        savingsPercentage = findViewById(R.id.savingsPercentage)
        wantsPercentage = findViewById(R.id.wantsPercentage)

        // Initialize buttons and containers
        addCategoryBtn = findViewById(R.id.addCategoryBtn)
        customCategoriesContainer = findViewById(R.id.customCategoriesContainer)

        // Display total budget
        totalBudgetDisplay.text = "₱${decimalFormat.format(totalBudget)} / ₱${decimalFormat.format(totalBudget)}"

        addCategoryBtn.setOnClickListener {
            addNewCategory()
        }
    }

    private fun prefillIfEditing() {
        val needsExtra = intent.getDoubleExtra("needs", 0.0).takeIf { intent.hasExtra("needs") }
        val savingsExtra = intent.getDoubleExtra("savings", 0.0).takeIf { intent.hasExtra("savings") }
        val wantsExtra = intent.getDoubleExtra("wants", 0.0).takeIf { intent.hasExtra("wants") }

        needsExtra?.let { needsInput.setText(it.toString()) }
        savingsExtra?.let { savingsInput.setText(it.toString()) }
        wantsExtra?.let { wantsInput.setText(it.toString()) }
    }

    private fun setupInputListeners() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateBudgetCalculations()
            }
        }

        needsInput.addTextChangedListener(textWatcher)
        savingsInput.addTextChangedListener(textWatcher)
        wantsInput.addTextChangedListener(textWatcher)
    }

    private fun addNewCategory() {
        // Create dialog to get category name
        val dialogView = android.widget.EditText(this).apply {
            hint = "Category name (e.g., Entertainment)"
            textSize = 16f
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("Add Custom Category")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val categoryName = dialogView.text.toString().trim()
                if (categoryName.isNotEmpty() && !customCategories.containsKey(categoryName)) {
                    createCategoryRow(categoryName)
                    updateBudgetCalculations()
                } else if (categoryName.isEmpty()) {
                    Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createCategoryRow(categoryName: String) {
        // Create container for the category
        val categoryLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 20) }
            orientation = LinearLayout.VERTICAL
        }

        // Label row with category name and percentage
        val labelLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 10) }
            orientation = LinearLayout.HORIZONTAL
        }

        val categoryLabel = TextView(this).apply {
            text = "$categoryName *"
            textSize = 14f
            setTextColor(getColor(R.color.dark_bg))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val categoryPercent = TextView(this).apply {
            text = "0%"
            textSize = 14f
            setTextColor(getColor(R.color.dark_bg))
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        labelLayout.addView(categoryLabel)
        labelLayout.addView(categoryPercent)

        // Input field
        val categoryInput = EditText(this).apply {
            hint = "0.00"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            textSize = 16f
            setTextColor(getColor(R.color.dark_bg))
            setHintTextColor(getColor(R.color.placeholder))
            setBackgroundResource(R.drawable.input_background)
            setPadding(45, 15, 45, 15)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120
            )
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    updateBudgetCalculations()
                    // Update this category's percentage
                    val value = this@apply.text.toString().toDoubleOrNull() ?: 0.0
                    categoryPercent.text = "${((value / totalBudget) * 100).toInt()}%"
                }
            })
        }

        // Delete button
        val deleteButton = android.widget.Button(this).apply {
            text = "Remove"
            setTextColor(getColor(R.color.dark_bg))
            setBackgroundResource(R.drawable.action_button_background)
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 10, 0, 0) }
            setPadding(15, 15, 15, 15)
            setOnClickListener {
                customCategoriesContainer.removeView(categoryLayout)
                customCategories.remove(categoryName)
                updateBudgetCalculations()
            }
        }

        categoryLayout.addView(labelLayout)
        categoryLayout.addView(categoryInput)
        categoryLayout.addView(deleteButton)

        customCategoriesContainer.addView(categoryLayout)
        customCategories[categoryName] = categoryInput
    }

    private fun updateBudgetCalculations() {
        val needs = needsInput.text.toString().toDoubleOrNull() ?: 0.0
        val savings = savingsInput.text.toString().toDoubleOrNull() ?: 0.0
        val wants = wantsInput.text.toString().toDoubleOrNull() ?: 0.0

        var customTotal = 0.0
        for ((_, input) in customCategories) {
            customTotal += input.text.toString().toDoubleOrNull() ?: 0.0
        }

        val total = needs + savings + wants + customTotal

        // Update percentages
        needsPercentage.text = "${((needs / totalBudget) * 100).toInt()}%"
        savingsPercentage.text = "${((savings / totalBudget) * 100).toInt()}%"
        wantsPercentage.text = "${((wants / totalBudget) * 100).toInt()}%"

        // Update total budget display
        totalBudgetDisplay.text = "₱${decimalFormat.format(total)} / ₱${decimalFormat.format(totalBudget)}"
    }

    private fun setupNavigationButtons() {
        val finishBtn: Button = findViewById(R.id.finishBtn)
        val backBtn: Button = findViewById(R.id.backBtn)
        val backToStep1Btn: Button = findViewById(R.id.backToStep1Btn)
        val backToStep2Btn: Button = findViewById(R.id.backToStep2Btn)

        finishBtn.setOnClickListener {
            if (validateBudgetAllocation()) {
                saveBudgetPlan()
                // go to dashboard after saving
                val intent = Intent(this, DashboardActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                if (!editMode) {
                    clearSessionDraft()
                }
                finish()
            }
        }

        backBtn.setOnClickListener {
            if (!editMode) saveSessionDraft()
            finish()
        }

        backToStep1Btn.setOnClickListener {
            if (!editMode) saveSessionDraft()
            val intent = Intent(this, BudgetPlanStep1Activity::class.java)
            intent.putExtra("schedule", selectedSchedule)
            if (editMode) {
                intent.putExtra("editMode", true)
            }
            startActivity(intent)
            finish()
        }

        backToStep2Btn.setOnClickListener {
            if (!editMode) saveSessionDraft()
            val intent = Intent(this, BudgetPlanStep2Activity::class.java)
            intent.putExtra("schedule", selectedSchedule)
            intent.putExtra("totalBudget", totalBudget)
            if (editMode) {
                intent.putExtra("editMode", true)
                intent.putExtra("needs", needsInput.text.toString().toDoubleOrNull() ?: 0.0)
                intent.putExtra("savings", savingsInput.text.toString().toDoubleOrNull() ?: 0.0)
                intent.putExtra("wants", wantsInput.text.toString().toDoubleOrNull() ?: 0.0)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun validateBudgetAllocation(): Boolean {
        val needs = needsInput.text.toString().toDoubleOrNull() ?: 0.0
        val savings = savingsInput.text.toString().toDoubleOrNull() ?: 0.0
        val wants = wantsInput.text.toString().toDoubleOrNull() ?: 0.0

        if (needs <= 0 || savings <= 0 || wants <= 0) {
            Toast.makeText(this, "All categories must have a value greater than 0", Toast.LENGTH_SHORT).show()
            return false
        }

        var customTotal = 0.0
        for ((_, input) in customCategories) {
            val value = input.text.toString().toDoubleOrNull() ?: 0.0
            if (value < 0) {
                Toast.makeText(this, "Custom category values cannot be negative", Toast.LENGTH_SHORT).show()
                return false
            }
            customTotal += value
        }

        val total = needs + savings + wants + customTotal
        if (total != totalBudget) {
            Toast.makeText(
                this,
                "Total allocation (₱${decimalFormat.format(total)}) must equal total budget (₱${decimalFormat.format(totalBudget)})",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        return true
    }

    private fun saveBudgetPlan() {
        val needs = needsInput.text.toString().toDouble()
        val savings = savingsInput.text.toString().toDouble()
        val wants = wantsInput.text.toString().toDouble()

        val budgetBreakdown = BudgetBreakdown(
            needs = needs,
            savings = savings,
            wants = wants
        )

        val budgetPlan = BudgetPlan(
            id = System.currentTimeMillis().toString(),
            totalBudget = totalBudget,
            schedule = selectedSchedule,
            budgetBreakdown = budgetBreakdown
        )

        // Save to SharedPreferences (overwrite for edit or create)
        val sharedPref = getSharedPreferences("budget_plans", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("latest_budget_plan", budgetPlan.toString())
            putString("total_budget", totalBudget.toString())
            putString("needs", needs.toString())
            putString("savings", savings.toString())
            putString("wants", wants.toString())
            putString("schedule", selectedSchedule)

            // Save custom categories as JSON
            val customCategoriesJson = buildCustomCategoriesJson()
            putString("custom_categories", customCategoriesJson)

            apply()
        }

        Toast.makeText(this, if (editMode) "Budget plan updated" else "Budget plan created successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun saveSessionDraft() {
        val prefs = getSharedPreferences("budget_session", MODE_PRIVATE)
        with(prefs.edit()) {
            putString("session_schedule", selectedSchedule)
            putString("session_total", totalBudget.toString())
            putString("session_needs", needsInput.text.toString())
            putString("session_savings", savingsInput.text.toString())
            putString("session_wants", wantsInput.text.toString())
            // Save custom categories draft as well
            putString("session_custom", buildCustomCategoriesJson())
            apply()
        }
    }

    private fun clearSessionDraft() {
        val prefs = getSharedPreferences("budget_session", MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    private fun loadSavedCustomCategories() {
        // first load from edit mode if any
        if (editMode) {
            val prefs = getSharedPreferences("budget_plans", MODE_PRIVATE)
            val customCategoriesJson = prefs.getString("custom_categories", "{}")
            if (!(customCategoriesJson == null || customCategoriesJson == "{}")) {
                try {
                    val customMap = parseCustomCategoriesJson(customCategoriesJson)
                    for ((categoryName, amount) in customMap) {
                        createCategoryRow(categoryName)
                        customCategories[categoryName]?.setText(amount.toString())
                    }
                } catch (_: Exception) {}
            }
        } else {
            // load draft session if present
            val prefs = getSharedPreferences("budget_session", MODE_PRIVATE)
            val draft = prefs.getString("session_custom", "{}")
            if (!(draft == null || draft == "{}")) {
                try {
                    val customMap = parseCustomCategoriesJson(draft)
                    for ((categoryName, amount) in customMap) {
                        createCategoryRow(categoryName)
                        customCategories[categoryName]?.setText(amount.toString())
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun parseCustomCategoriesJson(json: String): Map<String, Double> {
        val result = mutableMapOf<String, Double>()

        var content = json.trim().removePrefix("{").removeSuffix("}")

        if (content.isEmpty()) return result

        val pairs = content.split(",")
        for (pair in pairs) {
            try {
                val parts = pair.split(":")
                if (parts.size == 2) {
                    val key = parts[0].trim().removeSurrounding("\"")
                    val value = parts[1].trim().toDoubleOrNull() ?: 0.0
                    result[key] = value
                }
            } catch (e: Exception) {
                // Skip malformed pairs
            }
        }

        return result
    }

    private fun buildCustomCategoriesJson(): String {
        if (customCategories.isEmpty()) return "{}"

        val sb = StringBuilder("{")
        var isFirst = true
        for ((name, input) in customCategories) {
            if (!isFirst) sb.append(",")
            val value = input.text.toString().toDoubleOrNull() ?: 0.0
            sb.append("\"${name.replace("\"", "\\\"")}\":$value")
            isFirst = false
        }
        sb.append("}")
        return sb.toString()
    }
}
