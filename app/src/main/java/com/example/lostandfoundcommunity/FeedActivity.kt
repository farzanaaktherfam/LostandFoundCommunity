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

        // Initialize views
        drawerLayout = findViewById(R.id.main)
        menuButton = findViewById(R.id.menuButton)
        navigationView = findViewById(R.id.navigationView)
        searchView = findViewById(R.id.searchView)
        recyclerView = findViewById(R.id.recyclerView)

        // Menu Button Click
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Feed list
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FeedAdapter(shownPosts)
        recyclerView.adapter = adapter

        // Search
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                applyFilter()
                return true
            }
        })

        // Load User Information
        loadUserInfo()

        // Load posts
        loadPosts()

        // Navigation Menu Click
        navigationView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.createPost -> {
                    startActivity(
                        Intent(this, CreatePostActivity::class.java)
                    )
                }

                R.id.yourPosts -> {
                    startActivity(Intent(this, YourPostsActivity::class.java))
                }

                R.id.resetPassword -> {
                    Toast.makeText(
                        this,
                        "Reset Password",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                R.id.logout -> {
                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    // Load posts from Firebase (newest first)
    private fun loadPosts() {
        FirebaseDatabase.getInstance().getReference("posts")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    allPosts.clear()
                    for (child in snapshot.children) {
                        val post = child.getValue(Post::class.java) ?: continue
                        post.id = child.key ?: ""
                        allPosts.add(post)
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

    // Search by username (capital/small letters same)
    private fun applyFilter() {
        val q = searchView.query.toString().trim().lowercase()
        shownPosts.clear()
        if (q.isEmpty()) {
            shownPosts.addAll(allPosts)
        } else {
            shownPosts.addAll(allPosts.filter { it.username.lowercase().contains(q) })
        }
        adapter.notifyDataSetChanged()
    }

    // Load User Information
    private fun loadUserInfo() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {

            Toast.makeText(
                this,
                "Please login again",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        }

        val database = FirebaseDatabase.getInstance()

        database.getReference("users")
            .child(uid)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        val header = navigationView.getHeaderView(0)

                        val username = header.findViewById<TextView>(
                            R.id.headerUsername
                        )

                        val email = header.findViewById<TextView>(
                            R.id.headerEmail
                        )

                        username.text =
                            snapshot.child("username")
                                .getValue(String::class.java)
                                ?: "Username"

                        email.text =
                            snapshot.child("email")
                                .getValue(String::class.java)
                                ?: FirebaseAuth.getInstance()
                                    .currentUser?.email
                                        ?: "Email"
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
}