package com.btp.me.classroom

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.btp.me.classroom.Class.ChatMessage
import com.btp.me.classroom.Class.MessageType
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.adapter.ChatAdapter
import com.btp.me.classroom.slide.SlideActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_public_chat.*
import org.json.JSONObject
import java.sql.Date
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class PublicChatActivity : AppCompatActivity() {

    private val mRootRef = FirebaseDatabase.getInstance().reference
    private var mCurrentUser: FirebaseUser? = null
//    private lateinit var classId:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_public_chat)

        public_chat_recycler_list.setHasFixedSize(true)
        public_chat_recycler_list.layoutManager = LinearLayoutManager(this)

        mCurrentUser = FirebaseAuth.getInstance()?.currentUser ?: sendToHomepage()
//        classId = intent.getStringExtra(MainActivity.CLASSID)?: return
//        if(classId == null)
//            finish()


        // get User Name


        val classNameReference = mRootRef.child("Users/${mCurrentUser?.uid}/name")
        classNameReference.keepSynced(true)

        classNameReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                userName = p0.value.toString()
            }

        })
        setTitle()
        public_chat_send_button.setOnClickListener { getTypeMessage() }
        getSendMessageFromDatabase()

    }


    override fun onStart() {
        super.onStart()


    }

    private fun setTitle() {
        val database = mRootRef.child("Classroom/$classId/name")
        database.keepSynced(true)

        database.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "No Internet connections")
                Toast.makeText(this@PublicChatActivity, R.string.no_internet, Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.value == null || p0.value.toString() == "null")
                    finish()
                title = p0.value.toString()
            }

        })
    }

    private fun isCommand(command: String): Boolean {
        if (command[0] != '~')
            return false
        when (command.removePrefix("~").toLowerCase()) {
            "classname" -> {
                Toast.makeText(this, title, Toast.LENGTH_LONG).show()
                public_chat_type_message.text.clear()
                sendMessage(command.removePrefix("~"), "command", "me")
            }
            "goto slide" -> {
                public_chat_type_message.text.clear()
                sendMessage(command.removePrefix("~"), "command", "me")
                sendToSlideActivity()
            }
        }
        return true
    }


    private fun getTypeMessage() {                                                // The Message typed by the user in input box
        if (public_chat_type_message.text.isNotBlank()) {
            val message = public_chat_type_message.text.toString().trim()
            if (isCommand(message)) {
                Log.d(TAG, "Is a command : $message")
            } else {
                Log.d(TAG, "Not a command : $message")
                sendMessage(message, "message", "everyone")
            }
        }
    }

    private fun sendMessage(message: String, type: String, visibility: String) {
        val map = HashMap<String, String>()
        if (userName == "null") return


        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
        val dateTime = System.currentTimeMillis()
        val date = dateFormat.parse(dateFormat.format(dateTime)).time
//        val time = dateTime - date


        Log.d("chetan", "date $dateTime : $date.")

        map["message"] = message
        map["senderName"] = userName
        map["senderId"] = mCurrentUser?.uid.toString()
        map["visibility"] = visibility
        map["type"] = type
        map["time"] = dateTime.toString()

        val json = JSONObject(map).toString()

        mRootRef.child("Message/$classId/$date/$dateTime").setValue(json).addOnSuccessListener {
            public_chat_type_message.text.clear()
        }.addOnFailureListener { exception ->
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show()
            Log.d(TAG, "No internet connection : ${exception.message}")
        }
    }

    private fun getSendMessageFromDatabase() {
        mRootRef.child("Message/$classId").orderByKey().limitToLast(10).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "No Internet Connection")
                Toast.makeText(this@PublicChatActivity, R.string.no_internet, Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
//                public_chat_list.removeAllViews()
                var previous = "null"
//                Log.d(TAG,"datasnapshot all data : ${p0.value}")

                val chatList = ArrayList<ChatMessage>()

                for (dayDataSnapshot in p0.children) {
                    if (dayDataSnapshot.key == null || dayDataSnapshot.value == "null") continue

//                    Log.d(TAG,"Json class : $dataSnapshot")

                    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)

                    val date = dateFormat.format(Date(dayDataSnapshot.key!!.toLong()))

                    //show date before listing the message of each day
                    chatList.add(ChatMessage(visibility = "everyone",message = date,type = "date", viewType = MessageType.DATE))

                    for (dataSnapshot in dayDataSnapshot.children) {
                        if (dataSnapshot == null || dataSnapshot.value == "null") continue

                        val map = Gson().fromJson(dataSnapshot.value.toString(), ChatMessage::class.java)

                        map.time = timeFormat.format(Date(map.time.toLong()))


                        if (map.visibility == "me" && map.senderId != mCurrentUser?.uid) {
                            continue
                        }

                        if (map.type == "command") {
                            previous = "null"
                            map.viewType = MessageType.MY_COMMAND
                        } else {
                            if (map.senderId == mCurrentUser?.uid) {
                                map.viewType = when (previous) {map.senderId -> MessageType.MY_MESSAGE; else -> MessageType.MY_FIRST_MESSAGE
                                }
                            } else {
                                map.viewType = when (previous) {map.senderId -> MessageType.OTHER_MESSAGE; else -> MessageType.OTHER_FIRST_MESSAGE
                                }
                            }
                            previous = map.senderId
                        }
                        chatList.add(map)
                    }
                }
                val adapter = ChatAdapter(chatList)
                public_chat_recycler_list.adapter = adapter
                public_chat_recycler_list.scrollToPosition(chatList.size - 1)
                public_chat_recycler_list.post { kotlin.run { public_chat_recycler_list.scrollToPosition(chatList.size - 1) } }
            }
        })
    }

