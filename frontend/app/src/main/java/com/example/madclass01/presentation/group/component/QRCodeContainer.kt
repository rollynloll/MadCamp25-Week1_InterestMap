package com.example.madclass01.presentation.group.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QRCodeContainer(
    qrCodeBitmap: Bitmap? = null,
    inviteUrl: String = "",
    expiryTime: String = "24시간 후 만료",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(0.838f)  // 326/390
            .background(Color.White, shape = RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = Color(0xFFE5E7EB),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // QR Code Display
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = Color(0xFFE5E7EB),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (qrCodeBitmap != null) {
                Image(
                    bitmap = qrCodeBitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier
                        .size(220.dp)
                        .padding(10.dp)
                )
            } else {
                Text(
                    text = "QR 코드 생성 중...",
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp
                )
            }
        }
        
        // QR Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "QR 코드를 스캔하면 그룹에 가입할 수 있어요",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF6B7280),
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            
            // Expiry Badge
            Row(
                modifier = Modifier
                    .background(Color(0xFFFEF2F2), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏱",
                    fontSize = 14.sp
                )
                Text(
                    text = expiryTime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF991B1B)
                )
            }
        }
    }
}
