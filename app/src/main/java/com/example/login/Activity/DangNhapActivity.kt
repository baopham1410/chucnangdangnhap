package com.example.login.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.login.MainActivity
import com.example.login.R
import com.google.firebase.auth.FirebaseAuth

class DangNhapActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var registerText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dang_nhap)
        // Kiểm tra đăng nhập trước đó
        val sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Ánh xạ view
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        registerText = findViewById(R.id.tvRegister)

        registerText.setOnClickListener {
            val intent = Intent(this, DangKyActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Lưu thông tin đăng nhập
                    val sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("email", email)
                    editor.putString("password", password)
                    editor.putBoolean("isLoggedIn", true) // Đánh dấu đã đăng nhập
                    editor.apply()

                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                    // Chuyển sang HomeActivity
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Lỗi: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi Firebase: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


}
