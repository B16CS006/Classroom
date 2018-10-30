package com.btp.me.classroom.Assignment

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
import com.btp.me.classroom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.btp.me.classroom.MainActivity.Companion.classId
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import kotlinx.android.synthetic.main.activity_assignment.*
import kotlinx.android.synthetic.main.single_assignment_layout.view.*

class AssignmentActivity : AppCompatActivity() {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val databaseReference = FirebaseDatabase.getInstance().getReference("Classroom/$classId/Assignment")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignment)

        assignment_upload_button.setOnClickListener {
            sendToAssignmentUploadActivity()
        }

        assignment_list.setHasFixedSize(true)
        assignment_list.layoutManager = LinearLayoutManager(this)

        val options = FirebaseRecyclerOptions.Builder<Assignment>()
                .setQuery(databaseReference, Assignment::class.java)
                .setLifecycleOwner(this)
                .build()

        val adapter = object :FirebaseRecyclerAdapter<Assignment,AssignmentViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
                Log.d(TAG,"View Type: ${viewType.toString()}")

                return  AssignmentViewHolder(LayoutInflater
                        .from(parent.context).inflate(R.layout.single_assignment_layout,parent,false))
            }

            override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int, model: Assignment) {
                if(itemCount==0){
                    assignment_empty.visibility = View.VISIBLE
                    assignment_list.visibility - View.GONE
                }else{
                    assignment_empty.visibility = View.GONE
                    assignment_list.visibility = View.VISIBLE
                }

                holder.bind(model)
                holder.view.setOnClickListener{
                    Log.d(TAG,"Your have clicked $position")
                }
            }
        }

        assignment_list.adapter = adapter

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

    class AssignmentViewHolder(val view:View):RecyclerView.ViewHolder(view){
        fun bind(assignment: Assignment){
            Log.d(TAG,"ClassViewHolder")
            with(assignment){
                view.single_assignment_title.text = assignment.title
                view.single_assignment_description.text = assignment.description
                view.single_assignment_submission_date.text = assignment.submissionDate
            }
        }
    }

    companion object {
        const val TAG = "AssignmentActivity"
    }


}
