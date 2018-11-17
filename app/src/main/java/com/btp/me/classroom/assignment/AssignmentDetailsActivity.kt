package com.btp.me.classroom.assignment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.btp.me.classroom.Class.FileBuilder.Companion.createFile
import com.btp.me.classroom.IntentResult
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.R
import com.btp.me.classroom.slide.MyDownloadingService
import com.btp.me.classroom.slide.MyUploadingService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_assignment_details.*
import kotlinx.android.synthetic.main.single_students_marks_assignment_details.view.*
import java.io.File
import java.io.IOException

private lateinit var mCurrentUser:FirebaseUser
private val mRootRef = FirebaseDatabase.getInstance().reference
private const val REQUEST_CODE_ADD = 0

private var fileUri: Uri? = null
private var assignment:String = ""

class AssignmentDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignment_details)

        mCurrentUser = FirebaseAuth.getInstance()?.currentUser?: return
        assignment = intent.getStringExtra("assignment")

        setAssignmentDetails()
    }

    private fun setAssignmentDetails(){
        mRootRef.child("Classroom/$classId/Assignment/$assignment").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@AssignmentDetailsActivity,"Error : ${p0.message}",Toast.LENGTH_SHORT).show()
                Log.d(TAG,"Error : ${p0.message}")
            }

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

                    @SuppressLint("SetTextI18n")
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val type = dataSnapshot.child("${mCurrentUser.uid}/as").value.toString()

                        if(type == "teacher"){
                            assignment_details_marks.visibility = View.GONE

                            val marksList = ArrayList<ArrayList<String>>()

                            for(member in database.child("marks").children){
                                if(dataSnapshot.child("${member.key.toString()}/as").value.toString() != "student" ){
                                    continue
                                }
                                val list = ArrayList<String>()
                                list.add(member.child("name").value.toString())
                                list.add(member.child("marks").value.toString())
                                list.add(member.child("link").value.toString())
                                list.add(member.child("state").value.toString())
                                list.add(member.key.toString())

//                              Log.d(TAG,"name : ${member.child("name").value.toString()}")
//                              Log.d(TAG, "marks : ${member.child("marks").value.toString()}")

                                marksList.add(list)
                            }
                            if(marksList.size == 0){
                                assignment_details_marks_linear_layout.visibility = View.GONE
                                assignment_details_marks_empty.visibility = View.VISIBLE
                            }else{
                                assignment_details_marks_linear_layout.visibility = View.VISIBLE
                                assignment_details_marks_empty.visibility = View.GONE
                                showStudentMarks(marksList)
                            }
                        }else if(type == "student"){
                            var myMarks = database.child("marks/${mCurrentUser.uid}/marks").value.toString()
                            if(myMarks == "null")
                                myMarks = "0"

                            assignment_details_marks.text = "Marks Obtained : $myMarks"

                            assignment_details_marks_linear_layout.visibility = View.GONE
                            assignment_details_marks.visibility = View.VISIBLE
                            setSubmitButton()
                        }
                    }
                })
            }
        })
    }

    private fun showStudentMarks(marksList: ArrayList<ArrayList<String>>) {

        assignment_details_marks_recycler_view.setHasFixedSize(true)
        assignment_details_marks_recycler_view.layoutManager = LinearLayoutManager(this)

        val studentMarksAdapter = object: RecyclerView.Adapter<StudentMarksViewHolder>(){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentMarksViewHolder {
                Log.d(TAG,"chetan : 1")
                return StudentMarksViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.single_students_marks_assignment_details, parent, false))
            }

            override fun getItemCount() = marksList.size

            override fun onBindViewHolder(holder: StudentMarksViewHolder, position: Int) {
                holder.bind(marksList[position][0],marksList[position][1])

                with(holder.download){
                    if(marksList[position][2] != "null"){
                        if(marksList[position][3] == "submit"){
                            visibility = View.VISIBLE
                            isClickable = true

                            setOnClickListener {
                                Log.d(TAG, "You have clicked ${marksList[position][0]}")
                                try {
                                    val fileName: File = createFile(marksList[position][0]) ?: return@setOnClickListener
                                    val fileUrl = marksList[position][2]

                                    val downloadIntent = Intent(this@AssignmentDetailsActivity, MyDownloadingService::class.java)
                                    downloadIntent.putExtra(MyDownloadingService.EXTRA_FILE_PATH, fileName)
                                    downloadIntent.putExtra(MyDownloadingService.EXTRA_DOWNLOAD_PATH, fileUrl)
                                    downloadIntent.action = MyDownloadingService.ACTION_DOWNLOAD
                                    startService(downloadIntent)?: throw error("Can't download as No activity is running")
                                } catch (error: IOException) {
                                    Log.d(TAG, "Error while making folder ${error.message}")
                                    error.printStackTrace()
                                }
                            }

                        } else{
                            setImageResource(R.drawable.ic_cross_red_24dp)
                            visibility = View.VISIBLE
                            isClickable = false
                        }
                    } else{
                        visibility = View.INVISIBLE
                        isClickable = false
                    }
                }
            }
        }
        assignment_details_marks_recycler_view.adapter = studentMarksAdapter
    }

    private fun setSubmitButton() {

        mRootRef.child("Classroom/$classId/Assignment/$assignment/marks/${mCurrentUser.uid}/state").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@AssignmentDetailsActivity,"Error : ${p0.message}",Toast.LENGTH_SHORT).show()
                Log.d(TAG,"Error : ${p0.message}")
            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val state = dataSnapshot.value.toString()

                if (state.toLowerCase() == "submit"){
                    assignment_details_submit_button.text = "unsubmit"
                    assignment_details_add_button.visibility = View.GONE
                }else{
                    assignment_details_submit_button.text = "mark as done"
                    assignment_details_add_button.visibility = View.VISIBLE
                }
                assignment_details_linear_layout_submit.visibility = View.VISIBLE
            }
        })

        assignment_details_add_button.setOnClickListener{
            startActivityForResult(Intent.createChooser(IntentResult.forAll(), "Choose File"), REQUEST_CODE_ADD)
        }

        assignment_details_submit_button.setOnClickListener{
            onClickSubmitButton()
        }
    }

    private fun onClickSubmitButton() {

        val map = HashMap<String,String?>()

        when(assignment_details_submit_button.text.toString().toLowerCase()){
            "mark as done"->{
                map["name"] = mCurrentUser.displayName
                map["state"] = "submit"
                map["link"] = null
                map["marks"] = null
            }
            "turn in"-> {
                map["name"] = mCurrentUser.displayName
                map["state"] = "submit"
                map["link"] = fileUri.toString()
                map["marks"] = null
                if(fileUri != null) {
                    upload(fileUri!!, map)
                    Toast.makeText(this, "Check Notification for Result", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "No File Found", Toast.LENGTH_SHORT).show()
                }
                return
            }
            "unsubmit" -> {
                map["state"] = "unsubmit"
                map["marks"] = null
                map["link"] = null
            }
            else -> {
                Log.d(TAG,"Invalid Text on Submit Button")
                return
            }
        }
        mRootRef.child("Classroom/$classId/Assignment/$assignment/marks/${mCurrentUser.uid}").updateChildren(map.toMap()).addOnSuccessListener {
            Toast.makeText(this,"Assignment Uploaded Successfully", Toast.LENGTH_SHORT).show()
            Log.d(TAG,"Assignment is successfully uploaded")
        }.addOnFailureListener{exception ->
            Toast.makeText(this,"Error : ${exception.message}", Toast.LENGTH_SHORT).show()
            Log.d(TAG,"Error : ${exception.message}")
        }

    }

    /*
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
            assignment_details_marks_table_layout.addView(row)
        }

    }*/

    private fun upload(uri: Uri, map: HashMap<String,String?>) {
        Log.d(TAG, "uploading Uri : $uri")

        val data = Gson().toJson(map).toString()

        val uploadingIntent = Intent(this, MyUploadingService::class.java)

        val userId = mCurrentUser.uid

        uploadingIntent.putExtra("fileUri", uri)
        uploadingIntent.putExtra("storagePath","Assignment/$classId/$assignment/$userId")
        uploadingIntent.putExtra("databasePath","Classroom/$classId/Assignment/$assignment/marks/$userId")
        uploadingIntent.putExtra("data",data)

        uploadingIntent.action = MyUploadingService.ACTION_UPLOAD
        startService(uploadingIntent)
                ?: Log.d(TAG, "At this this no activity is running")
    }


    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_ADD && data != null && data.data != null){
            fileUri = data.data
            assignment_details_submit_button.text = "Turn In"
        }else{
            Toast.makeText(this,"Can't Retrieve",Toast.LENGTH_SHORT).show()
        }
    }

    private class StudentMarksViewHolder(val view:View):RecyclerView.ViewHolder(view){

        val download:ImageButton = view.single_student_marks_assignment_details_download_button

        fun bind(name: String,marks: String){
            Log.d(TAG,"chetan : $name")
            setName(name)
            setMarks(marks)
        }

        private fun setName(name:String){
            view.single_student_marks_assignment_details_name.text = name
        }

        private fun setMarks(marks:String){
            view.single_student_marks_assignment_details_marks.text = marks
        }
    }

    companion object {
        const val TAG = "Assignment Details"
    }
}
