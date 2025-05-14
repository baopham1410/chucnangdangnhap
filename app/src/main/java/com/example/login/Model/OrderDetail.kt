package com.example.login.Model

data class OrderDetail(
    val name: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val imageUrl: String? = null // Thêm trường imageUrl
)