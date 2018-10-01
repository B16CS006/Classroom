package com.btp.me.classroom.slide


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.WrapperListAdapter
import com.btp.me.classroom.ClassHomeActivity
import com.btp.me.classroom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_slide.*
import java.io.File
import java.io.IOException
import java.nio.file.Files.createFile
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


open class SlideFragment() : Fragment() {

    private var classId: String? = null
    private lateinit var databaseReference: DatabaseReference
    private var mCurrentUser = FirebaseAuth.getInstance().currentUser
    private val mRootRef = FirebaseDatabase.getInstance().reference

    private lateinit var floatingActionButton: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_slide, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        floatingActionButton = activity?.findViewById(R.id.class_home_floating_button) as FloatingActionButton
        floatingActionButton.setImageResource(R.drawable.ic_cloud_upload_white_24dp)
        floatingActionButton.setOnClickListener {
            val pdfFileIntent = Intent()
            pdfFileIntent.type = "application/pdf"
            pdfFileIntent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(pdfFileIntent, "Select Document"), 0)

        }

        slide_list.setHasFixedSize(true)
        slide_list.layoutManager = LinearLayoutManager(context)

        val context = activity as ClassHomeActivity
        classId = context.classId
        databaseReference = FirebaseDatabase.getInstance().getReference("Classroom/${context.classId}").child("slide")


        val slideList = ArrayList<HashMap<String, String>>()
        val slideAdapter = object : RecyclerView.Adapter<MyViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, p1: Int): MyViewHolder {
                Log.d("chetan", "Slide adapter on create viewHolder")
                return MyViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.file_single_layout, parent, false))

            }

            override fun getItemCount() = slideList.size

            override fun onBindViewHolder(holder: MyViewHolder, p1: Int) {
                Log.d("chetan", "Binding the holders")

                holder.title.text = slideList[p1]["title"]
                holder.date.text = slideList[p1]["date"]

                holder.download.setOnClickListener {
                    Log.d("chetan", "You have clicked ${slideList[p1]["title"]}")
                    try {
                        val fileName: File = createFile(slideList[p1]["title"]!!)
                                ?: return@setOnClickListener
                        val fileUrl = slideList[p1]["link"] ?: return@setOnClickListener

                        val downloadIntent = Intent(activity, MyDownloadingService::class.java)
                        downloadIntent.putExtra(MyDownloadingService.EXTRA_FILE_PATH, fileName)
                        downloadIntent.putExtra(MyDownloadingService.EXTRA_DOWNLOAD_PATH, fileUrl)
                        downloadIntent.action = MyDownloadingService.ACTION_DOWNLOAD
                        activity?.startService(downloadIntent)
                                ?: throw error("Can't download as No activity is running")


                    } catch (error: IOException) {
                        Log.d("chetan", "Error while making folder ${error.message}")
                        error.printStackTrace()
                    }
                }
            }
        }

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("chetan", "Database Reference for slide is on cancelled")
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
                }

                slide_list.adapter = slideAdapter


            }
        })

    }

    override fun onStart() {
        super.onStart()

    }

    fun createFile(title: String): File? {
        try {
            var fileName: File = File(Environment.getExternalStorageDirectory().toString(), "Classroom")

            if (!fileName.exists()) {
                Log.d("chetan", "Directory ${File.separator} not exist : $fileName")
                fileName.mkdir()
            }
            fileName = File(fileName.toString(), "media")
            if (!fileName.exists()) {
                Log.d("chetan", "Directory ${File.separator} not exist : $fileName")
                fileName.mkdir()
            }
            fileName = File(fileName.toString(), "slide")
            if (!fileName.exists()) {
                Log.d("chetan", "Directory ${File.separator} not exist : $fileName")
                fileName.mkdir()
            }
            fileName = File(fileName, title)

            fileName.createNewFile()

            return fileName
        } catch (error: IOException) {
            Log.d("chetan", "Error while making folder ${error.message}")
            error.printStackTrace()
        }

        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 0 && data != null && data.data != null) {
            val uri = data.data ?: return
            upload(uri)
        } else {
            Toast.makeText(activity, "PDF can't be retrieve.", Toast.LENGTH_LONG).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun upload(uri: Uri) {
        Log.d("chetan", "uploading Uri : ${uri.toString()}")
        val uploadingIntent = Intent(activity, MyUploadingService::class.java)
        uploadingIntent.putExtra("classId", classId)
        uploadingIntent.putExtra("userId", mCurrentUser!!.uid)
        uploadingIntent.putExtra("fileUri", uri)
        uploadingIntent.action = MyUploadingService.ACTION_UPLOAD
        activity?.startService(uploadingIntent)
                ?: Log.d("chetan", "At this this no activy is running")
    }

    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.file_single_title)
        val date: TextView = view.findViewById(R.id.file_single_date)
        val download: ImageButton = view.findViewById(R.id.file_single_download)
//        val pdf : WebView = view.findViewById(R.id.file_web_view)



    }


}
