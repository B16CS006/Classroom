package com.btp.me.classroom.assignment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.btp.me.classroom.Class.Assignment
import com.btp.me.classroom.HomepageActivity
import com.btp.me.classroom.IntentResult
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.R
import com.btp.me.classroom.slide.MyUploadingService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_assignment_upload.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AssignmentUploadActivity : AppCompatActivity() {

    private val mRootRef = FirebaseDatabase.getInstance().reference
    private val currentUser by lazy { FirebaseAuth.getInstance().currentUser }

    private var isAssignment = "Assignment"

    private var fileUri:Uri? = null

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignment_upload)

        if (currentUser == null){
            sendToHomepage()
            return
        }

        initialize()

        assignment_upload_submit.setOnClickListener { _ ->

            val type = isAssignment

            val title = when {
                assignment_upload_title.text.isNotBlank() -> assignment_upload_title.text.toString()
                else -> {
                    assignment_upload_title.error = "Field can't be empty"
                    return@setOnClickListener
                }
            }

            val description = when {
                assignment_upload_description.text.isNotBlank() -> assignment_upload_description.text.toString()
                else -> {
                    null
                }
            }

            val maxMarks = when {
                assignment_upload_max_marks.text.isNotBlank() -> {
                    val marks = assignment_upload_max_marks.text.toString()
                    if(!marks.matches("\\d+".toRegex())){
                       assignment_upload_max_marks.error = "Accept only Number"
                        return@setOnClickListener
                    }else{
                        marks
                    }
                }
                else -> {
                    assignment_upload_max_marks.error = "Field can't be empty"
                    return@setOnClickListener
                }

            }

            val submissionDate = when {
                type == "Examination" -> null
                assignment_upload_submission_date.text.isNotBlank() ->{
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                    val date = assignment_upload_submission_date.text.toString()
                    if(!isDate(date, dateFormat)){
                        assignment_upload_submission_date.error = "DD/MM/YYYY"
                        return@setOnClickListener
                    }else if(dateFormat.parse(date).time <= System.currentTimeMillis()){
                        assignment_upload_submission_date.error = "Must be Future Date"
                        return@setOnClickListener
                    }else{
                        date
                    }
                }
                else -> {
                    assignment_upload_submission_date.error = "Field can't be empty"
                    return@setOnClickListener
                }
            }

            val assignment = Assignment(title, description, submissionDate, maxMarks = maxMarks)

            assignment_upload_title.text.clear()
            assignment_upload_description.text.clear()
            assignment_upload_submission_date.text.clear()
            assignment_upload_max_marks.text.clear()

            if(fileUri == null){
                val currentTime = System.currentTimeMillis()
                mRootRef.child("$type/$classId/$currentTime").setValue(assignment).addOnSuccessListener {
                    Toast.makeText(this, "Assignment is successfully uploaded", Toast.LENGTH_LONG).show()
                    finish()
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to upload Assignment\nError : ${exception.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }else{
                upload(fileUri!!,assignment)
                finish()
            }
        }

    }

    private fun isDate(date:String?, dateFormat: SimpleDateFormat): Boolean{
        dateFormat.isLenient = false

        try {
            dateFormat.parse(date)
        }catch (e: ParseException){
            Log.d("chetan", "Date is invalid")
            return false
        }

        Log.d("chetan", "Date is valid")

        return true
    }


    private fun initialize(){
        title = "Upload"
        supportActionBar?.setDisplayShowHomeEnabled(true)

        assignment_upload_radio.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.assignment_upload_radio_assignment -> {
                    isAssignment = "Assignment"
                    assignment_upload_submission_date.visibility = View.VISIBLE
                    assignment_upload_submission_date.visibility = View.VISIBLE
                }
                R.id.assignment_upload_radio_exam -> {
                    isAssignment = "Examination"
                    assignment_upload_submission_date.visibility = View.GONE
                    assignment_upload_submission_date.visibility = View.GONE
                }
            }
        }

        assignment_upload_file.setOnClickListener {
            startActivityForResult(Intent.createChooser(IntentResult.forPDF(),"Select Document"),0)
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
        Log.d("chetan", "uploading Uri : $uri")

        val data = Gson().toJson(assignment).toString()

        val uploadingIntent = Intent(this, MyUploadingService::class.java)

//        uploadingIntent.putExtra("classId", classId)
//        uploadingIntent.putExtra("userId", currentUser!!.uid)

        val currentTime = System.currentTimeMillis().toString()
        val userId = currentUser!!.uid

        uploadingIntent.putExtra("fileUri", uri)
        uploadingIntent.putExtra("storagePath","Assignment/$classId/$userId/$currentTime")
        uploadingIntent.putExtra("databasePath","Assignment/$classId/$currentTime") ///todo backend : generate all student slist showing status as not complete
        uploadingIntent.putExtra("data",data)

        uploadingIntent.action = MyUploadingService.ACTION_UPLOAD
        startService(uploadingIntent)
                ?: Log.d("chetan", "At this this no activy is running")
    }

    private fun sendToHomepage(): FirebaseUser? {
        val intent = Intent(this, HomepageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
        return null
    }
}