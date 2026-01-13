package com.example.madclass01.presentation.common.screen

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageViewer(
    imageUrls: List<String>,
    initialPage: Int = 0,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { imageUrls.size })
    var isControlsVisible by remember { mutableStateOf(true) }

    var isScrollEnabled by remember { mutableStateOf(true) }

    // System Back Handler
    BackHandler(onBack = onDismiss)

    // Use Dialog with full screen properties
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Image Carousel
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = isScrollEnabled
            ) { page ->
                ZoomableImage(
                    imageUrl = imageUrls[page],
                    isVisible = page == pagerState.currentPage,
                    onTap = { isControlsVisible = !isControlsVisible },
                    scrollEnabled = { enabled -> 
                        isScrollEnabled = enabled
                    }
                )
            }

            // Top Bar
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "${pagerState.currentPage + 1} / ${imageUrls.size}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    
                    // Empty box to balance layout if needed, or just let spacer do it
                    Spacer(modifier = Modifier.size(48.dp)) 
                }
            }

            // Bottom Bar
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp), // Increased bottom padding
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share Button
                    IconButton(
                        onClick = {
                            val currentUrl = imageUrls[pagerState.currentPage]
                            shareImage(context, currentUrl)
                        },
                         modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }

                    // Save Button
                    IconButton(
                        onClick = {
                            val currentUrl = imageUrls[pagerState.currentPage]
                            scope.launch {
                                saveImageToGallery(context, currentUrl)
                            }
                        },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Save",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomableImage(
    imageUrl: String,
    isVisible: Boolean,
    onTap: () -> Unit,
    scrollEnabled: (Boolean) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    
    // Determine if zoomed in (with tolerance)
    val isZoomed = scale > 1.01f

    // Notify parent to enable/disable pager scroll
    LaunchedEffect(isZoomed, isVisible) {
        if (isVisible) {
            scrollEnabled(!isZoomed)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            // Visual transformation
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
            // Tap handling - does NOT block 1-finger swipe
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onTap() },
                onDoubleClick = {
                    if (scale > 1.01f) {
                        scale = 1f
                        offset = Offset.Zero
                    } else {
                        scale = 2.5f
                    }
                }
            )
            // Custom 2-finger zoom gesture - does NOT consume 1-finger events
            .pointerInput(Unit) {
                awaitEachGesture {
                    // Wait for first finger down
                    val firstDown = awaitFirstDown(requireUnconsumed = false)
                    
                    // Wait to see if a second finger comes down
                    var secondPointer: androidx.compose.ui.input.pointer.PointerId? = null
                    var event = awaitPointerEvent()
                    
                    // Check if we have 2+ fingers within a small window
                    while (event.changes.size < 2) {
                        val change = event.changes.firstOrNull { it.id != firstDown.id && it.pressed }
                        if (change != null) {
                            secondPointer = change.id
                            break
                        }
                        
                        // If the first finger is released before second touches, exit
                        if (event.changes.none { it.id == firstDown.id && it.pressed }) {
                            return@awaitEachGesture // Let other handlers process this
                        }
                        
                        // If movement exceeds threshold with 1 finger, it's a swipe - let pager handle it
                        val dragDistance = (event.changes.first().position - firstDown.position).getDistance()
                        if (dragDistance > 20f) {
                            return@awaitEachGesture // Exit without consuming - allow pager swipe
                        }
                        
                        event = awaitPointerEvent()
                    }
                    
                    // We have 2+ fingers - now handle pinch zoom
                    if (event.changes.size >= 2) {
                        var previousCentroid = event.calculateCentroid()
                        var previousSpan = calculateSpan(event)
                        
                        do {
                            event = awaitPointerEvent()
                            val currentCentroid = event.calculateCentroid()
                            val currentSpan = calculateSpan(event)
                            
                            if (previousSpan > 0f && currentSpan > 0f) {
                                val zoomChange = currentSpan / previousSpan
                                val newScale = (scale * zoomChange).coerceIn(1f, 5f)
                                scale = newScale
                                
                                if (newScale > 1.01f) {
                                    val panChange = currentCentroid - previousCentroid
                                    val maxX = (size.width * (newScale - 1)) / 2f
                                    val maxY = (size.height * (newScale - 1)) / 2f
                                    val newOffset = offset + panChange
                                    offset = Offset(
                                        x = newOffset.x.coerceIn(-maxX, maxX),
                                        y = newOffset.y.coerceIn(-maxY, maxY)
                                    )
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                            
                            previousCentroid = currentCentroid
                            previousSpan = currentSpan
                            
                            // Consume events to prevent other handlers
                            event.changes.forEach { it.consume() }
                        } while (event.changes.any { it.pressed })
                    }
                }
            }
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding() // Ensure image fits within safe area (excludes status/nav bars)
        )
    }
}

private fun calculateSpan(event: androidx.compose.ui.input.pointer.PointerEvent): Float {
    val pointers = event.changes.filter { it.pressed }
    if (pointers.size < 2) return 0f
    val dx = pointers[0].position.x - pointers[1].position.x
    val dy = pointers[0].position.y - pointers[1].position.y
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

private fun shareImage(context: Context, imageUrl: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, imageUrl) // Sharing URL as text for simplicity
        type = "text/plain" 
        // If we want to share the actual image binary, we need to download it first and get a URI.
        // For now, sharing the URL is safer and faster unless requested otherwise.
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Image")
    context.startActivity(shareIntent)
}

private suspend fun saveImageToGallery(context: Context, imageUrl: String) {
    withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Disable hardware bitmaps for verified saving
                .build()

            val result = loader.execute(request)
            val drawable = result.drawable
            
            if (drawable != null) {
                val bitmap = drawable.toBitmap()
                val filename = "IMG_${System.currentTimeMillis()}.jpg"
                var fos: OutputStream? = null
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { context.contentResolver.openOutputStream(it) }
                } else {
                    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val image = java.io.File(imagesDir, filename)
                    fos = java.io.FileOutputStream(image)
                }
                
                fos?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                 withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error saving image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
