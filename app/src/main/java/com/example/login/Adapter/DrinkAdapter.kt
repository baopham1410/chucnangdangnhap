package com.example.login.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.login.Model.Drink
import com.example.login.R

class DrinkAdapter(
    private val drinkList: List<Drink>,
    private val onAddClick: (Drink) -> Unit,
    private val onItemClick: (Drink) -> Unit // Callback cho nhấn vào món đồ uống
) : RecyclerView.Adapter<DrinkAdapter.DrinkViewHolder>() {

    class DrinkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgDrink: ImageView = view.findViewById(R.id.imgDrink)
        val tvDrinkName: TextView = view.findViewById(R.id.tvDrinkName)
        val tvDrinkDesc: TextView = view.findViewById(R.id.tvDrinkDesc)
        val tvDrinkPrice: TextView = view.findViewById(R.id.tvDrinkPrice)
        val btnAdd: ImageButton = view.findViewById(R.id.btnAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_drink, parent, false)
        return DrinkViewHolder(view)
    }

    override fun onBindViewHolder(holder: DrinkViewHolder, position: Int) {
        val drink = drinkList[position]
        holder.tvDrinkName.text = drink.name
        holder.tvDrinkDesc.text = drink.description
        holder.tvDrinkPrice.text = drink.price

        // Tải hình ảnh
        Glide.with(holder.imgDrink.context)
            .load(drink.imageResId) // Sử dụng imageResId để tải hình ảnh
            .into(holder.imgDrink)

        // Sự kiện nhấn vào món đồ uống
        holder.itemView.setOnClickListener {
            onItemClick(drink) // Gọi callback khi nhấn vào món đồ uống
        }

        // Sự kiện nhấn nút thêm
        holder.btnAdd.setOnClickListener {
            onAddClick(drink) // Gọi callback khi nhấn nút thêm
        }
    }

    override fun getItemCount() = drinkList.size
}