package com.wangjessica.jwfinalproject;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class MonthPageFragmentEdit extends Fragment {

    // Fragment state variables
    private String pageType;
    private String monthName;
    private String pageKey;
    private int background;

    // Layout components
    View view;

    // Firebase variables
    DatabaseReference pageRef;
    DatabaseReference journalRef;

    public static MonthPageFragmentEdit newInstance(String pageType, String pageKey, String monthName, int background){
        MonthPageFragmentEdit fragment = new MonthPageFragmentEdit();
        fragment.pageType = pageType;
        fragment.pageKey = pageKey;
        fragment.monthName = monthName;
        fragment.background = background;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.month_edit_spread, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Firebase info
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        pageRef = rootRef.child("Pages").child(pageKey);

        // Instantiate the proper text
        EditText monthNameEdit = view.findViewById(R.id.edit_month_name);
        monthNameEdit.setText(monthName);

        // Instantiate the background
        ImageView backgroundImg = view.findViewById(R.id.spread_background);
        backgroundImg.setBackgroundResource(background);

        monthNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String newMonthName = monthNameEdit.getText().toString();
                HashMap<String, Object> updateInfo = new HashMap<String, Object>();
                    updateInfo.put("Month", newMonthName);
                pageRef.updateChildren(updateInfo);
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
}
