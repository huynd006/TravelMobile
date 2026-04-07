package com.example.midterm

data class Note(
    val noteId: String = "",
    val title: String = "",
    val description: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)