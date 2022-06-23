package com.wangjessica.jwfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class WorkActivity extends AppCompatActivity implements CreateRoomDialogFragment.CreateRoomListener{

    // Layout components
    RecyclerView recycler;
    RoomsRecyclerAdapter adapter;

    // Firebase variables
    DatabaseReference roomsRef;
    DatabaseReference userRef;
    FirebaseAuth auth;
    String uId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        // Firebase info
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child("Users");
        auth = FirebaseAuth.getInstance();
        uId = auth.getCurrentUser().getUid();
        roomsRef = rootRef.child("Rooms");
        System.out.println(roomsRef);

        // Layout components
        recycler = findViewById(R.id.recycler_view);

        showRooms();
    }
    public void showRooms() {
        // Get the recycler view
        RecyclerView view = findViewById(R.id.recycler_view);

        // Create the recycler adapter
        System.out.println(roomsRef);
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Create list of the user's current journals
                List<Room> roomsList = new ArrayList<Room>();
                roomsList.add(new Room("Create A Room", R.drawable.plus, ""));

                // Add the journals to the list
                Iterator iterator = snapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    DataSnapshot room = (DataSnapshot) iterator.next();
                    String curTitle = room.child("Title").getValue().toString();
                    if (curTitle == null || curTitle.equals(""))
                        curTitle = "Untitled";
                    int capacity = Math.min(50, Integer.parseInt(room.child("Capacity").getValue().toString()));
                    roomsList.add(new Room(curTitle, capacity, room.getKey()));
                }

                // Display journals on RecyclerView
                adapter = new RoomsRecyclerAdapter(roomsList);
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(WorkActivity.this, 3);
                recycler.setLayoutManager(layoutManager);
                adapter.setOnItemClickListener(new ClickListener<Room>() {
                    @Override
                    public void onItemClick(Room target) {
                        System.out.println("CLICKED: " + target.getTitle());
                        if (target.getTitle().equals("Create A Room")) {
                            addRoom();
                        } else {
                            String key = target.getRoomKey();
                            // Add current user to this room
                            HashMap<String, Object> userInfo = new HashMap<String, Object>();
                                userInfo.put(uId, "");
                            roomsRef.child(key).updateChildren(userInfo);
                            // Send current user to that room
                            Intent intent = new Intent(WorkActivity.this, RoomActivity.class);
                            intent.putExtra("roomKey", key);
                            intent.putExtra("title", target.getTitle());
                            startActivity(intent);
                        }
                    }
                });
                recycler.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void addRoom(){
        CreateRoomDialogFragment fragment = new CreateRoomDialogFragment();
        fragment.show(getSupportFragmentManager(), "Create Room");
    }

    @Override
    public void onDialogPositiveClick(String title, int capacity) {
        String roomKey = roomsRef.push().getKey();
        HashMap<String, Object> roomInfo = new HashMap<String, Object>();
            roomInfo.put("Title", title);
            roomInfo.put("Capacity", capacity);
            roomInfo.put(uId, "");
        roomsRef.child(roomKey).updateChildren(roomInfo);
    }

    // Navigate between activities
    public void startJournal(View view){
        Intent intent = new Intent(WorkActivity.this, MainActivity.class);
        startActivity(intent);
    }
    public void startWork(View view){
    }
    public void startSettings(View view){
        Intent intent = new Intent(WorkActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}