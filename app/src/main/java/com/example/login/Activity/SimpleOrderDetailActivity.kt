package com.example.login.Activity

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login.Adapter.SimpleOrderDetailAdapter
import com.example.login.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SimpleOrderDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var orderIdTextView: TextView
    private lateinit var deliveryAddressTextView: TextView
    private lateinit var orderItemsRecyclerView: RecyclerView
    private lateinit var totalAmountTextView: TextView
    private lateinit var simpleOrderDetailAdapter: SimpleOrderDetailAdapter

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_order_detail)
        simpleOrderDetailAdapter = SimpleOrderDetailAdapter(mutableListOf())
        Log.d("SimpleOrderDetailActivity", "onCreate() được gọi")

        initializeViews()

        val orderId = intent.getStringExtra("orderId") // Sử dụng "ORDER_ID" (viết hoa)

        Log.d("SimpleOrderDetailActivity", "orderId nhận được từ Intent: $orderId")

        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("SimpleOrderDetailActivity", "Người dùng đã đăng nhập với UID: ${currentUser.uid}")
            orderId?.let {
                loadOrderDetails(it)
            } ?: run {
                Log.w("SimpleOrderDetailActivity", "Không có orderId được truyền vào Intent")
                Toast.makeText(this, "Lỗi: Không có mã đơn hàng", Toast.LENGTH_SHORT).show()
                // Có thể finish() Activity ở đây nếu không có orderId là không hợp lệ
            }
        } else {
            Log.w("SimpleOrderDetailActivity", "Người dùng chưa đăng nhập, không thể tải chi tiết đơn hàng.")
            Toast.makeText(this, "Bạn cần đăng nhập để xem chi tiết đơn hàng.", Toast.LENGTH_SHORT).show()
            // Có thể finish() Activity ở đây để ngăn người dùng xem trang này khi chưa đăng nhập
        }

        kiemTraSimpleOrderDetailActivityDaChay()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        orderIdTextView = findViewById(R.id.orderIdTextView)
        deliveryAddressTextView = findViewById(R.id.deliveryAddressTextView)
        orderItemsRecyclerView = findViewById(R.id.orderItemsRecyclerView)
        totalAmountTextView = findViewById(R.id.totalAmountTextView)

        orderItemsRecyclerView.layoutManager = LinearLayoutManager(this)
        orderItemsRecyclerView.adapter = simpleOrderDetailAdapter
    }
    private fun loadOrderDetails(orderId: String) {
        Log.d("SimpleOrderDetailActivity", "Bắt đầu tải chi tiết đơn hàng với ID: $orderId")

        firestore.collection("checkouts")
            .whereEqualTo("orderId", orderId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents.first() // Lấy document đầu tiên (và duy nhất)
                    Log.d("OrderDetailDebug", "DocumentSnapshot tồn tại: ${documentSnapshot.exists()}")

                    val deliveryAddress = documentSnapshot.getString("deliveryAddress")
                    Log.d("OrderDetailDebug", "deliveryAddress: $deliveryAddress")

                    // **Sửa ở đây: Lấy giá trị từ trường "total"**
                    val totalAmount = documentSnapshot.getDouble("total")
                    Log.d("OrderDetailDebug", "totalAmount: $totalAmount")

                    val items = documentSnapshot.get("items") as? List<Map<String, Any>>
                    Log.d("OrderDetailDebug", "items: $items")

                    orderIdTextView.text = "Mã đơn hàng: #${documentSnapshot.getString("orderId")}"
                    deliveryAddressTextView.text = "Địa chỉ: $deliveryAddress"

                    // **Sửa ở đây: Hiển thị totalAmount**
                    totalAmountTextView.text = String.format("%,.0f VNĐ", totalAmount ?: 0.0)

                    simpleOrderDetailAdapter.updateData(items ?: emptyList())
                    Log.d("SimpleOrderDetailActivity", "Dữ liệu đơn hàng đã tải thành công")
                } else {
                    Log.d("Firestore", "Không tìm thấy đơn hàng với orderId: $orderId")
                    Toast.makeText(this, "Lỗi: Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Lỗi khi tải chi tiết đơn hàng", e)
                Toast.makeText(this, "Lỗi tải dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Hàm kiểm tra xem Activity đã chạy hay chưa
    private fun kiemTraSimpleOrderDetailActivityDaChay() {
        Log.i("SimpleOrderDetailActivity", "Hàm kiemTraSimpleOrderDetailActivityDaChay() được gọi. Trang chi tiết đơn hàng có vẻ đã được khởi chạy.")
        Toast.makeText(this, "Trang chi tiết đơn hàng đã chạy", Toast.LENGTH_SHORT).show()
    }
}