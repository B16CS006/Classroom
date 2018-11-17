package com.btp.me.classroom.teacher

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_request)

        currentUser = FirebaseAuth.getInstance()?.currentUser?:return

        pending_request_list.setHasFixedSize(true)
        pending_request_list.layoutManager = LinearLayoutManager(this)

        val pendingRequestList = ArrayList<ArrayList<String>>()

        val pendingRequestAdapter = object :RecyclerView.Adapter<PendingRequestViewHolder>(){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingRequestViewHolder {
                Log.d(TAG , "OnCreate")
                return PendingRequestViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.single_pending_request,parent, false))
            }

            override fun getItemCount() = pendingRequestList.size

            override fun onBindViewHolder(holder: PendingRequestViewHolder, position: Int) {
                Log.d(TAG , "On Bind")
                holder.bind(pendingRequestList[position][2], pendingRequestList[position][1])

                holder.acceptButton.setOnClickListener {
                    holder.acceptButton.isEnabled = false
                    holder.rejectButton.isEnabled = false
                    mRootRef.child("Join-Class-Request/$classId/${pendingRequestList[position][2]}/request").setValue("accept").addOnSuccessListener {
                        Log.d(TAG,"Member is added")
                        Toast.makeText(this@PendingRequestActivity,"Member is added",Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener{exception ->
                        Log.d(TAG, "Error : ${exception.message}")
                        Toast.makeText(this@PendingRequestActivity,"Error : ${exception.message}",Toast.LENGTH_SHORT).show()
                    }
                }
                holder.rejectButton.setOnClickListener {
                    holder.acceptButton.isEnabled = false
                    holder.rejectButton.isEnabled = false
                    mRootRef.child("Join-Class-Request/$classId/${pendingRequestList[position][2]}/request").setValue("reject").addOnSuccessListener {
                        Log.d(TAG,"Member is added")
                        Toast.makeText(this@PendingRequestActivity,"Member is added",Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener{exception ->
                        Log.d(TAG, "Error : ${exception.message}")
                        Toast.makeText(this@PendingRequestActivity,"Error : ${exception.message}",Toast.LENGTH_SHORT).show()
                    }
                }
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
                    array.add(user.key.toString())

                    Log.d(TAG,"AS : ${user.child("as").value.toString()}")
                    Log.d(TAG,"roll : ${user.child("rollNumber").value.toString()}")
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

    /*
    private fun acceptRejectOnClick(request: String) {
        holder.acceptButton.isEnabled = false
        holder.rejectButton.isEnabled = false
        mRootRef.child("Join-Class-Request/$classId/${pendingRequestList[position][2]}/request").setValue("accept").addOnSuccessListener {
            Toast.makeText(this@PendingRequestActivity,"Member is added",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{exception ->
            Toast.makeText(this@PendingRequestActivity,"Error : ${exception.message}",Toast.LENGTH_SHORT).show()
        }
    }*/

    private class PendingRequestViewHolder(val view: View) :RecyclerView.ViewHolder(view){

        val acceptButton :Button = view.single_pending_request_accept_button
        val rejectButton :Button = view.single_pending_request_reject_button

        fun bind(name: String, rollNumber: String){
            setName(name)
            setRollNumber(rollNumber)
        }
        private fun setName(name:String){
            view.single_pending_request_name.text = name
        }
        private fun setRollNumber(rollNumber:String){
            view.single_pending_request_roll_number.text = rollNumber
        }
    }

    companion object {
        private const val TAG = "Pending Request"
    }
}