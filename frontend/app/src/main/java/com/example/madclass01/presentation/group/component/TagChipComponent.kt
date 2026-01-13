package com.example.madclass01.presentation.group.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.madclass01.presentation.common.component.TagChip

@Composable
fun TagChip(
    tag: String,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TagChip(
        label = tag,
        isSelected = true, // Force selected style for group creation context if desired, or false.
        onRemove = { onRemove(tag) },
        onToggle = null, // Not togglable here
        modifier = modifier
    )
}
