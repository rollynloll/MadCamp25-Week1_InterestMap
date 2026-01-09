package com.example.madclass01.presentation.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.madclass01.domain.model.ImageItem

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun ImageGalleryGrid(
    images: List<ImageItem>,
    onRemoveImage: (String) -> Unit,
    onAddImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ImagePickerButton(onClick = onAddImage)

        images.forEach { imageItem ->
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
        modifier = modifier
            .size(100.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.5.dp,
                color = Color(0xFFDDDDDD),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = "프로필 이미지",
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentScale = ContentScale.Crop
        )
        
        // 삭제 버튼
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(28.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(50)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "이미지 삭제",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
