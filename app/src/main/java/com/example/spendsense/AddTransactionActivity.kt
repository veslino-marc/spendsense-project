package com.example.spendsense

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.spendsense.budgetplan.data.AppDatabase
import com.example.spendsense.budgetplan.data.Transaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button
    private lateinit var tabCashIn: Button
    private lateinit var tabCashOut: Button
    private lateinit var categoryGrid: GridLayout
    
    private var transactionType = "CASH_OUT"
    private var selectedCategory = "Food"
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        // Initialize Views
        etAmount = findViewById(R.id.etAmount)
        etDate = findViewById(R.id.etDate)
        etDescription = findViewById(R.id.etDescription)
        btnSave = findViewById(R.id.btnSaveTransaction)
        tabCashIn = findViewById(R.id.tabCashIn)
        tabCashOut = findViewById(R.id.tabCashOut)
        categoryGrid = findViewById(R.id.categoryGrid)
        
        val logoIcon: ImageView = findViewById(R.id.logoIcon)
        logoIcon.setOnClickListener { finish() }

        val profileIcon: ImageView = findViewById(R.id.profileIcon)
        profileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Set default date
        updateDateLabel()

        // Handle Type selection - Changed to Button
        tabCashIn.setOnClickListener { setTransactionType("CASH_IN") }
        tabCashOut.setOnClickListener { setTransactionType("CASH_OUT") }

        // Date Picker
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateLabel()
        }

        etDate.setOnClickListener {
            DatePickerDialog(this, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Add text watcher to amount field to maintain peso sign
        setupAmountInput()

        // Setup Categories
        setupCategorySelection()

        // Save Transaction
        btnSave.setOnClickListener {
            saveTransaction()
        }

        // Initial state from intent
        val initialType = intent.getStringExtra("type") ?: "CASH_OUT"
        setTransactionType(initialType)
        
        // Setup Bottom Nav routing
        setupBottomNavigation()
    }

    private fun setupAmountInput() {
        etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()

                // If input doesn't start with ₱, add it
                if (input.isNotEmpty() && !input.startsWith("₱")) {
                    etAmount.setText("₱$input")
                    etAmount.setSelection(etAmount.text.length)
                }
                // If only ₱ is present, that's fine
            }
        })
    }

    private fun setTransactionType(type: String) {
        transactionType = type
        if (type == "CASH_IN") {
            tabCashIn.setBackgroundResource(R.drawable.tab_active)
            tabCashOut.setBackgroundResource(R.drawable.tab_inactive)
            btnSave.text = "Save Income"
        } else {
            tabCashIn.setBackgroundResource(R.drawable.tab_inactive)
            tabCashOut.setBackgroundResource(R.drawable.tab_active)
            btnSave.text = "Save Expense"
        }
    }

    private fun updateDateLabel() {
        val myFormat = "MM / dd / yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        etDate.setText(sdf.format(calendar.time))
    }

    private fun setupCategorySelection() {
        // Set default category to first button (Food)
        selectedCategory = "Food"

        // Find all button views in the grid and add click listeners
        for (i in 0 until categoryGrid.childCount) {
            val child = categoryGrid.getChildAt(i)
            if (child is Button) {
                child.setOnClickListener {
                    // Reset others
                    for (j in 0 until categoryGrid.childCount) {
                        val btn = categoryGrid.getChildAt(j) as? Button
                        btn?.alpha = 0.5f
                    }
                    // Highlight selected
                    it.alpha = 1.0f
                    // Get category name from button text
                    selectedCategory = (it as Button).text.toString()
                }
                // Default first one
                if (i == 0) child.alpha = 1.0f else child.alpha = 0.5f
            }
        }
    }

    private fun saveTransaction() {
        val amountStr = etAmount.text.toString().replace("₱", "").replace(",", "").trim()
        val amount = amountStr.toDoubleOrNull()
        val date = etDate.text.toString()
        val description = etDescription.text.toString()

        // Validate amount
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if cash out and validate sufficient balance
        if (transactionType == "CASH_OUT") {
            // Get user's total cash in
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(this@AddTransactionActivity)
                try {
                    val totalCashIn = db.transactionDao().getTotalCashIn().first() ?: 0.0
                    val totalCashOut = db.transactionDao().getTotalCashOut().first() ?: 0.0
                    val availableBalance = totalCashIn - totalCashOut

                    if (amount > availableBalance) {
                        Toast.makeText(
                            this@AddTransactionActivity,
                            "Insufficient balance! Available: ₱$availableBalance",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }

                    // Proceed with saving
                    insertTransaction(amount, date, description)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@AddTransactionActivity,
                        "Error checking balance",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            // For cash in, just save
            insertTransaction(amount, date, description)
        }
    }

    private fun insertTransaction(amount: Double, date: String, description: String) {
        val transaction = Transaction(
            amount = amount,
            category = selectedCategory,
            date = date,
            description = description,
            type = transactionType
        )

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@AddTransactionActivity)
            db.transactionDao().insert(transaction)
            Toast.makeText(this@AddTransactionActivity, "Transaction saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun setupBottomNavigation() {
        findViewById<View>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("from_login", true)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.navTrack).setOnClickListener {
            val intent = Intent(this, AnalyticsActivity::class.java)
            intent.putExtra("from_login", true)
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.navAdd).setOnClickListener {
            // Already on this activity
        }

        findViewById<View>(R.id.navRecord).setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("from_login", true)
            startActivity(intent)
            finish()
        }
    }
}
