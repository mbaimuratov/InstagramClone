package com.example.android.instagramclone.ui.addpost

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.instagramclone.data.Event
import com.example.android.instagramclone.data.FirebaseRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class AddPostViewModel(private val firebaseRepository: FirebaseRepository) : ViewModel() {

    private val TAG = "AddPostViewModel"

    private val _isLoading = MutableLiveData<Event<Boolean>>()
    val isLoading: LiveData<Event<Boolean>> = _isLoading

    fun savePost(descriptionInput: String, imageUri: Uri) {

        viewModelScope.launch {
            firebaseRepository.uploadPost(descriptionInput, imageUri)

            val eventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _isLoading.value = Event(false)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i(TAG, error.message)
                }
            }

            FirebaseDatabase.getInstance()
                .getReference("Posts")
                .addListenerForSingleValueEvent(eventListener)
        }

      /**  viewModelScope.launch {
            firebaseRepository.uploadPost(descriptionInput, imageUri)

            val childEventListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d("savePost", "onChildAdded: ${snapshot.key}")
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d("savePost", "onChildChanged: ${snapshot.key}")

                    //_isLoading.value = Event(false)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {

                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            }

            FirebaseDatabase.getInstance()
                .getReference("Posts")
                .addChildEventListener(childEventListener)
        }
       **/
    }
}