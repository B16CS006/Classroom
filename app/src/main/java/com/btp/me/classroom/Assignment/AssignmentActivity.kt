package com.btp.me.classroom.Assignment

import android.content.ComponentCallbacks2
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.btp.me.classroom.R
import com.google.firebase.database.FirebaseDatabase
import com.btp.me.classroom.MainActivity.Companion.classId
import kotlinx.android.synthetic.main.activity_assignment.*
import java.util.*

class AssignmentActivity : AppCompatActivity() {

    private val root = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignment)

        title = "Assignment/Examination"

        val assignment = Assignment()

        assignment_file.isEnabled = false
        assignment_file.setOnClickListener{
            //TODO get file from the mobile in form of either image or pdf
        }

        assignment_submit.setOnClickListener {

            if(assignment_title.text.isBlank() || assignment_submission_date.text.isBlank() || assignment_max_marks.text.isBlank()){
                Toast.makeText(this,"Fields can't be empty",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            assignment.title = assignment_title.text.toString()
            assignment.discription = assignment_discription.text.toString()
            assignment.file = "null"
            assignment.maxMarks = assignment_max_marks.text.toString()
            assignment.date = assignment_submission_date.text.toString()

            val currentTime = Date()
            root.child("Classroom/$classId/Assignment/$currentTime").setValue(assignment).addOnSuccessListener {
                Toast.makeText(this,"Assignment is successfully uploaded", Toast.LENGTH_LONG).show()
                assignment_title.text.clear()
                assignment_discription.text.clear()
                assignment_submission_date.text.clear()
                assignment_max_marks.text.clear()

            }.addOnFailureListener {exception ->
                Toast.makeText(this,"Failed to upload Assignment\nError : ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }

    }

    class Assignment(var title:String? = null, var discription:String? = null ,var date:String? = null ,var maxMarks:String? = null ,var file:String? = null)
}