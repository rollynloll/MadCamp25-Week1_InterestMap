package com.example.madclass01.presentation.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 그룹 아이콘
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = Color(0xFF333333),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (group.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = group.imageUrl,
                        contentDescription = "그룹 프로필",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Text(
                        text = groupIconEmoji(group.iconType),
                        fontSize = 28.sp
                    )
                }
            }
            
            // 그룹 정보
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 그룹명 및 멤버수
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = group.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    
                    Text(
                        text = "${group.memberCount}명",
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                }
                
                // 그룹 소개
                if (group.description.isNotBlank()) {
                    Text(
                        text = group.description,
                        fontSize = 13.sp,
                        color = Color(0xFF666666),
                        maxLines = 1
                    )
                }
                
                // 태그 (MAX 3개)
                if (group.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        group.tags.take(3).forEach { tag ->
                            TagChip(
                                label = tag.name,
                                isSelected = false,
                                onToggle = {},
                                modifier = Modifier
                            )
                        }
                    }
                }
                
                // 지역 정보
                if (group.region.isNotBlank()) {
                    Text(
                        text = "📍 ${group.region}",
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                }
            }
            
            // 화살표
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "상세보기",
                tint = Color(0xFFDDDDDD),
                modifier = Modifier.size(20.dp)
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
