package com.example.madclass01.presentation.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.madclass01.domain.model.ImageItem

@Composable
fun ImageGalleryGrid(
    images: List<ImageItem>,
    onRemoveImage: (String) -> Unit,
    onAddImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxWidth()
            .height((((images.size + 1) / 3 + 1) * 130).dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 추가 버튼
        item {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8E8E8))
                    .clickable { onAddImage() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "사진 추가",
                    tint = Color(0xFF999999),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        items(images) { imageItem ->
            ImageThumbnail(
                imageUri = imageItem.uri,
                onRemove = { onRemoveImage(imageItem.uri) }
            )
        }
    }
}

@Composable
fun ImageThumbnail(
    imageUri: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.aspectRatio(1f)
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = "갤러리 이미지",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE8E8E8)),
            contentScale = ContentScale.Crop
        )
        
        // 작은 삭제 버튼
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(18.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(50)
                )
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "이미지 삭제",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
