package com.example.android.instagramclone.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.instagramclone.data.FirebaseRepository
import com.example.android.instagramclone.ui.addpost.AddPostViewModel
import com.example.android.instagramclone.ui.feed.FeedViewModel

class ViewModelFactory(
    private val firebaseRepository: FirebaseRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddPostViewModel::class.java)) {
            return AddPostViewModel(firebaseRepository) as T
        } else if (modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            return FeedViewModel(firebaseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
