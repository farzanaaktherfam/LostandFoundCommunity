package com.example.lostandfoundcommunity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class YourPostsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var adapter: YourPostsAdapter

    private val posts = mutableListOf<Post>()
    private val database =
        FirebaseDatabase.getInstance().getReference("posts")

    private var postsListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_posts)

        recyclerView = findViewById(R.id.yourPostsRecyclerView)
        emptyText = findViewById(R.id.emptyPostsText)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = YourPostsAdapter(
            posts,
            onEdit = {
                Toast.makeText(
                    this,
                    "Edit feature will be added next",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDelete = { post ->
                deletePost(post)
            }
        )

        recyclerView.adapter = adapter

        loadYourPosts()
    }

    private fun loadYourPosts() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        postsListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                posts.clear()

                for (child in snapshot.children) {

                    val post = child.getValue(Post::class.java)
                        ?: continue

                    if (post.uid == uid) {
                        post.id = child.key ?: ""
                        posts.add(post)
                    }
                }

                posts.sortByDescending { it.timestamp }

                adapter.notifyDataSetChanged()

                emptyText.visibility =
                    if (posts.isEmpty()) View.VISIBLE else View.GONE

                recyclerView.visibility =
                    if (posts.isEmpty()) View.GONE else View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@YourPostsActivity,
                    error.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        database.addValueEventListener(postsListener!!)
    }

    private fun deletePost(post: Post) {

        AlertDialog.Builder(this)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->

                if (post.id.isEmpty()) return@setPositiveButton

                database.child(post.id)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Post deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Delete failed: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()

        postsListener?.let {
            database.removeEventListener(it)
        }
    }
}
