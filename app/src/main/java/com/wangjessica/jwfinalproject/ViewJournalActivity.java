package com.wangjessica.jwfinalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class ViewJournalActivity extends AppCompatActivity implements WeekPageFragment.UpdateTaskProgressHandler, WeekPageFragment.SendToWorkHandler{

    // Layout components
    FrameLayout frame;

    // Journal variables
    String journalKey;
    String title;
    ArrayList<String> pageKeys;
    int curPageIdx;

    // Did the user come from work?
    String roomKey;
    String roomTitle;
    boolean setToday;

    // Firebase
    DatabaseReference journalRef;
    DatabaseReference pagesRef;
    DatabaseReference curPage;
    FirebaseAuth auth;
    String uId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_journal);

        // Instantiate layout components
        frame = findViewById(R.id.journal_frame);

        // Get the journal key
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        for(String key: bundle.keySet()){
            System.out.println(key+" "+bundle.get(key).toString());
        }
        journalKey = intent.getStringExtra("journalKey");

        // Connection to Work?
        setToday = intent.getStringExtra("setToday").equals("true")? true:false;
        if(setToday){
            roomKey = intent.getStringExtra("roomKey");
            roomTitle = intent.getStringExtra("title");
            TextView toastView = findViewById(R.id.temp_toast);
            toastView.setVisibility(View.VISIBLE);
        }

        // Set the name
        title = intent.getStringExtra("title");
        TextView titleView = findViewById(R.id.title);
        titleView.setText(title);
        System.out.println("JOURNAL KEY: "+journalKey);

        // Instantiate Firebase info
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        uId = auth.getCurrentUser().getUid();
        journalRef = rootRef.child("Users").child(uId).child("Journals").child(journalKey);
        pagesRef = rootRef.child("Pages");

        // Keep a list of all the journal's pages
        pageKeys = new ArrayList<String>();
        instantiatePages();
        journalRef.child("Pages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                pageKeys.add(snapshot.toString());
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
    public void instantiatePages(){
        journalRef.child("Pages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println(snapshot);
                if(!snapshot.exists()) return;
                Iterator iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot next = (DataSnapshot) iterator.next();
                    String key = next.getValue().toString();
                    pagesRef.child(key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!((DataSnapshot)snapshot.child("Type")).getValue().toString().equals("blank"))
                                if(!pageKeys.contains(key)) pageKeys.add(key);
                            // Display the last page
                            displayPage(pageKeys.size()-1);
                            curPageIdx = pageKeys.size()-1;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                for(String s: pageKeys) System.out.println(s);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void displayPage(int id){
        String pageKey = pageKeys.get(id);
        curPage = pagesRef.child(pageKey);
        curPage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Do actions based on the type
                String type = snapshot.child("Type").getValue().toString();
                System.out.println(type);
                switch(type){
                    case "month":
                        displayMonth(snapshot);
                        break;
                    case "week":
                        displayWeek(snapshot);
                        break;
                    case "blank":
                        displayBlank(snapshot);
                }
                curPageIdx = id;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void editJournal(View view) {
        Intent intent = new Intent(ViewJournalActivity.this, EditJournalActivity.class);
        intent.putExtra("journalKey", journalKey);
        intent.putExtra("title", title);
        intent.putExtra("setToday", "false");
        startActivity(intent);
    }

    // Move between pages
    public void prevPage(View view){
        curPageIdx--;
        if(curPageIdx<0)
            curPageIdx = 0;
        displayPage(curPageIdx);
    }
    public void nextPage(View view){
        curPageIdx++;
        if(curPageIdx >= pageKeys.size())
            curPageIdx = pageKeys.size()-1;
        displayPage(curPageIdx);
    }

    // Displaying different types of spreads
    public void displayMonth(DataSnapshot snapshot){
        // Retrieve information
        String background = snapshot.child("Background").getValue().toString();
        int backgroundId = getResources().getIdentifier(background, "drawable", "com.wangjessica.jwfinalproject");
        String monthName = snapshot.child("Month").getValue().toString();

        // Replace the page fragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.journal_frame, MonthPageFragment.newInstance("month", monthName, backgroundId), "month");
        ft.commitAllowingStateLoss();
    }
    public void displayWeek(DataSnapshot snapshot){
        // Retrieve information
        String background = snapshot.child("Background").getValue().toString();
        int backgroundId = getResources().getIdentifier(background, "drawable", "com.wangjessica.jwfinalproject");

        String[] weekDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ArrayList<ArrayList<String>> weekInfo = new ArrayList<ArrayList<String>>();

        for(String day: weekDays){
            Iterator iterator = snapshot.child(day).getChildren().iterator();
            ArrayList<String> local = new ArrayList<String>();
            while(iterator.hasNext()){
                DataSnapshot cur = (DataSnapshot) iterator.next();
                local.add(cur.getValue().toString());
            }
            weekInfo.add(local);
        }

        // Update the page fragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.journal_frame, WeekPageFragment.newInstance("week", weekInfo, backgroundId, setToday), "Week");
        ft.commitAllowingStateLoss();
    }
    public void displayBlank(DataSnapshot snapshot){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.journal_frame, BlankPageFragment.newInstance("blank", ""), "Blank");
        ft.commitAllowingStateLoss();
    }

    @Override
    public void updateTask(ArrayList<String> taskList, int day) {
        String[] weekDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        curPage.child(weekDays[day]).setValue(taskList);
    }
    @Override
    public void sendToWork(ArrayList<String> userTasks) {
        Intent intent = new Intent(ViewJournalActivity.this, RoomActivity.class);
        intent.putExtra("roomKey", roomKey);
        intent.putExtra("title", roomTitle);
        intent.putExtra("tasks", userTasks);
        System.out.println("In send to work: roomKey-"+roomKey);
        startActivity(intent);
    }
}