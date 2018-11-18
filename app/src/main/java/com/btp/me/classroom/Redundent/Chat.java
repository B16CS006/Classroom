package com.btp.me.classroom.Redundent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.btp.me.classroom.R;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class Chat extends AppCompatActivity {
 /*   LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    DatabaseReference reference1, reference2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout)findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        Firebase.setAndroidContext(this);
        reference1 = new Firebase("https://androidchatapp-76776.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference2 = new Firebase("https://androidchatapp-76776.firebaseio.com/messages/" + UserDetails.chatWith + "_" + UserDetails.username);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if(!messageText.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", messageText);
                    map.put("user", UserDetails.username);
                    reference1.push().setValue(map);
                    reference2.push().setValue(map);
                    messageArea.setText("");
                }
            }
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String userName = map.get("user").toString();

                if(userName.equals(UserDetails.username)){
                    addMessageBox("You:-\n" + message, 1);
                }
                else{
                    addMessageBox(UserDetails.chatWith + ":-\n" + message, 2);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void addMessageBox(String message, int type){
        TextView textView = new TextView(Chat.this);
        textView.setText(message);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;

        if(type == 1) {
            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.bubble_in);
        }
        else{
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.bubble_out);
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }*/
}










//        val adapter = object : FirebaseRecyclerAdapter<ClassAttribute, ClassViewHolder>(options) {
//
//            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
//                Log.d("chetan", "View ttpe: ${viewType.toString()}")
//                return ClassViewHolder(LayoutInflater.from(parent.context)
//                        .inflate(R.layout.classroom_single_layout, parent, false))
//            }
//
//
//            override fun onBindViewHolder(holder: ClassViewHolder, position: Int, model: ClassAttribute) {
//                val id = getRef(position).key.toString()
//                Log.d("chetan", "The Id is : $id")
//
//                if (id == "null")
//                    return
//
//                val classListener = object : ValueEventListener {
//                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//
////                        if(dataSnapshot)
//                        val imageUri = dataSnapshot.child("profileImage").value.toString()
//                        val className = dataSnapshot.child("name").value.toString()
//                        val classStatus = dataSnapshot.child("status").value.toString()
//                        holder.setName(className)
//                        holder.setStatus(classStatus)
//                        holder.setProfileImage(imageUri)
//
//                        Log.d("chetan", "You have $dataSnapshot")
//
//                        holder.view.setOnClickListener {
//                            Log.d("chetan", "You have click $className class")
//
//                            sendToClassHomeActvity(id)
//                        }
//                    }
//
//                    override fun onCancelled(p0: DatabaseError) {
//                        Log.d("chetan", "Firebase Error : ${p0.message}")
//                    }
//                }
//
//                mClassroomReference.child(id).addValueEventListener(classListener as ValueEventListener)
////                holder.bind(model)
//            }
//
//            override fun onDataChanged() {
//                if(itemCount == 0)
//                    main_empty.visibility = View.VISIBLE
//                else
//                    main_empty.visibility = View.GONE
//            }
//        }
//
//        Log.d("chetan", "Adapter")
//
//        main_class_list.adapter = adapter