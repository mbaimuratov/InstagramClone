package com.example.android.instagramclone.trash

import com.example.android.instagramclone.data.Post
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow

class RealtimeDatabaseRepositoryImpl constructor(
    private val firebaseDatabase: FirebaseDatabase
) : RealtimeDatabaseRepository {

    companion object {
        const val POST_REFERENCE = "post"
    }

    @ExperimentalCoroutinesApi
    override fun fetchPosts() = callbackFlow<Result<List<Post>>> {
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
        firebaseDatabase.getReference(POST_REFERENCE)
            .addValueEventListener(postListener)

        awaitClose {
            firebaseDatabase.getReference(POST_REFERENCE)
                .removeEventListener(postListener)
        }
    }

    override fun uploadPost() {
        TODO("Not yet implemented")
    }

}