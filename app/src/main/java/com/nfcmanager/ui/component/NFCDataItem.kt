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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.nfcmanager.ui.theme.Pink
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfcmanager.R
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.data.model.NFCType
import com.nfcmanager.util.VirtualNFCExecutor
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFCDataItem(
    nfcData: NFCData,
    onItemClick: (NFCData) -> Unit,
    onEditClick: (NFCData) -> Unit,
    onDeleteClick: (NFCData) -> Unit,
    onExportClick: (NFCData) -> Unit = {},
    onQuickExecute: (NFCData) -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val virtualExecutor = remember { VirtualNFCExecutor(context) }
    
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TypeChip(type = nfcData.type)
                
                Text(
                    text = formatDateTime(nfcData.readTime),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 鏄剧ず鑷畾涔夊悕绉版垨鍐呭
            Text(
                text = if (nfcData.name.isNotEmpty()) nfcData.name else nfcData.content.take(80) + if (nfcData.content.length > 80) "..." else "",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                maxLines = 2
            )
            
            // 濡傛灉鏈夎嚜瀹氫箟鍚嶇О锛屾樉绀哄唴瀹归瑙?
            if (nfcData.name.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = nfcData.content.take(60) + if (nfcData.content.length > 60) "..." else "",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
            
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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 蹇嵎鎵ц鎸夐挳锛堣櫄鎷熷埛鍗★級
                IconButton(
                    onClick = {
                        virtualExecutor.quickExecute(nfcData)
                        Toast.makeText(context, "鎵ц: ${virtualExecutor.getActionDescription(nfcData)}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "蹇嵎鎵ц",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(
                    onClick = { 
                        clipboardManager.setText(AnnotatedString(nfcData.content))
                        Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = stringResource(R.string.copy),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                IconButton(
                    onClick = { onExportClick(nfcData) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FileDownload,
                        contentDescription = stringResource(R.string.export),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                IconButton(
                    onClick = { onEditClick(nfcData) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                IconButton(
                    onClick = { onDeleteClick(nfcData) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete),
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
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = getTypeName(type),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = when (type) {
                NFCType.TEXT -> Color.Blue.copy(alpha = 0.1f)
                NFCType.URL -> Color.Green.copy(alpha = 0.1f)
                NFCType.VCARD -> Color.Magenta.copy(alpha = 0.1f)
                NFCType.PHONE -> Color(0xFFFF9800).copy(alpha = 0.1f)
                NFCType.EMAIL -> Color.Cyan.copy(alpha = 0.1f)
                NFCType.WIFI -> Pink.copy(alpha = 0.1f)
                NFCType.GEO -> Color(0xFF3F51B5).copy(alpha = 0.1f)
                NFCType.APP -> Color.Magenta.copy(alpha = 0.1f)
                else -> Color.Gray.copy(alpha = 0.1f)
            },
            labelColor = when (type) {
                NFCType.TEXT -> Color.Blue
                NFCType.URL -> Color.Green
                NFCType.VCARD -> Color.Magenta
                NFCType.PHONE -> Color(0xFFFF9800)
                NFCType.EMAIL -> Color.Cyan
                NFCType.WIFI -> Pink
                NFCType.GEO -> Color(0xFF3F51B5)
                NFCType.APP -> Color.Magenta
                else -> Color.Gray
            }
        )
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
                        NFCType.TEXT -> "鏂?
                        NFCType.URL -> "缃?
                        NFCType.VCARD -> "鍚?
                        else -> "N"
                    },
                    fontWeight = FontWeight.Bold,
                    color = when (nfcData.type) {
                        NFCType.TEXT -> Color.Blue
                        NFCType.URL -> Color.Green
                        NFCType.VCARD -> Color.Magenta
                        else -> Color.Gray
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
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

@Composable
private fun formatTimeAgo(date: Date): String {
    val now = Date()
    val diff = now.time - date.time
    
    return when {
        diff < 60000 -> stringResource(R.string.just_now)
        diff < 3600000 -> stringResource(R.string.min_ago, diff / 60000)
        diff < 86400000 -> stringResource(R.string.hours_ago, diff / 3600000)
        diff < 604800000 -> stringResource(R.string.days_ago, diff / 86400000)
        else -> SimpleDateFormat("MM-dd", Locale.getDefault()).format(date)
    }
}
