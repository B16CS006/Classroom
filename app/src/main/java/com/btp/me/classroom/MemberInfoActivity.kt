package com.btp.me.classroom

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.btp.me.classroom.MainActivity.Companion.classId
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_class_member_info.*

class MemberInfoActivity : AppCompatActivity() {

    private val mRootRef = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_member_info)

        if(classId == "null") {
            finish()
            return
        }


        val memberUID = intent.getStringExtra("userId") ?: return
        Log.d(TAG, "user Id : $memberUID")

        val registeredAs = intent.getStringExtra("registeredAs") ?: return
        Log.d(TAG, "Registered As : $registeredAs")

        val name = intent.getStringExtra("name") ?: return

        mRootRef.child("Users/$memberUID").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Member_info Error : ${p0.message}")
                Toast.makeText(this@MemberInfoActivity,"Error : ${p0.message}",Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "error : $dataSnapshot")

                if (dataSnapshot.value == null) {
                    Toast.makeText(this@MemberInfoActivity, "No such member in Database", Toast.LENGTH_SHORT).show()
                    return
                }

                class_member_main.visibility = View.VISIBLE
                class_member_info_name.text = name
                class_member_info_phone_number.text =  dataSnapshot.child("phone").value?.toString() ?: "Error"
                class_member_info_as.text = registeredAs.toUpperCase()

                if(registeredAs == "student"){
                    class_member_info_roll_number.text = intent.getStringExtra("rollNumber")
                    class_member_info_roll_number_linear_layout.visibility = View.VISIBLE
                }else{
                    class_member_info_roll_number_linear_layout.visibility = View.GONE
                }

                val image = dataSnapshot.child("thumbImage").value.toString()
                val glideImage:Any = when(image) {"default","null" -> R.drawable.ic_default_profile else -> image}
                Glide.with(applicationContext).load(glideImage).into(class_member_info_image)
            }
        })
    }

    companion object {
        private const val TAG = "Member_Info_Activity"
    }
}
