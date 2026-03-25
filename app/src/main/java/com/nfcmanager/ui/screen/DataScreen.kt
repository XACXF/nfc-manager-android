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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nfcmanager.R
import com.nfcmanager.data.model.NFCType
import com.nfcmanager.ui.component.NFCDataItem
import com.nfcmanager.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(
    onBack: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    
    var showFilterMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedItemForDelete by remember { mutableStateOf<com.nfcmanager.data.model.NFCData?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    val displayData = viewModel.getDisplayData()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.local_data)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "杩斿洖")
                    }
                },
                actions = {
                    // 绛涢€夋寜閽?
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "绛涢€?)
                    }
                    
                    // 瀵煎嚭鎸夐挳
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "瀵煎嚭")
                    }
                }
            )
        },
        floatingActionButton = {
            if (displayData.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        // 鎵归噺鎿嶄綔
                    },
                    icon = { Icon(Icons.Filled.DeleteSweep, contentDescription = "鎵归噺鍒犻櫎") },
                    text = { Text("鎵归噺鍒犻櫎") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 鎼滅储妗?
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
                            Icon(Icons.Filled.Clear, contentDescription = "娓呴櫎")
                        }
                    }
                },
                singleLine = true
            )
            
            // 绛涢€夋爣绛?
            FilterChips(
                selectedType = filterType,
                onTypeSelected = viewModel::updateFilterType,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 鏁版嵁鏄剧ず
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
                                // 鏌ョ湅璇︽儏鎴栬Е鍙戝姛鑳?
                            },
                            onEditClick = {
                                // 缂栬緫鏁版嵁
                            },
                            onDeleteClick = {
                                selectedItemForDelete = nfcData
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
            
            // 缁熻淇℃伅
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
    
    // 绛涢€夎彍鍗?
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
    
    // 鍒犻櫎纭瀵硅瘽妗?
    if (showDeleteDialog && selectedItemForDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text("纭畾瑕佸垹闄よ繖鏉FC璁板綍鍚楋紵鍒犻櫎鍚庢棤娉曟仮澶嶃€?) },
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
    
    // 瀵煎嚭瀵硅瘽妗?
    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { format ->
                // 瀵煎嚭鏁版嵁
                showExportDialog = false
            }
        )
    }
}

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
        // 鍏ㄩ儴
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text(stringResource(R.string.type_all)) }
        )
        
        // 鏂囨湰
        FilterChip(
            selected = selectedType == NFCType.TEXT,
            onClick = { onTypeSelected(NFCType.TEXT) },
            label = { Text(stringResource(R.string.type_text)) }
        )
        
        // 缃戝潃
        FilterChip(
            selected = selectedType == NFCType.URL,
            onClick = { onTypeSelected(NFCType.URL) },
            label = { Text(stringResource(R.string.type_url)) }
        )
        
        // 鍚嶇墖
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
                    searchQuery.isNotEmpty() -> "娌℃湁鎵惧埌\"$searchQuery\"鐩稿叧鐨凬FC璁板綍"
                    filterType != null -> "娌℃湁${filterType.name}绫诲瀷鐨凬FC璁板綍"
                    else -> stringResource(R.string.no_data)
                },
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            if (searchQuery.isEmpty() && filterType == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "灏濊瘯璇诲彇涓€浜汵FC鏍囩鏉ュ紑濮嬩娇鐢?,
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
                    text = "鏁版嵁缁熻",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "鎬昏: $totalCount 鏉?,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (filteredCount < totalCount) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "绛涢€夌粨鏋?,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "$filteredCount 鏉?,
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
        title = { Text("绛涢€夌被鍨?) },
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
                        Text(type.name)
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
                    Text("鍏ㄩ儴绫诲瀷")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("纭畾")
            }
        }
    )
}

@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.export)) },
        text = {
            Column {
                Text("閫夋嫨瀵煎嚭鏍煎紡:")
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onExport("CSV") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CSV鏍煎紡")
                    }
                    
                    OutlinedButton(
                        onClick = { onExport("TXT") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("鏂囨湰鏍煎紡")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}