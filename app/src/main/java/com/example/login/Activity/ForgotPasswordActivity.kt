package com.example.login.Activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.login.R
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var resetPasswordButton: Button
    private lateinit var backToLoginTextView: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Ánh xạ các view từ layout
        editTextEmail = findViewById(R.id.editTextEmail)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)
        backToLoginTextView = findViewById(R.id.backToLoginTextView)

        // Khởi tạo Firebase Auth instance
        auth = FirebaseAuth.getInstance()

        // Xử lý sự kiện click cho nút "Gửi email đặt lại mật khẩu"
        resetPasswordButton.setOnClickListener {
            val email = editTextEmail.text.toString().trim()

            if (email.isEmpty()) {
                editTextEmail.error = "Vui lòng nhập email"
                return@setOnClickListener
            }

            sendPasswordResetEmail(email)
        }

        // Xử lý sự kiện click cho TextView "Quay lại đăng nhập"
        backToLoginTextView.setOnClickListener {
            finish() // Đóng activity hiện tại và quay lại activity trước đó (màn hình đăng nhập)
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        // Gửi email đặt lại mật khẩu bằng Firebase Auth
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Nếu email được gửi thành công
                    Toast.makeText(
                        this,
                        "Email đặt lại mật khẩu đã được gửi đến $email. Vui lòng kiểm tra hộp thư của bạn.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish() // Quay lại màn hình đăng nhập sau khi gửi email
                } else {
                    // Nếu có lỗi xảy ra khi gửi email
                    Toast.makeText(
                        this,
                        "Lỗi khi gửi email đặt lại mật khẩu: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}