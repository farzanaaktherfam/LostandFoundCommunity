package com.example.lostandfoundcommunity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity(){
lateinit var email : EditText
    lateinit var pass : EditText

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email=findViewById<EditText>(R.id.loginEmailEditText)
        pass=findViewById<EditText>(R.id.pass)

    }

    fun startFeed(view: View) {

        var sEmail= email.text.toString()
        var sPass= pass.text.toString()

        FirebaseAuth.getInstance().signInWithEmailAndPassword(sEmail,sPass)
            .addOnSuccessListener { result ->
                val uid = result. user!!.uid
                val email = result.user!!.email

                FirebaseDatabase.getInstance().getReference("users").child(uid)

                    .get().addOnSuccessListener { snapshot ->
                        val userName= snapshot.getValue (String::class.java)
                        val intent = Intent(this@LoginActivity, FeedActivity::class.java)
                        intent.putExtra("Id",uid)
                        intent.putExtra("Email",email)
                        intent.putExtra("Username",userName)

                        startActivity(intent)

                    }

            }
    }
}