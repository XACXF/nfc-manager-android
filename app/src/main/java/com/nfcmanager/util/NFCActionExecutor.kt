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
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.nfcmanager.R
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.data.model.NFCType
import java.net.URLEncoder

class NFCActionExecutor(private val context: Context) {
    
    fun execute(nfcData: NFCData): Boolean {
        return when (nfcData.type) {
            NFCType.URL -> openUrl(nfcData.content)
            NFCType.PHONE -> dialPhone(nfcData.content)
            NFCType.EMAIL -> sendEmail(nfcData.content)
            NFCType.GEO -> openMap(nfcData.content)
            NFCType.WIFI -> connectWifi(nfcData.content)
            NFCType.TEXT -> {
                // 鏅鸿兘璇嗗埆锛氬鏋滃唴瀹圭湅璧锋潵鍍忕數璇濆彿鐮侊紝鍒欐嫧鎵撶數璇?
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
                // 瀵逛簬鏈煡绫诲瀷锛屼篃灏濊瘯鏅鸿兘璇嗗埆
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
    
    private fun openUrl(url: String): Boolean {
        return try {
            val uri = if (url.startsWith("http://") || url.startsWith("https://")) {
                Uri.parse(url)
            } else {
                Uri.parse("https://$url")
            }
            
            // 妫€娴嬪厜閬囧窘绔犻摼鎺ワ紝鐢ㄥ厜閬嘇PP鎵撳紑
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
     * 鐢ㄥ厜閬嘇PP鎵撳紑閾炬帴
     */
    private fun openWithSkyGame(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            
            // 灏濊瘯鐢ㄥ厜閬嘇PP鎵撳紑
            val skyPackages = listOf(
                "com.tgc.sky.cn",      // 鍏夐亣鍥芥湇
                "com.tgc.sky.android", // 鍏夐亣鍥介檯鏈?
                "com.netease.sky"      // 缃戞槗鐗堬紙濡傛灉鏈夛級
            )
            
            var opened = false
            
            // 灏濊瘯姣忎釜鍙兘鐨勫寘鍚?
            for (packageName in skyPackages) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage(packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    Toast.makeText(context, "姝ｅ湪鐢ㄥ厜閬囨墦寮€...", Toast.LENGTH_SHORT).show()
                    opened = true
                    break
                } catch (e: Exception) {
                    // 杩欎釜鍖呭悕涓嶅瓨鍦紝缁х画灏濊瘯涓嬩竴涓?
                    continue
                }
            }
            
            // 濡傛灉娌℃湁瀹夎鍏夐亣锛屾墦寮€搴旂敤鍟嗗簵
            if (!opened) {
                try {
                    val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=鍏夐亣"))
                    storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(storeIntent)
                    Toast.makeText(context, "鏈畨瑁呭厜閬囷紝姝ｅ湪鎵撳紑搴旂敤鍟嗗簵...", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    // 闄嶇骇涓烘祻瑙堝櫒鎵撳紑
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    Toast.makeText(context, "璇峰厛瀹夎鍏夐亣娓告垙", Toast.LENGTH_SHORT).show()
                }
            }
            
            true
        } catch (e: Exception) {
            Toast.makeText(context, "鎵撳紑鍏夐亣澶辫触: ${e.message}", Toast.LENGTH_SHORT).show()
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
