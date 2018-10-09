package com.btp.me.classroom

import android.content.ComponentCallbacks2
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.btp.me.classroom.Class.ChatMessage
import com.btp.me.classroom.MainActivity.Companion.classId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_public_chat.*
import org.json.JSONObject

class PublicChatActivity : AppCompatActivity() {

    private val mRootRef = FirebaseDatabase.getInstance().reference
    private var mCurrentUser: FirebaseUser? = null
//    private lateinit var classId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_public_chat)

//        public_chat_list.setHasFixedSize(true)
//        public_chat_list.layoutManager = LinearLayoutManager(this)

        mCurrentUser = FirebaseAuth.getInstance()?.currentUser?: sendToHomepage()
//        classId = intent.getStringExtra(MainActivity.CLASSID)?: return
//        if(classId == null)
//            finish()


        // get User Name
        mRootRef.child("Users/${mCurrentUser?.uid}/name").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                userName = p0.value.toString()
            }

        })
        setTitle()
        public_chat_send_button.setOnClickListener{ getTypeMessage() }
        getSendMessageFromDatabase()

    }



    override fun onStart() {
        super.onStart()


    }

    private fun setTitle(){
        mRootRef.child("Classroom/$classId/name").addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG,"No Internet connections")
                Toast.makeText(this@PublicChatActivity,R.string.no_internet,Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.value == null || p0.value.toString() == "null")
                    finish()
                title = p0.value.toString()
            }

        })
    }

    private fun isCommand(command: String): Boolean {
        if(command[0] != '~')
            return false
        when(command.removePrefix("~").toLowerCase()){
            "classname" -> {
                Toast.makeText(this, title, Toast.LENGTH_LONG).show()
                public_chat_type_message.text.clear()
                sendMessage(command.removePrefix("~"),"command","me")
            }
        }
        return true
    }

    private fun getTypeMessage(){
        if(public_chat_type_message.text.isNotBlank()){
            val message = public_chat_type_message.text.toString().trim()
            if(isCommand(message)){
                Log.d(TAG,"Is a command : $message")
            }else{
                Log.d(TAG,"Not a command : $message")
                sendMessage(message,"message","everyone")
            }
        }

    }

    private fun sendMessage(message: String, type: String, visibility:String) {
        val map = HashMap<String,String>()
        if(userName == "null") return
        val time = System.currentTimeMillis().toString()


        map["message"] = message
        map["senderName"] = userName
        map["senderId"] = mCurrentUser?.uid.toString()
        map["visibility"] = visibility
        map["type"] = type
        map["time"] = time

        val json = JSONObject(map).toString()

        mRootRef.child("Message/$classId/$time").setValue(json).addOnSuccessListener {
            public_chat_type_message.text.clear()
        }.addOnFailureListener{exception ->
            Toast.makeText(this,R.string.no_internet,Toast.LENGTH_LONG).show()
            Log.d(TAG,"No internet connection")
        }
    }

    private fun getSendMessageFromDatabase() {
        mRootRef.child("Message/$classId").addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG,"No Internet Connection")
                Toast.makeText(this@PublicChatActivity,R.string.no_internet, Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                public_chat_list.removeAllViews()
                var previous = "null"
//                Log.d(TAG,"datasnapshot all data : ${p0.value}")
                for(dataSnapshot in p0.children) {
                    if(dataSnapshot == null || dataSnapshot.value == "null" ) continue

//                    Log.d(TAG,"Json class : $dataSnapshot")

                    val map = Gson().fromJson(dataSnapshot.value.toString(),ChatMessage::class.java)

//                    val visibility = dataSnapshot.child("visibility").value.toString()
//                    val type = dataSnapshot.child("type").value.toString()
//                    val senderId = dataSnapshot.child("senderId").value.toString()
//                    val message = dataSnapshot.child("message").value.toString()
//                    val time = dataSnapshot.child("time").value.toString()
//                    val senderName = dataSnapshot.child("senderName").value.toString()

                    val visibility = map.visibility
                    val type = map.type
                    val senderId = map.senderId
                    val senderName = map.senderName
                    val time = map.time
                    val message = map.message

                    if (visibility == "me" && senderId != mCurrentUser?.uid){
                        continue
                    }

                    val boxMessage:String
                    val boxType :BoxType

                    if(type == "command"){
                        previous = "null"
                        boxMessage = message
                        boxType = BoxType.MY_COMMAND_BOX
                    }else{
                        if(senderId == mCurrentUser?.uid){
                            boxMessage = message
                            boxType = when(previous) {senderId-> BoxType.MY_MESSAGE_BOX; else -> BoxType.MY_FIRST_MESSAGE_BOX }
                        }else{
                            boxMessage = "$senderName:\n$message"
                            boxType = when(previous) {senderId-> BoxType.OTHER_MESSAGE_BOX; else -> BoxType.OTHER_FIRST_MESSAGE_BOX }
                        }
                        previous = senderId
                    }

//                    Log.d(TAG,"Previous : $previous")
//                    Log.d(TAG,"message : $boxMessage")
//                    Log.d(TAG,"boxType : $boxType")

                    addMessageBox(boxMessage,boxType)
                }

                public_chat_scrollView.post {
                    kotlin.run {
                        public_chat_scrollView.fullScroll(View.FOCUS_DOWN)
                    }
                }
            }
        })
    }

    private fun addMessageBox(message: String,type:BoxType){
        val textView: TextView = TextView(this)
        textView.text = message
        textView.setTextColor(Color.parseColor("#000000"))
        val textViewParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        textViewParams.weight = 1f

        when(type){
            BoxType.MY_COMMAND_BOX->{
                Log.d(TAG,"Message My command : $message")
                textView.setBackgroundResource(R.drawable.my_command_bubble)
                textView.setPadding(10,10,10,10)

                textViewParams.gravity = Gravity.CENTER
                textViewParams.topMargin = 10
            }
            BoxType.MY_MESSAGE_BOX->{
                Log.d(TAG,"Message my Message : $message")
                textView.setPadding(10,10,30,10)
                textView.setBackgroundResource(R.drawable.my_message_bubble)

                textViewParams.gravity = Gravity.END
                textViewParams.topMargin = 4

            }
            BoxType.MY_FIRST_MESSAGE_BOX->{
                Log.d(TAG,"Message My first Message : $message")
                textView.setPadding(10,10,30,10)
                textView.setBackgroundResource(R.drawable.my_first_message_bubble)

                textViewParams.gravity = Gravity.END
                textViewParams.topMargin = 10
            }
            BoxType.OTHER_MESSAGE_BOX->{
                Log.d(TAG,"Message other message : $message")
                textView.setPadding(30,10,10,10)
                textView.setBackgroundResource(R.drawable.other_message_bubble)

                textViewParams.gravity = Gravity.START
                textViewParams.topMargin = 4
            }
            BoxType.OTHER_FIRST_MESSAGE_BOX->{
                Log.d(TAG,"Message other first message : $message")
                textView.setPadding(30,10,10,10)
                textView.setBackgroundResource(R.drawable.other_first_message_bubble)

                textViewParams.gravity = Gravity.START
                textViewParams.topMargin = 10
            }
            else -> return
        }

//        textViewParams.setMargins(10,10,10,10)

        textView.layoutParams = textViewParams
        public_chat_list.addView(textView)
    }

    private fun sendToMainActivity() {

    }

    private fun sendToHomepage(): FirebaseUser?{
        val intent = Intent(this,HomepageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
        return null
    }

    companion object {
        private const val TAG = "chetan"
        var userName = "null"
        private const val LEFT = 1
        private const val RIGHT = 2
        private const val CENTER = 3
        private enum class BoxType {MY_MESSAGE_BOX, MY_FIRST_MESSAGE_BOX, MY_COMMAND_BOX, OTHER_MESSAGE_BOX, OTHER_FIRST_MESSAGE_BOX, MY_COMMAND_RESULT_BOX}
    }
}
