package com.btp.me.classroom

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_homepage_activity.*

class HomepageActivity : AppCompatActivity() {

//    private var sign_up_btn: Button? = null
//    private var sign_in_btn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage_activity)

//        sign_up_btn = findViewById<View>(R.id.reg_sign_up_btn) as Button
//        sign_in_btn = findViewById<View>(R.id.reg_sign_in_btn) as Button

        home_start.setOnClickListener {
            val regIndent = Intent(this, PhoneAuthActivity::class.java)
            startActivity(regIndent)
        }
    }
}