//    private fun addMessageBox(message: String,type:BoxType){
//        val textView = TextView(this)
//        textView.text = message
//        textView.setTextColor(Color.parseColor("#000000"))
//        val textViewParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
//        textViewParams.weight = 8f
//
//        when(type){
//            BoxType.MY_COMMAND_BOX->{
//                Log.d(TAG,"Message My command : $message")
//                textView.setBackgroundResource(R.drawable.my_command_bubble)
//                textView.setPadding(10,10,10,10)
//
//                textViewParams.gravity = Gravity.CENTER
//                textViewParams.topMargin = 10
//            }
//            BoxType.MY_MESSAGE_BOX->{
//                Log.d(TAG,"Message my Message : $message")
//                textView.setPadding(10,10,30,10)
//                textView.setBackgroundResource(R.drawable.my_message_bubble)
//
//                textViewParams.gravity = Gravity.END
//                textViewParams.topMargin = 4
//
//            }
//            BoxType.MY_FIRST_MESSAGE_BOX->{
//                Log.d(TAG,"Message My first Message : $message")
//                textView.setPadding(10,10,30,10)
//                textView.setBackgroundResource(R.drawable.my_first_message_bubble)
////
//                textViewParams.gravity = Gravity.END
//                textViewParams.topMargin = 10
//            }
//            BoxType.OTHER_MESSAGE_BOX->{
//                Log.d(TAG,"Message other message : $message")
//                textView.setPadding(30,10,10,10)
//                textView.setBackgroundResource(R.drawable.other_message_bubble)
//
//                textViewParams.gravity = Gravity.START
//                textViewParams.topMargin = 4
//            }
//            BoxType.OTHER_FIRST_MESSAGE_BOX->{
//                Log.d(TAG,"Message other first message : $message")
//                textView.setPadding(30,10,10,10)
//                textView.setBackgroundResource(R.drawable.other_first_message_bubble)
//
//                textViewParams.gravity = Gravity.START
//                textViewParams.topMargin = 10
//            }
//            else -> return
//        }
//        textView.layoutParams = textViewParams
////        public_chat_list.addView(textView)
//    }

    private fun sendToMainActivity() {

    }

    private fun sendToHomepage(): FirebaseUser? {
        val intent = Intent(this, HomepageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
        return null
    }

    private fun sendToSlideActivity() {
        if (classId == "null") {
            sendToMainActivity()
            return
        }


        val intent = Intent(this, SlideActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "chetan"
        private var userName = "null"
//        private const val LEFT = 1
//        private const val RIGHT = 2
//        private const val CENTER = 3
//        private enum class BoxType {MY_MESSAGE_BOX, MY_FIRST_MESSAGE_BOX, MY_COMMAND_BOX, OTHER_MESSAGE_BOX, OTHER_FIRST_MESSAGE_BOX, MY_COMMAND_RESULT_BOX}

        const val CLASSID = "ClassId"


    }
}
