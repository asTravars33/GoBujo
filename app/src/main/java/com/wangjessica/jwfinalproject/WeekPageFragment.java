package com.wangjessica.jwfinalproject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class WeekPageFragment extends Fragment {

    // Fragment and layout info
    private String pageType;
    private boolean setToday;
    private int background;
    private ArrayList<ArrayList<String>> info;
    private LinearLayout[] weekLayouts = new LinearLayout[7];
    private ScrollView[] scrolls = new ScrollView[7];

    // Interface
    private UpdateTaskProgressHandler handler;
    private SendToWorkHandler workHandler;

    public static WeekPageFragment newInstance(String pageType, ArrayList<ArrayList<String>> info, int background, boolean setToday){
        WeekPageFragment fragment = new WeekPageFragment();
        fragment.pageType = pageType;
        fragment.info = info;
        fragment.background = background;
        fragment.setToday = setToday;
        System.out.println("WEEK page fragment created: "+setToday);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.week_spread, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            handler = (UpdateTaskProgressHandler) context;
        } catch(Exception e){}
        try{
            workHandler = (SendToWorkHandler) context;
        } catch(Exception e){}
    }

    public void instantiateLayouts(View view){
        String[] days = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        for(int i=0; i<7; i++){
            weekLayouts[i] = view.findViewById(getResources().getIdentifier(days[i], "id", "com.wangjessica.jwfinalproject"));
            TextView clicky = view.findViewById(getResources().getIdentifier(days[i]+"_scroll_hidden", "id", "com.wangjessica.jwfinalproject"));
            System.out.println(clicky);
            // For use with "Work"
            if(setToday){
                final int j = i;
                clicky.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        markToday(j);
                    }
                });
            }
        }
    }
    public void addTask(String task, int dayId){
        // Add task "task" to the day of the week "dayId"
        char progress = task.charAt(task.length()-1);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView taskView = new TextView(getContext());
        taskView.setLayoutParams(params);
        taskView.setTextSize(16f);
        taskView.setText(task.substring(0, task.length()-1));
        if(progress=='Y')
            taskView.setTextColor(getResources().getColor(R.color.gray, null));
        else
            taskView.setTextColor(Color.BLACK);
        taskView.setTag(""+progress);
        taskView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView tV = (TextView) view;
                char prog = tV.getTag().toString().charAt(0);
                if(prog=='Y')
                    tV.setTextColor(Color.BLACK);
                else
                    tV.setTextColor(getResources().getColor(R.color.gray, null));
                updateTaskProg(task, prog=='Y'?'N':'Y', dayId);
            }
        });
        weekLayouts[dayId].addView(taskView);
    }
    public void updateTaskProg(String task, char suffix, int day){
        ArrayList<String> subList = info.get(day);
        int taskInd = subList.indexOf(task);
        subList.set(taskInd, task.substring(0, task.length()-1)+suffix);
        handler.updateTask(subList, day);
    }
    public interface UpdateTaskProgressHandler{
        void updateTask(ArrayList<String> taskList, int day);
    }
    // For use with the "Work" tab
    public void markToday(int idx){
        System.out.println("Mark today called!");
        ArrayList<String> curTasks = info.get(idx);
        workHandler.sendToWork(curTasks);
    }
    public interface SendToWorkHandler{
        void sendToWork(ArrayList<String> userTasks);
    }
}
