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

    // UI Components
    private lateinit var needsInput: EditText
    private lateinit var savingsInput: EditText
    private lateinit var wantsInput: EditText
    private lateinit var totalBudgetDisplay: TextView
    private lateinit var addCategoryBtn: ImageView

    // Summary TextViews
    private lateinit var needsPercentage: TextView
    private lateinit var savingsPercentage: TextView
    private lateinit var wantsPercentage: TextView

    private val decimalFormat = DecimalFormat("0.00")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_plan_step3)

        // Get data from previous activity
        selectedSchedule = intent.getStringExtra("schedule") ?: ""
        totalBudget = intent.getDoubleExtra("totalBudget", 0.0)

        initializeUI()
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

        // Initialize buttons
        addCategoryBtn = findViewById(R.id.addCategoryBtn)

        // Display total budget
        totalBudgetDisplay.text = "₱${decimalFormat.format(totalBudget)} / ₱${decimalFormat.format(totalBudget)}"

        addCategoryBtn.setOnClickListener {
            addMoreCategory()
        }
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

    private fun updateBudgetCalculations() {
        val needs = needsInput.text.toString().toDoubleOrNull() ?: 0.0
        val savings = savingsInput.text.toString().toDoubleOrNull() ?: 0.0
        val wants = wantsInput.text.toString().toDoubleOrNull() ?: 0.0

        val total = needs + savings + wants

        // Update percentages
        needsPercentage.text = "${((needs / totalBudget) * 100).toInt()}%"
        savingsPercentage.text = "${((savings / totalBudget) * 100).toInt()}%"
        wantsPercentage.text = "${((wants / totalBudget) * 100).toInt()}%"

        // Update total budget display
        totalBudgetDisplay.text = "₱${decimalFormat.format(total)} / ₱${decimalFormat.format(totalBudget)}"
    }

    private fun addMoreCategory() {
        // This can be expanded for custom categories
        Toast.makeText(this, "Custom categories coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun setupNavigationButtons() {
        val finishBtn: Button = findViewById(R.id.finishBtn)
        val backBtn: Button = findViewById(R.id.backBtn)

        finishBtn.setOnClickListener {
            if (validateBudgetAllocation()) {
                saveBudgetPlan()
                finish()
            }
        }

        backBtn.setOnClickListener {
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

        val total = needs + savings + wants
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

        // Save to SharedPreferences (can be upgraded to database later)
        val sharedPref = getSharedPreferences("budget_plans", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("latest_budget_plan", budgetPlan.toString())
            putString("total_budget", totalBudget.toString())
            putString("needs", needs.toString())
            putString("savings", savings.toString())
            putString("wants", wants.toString())
            putString("schedule", selectedSchedule)
            apply()
        }

        Toast.makeText(this, "Budget plan created successfully!", Toast.LENGTH_SHORT).show()
    }
}

