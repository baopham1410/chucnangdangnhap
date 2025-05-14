package com.example.login.Activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.login.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import android.util.Log
import android.content.Intent
import com.example.login.Model.CartItem

class QRCodePaymentActivity : AppCompatActivity() {

    private lateinit var qrCodeImageView: ImageView
    private lateinit var paymentDoneButton: MaterialButton
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thanhtoanqr)

        qrCodeImageView = findViewById(R.id.qrCodeImageView)
        paymentDoneButton = findViewById(R.id.paymentDoneButton)

        // Tải hình ảnh mã QR (thay thế bằng URL hoặc resource ID của bạn)
        Glide.with(this)
            .load(R.drawable.qr) // Thay thế bằng hình ảnh thực tế
            .into(qrCodeImageView)

        val orderId = intent.getStringExtra("orderId")

        paymentDoneButton.setOnClickListener {
            if (!orderId.isNullOrEmpty()) {
                updatePaymentStatus(orderId)
            } else {
                Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePaymentStatus(orderId: String) {
        firestore.collection("checkouts")
            .whereEqualTo("orderId", orderId)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentId = querySnapshot.documents[0].id
                    firestore.collection("checkouts")
                        .document(documentId)
                        .update("paymentStatus", "Đã thanh toán đang chờ xác nhận")
                        .addOnSuccessListener {
                            Toast.makeText(this, "Đã xác nhận thanh toán", Toast.LENGTH_SHORT).show()
                            completeOrderAndClearCart()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Lỗi khi cập nhật trạng thái thanh toán", e)
                            Toast.makeText(this, "Lỗi khi xác nhận thanh toán", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Không tìm thấy đơn hàng với mã: $orderId", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Lỗi khi truy vấn đơn hàng", e)
                Toast.makeText(this, "Lỗi khi kiểm tra đơn hàng", Toast.LENGTH_SHORT).show()
            }
    }

    private fun completeOrderAndClearCart() {
        currentUser?.uid?.let { userId ->
            firestore.collection("cart_items")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val cartBatch = firestore.batch()
                    val cartItemsListForUpdate = mutableListOf<CartItem>()
                    for (document in querySnapshot) {
                        val cartItem = document.toObject(CartItem::class.java)
                        cartItemsListForUpdate.add(cartItem)
                        cartBatch.delete(document.reference)
                    }

                    val batchUpdateStock = firestore.batch()
                    for (cartItem in cartItemsListForUpdate) {
                        val drinkDocumentRef = firestore.collection("drink").document(cartItem.drinkId)
                        batchUpdateStock.update(
                            drinkDocumentRef,
                            "quantity",
                            FieldValue.increment(-cartItem.quantity.toLong())
                        )
                    }

                    batchUpdateStock.commit()
                        .addOnSuccessListener {
                            Log.d("Firestore", "Đã cập nhật số lượng kho")
                            cartBatch.commit()
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Giỏ hàng đã được xóa")
                                    val intent = Intent(this@QRCodePaymentActivity, HomeActivity::class.java)
                                    startActivity(intent)
                                    finishAffinity()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Lỗi khi xóa giỏ hàng", e)
                                    Toast.makeText(this@QRCodePaymentActivity, "Lỗi khi xóa giỏ hàng", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Lỗi khi cập nhật số lượng kho", e)
                            Toast.makeText(this@QRCodePaymentActivity, "Lỗi khi cập nhật kho", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Lỗi khi truy vấn giỏ hàng", e)
                    Toast.makeText(this@QRCodePaymentActivity, "Lỗi khi lấy giỏ hàng", Toast.LENGTH_SHORT).show()
                }
        }
    }
}