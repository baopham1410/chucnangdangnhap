package com.example.login.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login.Adapter.DrinkAdapter
import com.example.login.Model.Drink
import com.example.login.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.tabs.TabLayout

class DrinkMenuActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DrinkAdapter
    private val drinkList = mutableListOf<Drink>()
    private val originalDrinkList = mutableListOf<Drink>() // Danh sách gốc để lọc

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drink_menu)

        recyclerView = findViewById(R.id.recyclerViewDrinks)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = DrinkAdapter(drinkList, { drink ->
            Toast.makeText(this, "${drink.name} added!", Toast.LENGTH_SHORT).show()
        }, { drink ->
            val intent = Intent(this, DrinkDetailActivity::class.java)
            intent.putExtra("drink", drink)
            startActivity(intent)
        })
        recyclerView.adapter = adapter

        fetchDrinksFromFirestore()
        setupTabLayout()
    }

    private fun fetchDrinksFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("drink").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val drink = document.toObject(Drink::class.java)
                    drinkList.add(drink)
                    originalDrinkList.add(drink)
                }
                // Gọi filterDrinks để lọc theo tab đầu tiên ngay khi dữ liệu đã được tải
                filterDrinks(0) // Chỉ cần gọi một lần sau khi load dữ liệu
            }
            .addOnFailureListener { exception ->
                Log.w("DrinkMenuActivity", "Error getting documents: ", exception)
                Toast.makeText(this, "Failed to load drinks", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupTabLayout() {
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText("Coffee"))
        tabLayout.addTab(tabLayout.newTab().setText("Chocolate"))
        tabLayout.addTab(tabLayout.newTab().setText("Others"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                filterDrinks(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun filterDrinks(tabPosition: Int) {
        val filteredList = when (tabPosition) {
            0 -> originalDrinkList.filter { it.category == "Coffee" }
            1 -> originalDrinkList.filter { it.category == "Chocolate" }
            2 -> originalDrinkList
            else -> originalDrinkList
        }
        drinkList.clear()
        drinkList.addAll(filteredList)
        adapter.notifyDataSetChanged() // Gọi lại ở đây để cập nhật adapter
    }
}