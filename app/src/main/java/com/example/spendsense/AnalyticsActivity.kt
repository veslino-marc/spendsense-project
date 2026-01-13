package com.example.spendsense

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var navHome: ImageView
    private lateinit var navTrack: ImageView
    private lateinit var navAdd: ImageView
    private lateinit var navRecord: ImageView

    private lateinit var pieChart: PieChartView

    private lateinit var savingsPercent: TextView
    private lateinit var wantsPercent: TextView
    private lateinit var needsPercent: TextView

    private lateinit var budgetStatusAmount: TextView
    private lateinit var budgetStatusPercent: TextView
    private lateinit var budgetProgress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        pieChart = findViewById(R.id.pieChart)

        savingsPercent = findViewById(R.id.savingsPercent)
        wantsPercent = findViewById(R.id.wantsPercent)
        needsPercent = findViewById(R.id.needsPercent)

        budgetStatusAmount = findViewById(R.id.budgetStatusAmount)
        budgetStatusPercent = findViewById(R.id.budgetStatusPercent)
        budgetProgress = findViewById(R.id.budgetProgress)

        navHome = findViewById(R.id.navHome)
        navTrack = findViewById(R.id.navTrack)
        navAdd = findViewById(R.id.navAdd)
        navRecord = findViewById(R.id.navRecord)

        setTrackActive()
        setupNavigation()

        loadAnalytics()
    }

    private fun setupNavigation() {
        navHome.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("from_login", true)
            startActivity(intent)
            finish()
        }

        navTrack.setOnClickListener {
            setTrackActive()
        }

        navAdd.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        navRecord.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("from_login", true)
            startActivity(intent)
            finish()
        }

        // Add profile navigation
        val profileIcon: ImageView = findViewById(R.id.profileIcon)
        profileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setTrackActive() {
        navHome.setColorFilter(Color.GRAY)
        navTrack.setColorFilter(Color.BLACK)
        navAdd.setColorFilter(Color.GRAY)
        navRecord.setColorFilter(Color.GRAY)
    }

    private fun loadAnalytics() {
        val result = AnalyticsUtils.compute(this)

        val df = java.text.DecimalFormat("#,###")

        budgetStatusAmount.text = "₱${df.format(result.budgetUsed)} / ₱${df.format(result.budgetTotal)}"
        budgetStatusPercent.text = "${result.budgetPercentUsed}%"
        budgetProgress.progress = result.budgetPercentUsed

        needsPercent.text = "${formatPct(result.needsPercent)}%"
        savingsPercent.text = "${formatPct(result.savingsPercent)}%"
        wantsPercent.text = "${formatPct(result.wantsPercent)}%"

        pieChart.setData(
            listOf(
                PieChartView.Slice(result.needsValue, getColor(R.color.link_green)),
                PieChartView.Slice(result.savingsValue, getColor(R.color.button_green)),
                PieChartView.Slice(result.wantsValue, getColor(R.color.input_field))
            )
        )
    }

    private fun formatPct(value: Double): String {
        return java.text.DecimalFormat("0.#").format(value)
    }
}
