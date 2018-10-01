package com.btp.me.classroom.Redundent

import android.os.Environment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.btp.me.classroom.R
import java.io.File
import java.io.IOException

class SlideAdapter(val list: ArrayList<HashMap<String, String>>) : RecyclerView.Adapter<SlideAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): MyViewHolder {
        Log.d("chetan", "Slide adapter on create viewHolder")
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.file_single_layout, parent, false))

    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MyViewHolder, p1: Int) {

        Log.d("chetan", "Binding the holders")

        holder.title.text = list[p1]["title"]
        holder.date.text = list[p1]["date"]
//        holder.pdf.settings.javaScriptEnabled = true
//        holder.pdf.loadUrl(list[p1]["value"])
//        Log.d("chetan",list[p1]["value"])

        holder.view.setOnClickListener {
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
                fileName = File(fileName, "${list[p1]["title"]}.pdf")
                val fileUrl = list[p1]["link"]?: return@setOnClickListener

                fileName.createNewFile()
                Downloader().downloadFile(fileUrl, fileName)

            } catch (error: IOException) {
                Log.d("chetan", "Error while making folder ${error.message}")
                error.printStackTrace()
            }
        }
    }


    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.file_single_title)
        val date: TextView = view.findViewById(R.id.file_single_date)
//        val pdf : WebView = view.findViewById(R.id.file_web_view)
    }
}