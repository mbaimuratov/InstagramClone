package com.example.android.instagramclone.ui.addpost

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.android.instagramclone.R
import com.example.android.instagramclone.data.FirebaseRepositoryImpl
import com.example.android.instagramclone.databinding.AddPostFragmentBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddPostFragment : Fragment(R.layout.add_post_fragment) {

    private val REQUEST_CODE = 101
    private lateinit var viewModel: AddPostViewModel
    private lateinit var addPostFragmentBinding: AddPostFragmentBinding

    private var imageUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addPostFragmentBinding = AddPostFragmentBinding.bind(view)

        val firebaseRepository = FirebaseRepositoryImpl(
            FirebaseStorage.getInstance(),
            FirebaseDatabase.getInstance()
        )
        val factory = AddPostViewModelFactory(firebaseRepository)

        viewModel = ViewModelProvider(this, factory).get(AddPostViewModel::class.java)

        addPostFragmentBinding.newPostSelectImageBtn.setOnClickListener {
            openGalleryForImage()
        }

        addPostFragmentBinding.newPostSaveBtn.setOnClickListener {

            if (imageUri == null) {
                Toast.makeText(
                    requireContext(),
                    "Upload image before posting",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val descriptionInput = addPostFragmentBinding.newPostDescriptionEt.text.toString()

            if (descriptionInput.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Description should not be empty",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            viewModel.savePost(descriptionInput, imageUri!!)
        }

        viewModel.isLoading.observe(viewLifecycleOwner, {
            it.getContentIfNotHandledOrReturnNull()?.let { isLoading ->
                if (!isLoading) {
                    findNavController().popBackStack()
                }
            }
        })

    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            imageUri = data?.data!!
            Glide.with(requireContext()).load(imageUri).into(addPostFragmentBinding.newPostImageIv)
        }
    }

}