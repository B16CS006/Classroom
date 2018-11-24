package com.btp.me.classroom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.widget.*
import com.btp.me.classroom.slide.MyUploadingService
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register_activity.*

import kotlin.collections.HashMap

class UserProfileActivity : AppCompatActivity() {
    private var mRootRef = FirebaseStorage.getInstance().reference
    private val currentUser: FirebaseUser? by lazy { FirebaseAuth.getInstance().currentUser }
    private lateinit var mUserReference: DatabaseReference

    private val imageListener by lazy {  object :ValueEventListener{
        override fun onCancelled(databaseError: DatabaseError) {
            reg_progressBar.visibility = View.INVISIBLE
            reg_scroll_view.visibility = View.VISIBLE
            Toast.makeText(this@UserProfileActivity, "Error : ${databaseError.message}", Toast.LENGTH_LONG).show()
            Log.d("chetan", "error : ${databaseError.message}")
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            Log.d("chetan", "data imagge : $dataSnapshot")
            val thumbsImgUri = dataSnapshot.child("thumbImage").value?.toString()?: dataSnapshot.child("image").value.toString()


            if(thumbsImgUri != "null")
                Glide.with(reg_image).load(thumbsImgUri).into(reg_image)

        }
    }}

    var userMap = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_activity)

        if(currentUser == null){
            sendToHomepage()
            return
        }

        initialize()

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
                userMap["register"] = "yes"
                registerUser()
            } else {
                Toast.makeText(this, "Fields can not be empty", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun initialize() {
        title = "User Profile"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        reg_scroll_view.visibility = View.INVISIBLE

        mUserReference = FirebaseDatabase.getInstance().getReference("Users/${currentUser!!.uid}")

        mUserReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                Log.d("chetan", "Register : $dataSnapshot")
                if (!dataSnapshot.exists()) {
                    reg_progressBar.visibility = View.INVISIBLE
                    reg_scroll_view.visibility = View.VISIBLE
                    return
                }

                val name = dataSnapshot.child("name").value.toString()
                val status = dataSnapshot.child("status").value.toString()



                reg_name.setText(name, TextView.BufferType.EDITABLE)
                reg_status.setText(status, TextView.BufferType.EDITABLE)

                val fcmToken = dataSnapshot.child("fcm-token").value.toString()

                userMap["name"] = name
                userMap["status"] = status
                userMap["fcm-token"] = fcmToken

                Log.d("chetan", "Registerk: $userMap")

                reg_progressBar.visibility = INVISIBLE
                reg_scroll_view.visibility = View.VISIBLE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                reg_progressBar.visibility = View.INVISIBLE
                reg_scroll_view.visibility = View.VISIBLE
                Toast.makeText(this@UserProfileActivity, "Error : ${databaseError.message}", Toast.LENGTH_LONG).show()
                Log.d("chetan", "error : ${databaseError.message}")
            }
        })

        mUserReference.addValueEventListener(imageListener)
    }

    private fun sendToHomepage() {
        startActivity(Intent(this, HomepageActivity::class.java))
        finish()
    }

    private fun registerUser() {
        val sp = applicationContext.getSharedPreferences("me.chetan", Context.MODE_PRIVATE)
        val token = sp.getString("fcm-token", "null") ?: "null"

        userMap["fcm-token"] = token
        userMap["phone"] = currentUser!!.phoneNumber.toString()

        mUserReference.updateChildren(userMap.toMap()).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                setDisplayName(userMap["name"])

//                val mainIntent = Intent(this, MainActivity::class.java)
//                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                startActivity(mainIntent)
//                Toast.makeText(this, "Welcome ${currentUser!!.displayName}!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Log.d("chetan", task.exception!!.toString())
                Toast.makeText(this, task.exception!!.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setDisplayName(s: String?) {
        val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(s)
                .build()

        currentUser!!.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("chetan", "User profile updated.")
            }
        }

    }

    private fun upload(uri: Uri) {
        val data = """{"image": ""}"""
        Log.d("chetan", "uploading Uri : $uri")
        val uploadingIntent = Intent(this, MyUploadingService::class.java)

        uploadingIntent.putExtra("fileUri", uri)
        uploadingIntent.putExtra("storagePath", "UsersProfile/${currentUser!!.uid}")
        uploadingIntent.putExtra("databasePath", "Users/${currentUser!!.uid}")
        uploadingIntent.putExtra("data", data)
        uploadingIntent.putExtra("link", "image")

        uploadingIntent.action = MyUploadingService.ACTION_UPLOAD
        startService(uploadingIntent)
                ?: Log.d("chetan", "At this this no activity is running")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            if (requestCode == 1) {
                upload(data.data!!)
            }

//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == 1 && resultCode == Activity.RESULT_OK
//                && data != null && data.data != null) {
//
//            reg_progressBar.visibility = View.VISIBLE
//
//            try {
//                val imageUri = data.data
//
//                val thumbImage = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
//
//
//                val baos = ByteArrayOutputStream()
//                thumbImage.compress(Bitmap.CompressFormat.JPEG, 50, baos)
//                val thumbData = baos.toByteArray()
//
//                val currentUid = currentUser!!.uid
//                mRootRef = FirebaseStorage.getInstance().reference
//
//                val filePath = mRootRef.child("profile_images").child("$currentUid.jpg")
//                val thumb_filePath = mRootRef.child("profile_images").child("thumbs").child("$currentUid.jpg")
//
//
//                filePath.putFile(imageUri!!).addOnSuccessListener {
//                    filePath.downloadUrl.addOnSuccessListener { image_uri ->
//                        val uploadTask = thumb_filePath.putBytes(thumbData)
//                        uploadTask.addOnCompleteListener { thumb_task ->
//                            if (thumb_task.isSuccessful) {
//
//                                thumb_filePath.downloadUrl.addOnSuccessListener { thumbs_uri ->
//                                    userMap["image"] = image_uri.toString()
//                                    userMap["thumbImage"] = thumbs_uri.toString()
//
//                                    reg_progressBar.visibility = INVISIBLE
//                                    Toast.makeText(this, "Successfully uploaded", Toast.LENGTH_LONG).show()
//                                    val glide_image:Any = when(thumbs_uri.toString()){"default","null" -> R.drawable.ic_classroom else -> thumbs_uri}
//                                    Glide.with(reg_image).load(glide_image).into(reg_image)
////                                    Picasso.get().load(thumbs_uri).placeholder(R.drawable.ic_classroom).into(reg_image)
//                                }
//                            } else {
//                                reg_progressBar.visibility = INVISIBLE
//                                Toast.makeText(this, "Failed to upload thumbnail", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//                }.addOnFailureListener { e ->
//                    reg_progressBar.visibility = INVISIBLE
//                    Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//
//        } else {
//            Toast.makeText(this, "Image can not be retrieve", Toast.LENGTH_LONG).show()
        }

    }

    override fun onStop() {
        super.onStop()
        userMap.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        mUserReference.removeEventListener(imageListener)
    }

}