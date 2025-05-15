package com.example.login.Activity


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login.Model.Order // Cần tạo Model này
import com.example.login.OrderHistoryAdapter
import com.example.login.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileIconImageView: ImageView
    private lateinit var profileNameTextView: TextView
    private lateinit var profileEmailTextView: TextView
    private lateinit var orderHistoryRecyclerView: RecyclerView
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val orderList = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        profileIconImageView = findViewById(R.id.profileIcon)
        profileNameTextView = findViewById(R.id.profileName)
        profileEmailTextView = findViewById(R.id.profileEmail)
        orderHistoryRecyclerView = findViewById(R.id.orderHistoryRecyclerView)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        orderHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        orderHistoryAdapter = OrderHistoryAdapter(orderList) { orderId ->
            val intent = Intent(this, SimpleOrderDetailActivity::class.java)
            intent.putExtra("orderId", orderId)
            Log.d("ProfileActivity", "Đang gửi orderId: $orderId")
            startActivity(intent)
        }
        orderHistoryRecyclerView.adapter = orderHistoryAdapter

        setupBottomNavigationView()
        loadUserProfile()
        loadOrderHistory()
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        Log.d("Profile", "Loading profile for UID: ${currentUser?.uid}")
        currentUser?.let { user ->
            firestore.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    Log.d("Profile", "Firestore document loaded: ${document.data}")
                    if (document.exists()) {
                        val fullName = document.getString("fullName")
                        Log.d("Profile", "Full Name from Firestore: $fullName")
                        profileNameTextView.text = fullName ?: "Người dùng"
                        profileEmailTextView.text = document.getString("email") ?: ""
                        // Load ảnh đại diện nếu có
                    } else {
                        Log.d("Profile", "Document for UID ${user.uid} không tồn tại trong Firestore.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Profile", "Lỗi khi tải thông tin người dùng từ Firestore", e)
                }
        }
    }

    private fun loadOrderHistory() {
        val currentUser = auth.currentUser
        currentUser?.uid?.let { userId ->
            firestore.collection("checkouts")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    orderList.clear()
                    for (document in querySnapshot) {
                        val orderId = document.getString("orderId") ?: ""
                        val timestamp = document.getTimestamp("timestamp")
                        val totalAmount = document.getDouble("total") ?: 0.0
                        val paymentStatus = document.getString("paymentStatus") ?: "Đang xử lý"
                        val shippingStatus = document.getString("shippingStatus") ?: "Chờ xử lý" // Lấy trạng thái vận chuyển

                        Log.d("OrderHistory", "Order ID: $orderId, Payment Status: $paymentStatus, Shipping Status: $shippingStatus") // Kiểm tra log

                        val formattedDate = timestamp?.let {
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            sdf.format(Date(it.seconds * 1000))
                        } ?: ""

                        // Cập nhật Model Order để bao gồm shippingStatus
                        val order = Order(orderId, formattedDate, totalAmount, paymentStatus, shippingStatus)
                        orderList.add(order)
                    }
                    orderHistoryAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("Profile", "Lỗi khi tải lịch sử đơn hàng", e)
                }
        }
    }

    private fun setupBottomNavigationView() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.drinkMenu -> {
                    val intent = Intent(this, DrinkMenuActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.favorites -> {
                    val intent = Intent(this, FavoritesActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.myCart -> {
                    val intent = Intent(this, CartActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}