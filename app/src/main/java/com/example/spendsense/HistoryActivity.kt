package com.example.spendsense

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var navHome: ImageView
    private lateinit var navTrack: ImageView
    private lateinit var navAdd: ImageView
    private lateinit var navRecord: ImageView

    private lateinit var transactionsContainer: LinearLayout
    private lateinit var emptyStateText: TextView

    private var allTransactions: List<TransactionUtils.Transaction> = emptyList()
    private var availableCategories: List<String> = emptyList()
    private val selectedCategories: MutableSet<String> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        navHome = findViewById(R.id.navHome)
        navTrack = findViewById(R.id.navTrack)
        navAdd = findViewById(R.id.navAdd)
        navRecord = findViewById(R.id.navRecord)

        transactionsContainer = findViewById(R.id.transactionsContainer)
        emptyStateText = findViewById(R.id.emptyStateText)

        setRecordActive()
        setupNavigation()

        val filterBtn: Button = findViewById(R.id.filterBtn)
        filterBtn.setOnClickListener {
            showFilterDialog()
        }

        val profileIcon: ImageView = findViewById(R.id.profileIcon)
        profileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        loadAndRender()
    }

    override fun onResume() {
        super.onResume()
        loadAndRender()
    }

    private fun setupNavigation() {
        navHome.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("from_login", true)
            startActivity(intent)
            finish()
        }

        navTrack.setOnClickListener {
            val intent = Intent(this, AnalyticsActivity::class.java)
            intent.putExtra("from_login", true)
            startActivity(intent)
            finish()
        }

        navAdd.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        navRecord.setOnClickListener {
            setRecordActive()
        }
    }

    private fun setRecordActive() {
        navHome.setColorFilter(Color.GRAY)
        navTrack.setColorFilter(Color.GRAY)
        navAdd.setColorFilter(Color.GRAY)
        navRecord.setColorFilter(Color.BLACK)
    }

    private fun showFilterDialog() {
        if (availableCategories.isEmpty()) {
            Toast.makeText(this, "No categories to filter", Toast.LENGTH_SHORT).show()
            return
        }

        val checked = availableCategories.map { selectedCategories.contains(it) }.toBooleanArray()

        AlertDialog.Builder(this)
            .setTitle("Filter")
            .setMultiChoiceItems(availableCategories.toTypedArray(), checked) { _, which, isChecked ->
                val category = availableCategories[which]
                if (isChecked) {
                    selectedCategories.add(category)
                } else {
                    selectedCategories.remove(category)
                }
            }
            .setPositiveButton("Apply") { dialog, _ ->
                render()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Clear") { dialog, _ ->
                selectedCategories.clear()
                render()
                dialog.dismiss()
            }
            .show()
    }

    private fun loadAndRender() {
        allTransactions = TransactionUtils.readTransactions(this)
        availableCategories = allTransactions.map { it.category }.distinct().sorted()

        if (selectedCategories.isEmpty() && availableCategories.isNotEmpty()) {
            selectedCategories.addAll(availableCategories)
        } else {
            selectedCategories.retainAll(availableCategories.toSet())
        }

        render()
    }

    private fun render() {
        transactionsContainer.removeAllViews()

        val filtered = if (selectedCategories.isEmpty()) {
            emptyList()
        } else {
            allTransactions.filter { selectedCategories.contains(it.category) }
        }

        emptyStateText.visibility = if (filtered.isEmpty()) TextView.VISIBLE else TextView.GONE
        if (filtered.isEmpty()) return

        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

        var currentMonthKey: String? = null
        for (tx in filtered) {
            val monthKey = monthFormat.format(Date(tx.timestampMs))
            if (monthKey != currentMonthKey) {
                currentMonthKey = monthKey
                transactionsContainer.addView(createMonthHeader(monthKey))
            }

            transactionsContainer.addView(createTransactionRow(tx))
        }
    }

    private fun createMonthHeader(title: String): TextView {
        return TextView(this).apply {
            text = title
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(getColor(R.color.dark_bg))
            setPadding(0, dp(10), 0, dp(8))
        }
    }

    private fun createTransactionRow(tx: TransactionUtils.Transaction): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
            tag = tx.category
        }

        val icon = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(24), dp(24)).apply {
                rightMargin = dp(12)
            }
            setBackgroundColor(getColor(R.color.dark_bg))
        }

        val middle = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val title = TextView(this).apply {
            text = tx.category
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(getColor(R.color.dark_bg))
        }

        val time = TextView(this).apply {
            text = formatRelativeTime(tx.timestampMs)
            textSize = 10f
            setTextColor(getColor(R.color.placeholder))
        }

        middle.addView(title)
        middle.addView(time)

        val amountText = TextView(this).apply {
            val abs = kotlin.math.abs(tx.amount)
            val sign = if (tx.amount >= 0) "+" else "-"
            text = "$sign ₱${formatAmount(abs)}"
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#B20000"))
        }

        row.addView(icon)
        row.addView(middle)
        row.addView(amountText)
        return row
    }

    private fun formatAmount(value: Double): String {
        return java.text.DecimalFormat("#,###.##").format(value)
    }

    private fun formatRelativeTime(timestampMs: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestampMs
        val oneDay = 24L * 60L * 60L * 1000L

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val timePart = timeFormat.format(Date(timestampMs))

        return when {
            diff < oneDay && isSameDay(now, timestampMs) -> "Today, $timePart"
            diff < 2 * oneDay && isSameDay(now - oneDay, timestampMs) -> "Yesterday, $timePart"
            else -> "${dateFormat.format(Date(timestampMs))}, $timePart"
        }
    }

    private fun isSameDay(aMs: Long, bMs: Long): Boolean {
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return fmt.format(Date(aMs)) == fmt.format(Date(bMs))
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
