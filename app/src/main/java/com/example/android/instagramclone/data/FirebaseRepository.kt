package com.example.android.instagramclone.data

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface FirebaseRepository {
    suspend fun fetchPosts() : Flow<Result<List<Post>>>
    suspend fun uploadPost(description: String, imageUri: Uri)
}