package com.example.login.Model

data class CartItem(
    val documentId: String, // Firestore document ID
    val drinkId: String,
    val name: String,
    val price: Double,
    var quantity: Int,
    val imageResId: String
)