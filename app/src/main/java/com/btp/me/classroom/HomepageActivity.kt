package com.btp.me.classroom

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_homepage_activity.*

class HomepageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage_activity)

        home_start.setOnClickListener {
            val regIndent = Intent(this, PhoneAuthActivity::class.java)
            startActivity(regIndent)
        }

        home_stop.setOnClickListener {
            val regIndent = Intent(this, PhoneAuthenticationActivity::class.java)
            startActivity(regIndent)
        }
    }
}
