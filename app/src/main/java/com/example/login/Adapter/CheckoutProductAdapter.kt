package com.example.login.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.login.Model.CartItem
import com.example.login.R

class CheckoutProductAdapter(private val cartItems: List<CartItem>) :
    RecyclerView.Adapter<CheckoutProductAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImageView: ImageView = itemView.findViewById(R.id.productImageView)
        val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
        val productQuantityTextView: TextView = itemView.findViewById(R.id.productQuantityTextView)
        val productPriceTextView: TextView = itemView.findViewById(R.id.productPriceTextView)

        fun bind(cartItem: CartItem) {
            productNameTextView.text = cartItem.name
            productQuantityTextView.text = "Quantity: ${cartItem.quantity}"
            productPriceTextView.text = "${cartItem.price * cartItem.quantity}" // Hiển thị tổng giá của item

            // Load image using Glide or Picasso if you have image URLs
            // For now, we'll try to load from drawable resource ID if available
            if (cartItem.imageResId.isNotEmpty()) {
                try {
                    val resourceId = itemView.context.resources.getIdentifier(
                        cartItem.imageResId, "drawable", itemView.context.packageName
                    )
                    if (resourceId != 0) {
                        productImageView.setImageResource(resourceId)
                    } else {
                        productImageView.setImageResource(R.drawable.welcome) // Placeholder if not found
                    }
                } catch (e: NumberFormatException) {
                    productImageView.setImageResource(R.drawable.welcome) // Placeholder on error
                }
            } else {
                productImageView.setImageResource(R.drawable.welcome) // Default placeholder
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checkout_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartItem = cartItems[position]
        holder.bind(cartItem)
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }
}