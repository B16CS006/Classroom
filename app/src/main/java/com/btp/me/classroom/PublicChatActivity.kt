package com.btp.me.classroom

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.btp.me.classroom.assignment.AssignmentActivity
import com.btp.me.classroom.assignment.AssignmentUploadActivity
import com.btp.me.classroom.Class.ChatMessage
import com.btp.me.classroom.Class.MessageType
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.adapter.ChatAdapter
import com.btp.me.classroom.people.ClassMembersActivity
import com.btp.me.classroom.slide.SlideActivity
import com.btp.me.classroom.teacher.PendingRequestActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_public_chat.*
import org.json.JSONObject
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class PublicChatActivity : AppCompatActivity() {

    private val mRootRef = FirebaseDatabase.getInstance().reference
    private var currentUser: FirebaseUser? = null
//    private lateinit var classId:String

    private var isPendingRequestAccessed : Boolean= false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_public_chat)

        public_chat_recycler_list.setHasFixedSize(true)
        public_chat_recycler_list.layoutManager = LinearLayoutManager(this)

        currentUser = FirebaseAuth.getInstance()?.currentUser ?: sendToHomepage()
        if(classId == "null") finish()

        initialize()

        getSendMessageFromDatabase()
        public_chat_send_button.setOnClickListener { getTypeMessage() }
    }

    private fun initialize(){
        setTitle()
        setToolbar()
        setPendingRequestAccessed()
        setUserName()
    }

    private fun setUserName() {
        mRootRef.child("Users/${currentUser?.uid}/name").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                userName = "Anonymous"
            }

            override fun onDataChange(p0: DataSnapshot) {
                userName = p0.value.toString()
            }

        })
    }

    private fun setTitle(){
        mRootRef.child("Classroom/$classId/name").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "No Internet connections")
                Toast.makeText(this@PublicChatActivity, R.string.no_internet, Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.value == null || p0.value.toString() == "null")
                    finish()
                supportActionBar?.title = p0.value.toString()
            }

        })

    }

    private fun setToolbar() {
        setSupportActionBar(public_chat_toolbar)
        with(public_chat_toolbar){
            setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        }
    }

    private fun getTypeMessage() {                                                // The Message typed by the user in input box
        if (public_chat_type_message.text.isNotBlank()) {
            val message = public_chat_type_message.text.toString().trim()
            if (isCommand(message)) {
                Log.d(TAG, "Is a command : $message")
            } else {
                Log.d(TAG, "Not a command : $message")
                public_chat_type_message.text.clear()
                sendMessage(message, "message", "everyone")
            }
        }
    }

    private fun isCommand(command: String): Boolean {
        if (!command.startsWith(COMMAND_TAG))
            return false
        when (command.removePrefix(COMMAND_TAG).toLowerCase()) {
            commandList[0] ->{
                sendMessage(commandList[0],"command","me")
                Toast.makeText(this, userName,Toast.LENGTH_LONG).show()
            }
            commandList[1] -> {
                sendMessage(commandList[1], "command", "me")
                Toast.makeText(this, title, Toast.LENGTH_LONG).show()
            }
            commandList[2] ->{
                sendMessage(commandList[2],"command","me")
                sendToMembersActivity()
            }
            commandList[3] -> {
                sendMessage(commandList[3], "command", "me")
                sendToSlideActivity()
            }
            commandList[4] ->{
                sendMessage(commandList[4],"command","me")
                sendToAssignmentUploadActivity()
            }
            commandList[5] ->{
                sendMessage(commandList[5],"command", "me")
                sendToAssignmentActivity()
            }
            commandList[6] ->{
                sendMessage(commandList[6],"command","me")
                leaveClassroom()
            }
            commandList[7] ->{
                sendMessage(commandList[7],"command","me")
                sendToPendingRequestActivity()
            }

            else ->{
                Toast.makeText(this,"No Such Command Found",Toast.LENGTH_LONG).show()
                return true
            }
        }

        public_chat_type_message.text.clear()
        return true
    }

    private fun sendToPendingRequestActivity() {
        if (isPendingRequestAccessed)
            startActivity(Intent(this, PendingRequestActivity::class.java))
        else{
            Toast.makeText(this,"Be Teacher First", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isPendingRequest(){
        if(isPendingRequestAccessed){
            //todo show pending icon
        }else{
            //todo hide pending icon
        }
    }

    private fun setPendingRequestAccessed(){
        mRootRef.child("Class-Enroll/${currentUser?.uid}/$classId/as").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG,"Error : ${p0.message}")
            }

            override fun onDataChange(data: DataSnapshot) {
                isPendingRequestAccessed = data.value == "teacher"
            }

        })
    }

    private fun checkPendingRequest(){
        mRootRef.child("Join-Class-Request/$classId").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG,"Error : ${p0.message}")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (isPendingRequestAccessed && p0.exists()){
                    invalidateOptionsMenu()
                }
            }

        })
    }

    private fun leaveClassroom() {
        val map = HashMap<String, String>()
        map["request"] = "accept"
        map["as"] = "leave"
        mRootRef.child("Join-Class-Request/$classId/${currentUser?.uid}").setValue(map).addOnSuccessListener {
            Log.d(TAG, "Request send")
            Toast.makeText(this, "Request to Leave", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Error : ${exception.message}")
            Toast.makeText(this, "Error : ${exception.message}", Toast.LENGTH_SHORT).show()
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
        map["senderId"] = currentUser?.uid.toString()
        map["visibility"] = visibility
        map["type"] = type
        map["time"] = dateTime.toString()

        val json = JSONObject(map).toString()

        mRootRef.child("Message/$classId/$date/$dateTime").setValue(json).addOnSuccessListener {

        }.addOnFailureListener { exception ->
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show()
            Log.d(TAG, "No internet connection : ${exception.message}")
        }
    }

    private fun getSendMessageFromDatabase() {
        mRootRef.child("Message/$classId").orderByKey().limitToLast(10).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "No Internet Connection")
                Toast.makeText(this@PublicChatActivity, "Error: ${error.message}", Toast.LENGTH_LONG).show()
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


                        if (map.visibility == "me" && map.senderId != currentUser?.uid) {
                            continue
                        }

                        if (map.type == "command") {
                            previous = "null"
                            map.viewType = MessageType.MY_COMMAND
                        } else {
                            if (map.senderId == currentUser?.uid) {
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

/*
    private fun addMessageBox(message: String,type:BoxType){
        val textView = TextView(this)
        textView.text = message
        textView.setTextColor(Color.parseColor("#000000"))
        val textViewParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        textViewParams.weight = 8f

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
        textView.layoutParams = textViewParams
        public_chat_list.addView(textView)
    }
*/

    private fun sendToMainActivity() {
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
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

    private fun sendToMembersActivity() {
        if (classId == "null") {
            sendToMainActivity()
            return
        }

        val intent = Intent(this,ClassMembersActivity::class.java)
        startActivity(intent)
    }

    private fun sendToAssignmentUploadActivity() {
        if (classId == "null") {
            sendToMainActivity()
            return
        }

        startActivity(Intent(this,AssignmentUploadActivity::class.java))
    }

    private fun sendToAssignmentActivity(){
        if (classId == "null") {
            sendToMainActivity()
            return
        }

        startActivity(Intent(this,AssignmentActivity::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.public_chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if(menu == null)
            return false

        menu.findItem(R.id.pending_request).isVisible = isPendingRequestAccessed

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item == null) return false

        when(item.itemId){
            R.id.pending_request -> {
                sendToPendingRequestActivity()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "chetan"
        private var userName = "null"

        private const val COMMAND_TAG = "."

        private val commandList = arrayListOf<String>(
                "whoami",
                "classname",
                "members",
                "slide",
                "new assignment",
                "assignment",
                "leave",
                "pending request"
        )


    }
}
