package com.btp.me.classroom.Redundent


import android.util.Log
import java.io.File
import com.google.firebase.storage.FirebaseStorage


open class Downloader {
    fun downloadFile(fileURL: String, filename: File) {

        val httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(fileURL)

        httpsReference.getFile(filename).addOnSuccessListener {
//            Toast.makeText(,"File is successfully downloaded",Toast.LENGTH_LONG).show()
            Log.d("chetan","File is successfully downloaded")
        }.addOnFailureListener{exception ->
            Log.d("chetan","Downloading Failed : ${exception.message}")
        }
    }


}