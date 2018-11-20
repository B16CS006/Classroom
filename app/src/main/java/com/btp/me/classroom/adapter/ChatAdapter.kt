package com.btp.me.classroom.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.btp.me.classroom.Class.ChatMessage
import com.btp.me.classroom.Class.MessageType
import com.btp.me.classroom.Class.MyColor
import com.btp.me.classroom.R

class ChatAdapter(private val list: ArrayList<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MessageType.DATE -> DateMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.date_message_layout, parent, false))
            MessageType.MY_COMMAND -> MyCommandViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.my_command_layout, parent, false))
            MessageType.MY_MESSAGE -> MyMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.my_message_layout, parent, false))
            MessageType.MY_FIRST_MESSAGE -> MyFirstMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.my_first_message_layout, parent, false))
            MessageType.OTHER_MESSAGE -> OtherMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.other_message_layout, parent, false))
            MessageType.OTHER_FIRST_MESSAGE -> OtherFirstMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.other_first_message_layout, parent, false))
            else -> MyCommandViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.my_command_layout, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

//        Log.d(TAG,"Message : ${list.get(p1).message}")
//        Log.d(TAG,"View type : ${list.get(p1).viewType}")
//        Log.d(TAG,"view Type : ${p0.itemViewType}")

        when (p0.itemViewType) {
            MessageType.DATE -> (p0 as DateMessageViewHolder).bind(list[p1])
            MessageType.MY_COMMAND -> (p0 as MyCommandViewHolder).bind(list[p1])
            MessageType.MY_MESSAGE -> (p0 as MyMessageViewHolder).bind(list[p1])
            MessageType.MY_FIRST_MESSAGE -> (p0 as MyFirstMessageViewHolder).bind(list[p1])
            MessageType.OTHER_MESSAGE -> (p0 as OtherMessageViewHolder).bind(list[p1])
            MessageType.OTHER_FIRST_MESSAGE -> (p0 as OtherFirstMessageViewHolder).bind(list[p1])
//            else -> (p0 as MyCommandViewHolder).bind(list.get(p1))
        }

    }

    override fun getItemViewType(position: Int): Int {
        return list.get(position).viewType
    }

    class MyCommandViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val layout = view.findViewById<LinearLayout>(R.id.my_command_whole_view)
        private val commandView = view.findViewById<TextView>(R.id.my_command_view)

        fun bind(chatMessage: ChatMessage) {
            setCommand(chatMessage.message)
        }

        private fun setCommand(command: String) {
            commandView.text = command
        }
    }

    class DateMessageViewHolder(val view:View) : RecyclerView.ViewHolder(view){
        private val layout = view.findViewById<LinearLayout>(R.id.date_message_whole_view)
        private val dateMessageView = view.findViewById<TextView>(R.id.date_message_view)

        fun bind(chatMessage: ChatMessage) {
            setDate(chatMessage.message)
        }

        private fun setDate(date: String) {
            dateMessageView.text = date
        }
    }

    class MyMessageViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
//        private val layout = view.findViewById<RelativeLayout>(R.id.my_message_whole_view)
        private val messageView = view.findViewById<TextView>(R.id.my_message_view)
        private val timeView = view.findViewById<TextView>(R.id.my_time_view)

        fun bind(chatMessage: ChatMessage) {
            setMessage(chatMessage.message)
            setTime(chatMessage.time)
        }

        private fun setMessage(message: String) {
            messageView.text = message
        }

        private fun setTime(time: String) {
            timeView.text = time
        }
    }

    class MyFirstMessageViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val layout = view.findViewById<LinearLayout>(R.id.my_first_message_whole_view)
        private val messageView = view.findViewById<TextView>(R.id.my_first_message_view)
        private val timeView = view.findViewById<TextView>(R.id.my_first_time_view)

        fun bind(chatMessage: ChatMessage) {
            setMessage(chatMessage.message)
            setTime(chatMessage.time)
        }

        private fun setMessage(message: String) {
            messageView.text = message
        }

        private fun setTime(time: String) {
            timeView.text = time
        }
    }

    class OtherMessageViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val layout = view.findViewById<LinearLayout>(R.id.other_message_whole_view)
        private val messageView = view.findViewById<TextView>(R.id.other_message_view)
        private val timeView = view.findViewById<TextView>(R.id.other_time_view)

        fun bind(chatMessage: ChatMessage) {
            setMessage(chatMessage.message)
            setTime(chatMessage.time)
        }

        private fun setMessage(message: String) {
           messageView.text = message
        }

        private fun setTime(time: String) {
           timeView.text = time
        }
    }

    class OtherFirstMessageViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val layout = view.findViewById<LinearLayout>(R.id.other_message_whole_view)
        private val nameView = view.findViewById<TextView>(R.id.other_first_name_view)
        private val messageView = view.findViewById<TextView>(R.id.other_first_message_view)
        private val timeView = view.findViewById<TextView>(R.id.other_first_time_view)

        fun bind(chatMessage: ChatMessage) {
            setRollNumber(chatMessage.senderRollNumber, chatMessage.senderName)
            setMessage(chatMessage.message)
            setTime(chatMessage.time)
        }

        private fun setName(name: String) {
            nameView.text = name
            nameView.setTextColor(Color.parseColor(MyColor.chooseColor(name)))
        }

        private fun setRollNumber(rollNumber:String, name: String){
            if(rollNumber == "null")
                setName(name)
            else
                setName(rollNumber)
        }

        private fun setMessage(message: String) {
            messageView.text = message
        }

        private fun setTime(time: String) {
            timeView.text = time
        }
    }

    companion object {
        private const val TAG = "chetan"

    }
}