package com.example.login.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login.Adapter.CartAdapter
import com.example.login.MainActivity
import com.example.login.Model.CartItem
import com.example.login.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var emptyCartView: LinearLayout
    private lateinit var cartSummaryCard: CardView
    private lateinit var subtotalText: TextView
    private lateinit var deliveryFeeText: TextView
    private lateinit var taxText: TextView
    private lateinit var totalText: TextView
    private lateinit var checkoutButton: MaterialButton
    private lateinit var browseMenuButton: MaterialButton
    private lateinit var backButton: ImageButton
    private lateinit var clearCartButton: ImageButton

    private val cartItems = ArrayList<CartItem>()
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    private val DELIVERY_FEE = 15000.0
    private val TAX_RATE = 0.08 // 8% tax

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        initViews()
        setupRecyclerView()
        setupClickListeners()
        loadCartItems()
    }

    private fun initViews() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        emptyCartView = findViewById(R.id.emptyCartView)
        cartSummaryCard = findViewById(R.id.cartSummaryCard)
        subtotalText = findViewById(R.id.subtotalText)
        deliveryFeeText = findViewById(R.id.deliveryFeeText)
        taxText = findViewById(R.id.taxText)
        totalText = findViewById(R.id.totalText)
        checkoutButton = findViewById(R.id.checkoutButton)
        browseMenuButton = findViewById(R.id.browseMenuButton)
        backButton = findViewById(R.id.backButton)
        clearCartButton = findViewById(R.id.clearCartButton)

        deliveryFeeText.text = formatPrice(DELIVERY_FEE)
    }

    private fun setupRecyclerView() {
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(
            cartItems as MutableList<CartItem>,
            onQuantityChanged = { position, newQuantity -> updateItemQuantity(position, newQuantity) },
            onItemRemoved = { position -> removeCartItem(position) }
        )
        cartRecyclerView.adapter = cartAdapter
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener { finish() }

        clearCartButton.setOnClickListener {
            if (cartItems.isNotEmpty()) {
                showClearCartConfirmation()
            }
        }

        browseMenuButton.setOnClickListener {
            val intent = Intent(this, DrinkMenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        checkoutButton.setOnClickListener {
            if (cartItems.isNotEmpty()) {
                proceedToCheckout()
            } else {
                Toast.makeText(this, "Giỏ hàng của bạn đang trống!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCartItems() {
        cartItems.clear()

        currentUser?.uid?.let { userId ->
            firestore.collection("cart_items")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        for (document in querySnapshot.documents) {
                            val documentId = document.id
                            val drinkId = document.getString("drinkId") ?: ""
                            val name = document.getString("name") ?: ""
                            val price = document.getDouble("price") ?: 0.0
                            val quantity = document.getLong("quantity")?.toInt() ?: 1
                            val imageResId = document.getString("imageResId") ?: ""

                            val cartItem = CartItem(documentId, drinkId, name, price, quantity, imageResId)
                            cartItems.add(cartItem)
                        }
                        updateCartUI()
                        (cartAdapter as CartAdapter).notifyDataSetChanged()
                    } else {
                        updateCartUI()
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error getting cart items", e)
                    Toast.makeText(this, "Lỗi khi tải giỏ hàng: ${e.message}", Toast.LENGTH_SHORT).show()
                    updateCartUI()
                }
        } ?: run {
            Toast.makeText(this, "Bạn cần đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show()
            updateCartUI()
        }
    }

    private fun updateCartUI() {
        if (cartItems.isEmpty()) {
            emptyCartView.visibility = View.VISIBLE
            cartRecyclerView.visibility = View.GONE
            cartSummaryCard.visibility = View.GONE
            checkoutButton.isEnabled = false
            checkoutButton.alpha = 0.5f
        } else {
            emptyCartView.visibility = View.GONE
            cartRecyclerView.visibility = View.VISIBLE
            cartSummaryCard.visibility = View.VISIBLE
            checkoutButton.isEnabled = true
            checkoutButton.alpha = 1.0f
            calculateAndDisplayTotals()
        }
    }

    private fun calculateAndDisplayTotals() {
        var subtotal = 0.0
        for (item in cartItems) {
            subtotal += item.price * item.quantity
        }

        val tax = subtotal * TAX_RATE
        val total = subtotal + tax + DELIVERY_FEE

        subtotalText.text = formatPrice(subtotal)
        taxText.text = formatPrice(tax)
        totalText.text = formatPrice(total)
    }

    private fun formatPrice(price: Double): String {
        return currencyFormatter.format(price)
    }

    private fun updateItemQuantity(position: Int, newQuantity: Int) {
        if (position >= 0 && position < cartItems.size) {
            val item = cartItems[position]
            currentUser?.uid?.let { _ ->
                firestore.collection("cart_items").document(item.documentId)
                    .update("quantity", newQuantity)
                    .addOnSuccessListener {
                        item.quantity = newQuantity
                        (cartAdapter as CartAdapter).notifyItemChanged(position)
                        calculateAndDisplayTotals()
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error updating quantity", e)
                        Toast.makeText(this, "Lỗi khi cập nhật số lượng", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun removeCartItem(position: Int) {
        if (position >= 0 && position < cartItems.size) {
            val item = cartItems[position]
            firestore.collection("cart_items").document(item.documentId)
                .delete()
                .addOnSuccessListener {
                    cartItems.removeAt(position)
                    (cartAdapter as CartAdapter).notifyItemRemoved(position)
                    updateCartUI()
                    Toast.makeText(this, "Đã xóa ${item.name} khỏi giỏ hàng", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error removing item", e)
                    Toast.makeText(this, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showClearCartConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Xóa giỏ hàng")
            .setMessage("Bạn có chắc chắn muốn xóa tất cả sản phẩm khỏi giỏ hàng không?")
            .setPositiveButton("Xóa") { dialog, _ ->
                clearCart()
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun clearCart() {
        currentUser?.uid?.let { userId ->
            firestore.collection("cart_items")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val batch = firestore.batch()
                    for (document in querySnapshot.documents) {
                        batch.delete(document.reference)
                    }
                    batch.commit()
                        .addOnSuccessListener {
                            cartItems.clear()
                            (cartAdapter as CartAdapter).notifyDataSetChanged()
                            updateCartUI()
                            Toast.makeText(this, "Đã xóa tất cả sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error clearing cart", e)
                            Toast.makeText(this, "Lỗi khi xóa giỏ hàng", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error getting cart items to clear", e)
                    Toast.makeText(this, "Lỗi khi tải giỏ hàng để xóa", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun proceedToCheckout() {
        var subtotal = 0.0
        for (item in cartItems) {
            subtotal += item.price * item.quantity
        }
        val tax = subtotal * TAX_RATE
        val total = subtotal + tax + DELIVERY_FEE

        val intent = Intent(this, CheckoutActivity::class.java)
        intent.putExtra("SUBTOTAL", subtotal)
        intent.putExtra("TAX", tax)
        intent.putExtra("DELIVERY_FEE", DELIVERY_FEE)
        intent.putExtra("TOTAL", total)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

    }
}