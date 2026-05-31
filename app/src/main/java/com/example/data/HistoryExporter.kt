package com.example.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object HistoryExporter {

    sealed class ExportResult {
        data class Success(val filePath: String, val uri: Uri?) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }

    enum class ExportFormat {
        TXT, CSV, JSON
    }

    fun export(context: Context, history: List<HistoryItem>, format: ExportFormat): ExportResult {
        if (history.isEmpty()) {
            return ExportResult.Error("No history available to export.")
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Calculator_History_$timestamp.${format.name.lowercase()}"
        val mimeType = when (format) {
            ExportFormat.TXT -> "text/plain"
            ExportFormat.CSV -> "text/csv"
            ExportFormat.JSON -> "application/json"
        }

        val fileContent = generateFileContent(history, format)

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Modern Scoped Storage write
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        out.write(fileContent.toByteArray())
                    }
                    ExportResult.Success("Downloads / $fileName", uri)
                } else {
                    ExportResult.Error("Failed to initiate folder entry in MediaStore.")
                }
            } else {
                // Legacy Direct Storage write (Safe fallback for minSdk 24-28)
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { out ->
                    out.write(fileContent.toByteArray())
                }
                val uri = try {
                    FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                } catch (e: Exception) {
                    Uri.fromFile(file)
                }
                ExportResult.Success(file.absolutePath, uri)
            }
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "An unhandled error occurred during saving.")
        }
    }

    private fun generateFileContent(history: List<HistoryItem>, format: ExportFormat): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return when (format) {
            ExportFormat.TXT -> buildString {
                append("========================================\n")
                append("       CALCULATION HISTORY REPORT       \n")
                append("      Generated: ${dateFormat.format(Date())} \n")
                append("========================================\n\n")
                history.forEachIndexed { idx, item ->
                    append(String.format("%03d. [%s] [%s]\n", idx + 1, dateFormat.format(Date(item.timestamp)), if (item.isScientific) "Scientific" else "Basic"))
                    append("     Expression: ${item.expression}\n")
                    append("     Result:     ${item.result}\n")
                    append("     Favorite:   ${if (item.isFavorite) "Yes" else "No"}\n")
                    append("----------------------------------------\n")
                }
            }
            ExportFormat.CSV -> buildString {
                append("ID,Timestamp,Type,Expression,Result,Favorite\n")
                history.forEachIndexed { idx, item ->
                    val cleanExpr = item.expression.replace("\"", "\"\"")
                    val cleanRes = item.result.replace("\"", "\"\"")
                    append(String.format(
                        "\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        idx + 1,
                        dateFormat.format(Date(item.timestamp)),
                        if (item.isScientific) "Scientific" else "Basic",
                        "\"$cleanExpr\"",
                        "\"$cleanRes\"",
                        if (item.isFavorite) "Yes" else "No"
                    ))
                }
            }
            ExportFormat.JSON -> buildString {
                append("[\n")
                history.forEachIndexed { idx, item ->
                    append("  {\n")
                    append("    \"id\": ${idx + 1},\n")
                    append("    \"timestamp\": \"${dateFormat.format(Date(item.timestamp))}\",\n")
                    append("    \"type\": \"${if (item.isScientific) "Scientific" else "Basic"}\",\n")
                    append("    \"expression\": \"${item.expression.replace("\"", "\\\"")}\",\n")
                    append("    \"result\": \"${item.result.replace("\"", "\\\"")}\",\n")
                    append("    \"isFavorite\": ${item.isFavorite}\n")
                    append("  }${if (idx < history.size - 1) "," else ""}\n")
                }
                append("]\n")
            }
        }
    }

    fun shareFile(context: Context, path: String, uri: Uri?, format: ExportFormat) {
        val mimeType = when (format) {
            ExportFormat.TXT -> "text/plain"
            ExportFormat.CSV -> "text/csv"
            ExportFormat.JSON -> "application/json"
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            if (uri != null) {
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                val file = File(path)
                if (file.exists()) {
                    val fallbackUri = Uri.fromFile(file)
                    putExtra(Intent.EXTRA_STREAM, fallbackUri)
                }
            }
            putExtra(Intent.EXTRA_SUBJECT, "Calculator Calculation History")
            putExtra(Intent.EXTRA_TEXT, "Sending my exported calculator history ledger.")
        }
        context.startActivity(Intent.createChooser(intent, "Share Exported History"))
    }
}
