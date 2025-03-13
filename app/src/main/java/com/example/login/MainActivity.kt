package com.example.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.login.Activity.DangNhapActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

    }
    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // Nếu chưa đăng nhập, chuyển về màn hình đăng nhập
            val intent = Intent(this, DangNhapActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}