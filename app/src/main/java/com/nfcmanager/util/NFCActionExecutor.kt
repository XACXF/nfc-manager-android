package com.nfcmanager.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.nfcmanager.R
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.data.model.NFCType
import java.net.URLEncoder

class NFCActionExecutor(private val context: Context) {
    
    fun execute(nfcData: NFCData): Boolean {
        // 如果有AAR包名，优先用该包名打开
        if (!nfcData.aarPackage.isNullOrEmpty()) {
            return openWithPackage(nfcData.content, nfcData.aarPackage)
        }
        
        return when (nfcData.type) {
            NFCType.URL -> openUrl(nfcData.content)
            NFCType.PHONE -> dialPhone(nfcData.content)
            NFCType.EMAIL -> sendEmail(nfcData.content)
            NFCType.GEO -> openMap(nfcData.content)
            NFCType.WIFI -> connectWifi(nfcData.content)
            NFCType.TEXT -> {
                // 智能识别：如果内容看起来像电话号码，则拨打电话
                val content = nfcData.content.trim()
                if (content.matches(Regex("^[+]?[0-9\\s\\-()]{7,15}$"))) {
                    dialPhone(content)
                } else if (content.contains("@") && content.contains(".")) {
                    sendEmail(content)
                } else if (content.startsWith("http://") || content.startsWith("https://") || 
                           content.matches(Regex("^[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\\.?$"))) {
                    openUrl(content)
                } else {
                    copyToClipboard(content)
                }
            }
            NFCType.VCARD -> importContact(nfcData.content)
            NFCType.APP -> openApp(nfcData.content)
            NFCType.UNKNOWN -> {
                // 对于未知类型，也尝试智能识别
                val content = nfcData.content.trim()
                if (content.startsWith("http://") || content.startsWith("https://")) {
                    openUrl(content)
                } else if (content.startsWith("tel:")) {
                    dialPhone(content.removePrefix("tel:"))
                } else if (content.startsWith("mailto:")) {
                    sendEmail(content.removePrefix("mailto:"))
                } else if (content.startsWith("geo:")) {
                    openMap(content)
                } else if (content.startsWith("WIFI:")) {
                    connectWifi(content)
                } else {
                    copyToClipboard(content)
                }
            }
        }
    }
    
    /**
     * 用指定包名打开链接（支持AAR）
     */
    private fun openWithPackage(url: String, packageName: String): Boolean {
        return try {
            val uri = if (url.startsWith("http://") || url.startsWith("https://")) {
                Uri.parse(url)
            } else {
                Uri.parse("https://$url")
            }
            
            // 用指定包名打开
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(intent)
            Toast.makeText(context, "正在用 $packageName 打开...", Toast.LENGTH_SHORT).show()
            Log.d("NFCActionExecutor", "Opening with package: $packageName")
            true
        } catch (e: Exception) {
            Log.e("NFCActionExecutor", "Failed to open with package: $packageName", e)
            // 降级为普通打开方式
            Toast.makeText(context, "指定应用未安装，尝试其他方式...", Toast.LENGTH_SHORT).show()
            openUrl(url)
        }
    }
    
