package com.nfcmanager.ui.screen

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nfcmanager.R
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.data.model.NFCType
import com.nfcmanager.nfc.NFCEmulationService
import com.nfcmanager.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmulationScreen(
    onBack: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val allData by viewModel.uiState.collectAsState()
    val isEmulating by viewModel.isEmulating.collectAsState()
    val currentEmulatingId by viewModel.currentEmulatingId.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.emulation_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 标题区域
            Text(
                text = stringResource(R.string.emulation_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.emulation_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 当前模拟状态卡片
            EmulationStatusCard(
                isEmulating = isEmulating,
                onStop = { viewModel.stopEmulation() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 数据列表
            Text(
                text = "${stringResource(R.string.saved_data)} (${allData.allData.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (allData.allData.isEmpty()) {
                EmptyDataHint()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allData.allData) { data ->
                        EmulationDataItem(
                            data = data,
                            isEmulating = isEmulating && currentEmulatingId == data.id,
                            onEmulate = { viewModel.startEmulation(data) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmulationStatusCard(
    isEmulating: Boolean,
    onStop: () -> Unit
) {
    val gradientColors = if (isEmulating) {
        listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
    } else {
        listOf(Color(0xFF9E9E9E), Color(0xFF616161))
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 状态图标
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEmulating) Icons.Default.Nfc else Icons.Default.Nfc,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEmulating) "正在模拟中" else "未开启模拟",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEmulating) "将手机靠近读卡器即可读取" else "选择下方数据开始模拟",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
                
                if (isEmulating) {
                    Button(
                        onClick = onStop,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = gradientColors[1]
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("停止")
                    }
                }
            }
        }
    }
}

@Composable
fun EmulationDataItem(
    data: NFCData,
    isEmulating: Boolean,
    onEmulate: () -> Unit
) {
    val typeIcon = when (data.type) {
        NFCType.URL -> Icons.Default.Link
        NFCType.TEXT -> Icons.Default.TextFields
        NFCType.VCARD -> Icons.Default.ContactPhone
        NFCType.PHONE -> Icons.Default.Phone
        NFCType.EMAIL -> Icons.Default.Email
        NFCType.WIFI -> Icons.Default.Wifi
        NFCType.GEO -> Icons.Default.LocationOn
        NFCType.APP -> Icons.Default.Apps
        NFCType.UNKNOWN -> Icons.Default.Help
    }
    
    val typeColor = when (data.type) {
        NFCType.URL -> Color(0xFF2196F3)
        NFCType.TEXT -> Color(0xFF4CAF50)
        NFCType.VCARD -> Color(0xFF9C27B0)
        NFCType.PHONE -> Color(0xFF00BCD4)
        NFCType.EMAIL -> Color(0xFFFF5722)
        NFCType.WIFI -> Color(0xFF3F51B5)
        NFCType.GEO -> Color(0xFFE91E63)
        NFCType.APP -> Color(0xFFFF9800)
        NFCType.UNKNOWN -> Color(0xFF9E9E9E)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEmulating) {
                typeColor.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isEmulating) {
            androidx.compose.foundation.BorderStroke(2.dp, typeColor)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 类型图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(typeColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 内容
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.name.ifEmpty { data.type.name },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = data.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        .format(data.readTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            // 模拟按钮
            Button(
                onClick = onEmulate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEmulating) typeColor else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isEmulating) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("模拟中")
                } else {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("模拟")
                }
            }
        }
    }
}

@Composable
fun EmptyDataHint() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.DataObject,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.no_saved_data),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.read_nfc_first),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
