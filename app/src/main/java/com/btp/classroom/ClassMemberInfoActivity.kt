package com.btp.classroom

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_class_member_info.*
import kotlinx.android.synthetic.main.single_classroom_layout.*

class ClassMemberInfoActivity : AppCompatActivity() {

    private lateinit var currentUser:FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_member_info)

        if(classId == "null")
            return

        currentUser = FirebaseAuth.getInstance().currentUser?: return

        class_member_info_name.text = currentUser.displayName
        class_member_info_roll_number.text = currentUser.phoneNumber
        class_member_info_as.text = "Student"
    }
}
