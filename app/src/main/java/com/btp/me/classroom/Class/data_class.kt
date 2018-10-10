package com.btp.me.classroom.Class


//class ClassroomKotlin(var name:String, var status:String, var profileImage:String)

class ClassroomKotlin {

    var name:String = "default"
    var status:String = "default"
    var profileImage:String = "default"
}

abstract class MessageType {
    companion object {
//        const val NONE = 0
        const val MY_MESSAGE = 1
        const val MY_FIRST_MESSAGE = 2
        const val OTHER_MESSAGE = 3
        const val OTHER_FIRST_MESSAGE = 4
        const val MY_COMMAND = 5
    }
}

data class Slide(var title:String = "", var link:String = "")
data class ChatMessage(val senderId:String = "...",  val visibility:String = "...", val time:String = "...", val message:String = "...", val type:String = "...",  val senderName:String = "...", var viewType:Int)