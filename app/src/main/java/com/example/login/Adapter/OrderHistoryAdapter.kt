package com.example.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.login.Model.Order
import com.example.login.R
import java.text.NumberFormat
import java.util.Locale

class OrderHistoryAdapter(
    private val orderList: List<Order>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder>() {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderIdTextView: TextView = itemView.findViewById(R.id.orderIdTextView)
        val orderDateTextView: TextView = itemView.findViewById(R.id.orderDateTextView)
        val totalAmountTextView: TextView = itemView.findViewById(R.id.totalAmountTextView)
        val orderStatusTextView: TextView = itemView.findViewById(R.id.orderStatusTextView) // Thêm TextView này
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_beautiful, parent, false) // Sử dụng layout đẹp
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentOrder = orderList[position]
        holder.orderIdTextView.text = "Đơn hàng #${currentOrder.orderId.takeLast(5)}"
        holder.orderDateTextView.text = "Đặt ngày: ${currentOrder.orderDate}"
        holder.totalAmountTextView.text = currencyFormatter.format(currentOrder.totalAmount)
        holder.orderStatusTextView.text = currentOrder.paymentStatus // Hiển thị trạng thái

        holder.itemView.setOnClickListener {
            onItemClick(currentOrder.orderId)
        }
    }

    override fun getItemCount() = orderList.size
}