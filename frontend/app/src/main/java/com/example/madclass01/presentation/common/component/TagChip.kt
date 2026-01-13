package com.example.madclass01.presentation.common.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
    val containerColor = if (isSelected) Color(0xFFFF9945) else Color.White
    val contentColor = if (isSelected) Color.White else Color(0xFF666666)
    val border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE0E0E0))
    val elevation = if (isSelected) 2.dp else 0.dp

    Surface(
        modifier = modifier.clickable(enabled = onToggle != null) { onToggle?.invoke() },
        shape = CircleShape, // Fully rounded
        color = containerColor,
        contentColor = contentColor,
        border = border,
        shadowElevation = elevation
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Check icon for selection
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).padding(end = 4.dp),
                    tint = contentColor
                )
            }

            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )

            // Remove icon
            if (onRemove != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onRemove.invoke() },
                    tint = contentColor.copy(alpha = 0.8f) // Slightly clearer on click
                )
            }
        }
    }
}