    private fun openUrl(url: String): Boolean {
        return try {
            val uri = if (url.startsWith("http://") || url.startsWith("https://")) {
                Uri.parse(url)
            } else {
                Uri.parse("https://$url")
            }
            
            // 检测光遇徽章链接，用光遇APP打开
            if (url.contains("sky.thatg.co") || url.contains("skygame.com")) {
                return openWithSkyGame(url)
            }
            
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(context, context.getString(R.string.opening_url), Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.error_opening_url), Toast.LENGTH_SHORT).show()
            false
        }
    }
    
    /**
     * 用光遇APP打开链接
     */
    private fun openWithSkyGame(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            
            // 尝试用光遇APP打开
            val skyPackages = listOf(
                "com.tgc.sky.cn",      // 光遇国服
                "com.tgc.sky.android", // 光遇国际服
                "com.netease.sky"      // 网易版（如果有）
            )
            
            var opened = false
            
            // 尝试每个可能的包名
            for (packageName in skyPackages) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage(packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    Toast.makeText(context, "正在用光遇打开...", Toast.LENGTH_SHORT).show()
                    opened = true
                    break
                } catch (e: Exception) {
                    // 这个包名不存在，继续尝试下一个
                    continue
                }
            }
            
            // 如果没有安装光遇，打开应用商店
            if (!opened) {
                try {
                    val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=光遇"))
                    storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(storeIntent)
                    Toast.makeText(context, "未安装光遇，正在打开应用商店...", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    // 降级为浏览器打开
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    Toast.makeText(context, "请先安装光遇游戏", Toast.LENGTH_SHORT).show()
                }
            }
            
            true
        } catch (e: Exception) {
            Toast.makeText(context, "打开光遇失败: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
    
    private fun dialPhone(phone: String): Boolean {
        return try {
            val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
            val uri = Uri.parse("tel:$cleanPhone")
            val intent = Intent(Intent.ACTION_DIAL, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(context, context.getString(R.string.dialing_phone), Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.error_dialing_phone), Toast.LENGTH_SHORT).show()
            false
        }
    }
    
    private fun sendEmail(email: String): Boolean {
        return try {
            val uri = Uri.parse("mailto:$email")
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(context, context.getString(R.string.opening_email), Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.error_opening_email), Toast.LENGTH_SHORT).show()
            false
        }
    }
    
    private fun openMap(geo: String): Boolean {
        return try {
            // Parse geo:lat,lon format
            val uri = if (geo.startsWith("geo:")) {
                Uri.parse(geo)
            } else {
                // Try to parse as lat,lon
                val parts = geo.split(",")
                if (parts.size == 2) {
                    Uri.parse("geo:${parts[0].trim()},${parts[1].trim()}")
                } else {
                    Uri.parse("geo:0,0?q=${URLEncoder.encode(geo, "UTF-8")}")
                }
            }
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(context, context.getString(R.string.opening_map), Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.error_opening_map), Toast.LENGTH_SHORT).show()
            false
        }
    }
    
    private fun connectWifi(wifiData: String): Boolean {
        return try {
            // Parse WiFi config: WIFI:S:ssid;T:WPA;P:password;;
            val ssid = extractWifiField(wifiData, "S")
            val password = extractWifiField(wifiData, "P")
            val authType = extractWifiField(wifiData, "T")
            
            if (ssid.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.error_connecting_wifi), Toast.LENGTH_SHORT).show()
                return false
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val suggestionBuilder = WifiNetworkSuggestion.Builder()
                    .setSsid(ssid)
                
                // Only set password if provided
                if (password.isNotEmpty()) {
                    when (authType.uppercase()) {
                        "WPA", "WPA2" -> suggestionBuilder.setWpa2Passphrase(password)
                        "WEP" -> suggestionBuilder.setWpa2Passphrase(password) // WEP not directly supported
                        else -> suggestionBuilder.setWpa2Passphrase(password)
                    }
                }
                
                val suggestion = suggestionBuilder.build()
                wifiManager.addNetworkSuggestions(listOf(suggestion))
                Toast.makeText(context, context.getString(R.string.wifi_suggestion_added), Toast.LENGTH_SHORT).show()
            } else {
                // Open WiFi settings for older versions
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Toast.makeText(context, context.getString(R.string.open_wifi_settings), Toast.LENGTH_SHORT).show()
            }
            true
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.error_connecting_wifi), Toast.LENGTH_SHORT).show()
            false
        }
    }
    
    private fun extractWifiField(data: String, field: String): String {
        val regex = Regex("$field:([^;]*)")
        return regex.find(data)?.groupValues?.get(1) ?: ""
    }
    
    private fun copyToClipboard(text: String): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("NFC Data", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.error_copying), Toast.LENGTH_SHORT).show()
            false
        }
    }
    
    private fun importContact(vcard: String): Boolean {
        return try {
            val intent = Intent(ContactsContract.Intents.Insert.ACTION)
            intent.type = ContactsContract.RawContacts.CONTENT_TYPE
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // Parse vCard and add contact info
            // For simplicity, just open contacts app
            context.startActivity(intent)
            Toast.makeText(context, context.getString(R.string.opening_contacts), Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.error_importing_contact), Toast.LENGTH_SHORT).show()
            false
        }
    }
    
    private fun openApp(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Toast.makeText(context, context.getString(R.string.opening_app), Toast.LENGTH_SHORT).show()
                true
            } else {
                // Open Play Store if app not installed
                val uri = Uri.parse("market://details?id=$packageName")
                val storeIntent = Intent(Intent.ACTION_VIEW, uri)
                storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(storeIntent)
                Toast.makeText(context, context.getString(R.string.app_not_installed), Toast.LENGTH_SHORT).show()
                true
            }
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.error_opening_app), Toast.LENGTH_SHORT).show()
            false
        }
    }
    
    fun getActionDescription(type: NFCType): String {
        return when (type) {
            NFCType.URL -> context.getString(R.string.action_open_url)
            NFCType.PHONE -> context.getString(R.string.action_dial_phone)
            NFCType.EMAIL -> context.getString(R.string.action_send_email)
            NFCType.GEO -> context.getString(R.string.action_open_map)
            NFCType.WIFI -> context.getString(R.string.action_connect_wifi)
            NFCType.TEXT -> context.getString(R.string.action_copy_text)
            NFCType.VCARD -> context.getString(R.string.action_import_contact)
            NFCType.APP -> context.getString(R.string.action_open_app)
            NFCType.UNKNOWN -> context.getString(R.string.action_view)
        }
    }
}
