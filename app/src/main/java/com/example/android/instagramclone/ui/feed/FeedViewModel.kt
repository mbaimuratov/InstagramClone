package com.example.android.instagramclone.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.instagramclone.data.FirebaseRepository
import com.example.android.instagramclone.data.Post
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class FeedViewModel(
    private val firebaseRepository: FirebaseRepository
) :
    ViewModel() {

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    @ExperimentalCoroutinesApi
    fun getPosts() {
        viewModelScope.launch {
            firebaseRepository.fetchPosts().collect { result ->
                when {
                    result.isSuccess -> {
                        val list = result.getOrNull()
                        _posts.value = list!!
                    }
                    result.isFailure -> {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
            }
        }
    }

    fun likePost(checked: Boolean, postId: String?) {
        viewModelScope.launch {
            firebaseRepository.likePost(checked, postId)
        }
    }
}