package com.btp.me.classroom.assignment

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_assignment_details.*

private val mCurrentUser = FirebaseAuth.getInstance().currentUser
private val mRootRef = FirebaseDatabase.getInstance().reference
private lateinit var databaseReference: DatabaseReference

class AssignmentDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignment_details)

        val assignment = "1542081216211"

        databaseReference = mRootRef.child("Classroom/$classId/Assignment/$assignment")

        assignment_details_view_result.setOnClickListener{
            Toast.makeText(this,"Hey",Toast.LENGTH_SHORT).show()
        }

        databaseReference.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(database: DataSnapshot) {
                assignment_details_title.text = database.child("title").value.toString()
                assignment_details_submission_date.text = database.child("submissionDate").value.toString()
                assignment_details_max_marks.text = database.child("maxMarks").value.toString()
                assignment_details_description.text = database.child("description").value.toString()

                val myMarks = database.child("marks/${mCurrentUser?.uid}/marks").value.toString()

                if(myMarks == "null") {
                    assignment_details_view_result.visibility = View.VISIBLE
                    assignment_details_marks.visibility = View.GONE
                }else{
                    assignment_details_view_result.visibility = View.GONE
                    assignment_details_marks.visibility = View.VISIBLE
                    assignment_details_marks.text = myMarks
                }
            }

        })


    }
}
