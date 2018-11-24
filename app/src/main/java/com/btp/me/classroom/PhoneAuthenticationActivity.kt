package com.btp.me.classroom

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.google.firebase.FirebaseException
import kotlinx.android.synthetic.main.activity_phone_authentication.*
import java.util.concurrent.TimeUnit


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider


class PhoneAuthenticationActivity : AppCompatActivity(), View.OnClickListener {
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

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onverification completed")
                phone_authentication_help.text = "Verified successfully"
                mVerificationInProgress = false
                updateUI(STATE_VERIFY_SUCCESS, credential)
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                Log.d(TAG, "Verification Failed : $exception")
                mVerificationInProgress = false
                if (exception is FirebaseAuthInvalidCredentialsException) {
                    phone_authentication_phone.error = "Invalid Phone Number"
                    phone_authentication_help.text = "Invalid Phone Number"
                    updateUI(STATE_INITIALIZED)
                    return
                }
                phone_authentication_help.text = "Verification Failed"
                updateUI(STATE_VERIFY_FAILED)
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "onCodeSent : $verificationId")
                Log.d(TAG, "onCodeSent : $token")

                phone_authentication_help.text = "Code Sent"
                mVerificationId = verificationId
                mResendToken = token
                updateUI(STATE_CODE_SENT)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        updateUI(mAuth.currentUser)

        Log.d(TAG, "onStart $mVerificationInProgress")

        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(phone_authentication_country_code.text.toString() + phone_authentication_phone.text.toString())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress)
        Log.d(TAG, "onSave $mVerificationInProgress")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
        Log.d(TAG, "Onrestore : $mVerificationInProgress")
    }

    private fun verifyPhoneNumberWithOTP(verificationId: String, otp: String) {
        Log.d(TAG, "verifyphonenumberwith otp : $verificationId, $otp")
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        Log.d(TAG, "startPhoneNumber Verification")
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        )
        mVerificationInProgress = true

        Log.d(TAG, "start phoneNumber verification ends")
    }

    private fun resendVerificationCode(phoneNumber: String, token: PhoneAuthProvider.ForceResendingToken) {
        Log.d(TAG, "resendverificaion;")
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                token
        )
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Log.d(TAG, "singinwith auth crediential")
        mAuth.signInWithCredential(credential).addOnSuccessListener { it ->
            phone_authentication_help.text = "Sign In Successful"
            updateUI(STATE_SIGNIN_SUCCESS, it.user)
        }.addOnFailureListener { exception ->
            if (exception is FirebaseAuthInvalidCredentialsException) {
                phone_authentication_otp.error = "Invalid Code."
                phone_authentication_help.text = "Invalid Code"
            }
            updateUI(STATE_SIGNIN_FAILED)
        }
    }

    private fun updateUI(state: Int) {
        Log.d(TAG, "updateUi 1 : $state")
        updateUI(state, mAuth.currentUser, null)
    }

    private fun updateUI(user: FirebaseUser?) {
        Log.d(TAG, "updateUI 2, user $user")
        if (user == null)
            updateUI(STATE_INITIALIZED)
        else
            updateUI(STATE_SIGNIN_SUCCESS, user)
    }

    private fun updateUI(state: Int, user: FirebaseUser?) {
        Log.d(TAG, "updateUi 3 : $state, $user")
        updateUI(state, user, null)
    }

    private fun updateUI(state: Int, credential: PhoneAuthCredential) {
        Log.d(TAG, "updateui 4 : $state, $credential")
        updateUI(state, null, credential)
    }

    private fun updateUI(state: Int, user: FirebaseUser?, cred: PhoneAuthCredential?) {
        Log.d(TAG, "updateUi 5 : $state, $user, $cred")
        when (state) {
            STATE_INITIALIZED -> {
                phone_authentication_otp_constraint_layout.visibility = View.GONE
                phone_authentication_phone_constraint_layout.visibility = View.VISIBLE
                phone_authentication_phone_constraint_layout.isEnabled = true
                phone_authentication_help.text = null
//                phone_authentication_otp.text = null
            }

            STATE_CODE_SENT -> {
                phone_authentication_otp_constraint_layout.visibility = View.VISIBLE
                phone_authentication_phone_constraint_layout.visibility = View.GONE
                phone_authentication_otp_constraint_layout.isEnabled = true
                phone_authentication_help.text = "Code Sent"
            }

            STATE_VERIFY_FAILED -> {
                phone_authentication_otp_constraint_layout.visibility = View.VISIBLE
                phone_authentication_phone_constraint_layout.visibility = View.GONE
                phone_authentication_otp_constraint_layout.isEnabled = true
                phone_authentication_help.text = "Verify Failed"
            }

            STATE_VERIFY_SUCCESS -> {
                phone_authentication_phone_constraint_layout.visibility = View.GONE
                phone_authentication_otp_constraint_layout.visibility = View.VISIBLE
                phone_authentication_otp_constraint_layout.isEnabled = false
                phone_authentication_help.text = "Verification succeeded"

                if (cred != null) {
                    if (cred.smsCode != null) {
                        phone_authentication_otp.setText(cred.smsCode)
                    } else {
                        phone_authentication_otp.setText("(instant validation)")
                    }
                }
            }

            STATE_SIGNIN_FAILED -> {
                phone_authentication_help.text = "Sign-in failed"
            }

            STATE_SIGNIN_SUCCESS -> {
            }
        }

        if (user == null) {
            phone_authentication_sign_out.text = "Signed Out"
        } else {
            phone_authentication_sign_out.text = "Signed In"
            sendToRegister()
        }
    }

    private fun validatePhoneNumber(): Boolean {
        Log.d(TAG, "Validate Phone Number")
        val phoneNumber = phone_authentication_phone.text.toString()
        if (TextUtils.isEmpty(phoneNumber)) {
            phone_authentication_phone.error = "Invalid phone number."
            return false
        }

        return true
    }

    private fun sendToRegister() {
        val mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    override fun onClick(view: View?) {
        if (view == null) {
            Log.d(TAG, "clicked view is null")
            return
        }

        Log.d(TAG, "On click ${view.id}")

        when (view.id) {
            R.id.phone_authentication_send_button -> {
                if (validatePhoneNumber()) {
                    startPhoneNumberVerification(phone_authentication_country_code.text.toString() + phone_authentication_phone.text.toString())
                }
            }

            R.id.phone_authentication_change_phone -> {
                mVerificationInProgress = false
                updateUI(STATE_INITIALIZED)
            }

            R.id.phone_authentication_resend -> {
                if (validatePhoneNumber()) {
                    resendVerificationCode(phone_authentication_country_code.text.toString() + phone_authentication_phone.text.toString(), mResendToken)
                }
            }

            R.id.phone_authentication_verify -> {
                if (phone_authentication_otp.text.isBlank()) {
                    phone_authentication_otp.error = "field can't be empty"
                    return
                }
                verifyPhoneNumberWithOTP(mVerificationId, phone_authentication_otp.text.toString())
            }
        }
    }


    private fun onClick() {

        val listenerImplement = true

        if(listenerImplement) {

            phone_authentication_send_button.setOnClickListener(this)
            phone_authentication_change_phone.setOnClickListener(this)
            phone_authentication_resend.setOnClickListener(this)
            phone_authentication_verify.setOnClickListener(this)
        }else {

            phone_authentication_send_button.setOnClickListener {
                if (validatePhoneNumber()) {
                    startPhoneNumberVerification(phone_authentication_country_code.text.toString() + phone_authentication_phone.text.toString())
                }
            }
            phone_authentication_change_phone.setOnClickListener {
                mVerificationInProgress = false
                updateUI(STATE_INITIALIZED)
            }
            phone_authentication_resend.setOnClickListener {
                if (validatePhoneNumber()) {
                    resendVerificationCode(phone_authentication_country_code.text.toString() + phone_authentication_phone.text.toString(), mResendToken)
                }
            }
            phone_authentication_verify.setOnClickListener {
                if (phone_authentication_otp.text.isBlank()) {
                    phone_authentication_otp.error = "field can't be empty"
                    return@setOnClickListener
                }
                verifyPhoneNumberWithOTP(mVerificationId, phone_authentication_otp.text.toString())
            }
        }
    }

    companion object {
        private const val TAG = "Phone_Authentication"
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"

        private const val STATE_INITIALIZED = 1
        private const val STATE_CODE_SENT = 2
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_SIGNIN_FAILED = 5
        private const val STATE_SIGNIN_SUCCESS = 6
    }
}