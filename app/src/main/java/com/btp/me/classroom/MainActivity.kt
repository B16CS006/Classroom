package com.btp.me.classroom

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import com.btp.me.classroom.Class.ClassroomKotlin
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.classroom_single_layout.view.*

class MainActivity : AppCompatActivity() {

    private var mCurrentUser: FirebaseUser? = null
    private lateinit var mClassroomReference: DatabaseReference
    private lateinit var mClassEnrollReference: DatabaseReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mCurrentUser = FirebaseAuth.getInstance().currentUser

        if (mCurrentUser == null) {
            sendToHomePage()
            return
        }

//        Toast.makeText(this,"Welcome ${mCurrentUser!!.displayName}!",Toast.LENGTH_LONG).show()

        Log.d("chetan", "MainActivity : User : ${mCurrentUser!!.uid}")
        mClassroomReference = FirebaseDatabase.getInstance().getReference("Classroom")
        mClassEnrollReference = FirebaseDatabase.getInstance().getReference("Class-Enroll").child(mCurrentUser!!.uid)

//        mClassroomReference.keepSynced(true)
//        mClassEnrollReference.keepSynced(true)


        main_class_list.setHasFixedSize(true)
        main_class_list.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        if (mCurrentUser == null) {
            sendToHomePage()
            return
        }

//        Log.d("chetan", "MainActivity or of 4 and 5 : " + (12 or 3) + " and " + 4)

        main_create_class.setOnClickListener { sendToCreateClassActivity() }

        val options = FirebaseRecyclerOptions.Builder<ClassroomKotlin>()
                .setQuery(mClassEnrollReference, ClassroomKotlin::class.java)
                .setLifecycleOwner(this)
                .build()

        Log.d("chetan", "Options: ${options.snapshots}")

//        val list = ArrayList<Classes>()
//        val x = Classes("Name", "Status", "https://firebasestorage.googleapis.com/v0/b/classroom-a9b2e.appspot.com/o/profile_images%2F2bo3A3wADsbxJlep6MZqvI7LN283.jpg?alt=media&token=113d6f39-7d2f-49e5-8c3a-0d66cafd3d9c")
//        list.add(x)
//        list.add(x)
//        list.add(x)
//        list.add(x)
//
//        val adapter2 = object : RecyclerView.Adapter<ClassViewHolder>() {
//            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
//                return ClassViewHolder(LayoutInflater.from(parent.context)
//                        .inflate(R.layout.classroom_single_layout, parent, false))
//            }
//
//            override fun getItemCount(): Int {
//                return list.size
//            }
//
//            override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
//                holder.name.text = list[position].name
//                holder.status.text = list[position].status
//                Picasso.get().load(list[position].profileImage).placeholder(R.drawable.default_avatar).into(holder.cover)
//
//                holder.parent.setOnClickListener {
//                    Log.d("chetan", "Position: $position")
//                }
//            }
//
//        }


        val adapter = object : FirebaseRecyclerAdapter<ClassroomKotlin, ClassViewHolder>(options) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
                Log.d("chetan", "View ttpe: ${viewType.toString()}")
                return ClassViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.classroom_single_layout, parent, false))
            }


            override fun onBindViewHolder(holder: ClassViewHolder, position: Int, model: ClassroomKotlin) {

                if(itemCount == 0){
                    main_empty.visibility = View.VISIBLE
                    main_class_list.visibility = View.GONE
                }else{
                    main_empty.visibility = View.GONE
                    main_class_list.visibility = View.VISIBLE
                }

                val id = getRef(position).key.toString()
                Log.d("chetan", "The Id is : $id")

                if (id == "null")
                    return

                val classListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

//                        if(dataSnapshot)
                        val imageUri = dataSnapshot.child("profileImage").value.toString()
                        val className = dataSnapshot.child("name").value.toString()
                        val classStatus = dataSnapshot.child("status").value.toString()
                        holder.setName(className)
                        holder.setStatus(classStatus)
                        holder.setProfileImage(imageUri)

                        Log.d("chetan", "You have $dataSnapshot")

                        holder.view.setOnClickListener {
                            Log.d("chetan", "You have click $className class")

                            sendToClassHomeActvity(id)
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        Log.d("chetan", "Firebase Error : ${p0.message}")
                    }
                }

                mClassroomReference.child(id).addValueEventListener(classListener as ValueEventListener)
//                holder.bind(model)
            }
        }

        Log.d("chetan", "Adapter")

        main_class_list.adapter = adapter

    }

    private fun sendToClassHomeActvity(id: String) {
//        val startIntent = Intent(this, ClassHomeActivity::class.java)
//        startIntent.putExtra("classId", id)
//        startActivity(startIntent)

        val chatIntent = Intent(this,PublicChatActivity::class.java)
//        chatIntent.putExtra(MainActivity.CLASSID,id)
        classId = id
        startActivity(chatIntent)
    }

    private fun sendToCreateClassActivity() {
        startActivity(Intent(this, CreateClassActivity::class.java))
    }

    private fun sendToHomePage() {
        startActivity(Intent(this, HomepageActivity::class.java))
        finish()
    }

    //Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onOptionsItemSelected(item)

        Log.d("chetan", "${item?.itemId} is clicked")

        item ?: return false

        when {
            item.itemId == R.id.main_logout_btn -> {
                FirebaseAuth.getInstance().signOut()
                sendToHomePage()
            }
            item.itemId == R.id.main_join_class_btn ->{
                startActivity(Intent(this,JoinClass::class.java))
            }


        }
        return true
    }

    public class ClassViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

//        val cover: ImageView = view.findViewById(R.id.class_single_image)
//        val name: TextView = view.findViewById(R.id.class_single_name)
//        val status: TextView = view.findViewById(R.id.class_single_status)
//        val parent: ConstraintLayout = view.findViewById(R.id.class_parent)

        fun bind(_class: ClassroomKotlin) {
            Log.d("chetan", "ClassViewHolder")
            with(_class) {
                view.class_single_name.text = _class.name
                view.class_single_status.text = _class.status
                val glide_image:Any = when(_class.profileImage){"default","null" -> R.drawable.default_avatar else -> _class.profileImage}
                Glide.with(view.class_single_image).load(glide_image).into(view.class_single_image)
            }
        }

        fun setName(string: String) {
            view.class_single_name.text = string
        }

        fun setStatus(string: String) {
            view.class_single_status.text = string
        }

        fun setProfileImage(string: String) {
            val glide_image:Any = when(string){"default","null" -> R.drawable.default_avatar else -> string}
            Glide.with(view.class_single_image).load(glide_image).into(view.class_single_image)

        }
    }

    companion object {
        const val CLASSID = "classId"
        public var  classId : String = "null"
    }


}