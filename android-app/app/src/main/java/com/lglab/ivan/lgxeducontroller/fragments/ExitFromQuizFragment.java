package com.lglab.ivan.lgxeducontroller.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.lglab.ivan.lgxeducontroller.R;

public class ExitFromQuizFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Do you really want to exit from this page?");
        builder.setMessage("If you continue, you will lose all your progress.")
                .setPositiveButton("Yes", (dialog, id) -> {
                    this.getActivity().onBackPressed();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    this.getDialog().cancel();
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
