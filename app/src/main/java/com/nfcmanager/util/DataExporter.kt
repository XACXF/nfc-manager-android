package com.nfcmanager.util

import android.content.Context
import android.content.Intent
import com.nfcmanager.R
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.data.model.NFCType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * NFC数据导出工具类
 * 支持导出为CSV和TXT格式
 */
class DataExporter(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    /**
     * 导出结果
     */
    sealed class ExportResult {
        data class Success(val file: File, val message: String) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }
    
    /**
     * 导出格式
     */
    enum class ExportFormat {
        CSV, TXT
    }
    
    /**
     * 导出单条NFC数据
     */
    fun exportSingle(data: NFCData, format: ExportFormat): ExportResult {
        return try {
            val fileName = generateSingleFileName(data, format)
            val content = when (format) {
                ExportFormat.CSV -> generateSingleCsvContent(data)
                ExportFormat.TXT -> generateSingleTxtContent(data)
            }
            
            val file = saveToFile(fileName, content)
            ExportResult.Success(file, context.getString(R.string.export_success, file.name))
        } catch (e: Exception) {
            ExportResult.Error(context.getString(R.string.export_failed, e.message))
        }
    }
    
    /**
     * 导出所有NFC数据
     */
    fun exportAll(dataList: List<NFCData>, format: ExportFormat): ExportResult {
        return try {
            val fileName = generateAllFileName(format)
            val content = when (format) {
                ExportFormat.CSV -> generateAllCsvContent(dataList)
                ExportFormat.TXT -> generateAllTxtContent(dataList)
            }
            
            val file = saveToFile(fileName, content)
            ExportResult.Success(file, context.getString(R.string.export_success, file.name))
        } catch (e: Exception) {
            ExportResult.Error(context.getString(R.string.export_failed, e.message))
        }
    }
    
    /**
     * 生成单条数据的文件名
     */
    private fun generateSingleFileName(data: NFCData, format: ExportFormat): String {
        val name = if (data.name.isNotBlank()) {
            data.name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        } else {
            "${data.type.name}_${fileDateFormat.format(data.readTime)}"
        }
        val extension = format.name.lowercase()
        return "nfc_${name}.$extension"
    }
    
    /**
     * 生成批量导出的文件名
     */
    private fun generateAllFileName(format: ExportFormat): String {
        val timestamp = fileDateFormat.format(Date())
        val extension = format.name.lowercase()
        return "nfc_all_$timestamp.$extension"
    }
    
    /**
     * 生成单条CSV内容
     */
    private fun generateSingleCsvContent(data: NFCData): String {
        val builder = StringBuilder()
        builder.appendLine(context.getString(R.string.export_csv_header))
        builder.appendLine(formatCsvRow(data))
        return builder.toString()
    }
    
    /**
     * 生成单条TXT内容
     */
    private fun generateSingleTxtContent(data: NFCData): String {
        val builder = StringBuilder()
        builder.appendLine("═══════════════════════════════════")
        builder.appendLine("${context.getString(R.string.custom_name)}: ${data.name.ifBlank { "-" }}")
        builder.appendLine("${context.getString(R.string.export_field_type)}: ${getTypeName(data.type)}")
        builder.appendLine("${context.getString(R.string.content)}: ${data.content}")
        builder.appendLine("${context.getString(R.string.time)}: ${dateFormat.format(data.readTime)}")
        builder.appendLine("${context.getString(R.string.note)}: ${data.note.ifBlank { "-" }}")
        builder.appendLine("═══════════════════════════════════")
        return builder.toString()
    }
    
    /**
     * 生成批量CSV内容
     */
    private fun generateAllCsvContent(dataList: List<NFCData>): String {
        val builder = StringBuilder()
        builder.appendLine(context.getString(R.string.export_csv_header))
        dataList.forEach { data ->
            builder.appendLine(formatCsvRow(data))
        }
        return builder.toString()
    }
    
    /**
     * 生成批量TXT内容
     */
    private fun generateAllTxtContent(dataList: List<NFCData>): String {
        val builder = StringBuilder()
        builder.appendLine("╔══════════════════════════════════════╗")
        builder.appendLine("║     NFC ${context.getString(R.string.export_data_list)} - 共 ${dataList.size} 条     ║")
        builder.appendLine("║     导出时间: ${dateFormat.format(Date())}     ║")
        builder.appendLine("╚══════════════════════════════════════╝")
        builder.appendLine()
        
        dataList.forEachIndexed { index, data ->
            builder.appendLine("【${index + 1}】")
            builder.appendLine("${context.getString(R.string.custom_name)}: ${data.name.ifBlank { "-" }}")
            builder.appendLine("${context.getString(R.string.export_field_type)}: ${getTypeName(data.type)}")
            builder.appendLine("${context.getString(R.string.content)}: ${data.content}")
            builder.appendLine("${context.getString(R.string.time)}: ${dateFormat.format(data.readTime)}")
            builder.appendLine("${context.getString(R.string.note)}: ${data.note.ifBlank { "-" }}")
            builder.appendLine("─────────────────────────────────────")
        }
        return builder.toString()
    }
    
    /**
     * 格式化CSV行
     */
    private fun formatCsvRow(data: NFCData): String {
        val name = data.name.ifBlank { "-" }.replace("\"", "\"\"")
        val content = data.content.replace("\"", "\"\"")
        val note = data.note.ifBlank { "-" }.replace("\"", "\"\"")
        val time = dateFormat.format(data.readTime)
        val type = getTypeName(data.type)
        
        return "\"$name\",\"$type\",\"$content\",\"$time\",\"$note\""
    }
    
    /**
     * 获取类型名称
     */
    private fun getTypeName(type: NFCType): String {
        return context.getString(when (type) {
            NFCType.TEXT -> R.string.type_text
            NFCType.URL -> R.string.type_url
            NFCType.VCARD -> R.string.type_vcard
            NFCType.PHONE -> R.string.type_phone
            NFCType.EMAIL -> R.string.type_email
            NFCType.WIFI -> R.string.type_wifi
            NFCType.GEO -> R.string.type_geo
            NFCType.APP -> R.string.type_app
            NFCType.UNKNOWN -> R.string.type_other
        })
    }
    
    /**
     * 保存到文件
     */
    private fun saveToFile(fileName: String, content: String): File {
        // 使用应用的私有目录，不需要存储权限
        val exportsDir = File(context.getExternalFilesDir(null), "exports")
        if (!exportsDir.exists()) {
            exportsDir.mkdirs()
        }
        
        val file = File(exportsDir, fileName)
        FileOutputStream(file).use { output ->
            output.write(content.toByteArray(Charsets.UTF_8))
        }
        
        return file
    }
    
    /**
     * 分享导出的文件
     */
    fun shareFile(file: File) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share)))
    }
}
