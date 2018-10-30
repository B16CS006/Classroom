package com.btp.me.classroom.Assignment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.btp.me.classroom.Class.Assignment
import com.btp.me.classroom.IntentResult
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.R
import com.btp.me.classroom.slide.MyUploadingService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_assignment.*
import java.util.*
import kotlin.collections.HashMap

class AssignmentUpload : AppCompatActivity() {

    private val root = FirebaseDatabase.getInstance().reference
    private var currentUser = FirebaseAuth.getInstance().currentUser

    var fileUri:Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignment)

        title = "Assignment/Examination"

        assignment_file.setOnClickListener {
            startActivityForResult(Intent.createChooser(IntentResult.forPDF(),"Select Document"),0)
        }

        assignment_submit.setOnClickListener {

//            if (assignment_title.text.isBlank() || assignment_submission_date.text.isBlank() || assignment_max_marks.text.isBlank()) {
//                Toast.makeText(this, "Fields can't be empty", Toast.LENGTH_LONG).show()
//                return@setOnClickListener
//            }

            val title = when {
                assignment_title.text.isNotBlank() -> assignment_title.text.toString()
                else -> {
                    assignment_title.error = "Field can't be empty"
                    return@setOnClickListener
                }
            }

            val description = when {
                assignment_discription.text.isNotBlank() -> assignment_discription.text.toString()
                else -> ""
            }


            val maxMarks = when {
                assignment_max_marks.text.isNotBlank() -> assignment_max_marks.text.toString()
                else -> "100" //TODO set hint to 100 also
            }

            val submissionDate = when {
                assignment_submission_date.text.isNotBlank() -> assignment_submission_date.text.toString()
                else -> ""
                //todo date is always future date
            }

            val assignment = Assignment(title, description, submissionDate, maxMarks)

            if(fileUri == null){
                val currentTime = Date()
                root.child("Classroom/$classId/Assignment/$currentTime").setValue(assignment).addOnSuccessListener {
                    Toast.makeText(this, "Assignment is successfully uploaded", Toast.LENGTH_LONG).show()
                    assignment_title.text.clear()
                    assignment_discription.text.clear()
                    assignment_submission_date.text.clear()
                    assignment_max_marks.text.clear()
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to upload Assignment\nError : ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }else{
                upload(fileUri!!,assignment)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 0 && data != null && data.data != null) {
            fileUri = data.data
        } else {
            Toast.makeText(this, "PDF can't be retrieve.", Toast.LENGTH_LONG).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun upload(uri: Uri, assignment: Assignment) {
        Log.d("chetan", "uploading Uri : ${uri.toString()}")

        val data = Gson().toJson(assignment).toString()

        val uploadingIntent = Intent(this, MyUploadingService::class.java)

//        uploadingIntent.putExtra("classId", classId)
//        uploadingIntent.putExtra("userId", currentUser!!.uid)

        val currentTime = System.currentTimeMillis().toString()
        val userId = currentUser?.uid?:return

        uploadingIntent.putExtra("fileUri", uri)
        uploadingIntent.putExtra("storagePath","Assignment/$classId/$userId/$currentTime")
        uploadingIntent.putExtra("databasePath","Classroom/$classId/assignment/$currentTime") ///todo backend : genereage all student slist showing status as not complete
        uploadingIntent.putExtra("data",data)

        uploadingIntent.action = MyUploadingService.ACTION_UPLOAD
        startService(uploadingIntent)
                ?: Log.d("chetan", "At this this no activy is running")
    }


}