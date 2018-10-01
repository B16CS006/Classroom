package com.btp.me.classroom.slide

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
//import android.os.Build
import android.os.IBinder
//import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.widget.Toast
import com.btp.me.classroom.ClassHomeActivity
//import com.btp.me.classroom.MainActivity
import com.btp.me.classroom.MyBaseTaskService
import com.btp.me.classroom.R
import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.quickstart.firebasestorage.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MyUploadingService : MyBaseTaskService() {

    private lateinit var storageRef: StorageReference

    override fun onCreate() {
        super.onCreate()
        storageRef = FirebaseStorage.getInstance().getReference("Slide")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand:$intent:$startId")
        if (ACTION_UPLOAD == intent.action) {
            val fileUri = intent.getParcelableExtra<Uri>("fileUri")
            val classId = intent.getStringExtra("classId")
            val userId = intent.getStringExtra(("userId"))

            // Make sure we have permission to read the data
  //          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    //        contentResolver.takePersistableUriPermission(
      //              fileUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION//)
        //    }

            uploadFromUri(fileUri,classId,userId)
        }

        return Service.START_REDELIVER_INTENT
    }

    private fun uploadFromUri(fileUri: Uri,classId:String,userId:String) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString())
        Log.d(TAG,"Uploadclass : $classId")
        Log.d(TAG,"UploadUser : $userId")

        taskStarted()
        showProgressNotification(getString(R.string.progress_uploading), 0, 0,R.drawable.ic_cloud_upload_white_24dp)

        val currentTime =System.currentTimeMillis().toString()
        val fileName = fileUri.lastPathSegment

        val fileRef = storageRef.child("$classId/$userId/$currentTime")

        fileRef.putFile(fileUri).addOnProgressListener { taskSnapshot ->
            showProgressNotification(getString(R.string.progress_uploading),
                    taskSnapshot.bytesTransferred,
                    taskSnapshot.totalByteCount,R.drawable.ic_cloud_upload_white_24dp)
        }.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }

            Log.d(TAG, "uploadFromUri: upload success")

            // Request the public download URL
            fileRef.downloadUrl
        }.addOnSuccessListener { downloadUri ->
            Log.d(TAG, "uploadFromUri: getDownloadUri success")
            updateDatabase(currentTime,fileName!!,downloadUri,classId,userId)
//            broadcastUploadFinished(downloadUri, fileUri)
            showUploadFinishedNotification(downloadUri, fileUri)
            taskCompleted()
        }.addOnFailureListener { exception ->
            Log.w(TAG, "uploadFromUri:onFailure", exception)
//            broadcastUploadFinished(null, fileUri)
            showUploadFinishedNotification(null, fileUri)
            taskCompleted()
        }
    }

    private fun updateDatabase(currentTime: String, fileName: String, downloadUri: Uri,classId:String,userId: String) {
        val map = HashMap<String,String>()
        map["title"] = fileName
        map["link"] = downloadUri.toString()
        val mRootRef = FirebaseDatabase.getInstance().reference
        mRootRef.child("Classroom/$classId/slide/$userId/$currentTime").updateChildren(map.toMap()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("chetan", "Successfully uploaded")
                Toast.makeText(this, "Successfully uploaded", Toast.LENGTH_LONG).show()
            } else {
                Log.d("chetan", "failure listener mRootRef")
                Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Broadcast finished upload (success or failure).
     * @return true if a running receiver received the broadcast.
     */
//    private fun broadcastUploadFinished(downloadUrl: Uri?, fileUri: Uri?): Boolean {
//        val success = downloadUrl != null
//
//        val action = if (success) UPLOAD_COMPLETED else UPLOAD_ERROR
//
//        val broadcast = Intent(action)
//                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
//                .putExtra(EXTRA_FILE_URI, fileUri)
//        return LocalBroadcastManager.getInstance(applicationContext)
//                .sendBroadcast(broadcast)
//    }

    /**
     * Show a notification for a finished upload.
     */
    private fun showUploadFinishedNotification(downloadUrl: Uri?, fileUri: Uri?) {
        dismissProgressNotification()

        val intent = Intent(this, ClassHomeActivity::class.java)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val success = downloadUrl != null
        val caption = if (success) getString(R.string.upload_success) else getString(R.string.upload_failure)
        showFinishedNotification(caption, intent, success)
    }

    companion object {

        private const val TAG = "MyUploadService"

        /** Intent Actions  */
        const val ACTION_UPLOAD = "action_upload"
        const val UPLOAD_COMPLETED = "upload_completed"
        const val UPLOAD_ERROR = "upload_error"

        /** Intent Extras  */
        const val EXTRA_FILE_URI = "extra_file_uri"
        const val EXTRA_DOWNLOAD_URL = "extra_download_url"

        val intentFilter: IntentFilter
            get() {
                val filter = IntentFilter()
                filter.addAction(UPLOAD_COMPLETED)
                filter.addAction(UPLOAD_ERROR)

                return filter
            }
    }

}