package com.example.android.instagramclone.trash

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.android.instagramclone.R
import com.example.android.instagramclone.data.FirebaseRepositoryImpl
import com.example.android.instagramclone.ui.MainActivity
import com.example.android.instagramclone.ui.addpost.AddPostViewModelFactory
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi

class SplashActivity : AppCompatActivity() {
    private lateinit var sharedViewModel: SharedViewModel

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        //setTheme(R.style.Theme_InstagramClone_Launcher)
        setContentView(R.layout.activity_splash)
        super.onCreate(savedInstanceState, persistentState)

        val firebaseRepository = FirebaseRepositoryImpl(
            FirebaseStorage.getInstance(),
            FirebaseDatabase.getInstance()
        )
        val factory = AddPostViewModelFactory(firebaseRepository)

        Log.i("SplashActivity", "Activity started")

        sharedViewModel = ViewModelProvider(this, factory).get(SharedViewModel::class.java)

        sharedViewModel.posts.observe(this, {
            startActivity(Intent(this, MainActivity::class.java))
        })

        sharedViewModel.getPosts()
    }
}