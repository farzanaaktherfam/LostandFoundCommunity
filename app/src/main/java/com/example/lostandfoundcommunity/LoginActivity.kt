package com.example.lostandfoundcommunity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    lateinit var textV2: TextView
    lateinit var email: EditText
    lateinit var pass: EditText
    lateinit var login: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // আগে থেকে সাইন-ইন থাকলে সরাসরি Feed-এ যাও
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this@LoginActivity, FeedActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textV2 = findViewById(R.id.textV2)
        email = findViewById(R.id.email)
        pass = findViewById(R.id.password)
        login = findViewById(R.id.loginButton)

        textV2.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        login.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {

        val sEmail = email.text.toString().trim()
        val sPass = pass.text.toString()

        if (sEmail.isEmpty() || sPass.isEmpty()) {
            Toast.makeText(this@LoginActivity,
                "Please enter email and password.",
                Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(sEmail).matches()) {
            Toast.makeText(this@LoginActivity,
                "Enter a valid email address.",
                Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(sEmail, sPass)
            .addOnSuccessListener {

                startActivity(Intent(this@LoginActivity, FeedActivity::class.java))
                finish()
            }
            .addOnFailureListener {

                Toast.makeText(this@LoginActivity,
                    "Invalid email or password.",
                    Toast.LENGTH_SHORT).show()
            }
    }
}