package com.example.spendsense.budgetplan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.spendsense.R

/**
 * Activity for Step 1: Select Budget Schedule
 * User can choose between different schedule options (Weekly, Bi-weekly, Monthly)
 */
class BudgetPlanStep1Activity : AppCompatActivity() {

    private var selectedSchedule: String = ""
    private var editMode: Boolean = false
    private var existingTotal: Double? = null
    private var existingNeeds: Double? = null
    private var existingSavings: Double? = null
    private var existingWants: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_plan_step1)

        // read edit extras
        editMode = intent.getBooleanExtra("editMode", false)
        selectedSchedule = intent.getStringExtra("schedule") ?: ""
        existingTotal = intent.getDoubleExtra("totalBudget", 0.0).takeIf { intent.hasExtra("totalBudget") }
        existingNeeds = intent.getDoubleExtra("needs", 0.0).takeIf { intent.hasExtra("needs") }
        existingSavings = intent.getDoubleExtra("savings", 0.0).takeIf { intent.hasExtra("savings") }
        existingWants = intent.getDoubleExtra("wants", 0.0).takeIf { intent.hasExtra("wants") }

        // Load session data if not in edit mode
        if (!editMode && selectedSchedule.isEmpty()) {
            loadSessionData()
        }

        setupScheduleButtons()
        setupNavigationButtons()
    }

    private fun loadSessionData() {
        val prefs = getSharedPreferences("budget_session", MODE_PRIVATE)
        selectedSchedule = prefs.getString("session_schedule", "") ?: ""
    }

    private fun setupScheduleButtons() {
        val schedules = listOf(
            findViewById<Button>(R.id.scheduleBtn1),
            findViewById<Button>(R.id.scheduleBtn2),
            findViewById<Button>(R.id.scheduleBtn3),
            findViewById<Button>(R.id.scheduleBtn4)
        )

        val scheduleNames = listOf("Weekly", "Bi-weekly", "Monthly", "Custom")

        schedules.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedSchedule = scheduleNames[index]
                updateScheduleSelection(schedules, index)
            }
        }

        // preselect if editing
        if (selectedSchedule.isNotEmpty()) {
            val idx = scheduleNames.indexOf(selectedSchedule)
            if (idx >= 0) updateScheduleSelection(schedules, idx)
        }
    }

    private fun updateScheduleSelection(buttons: List<Button>, selectedIndex: Int) {
        val selectedTint = ContextCompat.getColorStateList(this, R.color.link_green)
        val unselectedTint = ContextCompat.getColorStateList(this, R.color.primary_green)

        buttons.forEachIndexed { index, button ->
            button.background = ContextCompat.getDrawable(this, R.drawable.budget_button_background)
            button.backgroundTintList = if (index == selectedIndex) selectedTint else unselectedTint
            button.setTextColor(ContextCompat.getColor(this, R.color.dark_bg))
        }
    }

    private fun setupNavigationButtons() {
        val nextBtn: Button = findViewById(R.id.nextBtn)
        val backBtn: Button = findViewById(R.id.backBtn)

        nextBtn.setOnClickListener {
            if (selectedSchedule.isNotEmpty()) {
                // Save to session
                val prefs = getSharedPreferences("budget_session", MODE_PRIVATE)
                with(prefs.edit()) {
                    putString("session_schedule", selectedSchedule)
                    apply()
                }

                val intent = Intent(this, BudgetPlanStep2Activity::class.java)
                intent.putExtra("schedule", selectedSchedule)
                if (editMode) {
                    intent.putExtra("editMode", true)
                    existingTotal?.let { intent.putExtra("totalBudget", it) }
                    existingNeeds?.let { intent.putExtra("needs", it) }
                    existingSavings?.let { intent.putExtra("savings", it) }
                    existingWants?.let { intent.putExtra("wants", it) }
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select a schedule", Toast.LENGTH_SHORT).show()
            }
        }

        backBtn.setOnClickListener {
            finish()
        }
    }
}
