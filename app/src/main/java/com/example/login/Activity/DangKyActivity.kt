package com.example.login.Activity

import android.content.Intent
import android.os.Bundle
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

                    // Chuyển về màn hình Đăng nhập
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
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("Users")

        val user = hashMapOf(
            "id" to userId,
            "username" to username,
            "email" to email
        )

        database.child(userId).setValue(user).addOnSuccessListener {
            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
            finish() // Quay về màn hình trước (hoặc chuyển sang LoginActivity)
        }.addOnFailureListener {
            Toast.makeText(this, "Lưu dữ liệu thất bại!", Toast.LENGTH_SHORT).show()
        }
    }


}