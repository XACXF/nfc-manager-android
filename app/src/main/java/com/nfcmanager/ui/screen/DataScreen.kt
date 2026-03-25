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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "Export")
                    }
                }
            )
        },
        floatingActionButton = {
            if (displayData.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {},
                    icon = { Icon(Icons.Filled.DeleteSweep, contentDescription = "Batch delete") },
                    text = { Text("Batch Delete") }
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
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
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
                            onItemClick = {},
                            onEditClick = {},
                            onDeleteClick = {
                                selectedItemForDelete = nfcData
                                showDeleteDialog = true
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
            text = { Text("Are you sure you want to delete this NFC record? This action cannot be undone.") },
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
            onDismiss = { showExportDialog = false },
            onExport = { format ->
                showExportDialog = false
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
                    searchQuery.isNotEmpty() -> "No NFC records found for \"$searchQuery\""
                    filterType != null -> "No ${filterType.name} type NFC records"
                    else -> stringResource(R.string.no_data)
                },
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            if (searchQuery.isEmpty() && filterType == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Try reading some NFC tags to get started",
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
                    text = "Data Statistics",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total: $totalCount",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (filteredCount < totalCount) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Filtered Results",
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
        title = { Text("Filter by Type") },
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
                    Text("All Types")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
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
                Text("Select export format:")
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onExport("CSV") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CSV")
                    }
                    
                    OutlinedButton(
                        onClick = { onExport("TXT") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("TXT")
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
