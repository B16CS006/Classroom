package com.btp.me.classroom.slide

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import com.btp.me.classroom.Class.FileBuilder.Companion.createFile
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.IntentResult
import com.btp.me.classroom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_slide.*
import kotlinx.android.synthetic.main.single_slide_layout.view.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SlideActivity : AppCompatActivity() {

    private var databaseReference = FirebaseDatabase.getInstance().getReference("Classroom/$classId/slide")
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slide)

        currentUser = FirebaseAuth.getInstance().currentUser

        slide_upload_button.setOnClickListener {
            startActivityForResult(Intent.createChooser(IntentResult.forPDF(),"Select Document"),0)
        }

        slide_list.setHasFixedSize(true)
        slide_list.layoutManager = LinearLayoutManager(this)

        val slideList = ArrayList<HashMap<String, String>>()

        val slideAdapter = object : RecyclerView.Adapter<SlideViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, p1: Int): SlideViewHolder {
                Log.d("chetan", "Slide adapter on create viewHolder")
                return SlideViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.single_slide_layout, parent, false))

            }

            override fun getItemCount() = slideList.size

            override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
//                Log.d("chetan", "Binding the holders")

                holder.bind(slideList[position]["title"]!!, slideList[position]["date"]!!)

                holder.download.setOnClickListener {
                    Log.d("chetan", "You have clicked ${slideList[position]["title"]}")
                    try {
                        val fileName: File = createFile(slideList[position]["title"]!!)
                                ?: return@setOnClickListener
                        val fileUrl = slideList[position]["link"] ?: return@setOnClickListener

                        val downloadIntent = Intent(this@SlideActivity, MyDownloadingService::class.java)
                        downloadIntent.putExtra(MyDownloadingService.EXTRA_FILE_PATH, fileName)
                        downloadIntent.putExtra(MyDownloadingService.EXTRA_DOWNLOAD_PATH, fileUrl)
                        downloadIntent.action = MyDownloadingService.ACTION_DOWNLOAD
                        startService(downloadIntent)?: throw error("Can't download as No activity is running")
                    } catch (error: IOException) {
                        Log.d("chetan", "Error while making folder ${error.message}")
                        error.printStackTrace()
                    }
                }
            }
        }

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("chetan", "Database Reference for slide is on cancelled, ${p0.message}")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("chetan", "Database Reference for Slide on Data Changed : ${dataSnapshot.key}")
                slideList.clear()
                for (userList in dataSnapshot.children) {
                    for (slide in userList.children) {
                        if (slide == null) continue
                        var date = slide.key ?: continue
                        date = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.ENGLISH).format(date.toLong())

                        val map = HashMap<String, String>()
                        map["date"] = date
                        map["title"] = slide.child("title").value.toString()
                        map["link"] = slide.child("link").value.toString()
                        slideList.add(map)
                    }
                }

                if(slide_list != null && slideList.size == 0) {
                    slide_empty.visibility = View.VISIBLE
                    slide_list.visibility = View.GONE
                }
                else {
                    slide_empty.visibility = View.GONE
                    slide_list.visibility = View.VISIBLE
                    slide_list.adapter = slideAdapter
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 0 && data != null && data.data != null) {
            val uri = data.data ?: return
            upload(uri)
        } else {
            Toast.makeText(this, "PDF can't be retrieve.", Toast.LENGTH_LONG).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun upload(uri: Uri) {
        var file = uri.lastPathSegment
        if(!file.endsWith(".pdf")){
            file += ".pdf"
        }
        val data = """{"title": "$file","link": ""}"""
        Log.d("chetan", "uploading Uri : $uri")
        val uploadingIntent = Intent(this, MyUploadingService::class.java)

//        uploadingIntent.putExtra("classId", classId)
//        uploadingIntent.putExtra("userId", currentUser!!.uid)

        val userId = currentUser?.uid?:return
        val currentTime = System.currentTimeMillis().toString()

        uploadingIntent.putExtra("fileUri", uri)
        uploadingIntent.putExtra("storagePath","Slide/$classId/$userId/$currentTime")
        uploadingIntent.putExtra("databasePath","Classroom/$classId/slide/$userId/$currentTime")
        uploadingIntent.putExtra("data",data)

        uploadingIntent.action = MyUploadingService.ACTION_UPLOAD
        startService(uploadingIntent)
                ?: Log.d("chetan", "At this this no activity is running")
    }

    private class SlideViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val download: ImageButton = view.file_single_download

        fun bind(title:String,date:String){
            setTitle(title)
            setDate(date)
        }
        private fun setTitle(s:String){
            view.file_single_title.text = s
        }
        private fun setDate(s:String){
            view.file_single_date.text = s
        }
    }


}

