package com.example.android.instagramclone.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.instagramclone.data.Post
import com.example.android.instagramclone.databinding.PostItemBinding
import java.util.*
import kotlin.collections.ArrayList

class FeedAdapter : RecyclerView.Adapter<FeedAdapter.PostViewHolder>(), Filterable {

    private var postList = mutableListOf<Post>()

    private var postListFull = mutableListOf<Post>()

    private var isFilterActive = false

    class PostViewHolder(private val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.postDescription.text = post.post_description

            Glide.with(binding.postImage.context)
                .load(post.post_image_url)
                .into(binding.postImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = postList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return when {
            isFilterActive -> postList.size
            else -> postListFull.size
        }
    }

    fun submitList(postList: List<Post>) {
        this.postList = postList as MutableList<Post>
        this.postListFull = ArrayList(this.postList)
        notifyDataSetChanged()
    }

    fun disableFilterActive() {
        isFilterActive = false
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                isFilterActive = true

                var filteredList = arrayListOf<Post>()
                filteredList.addAll(postListFull)
                if (!constraint.isNullOrBlank()) {
                    val filterPattern = constraint.toString().toLowerCase(Locale.ROOT).trim()

                    filteredList = postListFull.filter { post ->
                        post.post_description!!.toLowerCase(Locale.ROOT).contains(filterPattern)
                    } as ArrayList<Post>
                }

                val result = FilterResults()
                result.values = filteredList
                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                if (results.values != null) {
                    postList.clear()
                    postList.addAll(results.values as List<Post>)
                    notifyDataSetChanged()
                }
            }
        }
    }
}