package com.example.login.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.login.Model.Product
import com.example.login.R

class PopularAdapter(private val popularList: List<Product>) : RecyclerView.Adapter<PopularAdapter.PopularViewHolder>() {

    class PopularViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_popular, parent, false)
        return PopularViewHolder(view)
    }

    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        val product = popularList[position]

        // Load ảnh sản phẩm với Glide
        Glide.with(holder.itemView.context)
            .load(product.image_url) // Kiểm tra tên thuộc tính image_url

            .into(holder.imgProduct)

        // Hiển thị tên và giá sản phẩm
        holder.tvProductName.text = product.name
        holder.tvProductPrice.text = "$${product.price}"
    }

    override fun getItemCount(): Int = popularList.size
}
