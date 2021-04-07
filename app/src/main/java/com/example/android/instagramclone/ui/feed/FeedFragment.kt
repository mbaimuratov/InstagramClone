package com.example.android.instagramclone.ui.feed

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.android.instagramclone.R
import com.example.android.instagramclone.data.FirebaseRepositoryImpl
import com.example.android.instagramclone.data.Post
import com.example.android.instagramclone.databinding.FeedFragmentBinding
import com.example.android.instagramclone.ui.ViewModelFactory
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class FeedFragment : Fragment(R.layout.feed_fragment), FeedAdapter.OnActionClickListener {

    private lateinit var viewModel: FeedViewModel

    private lateinit var binding: FeedFragmentBinding

    private lateinit var mSearchView: SearchView

    private lateinit var feedRvAdapter: FeedAdapter

    private var mSavedSearchQuery = ""

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FeedFragmentBinding.bind(view)

        initViewModel()

        feedRvAdapter = FeedAdapter(this)

        viewModel.posts.observe(viewLifecycleOwner) { postList ->
            feedRvAdapter.submitList(postList)

            val itemCount = postList.size
            if (itemCount > 0) {
                binding.postsRecyclerview.smoothScrollToPosition(itemCount - 1)
            }
            binding.root.isRefreshing = false
        }

        //fetch posts
        viewModel.getPosts()

        binding.apply {
            root.setOnRefreshListener { viewModel.getPosts() }

            postsRecyclerview.adapter = feedRvAdapter

            val layoutManager = LinearLayoutManager(activity)
            layoutManager.reverseLayout = true
            layoutManager.stackFromEnd = true
            postsRecyclerview.layoutManager = layoutManager

            addPostFab.setOnClickListener {
                findNavController().navigate(R.id.action_feedFragment_to_addPostFragment)
            }
        }

        setHasOptionsMenu(true)
    }

    private fun initViewModel() {
        val firebaseRepository = FirebaseRepositoryImpl(
            FirebaseStorage.getInstance(),
            FirebaseDatabase.getInstance()
        )
        val factory = ViewModelFactory(firebaseRepository)

        viewModel = ViewModelProvider(this, factory).get(FeedViewModel::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)

        val searchMenuItem = menu.findItem(R.id.menu_search)

        mSearchView = searchMenuItem.actionView as SearchView

        mSearchView.onActionViewExpanded()

        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                mSearchView.requestFocus()

                feedRvAdapter.filter.filter(mSavedSearchQuery)

                val imm =
                    activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(mSearchView.findFocus(), 0)

                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                feedRvAdapter.filter.filter("")

                val imm =
                    activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view!!.windowToken, 0)

                mSavedSearchQuery = mSearchView.query.toString()

                feedRvAdapter.disableFilterActive()

                val itemCount = feedRvAdapter.itemCount
                if (itemCount > 0) {
                    binding.postsRecyclerview.smoothScrollToPosition(itemCount - 1)
                }

                return true
            }
        })

        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                feedRvAdapter.filter.filter(newText)

                if (newText == "") {
                    feedRvAdapter.disableFilterActive()
                }

                val itemCount = feedRvAdapter.itemCount
                if (itemCount > 0) {
                    binding.postsRecyclerview.smoothScrollToPosition(itemCount - 1)
                }

                return true
            }
        })
    }


    override fun onLikeClicked(isChecked: Boolean, postId: String?) {
        viewModel.likePost(isChecked, postId)
    }

    override fun onShareClicked(post: Post) {
        Glide.with(requireContext())
            .asBitmap()
            .load(post.post_image_url)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.putExtra(Intent.EXTRA_TEXT, post.post_description)
                    shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(resource!!))
                    shareIntent.type = "image/*"
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(Intent.createChooser(shareIntent, "send_to"))
                    return false
                }
            }).submit()
    }

    private fun getLocalBitmapUri(bmp: Bitmap): Uri? {
        var bmpUri: Uri? = null
        try {
            val file = File(
                requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "share_image_" + System.currentTimeMillis() + ".jpeg"
            )
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.close()
            bmpUri = Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bmpUri
    }

}