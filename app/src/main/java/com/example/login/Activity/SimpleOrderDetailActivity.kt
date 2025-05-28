package com.example.login.Activity

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login.Adapter.SimpleOrderDetailAdapter
import com.example.login.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SimpleOrderDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var orderIdTextView: TextView
    private lateinit var deliveryAddressTextView: TextView
    private lateinit var orderItemsRecyclerView: RecyclerView
    private lateinit var totalAmountTextView: TextView
    private lateinit var simpleOrderDetailAdapter: SimpleOrderDetailAdapter
    private lateinit var confirmReceivedButton: MaterialButton

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_order_detail)
        simpleOrderDetailAdapter = SimpleOrderDetailAdapter(mutableListOf())
        Log.d("SimpleOrderDetailActivity", "onCreate() được gọi")

        initializeViews()

        val orderId = intent.getStringExtra("orderId")

        Log.d("SimpleOrderDetailActivity", "orderId nhận được từ Intent: $orderId")

        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("SimpleOrderDetailActivity", "Người dùng đã đăng nhập với UID: ${currentUser.uid}")
            orderId?.let {
                loadOrderDetails(it)
            } ?: run {
                Log.w("SimpleOrderDetailActivity", "Không có orderId được truyền vào Intent")
                Toast.makeText(this, "Lỗi: Không có mã đơn hàng", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w("SimpleOrderDetailActivity", "Người dùng chưa đăng nhập, không thể tải chi tiết đơn hàng.")
            Toast.makeText(this, "Bạn cần đăng nhập để xem chi tiết đơn hàng.", Toast.LENGTH_SHORT).show()
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
        confirmReceivedButton = findViewById(R.id.confirmReceivedButton)

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
                    val documentSnapshot = querySnapshot.documents.first()
                    Log.d("OrderDetailDebug", "DocumentSnapshot tồn tại: ${documentSnapshot.exists()}")

                    val deliveryAddress = documentSnapshot.getString("deliveryAddress")
                    Log.d("OrderDetailDebug", "deliveryAddress: $deliveryAddress")

                    val totalAmount = documentSnapshot.getDouble("total")
                    Log.d("OrderDetailDebug", "totalAmount: $totalAmount")

                    val items = documentSnapshot.get("items") as? List<Map<String, Any>>
                    Log.d("OrderDetailDebug", "items: $items")

                    val shippingStatus = documentSnapshot.getString("shippingStatus") ?: "Chờ xử lý"
                    Log.d("SimpleOrderDetailActivity", "Trạng thái vận chuyển: $shippingStatus")

                    orderIdTextView.text = "Mã đơn hàng: #${documentSnapshot.getString("orderId")}"
                    deliveryAddressTextView.text = "Địa chỉ: $deliveryAddress"
                    totalAmountTextView.text = String.format("%,.0f VNĐ", totalAmount ?: 0.0)

                    simpleOrderDetailAdapter.updateData(items ?: emptyList())
                    Log.d("SimpleOrderDetailActivity", "Dữ liệu đơn hàng đã tải thành công")

                    // Cập nhật trạng thái nút dựa trên shippingStatus
                    if (shippingStatus == "shipping") {
                        confirmReceivedButton.isEnabled = true
                        confirmReceivedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.orange)) // Màu gốc
                        confirmReceivedButton.setTextColor(Color.WHITE)
                        confirmReceivedButton.setOnClickListener {
                            showConfirmationDialog(orderId)
                        }
                    } else {
                        confirmReceivedButton.isEnabled = false
                        confirmReceivedButton.setBackgroundColor(Color.GRAY) // Màu xám
                        confirmReceivedButton.setTextColor(Color.DKGRAY)
                        confirmReceivedButton.setOnClickListener(null) // Vô hiệu hóa click
                        if (shippingStatus == "Đã giao") {
                            confirmReceivedButton.text = "Đơn hàng đã hoàn thành"
                        } else {
                            confirmReceivedButton.text = "Đã Nhận Hàng" // Để không bị trống text
                        }
                    }

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

    private fun showConfirmationDialog(orderId: String) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận")
            .setMessage("Bạn có chắc chắn đã nhận được đơn hàng này?")
            .setPositiveButton("Có") { dialog, which ->
                updateOrderStatusToCompleted(orderId)
            }
            .setNegativeButton("Không", null)
            .show()
    }

    private fun updateOrderStatusToCompleted(orderId: String) {
        Log.d("SimpleOrderDetailActivity", "Cập nhật trạng thái đơn hàng thành 'Đã giao' cho orderId: $orderId")
        firestore.collection("checkouts")
            .whereEqualTo("orderId", orderId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty()) {
                    val documentSnapshot = querySnapshot.documents.first()
                    documentSnapshot.reference.update("shippingStatus", "Đã giao")
                        .addOnSuccessListener {
                            Log.d("Firestore", "Trạng thái đơn hàng đã được cập nhật thành 'Đã giao'")
                            Toast.makeText(this, "Đã xác nhận nhận hàng. Đơn hàng đã hoàn thành.", Toast.LENGTH_SHORT).show()
                            confirmReceivedButton.isEnabled = false
                            confirmReceivedButton.text = "Đơn hàng đã hoàn thành"
                            confirmReceivedButton.setBackgroundColor(Color.GRAY)
                            confirmReceivedButton.setTextColor(Color.DKGRAY)
                            confirmReceivedButton.setOnClickListener(null)
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Lỗi khi cập nhật trạng thái đơn hàng", e)
                            Toast.makeText(this, "Lỗi khi xác nhận: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.w("Firestore", "Không tìm thấy đơn hàng để cập nhật trạng thái.")
                    Toast.makeText(this, "Không tìm thấy đơn hàng.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Lỗi khi truy vấn đơn hàng để cập nhật trạng thái", e)
                Toast.makeText(this, "Lỗi khi cập nhật đơn hàng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun kiemTraSimpleOrderDetailActivityDaChay() {
        Log.i("SimpleOrderDetailActivity", "Hàm kiemTraSimpleOrderDetailActivityDaChay() được gọi. Trang chi tiết đơn hàng có vẻ đã được khởi chạy.")
        Toast.makeText(this, "Trang chi tiết đơn hàng đã chạy", Toast.LENGTH_SHORT).show()
    }
}