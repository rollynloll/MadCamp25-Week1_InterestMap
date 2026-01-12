package com.example.madclass01.presentation.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * 그룹 상세 헤더
 * - 배경: 그라데이션 (#10B981 → #059669)
 * - 왼쪽: 뒤로가기 버튼 (화살표)
 * - 중앙: 그룹 아이콘, 그룹명, 멤버 수, 활동 상태
 * - 오른쪽: QR 코드, 더보기 아이콘
 */
@Composable
fun GroupDetailHeaderComponent(
    groupName: String,
    memberCount: Int,
    activityStatus: String = "오늘 활동",
    groupIcon: String = "👥",
    profileImageUrl: String? = null,
    onBackClick: () -> Unit = {},
    onQRCodeClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(390f, 0f)
                )
            )
            .padding(16.dp)
    ) {
        // 뒤로가기 버튼
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(24.dp)
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "←",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 오른쪽 액션 버튼 (초대하기)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .clickable { onQRCodeClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "초대하기",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // 그룹 정보 (중앙)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 그룹 아이콘 (80x80)
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(24.dp))
                    .background(Color.White, shape = RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!profileImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "그룹 이미지",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = groupIcon,
                        fontSize = 38.sp
                    )
                }
            }

            // 그룹명
            Text(
                text = groupName,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )

            // 멤버 수 및 활동 상태
            Text(
                text = "${memberCount}명의 멤버 · $activityStatus",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * 미니 헤더 (스크롤 후)
 * - 간소화된 버전
 */
@Composable
fun GroupDetailMiniHeaderComponent(
    groupName: String,
    onBackClick: () -> Unit = {},
    onQRCodeClick: () -> Unit = {},
    onMoreClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF10B981))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 뒤로가기
        Text(
            text = "←",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onBackClick() }
        )

        // 제목
        Text(
            text = groupName,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )

        // 사람 초대
        Icon(
            imageVector = Icons.Default.PersonAdd,
            contentDescription = "초대하기",
            tint = Color.White,
            modifier = Modifier
                .size(24.dp)
                .clickable { onQRCodeClick() }
                .padding(end = 12.dp)
        )

        // 더보기
        Text(
            text = "⋮",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.clickable { onMoreClick() }
        )
    }
}
