package com.btp.me.classroom

import android.content.ComponentCallbacks2
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.btp.me.classroom.MainActivity.Companion.classId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_public_chat.*

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

    private fun getSendMessageFromDatabase() {
        mRootRef.child("Message/$classId").addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG,"No Internet Connection")
                Toast.makeText(this@PublicChatActivity,R.string.no_internet, Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                val previous = "null"
                for(dataSnapshot in p0.children) {
                    if(dataSnapshot == null) continue
                    val visibility = dataSnapshot.child("visibility").value.toString()
                    val type = dataSnapshot.child("type").value.toString()
                    val senderId = dataSnapshot.child("senderId").value.toString()
                    val message = dataSnapshot.child("message").value.toString()
                    val time = dataSnapshot.child("time").value.toString()

                    if (visibility == "me" && senderId != mCurrentUser?.uid) return

                    val boxMessage:String
                    val boxType :BoxType

                    if (type == "message" && senderId == mCurrentUser?.uid) {
                        boxMessage = "You:\n$message"
                        if (previous == senderId)
                            boxType = BoxType.MY_MESSAGE_BOX
                        else
                            boxType = BoxType.MY_FIRST_MESSAGE_BOX
                    } else if (type == "message" && senderId != mCurrentUser?.uid) {
                        boxMessage = "$senderId:\n$message"
                        if(previous == senderId)
                            boxType = BoxType.OTHER_MESSAGE_BOX
                        else
                            boxType = BoxType.OTHER_FIRST_MESSAGE_BOX
                    } else {
                        boxMessage = message
                        boxType = BoxType.MY_COMMAND_BOX
                    }

                    addMessageBox(boxMessage,boxType)
                }
            }

        })
    }

    override fun onStart() {
        super.onStart()


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

        map["senderName"] = userName
        map["message"] = message
        map["senderId"] = mCurrentUser?.uid.toString()
        map["visibility"] = visibility
        map["type"] = type
        map["time"] = time

        mRootRef.child("Message/$classId/$time").setValue(map.toMap()).addOnSuccessListener {
                public_chat_type_message.text.clear()
        }.addOnFailureListener{exception ->
            Toast.makeText(this,R.string.no_internet,Toast.LENGTH_LONG).show()
            Log.d(TAG,"No internet connection")
        }
    }

    private fun addMessageBox(message: String,type:BoxType){


//        val linearlayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        val linearLayout = LinearLayout(this)
//        linearLayout.orientation = LinearLayout.HORIZONTAL
//        linearLayout.layoutParams = linearlayoutParams
//
//        val view = TextView(this)
//        view.text = "c"
//        val viewParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        viewParams.weight = 2f
////        viewParams.weight = 2f
//
        val textView: TextView = TextView(this)
        textView.text = message
        val textViewParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        textViewParams.weight = 1f
//        textViewParams.weight = 8f

        when(type){
            BoxType.MY_COMMAND_BOX->{
                textViewParams.gravity = Gravity.CENTER
                textView.setBackgroundResource(R.drawable.my_command_bubble)
            }
            BoxType.MY_MESSAGE_BOX->{
                textViewParams.gravity = Gravity.END
                textView.setPadding(10,10,30,10)
                textView.setBackgroundResource(R.drawable.my_message_bubble)
            }
            BoxType.MY_FIRST_MESSAGE_BOX->{
                textViewParams.gravity = Gravity.END
                textView.setPadding(10,10,30,10)
                textView.setBackgroundResource(R.drawable.my_first_message_bubble)
            }
            BoxType.OTHER_MESSAGE_BOX->{
                textViewParams.gravity = Gravity.START
                textView.setPadding(30,10,10,10)
                textView.setBackgroundResource(R.drawable.other_message_bubble)
            }
            BoxType.OTHER_FIRST_MESSAGE_BOX->{
                textViewParams.gravity = Gravity.START
                textView.setPadding(30,10,10,10)
                textView.setBackgroundResource(R.drawable.other_first_message_bubble)
            }
            else -> return
        }
        textView.layoutParams = textViewParams
        public_chat_list.addView(textView)

        public_chat_scrollView.fullScroll(View.FOCUS_DOWN)
    }

    private fun isCommand(command: String): Boolean {
        if(command[0] != '~')
            return false
        when(command.removePrefix("~").toLowerCase()){
            "class name" -> {
                Toast.makeText(this, title, Toast.LENGTH_LONG).show()
                public_chat_type_message.text.clear()
                sendMessage(command.removePrefix("~"),"command","me")
            }
        }
        return true
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
