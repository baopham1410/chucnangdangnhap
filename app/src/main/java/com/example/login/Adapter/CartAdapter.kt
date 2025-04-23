package com.example.login.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.login.Model.CartItem
import com.example.login.R
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val cartItems: MutableList<CartItem>, // Chú ý: Chuyển thành MutableList để có thể xóa item
    private val onQuantityChanged: (Int, Int) -> Unit,
    private val onItemRemoved: (Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val itemName: TextView = itemView.findViewById(R.id.itemName)
        val itemPrice: TextView = itemView.findViewById(R.id.itemPrice)
        val quantityText: TextView = itemView.findViewById(R.id.quantityText)
        val decreaseButton: ImageButton = itemView.findViewById(R.id.decreaseButton)
        val increaseButton: ImageButton = itemView.findViewById(R.id.increaseButton)
        val itemTotalPrice: TextView = itemView.findViewById(R.id.itemTotalPrice)
        val removeButton: ImageButton = itemView.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val currentItem = cartItems[position]

        holder.itemName.text = currentItem.name
        holder.itemPrice.text = formatPrice(currentItem.price)
        holder.quantityText.text = currentItem.quantity.toString()
        holder.itemTotalPrice.text = formatPrice(currentItem.price * currentItem.quantity)

        // TODO: Load hình ảnh sản phẩm (nếu bạn có URL hoặc resource ID)
        // Ví dụ nếu imageResId là resource ID:
        // holder.itemImage.setImageResource(currentItem.imageResId.toInt())
        Glide.with(holder.itemView.context)
            .load(currentItem.imageResId)
            .into(holder.itemImage)

        holder.decreaseButton.setOnClickListener {
            val currentQuantity = holder.quantityText.text.toString().toInt()
            if (currentQuantity > 1) {
                val newQuantity = currentQuantity - 1
                onQuantityChanged(holder.adapterPosition, newQuantity) // Sử dụng holder.adapterPosition
                holder.quantityText.text = newQuantity.toString()
                holder.itemTotalPrice.text = formatPrice(currentItem.price * newQuantity)
            }
        }

        holder.increaseButton.setOnClickListener {
            val currentQuantity = holder.quantityText.text.toString().toInt()
            val newQuantity = currentQuantity + 1
            onQuantityChanged(holder.adapterPosition, newQuantity) // Sử dụng holder.adapterPosition
            holder.quantityText.text = newQuantity.toString()
            holder.itemTotalPrice.text = formatPrice(currentItem.price * newQuantity)
        }

        holder.removeButton.setOnClickListener {
            onItemRemoved(holder.adapterPosition) // Sử dụng holder.adapterPosition
        }
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    private fun formatPrice(price: Double): String {
        return currencyFormatter.format(price)
    }
}