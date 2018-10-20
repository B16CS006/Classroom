package com.btp.me.classroom.slide

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.btp.me.classroom.ClassHomeActivity
import com.btp.me.classroom.MyBaseTaskService
import com.btp.me.classroom.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MyDownloadingService : MyBaseTaskService() {

    private lateinit var storageRef: StorageReference

    override fun onCreate() {
        super.onCreate()

        // Initialize Storage
        storageRef = FirebaseStorage.getInstance().reference
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand:$intent:$startId")

        if (ACTION_DOWNLOAD == intent.action) {
            // Get the path to download from the intent
            val downloadPath = intent.getStringExtra(EXTRA_DOWNLOAD_PATH)
            Log.d("chetan","Downloading path $downloadPath")

//            val bundle = intent.getBundleExtra(EXTRA_FILE_PATH)

            val fileName = intent.extras[EXTRA_FILE_PATH] as File               // Unsafe use of a nullable receiver of type Bundle?
            Log.d("chetan","file Path $fileName")

//            val filename = intent.getStringExtra(EXTRA_FILE_PATH) as File
//            Log.d("chetan","file Paht $filename")


            downloadFromPath(downloadPath, fileName)
        }

        return Service.START_REDELIVER_INTENT
    }

    private fun downloadFromPath(downloadPath: String,filename:File) {
        Log.d(TAG, "downloadFromPath:$downloadPath")

        // Mark task started
        taskStarted()
        showProgressNotification(getString(R.string.progress_downloading), 0, 0,R.drawable.ic_cloud_download_white_24dp)


        val httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(downloadPath)

        httpsReference.getFile(filename).addOnSuccessListener {taskSnapshot->
            Log.d(TAG, "download:SUCCESS")

            // Send success broadcast with number of bytes downloaded
            broadcastDownloadFinished(downloadPath, taskSnapshot.totalByteCount)
            showDownloadFinishedNotification(downloadPath, taskSnapshot.totalByteCount.toInt())

            // Mark task completed
            taskCompleted()
        }.addOnFailureListener{exception ->
            Log.w(TAG, "download:FAILURE", exception)

            // Send failure broadcast
            broadcastDownloadFinished(downloadPath, -1)
            showDownloadFinishedNotification(downloadPath, -1)

            // Mark task completed
            taskCompleted()
        }.addOnProgressListener { taskSnapshot ->
            showProgressNotification(getString(R.string.progress_downloading),
                    taskSnapshot.bytesTransferred,
                    taskSnapshot.totalByteCount,R.drawable.ic_cloud_download_white_24dp)
        }


        // Download and get total bytes
//        storageRef.child(downloadPath).getStream { taskSnapshot, inputStream ->
//            val totalBytes = taskSnapshot.totalByteCount
//            var bytesDownloaded: Long = 0
//
//            val buffer = ByteArray(1024)
//            var size: Int = inputStream.read(buffer)
//
//            while (size != -1) {
//                bytesDownloaded += size.toLong()
//                showProgressNotification(getString(R.string.progress_downloading),
//                        bytesDownloaded, totalBytes)
//
//                size = inputStream.read(buffer)
//            }
//
//            // Close the stream at the end of the Task
//            inputStream.close()
//        }.addOnSuccessListener { taskSnapshot ->
//            Log.d(TAG, "download:SUCCESS")
//
//            // Send success broadcast with number of bytes downloaded
//            broadcastDownloadFinished(downloadPath, taskSnapshot.totalByteCount)
//            showDownloadFinishedNotification(downloadPath, taskSnapshot.totalByteCount.toInt())
//
//            // Mark task completed
//            taskCompleted()
//        }.addOnFailureListener { exception ->
//            Log.w(TAG, "download:FAILURE", exception)
//
//            // Send failure broadcast
//            broadcastDownloadFinished(downloadPath, -1)
//            showDownloadFinishedNotification(downloadPath, -1)
//
//            // Mark task completed
//            taskCompleted()
//        }
    }

    /**
     * Broadcast finished download (success or failure).
     * @return true if a running receiver received the broadcast.
     */
    private fun broadcastDownloadFinished(downloadPath: String, bytesDownloaded: Long): Boolean {
        val success = bytesDownloaded != -1L
        val action = if (success) DOWNLOAD_COMPLETED else DOWNLOAD_ERROR

        val broadcast = Intent(action)
                .putExtra(EXTRA_DOWNLOAD_PATH, downloadPath)
                .putExtra(EXTRA_BYTES_DOWNLOADED, bytesDownloaded)
        return LocalBroadcastManager.getInstance(applicationContext)
                .sendBroadcast(broadcast)
    }

    /**
     * Show a notification for a finished download.
     */
    private fun showDownloadFinishedNotification(downloadPath: String, bytesDownloaded: Int) {
        // Hide the progress notification
        dismissProgressNotification()

        // Make Intent to MainActivity
        val intent = Intent(this, ClassHomeActivity::class.java)
                .putExtra(EXTRA_DOWNLOAD_PATH, downloadPath)
                .putExtra(EXTRA_BYTES_DOWNLOADED, bytesDownloaded)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val success = bytesDownloaded != -1
        val caption = if (success) {
            getString(R.string.download_success)
        } else {
            getString(R.string.download_failure)
        }

        showFinishedNotification(caption, intent, true)
    }

    companion object {

        private val TAG = "Storage#DownloadService"

        /** Actions  */
        const val ACTION_DOWNLOAD = "action_download"
        const val DOWNLOAD_COMPLETED = "download_completed"
        const val DOWNLOAD_ERROR = "download_error"

        /** Extras  */
        const val EXTRA_DOWNLOAD_PATH = "extra_download_path"
        const val EXTRA_BYTES_DOWNLOADED = "extra_bytes_downloaded"
        const val EXTRA_FILE_PATH = "extra_file_path"


        val intentFilter: IntentFilter
            get() {
                val filter = IntentFilter()
                filter.addAction(DOWNLOAD_COMPLETED)
                filter.addAction(DOWNLOAD_ERROR)

                return filter
            }
    }
}