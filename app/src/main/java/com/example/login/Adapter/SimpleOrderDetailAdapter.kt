package com.example.login.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.login.R

class SimpleOrderDetailAdapter(private var orderItems: List<Map<String, Any>>) :
    RecyclerView.Adapter<SimpleOrderDetailAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
        val quantityTextView: TextView = itemView.findViewById(R.id.quantityTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simple_order_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = orderItems[position]

        val name = item["name"] as? String ?: ""
        val quantity = (item["quantity"] as? Long)?.toInt() ?: 0
        val price = (item["price"] as? Number)?.toDouble() ?: 0.0

        holder.productNameTextView.text = name
        holder.quantityTextView.text = "x${quantity}"
        holder.priceTextView.text = String.format("%,.0f VNĐ", price)
    }

    override fun getItemCount(): Int {
        return orderItems.size
    }

    fun updateData(newOrderItems: List<Map<String, Any>>) {
        orderItems = newOrderItems
        notifyDataSetChanged()
    }
}