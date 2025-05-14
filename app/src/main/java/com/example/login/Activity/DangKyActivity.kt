package com.example.login.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.login.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class DangKyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dang_ky)
        val loginText = findViewById<TextView>(R.id.tvLogin)

        loginText.setOnClickListener {
            val intent = Intent(this, DangNhapActivity::class.java)
            startActivity(intent)
        }
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val username = findViewById<EditText>(R.id.etUsername).text.toString().trim()
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()
            val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword).text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(email, password, username)
        }


    }
    private fun registerUser(email: String, password: String, username: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                    saveUserToDatabase(username, email) // Gọi hàm lưu vào Firestore
                    // Chuyển về màn hình Đăng nhập (đã có trong saveUserToDatabase hoặc sau đó)
                    val intent = Intent(this, DangNhapActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Xóa activity hiện tại khỏi stack
                    startActivity(intent)
                    finish() // Đóng màn hình đăng ký
                } else {
                    Toast.makeText(this, "Lỗi: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToDatabase(username: String, email: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("DangKy", "Saving user to Firestore: UID = $userId, Username = $username, Email = $email")
        if (userId != null) {
            val firestore = FirebaseFirestore.getInstance()
            val userMap = hashMapOf(
                "uid" to userId,
                "fullName" to username, // Lưu username dưới tên "fullName"
                "email" to email
            )

            firestore.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Đăng ký thành công và thông tin người dùng đã được lưu!", Toast.LENGTH_SHORT).show()
                    finish() // Quay về màn hình trước (hoặc chuyển sang LoginActivity)
                }.addOnFailureListener { e ->
                    Log.e("DangKy", "Lỗi khi lưu thông tin người dùng vào Firestore: ${e.message}")
                    Toast.makeText(this, "Lỗi: Lưu thông tin người dùng thất bại!", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("DangKy", "Không thể lấy UID của người dùng sau khi đăng ký.")
            Toast.makeText(this, "Lỗi: Không thể lấy thông tin người dùng.", Toast.LENGTH_SHORT).show()
        }
    }



}