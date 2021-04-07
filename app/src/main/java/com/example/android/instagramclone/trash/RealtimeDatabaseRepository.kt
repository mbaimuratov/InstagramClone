package com.example.android.instagramclone.trash

import com.example.android.instagramclone.data.Post
import kotlinx.coroutines.flow.Flow

interface RealtimeDatabaseRepository {
    fun fetchPosts() : Flow<Result<List<Post>>>
    fun uploadPost()
}