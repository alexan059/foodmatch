package com.fancyfood.foodmatch.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class NoResultDialogFragment extends DialogFragment {

    private static final String TAG = NoResultDialogFragment.class.getSimpleName();

    // Interface for callback options exchange
    public interface NoResultDialogListener {
        void onNoResultDialogPositiveClick(NoResultDialogFragment dialog);
    }

    public NoResultDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use builder class for dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Set text messages
        builder.setTitle("Keine Ergebnisse!")
                .setMessage("Ändere den Radius oder deinen Standort, um neue Ergebnisse zu erhalten.")
                .setPositiveButton("Radius ändern", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Accept changes
                        listener.onNoResultDialogPositiveClick(NoResultDialogFragment.this);
                        Log.d(TAG, "Positive Click");
                    }
                })
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancel
                        NoResultDialogFragment.this.getDialog().cancel();
                        Log.d(TAG, "Negative Click");
                    }
                });

        // Return new dialog
        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the NoResultDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoResultDialogListener so we can send events to the host
            listener = (NoResultDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoResultDialogListener");
        }
    }
}
