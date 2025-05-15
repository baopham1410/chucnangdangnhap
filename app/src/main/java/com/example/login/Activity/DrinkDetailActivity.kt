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
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.login.Model.Drink
import com.example.login.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class DrinkDetailActivity : AppCompatActivity() {

    // Khai báo các biến cho UI components
    private lateinit var imgDrink: ImageView
    private lateinit var tvDrinkName: TextView
    private lateinit var tvDrinkDesc: TextView
    private lateinit var tvStockQuantity: TextView // Thêm TextView cho số lượng kho
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
    private var isFavorite: Boolean = false
    private var stockQuantity: Int = 0 // Biến lưu số lượng trong kho

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = auth.currentUser
    private var drinkListener: ListenerRegistration? = null // Thêm biến ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drink_detail)

        initializeViews()

        val drink: Drink? = intent.getParcelableExtra("drink")
        currentDrink = drink

        drink?.let {
            setupDrinkData(it)
            setupListeners(it)
            loadCartQuantity(it.id.toString())
            startDrinkDetailsListener(it.documentId.toString()) // Lắng nghe cả giá và số lượng
        }

        setupBottomNavigationView()
    }

    private fun initializeViews() {
        imgDrink = findViewById(R.id.imgDrink)
        tvDrinkName = findViewById(R.id.tvDrinkName)
        tvDrinkDesc = findViewById(R.id.tvDrinkDesc)
        tvStockQuantity = findViewById(R.id.tvStockQuantity) // Khởi tạo TextView số lượng kho
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
            Log.e("DrinkDetail", "Lỗi khi chuyển đổi giá: ${e.message}")
        }
        updatePriceDisplay()
        Glide.with(this)
            .load(drink.imageResId)
            .into(imgDrink)

        // Kiểm tra trạng thái yêu thích khi tải dữ liệu
        checkIfDrinkIsFavorite(drink.id.toString())
    }

    private fun updateStockQuantityDisplay() {
        Log.d("DrinkDetail", "Giá trị stockQuantity chuẩn bị hiển thị: $stockQuantity")
        tvStockQuantity.text = if (stockQuantity > 0) {
            "Còn lại: $stockQuantity"
        } else {
            "Hết hàng"
        }
        tvStockQuantity.setTextColor(ContextCompat.getColor(this, if (stockQuantity > 0) R.color.darkGreen else R.color.red))
    }

    private fun checkIfDrinkIsFavorite(drinkId: String) {
        currentUser?.uid?.let { userId ->
            firestore.collection("users")
                .document(userId)
                .collection("favorite_drinks")
                .whereEqualTo("drinkId", currentDrink?.documentId) // Sử dụng currentDrink?.documentId
                .get()
                .addOnSuccessListener { querySnapshot ->
                    isFavorite = !querySnapshot.isEmpty
                    Log.d("FavoriteCheck", "Sản phẩm ${currentDrink?.documentId} có phải là yêu thích: $isFavorite")
                    updateFavoriteButtonUI()
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Lỗi khi kiểm tra trạng thái yêu thích", e)
                    isFavorite = false
                    updateFavoriteButtonUI()
                }
        } ?: run {
            isFavorite = false
            updateFavoriteButtonUI()
        }
    }
    private fun updateFavoriteButtonUI() {
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_red)
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_white)
        }
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
        favoriteButton.setOnClickListener { toggleFavorite(drink) }
        sizeOptions.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.small -> currentSizeMultiplier = 0.8
                R.id.medium -> currentSizeMultiplier = 1.0
                R.id.large -> currentSizeMultiplier = 1.2
            }
            updatePriceDisplay()
        }
        decreaseBtn.setOnClickListener { if (currentQuantity > 1) { currentQuantity--; quantityTextView.text = currentQuantity.toString(); updatePriceDisplay() } }
        increaseBtn.setOnClickListener {
            if (currentQuantity < stockQuantity) {
                currentQuantity++
                quantityTextView.text = currentQuantity.toString()
                updatePriceDisplay()
            } else {
                Toast.makeText(this, "Số lượng trong kho không đủ", Toast.LENGTH_SHORT).show()
            }
        }
        addToCartBtn.setOnClickListener {
            if (stockQuantity > 0) {
                currentUser?.uid?.let { userId ->
                    val cartItem = hashMapOf(
                        "userId" to userId,
                        "drinkId" to drink.documentId,
                        "name" to drink.name,
                        "quantity" to currentQuantity,
                        "price" to basePrice * currentSizeMultiplier,
                        "imageResId" to drink.imageResId
                    )

                    firestore.collection("cart_items")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("drinkId", drink.documentId)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val documentId = querySnapshot.documents[0].id
                                firestore.collection("cart_items").document(documentId)
                                    .update("quantity", currentQuantity, "price", basePrice * currentSizeMultiplier)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Đã cập nhật giỏ hàng", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Lỗi cập nhật giỏ hàng", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                firestore.collection("cart_items")
                                    .add(cartItem)
                                    .addOnSuccessListener { documentReference ->
                                        Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Lỗi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Lỗi kiểm tra giỏ hàng", Toast.LENGTH_SHORT).show()
                        }
                } ?: run {
                    Toast.makeText(this, "Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Sản phẩm hiện đang hết hàng", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun toggleFavorite(drink: Drink) {
        currentUser?.uid?.let { userId ->
            isFavorite = !isFavorite
            updateFavoriteButtonUI()

            val favoriteRef = firestore.collection("users")
                .document(userId)
                .collection("favorite_drinks")

            if (isFavorite) {
                val favoriteItem = hashMapOf("drinkId" to drink.documentId) // Sử dụng drink.documentId
                favoriteRef.add(favoriteItem)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Lỗi khi thêm vào yêu thích", Toast.LENGTH_SHORT).show()
                        isFavorite = !isFavorite
                        updateFavoriteButtonUI()
                    }
            } else {
                favoriteRef.whereEqualTo("drinkId", drink.documentId) // Sử dụng drink.documentId
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            for (document in querySnapshot) {
                                document.reference.delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Lỗi khi xóa khỏi yêu thích", Toast.LENGTH_SHORT).show()
                                        isFavorite = !isFavorite
                                        updateFavoriteButtonUI()
                                    }
                                break
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Lỗi khi thao tác với yêu thích", Toast.LENGTH_SHORT).show()
                    }
            }
        } ?: run {
            Toast.makeText(this, "Bạn cần đăng nhập để sử dụng tính năng yêu thích", Toast.LENGTH_SHORT).show()
        }
    }
    private fun setupBottomNavigationView() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.drinkMenu -> {
                    val intent = Intent(this, DrinkMenuActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.favorites -> {
                    val intent = Intent(this, FavoritesActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.myCart -> {
                    val intent = Intent(this, CartActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun updatePriceDisplay() {
        val calculatedPrice = basePrice * currentSizeMultiplier * currentQuantity
        val formattedPrice = String.format("%,.0fđ", calculatedPrice)
        tvPrice.text = formattedPrice
    }
    private fun startDrinkDetailsListener(drinkDocumentId: String) {
        val drinkDocumentRef = firestore.collection("drink").document(drinkDocumentId)
        drinkListener = drinkDocumentRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Lỗi khi lắng nghe thông tin đồ uống", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val updatedPrice = snapshot.getString("price")
                val updatedQuantity = snapshot.getLong("quantity")?.toInt() ?: 0

                if (updatedPrice != null) {
                    try {
                        basePrice = updatedPrice.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
                        updatePriceDisplay()
                    } catch (ex: Exception) {
                        Log.e("Firestore", "Lỗi khi xử lý giá cập nhật: ${ex.message}")
                    }
                }

                stockQuantity = updatedQuantity
                updateStockQuantityDisplay() // Đảm bảo hàm này được gọi
            }
        }
    }


    override fun onPause() {
        super.onPause()
        stopDrinkDetailsListener()
    }

    private fun stopDrinkDetailsListener() {
        drinkListener?.remove()
        drinkListener = null
    }

    override fun onResume() {
        super.onResume()
        currentDrink?.documentId?.let {
            loadCartQuantity(currentDrink!!.id.toString())
            if (drinkListener == null) {
                startDrinkDetailsListener(it)
            }
            // Thêm log để kiểm tra drinkId
            val currentDrinkId = currentDrink?.id?.toString()
            Log.d("FavoriteCheck", "onResume - Kiểm tra drinkId: $currentDrinkId")
            currentDrinkId?.let { drinkId ->
                checkIfDrinkIsFavorite(drinkId)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDrinkDetailsListener()
    }


}