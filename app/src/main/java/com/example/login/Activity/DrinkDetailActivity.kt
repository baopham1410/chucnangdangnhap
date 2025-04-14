package com.example.login.Activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.login.Model.Drink
import com.example.login.R

class DrinkDetailActivity : AppCompatActivity() {

    private lateinit var imgDrink: ImageView
    private lateinit var tvDrinkName: TextView
    private lateinit var tvDrinkDesc: TextView
    private lateinit var tvDrinkPrice: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drink_detail)

        imgDrink = findViewById(R.id.imgDrink)
        tvDrinkName = findViewById(R.id.tvDrinkName)
        tvDrinkDesc = findViewById(R.id.tvDrinkDesc)
        tvDrinkPrice = findViewById(R.id.tvDrinkPrice)

        // Nhận đối tượng Drink từ Intent
        val drink: Drink? = intent.getParcelableExtra("drink")

        drink?.let {
            tvDrinkName.text = it.name
            tvDrinkDesc.text = it.description
            tvDrinkPrice.text = it.price

            // Tải hình ảnh
            Glide.with(this)
                .load(it.imageResId) // Sử dụng imageResId để tải hình ảnh
                .into(imgDrink)
        }
    }
}