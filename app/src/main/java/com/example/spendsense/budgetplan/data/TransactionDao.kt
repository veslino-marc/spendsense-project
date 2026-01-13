package com.example.spendsense.budgetplan.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'CASH_IN'")
    fun getTotalCashIn(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'CASH_OUT'")
    fun getTotalCashOut(): Flow<Double?>
}
