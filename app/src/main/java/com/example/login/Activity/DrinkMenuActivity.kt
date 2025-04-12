package com.example.login.Activity

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

class DrinkMenuActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DrinkAdapter
    private val drinkList = mutableListOf<Drink>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drink_menu)

        recyclerView = findViewById(R.id.recyclerViewDrinks)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = DrinkAdapter(drinkList) { drink ->
            Toast.makeText(this, "${drink.name} added!", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        fetchDrinksFromFirestore()
    }

    private fun fetchDrinksFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("drink").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val drink = document.toObject(Drink::class.java)
                    drinkList.add(drink)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("DrinkMenuActivity", "Error getting documents: ", exception)
            }
    }
}