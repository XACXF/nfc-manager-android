package com.nfcmanager.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nfcmanager.R
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.nfc.NFCManager
import com.nfcmanager.ui.component.NFCScanningAnimation
import com.nfcmanager.ui.component.NFCStatusCard
import com.nfcmanager.viewmodel.MainViewModel
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadScreen(
    onBack: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val nfcStatus by viewModel.nfcStatus.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    var nameText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    
    // 监听扫描结果变化
    LaunchedEffect(scanResult) {
        // 当有新的扫描结果时，可以执行一些操作
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.read_nfc)) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NFCStatusCard(status = nfcStatus)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            NFCScanningAnimation(isScanning = isScanning)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (isScanning) {
                    stringResource(R.string.scanning)
                } else {
                    stringResource(R.string.place_nfc_tag_detail)
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.keep_tag_close),
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 真正的NFC扫描不需要点击按钮，但保留一个手动触发按钮
            // 提示用户只需将手机靠近NFC标签
            if (nfcStatus == NFCManager.NFCStatus.ENABLED && !isScanning && scanResult == null) {
                FilledTonalButton(
                    onClick = { viewModel.startScanning() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Nfc, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.ready_to_scan))
                }
            }
            
            // 显示扫描结果
            scanResult?.let { result ->
                Spacer(modifier = Modifier.height(32.dp))
                
                when (result) {
                    is NFCManager.NFCReadResult.Success -> {
                        ScanResultCard(
                            nfcData = result.data,
                            nameText = nameText,
                            noteText = noteText,
                            onNameTextChange = { nameText = it },
                            onNoteTextChange = { noteText = it },
                            onSave = {
                                val dataWithName = result.data.copy(name = nameText, note = noteText)
                                viewModel.saveNFCData(dataWithName)
                                nameText = ""
                                noteText = ""
                                viewModel.clearScanResult()
                            },
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(result.data.content))
                                Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
                            },
                            onShare = {
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, result.data.content)
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, context.getString(R.string.share)))
                            },
                            onDismiss = { 
                                nameText = ""
                                noteText = ""
                                viewModel.clearScanResult() 
                            }
                        )
                    }
                    is NFCManager.NFCReadResult.Error -> {
                        ErrorCard(
                            errorMessage = result.message,
                            onRetry = { viewModel.clearScanResult() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScanResultCard(
    nfcData: NFCData,
    nameText: String,
    noteText: String,
    onNameTextChange: (String) -> Unit,
    onNoteTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.scan_complete),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                
                AssistChip(
                    onClick = {},
                    label = { Text(nfcData.type.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (nfcData.type) {
                            com.nfcmanager.data.model.NFCType.TEXT -> Color.Blue.copy(alpha = 0.1f)
                            com.nfcmanager.data.model.NFCType.URL -> Color.Green.copy(alpha = 0.1f)
                            com.nfcmanager.data.model.NFCType.VCARD -> Color.Magenta.copy(alpha = 0.1f)
                            else -> Color.Gray.copy(alpha = 0.1f)
                        },
                        labelColor = when (nfcData.type) {
                            com.nfcmanager.data.model.NFCType.TEXT -> Color.Blue
                            com.nfcmanager.data.model.NFCType.URL -> Color.Green
                            com.nfcmanager.data.model.NFCType.VCARD -> Color.Magenta
                            else -> Color.Gray
                        }
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.content),
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.LightGray.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = nfcData.content,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 自定义名称输入
            Text(
                text = stringResource(R.string.custom_name),
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            
            OutlinedTextField(
                value = nameText,
                onValueChange = onNameTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.custom_name_hint)) },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.add_note),
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            
            OutlinedTextField(
                value = noteText,
                onValueChange = onNoteTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.add_note)) },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.save))
                }
                
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.copy))
                }
                
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

@Composable
fun ErrorCard(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.scan_failed),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = errorMessage,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text(stringResource(R.string.retry_scan))
            }
        }
    }
}
