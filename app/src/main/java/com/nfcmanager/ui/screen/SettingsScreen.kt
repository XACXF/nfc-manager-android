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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nfcmanager.R
import com.nfcmanager.nfc.NFCManager
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
            SettingsCategory(title = stringResource(R.string.nfc_settings))
            
            SettingsItem(
                icon = Icons.Filled.Nfc,
                title = stringResource(R.string.nfc_status),
                subtitle = when (nfcStatus) {
                    NFCManager.NFCStatus.ENABLED -> stringResource(R.string.nfc_enabled)
                    NFCManager.NFCStatus.DISABLED -> stringResource(R.string.nfc_disabled)
                    NFCManager.NFCStatus.NOT_SUPPORTED -> stringResource(R.string.nfc_not_supported)
                }
            )
            
            SettingsItem(
                icon = Icons.Filled.AutoAwesome,
                title = stringResource(R.string.auto_scan),
                subtitle = stringResource(R.string.auto_scan_desc),
                trailing = {
                    Switch(
                        checked = autoScanEnabled,
                        onCheckedChange = { autoScanEnabled = it }
                    )
                }
            )
            
            Divider()
            
            SettingsCategory(title = stringResource(R.string.notifications))
            
            SettingsItem(
                icon = Icons.Filled.Vibration,
                title = stringResource(R.string.vibration),
                subtitle = stringResource(R.string.vibration_desc),
                trailing = {
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it }
                    )
                }
            )
            
            SettingsItem(
                icon = Icons.Filled.VolumeUp,
                title = stringResource(R.string.sound),
                subtitle = stringResource(R.string.sound_desc),
                trailing = {
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                }
            )
            
            Divider()
            
            SettingsCategory(title = stringResource(R.string.data_management_title))
            
            SettingsItem(
                icon = Icons.Filled.Backup,
                title = stringResource(R.string.backup),
                subtitle = stringResource(R.string.backup_desc)
            )
            
            SettingsItem(
                icon = Icons.Filled.Restore,
                title = stringResource(R.string.restore),
                subtitle = stringResource(R.string.restore_desc)
            )
            
            Divider()
            
            SettingsCategory(title = stringResource(R.string.other))
            
            SettingsItem(
                icon = Icons.Filled.Info,
                title = stringResource(R.string.about),
                subtitle = stringResource(R.string.about_desc)
            )
            
            SettingsItem(
                icon = Icons.Filled.Help,
                title = stringResource(R.string.help),
                subtitle = stringResource(R.string.help_desc)
            )
            
            Divider()
            
            SettingsCategory(title = stringResource(R.string.danger_zone), color = Color.Red)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Red.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.clear_all_data),
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = stringResource(R.string.clear_all_data_msg),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = {},
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.clear_all_data))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = stringResource(R.string.app_version),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Text(
                text = stringResource(R.string.copyright),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                textAlign = TextAlign.Center,
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
    trailing: @Composable () -> Unit = {}
) {
    ListItem(
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
