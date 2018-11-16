package com.btp.me.classroom.assignment

import android.graphics.drawable.GradientDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_assignment_details.*

private lateinit var mCurrentUser:FirebaseUser
private val mRootRef = FirebaseDatabase.getInstance().reference

class AssignmentDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignment_details)

        val assignment = intent.getStringExtra("assignment")
        mCurrentUser = FirebaseAuth.getInstance()?.currentUser?: return

//        assignment_details_view_result.setOnClickListener{
//            Toast.makeText(this,"Hey",Toast.LENGTH_SHORT).show()
//        }


        setAssignmentDetails(assignment)

//        createTable()
    }

    private fun setAssignmentDetails(assignment:String){
        mRootRef.child("Classroom/$classId/Assignment/$assignment").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@AssignmentDetailsActivity,"Error : ${p0.message}",Toast.LENGTH_SHORT).show()
                Log.d(TAG,"Error : ${p0.message}")            }

            override fun onDataChange(database: DataSnapshot) {
                assignment_details_title.text = database.child("title").value.toString()
                assignment_details_submission_date.text = database.child("submissionDate").value.toString()
                assignment_details_max_marks.text = database.child("maxMarks").value.toString()
                assignment_details_description.text = database.child("description").value.toString()


                mRootRef.child("Classroom/$classId/members").addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        Toast.makeText(this@AssignmentDetailsActivity,"Error : ${p0.message}",Toast.LENGTH_SHORT).show()
                        Log.d(TAG,"Error : ${p0.message}")
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val type = dataSnapshot.child("${mCurrentUser?.uid}/as").value.toString()

                        if(type == "teacher"){
                            assignment_details_table_layout.visibility = View.VISIBLE
                            assignment_details_marks.visibility = View.GONE

                            val marksList = ArrayList<ArrayList<String>>()

                            for(member in database.child("marks").children){
                                if(dataSnapshot.child("${member.key.toString()}/as").value.toString() != "student" ){
                                    continue
                                }
                                val list = ArrayList<String>()
                                list.add(member.child("name").value.toString())
                                list.add(member.child("marks").value.toString())
                                list.add(member.key.toString())

//                        Log.d(TAG,"name : ${member.child("name").value.toString()}")
//                        Log.d(TAG, "marks : ${member.child("marks").value.toString()}")

                                marksList.add(list)
                            }
                            createTable(marksList)
                        }else if(type == "student"){
                            val myMarks = database.child("marks/${mCurrentUser?.uid}/marks").value.toString()
//                            Log.d(TAG,"My Marks ${mCurrentUser?.uid}: $myMarks")

                            assignment_details_table_layout.visibility = View.GONE
                            assignment_details_marks.visibility = View.VISIBLE
                            assignment_details_marks.text = "Marks Obtained : $myMarks"
                        }
                    }

                })
            }

        })
    }



    private fun createTable(marksList: ArrayList<ArrayList<String>>) {
        val rows = marksList.size
        val cols = marksList[0].size -1

        Log.d(TAG, "Row : $rows, Col : $cols")

        for(i in 0 until rows){
            val row = TableRow(this)
            row.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)

            for (j in 0 until cols){
                val textView = TextView(this)
                textView.apply {
                    layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                    text = marksList[i][j]
                }
                row.addView(textView)
            }
            assignment_details_table_layout.addView(row)
        }

    }

    companion object {
        const val TAG = "AssignmentDetails"
    }
}
