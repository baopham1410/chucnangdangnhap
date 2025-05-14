package com.example.login.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login.Adapter.FavoriteDrinkAdapter
import com.example.login.Model.Drink
import com.example.login.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesActivity : AppCompatActivity() {

    private lateinit var recyclerViewFavorites: RecyclerView
    private lateinit var favoriteDrinkAdapter: FavoriteDrinkAdapter
    private val favoriteDrinksList = mutableListOf<Drink>()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var bottomNavigationView: BottomNavigationView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites)
        recyclerViewFavorites.layoutManager = GridLayoutManager(this, 2)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        setupBottomNavigationView()

        // Khởi tạo Adapter với callbacks
        favoriteDrinkAdapter = FavoriteDrinkAdapter(
            favoriteDrinksList,
            { clickedDrink ->
                // Xử lý khi một item được click (ví dụ: mở trang chi tiết)
                Log.d("FavoritesActivity", "Item clicked: ${clickedDrink.name}")
                // Thực hiện hành động của bạn ở đây
            },
            { favoriteClickedDrink ->
                // Xử lý khi icon yêu thích được click (ví dụ: xóa khỏi yêu thích)
                Log.d("FavoritesActivity", "Favorite clicked: ${favoriteClickedDrink.name}")
                removeFavorite(favoriteClickedDrink)
            }
        )
        recyclerViewFavorites.adapter = favoriteDrinkAdapter

        loadFavoriteDrinks()
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
        finish() // Để không quay lại Activity hiện tại khi nhấn back từ Activity mới
    }

    private fun loadFavoriteDrinks() {
        val userId = getCurrentUserId()

        if (userId.isNotEmpty()) {
            firestore.collection("users")
                .document(userId)
                .collection("favorite_drinks")
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e("FavoritesActivity", "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    favoriteDrinksList.clear()
                    for (doc in value!!) {
                        val drinkId = doc.getString("drinkId") // **Lấy giá trị của trường "drinkId"**
                        if (drinkId != null) {
                            firestore.collection("drink").document(drinkId)
                                .get()
                                .addOnSuccessListener { drinkDocument ->
                                    drinkDocument.toObject(Drink::class.java)?.let { drink ->
                                        favoriteDrinksList.add(drink)
                                        favoriteDrinkAdapter.notifyDataSetChanged()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.w("FavoritesActivity", "Error getting favorite drink details for ID: $drinkId", e)
                                }
                        }
                    }
                    if (favoriteDrinksList.isEmpty()) {
                        Log.i("FavoritesActivity", "No favorite drinks found.")
                    }
                }
        } else {
            Log.w("FavoritesActivity", "User ID not found.")
        }
    }

    private fun removeFavorite(drink: Drink) {
        val userId = getCurrentUserId()
        if (userId.isNotEmpty()) {
            firestore.collection("users")
                .document(userId)
                .collection("favorite_drinks")
                .whereEqualTo("drinkId", drink.documentId) // **SỬ DỤNG drink.documentId ĐỂ TRUY VẤN XÓA**
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        for (document in querySnapshot) {
                            document.reference.delete()
                                .addOnSuccessListener {
                                    Log.d("FavoritesActivity", "Document ${drink.name} successfully deleted from favorites!")
                                    favoriteDrinksList.remove(drink)
                                    favoriteDrinkAdapter.notifyDataSetChanged()
                                }
                                .addOnFailureListener { e ->
                                    Log.w("FavoritesActivity", "Error deleting document", e)
                                }
                            break // Xóa document đầu tiên tìm thấy
                        }
                    } else {
                        Log.d("FavoritesActivity", "${drink.name} not found in favorites.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("FavoritesActivity", "Error searching for favorite drink to delete", e)
                }
        }
    }

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
}