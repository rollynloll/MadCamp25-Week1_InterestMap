package com.example.madclass01.presentation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.madclass01.presentation.profile.viewmodel.ProfileUiState
import com.example.madclass01.presentation.profile.viewmodel.ProfileViewModel
import kotlin.random.Random

@Composable
fun ProfileScreen(
    userId: String? = null,
    nickname: String? = null,
    age: Int? = null,
    gender: String? = null,
    region: String? = null,
    bio: String? = null,
    images: List<String> = emptyList(),
    tags: List<String> = emptyList(),
    onEditClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onProfileLoaded: (
        nickname: String?,
        age: Int?,
        gender: String?,
        region: String?,
        bio: String?,
        images: List<String>,
        interests: List<String>,
        photoInterests: List<String>
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var lastEmitted by remember { mutableStateOf<ProfileUiState?>(null) }
    
    // State for Full Screen Image Viewer
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }

    // Handle system back press
    // If viewing an image OR if onBack is provided (e.g. from GroupDetail), intercept back press
    androidx.activity.compose.BackHandler(enabled = selectedImageIndex != null || onBack != null) {
        if (selectedImageIndex != null) {
            selectedImageIndex = null
        } else {
            onBack?.invoke()
        }
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            viewModel.loadProfile(userId)
        }
    }

    LaunchedEffect(uiState) {
        if (!uiState.isLoading && uiState.userId != null && lastEmitted != uiState) {
            lastEmitted = uiState
            onProfileLoaded(
                uiState.nickname ?: nickname,
                uiState.age ?: age,
                uiState.gender ?: gender,
                uiState.region ?: region,
                uiState.bio ?: bio,
                if (uiState.images.isNotEmpty()) uiState.images else images,
                uiState.interests,
                uiState.photoInterests
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val currentImages = if (uiState.images.isNotEmpty()) uiState.images else images
            
            ProfileContent(
                nickname = uiState.nickname ?: nickname,
                age = uiState.age ?: age,
                gender = uiState.gender ?: gender,
                region = uiState.region ?: region,
                bio = uiState.bio ?: bio,
                images = currentImages,
                tags = if (uiState.tags.isNotEmpty()) uiState.tags else tags,
                onEditClick = onEditClick,
                onBack = onBack,
                onImageClick = { index -> selectedImageIndex = index }
            )
            
            // Full Screen Image Viewer Overlay
            if (selectedImageIndex != null) {
                com.example.madclass01.presentation.common.screen.FullScreenImageViewer(
                    imageUrls = currentImages,
                    initialPage = selectedImageIndex!!,
                    onDismiss = { selectedImageIndex = null }
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    nickname: String? = null,
    age: Int? = null,
    gender: String? = null,
    region: String? = null,
    bio: String? = null,
    images: List<String> = emptyList(),
    tags: List<String> = emptyList(),
    onEditClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onImageClick: (Int) -> Unit = {}
) {
    // Defines a modern gradient for the header
    val headerBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFF9945), // Original Orange
            Color(0xFFFFB775)  // Lighter Orange
        )
    )
    
    var showAllTags by remember { mutableStateOf(false) }

    if (showAllTags) {
        AllTagsDialog(tags = tags, onDismiss = { showAllTags = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9)) // Slightly off-white background for depth
    ) {
        // --- Header Section (Compacted) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(headerBrush, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .statusBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Combined Header Section (Back Button, Profile Image, Edit Button)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp) // Reduced padding
                ) {
                    // Back Button (Top Aligned)
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .align(Alignment.TopStart)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }

                    // Profile Image (Centered, Larger)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(140.dp) // Increased size
                            .border(3.dp, Color.White, CircleShape)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .align(Alignment.Center)
                    ) {
                        if (images.isNotEmpty()) {
                            AsyncImage(
                                model = images.first(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = nickname?.take(2) ?: "MY",
                                fontSize = 48.sp, // Increased font size for larger circle
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9945)
                            )
                        }
                    }

                    // Edit Button (Top Right Aligned)
                    if (onEditClick != null) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp)) // Reduced spacing to name

                // Name and Gender
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = nickname ?: "Anonymous",
                        fontSize = 22.sp, // Reduced from 26.sp
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    when (gender?.lowercase()) {
                        "male", "남성" -> Icon(
                            imageVector = Icons.Default.Male,
                            contentDescription = "Male",
                            tint = Color(0xFFE3F2FD),
                            modifier = Modifier.size(20.dp)
                        )
                        "female", "여성" -> Icon(
                            imageVector = Icons.Default.Female,
                            contentDescription = "Female",
                            tint = Color(0xFFFCE4EC),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Age and Region
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (age != null) {
                        Text(
                            text = "${age}세",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        if (region != null) {
                            Text(
                                text = "  |  ",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                    if (region != null) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = region,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bio
                if (!bio.isNullOrBlank()) {
                    Text(
                        text = bio,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.95f),
                        textAlign = TextAlign.Center,
                        maxLines = 2, // Reduced lines
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(vertical = 6.dp, horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tags (FlowRow) - Compact & "More" button
                if (tags.isNotEmpty()) {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val maxTagsToShow = 5
                        val displayTags = if (tags.size > maxTagsToShow) tags.take(maxTagsToShow - 1) else tags
                        
                        displayTags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 3.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFF9945),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                        
                        // "More" Button
                        if (tags.size > maxTagsToShow) {
                           Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.3f), // Slightly distinct
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .clickable { showAllTags = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "${tags.size - (maxTagsToShow - 1)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            } 
                        }
                    }
                }
            }
        }

        // --- Gallery Section ---
        Column(
            modifier = Modifier
                .fillMaxSize() // Takes remaining space
                .weight(1f) // Ensure it pushes down if needed, but in standard Column fillMaxSize works if children don't overflow
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = Color(0xFFFF9945),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mood & Interest",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${images.size} Photos",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    )
                )
            }

            // Staggered Grid for Pinterest-like look
            if (images.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Fill remaining space in this column
                        .background(Color(0xFFEEEEEE), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                         Text(
                            text = "No photos yet \uD83D\uDCF8",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    verticalItemSpacing = 12.dp,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(images) { index, imageUri ->
                        GalleryItem(
                            imageUri = imageUri,
                            onClick = { onImageClick(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllTagsDialog(
    tags: List<String>,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "All Interests",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9945)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFFF9945).copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Color(0xFFFF9945).copy(alpha = 0.5f)),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE65100),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                androidx.compose.material3.TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color(0xFF333333), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun GalleryItem(
    imageUri: String,
    onClick: () -> Unit
) {
    // Generate a pseudo-random aspect ratio based on the string hash to keep it consistent but staggered-looking
    val randomHeightRatio = remember(imageUri) { 
        Random(imageUri.hashCode()).nextDouble(0.8, 1.4).toFloat() 
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Gallery Image",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(randomHeightRatio) // Simulate different image heights
                .background(Color(0xFFEEEEEE)),
            contentScale = ContentScale.Crop
        )
    }
}
