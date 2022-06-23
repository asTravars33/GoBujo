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

public class CreateRoomDialogFragment extends DialogFragment {

    CreateRoomListener listener;

    public interface CreateRoomListener{
        void onDialogPositiveClick(String title, int capacity);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (CreateRoomListener) context;
    }
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set a custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.create_room_form, null);
        builder.setView(customLayout);
        // Create the AlertDialog object and return it
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String curTitle = ((EditText) customLayout.findViewById(R.id.room_name)).getText().toString();
                int capacity = Integer.parseInt(((EditText)customLayout.findViewById(R.id.capacity)).getText().toString());
                listener.onDialogPositiveClick(curTitle, capacity);
            }
        });
        return builder.create();
    }
}