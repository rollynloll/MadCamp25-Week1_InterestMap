package com.example.madclass01.presentation.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(2.dp) // Add slight padding to avoid clipping shadows
    ) {
        // Add Button
        item {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF9F9F9))
                    .clickable { onAddImage() },
                contentAlignment = Alignment.Center
            ) {
                 // Dashed border drawing to match ImagePickerButton
                androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                    val stroke = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    drawRoundRect(
                        color = Color(0xFFE0E0E0),
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                        style = stroke
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "사진 추가",
                    tint = Color(0xFFFF9945),
                    modifier = Modifier.size(28.dp)
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
    Box(modifier = modifier.aspectRatio(1f)) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Gallery Image",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFEEEEEE)),
            contentScale = ContentScale.Crop
        )
        
        // Remove Button with larger hit area
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(32.dp) // Increased touch target
                .clickable { onRemove() }
                .padding(4.dp), // Visual padding
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                modifier = Modifier.size(20.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
                contentColor = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
