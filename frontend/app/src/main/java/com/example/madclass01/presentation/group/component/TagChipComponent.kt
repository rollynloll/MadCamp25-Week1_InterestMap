package com.example.madclass01.presentation.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    tag: String,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(32.dp)
            .background(
                color = Color(0xFFEEF2FF),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tag,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4338CA)
        )
        
        Box(
            modifier = Modifier
                .size(14.dp)
                .clickable { onRemove(tag) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✕",
                fontSize = 12.sp,
                color = Color(0xFF6366F1),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
