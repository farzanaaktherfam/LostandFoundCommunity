package com.example.lostandfoundcommunity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class YourPostsAdapter(
    private val posts: List<Post>,
    private val onEdit: (Post) -> Unit,
    private val onDelete: (Post) -> Unit
) : RecyclerView.Adapter<YourPostsAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val username: TextView =
            view.findViewById(R.id.postUsername)

        val date: TextView =
            view.findViewById(R.id.postDate)

        val image: ImageView =
            view.findViewById(R.id.postImage)

        val caption: TextView =
            view.findViewById(R.id.postCaption)

        val editButton: Button =
            view.findViewById(R.id.editPostButton)

        val deleteButton: Button =
            view.findViewById(R.id.deletePostButton)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_your_post, parent, false)

        return PostViewHolder(view)
    }

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(
        holder: PostViewHolder,
        position: Int
    ) {

        val post = posts[position]

        holder.username.text = post.username
        holder.caption.text = post.caption

        holder.date.text =
            SimpleDateFormat(
                "dd/MM/yyyy hh:mm:ss",
                Locale.ENGLISH
            ).format(Date(post.timestamp))

        Glide.with(holder.image.context)
            .load(post.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .centerCrop()
            .into(holder.image)

        holder.editButton.setOnClickListener {
            onEdit(post)
        }

        holder.deleteButton.setOnClickListener {
            onDelete(post)
        }
    }
}
