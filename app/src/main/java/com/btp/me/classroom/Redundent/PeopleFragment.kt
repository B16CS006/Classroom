//package com.btp.me.classroom.Redundent
//
//import android.os.Bundle
//import android.support.v4.app.Fragment
//import android.support.v7.widget.RecyclerView
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import com.btp.me.classroom.ClassHomeActivity
//import com.btp.me.classroom.R
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ValueEventListener
//import kotlinx.android.synthetic.main.activity_class_members.*
//
//open class PeopleFragment : Fragment() {
//
//    private var classId : String? = null
//    private val mRootRef = FirebaseDatabase.getInstance().reference
//    private var mCurrentUser: FirebaseUser? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
//                              savedInstanceState: Bundle?): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.activity_class_members, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//
//        mCurrentUser = FirebaseAuth.getInstance().currentUser
//        val context = activity as ClassHomeActivity
//        classId = context.classId
//        if(mCurrentUser == null){
////            sendToHomepage()
//            return
//        }else if(classId == null || classId == "null"){
////            sendToMainActivity()
//            return
//        }
//
//        peoples_teacher_list.setHasFixedSize(true)
//        peoples_students_list.setHasFixedSize(true)
//
//        val teachersList = ArrayList<HashMap<String,String>>()
//        val studentsList = ArrayList<HashMap<String,String>>()
//
//
//
//        mRootRef.child("Classroom/$classId/members").addValueEventListener(object :ValueEventListener{
//            override fun onCancelled(p0: DatabaseError) {
//                Log.d(TAG,"Database Reference for People on data changed, ${p0.message}")
//            }
//
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                Log.d(TAG,"OnDataChanged function : $dataSnapshot")
//
//                teachersList.clear();studentsList.clear()
//
////                Log.d("chetan","Total Number of count : ${dataSnapshot.childrenCount.toString()}")
//
//                var i=1L;
//                for (people in dataSnapshot.children){
//                    Log.d(TAG,"People : $people")
//                    val userId = people?.key ?: continue
//                    val type = people.value.toString()
//
////                    Log.d(TAG,"userId : $userId")
////                    Log.d(TAG,"type : $type")
//
//                    mRootRef.child("Users/$userId/name").addListenerForSingleValueEvent(object : ValueEventListener{
//                        override fun onCancelled(p0: DatabaseError) {
//                            Log.d(TAG,"single event Listener failed, Failed error : ${p0.message}")
//                        }
//
//                        override fun onDataChange(dataSnapshot2: DataSnapshot) {
//                            Log.d(TAG,"The People data is : $dataSnapshot2")
//                            val userName = dataSnapshot2.value?.toString() ?: return
//
////                            Log.d(TAG,"UserName : $userName")
//
//                            val map = HashMap<String,String>()
//                            map["userName"] = userName
//                            map["userId"] = userId
//
//                            if(type == "teacher"){
//                                teachersList.add(map)
//                            }else if(type == "student"){
//                                studentsList.add(map)
//                            }
//
//                            if (dataSnapshot.childrenCount == i){
////                                Log.d("chetan","list size ${teachersList.size} and student size : ${studentsList.size}")
//                                showList(teachersList, studentsList)
//                            }else{
//                                i++
//                            }
//                        }
//
//                    })
//                }
//            }
//        })
//
//    }
//
//    private fun showList(teachersList:List<HashMap<String, String>>, studentsList:List<HashMap<String, String>>){
//        Log.d(TAG,"Total number of teacher : ${teachersList.size}")
//        Log.d(TAG,"Total number of students : ${studentsList.size}")
//
//        if(peoples_teacher_list != null && teachersList.size !=0){
//            peoples_teacher_list.visibility = View.VISIBLE
//            peoples_teacher_empty.visibility = View.GONE
//        }else{
//            peoples_teacher_list.visibility = View.GONE
//            peoples_teacher_empty.visibility = View.VISIBLE
//        }
//        if(peoples_students_list != null && studentsList.size !=0){
//            peoples_students_list.visibility = View.VISIBLE
//            peoples_students_empty.visibility = View.GONE
//        }else{
//            peoples_students_list.visibility = View.GONE
//            peoples_students_empty.visibility = View.VISIBLE
//        }
//
//        val teacherAdater = object: RecyclerView.Adapter<PeopleViewHolder>(){
//            override fun onCreateViewHolder(parent: ViewGroup, p1: Int): PeopleViewHolder {
////                Log.d(TAG, "Teacher adapter on create viewHolder")
//                return PeopleViewHolder(LayoutInflater.from(parent.context)
//                        .inflate(R.layout.single_people_layout, parent, false))
//            }
//
//            override fun getItemCount() = teachersList.size
//
//            override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
////                Log.d(TAG,"Teacher Adapter on Bind View Holder")
////                Log.d("chetan" ,"Teacher name  is : ${teachersList[position]["name"]}")
//
//                if(mCurrentUser?.uid == teachersList[position]["userId"])
//                    holder.current_user_dot.visibility = View.VISIBLE
//                holder.name.text = teachersList[position]["userName"]
//            }
//
//        }
//
//        val studentAdapter = object :RecyclerView.Adapter<PeopleViewHolder>(){
//            override fun onCreateViewHolder(parent: ViewGroup, p1: Int): PeopleViewHolder {
////                Log.d(TAG, "Student adapter on create viewHolder")
//                return PeopleViewHolder(LayoutInflater.from(parent.context)
//                        .inflate(R.layout.single_people_layout, parent, false))
//            }
//
//            override fun getItemCount() = studentsList.size
//
//            override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
////                Log.d(TAG,"Students Adapter on Bind View Holder")
//                if(mCurrentUser?.uid == studentsList[position]["userId"])
//                    holder.current_user_dot.visibility = View.VISIBLE
//                holder.name.text = studentsList[position]["userName"]
//            }
//        }
//
//        peoples_teacher_list.adapter = teacherAdater
//        peoples_students_list.adapter = studentAdapter
//    }
//
//
//    companion object {
//        private const val TAG = "chetan"
//    }
//
//    class PeopleViewHolder(view:View): RecyclerView.ViewHolder(view){
//        val name:TextView = view.findViewById(R.id.single_people_name)
//        val current_user_dot:ImageView = view.findViewById(R.id.single_current_user_dot)
//    }
//}
