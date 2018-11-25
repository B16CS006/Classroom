package com.btp.me.classroom.assignment

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.btp.me.classroom.Class.StudentAssignmentDetails
import com.btp.me.classroom.HomepageActivity
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_assignment_details.*
import kotlinx.android.synthetic.main.activity_examination_details.*
import kotlinx.android.synthetic.main.single_students_marks_assignment_details.view.*

class ExaminationDetailsActivity : AppCompatActivity() {

    private var examId = ""
    private val currentUser by lazy { FirebaseAuth.getInstance().currentUser }
    private val mRootRef  = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_examination_details)

        if(currentUser == null){
            sendToHomepage()
            return
        }

        initialize()

        mRootRef.child("Examination/$classId/").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Error : ${p0.message}")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                examination_details_title.text = dataSnapshot.child("title").value.toString()
                examination_details_max_marks.text = dataSnapshot.child("maxMarks").value.toString()
                examination_details_description.text = dataSnapshot.child("description").value.toString()

                mRootRef.child("Classroom/$classId/members").addValueEventListener(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        Log.d(TAG, "Error : ${p0.message}")
                    }

                    override fun onDataChange(studentDataSnapshot: DataSnapshot) {
                        val type = dataSnapshot.child("${currentUser!!.uid}/as").value.toString()

                        if(type == "teacher"){
                            examination_details_marks.visibility = View.GONE
                            val markList = ArrayList<StudentAssignmentDetails>()

                            for (student in studentDataSnapshot.children){
                                if (dataSnapshot.child("${student.key.toString()}/as").value.toString() != "student") {
                                    continue
                                }
                                val studentAssignmentDetails = StudentAssignmentDetails(
                                        marks = student.child("marks").value.toString(),
                                        name = dataSnapshot.child("${student.key.toString()}/name").value.toString(),
                                        rollNumber = dataSnapshot.child("${student.key.toString()}/rollNumber").value.toString(),
                                        userId = student.key.toString(),
                                        registeredAs = dataSnapshot.child("${student.key.toString()}/as").value.toString()
                                )

                                markList.add(studentAssignmentDetails)
                                showStudentMarks(markList)
                            }
                        }else if(type == "student"){
                            var myMarks = dataSnapshot.child("marks/${currentUser!!.uid}/marks").value.toString()
                            if (myMarks == "null"){
                                myMarks = "0"
                            }
                            examination_details_marks.text = "Marks Obtained : $myMarks"
                            assignment_details_marks.visibility = View.VISIBLE
                            assignment_details_marks_linear_layout.visibility = View.GONE
                        }
                    }

                })
            }

        })


    }

    private fun showStudentMarks(markList: ArrayList<StudentAssignmentDetails>) {
        examination_details_student_list.setHasFixedSize(true)
        examination_details_student_list.layoutManager = LinearLayoutManager(this)

        val studentMarkAdapter = object : RecyclerView.Adapter<StudentExamDetailsViewHolder>(){
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): StudentExamDetailsViewHolder {
                return StudentExamDetailsViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.single_students_marks_assignment_details, p0, false))
            }

            override fun getItemCount() = markList.size

            override fun onBindViewHolder(p0: StudentExamDetailsViewHolder, p1: Int) {
                p0.bind(markList[p1])
            }
        }

        examination_details_student_list.adapter = studentMarkAdapter
    }

    private fun sendToHomepage() {
        val intent = Intent(this, HomepageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return super.onNavigateUp()
    }

    private fun initialize() {
        title = "Examination Details"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        examId = intent.getStringExtra("examId")
    }

    private class StudentExamDetailsViewHolder(val view:View): RecyclerView.ViewHolder(view){
        fun bind(studentAssignmentDetails: StudentAssignmentDetails){
            setRollNumber(studentAssignmentDetails.rollNumber)
            setMarks(studentAssignmentDetails.marks)
        }

        private fun setMarks(marks: String?) {
            if (marks == null || marks == "null")
                view.single_student_marks_assignment_details_marks.text = "0"
            else
                view.single_student_marks_assignment_details_marks.text = marks
        }

        private fun setRollNumber(rollNumber: String?) {
            view.single_student_marks_assignment_details_name.text = rollNumber
        }
    }

    companion object {
        private const val TAG = "Examination_Details"
    }
}
