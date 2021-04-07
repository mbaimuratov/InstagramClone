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

    private val _isDataLoaded = MutableLiveData<Event<Boolean>>()
    val isDataLoaded: LiveData<Event<Boolean>> = _isDataLoaded

    fun savePost(descriptionInput: String, imageUri: Uri) {

        viewModelScope.launch {
            firebaseRepository.uploadPost(descriptionInput, imageUri)

            val eventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _isDataLoaded.value = Event(true)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i("AddPostViewModel", error.message)
                }
            }

            FirebaseDatabase.getInstance()
                .getReference("Posts")
                .addListenerForSingleValueEvent(eventListener)
        }
    }
}