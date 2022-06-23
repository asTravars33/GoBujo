package com.wangjessica.jwfinalproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class WeekPageFragmentEdit extends Fragment{

    // Page state variables
    private String pageType;
    private int background;
    private String pageKey;
    int dayId;
    private ArrayList<ArrayList<String>> info;

    // Layout components
    private LinearLayout[] weekLayouts = new LinearLayout[7];

    // Firebase info
    DatabaseReference pageRef;

    public static WeekPageFragmentEdit newInstance(String pageType, String pageKey, ArrayList<ArrayList<String>> info, int background){
        WeekPageFragmentEdit fragment = new WeekPageFragmentEdit();
        fragment.pageType = pageType;
        fragment.info = info;
        fragment.background = background;
        fragment.pageKey = pageKey;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.week_edit_spread, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Firebase info
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        pageRef = rootRef.child("Pages").child(pageKey);

        // Instantiate the proper text
        instantiateLayouts(view);

        // Set the background
        ImageView backgroundImg = view.findViewById(R.id.spread_background);
        backgroundImg.setBackgroundResource(background);

        // Add all the user's tasks
        for(int i=0; i<info.size(); i++){
            ArrayList<String> dayTasks = info.get(i);
            for(String task: dayTasks){
                addTask(task, i);
            }
        }
    }

    public void instantiateLayouts(View view){
        String[] days = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        for(int i=0; i<7; i++){
            weekLayouts[i] = view.findViewById(getResources().getIdentifier(days[i], "id", "com.wangjessica.jwfinalproject"));
            Button button = (Button) weekLayouts[i].getChildAt(0);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addNewTask(button);
                }
            });
        }
    }
    public void addTask(String task, int curDayId){
        // Add task "task" to the day of the week "dayId"
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView taskView = new TextView(getContext());
        taskView.setTextColor(Color.BLACK);
        taskView.setLayoutParams(params);
        taskView.setText(task.substring(0, task.length()-1));
        weekLayouts[curDayId].addView(taskView);
    }
    public void addNewTask(View view){
        // Day of the week?
        dayId = Integer.parseInt(view.getTag().toString());

        // Get the task from the user
        AddWeekTaskDialogFragment frag = new AddWeekTaskDialogFragment();
        frag.show(getActivity().getSupportFragmentManager(), "Add Weekday Task");
    }

    public void updateWithTask(String task){
        // Update the page info with the task
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ArrayList<String> tasks = new ArrayList<>();
        pageRef.child(days[dayId]).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot cur = (DataSnapshot) iterator.next();
                    tasks.add(cur.getValue().toString());
                }
                tasks.add(task+"N");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pageRef.child(days[dayId]).setValue(tasks);
            }
        }, 500);

        // Add the task to the ScrollView
        addTask(task, dayId);
    }
}
