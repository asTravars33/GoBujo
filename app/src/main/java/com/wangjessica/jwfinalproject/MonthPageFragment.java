package com.wangjessica.jwfinalproject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class MonthPageFragment extends Fragment {

    private String pageType;
    private String monthName;
    private int background;
    View view;

    public static MonthPageFragment newInstance(String pageType, String monthName, int background){
        MonthPageFragment fragment = new MonthPageFragment();
        fragment.pageType = pageType;
        fragment.monthName = monthName;
        fragment.background = background;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.month_spread, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Instantiate the proper text
        TextView monthNameView = view.findViewById(R.id.month_name);
        monthNameView.setText(monthName);
        System.out.println("MONTH NAME: "+monthName);

        // Set the background
        ImageView backgroundImg = view.findViewById(R.id.spread_background);
        backgroundImg.setBackgroundResource(background);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
}
