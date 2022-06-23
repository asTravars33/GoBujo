package com.wangjessica.jwfinalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.DragEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class EditJournalActivity extends AppCompatActivity implements AddWeekTaskDialogFragment.AddWeekTaskListener{

    // Layout components
    FrameLayout frame;
    FrameLayout framePartial;
    ConstraintLayout selectionFrame;
    ArrayList<ImageView> spreads;

    // Journal variables
    String journalKey;
    String pageKey;
    ArrayList<String> pageKeys;
    String curImgName = "_blank";
    boolean displayBlank = true;
    boolean isWeekPage = false;
    int curPageIdx;
    int dir = -1;
    String curTag = "";

    // Firebase
    DatabaseReference journalRef;
    DatabaseReference pagesRef;
    DatabaseReference curPage;
    FirebaseAuth auth;
    String uId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_journal);

        // Instantiate layout components
        frame = findViewById(R.id.journal_frame);
        selectionFrame = findViewById(R.id.fragment_frame);
        framePartial = findViewById(R.id.journal_frame_partial);

        String[] spreadLayouts = {"month_spreads", "week_spreads"};
        spreads = new ArrayList<ImageView>();
        for(String sId: spreadLayouts){
            LinearLayout subLayout = findViewById(getResources().getIdentifier(sId, "id", "com.wangjessica.jwfinalproject"));
            for(int i=0; i<subLayout.getChildCount(); i++){
                spreads.add((ImageView) subLayout.getChildAt(i));
            }
        }

        // Get the journal key
        Intent intent = getIntent();
        journalKey = intent.getStringExtra("journalKey");

        // Set the title
        String title = intent.getStringExtra("title");
        TextView titleView = findViewById(R.id.title);
        titleView.setText(title);

        // Instantiate Firebase info
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        uId = auth.getCurrentUser().getUid();
        journalRef = rootRef.child("Users").child(uId).child("Journals").child(journalKey);
        pagesRef = rootRef.child("Pages");

        // Keep a list of all the journal's pages
        pageKeys = new ArrayList<String>();
        instantiatePages();

        // Create all drag listeners
        initializeDragListeners();
    }
    public void instantiatePages(){
        journalRef.child("Pages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()) return;
                Iterator iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot next = (DataSnapshot) iterator.next();
                    String key = next.getValue().toString();
                    pagesRef.child(key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(displayBlank || !((DataSnapshot)snapshot.child("Type")).getValue().toString().equals("blank"))
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void displayPage(int id){
        curPageIdx = id;
        pageKey = pageKeys.get(id);
        curPage = pagesRef.child(pageKey);
        curPage.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Do actions based on the type
                String type = snapshot.child("Type").getValue().toString();
                switch(type){
                    case "month":
                        displayMonth(snapshot);
                        break;
                    case "week":
                        isWeekPage = true;
                        displayWeek(snapshot);
                        break;
                    case "blank":
                        displayBlank(snapshot);
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Move between pages
    public void prevPage(View view){
        curPageIdx--;
        dir = -1;
        if(curPageIdx<0)
            curPageIdx = 0;
        displayPage(curPageIdx);
    }
    public void nextPage(View view){
        curPageIdx++;
        dir = 1;
        if(curPageIdx >= pageKeys.size())
            curPageIdx = pageKeys.size()-1;
        displayPage(curPageIdx);
    }

    // Showing the spread choices
    public void showSelection(View view){
        selectionFrame.setVisibility(View.VISIBLE);
    }
    public void doneSelection(View view){
        selectionFrame.setVisibility(View.GONE);
    }

    // Dragging spreads onto the journal
    public void initializeDragListeners(){
        for(ImageView img: spreads){
            img.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    curImgName = img.getTag().toString();
                    ClipData.Item item = new ClipData.Item("spread");
                    ClipData dragData = new ClipData("spread", new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN }, item);
                    View.DragShadowBuilder shadow = new DragShadow(view);
                    view.startDragAndDrop(dragData, shadow, null, 0);
                    return true;
                }
            });
        }
        framePartial.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch(dragEvent.getAction()){
                    case DragEvent.ACTION_DRAG_STARTED:
                        System.out.println("Drag started");
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        System.out.println("Entered");
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        System.out.println("Exited");
                        return true;
                    case DragEvent.ACTION_DROP:
                        // Create a new page with this spread type
                        displayBlank = false;
                        addPage(null);
                        selectionFrame.setVisibility(View.GONE);
                        System.out.println("Dropped");
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        System.out.println("Ended");
                        return true;
                }
                return true;
            }
        });
    }

    @Override
    public void onDialogPositiveClick(String task) {
        WeekPageFragmentEdit fragment = (WeekPageFragmentEdit) getSupportFragmentManager().findFragmentByTag("Week");
        fragment.updateWithTask(task);
    }

    private class DragShadow extends View.DragShadowBuilder{
        Drawable shadow = ResourcesCompat.getDrawable(getResources(), getResources().getIdentifier(curImgName, "drawable", "com.wangjessica.jwfinalproject"), null);
        public DragShadow(View view){
            super(view);
        }
        @Override
        public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
            super.onProvideShadowMetrics(outShadowSize, outShadowTouchPoint);
            int width, height;
            width = getView().getWidth()/2;
            height = getView().getHeight()/2;
            shadow.setBounds(0, 0, width, height);
            outShadowSize.set(width, height);
            outShadowTouchPoint.set(width/2, height/2);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            shadow.draw(canvas);
        }
    }

    // Displaying different types of spreads
    public void displayMonth(DataSnapshot snapshot) {
        // Retrieve information
        String background = snapshot.child("Background").getValue().toString();
        int backgroundId = getResources().getIdentifier(background, "drawable", "com.wangjessica.jwfinalproject");
        String monthName = snapshot.child("Month").getValue().toString();

        // Replace the page fragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.journal_frame, MonthPageFragmentEdit.newInstance("month_edit", pageKey, monthName, backgroundId), pageKey);
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

        System.out.println("AHHHH DISPLAYING WEEK RN");

        // Update the page fragment
        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.journal_frame, WeekPageFragmentEdit.newInstance("week_edit", snapshot.getKey(), weekInfo, backgroundId), "Week");
            ft.commit();
        } catch(Exception e){}

        curTag = pageKey;
    }

    // Adding a new page
    public void addPage(View view){
        // Update database of pages in backend
        String newPageKey = pagesRef.push().getKey();
        pageKeys.add(newPageKey);
        journalRef.child("Pages").setValue(pageKeys);

        // Figure out what type of page it is
        int idx = curImgName.indexOf("_");
        String type = curImgName.substring(idx+1);
        switch(type){
            case "blank":
                addBlankPage(newPageKey);
                break;
            case "week":
                addWeekPage(newPageKey);
                break;
            case "month":
                addMonthPage(newPageKey);
                break;
        }

        // Display to the user the new blank page
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.journal_frame, BlankPageFragment.newInstance("blank", newPageKey), "Blank");
        ft.commit();

        // Update the location in the journal
        curPageIdx = pageKeys.size()-1;

        // Reset the page type
        curImgName = "_blank";
    }
    public void addBlankPage(String newPageKey){
        HashMap<String, Object> info = new HashMap<String, Object>();
            info.put("Type", "blank");
        displayBlank = true;
        DatabaseReference curRef = pagesRef.child(newPageKey);
        curRef.setValue(info);
    }
    public void addWeekPage(String newPageKey){
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        HashMap<String, Object> info = new HashMap<>();
            info.put("Type", "week");
            info.put("Background", getResources().getIdentifier(curImgName, "drawable", "com.wangjessica.jwfinalproject"));
            for(String day: days) info.put(day, "");
        DatabaseReference curRef = pagesRef.child(newPageKey);
        curRef.setValue(info);
    }
    public void addMonthPage(String newPageKey){
        HashMap<String, Object> info = new HashMap<>();
            info.put("Type", "month");
            info.put("Background", getResources().getIdentifier(curImgName, "drawable", "com.wangjessica.jwfinalproject"));
            info.put("Month", "Month Name");
        DatabaseReference curRef = pagesRef.child(newPageKey);
        curRef.setValue(info);
    }
    public void displayBlank(DataSnapshot snapshot){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.journal_frame, BlankPageFragment.newInstance("blank", ""), "Blank");
        ft.commitAllowingStateLoss();
    }
}