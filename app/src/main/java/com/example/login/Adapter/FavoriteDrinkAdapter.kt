package com.example.login.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.login.Model.Drink
import com.example.login.R

class FavoriteDrinkAdapter(
    private val favoriteDrinks: List<Drink>,
    private val onItemClick: (Drink) -> Unit, // Callback cho sự kiện click vào item
    private val onFavoriteClick: (Drink) -> Unit // Callback cho sự kiện click vào icon yêu thích
) : RecyclerView.Adapter<FavoriteDrinkAdapter.FavoriteDrinkViewHolder>() {

    class FavoriteDrinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val drinkImageView: ImageView = itemView.findViewById(R.id.imgProduct)
        val nameTextView: TextView = itemView.findViewById(R.id.tvProductName)
        val priceTextView: TextView = itemView.findViewById(R.id.tvProductPrice)
        val favoriteIcon: ImageView = itemView.findViewById(R.id.favoriteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteDrinkViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return FavoriteDrinkViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FavoriteDrinkViewHolder, position: Int) {
        val currentDrink = favoriteDrinks[position]
        holder.nameTextView.text = currentDrink.name
        holder.priceTextView.text = try {
            val priceDouble = currentDrink.price?.toDouble() ?: 0.0 // Chuyển đổi sang Double, xử lý null
            String.format("Rp %.2f", priceDouble)
        } catch (e: NumberFormatException) {
            // Xử lý trường hợp chuỗi giá không hợp lệ
            Log.e("FavoriteDrinkAdapter", "Error converting price to double: ${currentDrink.price}", e)
            currentDrink.price ?: "N/A" // Hiển thị "N/A" hoặc một giá trị mặc định khác
        }

        Glide.with(holder.itemView.context)
            .load(currentDrink.imageResId) // Đảm bảo imageResId là đường dẫn đúng
            .into(holder.drinkImageView)

        // Thiết lập icon yêu thích (ví dụ: icon đã tô đầy)
        holder.favoriteIcon.setImageResource(R.drawable.ic_favorite_red) // Đảm bảo bạn có drawable này

        // Xử lý sự kiện click vào item
        holder.itemView.setOnClickListener {
            onItemClick(currentDrink)
        }

        // Xử lý sự kiện click vào icon yêu thích (để xóa khỏi yêu thích)
        holder.favoriteIcon.setOnClickListener {
            onFavoriteClick(currentDrink)
        }
    }

    override fun getItemCount() = favoriteDrinks.size
}