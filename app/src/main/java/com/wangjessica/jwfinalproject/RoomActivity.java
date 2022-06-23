package com.wangjessica.jwfinalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewRenderProcess;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class RoomActivity extends AppCompatActivity {

    // Layout components
    LinearLayout peopleLayout;
    LinearLayout namesLayout;
    LinearLayout chatLayout;
    LinearLayout tasksLayout;
    EditText messageTxt;
    TextView title;

    // Room info
    ArrayList<String> companions = new ArrayList<String>();
    String roomTitle;
    int roomCapacity;
    String roomKey;
    String roomName;

    // Firebase info
    DatabaseReference roomRef;
    DatabaseReference rootRef;
    DatabaseReference chatRef;
    StorageReference storageRef;
    FirebaseAuth auth;
    String uId;
    String myName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        // Layout components
        peopleLayout = findViewById(R.id.people_list);
        namesLayout = findViewById(R.id.people_names);
        chatLayout = findViewById(R.id.chat);
        messageTxt = findViewById(R.id.message_text);
        tasksLayout = findViewById(R.id.tasks_list_layout);
        TextView tasksClick = findViewById(R.id.hidden_clickable);
        title = findViewById(R.id.title);

        // Extract room key data
        Intent parentIntent = getIntent();
        roomKey = parentIntent.getStringExtra("roomKey");
        roomName = parentIntent.getStringExtra("title");
        title.setText(roomName);

        // Did the user come from ViewJournal?
        if(parentIntent.hasExtra("tasks")){
            displayTasks(parentIntent.getStringArrayListExtra("tasks"));
        }
        else{
            ArrayList<String> temp = new ArrayList<String>();
            temp.add("Click to select your tasks for today!");
            displayTasks(temp);
            // User can click to select their tasks
            System.out.println("Setting click listener");
            tasksClick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("Scroll was clicked");
                    redirectViewJournal(view);
                }
            });
        }

        // Firebase info
        auth = FirebaseAuth.getInstance();
        uId = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        roomRef = rootRef.child("Rooms").child(roomKey);
        chatRef = rootRef.child("Chats").child(roomKey);
        storageRef = FirebaseStorage.getInstance().getReference().child("Profiles");
        rootRef.child("Users").child(uId).child("Name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myName = snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Get a list of users in this room
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot curData = ((DataSnapshot) iterator.next());
                    String cur = curData.getKey();
                    if(cur.equals("Title")){
                        roomTitle = curData.getValue().toString();
                    }
                    else if(cur.equals("Capacity")){
                        roomCapacity = Integer.parseInt(curData.getValue().toString());
                    }
                    else{
                        companions.add(cur);
                    }
                }
                showCompanions();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        showMessages();
    }
    public void showMessages(){
        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Iterator iterator = snapshot.getChildren().iterator();
                String content = ((DataSnapshot) iterator.next()).getValue().toString();
                String sender = ((DataSnapshot) iterator.next()).getValue().toString();
                // Add the new message to the layout
                TextView tv = new TextView(RoomActivity.this);
                LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tv.setText("\n"+sender+": "+content+"\n");
                tv.setLayoutParams(lParams);
                chatLayout.addView(tv);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void showCompanions(){
        for(String userId: companions){
            DatabaseReference userRef = rootRef.child("Users").child(userId);
            downloadImage(userId+".jpg");
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = "";
                    if(userId.equals(uId)){
                        name = "Me";
                    }
                    else if(snapshot.hasChild("Name")){
                        name = ((DataSnapshot) snapshot.child("Name")).getValue().toString();
                    }
                    // Add the text view with the image
                    TextView tView = new TextView(RoomActivity.this);
                    System.out.println("Added name: "+name);
                    LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(300, ViewGroup.LayoutParams.WRAP_CONTENT);
                    tView.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    tView.setTextSize(17);
                    tView.setText(name);
                    tView.setLayoutParams(lParams);
                    namesLayout.addView(tView);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    public void sendMessage(View view){
        // Get the message from the EditText
        String msg = messageTxt.getText().toString();
        if(msg!=null && !msg.equals("")){
            // Update firebase with the message
            String newKey = chatRef.push().getKey();
            HashMap<String, Object> msgInfo = new HashMap<String, Object>();
                msgInfo.put("Content", msg);
                msgInfo.put("Sender", myName);
            chatRef.child(newKey).updateChildren(msgInfo);
        }
    }
    public void downloadImage(String path){
        System.out.println("Downloading image: "+path);
        // Download the byte array from the path
        StorageReference imgRef = storageRef.child(path);
        System.out.println("Image ref: "+imgRef);
        ImageView img = new ImageView(RoomActivity.this);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(300, 400);
        img.setLayoutParams(lParams);
        imgRef.getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Convert the byte array to an image view to display
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                img.setImageBitmap(bitmap);
                img.setScaleType(ImageView.ScaleType.CENTER);
                peopleLayout.addView(img);
                return;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Use the default image
                img.setBackgroundResource(R.drawable.defaultpfp);
                img.setScaleType(ImageView.ScaleType.CENTER);
                peopleLayout.addView(img);
                return;
            }
        });
    }

    public void redirectViewJournal(View view) {
        // Make a toast telling the user what to do
        // TODO: DO THIS
        // Redirect user to journal selection
        Intent intent = new Intent(RoomActivity.this, MainActivity.class);
        intent.putExtra("setToday", "true");
        intent.putExtra("roomKey", roomKey);
        intent.putExtra("title", roomName);
        startActivity(intent);
    }
    public void displayTasks(ArrayList<String> tasks){
        for(String task: tasks) {
            LinearLayout.LayoutParams lP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView view = new TextView(RoomActivity.this);
            view.setLayoutParams(lP);
            view.setTextSize(17);
            view.setText(task.substring(0, task.length()-1));
            tasksLayout.addView(view);
        }
    }
}