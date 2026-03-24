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
    var backupFrequency by remember { mutableStateOf("每周") }
    var dataRetention by remember { mutableStateOf("30天") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
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
            // NFC设置
            SettingsCategory(title = "NFC设置")
            
            SettingsItem(
                icon = Icons.Filled.Nfc,
                title = "NFC状态",
                subtitle = when (nfcStatus) {
                    com.nfcmanager.nfc.NFCManager.NFCStatus.ENABLED -> "已启用"
                    com.nfcmanager.nfc.NFCManager.NFCStatus.DISABLED -> "未启用"
                    com.nfcmanager.nfc.NFCManager.NFCStatus.NOT_SUPPORTED -> "不支持"
                },
                trailing = {
                    Chip(
                        onClick = {},
                        label = {
                            Text(
                                when (nfcStatus) {
                                    com.nfcmanager.nfc.NFCManager.NFCStatus.ENABLED -> "正常"
                                    com.nfcmanager.nfc.NFCManager.NFCStatus.DISABLED -> "警告"
                                    com.nfcmanager.nfc.NFCManager.NFCStatus.NOT_SUPPORTED -> "错误"
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
                subtitle = "自动检测并读取NFC标签",
                trailing = {
                    Switch(
                        checked = autoScanEnabled,
                        onCheckedChange = { autoScanEnabled = it }
                    )
                }
            )
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // 通知设置
            SettingsCategory(title = "通知设置")
            
            SettingsItem(
                icon = Icons.Filled.Vibration,
                title = "振动反馈",
                subtitle = "扫描成功时振动",
                trailing = {
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it }
                    )
                }
            )
            
            SettingsItem(
                icon = Icons.Filled.VolumeUp,
                title = "声音提示",
                subtitle = "扫描成功时播放提示音",
                trailing = {
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                }
            )
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // 数据管理
            SettingsCategory(title = "数据管理")
            
            SettingsItem(
                icon = Icons.Filled.Backup,
                title = stringResource(R.string.backup),
                subtitle = "自动备份频率",
                trailing = {
                    Text(backupFrequency)
                },
                onClick = {
                    // 打开备份频率选择
                }
            )
            
            SettingsItem(
                icon = Icons.Filled.Restore,
                title = stringResource(R.string.restore),
                subtitle = "从备份恢复数据",
                trailing = {},
                onClick = {
                    // 打开恢复界面
                }
            )
            
            SettingsItem(
                icon = Icons.Filled.Delete,
                title = "数据保留",
                subtitle = "自动清理旧数据",
                trailing = {
                    Text(dataRetention)
                },
                onClick = {
                    // 打开数据保留设置
                }
            )
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // 其他设置
            SettingsCategory(title = "其他")
            
            SettingsItem(
                icon = Icons.Filled.Info,
                title = stringResource(R.string.about),
                subtitle = "版本信息和使用说明",
                trailing = {},
                onClick = {
                    // 打开关于页面
                }
            )
            
            SettingsItem(
                icon = Icons.Filled.Help,
                title = stringResource(R.string.help),
                subtitle = "常见问题和使用教程",
                trailing = {},
                onClick = {
                    // 打开帮助页面
                }
            )
            
            SettingsItem(
                icon = Icons.Filled.Share,
                title = "分享应用",
                subtitle = "推荐给朋友",
                trailing = {},
                onClick = {
                    // 分享应用
                }
            )
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // 危险区域
            SettingsCategory(title = "危险操作", color = Color.Red)
            
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
                        text = "清空所有数据",
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "这将删除所有保存的NFC记录，操作不可恢复",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = {
                            // 清空数据
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("清空所有数据")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 应用信息
            Text(
                text = "NFC管理器 v1.0.0",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Text(
                text = "© 2025 NFC Manager Team",
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