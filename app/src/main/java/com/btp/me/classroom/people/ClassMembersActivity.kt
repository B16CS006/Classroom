package com.btp.me.classroom.people

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.btp.me.classroom.HomepageActivity
import com.btp.me.classroom.MainActivity
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_class_members.*
import kotlinx.android.synthetic.main.single_people_layout.view.*

class ClassMembersActivity: AppCompatActivity() {
//    private var classId:String? = null
    private val mRootRef = FirebaseDatabase.getInstance().reference
    private val databaseReference = mRootRef.child("Classroom/$classId/members")
    private val mCurrentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_members)

//        databaseReference.keepSynced(true)

        if(mCurrentUser == null){
            sendToHomepage()
            return
        }else if(classId == "null"){
            sendToMainActivity()
            return
        }


        peoples_teacher_list.layoutManager = LinearLayoutManager(this)
        peoples_students_list.layoutManager = LinearLayoutManager(this)

        peoples_teacher_list.setHasFixedSize(true)
        peoples_students_list.setHasFixedSize(true)

        val teachersList = ArrayList<HashMap<String,String>>()
        val studentsList = ArrayList<HashMap<String,String>>()

        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG,"Database Reference for People on data changed, ${p0.message}")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG,"OnDataChanged function : $dataSnapshot")

                teachersList.clear();studentsList.clear()

//                Log.d("chetan","Total Number of count : ${dataSnapshot.childrenCount.toString()}")

                for (people in dataSnapshot.children){
                    if (people.value == null)
                        continue

                    val map = HashMap<String,String>()
                    map["userId"] = people.key.toString()
                    map["as"] = people.child("as").value.toString()
                    map["name"] = people.child("name").value.toString()
                    map["rollNumber"] = people.child("rollNumber").value.toString()

                    if(mCurrentUser.uid == map["userId"])
                        map["who"] = "me"

                    when(map["as"]){
                        "teacher" -> teachersList.add(map)
                        "student" -> studentsList.add(map)
                    }
                }

                showList(teachersList, studentsList)
            }
        })
    }

    private fun showList(teachersList:List<HashMap<String, String>>, studentsList:List<HashMap<String, String>>){
        Log.d(TAG,"Total number of teacher : ${teachersList.size}")
        Log.d(TAG,"Total number of students : ${studentsList.size}")

        if(peoples_teacher_list != null && teachersList.isNotEmpty()){
            peoples_teacher_list.visibility = View.VISIBLE
            peoples_teacher_empty.visibility = View.GONE
        }else{
            peoples_teacher_list.visibility = View.GONE
            peoples_teacher_empty.visibility = View.VISIBLE
        }
        if(peoples_students_list != null && studentsList.isNotEmpty()){
            peoples_students_list.visibility = View.VISIBLE
            peoples_students_empty.visibility = View.GONE
        }else{
            peoples_students_list.visibility = View.GONE
            peoples_students_empty.visibility = View.VISIBLE
        }

        val teacherAdapter = object: RecyclerView.Adapter<PeopleViewHolder>(){
            override fun onCreateViewHolder(parent: ViewGroup, p1: Int): PeopleViewHolder {
//                Log.d(TAG, "Teacher adapter on create viewHolder")
                return PeopleViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.single_people_layout, parent, false))
            }

            override fun getItemCount() = teachersList.size

            override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
                holder.bind(teachersList[position])
            }

        }

        val studentAdapter = object : RecyclerView.Adapter<PeopleViewHolder>(){
            override fun onCreateViewHolder(parent: ViewGroup, p1: Int): PeopleViewHolder {
//                Log.d(TAG, "Student adapter on create viewHolder")
                return PeopleViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.single_people_layout, parent, false))
            }

            override fun getItemCount() = studentsList.size

            override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
                holder.bind(studentsList[position])
            }
        }

        peoples_teacher_list.adapter = teacherAdapter
        peoples_students_list.adapter = studentAdapter
    }

    class PeopleViewHolder(val view:View): RecyclerView.ViewHolder(view){

        fun bind(map: HashMap<String,String>){
            setName(map["name"]!!)
            setRollNumber(map["rollNumber"]!!)
            setCurrentUserDot(map["who"])
        }

        private fun setName(name:String){
            view.single_people_name.text = name
        }

        private fun setRollNumber(rollNumber:String){
            view.single_people_roll_number.text = rollNumber
        }

        private fun setCurrentUserDot(me:String?){
            if(me == "me")
                view.single_current_user_dot.visibility = View.VISIBLE
        }
    }

    private fun sendToMainActivity() {
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    private fun sendToHomepage() {
        startActivity(Intent(this,HomepageActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "chetan"
    }

}