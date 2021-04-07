package com.example.android.instagramclone.trash

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class StorageRepositoryImpl(private val firebaseStorage: FirebaseStorage) : StorageRepository {

    override suspend fun uploadImage(uri: Uri) =
        flow<State<Uri>> {
            emit(State.loading())
            // Create a storage reference from our app
            val storageRef = firebaseStorage.reference

            val imageRef = storageRef.child("images/${uri.lastPathSegment}")
            val uploadTask = imageRef.putFile(uri)

            val urlTask = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result

                } else {
                    Log.i("", "")
                }
            }


        }.flowOn(Dispatchers.IO)

    override suspend fun savePost(description: String, uri: Uri) {
        TODO("Not yet implemented")
    }

}