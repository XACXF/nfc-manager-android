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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 筛选按钮
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "筛选")
                    }
                    
                    // 导出按钮
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "导出")
                    }
                }
            )
        },
        floatingActionButton = {
            if (displayData.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        // 批量操作
                    },
                    icon = { Icon(Icons.Filled.DeleteSweep, contentDescription = "批量删除") },
                    text = { Text("批量删除") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索框
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
                            Icon(Icons.Filled.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true
            )
            
            // 筛选标签
            FilterChips(
                selectedType = filterType,
                onTypeSelected = viewModel::updateFilterType,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 数据显示
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
                                // 查看详情或触发功能
                            },
                            onEditClick = {
                                // 编辑数据
                            },
                            onDeleteClick = {
                                selectedItemForDelete = nfcData
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
            
            // 统计信息
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
    
    // 筛选菜单
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
    
    // 删除确认对话框
    if (showDeleteDialog && selectedItemForDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text("确定要删除这条NFC记录吗？删除后无法恢复。") },
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
    
    // 导出对话框
    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { format ->
                // 导出数据
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
        // 全部
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text(stringResource(R.string.type_all)) }
        )
        
        // 文本
        FilterChip(
            selected = selectedType == NFCType.TEXT,
            onClick = { onTypeSelected(NFCType.TEXT) },
            label = { Text(stringResource(R.string.type_text)) }
        )
        
        // 网址
        FilterChip(
            selected = selectedType == NFCType.URL,
            onClick = { onTypeSelected(NFCType.URL) },
            label = { Text(stringResource(R.string.type_url)) }
        )
        
        // 名片
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
                    searchQuery.isNotEmpty() -> "没有找到\"$searchQuery\"相关的NFC记录"
                    filterType != null -> "没有${filterType.name}类型的NFC记录"
                    else -> stringResource(R.string.no_data)
                },
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            if (searchQuery.isEmpty() && filterType == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "尝试读取一些NFC标签来开始使用",
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
                    text = "数据统计",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "总计: $totalCount 条",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (filteredCount < totalCount) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "筛选结果",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "$filteredCount 条",
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
        title = { Text("筛选类型") },
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
                    Text("全部类型")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
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
                Text("选择导出格式:")
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onExport("CSV") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CSV格式")
                    }
                    
                    OutlinedButton(
                        onClick = { onExport("TXT") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("文本格式")
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