package com.btp.me.classroom

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.util.Log
import android.widget.EditText
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.slide.MyUploadingService
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

        class_profile_name.setOnClickListener{
            val editText = EditText(this)
            with(editText) {
                hint = "Class Name"
                setEms(10)
                maxEms = 10
                inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
            }
            getDialogBox(editText, 0)
        }

        class_profile_status.setOnClickListener{
            val editText = EditText(this)
            with(editText){
                hint = "Class Description"
                inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            }
            getDialogBox(editText, 1)
        }

        class_profile_image.setOnClickListener{
            val gallaryIntent = Intent()
            gallaryIntent.type = "image/*"
            gallaryIntent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(gallaryIntent, "Select Image"), 1)
        }
    }

    override fun onStart() {
        super.onStart()
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

                Glide.with(applicationContext).load(when(image){
                    "null","default","" -> R.drawable.ic_classroom
                    else -> image
                }).into(class_profile_image)
            }
        })
    }

    private fun getDialogBox(editText: EditText, type:Int){

        //0 -> className change
        //1 -> classStatus change

        val alertDialog = AlertDialog.Builder(this)

        with(alertDialog){
            setTitle(when(type){
                0-> "Enter Class Name"
                else -> "Enter Class Description"
            })
            setView(editText)
            alertDialog.setPositiveButton("Change") { _: DialogInterface, _: Int -> }
            alertDialog.setNegativeButton("Cancel"){_: DialogInterface,_: Int -> }
        }

        val dialog = alertDialog.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val text = editText.text.toString()
            if(text.isNotEmpty()){
                when(type){
                    0-> updateClassName(text)
                    else-> updateClassStatus(text)
                }
                dialog.dismiss()
            }else{
                editText.error = "Can't be Empty"
            }
        }

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener{
            dialog.cancel()
        }
    }

    private fun updateClassName(name: String) {
        mRootRef.child("Classroom/$classId/name").setValue(name).addOnCompleteListener { task->
            Log.d(TAG, "Name : ${task.isSuccessful}")
        }
    }

    private fun updateClassStatus(status:String){
        mRootRef.child("Classroom/$classId/status").setValue(status).addOnCompleteListener { task->
            Log.d(TAG, "Name : ${task.isSuccessful}")
        }
    }

    private fun sendToHomepage() {
        val homeIntent = Intent(this, HomepageActivity::class.java)
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(Intent(homeIntent))
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            if (requestCode == 1) {
                upload(data.data!!)
            }
        }
    }

    private fun upload(uri: Uri) {
        val data = """{"image": ""}"""
        Log.d("chetan", "uploading Uri : $uri")
        val uploadingIntent = Intent(this, MyUploadingService::class.java)

        uploadingIntent.putExtra("fileUri", uri)
        uploadingIntent.putExtra("storagePath", "ClassProfile/$classId")
        uploadingIntent.putExtra("databasePath", "Classroom/$classId")
        uploadingIntent.putExtra("data", data)
        uploadingIntent.putExtra("link", "image")

        uploadingIntent.action = MyUploadingService.ACTION_UPLOAD
        startService(uploadingIntent)
                ?: Log.d("chetan", "At this this no activity is running")
    }

    companion object {
        private const val TAG = "Class_Profile_Activity"
    }
}
