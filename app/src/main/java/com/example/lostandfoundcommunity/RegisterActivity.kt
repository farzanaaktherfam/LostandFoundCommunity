package com.example.lostandfoundcommunity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterActivity : AppCompatActivity() {
   lateinit var user: EditText
    lateinit var email: EditText
    lateinit var pass: EditText
    lateinit var confirm: EditText
    lateinit var register: Button
    lateinit var Login: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        user = findViewById<EditText>(R.id.name)
        email = findViewById<EditText>(R.id.email)
        pass = findViewById<EditText>(R.id.Password)
        confirm = findViewById<EditText>(R.id.confirmPassword)
        register = findViewById<Button>(R.id.register)
        Login = findViewById<TextView>(R.id.Login)

        register.setOnClickListener {  }
        Login.setOnClickListener {  }


    }

    fun startFeed(view: View) {

    }
    fun loginUser(view: View) {
        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
    }
}