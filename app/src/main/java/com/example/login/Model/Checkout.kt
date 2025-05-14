package com.example.login.Model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Checkout(
    @DocumentId val id: String = "",
    val userId: String = "",
    @ServerTimestamp val timestamp: Date? = null,
    val deliveryAddress: String = "",
    val totalAmount: Double = 0.0,
    val items: List<Map<String, Any>> = emptyList() // Ví dụ: Danh sách các sản phẩm
    // Hoặc val orderDetails: List<OrderDetail> = emptyList()
)