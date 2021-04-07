package com.example.android.instagramclone.ui.feed

import android.content.Context
import android.os.Bundle
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
import com.example.android.instagramclone.R
import com.example.android.instagramclone.data.FirebaseRepositoryImpl
import com.example.android.instagramclone.databinding.FeedFragmentBinding
import com.example.android.instagramclone.ui.addpost.AddPostViewModelFactory
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi


class FeedFragment : Fragment(R.layout.feed_fragment) {

    private lateinit var viewModel: FeedViewModel

    private lateinit var binding: FeedFragmentBinding

    private lateinit var mSearchView: SearchView

    private lateinit var feedRvAdapter: FeedAdapter

    private var mSavedQuery = ""

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FeedFragmentBinding.bind(view)

        initViewModel()

        setOnClickListeners()

        feedRvAdapter = FeedAdapter()

        val layoutManager = LinearLayoutManager(activity)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true

        viewModel.posts.observe(viewLifecycleOwner) { postList ->
            feedRvAdapter.submitList(postList)

            val itemCount = postList.size - 1
            if (itemCount > 0) {
                binding.postsRecyclerview.smoothScrollToPosition(itemCount - 1)
            }
            binding.root.isRefreshing = false
        }

        binding.postsRecyclerview.adapter = feedRvAdapter
        binding.postsRecyclerview.layoutManager = layoutManager

        setOnClickListeners()

        viewModel.getPosts()

        setHasOptionsMenu(true)
    }

    private fun initViewModel() {
        val firebaseRepository = FirebaseRepositoryImpl(
            FirebaseStorage.getInstance(),
            FirebaseDatabase.getInstance()
        )
        val factory = AddPostViewModelFactory(firebaseRepository)

        viewModel = ViewModelProvider(this, factory).get(FeedViewModel::class.java)
    }

    @ExperimentalCoroutinesApi
    private fun setOnClickListeners() {
        binding.addPostFab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_addPostFragment)
        }

        binding.root.setOnRefreshListener { viewModel.getPosts() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)

        val searchMenuItem = menu.findItem(R.id.menu_search)

        mSearchView = searchMenuItem.actionView as SearchView

        mSearchView.onActionViewExpanded()

        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                mSearchView.requestFocus()

                feedRvAdapter.filter.filter(mSavedQuery)

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

                mSavedQuery = mSearchView.query.toString()

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

}