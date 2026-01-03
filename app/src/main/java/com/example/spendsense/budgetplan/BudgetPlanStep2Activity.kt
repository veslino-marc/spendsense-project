package com.example.spendsense.budgetplan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.spendsense.R

/**
 * Activity for Step 2: Input Total Allotted Budget
 * User enters the total monthly budget amount
 */
class BudgetPlanStep2Activity : AppCompatActivity() {

    private var selectedSchedule: String = ""
    private var editMode: Boolean = false
    private var existingNeeds: Double? = null
    private var existingSavings: Double? = null
    private var existingWants: Double? = null
    private lateinit var budgetInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_plan_step2)

        // Get the schedule from previous activity
        selectedSchedule = intent.getStringExtra("schedule") ?: ""
        editMode = intent.getBooleanExtra("editMode", false)
        val existingTotal = intent.getDoubleExtra("totalBudget", 0.0).takeIf { intent.hasExtra("totalBudget") }
        existingNeeds = intent.getDoubleExtra("needs", 0.0).takeIf { intent.hasExtra("needs") }
        existingSavings = intent.getDoubleExtra("savings", 0.0).takeIf { intent.hasExtra("savings") }
        existingWants = intent.getDoubleExtra("wants", 0.0).takeIf { intent.hasExtra("wants") }

        budgetInput = findViewById(R.id.budgetInput)
        existingTotal?.let { budgetInput.setText(it.toString()) }

        setupNavigationButtons()
    }

    private fun setupNavigationButtons() {
        val nextBtn: Button = findViewById(R.id.nextBtn)
        val backBtn: Button = findViewById(R.id.backBtn)

        nextBtn.setOnClickListener {
            val budgetAmount = budgetInput.text.toString().trim()

            if (budgetAmount.isEmpty()) {
                Toast.makeText(this, "Please enter a budget amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val budget = budgetAmount.toDouble()
                if (budget <= 0) {
                    Toast.makeText(this, "Budget must be greater than 0", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Pass schedule and budget to next activity
                val intent = Intent(this, BudgetPlanStep3Activity::class.java)
                intent.putExtra("schedule", selectedSchedule)
                intent.putExtra("totalBudget", budget)
                if (editMode) {
                    intent.putExtra("editMode", true)
                    existingNeeds?.let { intent.putExtra("needs", it) }
                    existingSavings?.let { intent.putExtra("savings", it) }
                    existingWants?.let { intent.putExtra("wants", it) }
                }
                startActivity(intent)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }

        backBtn.setOnClickListener {
            finish()
        }
    }
}
