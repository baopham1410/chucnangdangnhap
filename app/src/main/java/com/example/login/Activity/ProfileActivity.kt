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
import com.google.firebase.firestore.ListenerRegistration
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
    private var userListener: ListenerRegistration? = null
    private var orderHistoryListener: ListenerRegistration? = null


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
        loadUserProfileRealtime()
        loadOrderHistoryRealtime()
    }

    override fun onStart() {
        super.onStart()
        loadUserProfileRealtime() // Gọi hàm lắng nghe thay đổi người dùng
        loadOrderHistoryRealtime() // Gọi hàm lắng nghe thay đổi lịch sử đơn hàng
    }

    override fun onStop() {
        super.onStop()
        userListener?.remove()
        userListener = null
        orderHistoryListener?.remove()
        orderHistoryListener = null
    }

    private fun loadUserProfileRealtime() {
        val currentUser = auth.currentUser
        currentUser?.uid?.let { userId ->
            val userDocumentRef = firestore.collection("users").document(userId)
            userListener = userDocumentRef.addSnapshotListener { documentSnapshot, e ->
                if (e != null) {
                    Log.e("Profile", "Lỗi khi lắng nghe thông tin người dùng", e)
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val fullName = documentSnapshot.getString("fullName")
                    profileNameTextView.text = fullName ?: "Người dùng"
                    profileEmailTextView.text = documentSnapshot.getString("email") ?: ""
                    // Cập nhật ảnh đại diện nếu có sự thay đổi
                } else {
                    Log.d("Profile", "Không có thông tin người dùng cập nhật.")
                }
            }
        }
    }

    private fun getPaymentStatusText(status: String): String {
        return when (status) {
            "pending" -> "Chờ Thanh Toán"
            "paid" -> "Đã Thanh Toán"
            "unpaid" -> "Chưa Thanh Toán"
            "refunded" -> "Đã Hoàn Tiền"
            else -> status // Trả về nguyên trạng nếu không khớp
        }
    }

    private fun loadOrderHistoryRealtime() {
        val currentUser = auth.currentUser
        currentUser?.uid?.let { userId ->
            val ordersCollectionRef = firestore.collection("checkouts")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)

            orderHistoryListener = ordersCollectionRef.addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.e("Profile", "Lỗi khi lắng nghe lịch sử đơn hàng", e)
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    orderList.clear()
                    for (document in querySnapshot) {
                        val orderId = document.getString("orderId") ?: ""
                        val timestamp = document.getTimestamp("timestamp")
                        val totalAmount = document.getDouble("total") ?: 0.0
                        val paymentStatusEng = document.getString("paymentStatus") ?: "Đang xử lý" // Lấy trạng thái tiếng Anh
                        val shippingStatus = document.getString("shippingStatus") ?: "Chờ xử lý"

                        val paymentStatusVi = getPaymentStatusText(paymentStatusEng) // Chuyển đổi sang tiếng Việt

                        val formattedDate = timestamp?.let {
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            sdf.format(Date(it.seconds * 1000))
                        } ?: ""

                        val order = Order(orderId, formattedDate, totalAmount, paymentStatusVi, shippingStatus)
                        orderList.add(order)
                    }
                    orderHistoryAdapter.notifyDataSetChanged()
                } else {
                    Log.d("Profile", "Không có lịch sử đơn hàng cập nhật.")
                }
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