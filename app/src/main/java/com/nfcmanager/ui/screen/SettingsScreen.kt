package com.nfcmanager.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nfcmanager.R
import com.nfcmanager.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val nfcStatus by viewModel.nfcStatus.collectAsState()
    
    var autoScanEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var backupFrequency by remember { mutableStateOf("姣忓懆") }
    var dataRetention by remember { mutableStateOf("30澶?) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "杩斿洖")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // NFC璁剧疆
            SettingsCategory(title = "NFC璁剧疆")
            
            SettingsItem(
                icon = Icons.Filled.Nfc,
                title = "NFC鐘舵€?,
                subtitle = when (nfcStatus) {
                    com.nfcmanager.nfc.NFCManager.NFCStatus.ENABLED -> "宸插惎鐢?
                    com.nfcmanager.nfc.NFCManager.NFCStatus.DISABLED -> "鏈惎鐢?
                    com.nfcmanager.nfc.NFCManager.NFCStatus.NOT_SUPPORTED -> "涓嶆敮鎸?
                },
                trailing = {
                    Chip(
                        onClick = {},
                        label = {
                            Text(
                                when (nfcStatus) {
                                    com.nfcmanager.nfc.NFCManager.NFCStatus.ENABLED -> "姝ｅ父"
                                    com.nfcmanager.nfc.NFCManager.NFCStatus.DISABLED -> "璀﹀憡"
                                    com.nfcmanager.nfc.NFCManager.NFCStatus.NOT_SUPPORTED -> "閿欒"
                                }
                            )
                        },
                        colors = ChipDefaults.chipColors(
                            containerColor = when (nfcStatus) {
                                com.nfcmanager.nfc.NFCManager.NFCStatus.ENABLED -> Color.Green.copy(alpha = 0.1f)
                                com.nfcmanager.nfc.NFCManager.NFCStatus.DISABLED -> Color.Yellow.copy(alpha = 0.1f)
                                com.nfcmanager.nfc.NFCManager.NFCStatus.NOT_SUPPORTED -> Color.Red.copy(alpha = 0.1f)
                            },
                            labelColor = when (nfcStatus) {
                                com.nfcmanager.nfc.NFCManager.NFCStatus.ENABLED -> Color.Green
                                com.nfcmanager.nfc.NFCManager.NFCStatus.DISABLED -> Color(0xFFF57C00)
                                com.nfcmanager.nfc.NFCManager.NFCStatus.NOT_SUPPORTED -> Color.Red
                            }
                        )
                    )
                }
            )
            
            SettingsItem(
                icon = Icons.Filled.AutoAwesome,
                title = stringResource(R.string.auto_scan),
                subtitle = "鑷姩妫€娴嬪苟璇诲彇NFC鏍囩",
                trailing = {
                    Switch(
                        checked = autoScanEnabled,
                        onCheckedChange = { autoScanEnabled = it }
                    )
                }
            )
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // 閫氱煡璁剧疆
            SettingsCategory(title = "閫氱煡璁剧疆")
            
            SettingsItem(
                icon = Icons.Filled.Vibration,
                title = "鎸姩鍙嶉",
                subtitle = "鎵弿鎴愬姛鏃舵尟鍔?,
                trailing = {
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it }
                    )
                }
            )
            
            SettingsItem(
                icon = Icons.Filled.VolumeUp,
                title = "澹伴煶鎻愮ず",
                subtitle = "鎵弿鎴愬姛鏃舵挱鏀炬彁绀洪煶",
                trailing = {
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                }
            )
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // 鏁版嵁绠＄悊
            SettingsCategory(title = "鏁版嵁绠＄悊")
            
            SettingsItem(
                icon = Icons.Filled.Backup,
                title = stringResource(R.string.backup),
                subtitle = "鑷姩澶囦唤棰戠巼",
                trailing = {
                    Text(backupFrequency)
                },
                onClick = {
                    // 鎵撳紑澶囦唤棰戠巼閫夋嫨
                }
            )
            
            SettingsItem(
                icon = Icons.Filled.Restore,
                title = stringResource(R.string.restore),
                subtitle = "浠庡浠芥仮澶嶆暟鎹?,
                trailing = {},
                onClick = {
                    // 鎵撳紑鎭㈠鐣岄潰
                }
            )
            
            SettingsItem(
                icon = Icons.Filled.Delete,
                title = "鏁版嵁淇濈暀",
                subtitle = "鑷姩娓呯悊鏃ф暟鎹?,
                trailing = {
                    Text(dataRetention)
                },
                onClick = {
                    // 鎵撳紑鏁版嵁淇濈暀璁剧疆
                }
            )
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // 鍏朵粬璁剧疆
            SettingsCategory(title = "鍏朵粬")
            
            SettingsItem(
                icon = Icons.Filled.Info,
                title = stringResource(R.string.about),
                subtitle = "鐗堟湰淇℃伅鍜屼娇鐢ㄨ鏄?,
                trailing = {},
                onClick = {
                    // 鎵撳紑鍏充簬椤甸潰
                }
            )
            
            SettingsItem(
                icon = Icons.Filled.Help,
                title = stringResource(R.string.help),
                subtitle = "甯歌闂鍜屼娇鐢ㄦ暀绋?,
                trailing = {},
                onClick = {
                    // 鎵撳紑甯姪椤甸潰
                }
            )
            
            SettingsItem(
                icon = Icons.Filled.Share,
                title = "鍒嗕韩搴旂敤",
                subtitle = "鎺ㄨ崘缁欐湅鍙?,
                trailing = {},
                onClick = {
                    // 鍒嗕韩搴旂敤
                }
            )
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // 鍗遍櫓鍖哄煙
            SettingsCategory(title = "鍗遍櫓鎿嶄綔", color = Color.Red)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Red.copy(alpha = 0.1f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "娓呯┖鎵€鏈夋暟鎹?,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "杩欏皢鍒犻櫎鎵€鏈変繚瀛樼殑NFC璁板綍锛屾搷浣滀笉鍙仮澶?,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = {
                            // 娓呯┖鏁版嵁
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("娓呯┖鎵€鏈夋暟鎹?)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 搴旂敤淇℃伅
            Text(
                text = "NFC绠＄悊鍣?v1.0.0",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Text(
                text = "漏 2025 NFC Manager Team",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun SettingsCategory(
    title: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val itemModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }
    
    ListItem(
        modifier = itemModifier,
        headlineContent = {
            Text(
                text = title,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = trailing
    )
}