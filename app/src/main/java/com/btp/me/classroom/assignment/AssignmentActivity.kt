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
import android.widget.Toast
import com.btp.me.classroom.Class.Assignment
import com.btp.me.classroom.MainActivity
import com.btp.me.classroom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.btp.me.classroom.MainActivity.Companion.classId
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_assignment.*
import kotlinx.android.synthetic.main.single_assignment_layout.view.*

class AssignmentActivity : AppCompatActivity() {

    private lateinit var currentUser:FirebaseUser
    private lateinit var databaseReference:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignment)

        currentUser = FirebaseAuth.getInstance()?.currentUser?:return

        if(classId == "null"){
            sendToMainActivity()
            return
        }

        title = "Assignment"

        assignment_upload_button.setOnClickListener {
            sendToAssignmentUploadActivity()
        }

        assignment_list.setHasFixedSize(true)
        assignment_list.layoutManager = LinearLayoutManager(this)





    }

    override fun onStart() {
        super.onStart()

        databaseReference = FirebaseDatabase.getInstance().getReference("Classroom/$classId/Assignment")

        Toast.makeText(this,"hey $classId",Toast.LENGTH_SHORT).show()


        val options = FirebaseRecyclerOptions.Builder<Assignment>()
                .setQuery(databaseReference, Assignment::class.java)
                .setLifecycleOwner(this)
                .build()

        val adapter = object :FirebaseRecyclerAdapter<Assignment,AssignmentViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
//                Log.d(TAG,"View Type: ${viewType.toString()}")

                return AssignmentViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.single_assignment_layout, parent, false))
            }

            override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int, model: Assignment) {

//                holder.bind(model)

                holder.setTitle(model.title)
                holder.setDescription(model.description)
                holder.setSubmissionDate(model.submissionDate)

                holder.view.setOnClickListener {
//                    Log.d(TAG, "Your have clicked $position")
//                    Log.d(TAG,"Ref : ${getRef(position).key.toString()}")
                    sendToAssignmentDetails(getRef(position).key.toString())
                }
            }

            override fun onDataChanged() {
                if(itemCount == 0)
                    assignment_empty.visibility = View.VISIBLE
                else
                    assignment_empty.visibility = View.GONE
            }
        }

        assignment_list.adapter = adapter
    }

    private fun sendToAssignmentDetails(assignment: String) {
        if (classId == "null") {
            sendToMainActivity()
            return
        }

        val intent = Intent(this,AssignmentDetailsActivity::class.java)
        intent.putExtra("assignment",assignment)
        startActivity(intent)
    }


    private fun sendToAssignmentUploadActivity() {
        if (classId == "null") {
            sendToMainActivity()
            return
        }

        startActivity(Intent(this,AssignmentUploadActivity::class.java))
    }

    private fun sendToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    public class AssignmentViewHolder(val view:View):RecyclerView.ViewHolder(view){
        fun bind(assignment: Assignment) {

//            Log.d(TAG,"ClassViewHolder")
            Log.d(TAG, "title : ${assignment.title}")
//            Log.d(TAG,"description : ${assignment.description}")
//            Log.d(TAG,"link : ${assignment.link}")
//            Log.d(TAG,"submission : ${assignment.submissionDate}")
//            Log.d(TAG,"maxMarks : ${assignment.maxMarks}")

            with(assignment) {
                view.single_assignment_title.text = this.title
                view.single_assignment_description.text = this.description
                view.single_assignment_submission_date.text = this.submissionDate
            }
        }

        fun setTitle(string:String){
            view.single_assignment_title.text = string
        }

        fun setDescription(string:String){
            view.single_assignment_description.text = string
        }

        fun setSubmissionDate(string:String){
            view.single_assignment_submission_date.text = string
        }
    }

    companion object {
        const val TAG = "Assignment Activity"
    }


}
