package com.example.login.Activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText // Import EditText
import android.widget.ImageView // Import ImageView (nếu bạn có icon search)
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login.Adapter.DrinkAdapter
import com.example.login.Model.Drink
import com.example.login.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class DrinkMenuActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DrinkAdapter
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var searchEditText: EditText // Khai báo EditText cho tìm kiếm
    private lateinit var settingsIcon: ImageView // Khai báo ImageView cho cài đặt (nếu có trong layout)
    private lateinit var tabLayout: TabLayout
    private val drinkList = mutableListOf<Drink>()
    private val originalDrinkList = mutableListOf<Drink>()
    private var currentTab: String = "All" // Mặc định là "All" khi mới khởi chạy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drink_menu)

        recyclerView = findViewById(R.id.recyclerViewDrinks)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        searchEditText = findViewById(R.id.searchEditText) // Ánh xạ EditText từ layout
        settingsIcon = findViewById(R.id.settingsIcon) // Ánh xạ ImageView từ layout (nếu có)
        tabLayout = findViewById(R.id.tabLayout)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = DrinkAdapter(drinkList, { drink ->
            val intent = Intent(this, DrinkDetailActivity::class.java)
            intent.putExtra("drink", drink)
            startActivity(intent)
        })
        recyclerView.adapter = adapter

        fetchDrinksFromFirestore()
        setupTabLayout()
        setupBottomNavigationView()
        setupSearchFunctionality() // Gọi hàm thiết lập chức năng tìm kiếm
    }

    private fun fetchDrinksFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("drink").get()
            .addOnSuccessListener { result ->
                originalDrinkList.clear()
                for (document in result) {
                    val drink = document.toObject(Drink::class.java)
                    drink?.documentId = document.id // Gán ID TỰ ĐỘNG
                    originalDrinkList.add(drink)
                }
                filterDrinksByCategory(currentTab)
            }
            .addOnFailureListener { exception ->
                Log.w("DrinkMenuActivity", "Error getting documents: ", exception)
                Toast.makeText(this, "Failed to load drinks", Toast.LENGTH_SHORT).show()
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

    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }

    private fun setupTabLayout() {
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText("All"))
        tabLayout.addTab(tabLayout.newTab().setText("Coffee"))
        tabLayout.addTab(tabLayout.newTab().setText("Chocolate"))
        tabLayout.addTab(tabLayout.newTab().setText("Others"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.text.toString() // Cập nhật tab hiện tại
                filterDrinksByCategory(currentTab) // Lọc lại theo tab mới
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filterDrinksByCategory(category: String) {
        val filteredList = if (category == "All") {
            originalDrinkList // Hiển thị danh sách gốc nếu chọn "All"
        } else {
            originalDrinkList.filter { it.category == category }
        }
        drinkList.clear()
        drinkList.addAll(filteredList)
        adapter.notifyDataSetChanged()
    }

    // CHỨC NĂNG TÌM KIẾM
    private fun setupSearchFunctionality() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Không cần xử lý trước khi văn bản thay đổi
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterDrinksBySearch(s.toString()) // Gọi hàm lọc khi văn bản thay đổi
            }

            override fun afterTextChanged(s: Editable?) {
                // Không cần xử lý sau khi văn bản thay đổi
            }
        })
    }

    private fun filterDrinksBySearch(query: String) {
        val lowerCaseQuery = query.toLowerCase(Locale.getDefault())
        val filteredList = if (currentTab == "All") {
            originalDrinkList.filter {
                it.name?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery) == true
            }
        } else {
            originalDrinkList.filter {
                it.category == currentTab &&
                        it.name?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery) == true
            }
        }

        drinkList.clear()
        drinkList.addAll(filteredList)
        adapter.notifyDataSetChanged()

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm trong mục này", Toast.LENGTH_SHORT).show()
        }
    }
}