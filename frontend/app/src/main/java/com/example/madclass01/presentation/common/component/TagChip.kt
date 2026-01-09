package com.example.madclass01.presentation.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TagChip(
    label: String,
    isSelected: Boolean = false,
    onRemove: (() -> Unit)? = null,
    onToggle: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFFFF9945) else Color(0xFFF5F5F5)
    val textColor = if (isSelected) Color.White else Color(0xFF333333)
    val borderColor = if (isSelected) Color(0xFFFF9945) else Color(0xFFDDDDDD)
    
    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onToggle?.invoke() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 체크 아이콘 (선택 시)
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "선택됨",
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
            }
            
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            
            // 삭제 버튼 (onRemove가 있을 때만)
            if (onRemove != null) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "삭제",
                    tint = textColor,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(start = 6.dp)
                        .clickable { onRemove() }
                )
            }
        }
    }
}
