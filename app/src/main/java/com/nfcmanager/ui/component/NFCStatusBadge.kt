package com.nfcmanager.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfcmanager.R
import com.nfcmanager.nfc.NFCManager

@Composable
fun NFCStatusBadge(status: NFCManager.NFCStatus) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    when (status) {
                        NFCManager.NFCStatus.ENABLED -> Color.Green
                        NFCManager.NFCStatus.DISABLED -> Color.Yellow
                        NFCManager.NFCStatus.NOT_SUPPORTED -> Color.Red
                    }
                )
        )
        
        Text(
            text = when (status) {
                NFCManager.NFCStatus.ENABLED -> stringResource(R.string.nfc_enabled)
                NFCManager.NFCStatus.DISABLED -> stringResource(R.string.nfc_disabled)
                NFCManager.NFCStatus.NOT_SUPPORTED -> stringResource(R.string.nfc_not_supported)
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = when (status) {
                NFCManager.NFCStatus.ENABLED -> MaterialTheme.colorScheme.primary
                NFCManager.NFCStatus.DISABLED -> Color(0xFFF57C00)
                NFCManager.NFCStatus.NOT_SUPPORTED -> MaterialTheme.colorScheme.error
            }
        )
    }
}

@Composable
fun NFCStatusCard(status: NFCManager.NFCStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                NFCManager.NFCStatus.ENABLED -> Color(0xFFE8F5E8)
                NFCManager.NFCStatus.DISABLED -> Color(0xFFFFF8E1)
                NFCManager.NFCStatus.NOT_SUPPORTED -> Color(0xFFFFEBEE)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = when (status) {
                    NFCManager.NFCStatus.ENABLED -> Icons.Filled.CheckCircle
                    NFCManager.NFCStatus.DISABLED -> Icons.Filled.Warning
                    NFCManager.NFCStatus.NOT_SUPPORTED -> Icons.Filled.Error
                },
                contentDescription = null,
                tint = when (status) {
                    NFCManager.NFCStatus.ENABLED -> Color.Green
                    NFCManager.NFCStatus.DISABLED -> Color(0xFFF57C00)
                    NFCManager.NFCStatus.NOT_SUPPORTED -> Color.Red
                },
                modifier = Modifier.size(24.dp)
            )
            
            Column {
                Text(
                    text = stringResource(R.string.nfc_status),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                
                Text(
                    text = when (status) {
                        NFCManager.NFCStatus.ENABLED -> "NFC is enabled and ready to use"
                        NFCManager.NFCStatus.DISABLED -> "NFC is disabled, please enable in settings"
                        NFCManager.NFCStatus.NOT_SUPPORTED -> "This device does not support NFC"
                    },
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun NFCScanningAnimation(isScanning: Boolean) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        if (isScanning) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            )
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Nfc,
                contentDescription = "NFC Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
