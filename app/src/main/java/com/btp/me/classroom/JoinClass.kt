package com.btp.me.classroom

import android.content.ComponentCallbacks2
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_join_class.*

class JoinClass : AppCompatActivity() {

//    private val type = arrayOf("Student","Teacher")

    private val mRootRef = FirebaseDatabase.getInstance().reference
    private var mCurrentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_class)

        title = "Join Class"

        mCurrentUser = FirebaseAuth.getInstance().currentUser

        if(mCurrentUser == null){
            finish()
        }

////        For Selecting the type to join the new class
//        val arrayAdapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item)
//        join_class_type.adapter = arrayAdapter


        join_class_button.setOnClickListener{
            val classCode = extractDataFromView()?: return@setOnClickListener

            mRootRef.child("Classroom/$classCode/members").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Log.d("chetan","Join Class : ${p0.message}")
                    Toast.makeText(this@JoinClass,"Check you Internet Connection",Toast.LENGTH_LONG).show()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value == null){
                        Log.d("chetan","Invalid class code")
                        Toast.makeText(this@JoinClass,"Invalid Class Code",Toast.LENGTH_LONG).show()
                        return
                    }else if(dataSnapshot.hasChild(mCurrentUser!!.uid)){
                        Log.d("chetan","You are already a member of this class")
                        Toast.makeText(this@JoinClass,"You are already a member of this class",Toast.LENGTH_LONG).show()
                    }else{
                        mRootRef.child("Join-Class-Request/${mCurrentUser!!.uid}/$classCode").setValue("student").addOnSuccessListener {
                            Log.d("chetan","Request is sended")
                            Toast.makeText(this@JoinClass,"Request is sended", Toast.LENGTH_LONG).show()
                            finish()
                        }.addOnFailureListener{exception ->
                            Log.d("chetan","Error while joining the class : ${exception.message}")
                            Toast.makeText(this@JoinClass,"Please check your Internet Connectivity", Toast.LENGTH_LONG).show()
                        }
                    }
                }

            })

        }
    }

    private fun extractDataFromView(): String? {
        val classCode = join_class_code.text.toString()
        Log.d("chetan","Class Code : $classCode")
        if(classCode != ""){
            return classCode
        }

        Toast.makeText(this,"Field can't be empty",Toast.LENGTH_LONG).show()
        Log.d(TAG,"In Join Class Activity Field can't be empty")
        return null
    }

    companion object {
        private const val TAG = "chetan"
    }
}
