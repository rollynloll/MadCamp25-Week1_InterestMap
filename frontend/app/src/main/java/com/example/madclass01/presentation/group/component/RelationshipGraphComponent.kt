package com.example.madclass01.presentation.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madclass01.domain.model.GraphNodePosition
import com.example.madclass01.domain.model.RelationshipGraph
import com.example.madclass01.utils.GraphLayoutCalculator

/**
 * 관계 그래프 캔버스
 * - 중앙: 현재 사용자 (72px, 그라데이션 배경)
 * - 주변: 다른 사용자들 (유사도 기반 거리)
 * - 노드 크기: 유사도에 따라 40~56px
 * - 노드 색상: 유사도에 따라 다름
 */
@Composable
fun RelationshipGraphComponent(
    relationshipGraph: RelationshipGraph,
    selectedUserId: String? = null,
    onNodeClick: (String) -> Unit = {},
    onNodeLongClick: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(width = 390.dp, height = 520.dp)
            .background(Color(0xFFFAFBFC)),
        contentAlignment = Alignment.Center
    ) {
        // 현재 사용자 노드 (중앙)
        CenterNodeComponent(
            selectedUserId = selectedUserId,
            onNodeClick = { onNodeClick(relationshipGraph.currentUserId) }
        )

        // 다른 사용자들의 노드
        relationshipGraph.otherUserNodes.forEach { nodePosition ->
            val embedding = relationshipGraph.embeddings[nodePosition.userId]
            if (embedding != null) {
                OtherUserNodeComponent(
                    nodePosition = nodePosition,
                    userName = embedding.userName,
                    profileImageUrl = embedding.profileImageUrl,
                    isSelected = selectedUserId == nodePosition.userId,
                    similarity = nodePosition.similarityScore,
                    onNodeClick = { onNodeClick(nodePosition.userId) },
                    onNodeLongClick = { onNodeLongClick(nodePosition.userId) }
                )
            }
        }
    }
}

/**
 * 중앙 노드 (현재 사용자)
 * - 72x72px
 * - 그라데이션 배경 (#667EEA → #764BA2)
 * - "나" 텍스트
 */
@Composable
fun CenterNodeComponent(
    selectedUserId: String? = null,
    onNodeClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                ),
                shape = CircleShape
            )
            .clickable { onNodeClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "나",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 다른 사용자 노드
 * - 크기: 유사도 기반 (40~56px)
 * - 색상: 유사도 기반
 *   - 0.7 이상: #10B981 (초록색)
 *   - 0.5~0.7: #10B981 (초록색)
 *   - 0.3~0.5: #F59E0B (주황색)
 *   - 0.3 미만: #E5E7EB (회색)
 * - 위치: 중앙에서 거리 (유사도 기반)
 */
@Composable
fun OtherUserNodeComponent(
    nodePosition: GraphNodePosition,
    userName: String,
    profileImageUrl: String? = null,
    isSelected: Boolean = false,
    similarity: Float = 0.5f,
    onNodeClick: () -> Unit = {},
    onNodeLongClick: () -> Unit = {}
) {
    val nodeSize = GraphLayoutCalculator.calculateNodeSize(similarity)
    val nodeColor = GraphLayoutCalculator.selectNodeColor(similarity)
    
    val xOffset = (nodePosition.x - 195).dp  // 390/2 = 195
    val yOffset = (nodePosition.y - 260).dp  // 520/2 = 260

    Column(
        modifier = Modifier
            .offset(x = xOffset, y = yOffset)
            .size(nodeSize.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(nodeSize.dp)
                .background(
                    color = Color(android.graphics.Color.parseColor(nodeColor)),
                    shape = CircleShape
                )
                .clickable { onNodeClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.take(2),  // 이름 첫 2글자
                color = Color.White,
                fontSize = if (nodeSize >= 48) 13.sp else 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * 연결선 컴포넌트 (선택사항)
 * - 중앙 노드와 다른 노드들을 연결하는 선
 * - 유사도에 따라 선의 투명도 조정
 */
@Composable
fun ConnectionLineComponent(
    fromX: Float,
    fromY: Float,
    toX: Float,
    toY: Float,
    similarity: Float = 0.5f
) {
    Box(
        modifier = Modifier
            .size(1.dp)
            .background(
                color = Color.Gray.copy(alpha = similarity * 0.5f)
            )
    )
}
