package com.example.login.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.login.Model.Drink
import com.example.login.R

class DrinkAdapter(
    private val drinkList: List<Drink>,
    private val onAddClick: (Drink) -> Unit // Callback cho nhấn vào nút thêm (để xem chi tiết)
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

        // Thiết lập dữ liệu văn bản
        holder.tvDrinkName.text = drink.name
        holder.tvDrinkDesc.text = drink.description
        holder.tvDrinkPrice.text = drink.price

        // Tạo RequestOptions để xử lý hình ảnh
        val requestOptions = RequestOptions()
            .centerCrop() // Cắt và căn giữa để hình ảnh lấp đầy không gian
            .transform(CenterCrop(), RoundedCorners(12)) // Thêm góc bo nếu không có background drawable riêng

        // Tải hình ảnh với các tùy chọn đã thiết lập
        Glide.with(holder.imgDrink.context)
            .load(drink.imageResId)
            .apply(requestOptions)
            .into(holder.imgDrink)

        // Thiết lập sự kiện nhấn vào toàn bộ item (không chỉ nút thêm)
        holder.itemView.setOnClickListener {
            onAddClick(drink)
        }

        // Sự kiện nhấn nút thêm (để xem chi tiết)
        holder.btnAdd.setOnClickListener {
            onAddClick(drink)
        }
    }

    override fun getItemCount() = drinkList.size
}