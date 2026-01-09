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
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "그룹 아이콘",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
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
                        text = "${group.memberCount}명 · ${group.activity}",
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                }
                
                // 태그
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    group.tags.take(2).forEach { tag ->
                        TagChip(
                            label = tag.name,
                            isSelected = false,
                            onToggle = {},
                            modifier = Modifier
                        )
                    }
                }
                
                // 활동 정보
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "메시지",
                            tint = Color(0xFFFF9945),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = group.messageCount.toString(),
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "날짜",
                            tint = Color(0xFFFF9945),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = group.lastActivityDate,
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                    }
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
