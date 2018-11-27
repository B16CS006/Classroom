package com.btp.me.classroom

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_homepage_activity.*

class HomepageActivity : AppCompatActivity() {

    private lateinit var animationDrawable: AnimationDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage_activity)

        animationDrawable = home_main.background as AnimationDrawable

//        window.decorView.setBackgroundColor(R.drawable.animated_homepage_background)

//        animationDrawable = window.decorView.background as AnimationDrawable

        animationDrawable.setEnterFadeDuration(5000)
        animationDrawable.setExitFadeDuration(2000)

        home_start.setOnLongClickListener {
            home_stop.visibility = View.VISIBLE
            return@setOnLongClickListener true
        }

        home_stop.setOnLongClickListener {
            home_stop.visibility = View.INVISIBLE
            return@setOnLongClickListener true
        }

        home_start.setOnClickListener {
            val regIndent = Intent(this, PhoneAuthenticationActivity::class.java)
            startActivity(regIndent)
        }

        home_stop.setOnClickListener {
            val regIndent = Intent(this, PhoneAuthActivity::class.java)
            startActivity(regIndent)
        }
    }

    override fun onResume() {
        super.onResume()
        animationDrawable.start()
    }

    override fun onPause() {
        super.onPause()
        animationDrawable.stop()
    }
}
