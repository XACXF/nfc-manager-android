package com.nfcmanager.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nfcmanager.R
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.data.model.NFCType
import com.nfcmanager.ui.component.NFCDataItem
import com.nfcmanager.util.DataExporter
import com.nfcmanager.util.NFCActionExecutor
import com.nfcmanager.viewmodel.MainViewModel
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(
    onBack: () -> Unit,
    onWrite: (NFCData) -> Unit = {},
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    
    var showFilterMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedItemForDelete by remember { mutableStateOf<NFCData?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showActionDialog by remember { mutableStateOf(false) }
    var selectedItemForAction by remember { mutableStateOf<NFCData?>(null) }
    var selectedItemForExport by remember { mutableStateOf<NFCData?>(null) }
    
    val context = LocalContext.current
    val actionExecutor = remember { NFCActionExecutor(context) }
    val dataExporter = remember { DataExporter(context) }
    
    val displayData = viewModel.getDisplayData()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.local_data)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = stringResource(R.string.filter))
                    }
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Filled.FileDownload, contentDescription = stringResource(R.string.export))
                    }
                }
            )
        },
        floatingActionButton = {
            if (displayData.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {},
                    icon = { Icon(Icons.Filled.DeleteSweep, contentDescription = null) },
                    text = { Text(stringResource(R.string.batch_delete)) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(stringResource(R.string.search)) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.clear))
                        }
                    }
                },
                singleLine = true
            )
            
            FilterChips(
                selectedType = filterType,
                onTypeSelected = viewModel::updateFilterType,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (displayData.isEmpty()) {
                EmptyState(
                    searchQuery = searchQuery,
                    filterType = filterType,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(displayData) { nfcData ->
                        NFCDataItem(
                            nfcData = nfcData,
                            onItemClick = {
                                selectedItemForAction = nfcData
                                showActionDialog = true
                            },
                            onEditClick = {},
                            onDeleteClick = {
                                selectedItemForDelete = nfcData
                                showDeleteDialog = true
                            },
                            onWriteToNFC = { data ->
                                onWrite(data)
                            },
                            onExportClick = {
                                selectedItemForExport = nfcData
                                showExportDialog = true
                            }
                        )
                    }
                }
            }
            
            if (displayData.isNotEmpty()) {
                DataStatistics(
                    totalCount = displayData.size,
                    filteredCount = when {
                        searchQuery.isNotEmpty() -> uiState.searchResults.size
                        filterType != null -> uiState.filteredData.size
                        else -> uiState.allData.size
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
    
    if (showFilterMenu) {
        FilterMenu(
            selectedType = filterType,
            onTypeSelected = { type ->
                viewModel.updateFilterType(type)
                showFilterMenu = false
            },
            onClearFilter = {
                viewModel.updateFilterType(null)
                showFilterMenu = false
            },
            onDismiss = { showFilterMenu = false }
        )
    }
    
    if (showDeleteDialog && selectedItemForDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text(stringResource(R.string.confirm_delete_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedItemForDelete?.let { viewModel.deleteNFCData(it) }
                        showDeleteDialog = false
                        selectedItemForDelete = null
                    }
                ) {
                    Text(stringResource(R.string.yes), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    if (showExportDialog) {
        ExportDialog(
            isSingleExport = selectedItemForExport != null,
            totalCount = uiState.allData.size,
            onDismiss = { 
                showExportDialog = false 
                selectedItemForExport = null
            },
            onExport = { format, exportAll ->
                val result = if (exportAll) {
                    if (uiState.allData.isEmpty()) {
                        Toast.makeText(context, R.string.no_data_to_export, Toast.LENGTH_SHORT).show()
                        return@ExportDialog
                    }
                    dataExporter.exportAll(uiState.allData, 
                        if (format == "CSV") DataExporter.ExportFormat.CSV else DataExporter.ExportFormat.TXT)
                } else {
                    selectedItemForExport?.let { data ->
                        dataExporter.exportSingle(data, 
                            if (format == "CSV") DataExporter.ExportFormat.CSV else DataExporter.ExportFormat.TXT)
                    } ?: run {
                        Toast.makeText(context, R.string.no_data_to_export, Toast.LENGTH_SHORT).show()
                        return@ExportDialog
                    }
                }
                
                when (result) {
                    is DataExporter.ExportResult.Success -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        // 询问是否分享文件
                        showExportDialog = false
                        selectedItemForExport = null
                    }
                    is DataExporter.ExportResult.Error -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
    
    if (showActionDialog && selectedItemForAction != null) {
        AlertDialog(
            onDismissRequest = { showActionDialog = false },
            title = { Text(stringResource(R.string.execute_action)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.action_dialog_msg),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = selectedItemForAction!!.content,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedItemForAction?.let { actionExecutor.execute(it) }
                        showActionDialog = false
                        selectedItemForAction = null
                    }
                ) {
                    Text(actionExecutor.getActionDescription(selectedItemForAction!!.type))
                }
            },
            dismissButton = {
                TextButton(onClick = { showActionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    selectedType: NFCType?,
    onTypeSelected: (NFCType?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text(stringResource(R.string.type_all)) }
        )
        FilterChip(
            selected = selectedType == NFCType.TEXT,
            onClick = { onTypeSelected(NFCType.TEXT) },
            label = { Text(stringResource(R.string.type_text)) }
        )
        FilterChip(
            selected = selectedType == NFCType.URL,
            onClick = { onTypeSelected(NFCType.URL) },
            label = { Text(stringResource(R.string.type_url)) }
        )
        FilterChip(
            selected = selectedType == NFCType.VCARD,
            onClick = { onTypeSelected(NFCType.VCARD) },
            label = { Text(stringResource(R.string.type_vcard)) }
        )
    }
}

@Composable
fun EmptyState(
    searchQuery: String,
    filterType: NFCType?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Storage,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = when {
                    searchQuery.isNotEmpty() -> stringResource(R.string.no_data_search, searchQuery)
                    filterType != null -> stringResource(R.string.no_data_filter, filterType.name)
                    else -> stringResource(R.string.no_data)
                },
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            if (searchQuery.isEmpty() && filterType == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.try_read_nfc),
                    fontSize = 14.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DataStatistics(
    totalCount: Int,
    filteredCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = stringResource(R.string.data_statistics),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stringResource(R.string.total)}: $totalCount",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (filteredCount < totalCount) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.filtered_results),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$filteredCount",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun FilterMenu(
    selectedType: NFCType?,
    onTypeSelected: (NFCType?) -> Unit,
    onClearFilter: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filter_by_type)) },
        text = {
            Column {
                NFCType.values().forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { onTypeSelected(type) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(getTypeName(type))
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedType == null,
                        onClick = { onClearFilter() }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.all_types))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    isSingleExport: Boolean,
    totalCount: Int,
    onDismiss: () -> Unit,
    onExport: (format: String, exportAll: Boolean) -> Unit
) {
    var selectedFormat by remember { mutableStateOf("CSV") }
    var exportAll by remember { mutableStateOf(!isSingleExport) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.export)) },
        text = {
            Column {
                // 导出范围选择
                Text(
                    text = stringResource(R.string.select_export_range),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !exportAll,
                        onClick = { exportAll = false },
                        enabled = isSingleExport
                    )
                    Text(
                        text = stringResource(R.string.export_single),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = exportAll,
                        onClick = { exportAll = true }
                    )
                    Text(
                        text = "${stringResource(R.string.export_all)} ($totalCount)",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 格式选择
                Text(
                    text = stringResource(R.string.select_export_format),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFormat == "CSV",
                        onClick = { selectedFormat = "CSV" },
                        label = { Text("CSV") }
                    )
                    FilterChip(
                        selected = selectedFormat == "TXT",
                        onClick = { selectedFormat = "TXT" },
                        label = { Text("TXT") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onExport(selectedFormat, exportAll) }
            ) {
                Text(stringResource(R.string.export))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun getTypeName(type: NFCType): String {
    return when (type) {
        NFCType.TEXT -> stringResource(R.string.type_text)
        NFCType.URL -> stringResource(R.string.type_url)
        NFCType.VCARD -> stringResource(R.string.type_vcard)
        NFCType.PHONE -> stringResource(R.string.type_phone)
        NFCType.EMAIL -> stringResource(R.string.type_email)
        NFCType.WIFI -> stringResource(R.string.type_wifi)
        NFCType.GEO -> stringResource(R.string.type_geo)
        NFCType.APP -> stringResource(R.string.type_app)
        NFCType.UNKNOWN -> stringResource(R.string.type_other)
    }
}
