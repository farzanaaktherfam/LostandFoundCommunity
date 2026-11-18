package com.example.lostandfoundcommunity

data class Post(
    var id: String = "",
    val uid: String = "",
    val username: String = "",
    val caption: String = "",
    val imageUrl: String = "",
    val publicId: String = "",
    val timestamp: Long = 0L
)