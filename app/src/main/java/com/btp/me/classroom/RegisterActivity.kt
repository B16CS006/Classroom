package com.btp.me.classroom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.widget.*
import com.bumptech.glide.Glide

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register_activity.*

import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.collections.HashMap

class RegisterActivity : AppCompatActivity() {
    private var mStorageRef = FirebaseStorage.getInstance().reference
    private var currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var mUserReference: DatabaseReference

    var userMap = HashMap<String,String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_activity)
        title = "Registration"

        val currentUid = currentUser?.uid ?: finish()



        mUserReference = FirebaseDatabase.getInstance().getReference("Users/$currentUid")

        mUserReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val name = dataSnapshot.child("name").value?.toString()?: "default"
                val status = dataSnapshot.child("status").value?.toString()?: "default"

//                reg_name.hint = name
//                reg_status.hint = status

                reg_name.setText(name, TextView.BufferType.EDITABLE)
                reg_status.setText(status, TextView.BufferType.EDITABLE)

                val thumbsImgUri: String = dataSnapshot.child("thumbsImage").value?.toString()
                        ?: "default"

                val imgUri: String = dataSnapshot.child("image").value?.toString()
                        ?: "default"

                Log.d("chetan", thumbsImgUri)

                userMap["name"] = name
                userMap["status"] = status
                userMap["image"] = imgUri
                userMap["thumbsImage"] = thumbsImgUri
                userMap["fcm-token"] = "not assigned"

                val glide_image:Any = when(thumbsImgUri){"default","null" -> R.drawable.default_avatar else -> thumbsImgUri}
                Glide.with(reg_image).load(glide_image).into(reg_image)
                reg_progressBar.visibility = INVISIBLE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                reg_progressBar.visibility = View.INVISIBLE
                Toast.makeText(this@RegisterActivity, "On Cancelled", Toast.LENGTH_LONG).show()
            }
        })

        reg_image.setOnClickListener {
            val gallaryIntent = Intent()
            gallaryIntent.type = "image/*"
            gallaryIntent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(gallaryIntent, "Select Image"), 1)
        }

        reg_continue_btn.setOnClickListener {
            val name = reg_name.text.toString()
            val status = reg_status.text.toString()

            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(status)) {
                userMap["name"] = name
                userMap["status"] = status
                registerUser()
            } else {
                Toast.makeText(this, "Fields can not be empty", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendToHomePage() {
        startActivity(Intent(this, HomepageActivity::class.java))
        finish()
    }

    private fun registerUser() {
        currentUser?: sendToHomePage()

        val sp = applicationContext.getSharedPreferences("me.chetan", Context.MODE_PRIVATE)
        val token = sp.getString("fcm-token", "not assigned")

        userMap["fcm-token"] = token?: "not assigned"

        mUserReference.updateChildren(userMap.toMap()).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val mainIntent = Intent(this, MainActivity::class.java)
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(mainIntent)
                finish()
            } else {
                Log.d("chetan", task.exception!!.toString())
                Toast.makeText(this, task.exception!!.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        currentUser?: sendToHomePage()
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK
                && data != null && data.data != null) {

            reg_progressBar.visibility = View.VISIBLE

            try {
                val imageUri = data.data

                val thumbImage = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)


                val baos = ByteArrayOutputStream()
                thumbImage.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                val thumbData = baos.toByteArray()

                val currentUid = currentUser!!.uid
                mStorageRef = FirebaseStorage.getInstance().reference

                val filePath = mStorageRef.child("profile_images").child("$currentUid.jpg")
                val thumb_filePath = mStorageRef.child("profile_images").child("thumbs").child("$currentUid.jpg")


                filePath.putFile(imageUri!!).addOnSuccessListener {
                    filePath.downloadUrl.addOnSuccessListener { image_uri ->
                        val uploadTask = thumb_filePath.putBytes(thumbData)
                        uploadTask.addOnCompleteListener { thumb_task ->
                            if (thumb_task.isSuccessful) {

                                thumb_filePath.downloadUrl.addOnSuccessListener { thumbs_uri ->
                                    userMap["image"] = image_uri.toString()
                                    userMap["thumbsImage"] = thumbs_uri.toString()

                                    reg_progressBar.visibility = INVISIBLE
                                    Toast.makeText(this, "Successfully uploaded", Toast.LENGTH_LONG).show()
                                    val glide_image:Any = when(thumbs_uri.toString()){"default","null" -> R.drawable.default_avatar else -> thumbs_uri}
                                    Glide.with(reg_image).load(glide_image).into(reg_image)
//                                    Picasso.get().load(thumbs_uri).placeholder(R.drawable.default_avatar).into(reg_image)
                                }
                            } else {
                                reg_progressBar.visibility = INVISIBLE
                                Toast.makeText(this, "Failed to upload thumbnail", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }.addOnFailureListener { e ->
                    reg_progressBar.visibility = INVISIBLE
                    Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else {
            Toast.makeText(this, "Image can not be retrieve", Toast.LENGTH_LONG).show()
        }

    }

    override fun onStop() {
        super.onStop()
        userMap.clear()
    }
}