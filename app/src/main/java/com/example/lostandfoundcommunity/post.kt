
package com.example.lostandfoundcommunity

data class Post(
    var id: String = "",
    var uid: String = "",
    var username: String = "",
    var caption: String = "",
    var imageUrl: String = "",
    var publicId: String = "",
    var timestamp: Long = 0L
)
