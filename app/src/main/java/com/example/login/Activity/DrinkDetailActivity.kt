package com.example.login.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.login.MainActivity
import com.example.login.Model.Drink
import com.example.login.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DrinkDetailActivity : AppCompatActivity() {

    // Khai báo các biến cho UI components
    private lateinit var imgDrink: ImageView
    private lateinit var tvDrinkName: TextView
    private lateinit var tvDrinkDesc: TextView
    private lateinit var backButton: LinearLayout
    private lateinit var favoriteButton: ImageButton
    private lateinit var cartButtonContainer: FrameLayout
    private lateinit var cartButton: ImageButton
    private lateinit var cartCountTextView: TextView
    private lateinit var sizeOptions: RadioGroup
    private lateinit var smallRadioButton: RadioButton
    private lateinit var mediumRadioButton: RadioButton
    private lateinit var largeRadioButton: RadioButton
    private lateinit var decreaseBtn: ImageButton
    private lateinit var increaseBtn: ImageButton
    private lateinit var quantityTextView: TextView
    private lateinit var tvPrice: TextView
    private lateinit var addToCartBtn: MaterialButton
    private lateinit var bottomNavigationView: BottomNavigationView

    // Biến để lưu trạng thái hiện tại
    private var currentQuantity = 1
    private var basePrice = 0.0
    private var currentSizeMultiplier = 1.0
    private var currentDrink: Drink? = null

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drink_detail)

        initializeViews()

        val drink: Drink? = intent.getParcelableExtra("drink")
        currentDrink = drink

        drink?.let {
            setupDrinkData(it)
            setupListeners(it)
            loadCartQuantity(it.id.toString()) // Tải số lượng từ Firestore
        }

        setupBottomNavigationView()
    }

    private fun initializeViews() {
        imgDrink = findViewById(R.id.imgDrink)
        tvDrinkName = findViewById(R.id.tvDrinkName)
        tvDrinkDesc = findViewById(R.id.tvDrinkDesc)
        backButton = findViewById(R.id.backButton)
        favoriteButton = findViewById(R.id.favoriteButton)
        cartButtonContainer = findViewById(R.id.cartButtonContainer)
        cartButton = findViewById(R.id.cartButton)
        cartCountTextView = findViewById(R.id.cartCountTextView)
        sizeOptions = findViewById(R.id.sizeOptions)
        smallRadioButton = findViewById(R.id.small)
        mediumRadioButton = findViewById(R.id.medium)
        largeRadioButton = findViewById(R.id.large)
        decreaseBtn = findViewById(R.id.decreaseBtn)
        increaseBtn = findViewById(R.id.increaseBtn)
        quantityTextView = findViewById(R.id.quantity)
        tvPrice = findViewById(R.id.tvPrice)
        addToCartBtn = findViewById(R.id.addToCartBtn)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
    }

    private fun setupDrinkData(drink: Drink) {
        tvDrinkName.text = drink.name
        tvDrinkDesc.text = drink.description
        try {
            basePrice = drink.price.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            basePrice = 0.0
        }
        updatePriceDisplay()
        Glide.with(this)
            .load(drink.imageResId)
            .into(imgDrink)
    }

    private fun loadCartQuantity(drinkId: String) {
        currentUser?.uid?.let { userId ->
            firestore.collection("cart_items")
                .whereEqualTo("userId", userId)
                .whereEqualTo("drinkId", drinkId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        val quantity = document.getLong("quantity")?.toInt() ?: 1
                        currentQuantity = quantity
                        quantityTextView.text = quantity.toString()
                        cartCountTextView.text = quantity.toString()
                        cartCountTextView.visibility = View.VISIBLE
                    } else {
                        currentQuantity = 1
                        quantityTextView.text = "1"
                        cartCountTextView.visibility = View.GONE
                    }
                    updatePriceDisplay()
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Lỗi khi tải số lượng giỏ hàng", e)
                    currentQuantity = 1
                    quantityTextView.text = "1"
                    cartCountTextView.visibility = View.GONE
                    updatePriceDisplay()
                }
        } ?: run {
            currentQuantity = 1
            quantityTextView.text = "1"
            cartCountTextView.visibility = View.GONE
            updatePriceDisplay()
        }
    }

    private fun setupListeners(drink: Drink) {
        backButton.setOnClickListener { finish() }
        favoriteButton.setOnClickListener { toggleFavorite() }
        sizeOptions.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.small -> currentSizeMultiplier = 0.8
                R.id.medium -> currentSizeMultiplier = 1.0
                R.id.large -> currentSizeMultiplier = 1.2
            }
            updatePriceDisplay()
        }
        decreaseBtn.setOnClickListener { if (currentQuantity > 1) { currentQuantity--; quantityTextView.text = currentQuantity.toString(); updatePriceDisplay() } }
        increaseBtn.setOnClickListener { currentQuantity++; quantityTextView.text = currentQuantity.toString(); updatePriceDisplay() }
        addToCartBtn.setOnClickListener {
            currentUser?.uid?.let { userId ->
                val cartItem = hashMapOf(
                    "userId" to userId,
                    "drinkId" to drink.id.toString(),
                    "name" to drink.name,
                    "quantity" to currentQuantity,
                    "price" to basePrice * currentSizeMultiplier,
                    "imageResId" to drink.imageResId
                )

                firestore.collection("cart_items")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("drinkId", drink.id.toString())
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val documentId = querySnapshot.documents[0].id
                            firestore.collection("cart_items").document(documentId)
                                .update("quantity", currentQuantity)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Đã cập nhật giỏ hàng cho ${drink.name} lên $currentQuantity")
                                    Toast.makeText(this, "Đã cập nhật giỏ hàng", Toast.LENGTH_SHORT).show()
                                    // Không cần cập nhật cartCountTextView ở đây nếu bạn muốn nó chỉ hiển thị tổng giỏ hàng
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Firestore", "Lỗi khi cập nhật giỏ hàng", e)
                                    Toast.makeText(this, "Lỗi cập nhật giỏ hàng", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            firestore.collection("cart_items")
                                .add(cartItem)
                                .addOnSuccessListener { documentReference ->
                                    Log.d("Firestore", "Đã thêm ${drink.name} vào giỏ hàng với số lượng $currentQuantity")
                                    Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                                    // Không cần cập nhật cartCountTextView ở đây nếu bạn muốn nó chỉ hiển thị tổng giỏ hàng
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Firestore", "Lỗi khi thêm vào giỏ hàng", e)
                                    Toast.makeText(this, "Lỗi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Lỗi khi kiểm tra sản phẩm trong giỏ hàng", e)
                        Toast.makeText(this, "Lỗi kiểm tra giỏ hàng", Toast.LENGTH_SHORT).show()
                    }
            } ?: run {
                Toast.makeText(this, "Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show()
            }
        }

        // Loại bỏ OnClickListener cho cartButtonContainer ở đây nếu bạn chỉ muốn điều hướng bằng BottomNavigationView
        // cartButtonContainer.setOnClickListener {
        //     val intent = Intent(this, CartActivity::class.java)
        //     startActivity(intent)
        // }
    }

    private fun setupBottomNavigationView() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.drinkMenu -> { // Đã đổi ID cho phù hợp với file menu
                    val intent = Intent(this, DrinkMenuActivity::class.java)
                    startActivity(intent)
                    true
                }
//                R.id.favorites -> {
//                    val intent = Intent(this, FavoritesActivity::class.java)
//                    startActivity(intent)
//                    true
//                }
                R.id.myCart -> {
                    val intent = Intent(this, CartActivity::class.java)
                    startActivity(intent)
                    true
                }
//                R.id.profile -> {
//                    val intent = Intent(this, ProfileActivity::class.java)
//                    startActivity(intent)
//                    true
//                }
                else -> false
            }
        }
    }

    private fun updatePriceDisplay() {
        val calculatedPrice = basePrice * currentSizeMultiplier * currentQuantity
        val formattedPrice = String.format("%,.0fđ", calculatedPrice)
        tvPrice.text = formattedPrice
    }

    private fun toggleFavorite() {
        // Thêm logic yêu thích (nếu cần)
    }

    override fun onResume() {
        super.onResume()
        currentDrink?.let {
            loadCartQuantity(it.id.toString()) // Tải lại số lượng khi Activity được resume
        }
    }
}