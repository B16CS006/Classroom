package com.btp.me.classroom

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class Classroom: Application() {

    override fun onCreate() {
        super.onCreate()
        
//        var presenceRef = firebase.database().ref("disconnectmessage");
//        // Write a string when this client loses connection
//        presenceRef.onDisconnect().set("I disconnected!");
        
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

}
