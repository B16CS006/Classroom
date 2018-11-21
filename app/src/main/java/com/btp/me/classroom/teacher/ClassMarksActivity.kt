package com.btp.me.classroom.teacher

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TableLayout
import com.btp.me.classroom.MainActivity.Companion.classId
import com.btp.me.classroom.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ClassMarksActivity : AppCompatActivity() {

    private var n:Int = 0
    private var m:Int = 0

    private val userMap = HashMap<String, ArrayList<String>>()
    private val assignmentMap = HashMap<String, ArrayList<String>>()

    private val mRootRef = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_marks)

        initialize()
    }

    private fun initialize(){}

    private fun getStudentList(){

        val map = HashMap<String, Map<String,HashMap<String,String>>>()

        mRootRef.child("Classroom/$classId/members").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children){
                    if (data.child("as").value == "student") {
                        val userMap = HashMap<String, HashMap<String,String>>()
                        val details = HashMap<String,String>()

                        details["rollNumber"] = data.child("rollNumber").value.toString()
                        details["name"] = data.child("name").value.toString()

                        userMap["details"] = details
                        map[data.key.toString()] = userMap
                    }
                }
            }
        })
    }

    private fun getAllUsers(){}        //make a array of userId to index the userId, array of map

    private fun getAllAssignment(){
        mRootRef.child("Assignment/$classId").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val table = ArrayList<ArrayList<Int>>()
                var i:Int = 0
                for (assignmentSnapshot in dataSnapshot.children){
                    val assignment = ArrayList<String>()
                    assignment.add(i.toString())
                    assignment.add(assignmentSnapshot.child(""))
                }

            }

        })
    }

    private fun showMarks(){

    }
}