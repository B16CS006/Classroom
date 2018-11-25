package com.btp.me.classroom

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.TextView
import com.btp.me.classroom.MainActivity.Companion.classId
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_class_profile.*

class ClassProfileActivity : AppCompatActivity() {

    private val mRootRef by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_profile)

        if(FirebaseAuth.getInstance().currentUser == null){
            sendToHomepage()
            return
        }

        initialize()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun initialize() {
        title = "Class Profile"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mRootRef.child("Classroom/$classId").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Error : ${p0.message}")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                class_profile_name.text = dataSnapshot.child("name").value.toString()
                class_profile_status.text = dataSnapshot.child("status").value.toString()
            }

        })
        mRootRef.child("Classroom/$classId").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Error : ${p0.message}")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val image = dataSnapshot.child("thumbImage").value?.toString() ?: dataSnapshot.child("image").value.toString()

                Glide.with(class_profile_image).load(when(image){
                    "null","default","" -> R.drawable.ic_classroom
                    else -> image
                }).into(class_profile_image)
            }
        })
    }

    private fun sendToHomepage() {
        val homeIntent = Intent(this, HomepageActivity::class.java)
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(Intent(homeIntent))
        finish()
    }

    companion object {
        private const val TAG = "Class_Profile_Activity"
    }
}
