package com.example.spendsense.budgetplan.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val category: String,
    val date: String,
    val description: String,
    val type: String // "CASH_IN" or "CASH_OUT"
)
