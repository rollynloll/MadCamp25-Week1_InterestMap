package com.example.madclass01.presentation.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TagInputField(
    onAddTag: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "관심사를 입력해주세요"
) {
    var inputValue by remember { mutableStateOf(TextFieldValue("")) }
    var isFocused by remember { mutableStateOf(false) }

    val borderColor = if (isFocused) Color(0xFFFF9945) else Color(0xFFE0E0E0)
    val borderWidth = if (isFocused) 1.5.dp else 1.dp
    
    // Function to handle addition
    fun handleAdd() {
        if (inputValue.text.isNotBlank()) {
            onAddTag(inputValue.text.trim())
            inputValue = TextFieldValue("")
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (inputValue.text.isEmpty()) {
                Text(
                    text = placeholderText,
                    color = Color(0xFF999999),
                    fontSize = 15.sp
                )
            }
            
            BasicTextField(
                value = inputValue,
                onValueChange = { newValue ->
                    if (newValue.text.length <= 15) { // Limit tag length
                        inputValue = newValue
                    }
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 15.sp,
                    color = Color(0xFF333333)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { handleAdd() }),
                cursorBrush = SolidColor(Color(0xFFFF9945)),
                modifier = Modifier
                     .fillMaxWidth()
                     .onFocusChanged { isFocused = it.isFocused }
            )
        }
        
        IconButton(
            onClick = { handleAdd() },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "추가",
                tint = if (inputValue.text.isNotEmpty()) Color(0xFFFF9945) else Color(0xFFCCCCCC),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
