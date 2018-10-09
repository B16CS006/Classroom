package com.btp.me.classroom.Class


//class ClassroomKotlin(var name:String, var status:String, var profileImage:String)

class ClassroomKotlin {

    var name:String = "default"
    var status:String = "default"
    var profileImage:String = "default"
}

data class Slide(var title:String = "", var link:String = "")
data class ChatMessage(val senderId:String = "...",  val visibility:String = "...", val time:String = "...", val message:String = "...", val type:String = "...",  val senderName:String = "...")