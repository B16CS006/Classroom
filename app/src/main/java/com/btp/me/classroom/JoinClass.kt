package com.btp.me.classroom

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_class)

        title = "Join Class"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mCurrentUser = FirebaseAuth.getInstance().currentUser

        if(mCurrentUser == null){
            finish()
        }

////        For Selecting the type to join the new class
//        val arrayAdapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item)
//        join_class_type.adapter = arrayAdapter


        join_class_button.setOnClickListener{
            val detail = extractDataFromView()

            if(detail.invalid)
                return@setOnClickListener

            detail.name = mCurrentUser?.displayName?:"Anonymous"

            mRootRef.child("Classroom/${detail.classId}/members").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Log.d(TAG,"Join Class : ${p0.message}")
                    Toast.makeText(this@JoinClass,"Check you Internet Connection",Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    when {
                        dataSnapshot.value == null -> {
                            Log.d(TAG,"Invalid class code")
                            Toast.makeText(this@JoinClass,"Invalid Class Code",Toast.LENGTH_SHORT).show()
                            return
                        }
                        dataSnapshot.hasChild(mCurrentUser!!.uid) -> {
                            Log.d(TAG,"You are already a member of this class")
                            Toast.makeText(this@JoinClass,"You are already a member of this class",Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            val map = HashMap<String, String>()
                            map["as"] = "student"
                            map["request"] = "pending"
                            map["rollNumber"] = detail.rollNumber
                            map["name"] = detail.name
                            mRootRef.child("Join-Class-Request/${detail.classId}/${mCurrentUser?.uid}").setValue(map).addOnSuccessListener {
                                Log.d(TAG, "Request send")
                                Toast.makeText(this@JoinClass, "Request send", Toast.LENGTH_SHORT).show()
                                finish()
                            }.addOnFailureListener { exception ->
                                Log.d(TAG, "Error : ${exception.message}")
                                Toast.makeText(this@JoinClass, "Error : ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

            })

        }
    }

    private fun extractDataFromView(): Detail {
        val detail = Detail()
        if(join_class_code.text.isBlank() || join_class_roll_number.text.isBlank()) {
            detail.invalid = true
            Toast.makeText(this,"Field can't be empty",Toast.LENGTH_SHORT).show()
            return detail
        }

        detail.classId = join_class_code.text.toString()
        detail.rollNumber = join_class_roll_number.text.toString().toUpperCase()
        detail.invalid = false
        return detail
    }

    private class Detail{
        var classId:String = ""
        var rollNumber:String = ""
        var name: String = ""
        var invalid:Boolean = false
    }

    companion object {
        private const val TAG = "Join Class"
    }
}
