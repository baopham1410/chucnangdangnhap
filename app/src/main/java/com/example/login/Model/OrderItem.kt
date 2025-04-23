package com.example.login.Model

import java.io.Serializable

data class OrderItem(
    val drinkId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0
) : Serializable