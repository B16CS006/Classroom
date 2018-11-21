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

    private var n: Int = 0
    private var m: Int = 0

    private var isTableComplete: Boolean = false

    private val userMap = HashMap<String, ArrayList<String>>()
    private val assignmentMap = HashMap<String, ArrayList<String>>()

    private val table = ArrayList<ArrayList<String>>()

    private val mRootRef = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_marks)

        initialize()
    }

    private fun initialize() {}

    private fun getStudentList() {

        val map = HashMap<String, Map<String, HashMap<String, String>>>()

        mRootRef.child("Classroom/$classId/members").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {
                    if (data.child("as").value == "student") {
                        val userMap = HashMap<String, HashMap<String, String>>()
                        val details = HashMap<String, String>()

                        details["rollNumber"] = data.child("rollNumber").value.toString()
                        details["name"] = data.child("name").value.toString()

                        userMap["details"] = details
                        map[data.key.toString()] = userMap
                    }
                }
            }
        })
    }

    private fun getAllUsers() {}        //make a array of userId to index the userId, array of map


    private fun getAllAssignment() {
        mRootRef.child("Assignment/$classId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for ((i, assignmentSnapshot) in dataSnapshot.children.withIndex()) {
                    val assignment = ArrayList<String>()
                    assignment.add(i.toString())
                    assignment.add(assignmentSnapshot.child("title").value.toString())
                    assignment.add(assignmentSnapshot.child("maxMarks").value.toString())

                    assignmentMap[assignmentSnapshot.key.toString()] = assignment

                    mRootRef.child("Classroom/$classId/members").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(dataSnapshot2: DataSnapshot) {
                            for ((k, member) in dataSnapshot2.children.withIndex()) {
                                if (member.child("as").value.toString() == "student") {
                                    val arrayList = ArrayList<String>()
                                    arrayList.add(k.toString())
                                    arrayList.add(member.child("name").value.toString())
                                    arrayList.add(member.child("rollNumber").value.toString())
                                    userMap[member.key.toString()] = arrayList

                                    val j = userMap[member.key]!![0].toInt()

                                    val marks = assignmentSnapshot.child("marks/${member.key.toString()}/marks").value.toString()

                                    if (marks != "null")
                                        table[j].add(marks)
                                    else
                                        table[j].add(0.toString())
                                }
                            }

                            mRootRef.child("Examination/$classId").addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun onDataChange(dataSnapshot3: DataSnapshot) {

                                    for ((i, examSnapshot) in dataSnapshot3.children.withIndex()) {
                                        val exam = ArrayList<String>()
                                        exam.add(i.toString())
                                        exam.add(examSnapshot.child("title").value.toString())
                                        exam.add(examSnapshot.child("maxMarks").value.toString())

                                        assignmentMap[examSnapshot.key.toString()] = exam

                                        mRootRef.child("Classroom/$classId/members").addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onCancelled(dataSnapshot4: DatabaseError) {

                                            }

                                            override fun onDataChange(dataSnapshot4: DataSnapshot) {
                                                for ((k, member2) in dataSnapshot4.children.withIndex()) {
                                                    if (member2.child("as").value.toString() == "student") {
                                                        val arrayList = ArrayList<String>()
                                                        arrayList.add(k.toString())
                                                        arrayList.add(member2.child("name").value.toString())
                                                        arrayList.add(member2.child("rollNumber").value.toString())
                                                        userMap[member2.key.toString()] = arrayList

                                                        val j = userMap[member2.key]!![0].toInt()

                                                        val marks = examSnapshot.child("marks/${member2.key.toString()}/marks").value.toString()

                                                        if (marks != "null")
                                                            table[j].add(marks)
                                                        else
                                                            table[j].add(0.toString())
                                                    }
                                                }

                                                isTableComplete = true
                                            }
                                        })
                                    }
                                }

                            })
                        }
                    })
                }
            }

        })
    }
}