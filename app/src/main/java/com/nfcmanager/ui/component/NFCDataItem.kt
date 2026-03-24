package com.nfcmanager.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.data.model.NFCType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFCDataItem(
    nfcData: NFCData,
    onItemClick: (NFCData) -> Unit,
    onEditClick: (NFCData) -> Unit,
    onDeleteClick: (NFCData) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(nfcData) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (nfcData.type) {
                NFCType.TEXT -> Color(0xFFE3F2FD)
                NFCType.URL -> Color(0xFFE8F5E9)
                NFCType.VCARD -> Color(0xFFF3E5F5)
                NFCType.PHONE -> Color(0xFFFFF8E1)
                NFCType.EMAIL -> Color(0xFFE0F7FA)
                NFCType.WIFI -> Color(0xFFFCE4EC)
                NFCType.GEO -> Color(0xFFE8EAF6)
                NFCType.APP -> Color(0xFFF3E5F5)
                else -> Color(0xFFF5F5F5)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 类型标签
                TypeChip(type = nfcData.type)
                
                // 时间
                Text(
                    text = formatDateTime(nfcData.readTime),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 内容预览
            Text(
                text = nfcData.content.take(80) + if (nfcData.content.length > 80) "..." else "",
                fontSize = 14.sp,
                lineHeight = 18.sp,
                maxLines = 3
            )
            
            // 备注（如果有）
            if (nfcData.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notes,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = nfcData.note,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 2
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 复制按钮
                IconButton(
                    onClick = { /* 复制内容到剪贴板 */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "复制",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // 编辑按钮
                IconButton(
                    onClick = { onEditClick(nfcData) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // 删除按钮
                IconButton(
                    onClick = { onDeleteClick(nfcData) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "删除",
                        tint = Color.Red,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TypeChip(type: NFCType) {
    Chip(
        onClick = {},
        label = {
            Text(
                text = when (type) {
                    NFCType.TEXT -> "文本"
                    NFCType.URL -> "网址"
                    NFCType.VCARD -> "名片"
                    NFCType.PHONE -> "电话"
                    NFCType.EMAIL -> "邮箱"
                    NFCType.WIFI -> "WiFi"
                    NFCType.GEO -> "位置"
                    NFCType.APP -> "应用"
                    else -> "其他"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        },
        colors = ChipDefaults.chipColors(
            containerColor = when (type) {
                NFCType.TEXT -> Color.Blue.copy(alpha = 0.1f)
                NFCType.URL -> Color.Green.copy(alpha = 0.1f)
                NFCType.VCARD -> Color.Purple.copy(alpha = 0.1f)
                NFCType.PHONE -> Color.Orange.copy(alpha = 0.1f)
                NFCType.EMAIL -> Color.Cyan.copy(alpha = 0.1f)
                NFCType.WIFI -> Color.Pink.copy(alpha = 0.1f)
                NFCType.GEO -> Color.Indigo.copy(alpha = 0.1f)
                NFCType.APP -> Color.Magenta.copy(alpha = 0.1f)
                else -> Color.Gray.copy(alpha = 0.1f)
            },
            labelColor = when (type) {
                NFCType.TEXT -> Color.Blue
                NFCType.URL -> Color.Green
                NFCType.VCARD -> Color.Purple
                NFCType.PHONE -> Color.Orange
                NFCType.EMAIL -> Color.Cyan
                NFCType.WIFI -> Color.Pink
                NFCType.GEO -> Color.Indigo
                NFCType.APP -> Color.Magenta
                else -> Color.Gray
            }
        )
    )
}

@Composable
fun CompactNFCDataItem(
    nfcData: NFCData,
    onClick: (NFCData) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(nfcData) },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 类型图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = when (nfcData.type) {
                            NFCType.TEXT -> Color.Blue.copy(alpha = 0.1f)
                            NFCType.URL -> Color.Green.copy(alpha = 0.1f)
                            else -> Color.Gray.copy(alpha = 0.1f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (nfcData.type) {
                        NFCType.TEXT -> "T"
                        NFCType.URL -> "U"
                        NFCType.VCARD -> "V"
                        else -> "N"
                    },
                    fontWeight = FontWeight.Bold,
                    color = when (nfcData.type) {
                        NFCType.TEXT -> Color.Blue
                        NFCType.URL -> Color.Green
                        NFCType.VCARD -> Color.Purple
                        else -> Color.Gray
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 内容和时间
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = nfcData.content.take(40) + if (nfcData.content.length > 40) "..." else "",
                    fontSize = 14.sp,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = formatTimeAgo(nfcData.readTime),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

private fun formatDateTime(date: Date): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return formatter.format(date)
}

private fun formatTimeAgo(date: Date): String {
    val now = Date()
    val diff = now.time - date.time
    
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000}分钟前"
        diff < 86400000 -> "${diff / 3600000}小时前"
        diff < 604800000 -> "${diff / 86400000}天前"
        else -> SimpleDateFormat("MM-dd", Locale.getDefault()).format(date)
    }
}