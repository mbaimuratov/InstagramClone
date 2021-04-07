package com.example.android.instagramclone.trash

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    suspend fun uploadImage(uri: Uri): Flow<State<Uri>>
    suspend fun savePost(description: String, uri: Uri)
}