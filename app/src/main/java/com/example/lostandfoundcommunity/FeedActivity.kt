
package com.example.lostandfoundcommunity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FeedActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menuButton: ImageButton
    private lateinit var navigationView: NavigationView
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeedAdapter

    private val allPosts = mutableListOf<Post>()
    private val shownPosts = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        drawerLayout = findViewById(R.id.main)
        menuButton = findViewById(R.id.menuButton)
        navigationView = findViewById(R.id.navigationView)
        searchView = findViewById(R.id.searchView)
        recyclerView = findViewById(R.id.recyclerView)

        // Open Navigation Drawer
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FeedAdapter(shownPosts)
        recyclerView.adapter = adapter

        // Search
        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    applyFilter()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    applyFilter()
                    return true
                }
            }
        )

        // Load Data
        loadUserInfo()
        loadPosts()

        // Navigation Menu
        navigationView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.createPost -> {
                    startActivity(
                        Intent(this, CreatePostActivity::class.java)
                    )
                }

                R.id.yourPosts -> {
                    startActivity(
                        Intent(this, YourPostsActivity::class.java)
                    )
                }


                R.id.resetPassword -> {
                    startActivity(
                        Intent(this, ResetPasswordActivity::class.java)
                    )
                }


                R.id.logout -> {
                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    finish()
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Back Button
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    // Load Posts from Firebase
    private fun loadPosts() {

        FirebaseDatabase.getInstance()
            .getReference("posts")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    allPosts.clear()

                    for (child in snapshot.children) {

                        val post = child.getValue(Post::class.java)

                        if (post != null) {
                            post.id = child.key ?: ""
                            allPosts.add(post)
                        }
                    }

                    allPosts.sortByDescending { it.timestamp }
                    applyFilter()
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

    // Search Posts by Username
    private fun applyFilter() {

        val query = searchView.query.toString()
            .trim()
            .lowercase()

        shownPosts.clear()

        if (query.isEmpty()) {
            shownPosts.addAll(allPosts)
        } else {
            shownPosts.addAll(
                allPosts.filter {
                    it.username.lowercase().contains(query)
                }
            )
        }

        adapter.notifyDataSetChanged()
    }

    // Load User Information
    private fun loadUserInfo() {

        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid

        if (uid == null) {

            Toast.makeText(
                this,
                "Please login again",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
            return
        }

        val database = FirebaseDatabase.getInstance().reference

        // First check users/UID
        database.child("users")
            .child(uid)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.child("username").exists()) {

                            showUserInfo(snapshot, currentUser.email)

                        } else {

                            // If not found, check UID directly
                            database.child(uid)
                                .addListenerForSingleValueEvent(
                                    object : ValueEventListener {

                                        override fun onDataChange(
                                            rootSnapshot: DataSnapshot
                                        ) {
                                            showUserInfo(
                                                rootSnapshot,
                                                currentUser.email
                                            )
                                        }

                                        override fun onCancelled(
                                            error: DatabaseError
                                        ) {
                                            Toast.makeText(
                                                this@FeedActivity,
                                                error.message,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@FeedActivity,
                            error.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
    }

    // Show Username, Email and Profile Initial
    private fun showUserInfo(
        snapshot: DataSnapshot,
        authEmail: String?
    ) {

        val header = navigationView.getHeaderView(0)

        val initial = header.findViewById<TextView>(
            R.id.headerInitial
        )

        val username = header.findViewById<TextView>(
            R.id.headerUsername
        )

        val email = header.findViewById<TextView>(
            R.id.headerEmail
        )

        val savedUsername =
            snapshot.child("username")
                .getValue(String::class.java)

        val savedEmail =
            snapshot.child("email")
                .getValue(String::class.java)

        if (!savedUsername.isNullOrBlank()) {

            username.text = savedUsername
            initial.text = savedUsername.first().uppercase()

        } else {

            username.text = "Name not found"
            initial.text = "?"
        }

        email.text = savedEmail ?: authEmail ?: "Email not found"
    }
}
