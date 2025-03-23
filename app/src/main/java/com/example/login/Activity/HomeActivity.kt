package com.example.login.Activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login.Adapter.PopularAdapter
import com.example.login.R
import com.example.login.Adapter.ProductAdapter
import com.example.login.Model.Product
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {
    private lateinit var recyclerViewPopular: RecyclerView
    private lateinit var popularAdapter: PopularAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBarCategory: ProgressBar
    private lateinit var progressBarPopular: ProgressBar

    private val popularList = mutableListOf<Product>()
    private val productList = mutableListOf<Product>()
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerViewPopular = findViewById(R.id.recyclerViewPopular)
        recyclerViewPopular.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        popularAdapter = PopularAdapter(popularList)
        recyclerViewPopular.adapter = popularAdapter


        recyclerView = findViewById(R.id.recyclerViewCat)
        progressBar = findViewById(R.id.progressBar)
        progressBarCategory = findViewById(R.id.progressBarCategory)
        progressBarPopular = findViewById(R.id.progressBarPopular)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        productAdapter = ProductAdapter(productList)
        recyclerView.adapter = productAdapter


        // Ẩn tất cả ProgressBar khi mới vào
        progressBar.visibility = View.GONE
        progressBarCategory.visibility = View.GONE
        progressBarPopular.visibility = View.GONE

        fetchProductsFromFirestore()
        fetchPopularProductsFromFirestore()
    }

    private fun fetchProductsFromFirestore() {
        progressBar.visibility = View.VISIBLE
        progressBarCategory.visibility = View.VISIBLE
        progressBarPopular.visibility = View.VISIBLE

        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                productList.clear()
                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val price = document.getString("price") ?: ""
                    val imageUrl = document.getString("image_url") ?: ""

                    productList.add(Product(name, price, imageUrl))
                }
                productAdapter.notifyDataSetChanged()

                // Ẩn tất cả ProgressBar khi tải xong
                progressBar.visibility = View.GONE
                progressBarCategory.visibility = View.GONE
                progressBarPopular.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Lỗi lấy dữ liệu", e)

                // Ẩn tất cả ProgressBar nếu có lỗi
                progressBar.visibility = View.GONE
                progressBarCategory.visibility = View.GONE
                progressBarPopular.visibility = View.GONE
            }
    }
    private fun fetchPopularProductsFromFirestore() {
        progressBarPopular.visibility = View.VISIBLE

        db.collection("products")
            .whereEqualTo("popular", true) // Lọc sản phẩm phổ biến
            .get()
            .addOnSuccessListener { documents ->
                popularList.clear()
                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val price = document.getString("price") ?: ""
                    val imageUrl = document.getString("image_url") ?: ""

                    val product = Product(name, price, imageUrl)
                    popularList.add(product)
                }
                popularAdapter.notifyDataSetChanged()
                progressBarPopular.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Lỗi lấy dữ liệu popular", e)
                progressBarPopular.visibility = View.GONE
            }
    }


}
