package com.btp.me.classroom.Class

import android.util.Log


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

abstract class MyColor{
    companion object {
        val colorCode = arrayListOf<String>("#FFA631","#5D8CAE","#008000","#FFFF00","#00FFFF","#875F9A","#2ABB9B","#A17917","#C93756","#FA8072")
        val colorName = arrayListOf<String>("orange","ultraMarine","green","yellow","aqua","purple","jungleGreen","brown","crimson","salmon")
        fun chooseColor(string:String): String{
            var sum = 0
            for(ch in string){
                sum += ch.toInt()
            }
            if(sum<0)
                sum = -sum

            while(sum>9){
                sum = sumOfDigits(sum)
            }

            return colorCode[sum%10]
        }

        private fun sumOfDigits(number:Int):Int{
            var num = number
            var result = 0
            while(num > 0){
                result += num%10
                num /= 10
            }
//            Log.d("sum","num : $number and result : $result")
            return result
        }
    }
}

data class Slide(var title:String = "", var link:String = "")
data class ChatMessage(val senderId:String = "...",  val visibility:String = "...", val time:String = "...", val message:String = "...", val type:String = "...",  val senderName:String = "...", var viewType:Int)