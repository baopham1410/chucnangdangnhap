package com.example.login.Model

data class Product(
    val name: String = "",
    val price: String = "",
    val image_url: String = "" // Đảm bảo trùng với Firestore
)
