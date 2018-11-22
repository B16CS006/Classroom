package com.btp.me.classroom.student

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_student_marks.*
import kotlinx.android.synthetic.main.single_student_marks.view.*

class StudentMarksActivity : AppCompatActivity() {

    private lateinit var currentUser:FirebaseUser
    private val mRootRef = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_marks)

        initialize()

        currentUser = FirebaseAuth.getInstance().currentUser?:return

        val studentMarksList = ArrayList<StudentMarks>()

        val marksAdapter = object :RecyclerView.Adapter<StudentMarksViewHolder>(){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentMarksViewHolder {
                return StudentMarksViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.single_student_marks, parent, false))
            }

            override fun getItemCount() = studentMarksList.size

            override fun onBindViewHolder(holder: StudentMarksViewHolder, position: Int) {
                holder.bind(studentMarksList[position])
            }

        }

        mRootRef.child("Assignment/$classId").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Error : ${p0.message}")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (assignmentSnapshot in dataSnapshot.children){

                    val studentMarks = StudentMarks()

                    studentMarks.title = assignmentSnapshot.child("title").value.toString()
                    studentMarks.maxMarks = assignmentSnapshot.child("maxMarks").value.toString()
                    val marks = assignmentSnapshot.child("marks/${currentUser.uid}/marks").value.toString()

                    if(marks=="null")
                        studentMarks.marks = 0.toString()
                    else
                        studentMarks.marks = marks

                    studentMarksList.add(studentMarks)
                }

                mRootRef.child("Examination/$classId").addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        Log.d(TAG, "Error : ${p0.message}")
                    }

                    override fun onDataChange(dataSnapshot2: DataSnapshot) {
                        for (examDataSnapShot in dataSnapshot2.children){
                            val studentMarks = StudentMarks()

                            studentMarks.title = examDataSnapShot.child("title").value.toString()
                            studentMarks.maxMarks = examDataSnapShot.child("maxMarks").value.toString()
                            val marks = examDataSnapShot.child("marks/${currentUser.uid}/marks").value.toString()

                            if(marks=="null")
                                studentMarks.marks = 0.toString()
                            else
                                studentMarks.marks = marks

                            studentMarksList.add(studentMarks)
                        }

                        if(studentMarksList.size != 0){
                            student_marks_empty_list.visibility = View.GONE
                            student_marks_list.visibility = View.VISIBLE
                            student_marks_list.adapter = marksAdapter
                        }else{
                            student_marks_empty_list.visibility = View.VISIBLE
                            student_marks_list.visibility = View.GONE
                        }
                    }

                })
            }

        })

    }

    private fun initialize() {
        title = "Marks"
        student_marks_list.setHasFixedSize(true)
        student_marks_list.layoutManager = LinearLayoutManager(this)
    }

    private class StudentMarks{
        var title:String = ""
        var marks:String = ""
        var maxMarks:String = ""
    }

    private class StudentMarksViewHolder(val view:View):RecyclerView.ViewHolder(view){
        fun bind(studentMarks: StudentMarks){
            setTitle(studentMarks.title)
            setMarks(studentMarks.marks)
            setMaxMarks(studentMarks.maxMarks)
        }

        private fun setTitle(title:String){
            view.single_student_marks_title.text = title
        }
        private fun setMarks(marks:String){
            view.single_student_marks_marks.text = marks
        }
        private fun setMaxMarks(maxMarks: String){
            view.single_student_marks_max_marks.text = maxMarks
        }
    }


    companion object {
        private const val TAG = "student_marks_activity"
    }
}
