package com.wangjessica.jwfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CreateJournalDialogFragment.CreateJournalListener{

    // Layout variables
    RecyclerView recycler;
    RecyclerAdapter adapter;

    // Journal info
    String newImg = "none";
    String roomKey;
    String roomTitle;
    private boolean setToday;
    ImageView[] covers = new ImageView[3];

    // Firebase variables
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference journalRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Layout components
        recycler = findViewById(R.id.recycler_view);
        Intent i = getIntent();
        setToday = i.hasExtra("setToday")? true: false;
        if(setToday){
            roomKey = i.getStringExtra("roomKey");
            roomTitle = i.getStringExtra("title");
            TextView toastView = findViewById(R.id.temp_toast);
            toastView.setVisibility(View.VISIBLE);
        }

        // Firebase info
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child("Users").child(userId);
        journalRef = rootRef.child("Journals");

        // Instantiate the journal cards
        showJournals();
    }
    public void showJournals(){
        // Get the recycler view
        RecyclerView view = findViewById(R.id.recycler_view);

        // Create the recycler adapter
        userRef.child("Journals").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Create list of the user's current journals
                List<Journal> journalList = new ArrayList<Journal>();
                journalList.add(new Journal("Add New", R.drawable.plus, ""));

                // Add the journals to the list
                Iterator iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot journal = (DataSnapshot) iterator.next();
                    System.out.println("CURRENT JOURNAL: "+journal);
                    String curTitle = journal.child("Title").getValue().toString();
                    System.out.println("LE TITLE: "+curTitle);
                    int curImage = Integer.parseInt(journal.child("Cover").getValue().toString());
                    journalList.add(new Journal(curTitle, curImage, journal.getKey()));
                }

                // Display journals on RecyclerView
                adapter = new RecyclerAdapter(journalList);
                for(Journal journal: journalList)
                    System.out.println(journal.getTitle());
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(MainActivity.this, 3);
                recycler.setLayoutManager(layoutManager);
                adapter.setOnItemClickListener(new ClickListener<Journal>() {
                    @Override
                    public void onItemClick(Journal target) {
                        System.out.println("CLICKED: "+target.getTitle());
                        System.out.println(target.getJournalKey());
                        if(target.getTitle().equals("Add New")){
                            addJournal();
                        }
                        else{
                            Intent intent = new Intent(getApplicationContext(), ViewJournalActivity.class);
                            intent.putExtra("journalKey", target.getJournalKey());
                            intent.putExtra("title", target.getTitle());
                            intent.putExtra("setToday", setToday?"true":"false");
                            if(setToday){
                                intent.putExtra("roomKey", roomKey);
                                intent.putExtra("title", roomTitle);
                            }
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

    public void addJournal(){
        CreateJournalDialogFragment fragment = new CreateJournalDialogFragment();
        fragment.show(getSupportFragmentManager(), "Create Journal");
    }
    public void updateSelected(View view){
        newImg = view.getTag().toString();
        Toast.makeText(MainActivity.this, "Cover "+newImg.charAt(newImg.length()-1)+" selected!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDialogPositiveClick(String title) {
        // Error control - no name entered
        if(title==null || title.equals("")){
            title = "Journal";
        }
        // Update the user's list of journals in Firebase
        String nextJournalKey = userRef.child("Journals").push().getKey();
        String coverImg = newImg;
        if(newImg.equals("none")){
            coverImg = "default_journal";
        }
        int cover = getResources().getIdentifier(coverImg, "drawable", "com.wangjessica.jwfinalproject");
        HashMap<String, Object> info = new HashMap<String, Object>();
            info.put("Title", title);
            info.put("Cover", ""+cover);
            info.put("Pages", new ArrayList<String>());
        userRef.child("Journals").child(nextJournalKey).setValue(info);
        // Create the cover page
        String pageKey = makeMonthPage(title, cover);
        ArrayList<String> pages = new ArrayList<String>();
        pages.add(pageKey);
        userRef.child("Journals").child(nextJournalKey).child("Pages").setValue(pages);
    }

    public String makeMonthPage(String monthName, int image){
        DatabaseReference pagesRef = rootRef.child("Pages");
        String newKey = pagesRef.push().getKey();
        HashMap<String, Object> info = new HashMap<String, Object>();
            info.put("Type", "month");
            info.put("Background", ""+image);
            info.put("Month", monthName);
        DatabaseReference curRef = pagesRef.child(newKey);
        System.out.println("NEW KEY: "+newKey);
        curRef.setValue(info);
        return newKey;
    }

    // Moving between areas of the app
    public void startJournal(View view){}
    public void startWork(View view){
        Intent intent = new Intent(MainActivity.this, WorkActivity.class);
        intent.putExtra("setToday", setToday?"true":"false");
        startActivity(intent);
    }
    public void startSettings(View view){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}