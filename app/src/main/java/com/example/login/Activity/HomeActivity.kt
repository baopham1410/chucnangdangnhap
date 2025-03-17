package com.example.login.Activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login.R
import com.example.login.Adapter.ProductAdapter
import com.example.login.Model.Product
import com.google.firebase.firestore.FirebaseFirestore
class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<Product>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // ⚠️ Không khai báo lại productList ở đây
        productAdapter = ProductAdapter(productList) // 🔥 Đảm bảo khởi tạo trước khi dùng
        recyclerView.adapter = productAdapter

        // Gọi hàm lấy dữ liệu từ Firestore
        fetchProductsFromFirestore()
    }

    private fun fetchProductsFromFirestore() {
        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                productList.clear()
                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val price = document.getString("price") ?: ""
                    val imageUrl = document.getString("image_url") ?: ""

                    productList.add(Product(name, price, imageUrl)) // 🔥 Đúng thứ tự của model
                }
                productAdapter.notifyDataSetChanged() // Cập nhật RecyclerView
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Lỗi lấy dữ liệu", e)
            }
    }
}
