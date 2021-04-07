package com.example.android.instagramclone.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    val post_id: String? = null,
    val post_description: String? = null,
    val post_image_url: String? = null,
    val post_has_like: Boolean = false
) : Parcelable
