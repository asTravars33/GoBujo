package com.wangjessica.jwfinalproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class AddWeekTaskDialogFragment extends DialogFragment {

    AddWeekTaskListener listener;

    public interface AddWeekTaskListener{
        void onDialogPositiveClick(String task);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (AddWeekTaskListener) context;
    }
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set a custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.add_week_task_form, null);
        builder.setView(customLayout);
        // Create the AlertDialog object and return it
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String taskDesc = ((EditText) customLayout.findViewById(R.id.task_desc)).getText().toString();
                listener.onDialogPositiveClick(taskDesc);
            }
        });
        return builder.create();
    }
}