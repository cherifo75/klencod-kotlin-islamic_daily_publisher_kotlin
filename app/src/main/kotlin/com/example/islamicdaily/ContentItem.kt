package com.example.islamicdaily

data class ContentItem(
    val id: String,
    val type: String,
    val text: String,
    val reference: String,
    val sourceUrl: String,
    val grade: String? = null,
    var approved: Boolean = false,
    var videoPath: String? = null
)
