package com.example.android.instagramclone.data

import android.net.Uri
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
    database: FirebaseDatabase
) : FirebaseRepository {

    private val postsNodeRef = database.getReference("Posts")

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

        postsNodeRef.addListenerForSingleValueEvent(postListener)

        awaitClose {
            postsNodeRef.removeEventListener(postListener)
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
                    val postId = postsNodeRef.push().key

                    val postMap = HashMap<String, Any>()
                    postMap["post_id"] = postId!!
                    postMap["post_description"] = description
                    postMap["post_image_url"] = task.result.toString()
                    postMap["post_has_like"] = false

                    postsNodeRef.child(postId).updateChildren(postMap)
                }
            }
        }

    override suspend fun likePost(isChecked: Boolean, postId: String?) {
        withContext(Dispatchers.IO) {
            val postRef = postsNodeRef.child(postId!!)
            postRef.child("post_has_like").setValue(isChecked)
        }
    }
}