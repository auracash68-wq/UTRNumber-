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

    enum class ExportFormat {
        CSV, JSON, TXT
    }

    sealed class ExportResult {
        data class Success(val filePath: String, val uri: Uri) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }

    fun export(context: Context, historyList: List<HistoryItem>, format: ExportFormat): ExportResult {
        return try {
            val fileName = "calculator_history_${System.currentTimeMillis()}.${format.name.lowercase()}"
            val content = when (format) {
                ExportFormat.CSV -> generateCsv(historyList)
                ExportFormat.JSON -> generateJson(historyList)
                ExportFormat.TXT -> generateTxt(historyList)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(format))
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return ExportResult.Error("Could not create MediaStore entry")
                
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                } ?: return ExportResult.Error("Could not open output stream")

                ExportResult.Success("${Environment.DIRECTORY_DOWNLOADS}/$fileName", uri)
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { fos ->
                    fos.write(content.toByteArray())
                }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                ExportResult.Success(file.absolutePath, uri)
            }
        } catch (e: Exception) {
            ExportResult.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    fun shareFile(context: Context, filePath: String, uri: Uri, format: ExportFormat) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = getMimeType(format)
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Calculator Logs Export")
            putExtra(Intent.EXTRA_TEXT, "Here is my exported calculation history from Aura Calculator.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Calculation Logs"))
    }

    private fun getMimeType(format: ExportFormat): String {
        return when (format) {
            ExportFormat.CSV -> "text/csv"
            ExportFormat.JSON -> "application/json"
            ExportFormat.TXT -> "text/plain"
        }
    }

    private fun generateCsv(historyList: List<HistoryItem>): String {
        val sb = java.lang.StringBuilder()
        sb.append("ID,Expression,Result,Scientific,Favorite,Timestamp\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        for (item in historyList) {
            val dateStr = dateFormat.format(Date(item.timestamp))
            val exprEscaped = item.expression.replace("\"", "\"\"")
            val resEscaped = item.result.replace("\"", "\"\"")
            sb.append("${item.id},\"$exprEscaped\",\"$resEscaped\",${item.isScientific},${item.isFavorite},\"$dateStr\"\n")
        }
        return sb.toString()
    }

    private fun generateJson(historyList: List<HistoryItem>): String {
        val sb = java.lang.StringBuilder()
        sb.append("[\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        historyList.forEachIndexed { index, item ->
            val dateStr = dateFormat.format(Date(item.timestamp))
            val exprEscaped = item.expression.replace("\"", "\\\"").replace("\n", " ")
            val resEscaped = item.result.replace("\"", "\\\"").replace("\n", " ")
            sb.append("  {\n")
            sb.append("    \"id\": ${item.id},\n")
            sb.append("    \"expression\": \"$exprEscaped\",\n")
            sb.append("    \"result\": \"$resEscaped\",\n")
            sb.append("    \"isScientific\": ${item.isScientific},\n")
            sb.append("    \"isFavorite\": ${item.isFavorite},\n")
            sb.append("    \"timestamp\": \"$dateStr\"\n")
            sb.append("  }")
            if (index < historyList.size - 1) {
                sb.append(",")
            }
            sb.append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun generateTxt(historyList: List<HistoryItem>): String {
        val sb = java.lang.StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        for (item in historyList) {
            val dateStr = dateFormat.format(Date(item.timestamp))
            sb.append("[Date: $dateStr]  ${item.expression} = ${item.result}  (Fav: ${item.isFavorite})\n")
        }
        return sb.toString()
    }
}
