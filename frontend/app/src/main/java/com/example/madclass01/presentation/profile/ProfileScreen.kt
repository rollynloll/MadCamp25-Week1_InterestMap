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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        ProfileContent(
            nickname = uiState.nickname ?: nickname,
            age = uiState.age ?: age,
            gender = uiState.gender ?: gender,
            region = uiState.region ?: region,
            bio = uiState.bio ?: bio,
            images = if (uiState.images.isNotEmpty()) uiState.images else images,
            tags = if (uiState.tags.isNotEmpty()) uiState.tags else tags,
            onEditClick = onEditClick,
            onBack = onBack
        )
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
    onBack: (() -> Unit)? = null
) {
    // Defines a modern gradient for the header
    val headerBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFF9945), // Original Orange
            Color(0xFFFFB775)  // Lighter Orange
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9)) // Slightly off-white background for depth
    ) {
        // --- Header Section ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(headerBrush, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .statusBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Bar with Edit Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Button
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }

                    // Edit Button
                    if (onEditClick != null) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = Color.White
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }

                // Profile Image with Border
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(120.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color.White)
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
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9945)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name and Gender
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = nickname ?: "Anonymous",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    when (gender?.lowercase()) {
                        "male", "남성" -> Icon(
                            imageVector = Icons.Default.Male,
                            contentDescription = "Male",
                            tint = Color(0xFFE3F2FD),
                            modifier = Modifier.size(24.dp)
                        )
                        "female", "여성" -> Icon(
                            imageVector = Icons.Default.Female,
                            contentDescription = "Female",
                            tint = Color(0xFFFCE4EC),
                            modifier = Modifier.size(24.dp)
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
                            fontSize = 15.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        if (region != null) {
                            Text(
                                text = "  |  ",
                                fontSize = 15.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                    if (region != null) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = region,
                            fontSize = 15.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bio
                if (!bio.isNullOrBlank()) {
                    Text(
                        text = bio,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.95f),
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tags (FlowRow)
                if (tags.isNotEmpty()) {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tags.take(5).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 3.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFF9945),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Gallery Section ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
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
                    text = "My Gallery",
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
                        .height(200.dp)
                        .background(Color(0xFFEEEEEE), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No photos yet \uD83D\uDCF8",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    verticalItemSpacing = 12.dp,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(images) { imageUri ->
                        // Calculate a random aspect ratio for demo if we don't have real intrinsic sizes,
                        // OR just respect the content mostly. 
                        // Since we are loading from URL, we often just fit width. 
                        // To simulate Pinterest "staggered" feel even with same-size images, 
                        // we can change aspect ratio slightly or just rely on the content.
                        // Here, we'll let AsyncImage handle it but wrap in a card.
                        
                        GalleryItem(imageUri = imageUri)
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryItem(imageUri: String) {
    // Generate a pseudo-random aspect ratio based on the string hash to keep it consistent but staggered-looking
    // If the image itself has varying dimensions, `ContentScale.Crop` with a fixed ratio is common, 
    // OR `ContentScale.FillWidth` with `wrapContentHeight`.
    // For a requested "Pinterest" look, we usually want variable heights.
    // Since Coil needs to load first to know size, simple staggered grid often needs a hint or we just random-size the placeholder.
    // For now, I will use a randomized height factor to simulated the "Pinterest" look visually.
    
    val randomHeightRatio = remember(imageUri) { 
        Random(imageUri.hashCode()).nextDouble(0.8, 1.4).toFloat() 
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
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
