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
import com.btp.me.classroom.Class.Assignment
import com.btp.me.classroom.MainActivity
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_examination.*
import kotlinx.android.synthetic.main.single_assignment_layout.view.*

class ExaminationActivity : AppCompatActivity() {

    private val currentUser by lazy { FirebaseAuth.getInstance().currentUser }
    private val mRootRef = FirebaseDatabase.getInstance().reference

    private var isTeacher = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_examination)

        initialize()

        val examList = ArrayList<Assignment>()

        val examAdapter = object : RecyclerView.Adapter<ExamViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
                return ExamViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.single_assignment_layout, parent, false))
            }

            override fun getItemCount() = examList.size

            override fun onBindViewHolder(holder: ExamViewHolder, p: Int) {
                holder.bind(examList[p])
                holder.view.setOnClickListener {
                    sendToExamDetailActivity(examList[p].uploadingDate)
                }
            }

        }

        mRootRef.child("Examination/$classId").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Examination : ${p0.message}")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                examList.clear()

                for (exam in dataSnapshot.children) {
                    examList.add(Assignment(
                            title = exam.child("title").value.toString(),
                            description = exam.child("description").value.toString(),
                            maxMarks = exam.child("maxMarks").value.toString(),
                            uploadingDate = exam.key.toString()
                    ))
                }

                if (examList.size == 0) {
                    examination_list.visibility = View.GONE
                    examination_empty.visibility = View.VISIBLE
                } else {
                    examination_list.adapter = examAdapter
                    examination_list.visibility = View.VISIBLE
                    examination_empty.visibility = View.GONE
                }
            }

        })
    }

    private fun sendToExamDetailActivity(examId: String?) {
        if (examId == null) {
            return
        }

        val intent = Intent(this,ExaminationDetailsActivity::class.java)
        intent.putExtra("examId",examId)
        startActivity(intent)

    }

    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return super.onNavigateUp()
    }

    private fun initialize() {
        title = "Examination"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        examination_list.setHasFixedSize(true)
        examination_list.layoutManager = LinearLayoutManager(this)

        mRootRef.child("Classroom/$classId/members/${currentUser!!.uid}/as").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Examination Error : ${p0.message}")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                when (dataSnapshot.exists() && dataSnapshot.value.toString() == "teacher") {
                    true -> {
                        isTeacher = true
                        examination_create_button.show()
                        examination_create_button.setOnClickListener {
                            sendToAssignmentUploadActivity()
                        }
                    }
                    else -> {
                        isTeacher = false
                        examination_create_button.hide()
                    }
                }
            }
        })
    }

    private fun sendToAssignmentUploadActivity() {
        if (classId == "null") {
            sendToMainActivity()
            return
        }

        startActivity(Intent(this, AssignmentUploadActivity::class.java))
    }

    private fun sendToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    class ExamViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(exam: Assignment) {
            setTitle(exam.title)
            setDescription(exam.description)
        }

        private fun setTitle(title: String?) {
            view.single_assignment_title.text = title
        }

        private fun setDescription(description: String?) {
            view.single_assignment_description.text = description
        }
    }

    companion object {
        private const val TAG = "Examination_Activity"
    }
}
