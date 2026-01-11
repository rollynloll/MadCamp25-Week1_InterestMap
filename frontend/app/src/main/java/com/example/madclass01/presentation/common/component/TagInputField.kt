package com.example.madclass01.presentation.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TagInputField(
    onAddTag: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "관심사를 입력하세요"
) {
    var inputValue by remember { mutableStateOf(TextFieldValue("")) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(
                width = 1.5.dp,
                color = Color(0xFFFF9945),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BasicTextField(
            value = inputValue,
            onValueChange = { newValue ->
                if (newValue.text.length <= 20) {
                    inputValue = newValue
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(40.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputValue.text.isEmpty()) {
                        Text(
                            text = placeholderText,
                            fontSize = 14.sp,
                            color = Color(0xFFCCCCCC)
                        )
                    }
                    innerTextField()
                }
            },
            singleLine = true,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                fontSize = 14.sp,
                color = Color(0xFF333333)
            )
        )
        
        IconButton(
            onClick = {
                if (inputValue.text.isNotEmpty()) {
                    onAddTag(inputValue.text.trim())
                    inputValue = TextFieldValue("")
                }
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "태그 추가",
                tint = Color(0xFFFF9945),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
