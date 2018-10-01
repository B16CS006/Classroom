package com.btp.me.classroom.Redundent

import android.app.DownloadManager
import android.app.PendingIntent.getActivity
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.btp.me.classroom.ClassHomeActivity
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.support.v4.content.ContextCompat.getSystemService
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