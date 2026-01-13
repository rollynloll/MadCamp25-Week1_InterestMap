package com.example.madclass01.presentation.search.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.madclass01.domain.model.Group
import com.example.madclass01.presentation.common.component.TagChip

@Composable
fun SearchResultCard(
    group: Group,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group Icon / Image
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = Color(0xFFF0F0F0),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (group.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = group.imageUrl,
                        contentDescription = "Group Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = groupIconEmoji(group.iconType),
                        fontSize = 32.sp
                    )
                }
            }
            
            // Group Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Name & Member Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = group.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                         maxLines = 1
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color(0xFF999999),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${group.memberCount}",
                            fontSize = 12.sp,
                            color = Color(0xFF999999)
                        )
                    }
                }
                
                // Description
                if (group.description.isNotBlank()) {
                    Text(
                        text = group.description,
                        fontSize = 13.sp,
                        color = Color(0xFF666666),
                        maxLines = 1,
                        lineHeight = 18.sp
                    )
                }
                
                // Region
                if (group.region.isNotBlank()) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFFF9945),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = group.region,
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
                
                // Tags
                if (group.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        group.tags.forEach { tag ->
                            TagChip(
                                label = tag.name,
                                isSelected = false
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun groupIconEmoji(iconType: String): String {
    return when (iconType) {
        "users" -> "👥"
        "coffee" -> "☕"
        "camera" -> "📷"
        "mountain" -> "⛰️"
        "music" -> "🎵"
        "book" -> "📚"
        "sports" -> "⚽"
        "food" -> "🍔"
        else -> "👥"
    }
}
