package com.btp.me.classroom.adapter

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.btp.me.classroom.Class.ChatMessage
import com.btp.me.classroom.Class.MessageType
import com.btp.me.classroom.R
import kotlinx.android.synthetic.main.my_command_layout.view.*
import kotlinx.android.synthetic.main.my_first_message_layout.view.*
import kotlinx.android.synthetic.main.my_message_layout.view.*
import kotlinx.android.synthetic.main.other_first_message_layout.view.*
import kotlinx.android.synthetic.main.other_message_layout.view.*

class ChatAdapter(private val list: ArrayList<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            MessageType.MY_MESSAGE-> MyMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.my_message_layout, parent, false))
            MessageType.MY_FIRST_MESSAGE-> MyFirstMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.my_first_message_layout, parent, false))
            MessageType.OTHER_MESSAGE-> OtherMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.other_message_layout, parent, false))
            MessageType.OTHER_FIRST_MESSAGE-> OtherFirstMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.other_first_message_layout, parent, false))
            else-> MyCommandViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.my_command_layout, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

//        Log.d(TAG,"Message : ${list.get(p1).message}")
//        Log.d(TAG,"View type : ${list.get(p1).viewType}")
//        Log.d(TAG,"view Type : ${p0.itemViewType}")

        when(p0.itemViewType){
            MessageType.MY_MESSAGE-> (p0 as MyMessageViewHolder).bind(list.get(p1))
            MessageType.MY_FIRST_MESSAGE-> (p0 as MyFirstMessageViewHolder).bind(list.get(p1))
            MessageType.OTHER_MESSAGE-> (p0 as OtherMessageViewHolder).bind(list.get(p1))
            MessageType.OTHER_FIRST_MESSAGE-> (p0 as OtherFirstMessageViewHolder).bind(list.get(p1))
            else-> (p0 as MyCommandViewHolder).bind(list.get(p1))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return list.get(position).viewType
    }

    class MyCommandViewHolder(val view:View): RecyclerView.ViewHolder(view){
        fun bind(chatMessage: ChatMessage){
            setCommand(chatMessage.message)
        }
        fun setCommand(command:String){
            view.my_command_view.text = command
        }
    }

    class MyMessageViewHolder(val view:View): RecyclerView.ViewHolder(view){
        fun bind(chatMessage: ChatMessage){
            setMessage(chatMessage.message)
            setTime(chatMessage.time)
        }
        fun setMessage(message: String){
            view.my_message_view.text = message
        }
        fun setTime(time:String){
            view.my_time_view.text = time
        }
    }

    class MyFirstMessageViewHolder(val view:View): RecyclerView.ViewHolder(view){
        fun bind(chatMessage: ChatMessage){
            setMessage(chatMessage.message)
            setTime(chatMessage.time)
        }
        fun setMessage(message: String){
            view.my_first_message_view.text = message
        }
        fun setTime(time:String){
            view.my_first_time_view.text = time
        }
    }

    class OtherMessageViewHolder(val view:View): RecyclerView.ViewHolder(view){
        fun bind(chatMessage: ChatMessage){
            setMessage(chatMessage.message)
            setTime(chatMessage.time)
        }
        fun setMessage(message: String){
            view.other_message_view.text = message
        }
        fun setTime(time:String){
            view.other_time_view.text = time
        }
    }

    class OtherFirstMessageViewHolder(val view:View): RecyclerView.ViewHolder(view){
        fun bind(chatMessage: ChatMessage){
            setName(chatMessage.senderName)
            setMessage(chatMessage.message)
            setTime(chatMessage.time)
        }
        fun setName(name:String){
            view.other_first_name_view.text = name
        }
        fun setMessage(message: String){
            view.other_first_message_view.text = message
        }
        fun setTime(time:String){
            view.other_first_time_view.text = time
        }
    }

    companion object {
        private const val TAG = "chetan"
    }
}