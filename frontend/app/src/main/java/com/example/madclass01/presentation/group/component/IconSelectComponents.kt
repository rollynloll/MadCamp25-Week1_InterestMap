package com.example.madclass01.presentation.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madclass01.R

@Composable
fun IconSelectButton(
    iconType: String,
    iconResId: Int,
    isSelected: Boolean,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .background(
                color = if (isSelected) Color(0xFFFFF4E6) else Color(0xFFF9FAFB),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFFFF9945) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick(iconType) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (iconType) {
                "users" -> "👥"
                "coffee" -> "☕"
                "camera" -> "📷"
                "mountain" -> "⛰️"
                "music" -> "🎵"
                "book" -> "📚"
                "sports" -> "⚽"
                "food" -> "🍔"
                else -> "👥"
            },
            fontSize = 24.sp
        )
    }
}

@Composable
fun IconPreview(
    iconType: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(96.dp)
            .background(
                color = Color(0xFFFF9945),
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (iconType) {
                "users" -> "👥"
                "coffee" -> "☕"
                "camera" -> "📷"
                "mountain" -> "⛰️"
                "music" -> "🎵"
                "book" -> "📚"
                "sports" -> "⚽"
                "food" -> "🍔"
                else -> "👥"
            },
            fontSize = 48.sp
        )
    }
}
