package com.btp.me.classroom

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_create_class.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class CreateClassActivity : AppCompatActivity() {

    private val mStorageRef by lazy { FirebaseStorage.getInstance().reference }
    private val mCurrentUser by lazy { FirebaseAuth.getInstance().currentUser }
    private val mRootRef by lazy{ FirebaseDatabase.getInstance().reference }

    private var userMap = HashMap<String, Any>()
    private val classId by lazy { mRootRef.child("Classroom").push().key }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_class)

        title = "Create New Class"

        mCurrentUser?:sendToHomePage()

        userMap["Classroom/$classId/name"] = "default"
        userMap["Classroom/$classId/status"] = "default"
        userMap["Classroom/$classId/profileImage"] = "default"
        userMap["Classroom/$classId/thumbsProfileImage"] = "default"


//        mRootRef.child("Classroom/$classId").addListenerForSingleValueEvent(object :ValueEventListener{
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val imgUri: String = dataSnapshot.child("thumbsProfileImage").value?.toString()?: "default"
//                Log.d("chetan", "Class Profile Image Link : $imgUri")
//                Glide.with(create_class_image).load(imgUri).into(create_class_image)
//                Picasso.get().load(imgUri).placeholder(R.drawable.default_avatar).into(create_class_image)
//            }
//
//            override fun onCancelled(p0: DatabaseError) {
//                Log.d("chetan","Database error : ${p0.message}")
//            }
//        })

        create_class_image.setOnClickListener{
            val galleryIntent = Intent()
            galleryIntent.type = "image/*"
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), 1)
        }

        create_Class_create_button.setOnClickListener {
            userMap["Classroom/$classId/name"] = create_class_name.text.toString()
            userMap["Classroom/$classId/status"] = create_class_status.text.toString()

            if (!TextUtils.isEmpty(userMap["Classroom/$classId/name"].toString()) &&
                    !TextUtils.isEmpty(userMap["Classroom/$classId/status"].toString())) {
                createClass()
            } else {
                Toast.makeText(this, "Fields can not be empty", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createClass() {
        mCurrentUser?:sendToHomePage()
        userMap["Class-Enroll/${mCurrentUser!!.uid}/$classId/as"] = "teacher"
        userMap["Classroom/$classId/members/${mCurrentUser!!.uid}"] = "teacher"
        mRootRef.updateChildren(userMap.toMap()).addOnCompleteListener { task ->
            if(task.isSuccessful){
                Toast.makeText(this,"Class is successfully created.",Toast.LENGTH_LONG).show()
                Log.d("chetan","Class is successfully created")
                finish()
            }else{
                Log.d("chetan","Error while uploading the final data on firebase")
                Toast.makeText(this,"Data is now uploaded to firebase",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendToHomePage() {
        startActivity(Intent(this, HomepageActivity::class.java))
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mCurrentUser?:sendToHomePage()
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK
                && data != null && data.data != null) {

            create_class_progress_bar.visibility = View.VISIBLE

            try {
                val imageUri = data.data
                val thumbImage = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

                val baos = ByteArrayOutputStream()
                thumbImage.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                val thumbData = baos.toByteArray()

                val filePath = mStorageRef.child("class_profile_images").child("$classId.jpg")
                val thumb_filePath = mStorageRef.child("class_profile_images").child("thumbs").child("$classId.jpg")

                filePath.putFile(imageUri!!).addOnSuccessListener {
                    filePath.downloadUrl.addOnSuccessListener { image_uri ->
                        val uploadTask = thumb_filePath.putBytes(thumbData)
                        uploadTask.addOnCompleteListener { thumb_task ->
                            if (thumb_task.isSuccessful) {
                                thumb_filePath.downloadUrl.addOnSuccessListener { thumbs_uri ->
                                    val glide_image:Any = when(thumbs_uri.toString()){"default","null" -> R.drawable.default_avatar else -> thumbs_uri}
                                    Glide.with(create_class_image).load(glide_image).into(create_class_image)
                                    userMap["Classroom/$classId/profileImage"] = image_uri.toString()
                                    userMap["Classroom/$classId/thumbsProfileImage"] = thumbs_uri.toString()
                                    mRootRef.updateChildren(userMap.toMap()).addOnCompleteListener {task ->
                                        if(task.isSuccessful){
                                            Toast.makeText(this, "Successfully uploaded", Toast.LENGTH_LONG).show()
                                        }else{
                                            Toast.makeText(this, "Failed to upload Image Try Again", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(this, "Failed to upload Image Try Again", Toast.LENGTH_SHORT).show()
                            }
                            create_class_progress_bar.visibility = View.INVISIBLE
                        }
                    }
                }.addOnFailureListener { e ->
                    create_class_progress_bar.visibility = View.INVISIBLE
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
