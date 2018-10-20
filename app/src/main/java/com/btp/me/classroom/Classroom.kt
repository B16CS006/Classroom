package com.btp.me.classroom

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class Classroom: Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

}