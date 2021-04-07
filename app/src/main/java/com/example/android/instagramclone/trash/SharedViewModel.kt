package com.example.android.instagramclone.trash

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.instagramclone.data.FirebaseRepository
import com.example.android.instagramclone.data.Post
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SharedViewModel(private val firebaseRepository: FirebaseRepository) : ViewModel() {
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    @ExperimentalCoroutinesApi
    fun getPosts() {
        viewModelScope.launch {
            Log.i("getPosts", "launched")
            firebaseRepository.fetchPosts().collect { result ->
                when {
                    result.isSuccess -> {
                        Log.i("getPosts", "result.isSuccess")
                        val list = result.getOrNull()
                        _posts.value = list!!
                    }
                    result.isFailure -> {
                        Log.i("getPosts", "result.isFailure")
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
            }
        }
    }
}
