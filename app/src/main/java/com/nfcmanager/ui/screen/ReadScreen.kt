package com.nfcmanager.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadScreen(
    onBack: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val nfcStatus by viewModel.nfcStatus.collectAsState()
    var isScanning by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<NFCManager.NFCReadResult?>(null) }
    var noteText by remember { mutableStateOf("") }
    
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
            
            Button(
                onClick = {
                    isScanning = true
                    simulateScan(viewModel) { result ->
                        isScanning = false
                        scanResult = result
                    }
                },
                enabled = nfcStatus == NFCManager.NFCStatus.ENABLED && !isScanning
            ) {
                Text(stringResource(R.string.start_scan))
            }
            
            scanResult?.let { result ->
                Spacer(modifier = Modifier.height(32.dp))
                
                when (result) {
                    is NFCManager.NFCReadResult.Success -> {
                        ScanResultCard(
                            nfcData = result.data,
                            noteText = noteText,
                            onNoteTextChange = { noteText = it },
                            onSave = {
                                val dataWithNote = result.data.copy(note = noteText)
                                viewModel.saveNFCData(dataWithNote)
                                noteText = ""
                                scanResult = null
                            },
                            onCopy = {},
                            onShare = {}
                        )
                    }
                    is NFCManager.NFCReadResult.Error -> {
                        ErrorCard(
                            errorMessage = result.message,
                            onRetry = {
                                scanResult = null
                                isScanning = true
                                simulateScan(viewModel) { newResult ->
                                    isScanning = false
                                    scanResult = newResult
                                }
                            }
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
    noteText: String,
    onNoteTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
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
                    onClick = onShare,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.share))
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

private fun simulateScan(
    viewModel: MainViewModel,
    onResult: (NFCManager.NFCReadResult) -> Unit
) {
    val sampleData = NFCData(
        content = "https://example.com\nSample NFC tag content",
        type = com.nfcmanager.data.model.NFCType.URL
    )
    
    onResult(NFCManager.NFCReadResult.Success(sampleData))
}
