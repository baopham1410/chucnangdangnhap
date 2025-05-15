package com.example.login.Model

data class Order(
    val orderId: String = "",
    val orderDate: String = "",
    val totalAmount: Double = 0.0,
    val paymentStatus: String = "", // Thêm thuộc tính này với giá trị mặc định
    val shippingStatus: String = "" // Thêm trường này
)