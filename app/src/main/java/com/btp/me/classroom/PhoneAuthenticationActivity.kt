package com.btp.me.classroom

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_phone_authentication.*
import java.util.concurrent.TimeUnit

class PhoneAuthenticationActivity : AppCompatActivity() {

    private lateinit var mVerificationId: String
    private lateinit var mResendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var mVerificationInProgress = false

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_authentication)

        onClick()

        mAuth = FirebaseAuth.getInstance()

        mCallbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                phone_authentication_help.text = "Verified successfully"
                mVerificationInProgress = false
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                Log.d(TAG,"Verification Failed : $exception")
                mVerificationInProgress = false
                if(exception is FirebaseAuthInvalidCredentialsException){

                    viewLayout(ViewLayoutEnum.PHONE)
                    phone_authentication_phone.error = "Invalid Phone Number"
                    phone_authentication_help.text = "Invalid Phone Number"

                    return
                }
                phone_authentication_help.text = "Verification Failed"
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG,"onCodeSent : $verificationId")
                phone_authentication_help.text = "Code Sent"
                mVerificationId = verificationId
                mResendToken = token
            }
        }

    }

    override fun onStart() {
        super.onStart()
        if(mAuth.currentUser != null){
            sendToRegister()
        }else if(mVerificationInProgress && phone_authentication_phone.text.isNotBlank()){
            startPhoneNumberVerification(phone_authentication_country_code.text.toString() + phone_authentication_phone.text.toString())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    private fun viewLayout(layout:ViewLayoutEnum) {

        when(layout){
            ViewLayoutEnum.PHONE -> {
                phone_authentication_otp_constraint_layout.visibility = View.GONE
                phone_authentication_phone_constraint_layout.visibility = View.VISIBLE
            }
            ViewLayoutEnum.OTP -> {
                phone_authentication_phone_constraint_layout.visibility = View.GONE
                phone_authentication_otp_constraint_layout.visibility = View.VISIBLE
            }
        }
    }

    private fun verifyPhoneNumberWithOTP(verificationId: String, otp: String){
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun startPhoneNumberVerification(phoneNumber:String){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                30,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        )

        mVerificationInProgress = true
    }

    private fun resendVerificationCode(phoneNumber: String, token: PhoneAuthProvider.ForceResendingToken){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                30,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                token
        )
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential){
        mAuth.signInWithCredential(credential).addOnSuccessListener {it->
            phone_authentication_help.text = "Sign In Successful"
            sendToRegister()
        }.addOnFailureListener { exception ->
            if(exception is FirebaseAuthInvalidCredentialsException) {
                phone_authentication_otp.error = "Invalid Code."
                phone_authentication_help.text = "Invalid Code"
            }else{
                phone_authentication_help.text = "Sign In Failed"
            }
        }
    }

    private fun sendToRegister() {
        val mainIntent = Intent(this, RegisterActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    private fun onClick() {
        phone_authentication_send_button.setOnClickListener {
            if (phone_authentication_phone.text.isBlank()) {
                phone_authentication_phone.error = "field can't be empty"
                phone_authentication_help.text = "Error : Phone Number not Provided"
            }else if(phone_authentication_phone.text.toString().length != 10){
                phone_authentication_phone.error = "Invalid Phone Number"
                phone_authentication_help.text = "Invalid Phone Number"
            }else{
                startPhoneNumberVerification(phone_authentication_country_code.text.toString() + phone_authentication_phone.text.toString())
                viewLayout(ViewLayoutEnum.OTP)
            }
        }

        phone_authentication_change_phone.setOnClickListener {
            viewLayout(ViewLayoutEnum.PHONE)
            mVerificationInProgress = false
        }

        phone_authentication_resend.setOnClickListener {
            if (phone_authentication_phone.text.isBlank()) {
                phone_authentication_phone.error = "field can't be empty"
                phone_authentication_help.text = "Error : Phone Number not Provided"
            } else {
                resendVerificationCode(phone_authentication_country_code.text.toString() + phone_authentication_phone.text.toString(), mResendToken)
            }
        }

        phone_authentication_verify.setOnClickListener {
            if (phone_authentication_otp.text.isBlank()) {
                phone_authentication_otp.error = "field can't be empty"
                phone_authentication_help.text = "Error : OTP not Provided"
            } else {
                verifyPhoneNumberWithOTP(mVerificationId,phone_authentication_otp.text.toString())
            }
        }
    }

    companion object {
        private const val TAG = "Phone Authentication"
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private enum class ViewLayoutEnum{PHONE,OTP}
    }
}
