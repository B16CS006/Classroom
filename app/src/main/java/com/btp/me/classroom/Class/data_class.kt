package com.btp.me.classroom.Class

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException

class ClassAttribute {
    var id:String = ""
    var name:String = ""
    var status:String = ""
    var profileImage:String = ""
    var registeredAs = ""
}

//class Assignment{
//    var title:String = "default"
//    var description:String = ""
//    var submissionDate:Long = 0
//    var maxMarks:Int = 100
//}

abstract class MessageType {
    companion object {
//        const val NONE = 0
        const val MY_MESSAGE = 1
        const val MY_FIRST_MESSAGE = 2
        const val OTHER_MESSAGE = 3
        const val OTHER_FIRST_MESSAGE = 4
        const val MY_COMMAND = 5
        const val DATE = 6
    }
}

abstract class MyColor{
    companion object {
        val colorCode = arrayListOf<String>("#FFA631","#5D8CAE","#008000","#FFFF00","#00FFFF","#875F9A","#2ABB9B","#A17917","#C93756","#FA8072")
        val colorName = arrayListOf<String>("orange","ultraMarine","green","yellow","aqua","purple","jungleGreen","brown","crimson","salmon")

        val colorCount = colorCode.size

        fun chooseColor(string:String): String{
            var sum = 0
            for(ch in string){
                sum += ch.toInt()
            }
            return colorCode[sum% colorCount] // colorCode[sumOfDigits(sum)]
        }

        private fun sumOfDigits(number:Int):Int{
            var result:Int
            if(number <0)
                result = -number
            else
                result = number

            while (result>= colorCount) {
                var temp = 0
                while (result > 0) {
                    temp += result % 10
                    result /= 10
                }
                result = temp
            }
            return result
        }
    }
}

abstract class FileBuilder{
    companion object {
        fun createFile(title: String): File? {
            try {
                Log.d("chetan",title)

                var fileName = File(Environment.getExternalStorageDirectory().toString(), "Classroom")

                if (!fileName.exists()) {
                    Log.d("chetan", "Directory ${File.separator} not exist : $fileName")
                    fileName.mkdir()
                }
                fileName = File(fileName.toString(), "media")
                if (!fileName.exists()) {
                    Log.d("chetan", "Directory ${File.separator} not exist : $fileName")
                    fileName.mkdir()
                }
                fileName = File(fileName.toString(), "pdf")
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
    }
}

data class ChatMessage(var senderId:String = "...",var visibility:String = "...", var time:String = "0", var message:String = "...", var type:String = "...",  var senderName:String = "...", var viewType:Int)

data class Assignment(
    var title: String = "",
    var description: String = "",
    var submissionDate: String = "",
    var uploadingDate: String = "",
    var maxMarks: String = "",
    var link: String? = null
)

data class StudentAssignmentDetails(
        var link: String? = null,
        var marks: String? = null,
        var name: String? = null,
        var rollNumber: String? = null,
        var state: String? = null,
        var userId: String? = null,
        var registeredAs: String? = null
)