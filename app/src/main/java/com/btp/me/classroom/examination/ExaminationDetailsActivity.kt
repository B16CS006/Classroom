package com.btp.me.classroom.examination

import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.btp.me.classroom.Class.StudentAssignmentDetails
import com.btp.me.classroom.HomepageActivity
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_examination_details.*
import kotlinx.android.synthetic.main.single_students_marks_assignment_details.view.*

class ExaminationDetailsActivity : AppCompatActivity() {

    private var examId = ""
    private var maximumMarks = "0"
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

        Log.d(TAG, "Exam Id : $examId")

        mRootRef.child("Examination/$classId/$examId").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Error : ${p0.message}")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                maximumMarks = dataSnapshot.child("maxMarks").value.toString()
                examination_details_max_marks.text = maximumMarks
                examination_details_title.text = dataSnapshot.child("title").value?.toString()
                examination_details_description.text = dataSnapshot.child("description").value?.toString()

                mRootRef.child("Classroom/$classId/members").addValueEventListener(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        Log.d(TAG, "Error : ${p0.message}")
                    }

                    override fun onDataChange(studentDataSnapshot: DataSnapshot) {
                        val type = studentDataSnapshot.child("${currentUser!!.uid}/as").value.toString()

                        Log.d(TAG, "type : $studentDataSnapshot")

                        if(type == "teacher"){
                            examination_details_marks.visibility = View.GONE
                            val markList = ArrayList<StudentAssignmentDetails>()

                            for (student in studentDataSnapshot.children){
                                Log.d(TAG, "student : $student")
                                if (student.child("as").value.toString() != "student") {
                                    continue
                                }
                                val id = student.key.toString()
                                val studentAssignmentDetails = StudentAssignmentDetails(
                                        marks = dataSnapshot.child("marks/$id/marks").value.toString(),
                                        name = student.child("name").value.toString(),
                                        rollNumber = student.child("rollNumber").value.toString(),
                                        userId = student.key.toString()
                                )

                                markList.add(studentAssignmentDetails)
                            }
                            showStudentMarks(markList)
                        }else if(type == "student"){
                            var myMarks = dataSnapshot.child("marks/${currentUser!!.uid}/marks").value.toString()
                            if (myMarks == "null"){
                                myMarks = "0"
                            }
                            examination_details_marks.text = "Marks Obtained : $myMarks"
                            examination_details_marks.visibility = View.VISIBLE
                            examination_details_marks_linear_layout.visibility = View.GONE
                        }
                    }

                })
            }

        })


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun showStudentMarks(markList: ArrayList<StudentAssignmentDetails>) {
        Log.d(TAG, "showStudentmarks ${markList.size}")
        examination_details_student_list.setHasFixedSize(true)
        examination_details_student_list.layoutManager = LinearLayoutManager(this)
        examination_details_marks_linear_layout.visibility = View.VISIBLE

        val studentMarkAdapter = object : RecyclerView.Adapter<StudentExamDetailsViewHolder>(){
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): StudentExamDetailsViewHolder {
                return StudentExamDetailsViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.single_students_marks_assignment_details, p0, false))
            }

            override fun getItemCount() = markList.size

            override fun onBindViewHolder(p0: StudentExamDetailsViewHolder, p1: Int) {
                p0.bind(markList[p1])
                p0.view.setOnClickListener {
                    getDialogBox(markList[p1].userId)
                }
            }
        }
        examination_details_student_list.adapter = studentMarkAdapter
    }

    private fun getDialogBox(userId:String?){
        Log.d(TAG, "Examination/$classId/$examId/marks/$userId/marks")
        if (userId == null)
            return

        val editText = EditText(this)
        with(editText) {
            hint = "Marks"
            setEms(5)
            maxEms = 10
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        val alertDialog = AlertDialog.Builder(this)

        with(alertDialog){
            setTitle("Enter Marks")
            setView(editText)
            alertDialog.setPositiveButton("Change") { _: DialogInterface, _: Int -> }
            alertDialog.setNegativeButton("Cancel"){ _: DialogInterface, _: Int -> }
        }

        val dialog = alertDialog.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { _ ->
            val text = editText.text.toString()
            when {
                text.isEmpty() -> editText.error = "Can't be Empty"

                text.toLong() <= maximumMarks.toLong() -> {
                    mRootRef.child("Examination/$classId/$examId/marks/$userId/marks").setValue(text).addOnCompleteListener { task->
                        Log.d(TAG, "Update marks ${task.isSuccessful}")
                    }
                    dialog.dismiss()
                }

                else -> editText.error = "should be less than to Maximum Marks"
            }
        }

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener{
            dialog.cancel()
        }
    }

    private fun sendToHomepage() {
        val intent = Intent(this, HomepageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
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
            view.single_student_marks_assignment_details_download_button.visibility = View.INVISIBLE
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
