package com.example.login.Activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.login.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CheckoutActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
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
    private lateinit var savePersonalInfoButton: MaterialButton // Nút lưu thông tin cá nhân

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        initViews()
        setupClickListeners()
        loadSavedAddress() // Tải địa chỉ và thông tin cá nhân đã lưu khi Activity được tạo
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

    }

    private fun setupClickListeners() {
        backButton.setOnClickListener { finish() }


        changeAddressBtn.setOnClickListener {
            savedAddressContainer.visibility = View.GONE
            newAddressLayout.visibility = View.VISIBLE
        }

        addNewAddressTextView.setOnClickListener {
            savedAddressContainer.visibility = View.GONE
            newAddressLayout.visibility = View.VISIBLE
        }

        cancelNewAddressButton.setOnClickListener {
            newAddressLayout.visibility = View.GONE
            savedAddressContainer.visibility = View.VISIBLE
        }

        saveNewAddressButton.setOnClickListener {
            saveNewDeliveryAddressAndPersonalInfo()
        }
    }



    private fun loadSavedAddress() {
        currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("deliveryName")
                        val address = document.getString("deliveryAddress")
                        if (!name.isNullOrEmpty() && !address.isNullOrEmpty()) {
                            // Nếu đã có địa chỉ, hiển thị nó (cần cập nhật UI)
                        } else {
                            // Nếu chưa có địa chỉ, có thể hiển thị form thêm địa chỉ
                            newAddressLayout.visibility = View.VISIBLE
                            savedAddressContainer.visibility = View.GONE
                        }
                        // Load thông tin cá nhân
                        val fullName = document.getString("fullName")
                        val phoneNumber = document.getString("phoneNumber")
                        val email = document.getString("email")
                        nameEditText.setText(fullName)
                        phoneEditText.setText(phoneNumber)
                        emailEditText.setText(email)
                    } else {
                        // Nếu document người dùng không tồn tại, có thể hiển thị form địa chỉ
                        newAddressLayout.visibility = View.VISIBLE
                        savedAddressContainer.visibility = View.GONE
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error loading user data", e)
                    Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show()
                    newAddressLayout.visibility = View.VISIBLE
                    savedAddressContainer.visibility = View.GONE
                }
        }
    }

    private fun saveNewDeliveryAddressAndPersonalInfo() {
        val street = streetAddressEditText.text.toString().trim()
        val city = cityEditText.text.toString().trim()
        val postalCode = postalCodeEditText.text.toString().trim()
        val notes = deliveryNotesEditText.text.toString().trim()

        val fullName = nameEditText.text.toString().trim()
        val phoneNumber = phoneEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()

        if (street.isEmpty() || city.isEmpty() || postalCode.isEmpty() || fullName.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please fill in all required details", Toast.LENGTH_SHORT).show()
            return
        }

        val fullAddress = "$street, $city, $postalCode"

        currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId)
                .update(
                    "fullName", fullName,
                    "phoneNumber", phoneNumber,
                    "email", email,
                    "deliveryAddress", fullAddress,
                    "deliveryName", street // Using street for name for simplicity
                )
                .addOnSuccessListener {
                    Toast.makeText(this, "Information and delivery address saved", Toast.LENGTH_SHORT).show()
                    loadSavedAddress() // Tải lại để hiển thị địa chỉ đã lưu
                    newAddressLayout.visibility = View.GONE
                    savedAddressContainer.visibility = View.VISIBLE
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error saving information and delivery address", e)
                    Toast.makeText(this, "Error saving information and delivery address", Toast.LENGTH_SHORT).show()
                }
        }
    }
}