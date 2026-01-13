package com.example.spendsense

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userManager = UserManager(this)

        val profileUsername: TextView = findViewById(R.id.profileUsername)
        val profileEmail: TextView = findViewById(R.id.profileEmail)
        val btnChangePin: TextView = findViewById(R.id.btnChangePin)
        val btnChangeUsername: TextView = findViewById(R.id.btnChangeUsername)
        val btnLogout: TextView = findViewById(R.id.btnLogout)
        val backButton: ImageView = findViewById(R.id.logoIcon)

        // Load and display user data from UserManager
        loadUserData(profileUsername, profileEmail)

        // Back button
        backButton.setOnClickListener {
            finish()
        }

        // Change PIN button
        btnChangePin.setOnClickListener {
            showChangePinDialog()
        }

        // Change Username button
        btnChangeUsername.setOnClickListener {
            showChangeUsernameDialog(profileUsername)
        }

        // Logout button
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        // Setup bottom navigation
        setupBottomNavigation()
    }

    private fun loadUserData(usernameView: TextView, emailView: TextView) {
        val username = userManager.getUsername()
        val email = userManager.getEmail()

        usernameView.text = username
        emailView.text = if (email.isEmpty()) "No email set" else email
    }

    private fun setupBottomNavigation() {
        val navHome: ImageView = findViewById(R.id.navHome)
        val navTrack: ImageView = findViewById(R.id.navTrack)
        val navAdd: ImageView = findViewById(R.id.navAdd)
        val navRecord: ImageView = findViewById(R.id.navRecord)

        setProfileActive(navHome, navTrack, navAdd, navRecord)

        navHome.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("from_login", true)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
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
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("from_login", true)
            startActivity(intent)
            finish()
        }
    }

    private fun setProfileActive(navHome: ImageView, navTrack: ImageView, navAdd: ImageView, navRecord: ImageView) {
        navHome.setColorFilter(Color.GRAY)
        navTrack.setColorFilter(Color.GRAY)
        navAdd.setColorFilter(Color.GRAY)
        navRecord.setColorFilter(Color.GRAY)
    }

    private fun showChangePinDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change PIN")

        // Create container for inputs
        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(50, 20, 50, 20)
        }

        // Current PIN
        val currentPinLabel = TextView(this).apply {
            text = "Current PIN:"
            textSize = 14f
        }
        container.addView(currentPinLabel)

        val currentPinInput = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Enter current PIN"
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(currentPinInput)

        // New PIN
        val newPinLabel = TextView(this).apply {
            text = "New PIN (4 digits):"
            textSize = 14f
        }
        container.addView(newPinLabel)

        val newPinInput = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Enter new PIN"
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(newPinInput)

        // Confirm PIN
        val confirmPinLabel = TextView(this).apply {
            text = "Confirm PIN:"
            textSize = 14f
        }
        container.addView(confirmPinLabel)

        val confirmPinInput = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Confirm new PIN"
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(confirmPinInput)

        builder.setView(container)

        builder.setPositiveButton("Update") { dialog, _ ->
            val currentPin = currentPinInput.text.toString()
            val newPin = newPinInput.text.toString()
            val confirmPin = confirmPinInput.text.toString()

            // Validation
            if (currentPin.isEmpty()) {
                Toast.makeText(this, "Please enter current PIN", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (currentPin != userManager.getPin()) {
                Toast.makeText(this, "Current PIN is incorrect", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (newPin.isEmpty()) {
                Toast.makeText(this, "Please enter new PIN", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (newPin.length != 4) {
                Toast.makeText(this, "PIN must be exactly 4 digits", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (newPin != confirmPin) {
                Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // Update PIN
            userManager.setPin(newPin)
            Toast.makeText(this, "PIN updated successfully!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun showChangeUsernameDialog(usernameView: TextView) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change Username")

        val input = EditText(this).apply {
            setText(userManager.getUsername())
            setSelection(text.length)
        }

        builder.setView(input)

        builder.setPositiveButton("Update") { dialog, _ ->
            val newUsername = input.text.toString().trim()

            if (newUsername.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (newUsername.length < 3) {
                Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // Update username
            userManager.updateUsername(newUsername)
            usernameView.text = newUsername
            Toast.makeText(this, "Username updated successfully!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout? This will clear your session.")

        builder.setPositiveButton("Yes") { dialog, _ ->
            // Clear all user data
            userManager.logout()
            userManager.clearAllData()

            // Navigate to Login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
}
