
package com.example.lostandfoundcommunity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FeedActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menuButton: ImageButton
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        drawerLayout = findViewById(R.id.main)
        menuButton = findViewById(R.id.menuButton)
        navigationView = findViewById(R.id.navigationView)

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }

        loadUserInfo()

        navigationView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.createPost -> {
                    Toast.makeText(this, "Create Post", Toast.LENGTH_SHORT).show()
                }

                R.id.yourPosts -> {
                    Toast.makeText(this, "Your Posts", Toast.LENGTH_SHORT).show()
                }

                R.id.resetPassword -> {
                    Toast.makeText(this, "Reset Password", Toast.LENGTH_SHORT).show()
                }

                R.id.logout -> {
                    FirebaseAuth.getInstance().signOut()

                    startActivity(
                        Intent(this, LoginActivity::class.java)
                    )
                    finish()
                }
            }

            drawerLayout.closeDrawers()
            true
        }
    }

    private fun loadUserInfo() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val database = FirebaseDatabase.getInstance()

        database.getReference("users")
            .child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val header = navigationView.getHeaderView(0)

                    val username = header.findViewById<TextView>(
                        R.id.headerUsername
                    )

                    val email = header.findViewById<TextView>(
                        R.id.headerEmail
                    )

                    username.text =
                        snapshot.child("username").getValue(String::class.java)
                            ?: "Username"

                    email.text =
                        snapshot.child("email").getValue(String::class.java)
                            ?: "Email"
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@FeedActivity,
                        error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
