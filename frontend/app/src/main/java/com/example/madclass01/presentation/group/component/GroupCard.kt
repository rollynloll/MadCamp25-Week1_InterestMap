package com.example.madclass01.presentation.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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
fun GroupCard(
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
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Group Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (group.imageUrl.isNotBlank()) {
                    coil.compose.SubcomposeAsyncImage(
                        model = group.imageUrl,
                        contentDescription = "Group Profile",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        error = {
                             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = groupIconEmoji(group.iconType),
                                    fontSize = 32.sp
                                )
                             }
                        }
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
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header: Name and Member Count
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
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "${group.memberCount}명",
                        fontSize = 13.sp,
                        color = Color(0xFF999999)
                    )
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
                
                // Region & Tags Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                     if (group.region.isNotBlank()) {
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
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                 // Tags
                if (group.tags.isNotEmpty()) {
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
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFDDDDDD),
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterVertically)
            )
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
