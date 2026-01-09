package com.example.madclass01.domain.model

data class Tag(
    val id: String = "",
    val name: String = "",
    val category: String = "", // "hobby" 또는 "interest"
    val isSelected: Boolean = false
)
