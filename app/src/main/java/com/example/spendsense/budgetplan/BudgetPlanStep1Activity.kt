package com.example.spendsense.budgetplan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.spendsense.R

/**
 * Activity for Step 1: Select Budget Schedule
 * User can choose between different schedule options (Weekly, Bi-weekly, Monthly)
 */
class BudgetPlanStep1Activity : AppCompatActivity() {

    private var selectedSchedule: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_plan_step1)

        setupScheduleButtons()
        setupNavigationButtons()
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
    }

    private fun updateScheduleSelection(buttons: List<Button>, selectedIndex: Int) {
        buttons.forEachIndexed { index, button ->
            if (index == selectedIndex) {
                button.isSelected = true
                button.setBackgroundColor(getColor(android.R.color.holo_green_light))
            } else {
                button.isSelected = false
                button.setBackgroundColor(getColor(R.color.primary_green))
            }
        }
    }

    private fun setupNavigationButtons() {
        val nextBtn: Button = findViewById(R.id.nextBtn)
        val backBtn: Button = findViewById(R.id.backBtn)

        nextBtn.setOnClickListener {
            if (selectedSchedule.isNotEmpty()) {
                // Pass selected schedule to next activity
                val intent = Intent(this, BudgetPlanStep2Activity::class.java)
                intent.putExtra("schedule", selectedSchedule)
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

