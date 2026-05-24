package com.example.expensetracker.utils

import android.content.Context
import android.os.Environment
import com.example.expensetracker.data.Expense
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {
    fun exportExpensesToCsv(expenses: List<Expense>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val csvHeader = "ID,Amount,Category,Date,Note\n"
        val csvBody = expenses.joinToString("\n") { expense ->
            "${expense.id},${expense.amount},${expense.category},${dateFormat.format(Date(expense.date))},${expense.note.replace(",", " ")}"
        }
        return csvHeader + csvBody
    }

    fun saveCsvToFile(context: Context, csvContent: String): File? {
        val fileName = "expenses_export_${System.currentTimeMillis()}.csv"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        return try {
            FileOutputStream(file).use { out ->
                out.write(csvContent.toByteArray())
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
