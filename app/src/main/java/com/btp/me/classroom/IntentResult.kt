package com.btp.me.classroom

import android.content.Intent

class IntentResult{
    companion object {
        fun forPDF(): Intent{
            val intent = Intent()
            intent.type = "application/pdf"
            intent.action = Intent.ACTION_GET_CONTENT
            return intent
        }

        fun forImage(): Intent {
            val intent = Intent()
            intent.type = "application/image"
            intent.action = Intent.ACTION_GET_CONTENT
            return intent
        }
    }
}