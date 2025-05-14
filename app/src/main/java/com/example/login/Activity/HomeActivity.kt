package com.example.login.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login.Adapter.PopularAdapter
import com.example.login.R
import com.example.login.Adapter.ProductAdapter
import com.example.login.MainActivity
import com.example.login.Model.Product
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {
    private lateinit var recyclerViewPopular: RecyclerView
    private lateinit var popularAdapter: PopularAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBarCategory: ProgressBar
    private lateinit var progressBarPopular: ProgressBar
    private lateinit var bottomNavigationView : BottomNavigationView

    private val popularList = mutableListOf<Product>()
    private val productList = mutableListOf<Product>()
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val menuButton: ImageView = findViewById(R.id.imageView2)

        menuButton.setOnClickListener {
            showPopupMenu(menuButton)
        }
        // Thiết lập sự kiện cho nút
        // Khởi tạo ImageView cho nút chuyển đến DrinkMenuActivity
        val btnToDrinkDetail: ImageView = findViewById(R.id.btnToDrinkDetail)

        // Thiết lập sự kiện cho nút
        btnToDrinkDetail.setOnClickListener {
            Log.d("HomeActivity", "Attempting to open DrinkMenuActivity")
            try {
                val intent = Intent(this, DrinkMenuActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("HomeActivity", "Error starting DrinkMenuActivity", e)
                Toast.makeText(this, "Failed to open Drink Menu", Toast.LENGTH_SHORT).show()
            }
        }

        recyclerViewPopular = findViewById(R.id.recyclerViewPopular)
        recyclerViewPopular.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        popularAdapter = PopularAdapter(popularList)
        recyclerViewPopular.adapter = popularAdapter


        recyclerView = findViewById(R.id.recyclerViewCat)
        progressBar = findViewById(R.id.progressBar)
        progressBarCategory = findViewById(R.id.progressBarCategory)
        progressBarPopular = findViewById(R.id.progressBarPopular)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        productAdapter = ProductAdapter(productList)
        recyclerView.adapter = productAdapter


        // Ẩn tất cả ProgressBar khi mới vào
        progressBar.visibility = View.GONE
        progressBarCategory.visibility = View.GONE
        progressBarPopular.visibility = View.GONE

        fetchProductsFromFirestore()
        fetchPopularProductsFromFirestore()
        setupBottomNavigationView()
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
    private fun setupBottomNavigationView() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.drinkMenu -> { // Đã đổi ID cho phù hợp với file menu
                    val intent = Intent(this, DrinkMenuActivity::class.java)
                    startActivity(intent)
                    true
                }
//                R.id.favorites -> {
//                    val intent = Intent(this, FavoritesActivity::class.java)
//                    startActivity(intent)
//                    true
//                }
                R.id.myCart -> {
                    val intent = Intent(this, CartActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.favorites -> {
                    val intent = Intent(this, FavoritesActivity::class.java)
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
    private fun showPopupMenu(view: View) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.menu_popup, null)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.elevation = 10f
        popupWindow.showAsDropDown(view, -100, 20)

        val logout = popupView.findViewById<LinearLayout>(R.id.menu_logout)

        logout.setOnClickListener {
            popupWindow.dismiss()
            FirebaseAuth.getInstance().signOut()

            // Xóa trạng thái đăng nhập
            val sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear() // Xóa toàn bộ dữ liệu đăng nhập
            editor.apply()

            Log.d("Logout", "User logged out successfully")

            // Chuyển về màn hình đăng nhập
            val intent = Intent(this@HomeActivity, DangNhapActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finishAffinity()
        }

    }


}
