package com.example.login.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login.Adapter.CheckoutProductAdapter
import com.example.login.Model.CartItem
import com.example.login.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import java.text.NumberFormat
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var nameEditText: TextInputEditText
    private lateinit var phoneEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var savedAddressContainer: LinearLayout
    private lateinit var addNewAddressTextView: TextView
    private lateinit var changeAddressBtn: TextView
    private lateinit var newAddressLayout: LinearLayout
    private lateinit var streetAddressEditText: TextInputEditText
    private lateinit var cityEditText: TextInputEditText
    private lateinit var postalCodeEditText: TextInputEditText
    private lateinit var deliveryNotesEditText: TextInputEditText
    private lateinit var saveNewAddressButton: MaterialButton
    private lateinit var cancelNewAddressButton: MaterialButton
    private lateinit var creditCardLayout: LinearLayout
    private lateinit var cashLayout: LinearLayout
    private lateinit var creditCardCheckBox: CheckBox
    private lateinit var cashCheckBox: CheckBox
    private lateinit var placeOrderButton: MaterialButton
    private lateinit var orderItemsRecyclerView: RecyclerView
    private lateinit var checkoutProductAdapter: CheckoutProductAdapter
    private lateinit var subtotalText: TextView
    private lateinit var deliveryFeeText: TextView
    private lateinit var taxText: TextView
    private lateinit var totalText: TextView
    private lateinit var addressNameTextView: TextView
    private lateinit var addressDetailsTextView: TextView
    private lateinit var editAddressButton: ImageButton
    private lateinit var addPaymentBtnTextView: TextView
    private lateinit var couponCodeInputLayout: TextInputLayout
    private lateinit var couponCodeEditText: TextInputEditText
    private lateinit var applyCouponButton: MaterialButton
    private lateinit var termsAndConditionsTextView: TextView

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = auth.currentUser
    private var cartItemsList: List<CartItem> = emptyList()
    private var isEditingAddress = false // Biến để theo dõi trạng thái chỉnh sửa địa chỉ

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    private val DELIVERY_FEE = 15000.0
    private val TAX_RATE = 0.08 // 8% tax
    private var subtotalValue = 0.0
    private var totalValue = 0.0
    private var selectedPaymentMethod: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        initViews()
        setupClickListeners()
        loadSavedAddress()
        loadCartItemsForCheckout()
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        nameEditText = findViewById(R.id.nameEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        emailEditText = findViewById(R.id.emailEditText)
        savedAddressContainer = findViewById(R.id.savedAddressContainer)
        addNewAddressTextView = findViewById(R.id.addNewAddressTextView)
        changeAddressBtn = findViewById(R.id.changeAddressBtn)
        newAddressLayout = findViewById(R.id.newAddressLayout)
        streetAddressEditText = findViewById(R.id.streetAddressEditText)
        cityEditText = findViewById(R.id.cityEditText)
        postalCodeEditText = findViewById(R.id.postalCodeEditText)
        deliveryNotesEditText = findViewById(R.id.deliveryNotesEditText)
        saveNewAddressButton = findViewById(R.id.saveNewAddressButton)
        cancelNewAddressButton = findViewById(R.id.cancelNewAddressButton)
        creditCardLayout = findViewById(R.id.creditCardLayout)
        cashLayout = findViewById(R.id.cashLayout)
        creditCardCheckBox = findViewById(R.id.creditCardCheckBox)
        cashCheckBox = findViewById(R.id.cashCheckBox)
        placeOrderButton = findViewById(R.id.placeOrderButton)
        orderItemsRecyclerView = findViewById(R.id.orderItemsRecyclerView)
        checkoutProductAdapter = CheckoutProductAdapter(cartItemsList)
        orderItemsRecyclerView.adapter = checkoutProductAdapter
        orderItemsRecyclerView.layoutManager = LinearLayoutManager(this)
        subtotalText = findViewById(R.id.subtotalText)
        deliveryFeeText = findViewById(R.id.deliveryFeeText)
        taxText = findViewById(R.id.taxText)
        totalText = findViewById(R.id.totalText)
        addressNameTextView = findViewById(R.id.addressName)
        addressDetailsTextView = findViewById(R.id.addressDetails)
        editAddressButton = findViewById(R.id.editAddressButton)
        addPaymentBtnTextView = findViewById(R.id.addPaymentBtn)
        couponCodeInputLayout = findViewById(R.id.couponCodeInputLayout)
        couponCodeEditText = findViewById(R.id.couponCodeEditText)
        applyCouponButton = findViewById(R.id.applyCouponButton)
        termsAndConditionsTextView = findViewById(R.id.termsAndConditionsTextView)

        deliveryFeeText.text = formatPrice(DELIVERY_FEE)
        saveNewAddressButton.visibility = View.GONE
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener { finish() }

        changeAddressBtn.setOnClickListener {
            savedAddressContainer.visibility = View.GONE
            newAddressLayout.visibility = View.VISIBLE
            isEditingAddress = false // Người dùng đang nhập địa chỉ mới
            saveNewAddressButton.text = "Lưu địa chỉ giao hàng"
        }

        addNewAddressTextView.setOnClickListener {
            savedAddressContainer.visibility = View.GONE
            newAddressLayout.visibility = View.VISIBLE
            isEditingAddress = false // Người dùng đang nhập địa chỉ mới
            saveNewAddressButton.text = "Lưu địa chỉ giao hàng"
        }

        cancelNewAddressButton.setOnClickListener {
            newAddressLayout.visibility = View.GONE
            savedAddressContainer.visibility = View.VISIBLE
            isEditingAddress = false
            saveNewAddressButton.text = "Lưu địa chỉ giao hàng"
        }

        creditCardLayout.setOnClickListener {
            creditCardCheckBox.isChecked = true
            cashCheckBox.isChecked = false
            selectedPaymentMethod = "Credit Card"
            Log.d("CheckoutDebug", "Phương thức thanh toán đã chọn: Credit Card (CheckBox)")
        }

        cashLayout.setOnClickListener {
            cashCheckBox.isChecked = true
            creditCardCheckBox.isChecked = false
            selectedPaymentMethod = "Cash on Delivery"
            Log.d("CheckoutDebug", "Phương thức thanh toán đã chọn: Cash on Delivery (CheckBox)")
        }

        placeOrderButton.setOnClickListener {
            saveOrderDetails()
        }

        saveNewAddressButton.setOnClickListener {
            if (isEditingAddress) {
                updateDeliveryAddress()
            } else {
                saveNewDeliveryAddress()
            }
        }

        editAddressButton.setOnClickListener {
            val currentAddress = addressDetailsTextView.text.toString()
            val parts = currentAddress.split(", ")
            if (parts.size == 3) {
                streetAddressEditText.setText(parts[0].trim())
                cityEditText.setText(parts[1].trim())
                postalCodeEditText.setText(parts[2].trim())
            }
            savedAddressContainer.visibility = View.GONE
            newAddressLayout.visibility = View.VISIBLE
            isEditingAddress = true
            saveNewAddressButton.text = "Cập nhật địa chỉ"
        }

        applyCouponButton.setOnClickListener {
            Toast.makeText(
                this,
                "Chức năng áp dụng mã giảm giá đang được phát triển",
                Toast.LENGTH_SHORT
            ).show()
        }

        addPaymentBtnTextView.setOnClickListener {
            Toast.makeText(
                this,
                "Chức năng thêm phương thức thanh toán đang được phát triển",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadCartItemsForCheckout() {
        currentUser?.uid?.let { userId ->
            firestore.collection("cart_items")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    cartItemsList = querySnapshot.documents.map { document ->
                        val documentId = document.id
                        val drinkId = document.getString("drinkId") ?: ""
                        val name = document.getString("name") ?: ""
                        val price = document.getDouble("price") ?: 0.0
                        val quantity = document.getLong("quantity")?.toInt() ?: 1
                        val imageResId = document.getString("imageResId") ?: ""
                        CartItem(documentId, drinkId, name, price, quantity, imageResId)
                    }
                    calculateAndDisplayTotals()
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error getting cart items for checkout", e)
                    Toast.makeText(this, "Lỗi khi tải thông tin giỏ hàng", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun loadSavedAddress() {
        currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val fullName = document.getString("fullName") ?: ""
                        val phoneNumber = document.getString("phoneNumber") ?: ""
                        val email = document.getString("email") ?: ""
                        val deliveryAddress = document.getString("deliveryAddress") ?: ""
                        val deliveryName = document.getString("deliveryName") ?: ""
                        val city = document.getString("deliveryCity") ?: ""
                        val postalCode = document.getString("deliveryPostalCode") ?: ""

                        nameEditText.setText(fullName)
                        phoneEditText.setText(phoneNumber)
                        emailEditText.setText(email)

                        if (deliveryAddress.isNotEmpty() && deliveryName.isNotEmpty() && city.isNotEmpty() && postalCode.isNotEmpty()) {
                            addressNameTextView.text = deliveryName
                            addressDetailsTextView.text = "$deliveryAddress, $city, $postalCode"
                            savedAddressContainer.visibility = View.VISIBLE
                            newAddressLayout.visibility = View.GONE
                            isEditingAddress = false
                            saveNewAddressButton.text = "Lưu địa chỉ giao hàng"
                        } else {
                            newAddressLayout.visibility = View.VISIBLE
                            savedAddressContainer.visibility = View.GONE
                            isEditingAddress = false
                            saveNewAddressButton.text = "Lưu địa chỉ giao hàng"
                        }
                    } else {
                        newAddressLayout.visibility = View.VISIBLE
                        savedAddressContainer.visibility = View.GONE
                        isEditingAddress = false
                        saveNewAddressButton.text = "Lưu địa chỉ giao hàng"
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error loading user data", e)
                    Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show()
                    newAddressLayout.visibility = View.VISIBLE
                    savedAddressContainer.visibility = View.GONE
                    isEditingAddress = false
                    saveNewAddressButton.text = "Lưu địa chỉ giao hàng"
                }
        }
    }

    private fun saveNewDeliveryAddress() {
        val street = streetAddressEditText.text.toString().trim()
        val city = cityEditText.text.toString().trim()
        val postalCode = postalCodeEditText.text.toString().trim()

        if (street.isEmpty() || city.isEmpty() || postalCode.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin địa chỉ", Toast.LENGTH_SHORT)
                .show()
            return
        }

        currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId)
                .update(
                    "deliveryAddress", street,
                    "deliveryName", "Home", // Bạn có thể cho phép người dùng đặt tên địa chỉ sau
                    "deliveryCity", city,
                    "deliveryPostalCode", postalCode
                )
                .addOnSuccessListener {
                    Toast.makeText(this, "Địa chỉ giao hàng đã được lưu", Toast.LENGTH_SHORT).show()
                    loadSavedAddress() // Tải lại địa chỉ để cập nhật UI
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Lỗi khi cập nhật địa chỉ giao hàng", e)
                    Toast.makeText(this, "Lỗi khi lưu địa chỉ giao hàng", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateDeliveryAddress() {
        val street = streetAddressEditText.text.toString().trim()
        val city = cityEditText.text.toString().trim()
        val postalCode = postalCodeEditText.text.toString().trim()

        if (street.isEmpty() || city.isEmpty() || postalCode.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin địa chỉ", Toast.LENGTH_SHORT).show()
            return
        }

        currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId)
                .update(
                    "deliveryAddress", street,
                    "deliveryName", "Home", // Giữ nguyên hoặc cho phép người dùng thay đổi
                    "deliveryCity", city,
                    "deliveryPostalCode", postalCode
                )
                .addOnSuccessListener {
                    Toast.makeText(this, "Địa chỉ giao hàng đã được cập nhật", Toast.LENGTH_SHORT).show()
                    loadSavedAddress() // Tải lại địa chỉ để cập nhật UI
                    isEditingAddress = false
                    saveNewAddressButton.text = "Lưu địa chỉ giao hàng"
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Lỗi khi cập nhật địa chỉ giao hàng", e)
                    Toast.makeText(this, "Lỗi khi cập nhật địa chỉ giao hàng", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun calculateAndDisplayTotals() {
        subtotalValue = 0.0
        for (item in cartItemsList) {
            subtotalValue += item.price * item.quantity
        }

        val tax = subtotalValue * TAX_RATE
        totalValue = subtotalValue + tax + DELIVERY_FEE

        subtotalText.text = formatPrice(subtotalValue)
        taxText.text = formatPrice(tax)
        totalText.text = formatPrice(totalValue)
    }

    private fun formatPrice(price: Double): String {
        return currencyFormatter.format(price)
    }

    private fun saveOrderDetails() {
        val street = streetAddressEditText.text.toString().trim()
        val city = cityEditText.text.toString().trim()
        val postalCode = postalCodeEditText.text.toString().trim()
        val notes = deliveryNotesEditText.text.toString().trim()
        val fullName = nameEditText.text.toString().trim()
        val phoneNumber = phoneEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()

        val paymentMethod = selectedPaymentMethod
        Log.d("CheckoutDebug", "Phương thức thanh toán cuối cùng: $paymentMethod")

        if (paymentMethod.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn một phương thức thanh toán", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val fullAddress = if (newAddressLayout.visibility == View.VISIBLE) {
            "$street, $city, $postalCode"
        } else {
            addressDetailsTextView.text.toString()
        }

        currentUser?.uid?.let { userId ->
            val orderId = System.currentTimeMillis().toString()

            val orderData = hashMapOf(
                "userId" to userId,
                "orderId" to orderId,
                "fullName" to fullName,
                "phoneNumber" to phoneNumber,
                "email" to email,
                "deliveryAddress" to fullAddress,
                "deliveryName" to if (newAddressLayout.visibility == View.VISIBLE) street else addressNameTextView.text.toString(),
                "deliveryNotes" to notes,
                "paymentMethod" to paymentMethod,
                "subtotal" to subtotalValue,
                "deliveryFee" to DELIVERY_FEE,
                "tax" to (subtotalValue * TAX_RATE),
                "total" to totalValue,
                "items" to cartItemsList.map { item ->
                    hashMapOf(
                        "drinkId" to item.drinkId,
                        "name" to item.name,
                        "price" to item.price,
                        "quantity" to item.quantity,
                        "imageResId" to item.imageResId
                    )
                },
                "timestamp" to Timestamp.now(),
                "paymentStatus" to if (paymentMethod == "Credit Card") "Pending" else "Chưa thanh toán"
            )

            firestore.collection("checkouts")
                .add(orderData as Map<String, Any>)
                .addOnSuccessListener { documentReference ->
                    Log.d(
                        "Firestore",
                        "Order details saved with ID: ${documentReference.id}, Order ID: $orderId"
                    )
                    Toast.makeText(
                        this,
                        "Đặt hàng thành công! Mã đơn hàng: $orderId",
                        Toast.LENGTH_LONG
                    ).show()

                    if (paymentMethod == "Credit Card") {
                        val intentToQR = Intent(this, QRCodePaymentActivity::class.java)
                        intentToQR.putExtra("totalAmount", totalValue)
                        intentToQR.putExtra("orderId", orderId)
                        startActivity(intentToQR)
                    } else {
                        val intentToHome = Intent(this, HomeActivity::class.java)
                        startActivity(intentToHome)
                        finish() // Kết thúc CheckoutActivity
                    }

                    val batch = firestore.batch()

                    for (cartItem in cartItemsList) {
                        val drinkDocumentRef =
                            firestore.collection("drink").document(cartItem.drinkId)
                        batch.update(
                            drinkDocumentRef,
                            "quantity",
                            FieldValue.increment(-cartItem.quantity.toLong())
                        )
                    }

                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("Firestore", "Đã cập nhật số lượng kho sau khi đặt hàng")

                            firestore.collection("cart_items")
                                .whereEqualTo("userId", userId)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    val cartBatch = firestore.batch()
                                    for (document in querySnapshot.documents) {
                                        cartBatch.delete(document.reference)
                                    }
                                    cartBatch.commit()
                                        .addOnSuccessListener {
                                            Log.d("Firestore", "Giỏ hàng đã được xóa sau khi đặt hàng")
                                            if (paymentMethod != "Credit Card") {
                                                finish() // Kết thúc CheckoutActivity sau khi xóa giỏ hàng nếu không chuyển sang QR
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(
                                                "Firestore",
                                                "Lỗi khi xóa giỏ hàng sau khi đặt hàng",
                                                e
                                            )
                                            Toast.makeText(
                                                this,
                                                "Lỗi khi xóa giỏ hàng",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            // Có thể xử lý lỗi xóa giỏ hàng ở đây
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Lỗi khi truy vấn giỏ hàng để xóa", e)
                                    Toast.makeText(
                                        this,
                                        "Lỗi khi truy vấn giỏ hàng",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // Có thể xử lý lỗi truy vấn giỏ hàng ở đây
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Lỗi khi cập nhật số lượng kho", e)
                            Toast.makeText(
                                this,
                                "Lỗi khi cập nhật số lượng kho",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Có thể xử lý lỗi cập nhật kho ở đây
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error saving order details", e)
                    Toast.makeText(this, "Lỗi khi đặt hàng", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "Bạn cần đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show()
        }
    }
}