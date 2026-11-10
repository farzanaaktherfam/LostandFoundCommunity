package com.example.lostandfoundcommunity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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


''
    }

    fun startFeed(view: View) {
val sUser = user.text.toString()
val sEmail = email.text.toString()
val sPass = pass.text.toString()
val sConfirm = confirm.text.toString()
        if (sPass.length <6){
            Toast.makeText(this@RegisterActivity,
                "Password must be atleast 6 characters.",
                Toast.LENGTH_SHORT).show()

            return

        }
        if(sPass != sConfirm){
            Toast.makeText(this@RegisterActivity,
                "Password does not match.",
                Toast.LENGTH_SHORT).show()

            return
        }
        FirebaseDatabase.getInstance().getReference("users")
            .orderByValue().equalTo(sUser)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        Toast.makeText(this@RegisterActivity,
                            "user name already exists.",
                            Toast.LENGTH_SHORT).show()
                        return

                    }
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(sEmail, sPass)
                        .addOnSuccessListener { result ->
                            val uid = result.user!!.uid
                            FirebaseDatabase.getInstance().getReference("users")
                                .child(uid).setValue(sUser)
                            startActivity(Intent(this@RegisterActivity, FeedActivity::class.java))
                        finish()
                    }.addOnFailureListener {
                            Toast.makeText(this@RegisterActivity,
                                it.message,
                                Toast.LENGTH_SHORT).show()
                        }

                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegisterActivity,
                        error.message,
                        Toast.LENGTH_SHORT).show()
                }

            })
    }
    fun loginUser(view: View) {
        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
    }
}