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

public class CreateJournalDialogFragment extends DialogFragment {

    CreateJournalListener listener;

    public interface CreateJournalListener{
        public void onDialogPositiveClick(String title);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (CreateJournalListener) context;
    }
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set a custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.create_journal_form, null);
        builder.setView(customLayout);
        // Create the AlertDialog object and return it
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String curTitle = ((EditText) customLayout.findViewById(R.id.journal_name)).getText().toString();
                listener.onDialogPositiveClick(curTitle);
            }
        });
        return builder.create();
    }
}