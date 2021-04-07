package com.example.android.instagramclone.data

import android.net.Uri
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap

class FirebaseRepositoryImpl(
    private val storage: FirebaseStorage,
    private val database: FirebaseDatabase
) : FirebaseRepository {

    @ExperimentalCoroutinesApi
    override suspend fun fetchPosts() = callbackFlow<Result<List<Post>>> {

        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.sendBlocking(Result.failure(error.toException()))
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val items = dataSnapshot.children.map { ds ->
                    ds.getValue(Post::class.java)
                }
                this@callbackFlow.sendBlocking(Result.success(items.filterNotNull()))
            }
        }

        val postRef = database.getReference("Posts")

        postRef.addListenerForSingleValueEvent(postListener)

        awaitClose {
            postRef.removeEventListener(postListener)
        }
    }

    override suspend fun uploadPost(description: String, imageUri: Uri): Unit =
        withContext(Dispatchers.IO) {
            val storageRef = storage.reference
            val imageRef = storageRef.child("images/${imageUri.lastPathSegment}")
            val uploadTask = imageRef.putFile(imageUri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result.toString()

                    val postRef = database.reference.child("Posts")
                    val postId = postRef.push().key

                    val postMap = HashMap<String, Any>()
                    postMap["post_id"] = postId!!
                    postMap["post_description"] = description
                    postMap["post_image_url"] = downloadUrl

                    postRef.child(postId).updateChildren(postMap)
                        .addOnCompleteListener {
                            Log.i("uploadPost", "$postId is updated")
                        }
                }
            }
        }
}