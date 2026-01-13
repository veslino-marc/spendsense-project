package com.example.spendsense

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope

import com.example.spendsense.budgetplan.BudgetPlanStep1Activity
import com.example.spendsense.budgetplan.BudgetPlanStep3Activity
import com.example.spendsense.budgetplan.data.AppDatabase
import com.example.spendsense.budgetplan.data.Transaction
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class DashboardActivity : AppCompatActivity() {
    private lateinit var userManager: UserManager
    private lateinit var navHome: ImageView
    private lateinit var navTrack: ImageView
    private lateinit var navAdd: ImageView
    private lateinit var navRecord: ImageView

    // Budget UI references
    private lateinit var budgetStatusAmount: TextView
    private lateinit var budgetStatusPercent: TextView
    private lateinit var budgetAlertText: TextView
    private lateinit var needsAmount: TextView
    private lateinit var savingsAmount: TextView
    private lateinit var wantsAmount: TextView
    private lateinit var budgetProgress: ProgressBar
    private lateinit var createBudgetBtn: Button
    private lateinit var customCategoriesDisplay: LinearLayout
    private lateinit var recentTransactionsContainer: LinearLayout

    private var totalBudgetAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        userManager = UserManager(this)

        // If not logged in, go to Login
        if (!userManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Check if coming from login/register flow
        val fromLogin = intent.getBooleanExtra("from_login", false)

        // If NOT from login, ask for PIN
        if (!fromLogin) {
            startActivity(Intent(this, PINVerifyActivity::class.java))
            finish()
            return
        }

        // Set username
        val greeting: TextView = findViewById(R.id.greeting)
        val username: TextView = findViewById(R.id.username)

        greeting.text = "Good Day!"
        val displayName = userManager.getUsername()
        username.text = "Hello, $displayName"

        // Initialize navigation icons
        navHome = findViewById(R.id.navHome)
        navTrack = findViewById(R.id.navTrack)
        navAdd = findViewById(R.id.navAdd)
        navRecord = findViewById(R.id.navRecord)

        // Set home as active
        setHomeActive()

        // Setup navigation
        setupNavigation()

        // Action buttons
        createBudgetBtn = findViewById(R.id.createBudgetBtn)
        val addExpenseBtn: Button = findViewById(R.id.addExpenseBtn)
        val addCashBtn: Button = findViewById(R.id.addCashBtn)
        val profileIcon: ImageView = findViewById(R.id.profileIcon)

        addExpenseBtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("type", "CASH_OUT")
            startActivity(intent)
        }

        addCashBtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("type", "CASH_IN")
            startActivity(intent)
        }

        profileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Budget UI references
        budgetStatusAmount = findViewById(R.id.budgetStatusAmount)
        budgetStatusPercent = findViewById(R.id.budgetStatusPercent)
        budgetAlertText = findViewById(R.id.budgetAlertText)
        needsAmount = findViewById(R.id.needsAmount)
        savingsAmount = findViewById(R.id.savingsAmount)
        wantsAmount = findViewById(R.id.wantsAmount)
        budgetProgress = findViewById(R.id.budgetProgress)
        customCategoriesDisplay = findViewById(R.id.customCategoriesDisplay)
        
        // Find or create container for dynamic recent transactions
        // Note: I already updated the layout earlier to include recentTransactionsContainer
        recentTransactionsContainer = findViewById(R.id.recentTransactionsContainer)

        // Populate budget plan if saved
        loadBudgetPlan()
        
        // Start observing database changes
        observeTransactions()
    }

    private fun setupNavigation() {
        navHome.setOnClickListener {
            setHomeActive()
        }

        navTrack.setOnClickListener {
            val intent = Intent(this, AnalyticsActivity::class.java)
            intent.putExtra("from_login", true)
            startActivity(intent)
        }

        navAdd.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        navRecord.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("from_login", true)
            startActivity(intent)
        }
    }

    private fun setHomeActive() {
        navHome.setColorFilter(Color.BLACK)
        navTrack.setColorFilter(Color.GRAY)
        navAdd.setColorFilter(Color.GRAY)
        navRecord.setColorFilter(Color.GRAY)
    }

    private fun observeTransactions() {
        val db = AppDatabase.getDatabase(this)
        val dao = db.transactionDao()

        // Observe total cash out for budget tracking
        lifecycleScope.launch {
            dao.getTotalCashOut().collectLatest { used ->
                updateBudgetDisplay(used ?: 0.0)
            }
        }

        // Observe all transactions for recent list
        lifecycleScope.launch {
            dao.getAllTransactions().collectLatest { transactions ->
                updateRecentTransactions(transactions)
            }
        }
    }

    private fun updateBudgetDisplay(used: Double) {
        val df = DecimalFormat("#,###")
        val percent = if (totalBudgetAmount > 0) ((used / totalBudgetAmount) * 100).toInt() else 0

        budgetStatusAmount.text = "₱${df.format(used)} / ₱${df.format(totalBudgetAmount)}"
        budgetStatusPercent.text = "$percent%"
        budgetProgress.progress = percent.coerceIn(0, 100)

        // Alert message
        val alertText = when {
            percent >= 90 -> "Budget Alert: You've used $percent% of your budget"
            percent >= 75 -> "Heads up: $percent% of your budget used"
            totalBudgetAmount == 0.0 -> "Create a budget plan to start tracking"
            else -> "You're on track with your budget"
        }
        budgetAlertText.text = alertText
    }

    private fun updateRecentTransactions(transactions: List<Transaction>) {
        recentTransactionsContainer.removeAllViews()
        val df = DecimalFormat("#,###")
        
        // Show last 5
        val displayList = transactions.take(5)
        
        for (tx in displayList) {
            val view = LayoutInflater.from(this).inflate(R.layout.item_transaction, recentTransactionsContainer, false)
            
            val category: TextView = view.findViewById(R.id.txCategory)
            val date: TextView = view.findViewById(R.id.txDate)
            val amount: TextView = view.findViewById(R.id.txAmount)
            
            category.text = tx.category
            date.text = tx.date
            
            val prefix = if (tx.type == "CASH_IN") "+" else "-"
            val color = if (tx.type == "CASH_IN") Color.parseColor("#4CAF50") else Color.parseColor("#FF6B6B")
            
            amount.text = "${prefix}₱${df.format(tx.amount)}"
            amount.setTextColor(color)
            
            recentTransactionsContainer.addView(view)
        }
    }

    @Suppress("MissingSuperCall")
    override fun onBackPressed() {
        showLogoutDialog()
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Do you want to logout?")

        builder.setPositiveButton("Yes") { dialog, which ->
            userManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun loadBudgetPlan() {
        val prefs = getSharedPreferences("budget_plans", MODE_PRIVATE)
        val totalBudget = prefs.getString("total_budget", null)?.toDoubleOrNull()
        val needs = prefs.getString("needs", null)?.toDoubleOrNull()
        val savings = prefs.getString("savings", null)?.toDoubleOrNull()
        val wants = prefs.getString("wants", null)?.toDoubleOrNull()
        val schedule = prefs.getString("schedule", null)

        val hasPlan = totalBudget != null && needs != null && savings != null && wants != null && schedule != null
        totalBudgetAmount = totalBudget ?: 0.0

        createBudgetBtn.text = if (hasPlan) "See budget plan" else "Create a budget plan"
        createBudgetBtn.setOnClickListener {
            if (hasPlan) {
                val intent = Intent(this, BudgetPlanStep3Activity::class.java)
                intent.putExtra("editMode", true)
                intent.putExtra("schedule", schedule)
                intent.putExtra("totalBudget", totalBudget)
                intent.putExtra("needs", needs)
                intent.putExtra("savings", savings)
                intent.putExtra("wants", wants)
                startActivity(intent)
            } else {
                startActivity(Intent(this, BudgetPlanStep1Activity::class.java))
            }
        }

        if (!hasPlan) {
            budgetStatusAmount.text = "₱0 / ₱0"
            budgetStatusPercent.text = "0%"
            budgetAlertText.text = "Create a budget plan to start tracking"
            budgetProgress.progress = 0
            needsAmount.text = "₱0"
            savingsAmount.text = "₱0"
            wantsAmount.text = "₱0"
            customCategoriesDisplay.removeAllViews()
            return
        }

        val df = DecimalFormat("#,###")
        needsAmount.text = "₱${df.format(needs)}"
        savingsAmount.text = "₱${df.format(savings)}"
        wantsAmount.text = "₱${df.format(wants)}"

        // Load and display custom categories
        loadCustomCategories(prefs)
    }

    private fun loadCustomCategories(prefs: android.content.SharedPreferences) {
        customCategoriesDisplay.removeAllViews()
        val customCategoriesJson = prefs.getString("custom_categories", "{}")

        if (customCategoriesJson == null || customCategoriesJson == "{}") {
            return
        }

        // Simple JSON parsing for custom categories
        try {
            val customMap = parseCustomCategoriesJson(customCategoriesJson)
            val df = DecimalFormat("#,###")

            for ((categoryName, amount) in customMap) {
                val categoryLayout = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 6, 0, 6) }
                    orientation = LinearLayout.HORIZONTAL
                }

                val categoryLabel = TextView(this).apply {
                    text = categoryName
                    textSize = 14f
                    setTextColor(getColor(R.color.dark_bg))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val categoryAmount = TextView(this).apply {
                    text = "₱${df.format(amount)}"
                    textSize = 14f
                    setTextColor(getColor(R.color.dark_bg))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                categoryLayout.addView(categoryLabel)
                categoryLayout.addView(categoryAmount)
                customCategoriesDisplay.addView(categoryLayout)
            }
        } catch (e: Exception) {
            // If JSON parsing fails, just skip custom categories
        }
    }

    private fun parseCustomCategoriesJson(json: String): Map<String, Double> {
        val result = mutableMapOf<String, Double>()

        // Remove braces
        var content = json.trim().removePrefix("{").removeSuffix("}")

        if (content.isEmpty()) return result

        // Split by commas (basic parsing)
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
}
