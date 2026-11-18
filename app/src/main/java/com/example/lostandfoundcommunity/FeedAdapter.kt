package com.example.lostandfoundcommunity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FeedAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<FeedAdapter.VH>() {

    private val fmt = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.ENGLISH)

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val username: TextView = v.findViewById(R.id.postUsername)
        val date: TextView = v.findViewById(R.id.postDate)
        val image: ImageView = v.findViewById(R.id.postImage)
        val caption: TextView = v.findViewById(R.id.postCaption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feed, parent, false)
        return VH(v)
    }

    override fun getItemCount() = posts.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val p = posts[position]
        h.username.text = p.username
        h.date.text = fmt.format(Date(p.timestamp))
        h.caption.text = p.caption
        Glide.with(h.image.context)
            .load(p.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .centerCrop()
            .into(h.image)
    }
}