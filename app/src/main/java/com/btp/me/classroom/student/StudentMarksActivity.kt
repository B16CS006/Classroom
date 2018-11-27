package com.btp.me.classroom.student

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
import android.widget.Toast
import com.btp.me.classroom.HomepageActivity
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
import kotlin.math.max

class StudentMarksActivity : AppCompatActivity() {

    private val currentUser by lazy { FirebaseAuth.getInstance().currentUser }
    private val mRootRef = FirebaseDatabase.getInstance().reference

    private var isTeacher = false

    private val assignmentMarksList = ArrayList<StudentMarks>()
    private val examMarksList = ArrayList<StudentMarks>()
    private val marksList = ArrayList<StudentMarks>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_marks)

        if (currentUser == null) {
            sendToHomepage()
            return
        }

        initialize()

        val marksAdapter = object : RecyclerView.Adapter<StudentMarksViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentMarksViewHolder {
                return StudentMarksViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.single_student_marks, parent, false))
            }

            override fun getItemCount() = marksList.size

            override fun onBindViewHolder(holder: StudentMarksViewHolder, position: Int) {
                holder.bind(marksList[position])
                if (isTeacher)
                    holder.view.setOnClickListener {
                        getDialogBox(marksList[position].userId, marksList[position].assignmentExamId, marksList[position].maxMarks)
                    }
            }
        }

        student_marks_list.adapter = marksAdapter

        mRootRef.child("Assignment/$classId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Error : ${p0.message}")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                assignmentMarksList.clear()
                marksList.clear()
                for (assignmentSnapshot in dataSnapshot.children) {

                    val studentMarks = StudentMarks(
                            assignmentSnapshot.child("title").value?.toString(),
                            assignmentSnapshot.child("marks/${currentUser!!.uid}/marks").value?.toString(),
                            assignmentSnapshot.child("maxMarks").value?.toString(),
                            currentUser?.uid,
                            "Assignment/$classId/${assignmentSnapshot.key}"
                    )
                    Log.d(TAG, "student_marks max marks max marks asdf : ${studentMarks.marks}")

                    if (studentMarks.marks == null)
                        studentMarks.marks = 0.toString()

                    assignmentMarksList.add(studentMarks)
                }

                marksList.add(StudentMarks("TITLE", "MARKS", "TOTAL"))
                marksList.addAll(assignmentMarksList)
                marksList.addAll(examMarksList)
                marksAdapter.notifyDataSetChanged()
            }
        })

        mRootRef.child("Examination/$classId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Error : ${p0.message}")
            }

            override fun onDataChange(dataSnapshot2: DataSnapshot) {
                marksList.clear()
                examMarksList.clear()
                for (examDataSnapShot in dataSnapshot2.children) {
                    val studentMarks = StudentMarks(
                            examDataSnapShot.child("title").value?.toString(),
                            examDataSnapShot.child("marks/${currentUser!!.uid}/marks").value?.toString(),
                            examDataSnapShot.child("maxMarks").value?.toString(),
                            currentUser?.uid,
                            "Examination/$classId/${examDataSnapShot.key}"
                    )

                    if (studentMarks.marks == null)
                        studentMarks.marks = 0.toString()

                    examMarksList.add(studentMarks)
                }

                marksList.add(StudentMarks("TITLE", "MARKS", "TOTAL"))
                marksList.addAll(assignmentMarksList)
                marksList.addAll(examMarksList)
                marksAdapter.notifyDataSetChanged()
            }
        })

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun initialize() {
        title = "Marks"
        getCurrentDetails()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        student_marks_list.setHasFixedSize(true)
        student_marks_list.layoutManager = LinearLayoutManager(this)

        mRootRef.child("Classroom/$classId/members/${currentUser!!.uid}/as").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "error")
            }

            override fun onDataChange(p0: DataSnapshot) {
                isTeacher = p0.exists() && p0.value.toString() == "teacher"
            }

        })
    }

    private fun getDialogBox(userId: String?, assignmentExamId: String?, MaximumMarks: String?) {
        Log.d(TAG, "Examination/$classId/$assignmentExamId/marks/$userId/marks")
        if (userId == null || assignmentExamId == null)
            return

        val maximumMarks: String = MaximumMarks ?: "0"

        val editText = EditText(this)
        with(editText) {
            hint = "Marks"
            setEms(5)
            maxEms = 10
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        val alertDialog = AlertDialog.Builder(this)

        with(alertDialog) {
            setTitle("Enter Marks")
            setView(editText)
            alertDialog.setPositiveButton("Change") { _: DialogInterface, _: Int -> }
            alertDialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int -> }
        }

        val dialog = alertDialog.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { _ ->
            val text = editText.text.toString()
            when {
                text.isEmpty() -> editText.error = "Can't be Empty"

                text.toLong() <= maximumMarks.toLong() -> {
                    mRootRef.child("$assignmentExamId/marks/$userId/marks").setValue(text).addOnCompleteListener { task ->
                        Log.d(TAG, "Update marks ${task.isSuccessful}")
                    }
                    dialog.dismiss()
                }

                else -> editText.error = "should be less than to Maximum Marks"
            }
        }

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            dialog.cancel()
        }
    }

    private fun getCurrentDetails() {
        mRootRef.child("Classroom/$classId/members/${currentUser!!.uid}").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Error while retrieve data : ${p0.message}")
                student_marks_main.visibility = View.GONE
                Toast.makeText(this@StudentMarksActivity, "Error : Something Went Wrong", Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onDataChange(p0: DataSnapshot) {
                student_marks_name.text = p0.child("name").value.toString()
                student_marks_roll_number.text = p0.child("rollNumber").value.toString()
                student_marks_main.visibility = View.VISIBLE
            }
        })
    }

    private fun sendToHomepage(): FirebaseUser? {
        val intent = Intent(this, HomepageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
        return null
    }


    private data class StudentMarks(
            var title: String? = null,
            var marks: String? = null,
            var maxMarks: String? = null,
            var userId: String? = null,
            var assignmentExamId: String? = null
    )

    private class StudentMarksViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(studentMarks: StudentMarks) {
            Log.d(TAG, "Student_marks bind : ${studentMarks.title} : ${studentMarks.marks} : ${studentMarks.maxMarks}")
            setTitle(studentMarks.title)
            setMarks(studentMarks.marks)
            setMaxMarks(studentMarks.maxMarks)
        }

        private fun setTitle(title: String?) {
            view.single_student_marks_title.text = title
        }

        private fun setMarks(marks: String?) {
            view.single_student_marks_marks.text = marks
        }

        private fun setMaxMarks(maxMarks: String?) {
            view.single_student_marks_max_marks.text = maxMarks
        }
    }


    companion object {
        private const val TAG = "student_marks_activity"
    }
}
