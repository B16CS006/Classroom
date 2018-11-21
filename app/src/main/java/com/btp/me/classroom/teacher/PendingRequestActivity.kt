package com.btp.me.classroom.teacher

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_pending_request.*
import kotlinx.android.synthetic.main.single_pending_request.view.*

class PendingRequestActivity : AppCompatActivity() {

    private val mRootRef = FirebaseDatabase.getInstance().reference
    private lateinit var currentUser :FirebaseUser

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_request)

        title = "Pending Requests"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        currentUser = FirebaseAuth.getInstance()?.currentUser?:return

        pending_request_list.setHasFixedSize(true)
        pending_request_list.layoutManager = LinearLayoutManager(this)

        val pendingRequestList = ArrayList<ArrayList<String>>()

        val pendingRequestAdapter = object :RecyclerView.Adapter<PendingRequestViewHolder>(){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingRequestViewHolder {
                Log.d(TAG , "OnCreate")
                return PendingRequestViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.single_pending_request,parent, false),this@PendingRequestActivity)
            }

            override fun getItemCount() = pendingRequestList.size

            override fun onBindViewHolder(holder: PendingRequestViewHolder, position: Int) {
                Log.d(TAG , "On Bind")
                holder.bind(pendingRequestList[position])
            }

        }

        mRootRef.child("Join-Class-Request/$classId").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Error : ${p0.message}")
                Toast.makeText(this@PendingRequestActivity,"Error : ${p0.message}",Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG,"data : $dataSnapshot")
                pendingRequestList.clear()

                for (user in dataSnapshot.children){
                    Log.d(TAG,"User : $user")
                    if(user == null)
                        continue

                    val array =  ArrayList<String>()
                    array.add(user.child("as").value.toString())
                    array.add(user.child("rollNumber").value.toString())
                    array.add(user.child("name").value.toString())
                    array.add(user.key.toString())

                    Log.d(TAG,"AS : ${user.child("as").value.toString()}")
                    Log.d(TAG,"roll : ${user.child("rollNumber").value.toString()}")
                    Log.d(TAG,"name : ${user.child("name").value.toString()}")
                    Log.d(TAG,"uid : ${user.key.toString()}")

                    pendingRequestList.add(array)
                }

                if(pendingRequestList.size == 0){
                    Log.d(TAG , "size 0")
                    pending_request_list.visibility = View.GONE
                    pending_request_empty.visibility = View.VISIBLE
                }else{
                    Log.d(TAG , "Size ${pendingRequestList.size}")
                    pending_request_list.visibility = View.VISIBLE
                    pending_request_empty.visibility = View.GONE
                    pending_request_list.adapter = pendingRequestAdapter
                }
            }
        })


    }

    private class PendingRequestViewHolder(val view: View, val context:Context) :RecyclerView.ViewHolder(view){

        val acceptButton:Button = view.single_pending_request_accept_button
        val rejectButton: Button = view.single_pending_request_reject_button
        val nameView: TextView = view.single_pending_request_name
        val rollNumberView: TextView = view.single_pending_request_roll_number


        fun bind(list:ArrayList<String>){

            //as, rollNumber, name, uid

            bind(list[2],list[1])
            onClick(list[3])
        }

        fun bind(name: String, rollNumber: String){
            setName(name)
            setRollNumber(rollNumber)
        }

        private fun setName(name: String){
            nameView.text = name

//            view.visibility = View.GONE
//
//            FirebaseDatabase.getInstance().getReference("Users/$name/name").addValueEventListener(object : ValueEventListener{
//                override fun onCancelled(p0: DatabaseError) {
//                    Log.d(TAG,"Error : ${p0.message}")
//                    nameView.text = "$name -Be aware"
//                    view.visibility = View.VISIBLE
//                }
//
//                override fun onDataChange(data: DataSnapshot) {
//                    view.visibility = View.VISIBLE
//                    nameView.text = when(data.value){ null -> name; else -> data.value.toString() }
//                }
//
//            })
        }

        private fun setRollNumber(rollNumber:String){
            if (rollNumber == "null"){
                rollNumberView.visibility = View.GONE
                view.single_pending_request_as.text = "Teacher"
            }else {
                rollNumberView.text = rollNumber
            }
        }

        private fun onClick(userId: String){
            view.single_pending_request_accept_button.setOnClickListener{ acceptRejectOnClick(userId, "accept") }
            view.single_pending_request_reject_button.setOnClickListener{ acceptRejectOnClick(userId, "reject") }
        }

        private fun acceptRejectOnClick(userId:String,request: String) {
            acceptButton.isEnabled = false
            rejectButton.isEnabled = false

            FirebaseDatabase.getInstance().getReference("Join-Class-Request/$classId/$userId/request").setValue(request).addOnSuccessListener {
                Toast.makeText(context,"Request $request Successfully",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{exception ->
                Toast.makeText(context,"Error : ${exception.message}",Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TAG = "Pending Request"
    }
}