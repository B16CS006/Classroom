package com.btp.me.classroom

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.btp.me.classroom.Class.ClassAttribute
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_join_class.*
import kotlinx.android.synthetic.main.single_classroom_layout.view.*

class JoinClass : AppCompatActivity() {

//    private val type = arrayOf("Student","Teacher")

    private val mRootRef = FirebaseDatabase.getInstance().reference
    private val mCurrentUser by lazy { FirebaseAuth.getInstance().currentUser }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_class)


        if(mCurrentUser == null){
            sendToHomepage()
            return
        }

        initialize()
    }

    override fun onStart() {
        super.onStart()

        val classList = ArrayList<ClassAttribute>()
        val classListAdapter = object : RecyclerView.Adapter<ClassViewHolder>(){
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ClassViewHolder {
                return ClassViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.single_classroom_layout, p0, false), applicationContext)
            }

            override fun getItemCount() = classList.size

            override fun onBindViewHolder(holder: ClassViewHolder, p: Int) {
                holder.bind(classList[p])
                holder.view.setOnClickListener{
                    getDialogBoxRollNumber(classList[p].id)
                }
            }

        }
        mRootRef.child("Classroom").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Error : ${p0.message}")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                classList.clear()
                for (data in dataSnapshot.children){
                    if(data.hasChild("name")){
                        if(data.hasChild("members") && data.child("members").hasChild(mCurrentUser!!.uid))
                            continue
                        val classAttribute = ClassAttribute(
                                data.key.toString(),
                                data.child("name").value.toString(),
                                data.child("status").value.toString(),
                                data.child("thumbImage").value?.toString() ?: data.child("image").value.toString()
                        )

                        classList.add(classAttribute)
                    }
                }

                if (classList.size == 0){
                    join_class_empty_list.visibility = View.VISIBLE
                    join_class_list.visibility = View.INVISIBLE
                }else{
                    join_class_empty_list.visibility = View.INVISIBLE
                    join_class_list.visibility = View.VISIBLE
                    join_class_list.adapter = classListAdapter
                }
            }

        })

    }

    private fun initialize(){
        title = "Join Class"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        join_class_list.setHasFixedSize(true)
        join_class_list.layoutManager = LinearLayoutManager(this)
    }

//    private fun extractDataFromView(): Detail {
//        val detail = Detail()
//        if(join_class_code.text.isBlank() || join_class_roll_number.text.isBlank()) {
//            detail.invalid = true
//            Toast.makeText(this,"Field can't be empty",Toast.LENGTH_SHORT).show()
//            return detail
//        }
//
//        detail.classId = join_class_code.text.toString()
//        detail.rollNumber = join_class_roll_number.text.toString().toUpperCase()
//        detail.invalid = false
//        return detail
//    }


    private fun getDialogBoxRollNumber(id:String){
        val rollNumberEditText = EditText(this)
        with(rollNumberEditText) {
            hint = "Roll Number"
            setEms(5)
            maxEms = 10
            minEms = 2
            gravity = View.TEXT_ALIGNMENT_CENTER
            inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        }

        val alertDialog = AlertDialog.Builder(this)

        with(alertDialog){
            setTitle("Enter Roll Number")
            setView(rollNumberEditText)
            alertDialog.setPositiveButton("Join") { _: DialogInterface, _: Int -> }
            alertDialog.setNegativeButton("Cancel"){_: DialogInterface,_: Int -> }
        }

        val dialog = alertDialog.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val rollNumber = rollNumberEditText.text.toString()
            if(rollNumber.isNotEmpty()){
                joinClass(id, rollNumber)
                dialog.dismiss()
            }else{
                rollNumberEditText.error = "Can't be Empty"
            }
        }

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener{
            dialog.cancel()
        }
    }

    private fun joinClass(id:String, rollNumber:String){
        val map = HashMap<String, String>()
        map["as"] = "student"
        map["request"] = "pending"
        map["rollNumber"] = rollNumber
        map["name"] = mCurrentUser!!.displayName.toString()
        mRootRef.child("Join-Class-Request/$id/${mCurrentUser!!.uid}").setValue(map).addOnSuccessListener {
            Log.d(TAG, "Request send")
            Toast.makeText(this@JoinClass, "Request send", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Error : ${exception.message}")
            Toast.makeText(this@JoinClass, "Error : ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendToHomepage(): FirebaseUser? {
        val intent = Intent(this, HomepageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
        return null
    }


//    private class Detail(
//        var classId:String = "",
//        var rollNumber:String = "",
//        var name: String = "",
//        var invalid:Boolean = false
//    )

    private class ClassViewHolder(val view:View, val ctx: Context): RecyclerView.ViewHolder(view){
        fun bind(classAttribute: ClassAttribute){
            setImage(classAttribute.profileImage)
            setName(classAttribute.name)
            setStatus(classAttribute.status)
            view.class_single_registered_as.visibility = View.INVISIBLE
        }

        private fun setName(name:String){ view.class_single_name.text = name }
        private fun setStatus(status: String){ view.class_single_status.text = status }
        private fun setImage(image:String){
            val glideImage:Any = when(image){"default","null", "" -> R.drawable.ic_classroom else -> image}
            Glide.with(ctx).load(glideImage).into(view.class_single_image)
        }

    }

    companion object {
        private const val TAG = "Join_Class"
    }
}
