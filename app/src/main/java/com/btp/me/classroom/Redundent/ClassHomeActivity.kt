//package com.btp.me.classroom
//
//import android.content.Intent
//import android.support.v7.app.AppCompatActivity
//import android.os.Bundle
//import android.util.Log
//import com.btp.me.classroom.adapter.ClassHomeSelectionAdapter
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.*
//import kotlinx.android.synthetic.main.activity_class_home.*
//
//class ClassHomeActivity : AppCompatActivity() {
//
//    private  var mCurrentUser = FirebaseAuth.getInstance().currentUser
//    private val mRootRef = FirebaseDatabase.getInstance().reference
//
//    var classId : String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_class_home)
//
//        classId = intent.getStringExtra("classId")
//
//        if(classId == null || classId == "null"){
//            sendToMainActivity()
//            return
//        }
//
//        check()
//
//
//        mRootRef.child("Classroom/$classId/name").addValueEventListener(object :ValueEventListener{
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                title = dataSnapshot.value.toString()
//                if (title == "null") {
//                    finish()
//                }
//            }
//            override fun onCancelled(p0: DatabaseError) {
//                Log.d("chetan","Data is cancelled ${p0.message}")
//            }
//        })
//
//        if(mCurrentUser == null){
//            sendToHomepage()
//            return
//        }
//
//        val classHomeSelectionAdapter = ClassHomeSelectionAdapter(supportFragmentManager)
//        class_home_view_pager.adapter = classHomeSelectionAdapter
//        class_home_tab_layout.setupWithViewPager(class_home_view_pager)
//    }
//
//    override fun onStart() {
//        super.onStart()
//
//    }
//
//    private fun checkPendingRequest(){
//    }
//
//    private fun sendToHomepage() {
//        Log.d("chetan","User is null")
//        startActivity(Intent(this, HomepageActivity::class.java))
//        finish()
//    }
//
//    private fun sendToMainActivity(){
//        Log.d("chetan","Somehow this class is deleted so we have no reference to this class")
//        startActivity(Intent(this,MainActivity::class.java))
//        finish()
//    }
//
//    private fun check() {
//        val currentUser = mCurrentUser?.uid
//
//        if(currentUser == null){ sendToHomepage(); return}
//        else if(classId == "null"){ sendToMainActivity(); return}
//
//        mRootRef.child("Class-Enroll/$currentUser/$classId/as").addValueEventListener(object : ValueEventListener{
//            override fun onCancelled(p0: DatabaseError) {
//                Log.d("chetan","Check Internet connection : ${p0.message}")
//            }
//
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//
//                if(dataSnapshot.value != null && dataSnapshot.value == "teacher"){
//                    Log.d("chetan",dataSnapshot.value.toString())
//                    class_home_floating_button.show()
//                }else{
//                    Log.d("chetan","hide")
//                    class_home_floating_button.hide()
//                }
//            }
//
//        })
//    }
//
////    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
////        if(resultCode == Activity.RESULT_OK && requestCode == 0 && data != null && data.data != null){
////            val uri = data.data?: return
////            upload(uri)
////        }
////        else{
////            Toast.makeText(this,"PDF can't be retrieve.", Toast.LENGTH_LONG).show()
////        }
////        super.onActivityResult(requestCode, resultCode, data)
////    }
////
////    private fun upload(uri: Uri) {
////            Log.d("chetan","uploading Uri : ${uri.toString()}")
////            val uploadingIntent = Intent(this,MyUploadingService::class.java)
////            uploadingIntent.putExtra("classId",classId)
////            uploadingIntent.putExtra("userId",mCurrentUser!!.uid)
////            uploadingIntent.putExtra("fileUri",uri)
////            uploadingIntent.action = MyUploadingService.ACTION_UPLOAD
////            startService(uploadingIntent)
////    }
//}
