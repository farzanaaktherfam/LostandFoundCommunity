
package com.example.lostandfoundcommunity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

        user = findViewById(R.id.name)
        email = findViewById(R.id.email)
        pass = findViewById(R.id.Password)
        confirm = findViewById(R.id.confirmPassword)
        register = findViewById(R.id.register)
        register.setOnClickListener {
            Toast.makeText(this, "Button clicked", Toast.LENGTH_SHORT).show()
            startFeed(it)
        }
        Login = findViewById(R.id.Login)



        Login.setOnClickListener {
            startActivity(
                Intent(this@RegisterActivity, LoginActivity::class.java)
            )
        }
    }

    fun startFeed(view: View) {
        Toast.makeText(this, "Button is working", Toast.LENGTH_LONG).show()
        val sUser = user.text.toString().trim()
        val sEmail = email.text.toString().trim()
        val sPass = pass.text.toString()
        val sConfirm = confirm.text.toString()

        if (sUser.isEmpty() || sEmail.isEmpty() ||
            sPass.isEmpty() || sConfirm.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!sUser.matches(Regex("^[a-zA-Z0-9_]{3,20}$"))) {
            Toast.makeText(this, "Username must be 3-20 characters (letters, numbers or _).", Toast.LENGTH_LONG).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(sEmail).matches()) {
            Toast.makeText(this, "Enter a valid email address.", Toast.LENGTH_SHORT).show()
            return
        }

        if (sPass.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
            return
        }

        if (sPass != sConfirm) {
            Toast.makeText(this, "Password does not match.", Toast.LENGTH_SHORT).show()
            return
        }

        register.isEnabled = false

        val usernameKey = sUser.lowercase()
        val database = FirebaseDatabase.getInstance()
        val usernamesRef = database.getReference("usernames")

        usernamesRef.child(usernameKey)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        register.isEnabled = true
                        Toast.makeText(
                            this@RegisterActivity,
                            "Username already exists.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(sEmail, sPass)
                        .addOnSuccessListener { result ->

                            val uid = result.user!!.uid

                            val userData = HashMap<String, String>()
                            userData["username"] = sUser
                            userData["email"] = sEmail

                            val updates = HashMap<String, Any>()
                            updates["users/$uid"] = userData
                            updates["usernames/$usernameKey"] = uid

                            database.reference.updateChildren(updates)
                                .addOnSuccessListener {

                                    startActivity(
                                        Intent(
                                            this@RegisterActivity,
                                            FeedActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                                .addOnFailureListener {
                                    register.isEnabled = true
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        it.message ?: "Database error",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                        .addOnFailureListener {
                            register.isEnabled = true
                            Toast.makeText(
                                this@RegisterActivity,
                                it.message ?: "Registration failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    register.isEnabled = true
                    Toast.makeText(
                        this@RegisterActivity,
                        error.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    fun loginUser(view: View) {
        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
    }
}
